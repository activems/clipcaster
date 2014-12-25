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

package com.actisec.clipcaster.util;

import android.os.Handler;
import android.os.Looper;

/**
 * Created by xiao on 23/12/14.
 */
public class ThreadUtil {
    private ThreadUtil() {}

    public static void runOnThread(Runnable runnable, Looper threadLooper){
        if(Looper.myLooper() == threadLooper){
            runnable.run();
        } else {
            new Handler(threadLooper).post(runnable);
        }

    }

    public static void runOnThread(Runnable runnable, Handler threadHandler){
        if(onThread(threadHandler)){
            runnable.run();
        } else {
            threadHandler.post(runnable);
        }

    }

    public static void runOnMainThread(Runnable runnable){
        runOnThread(runnable,Looper.getMainLooper());
    }

    public static boolean onThread(Thread thread){
        return Thread.currentThread().equals(thread);
    }


    public static boolean onThread(Looper looper){
        return Thread.currentThread().equals(looper.getThread());
    }

    public static boolean onThread(Handler handler){
        return Thread.currentThread().equals(handler.getLooper().getThread());
    }
}
