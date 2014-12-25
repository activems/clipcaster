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

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actisec.clipcaster.AbstractJavaScriptTestCase;
import com.actisec.clipcaster.util.JavaScript;
import com.actisec.clipcaster.util.ThreadUtil;
import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class JavaScriptTest extends AbstractJavaScriptTestCase {


    public void testTestReadingTimeFromFile() throws Throwable{
        String input = mTestUtils.readString(com.actisec.clipcaster.test.R.raw.number);

        assertEquals(1418693814750L,Long.parseLong(input));
    }

    public void testTestEscapedChars() throws Throwable {
        String source = mTestUtils.readString(com.actisec.clipcaster.test.R.raw.escapedprotocol);
        assertTrue(StringEscapeUtils.escapeJava(source), source.contains("/https?:\\/\\//"));
    }
    public void testTestMainThreadRuns() throws Throwable {
        final boolean[] result = new boolean[1];
        ThreadUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                synchronized (result){
                    result[0] = true;
                }
            }
        });

        synchronized (result) {
            if (!result[0]) {
                result.wait(1000);

            }
            assertTrue(result[0]);
        }
    }
    public void testEvaluate_ctorOnNotMain_evalOnNotMain() throws Exception {
        HandlerThread thread = new HandlerThread("test_thread");
        thread.start();
        Handler handler = new Handler(thread.getLooper());
        final JsEvaluator[] evaluator = new JsEvaluator[1];
        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (evaluator){
                    evaluator[0] = new JsEvaluator(getContext());
                }
            }
        });

        Thread.sleep(2000);
        assertNotNull(evaluator[0]);
        final String[] result = new String[1];
        handler.post(new Runnable() {
            @Override
            public void run() {
                synchronized (evaluator){
                    evaluator[0].evaluate("2 + 3", new JsCallback() {
                        @Override
                        public void onResult(String value) {
                            synchronized (result){
                                result[0] = value;
                            }
                        }
                    });
                }
            }
        });

        Thread.sleep(2000);
        assertNotNull(result[0]);
        assertEquals(5, Integer.parseInt(result[0]));

    }

    public void testGetFunction() throws Throwable {
        String fromFile =  mTestUtils.readString(com.actisec.clipcaster.test.R.raw.ls);
        JavaScript javaScript = new JavaScript(fromFile);
        String lsfunc = javaScript.getFunction("l_s");
        assertNotNull(lsfunc);
        assertEquals("l_s=function(c){var l=0,a=0,f=[],m=[1732584193,4023233417,2562383102,271733878,3285377520],b,d,g,h,p,e,n=[],k=unescape(encodeURI(c));for(b=k.length;a<=b;)n[a>>2]|=(k.charCodeAt(a)||128)<<8*(3-a++%4);for(n[c=(b+8>>6<<4)+15]=b<<3;l<=c;l+=16){b=m;for(a=0;80>a;b=[[(e=((k=b[0])<<5|k>>>27)+b[4]+(f[a]=16>a?~~n[l+a]:e<<1|e>>>31)+1518500249)+((d=b[1])&(g=b[2])|~d&(h=b[3])),p=e+(d^g^h)+341275144,e+(d&g|d&h|g&h)+882459459,p+1535694389][0|a++/20]|0,k,d<<30|d>>>2,g,h])e=f[a-3]^f[a-8]^f[a-14]^f[a-16];for(a=5;a;)m[--a]=m[a]+b[a]|0}for(c='';40>a;)c+=(m[a>>3]>>4*(7-a++%8)&15).toString(16);return c}", lsfunc.trim());

    }
    public void testGetParameters() throws Throwable {
        String fromFile = mTestUtils.readString(com.actisec.clipcaster.test.R.raw.params);
        JavaScript javaScript = new JavaScript(fromFile);
        String[][] expected = new String[][]{ new String[] {"atob('QBYEQHQESFcIEgkAGlQNCA==')", "61","4"},
        new String[]{"atob('RVESQUNRQlI=')", "61","4"}};
        for(int ordinal = 0; ordinal < 2; ordinal ++){
            String[] lxParams = javaScript.getParams("l_x", ordinal);
            assertNotNull(lxParams);
            assertTrue(Arrays.toString(expected[ordinal]) +  " vs "  + Arrays.toString(lxParams),
                    Arrays.equals(expected[ordinal],lxParams));
        }
    }

    public void testAppendParameter() throws Throwable {
        JavaScript javaScript = new JavaScript("var func1(param1,param2) { console.log(param1); }; func1('Hello', 'World');");

        String result = javaScript.appendParameter("func1","param3");
        assertTrue(result, result.contains("func1(param1,param2,param3) "));
    }

    public void testRunJs() throws Throwable {
        final String js = "2 + 17";
        String result = evaluate(js);
        assertNotNull(result);
        assertEquals(19L, Long.parseLong(result));

    }
    public void testRunJsTime() throws Throwable {
        String js = "new Date().getTime().toFixed()";
        final long timeWhenStarted = System.currentTimeMillis();

        String result = evaluate(js);
        final long TOLERANCE = 1000; // Shouldn't take more than a second to run
        assertTrue(Math.abs(Long.parseLong(result) - timeWhenStarted) < TOLERANCE);

    }

    public void testRunTwoFunction() throws Throwable {
        String js = "var func1 = function(){ new Date().getTime().toFixed()}; var func2 = function() { return 3 }; func2();";


        String result = evaluate(js);
        assertEquals(3, Integer.parseInt(result));

    }

    final static String FULL_PROGRAM = "var com_actisec_clipcaster_injectedurl= atob('aHR0cHM6Ly9tLmZhY2Vib29rLmNvbS8/cmVmc3JjPWh0dHBzJTNBJTJGJTJGd3d3LmZhY2Vib29rLmNvbSUyRiZfcmRy');Date.prototype.getTime = function(){return 1419231136224; };l_f=function(m){ var t=new Date().getTime() / 1000 | 0; while(t % 10!=m){--t;} return t;};l_s=function(c){var l=0,a=0,f=[],m=[1732584193,4023233417,2562383102,271733878,3285377520],b,d,g,h,p,e,n=[],k=unescape(encodeURI(c));for(b=k.length;a<=b;)n[a>>2]|=(k.charCodeAt(a)||128)<<8*(3-a++%4);for(n[c=(b+8>>6<<4)+15]=b<<3;l<=c;l+=16){b=m;for(a=0;80>a;b=[[(e=((k=b[0])<<5|k>>>27)+b[4]+(f[a]=16>a?~~n[l+a]:e<<1|e>>>31)+1518500249)+((d=b[1])&(g=b[2])|~d&(h=b[3])),p=e+(d^g^h)+341275144,e+(d&g|d&h|g&h)+882459459,p+1535694389][0|a++/20]|0,k,d<<30|d>>>2,g,h])e=f[a-3]^f[a-8]^f[a-14]^f[a-16];for(a=5;a;)m[--a]=m[a]+b[a]|0}for(c='';40>a;)c+=(m[a>>3]>>4*(7-a++%8)&15).toString(16);return c};l_x=function(t,l,m){ var o=[]; var b=''; var p=com_actisec_clipcaster_injectedurl.replace(/https?:\\/\\//, '').substring(0,l); p=l_s(''+l_f(m)+p); for (z=1; z<=255; z++){o[String.fromCharCode(z)]=z;} for (j=z=0; z<t.length; z++){ b+=String.fromCharCode(o[t.substr(z, 1)]^o[p.substr(j, 1)]); j=(j<p.length)?j+1:0; } return decodeURIComponent(escape(b));};l_x(atob('FkRSQHcDSwdaSVRcTVoJWg=='),61,5);";
    public void testRunSampleProgram() throws Exception {

        assertEquals("user@example.com",evaluate(FULL_PROGRAM));
    }



}