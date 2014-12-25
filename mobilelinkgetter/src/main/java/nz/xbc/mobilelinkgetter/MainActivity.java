package nz.xbc.mobilelinkgetter;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class MainActivity extends Activity implements MobileLinkCompiler.CompiledLinkHandler {

    MobileLinkGetter mLinkGetter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLinkGetter = new MobileLinkGetter(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            updateUrls();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateUrls(){
        final MobileLinkCompiler mobileLinkCompiler = new MobileLinkCompiler(this);
        mobileLinkCompiler.getUrls(this);

    }

    @Override
    public void handle(MobileLinkCompiler.UrlEntry[] links) {
        Log.d("moblnkgt","done: " + Arrays.toString(links));
    }

}
