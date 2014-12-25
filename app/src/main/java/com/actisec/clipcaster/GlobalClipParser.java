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
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Debug;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.util.Pair;

import com.actisec.clipcaster.parser.ClipParser;
import com.actisec.clipcaster.parser.Parsers;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by xiao on 9/12/14.
 */
public class GlobalClipParser{
    public static final String CLIPLOG_FILENAME = "clip.log";
    public static final String CREDLOG_FILENAME = "creds.log";
    public static final List<String> mClips = new ArrayList<String>();
    public static final List<ScrapedData> mData = new ArrayList<ScrapedData>();

    public static void onClipEvent(final Context context, ClipboardManager manager){
        Log.d("ClipCaster", "" + System.currentTimeMillis());
        final ClipData primaryClip = getManager(context).getPrimaryClip();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < primaryClip.getItemCount(); i++){
            builder.append(primaryClip.getItemAt(i).coerceToText(context));
            if(i != primaryClip.getItemCount() - 1){
                builder.append('\n');
            }
        }
        final String text = builder.toString();
        mClips.add(text);
        for(ClipParser parser : Parsers.getClipParsers()){
            parser.onClip(context, new ScrapedDataHandler(context), text);
        }
        onClipDebug(context,text);
    }


    private static void onClipDebug(Context context, final String text){
        if(Debug.isDebuggerConnected()) {
            System.out.println(text);
            writeToFile(context, CLIPLOG_FILENAME, text);
        }
    }

    private static void onCredDebug(Context context, final ScrapedCredentials credentials){
        if(Debug.isDebuggerConnected()) {
            System.out.println(credentials.toString());
            writeToFile(context, CREDLOG_FILENAME, credentials.toString());
        }
    }

    private static String currentTimestamp(){
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(cal.getTime());
    }


    private static void writeToFile(Context context, String name, String text){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new PrintWriter(context.openFileOutput(name, Context.MODE_APPEND)));
            writer.write(currentTimestamp() + ": " + text);
            writer.newLine();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static class ScrapedDataHandler implements com.actisec.clipcaster.ScrapedDataHandler {

        private final Context mContext;

        private ScrapedDataHandler(Context context) {
            mContext = context;
        }

        @Override
        public synchronized void handleData(ScrapedData scrapedData) {
            if(scrapedData != null) {
                mData.add(scrapedData);
                if(scrapedData.creds != null) {
                    postNotification(scrapedData);

                    onCredDebug(mContext, scrapedData.creds);
                }
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        private void postNotification(ScrapedData scrapedData){

            ScrapedCredentials credentials = scrapedData.creds;
            Notification.Builder builder = new Notification.Builder(mContext);
            builder.setContentTitle(mContext.getString(R.string.creds_notif_title));
            builder.setSmallIcon(R.drawable.ic_launcher);
            Spanned contentText, contentTextBig;

            final ArrayList<Pair<Integer, String>> pairsToDisplay = new ArrayList<>();
            if(credentials.unknown != null){
                pairsToDisplay.add(new Pair<>(R.string.cred,credentials.unknown));
            } else {
                pairsToDisplay.add(new Pair<>(R.string.user,credentials.user));
                pairsToDisplay.add(new Pair<>(R.string.pass,credentials.pass));
            }
            if(scrapedData.destinationUrl != null){
                pairsToDisplay.add(new Pair<>(R.string.url,scrapedData.destinationUrl));
            }
            Pair<Spanned,Spanned> spannedPair = getSpannedFromCreds(mContext,pairsToDisplay);
            contentText = spannedPair.first;
            contentTextBig = spannedPair.second;


            builder.setContentText(contentText);
            builder.setContentIntent(PendingIntent.getActivity(mContext, 0, new Intent(mContext, ClipboardHistoryActivity.class), 0));
            builder.setTicker(contentText);

            builder.setStyle(new Notification.BigTextStyle().bigText(contentTextBig));
            Notification n = builder.build();
            ((NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int)(Math.random() * Integer.MAX_VALUE), n);
        }

        private String getDefinitionHtml(String contents){
            return "<b>" + contents + ":</b> ";
        }

        private Pair<Spanned,Spanned> getSpannedFromCreds(Context context, ArrayList<Pair<Integer,String>> values){

            String html = "";
            String htmlSplit = "";

            for (int i = 0; i < values.size(); i++) {
                Pair<Integer, String> value = values.get(i);

                String currHtml = getDefinitionHtml(mContext.getString(value.first)) + value.second;
                html += currHtml;
                htmlSplit += currHtml;
                if(i != values.size() - 1){
                    html += ", ";
                    htmlSplit += "<br />";
                }
            }

            return new Pair<>(Html.fromHtml(html), Html.fromHtml(htmlSplit));
        }
    }

    private static ClipboardManager getManager(Context context){
        return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
    }
}
