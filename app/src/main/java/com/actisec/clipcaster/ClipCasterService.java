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

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

import com.actisec.clipcaster.parser.ClipParser;
import com.actisec.clipcaster.parser.Parsers;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * @author Xiao Bao Clark
 */
public class ClipCasterService extends Service {

    public static ClipboardManager sClipboardManager;


    private ClipboardManager.OnPrimaryClipChangedListener mListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            GlobalClipParser.onClipEvent(ClipCasterService.this, sClipboardManager);
        }
    };



    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        return START_STICKY;
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public void onCreate(){
        toast(this, "ClipCaster service starting", false);
        startForeground(42, createOngoingNotification());
        sClipboardManager = getManager();
        getManager().addPrimaryClipChangedListener(mListener);
        getManager().addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                //Do things here
            }
        });
    }

    private Notification createOngoingNotification() {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_launcher).setTicker("Monitoring clipboard").setContentTitle(getString(R.string.app_name)).setContentText("Monitoring clipboard");
        builder.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, AboutActivity.class), 0));
        builder.setPriority(Notification.PRIORITY_MIN);
        builder.setOngoing(true);
        return builder.build();
    }

    @Override
    public void onDestroy(){
        toast(this, "ClipCaster service stopping", false);
        stopForeground(true);
        getManager().removePrimaryClipChangedListener(mListener);
    }

    public static void toast(final Context context, final String message, final boolean showLonger){
        new Handler(context.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                android.widget.Toast.makeText(context, message, (showLonger ? android.widget.Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
            }
        });
    }

    private ClipboardManager getManager(){
        return (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public ClipboardManager getManager(Context context){
        return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }

    public void foo(Context context){
        ClipboardManager manager = getManager(context);
        CharSequence textOnClipboard = manager.getPrimaryClip().getItemAt(0).coerceToText(context);
    }


}
