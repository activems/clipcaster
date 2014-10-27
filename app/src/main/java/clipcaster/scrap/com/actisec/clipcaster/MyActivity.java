package clipcaster.scrap.com.actisec.clipcaster;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class MyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        startService(new Intent(this,ClipCasterService.class));
        ((TextView)findViewById(R.id.maintext)).setText(getLog(ClipCasterService.CLIPLOG_FILENAME));
    }

    @Override
    protected void onResume(){
        super.onResume();
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
        if (id == R.id.action_cliplog) {
            ((TextView)findViewById(R.id.maintext)).setText(getLog(ClipCasterService.CLIPLOG_FILENAME));
            return true;
        } else if (id == R.id.action_credlog) {
            ((TextView)findViewById(R.id.maintext)).setText(getLog(ClipCasterService.CREDLOG_FILENAME));
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
}
