package clipcaster.scrap.com.actisec.clipcaster;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by xcla001 on 28/10/14.
 */
public class FirstRunDialog {
    public static View getView(Activity activity){
        View dialogView = View.inflate(activity,R.layout.dialog_first_run,null);
        final ViewGroup viewGroup = (ViewGroup) dialogView;
        for(int i = 0; i < viewGroup.getChildCount(); i++){
            final View childView = viewGroup.getChildAt(i);
            if(childView instanceof TextView){
                final TextView curr = (TextView) childView;
                curr.setText(Html.fromHtml(curr.getText().toString()));
            }
        }

        return dialogView;
    }

}
