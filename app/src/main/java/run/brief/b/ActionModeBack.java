package run.brief.b;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Created by coops on 16/12/14.
 */
public class ActionModeBack implements ActionMode.Callback {

    private Activity activity;
    private boolean wipeonfinish=true;
    public ActionMode mMode;
    public boolean isActionModeShowing=false;

    //private List<MenuItem> menuitems = new ArrayList<MenuItem>();
    private Menu menu;
    private String title;
    private int useRmenu;
    ActionModeCallback callback;

    //private boolean mBackWasPressedInActionMode = false;

    public ActionModeBack(Activity activity, String title, int Rmenu, ActionModeCallback callback) {
        //BLog.e("ACTION MODE CALL NEW INSTANCE !!!!!!!");
        this.activity = activity;
        this.title=title;
        this.callback=callback;
        this.useRmenu=Rmenu;
        isActionModeShowing=true;
        mMode = ((AppCompatActivity)activity).startSupportActionMode(this);
        mMode.setTitle(title);

/*
        RelativeLayout lay = new RelativeLayout(activity);
        RelativeLayout.LayoutParams rlp =new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
        lay.setLayoutParams(rlp);
        lay.setBackgroundColor(activity.getResources().getColor(R.color.browse_brand));
        TextView txt = new TextView(activity);
        RelativeLayout.LayoutParams tlp =new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        txt.setLayoutParams(tlp);
        B.addStyle(txt);
        lay.addView(txt);
        //ActionBar ab = ((AppCompatActivity) activity).getSupportActionBar();

        //mMode.setDisplayShowCustomEnabled(true);
        mMode.setCustomView(lay);
        */

    }
    public void updateTitle() {

        mMode.setTitle(title);
    }

    public void done() {
        isActionModeShowing=false;

        //this.mMode.finish();
    }
    public void finish() {
        mMode.finish();
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {

        //BLog.e("ACTION MODE CREATE !!!!!!!");
        if(this.menu==null) {
            this.menu = menu;
            MenuInflater mf = activity.getMenuInflater();
            mf.inflate(useRmenu, menu);
        }
        //isActionModeShowing=true;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        //BLog.e("ACTION MODE PREPARE !!!!!!!");
        isActionModeShowing=true;
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        done();
        callback.onActionMenuItem(mode,item);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        //BLog.e("ACTION MODE onDestroyActionMode");
        if(isActionModeShowing) {
            //BLog.e("ACTION MODE BACK CALLING PREVIOUS FRAGMENT");
            Bgo.goPreviousFragment(activity);
        }

    }
}
