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

package com.actisec.clipcaster;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.test.ActivityInstrumentationTestCase2;
import android.test.AndroidTestCase;
import android.webkit.ConsoleMessage;
import android.webkit.ValueCallback;

import com.actisec.clipcaster.util.JavaScript;
import com.actisec.clipcaster.util.ThreadUtil;
import com.evgenii.jsevaluator.JsEvaluator;
import com.evgenii.jsevaluator.interfaces.JsCallback;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by xiao on 19/12/14.
 */
public class AbstractJavaScriptTestCase extends AndroidTestCase {

    protected static final String FACEBOOK_URL ="https://m.facebook.com/?refsrc=https%3A%2F%2Fwww.facebook.com%2F&_rdr";
//    protected Activity mActivity;
    protected TestUtilsStatic.TestUtils mTestUtils;

    protected AbstractJavaScriptTestCase() {

    }


    @Override
    public void setUp() throws Exception {
        super.setUp();
        mTestUtils = new TestUtilsStatic.TestUtils(getContext());
//        mActivity = getActivity();
    }

    public Context getContext(){
        return super.getContext();
    }

    protected String evaluate(final String program) throws Exception {

        final String[] result = new String[1];

        HandlerThread t = new HandlerThread("test_thread");
        t.start();
        new Handler(t.getLooper()).post(new Runnable() {
            @Override
            public void run() {
                JavaScript.evaluate(getContext(), program, new JsCallback() {
                    @Override
                    public void onResult(String s) {
                        synchronized (result) {
                            result[0] = s;
                            result.notifyAll();
                        }
                    }
                });
            }
        });

        synchronized (result){
            if(result[0] == null){
                result.wait(1000);
                if(result[0] == null) {
                    throw new TimeoutException();
                }
            }
            return result[0];
        }


    }
}
