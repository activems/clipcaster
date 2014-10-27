package clipcaster.scrap.com.actisec.clipcaster;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MyActivity extends ListActivity {

    ArrayAdapter<String> mAdapter;

    private void clearClips(){
            mAdapter.clear();
    }

    protected void showAboutDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.firstrun_title));
        builder.setView(FirstRunDialog.getView(this));
        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        startService(new Intent(this, ClipCasterService.class));

        mAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, ClipCasterService.mClips);

        setListAdapter(mAdapter);

        TextView emptyText = (TextView) findViewById(android.R.id.empty);
        emptyText.setText(Html.fromHtml(getString(R.string.cliplist_empty)));
        prefs = getSharedPreferences("com.mycompany.myAppName", MODE_PRIVATE);
        if(prefs.getBoolean("firstrun", true)){
            showAboutDialog();
        }
    }
    SharedPreferences prefs = null;

    @Override
    protected void onResume() {
        super.onResume();

        if (prefs.getBoolean("firstrun", true)) {
            // Do first run stuff here then set 'firstrun' as false
            // using the following line to edit/commit prefs
            prefs.edit().putBoolean("firstrun", false).apply();
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_clear) {
            clearClips();
            return true;
        }
        if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getLog(final String name){

        try {
            BufferedReader br = null;
            br = new BufferedReader(new FileReader(getFileStreamPath(name)));
            try {
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();

                while (line != null) {
                    sb.append(line);
                    sb.append(System.lineSeparator());
                    line = br.readLine();
                }
                if(sb.toString().isEmpty()){
                    return "<" + name + " is empty>";
                }
                return sb.toString();
            } finally {
                br.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Cannot read file " + name;
    }


    ClipboardManager getManager(){
        return (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    }
}
