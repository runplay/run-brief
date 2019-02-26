package run.brief.util.explore;

import android.app.Activity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import run.brief.R;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.locker.LockerFragment;
import run.brief.util.explore.fm.FileManagerDisk;
import run.brief.util.json.JSONArray;

/**
 * Created by coops on 16/12/14.
 */
public class FilesActionModeSelecter implements ActionMode.Callback {

    private Activity activity;
    private boolean wipeonfinish=true;
    public ActionMode mMode;
    public boolean isActionModeShowing;
    private FileManagerDisk fm;
    private List<MenuItem> menuitems = new ArrayList<MenuItem>();
    private String title;

    public FilesActionModeSelecter(Activity activity,FileManagerDisk filemanager) {
        this.activity = activity;
        fm=filemanager;

    }

    public void updateTitle() {
        if(fm!=null) {
            title=fm.getSelectedFiles().size()+"";
            mMode.setTitle(title);
        }

    }
    public void setWipeonfinish(boolean wipe) {
        this.wipeonfinish=wipe;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        menuitems.clear();

        //activity.getActionBar().setBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.browse_brand)));
        //activity.getActionBar().setSplitBackgroundDrawable(new ColorDrawable(activity.getResources().getColor(R.color.browse_brand)));
        //actionBar.setSplitBackgroundDrawable
        if(State.getFileExploreState()==State.FILE_EXPLORE_STATE_STANDALONE) {
            MenuItem delMenu = menu.add(activity.getString(R.string.label_delete));
            delMenu.setIcon(R.drawable.content_discard).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuitems.add(delMenu);

            MenuItem zipMenu = menu.add(activity.getString(R.string.label_archive));
            zipMenu.setIcon(R.drawable.btn_zip).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            //zipMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuitems.add(zipMenu);

            MenuItem cutMenu = menu.add(activity.getString(R.string.label_move));
            cutMenu.setIcon(R.drawable.content_cut).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuitems.add(cutMenu);

            MenuItem copyMenu = menu.add(activity.getString(R.string.label_copy));
            copyMenu.setIcon(R.drawable.content_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuitems.add(copyMenu);

            MenuItem lockMenu = menu.add(activity.getString(R.string.title_locker));
            lockMenu.setIcon(R.drawable.locker).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menuitems.add(lockMenu);
            //menu.add("Lock").setIcon(R.drawable.locker).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        } else {
            menu.add("Done").setIcon(R.drawable.navigation_accept).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        //BLog.e("onPrepareActionMode.........................");


        return false;
    }
    private JSONArray getSelectedFilesAsJSONArray() {
        JSONArray jarr = new JSONArray();
        Iterator<String> it = fm.getSelectedFiles().keySet().iterator();
        while (it.hasNext()) {
            String pairs = (String)it.next();
            jarr.put(pairs);
            //it.remove(); // avoids a ConcurrentModificationException
        }
        return jarr;
    }
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

        StringBuilder selectedItems = new StringBuilder();
        boolean intercepted=false;
        if(State.getFileExploreState()==State.FILE_EXPLORE_STATE_STANDALONE) {
            if (item == menuitems.get(0)) {
                // Delete
                wipeonfinish = false;
                if (fm != null)
                    State.addCachedFileManager(fm);
                intercepted = true;
                Bgo.openFragmentBackStack(activity, FilesDeleteFragment.class);

            } else if (item == menuitems.get(1)) {
                // Archive
                wipeonfinish = false;
                if (fm != null)
                    State.addCachedFileManager(fm);
                intercepted = true;
                Bgo.openFragmentBackStack(activity, FilesArchiveFragment.class);

            } else if (item == menuitems.get(2)) {

                fm.moveSelectedFilesToClipboard();
                fm.isCutPasteFilesOnClipboard = true;
                intercepted = true;
                //Bgo.goPreviousFragment(activity);
                Bgo.refreshCurrentFragment(activity);
                wipeonfinish = true;

            } else if (item == menuitems.get(3)) {
                //BLog.e("FEF", getSelectedFilesAsJSONArray().toString());

                fm.moveSelectedFilesToClipboard();
                fm.isCutPasteFilesOnClipboard = false;
                intercepted = true;
                Bgo.refreshCurrentFragment(activity);
                wipeonfinish = true;

            } else {
                State.addToState(State.SECTION_LOCKER,new StateObject(StateObject.STRING_BJSON_ARRAY,getSelectedFilesAsJSONArray().toString()));
                //ActionBarManager.setActionBarBackOnly(activity, activity.getResources().getString(R.string.files),R.menu.basic);

                Bgo.openFragmentBackStackAnimate(activity, LockerFragment.class);
                wipeonfinish=true;
                intercepted=true;
            }
        } else{
            State.addToState(State.SECTION_FILE_EXPLORE,new StateObject(StateObject.STRING_BJSON_ARRAY,getSelectedFilesAsJSONArray().toString()));
            //FileManager.getSelectedFiles().clear();
            //ActionBarManager.setActionBarBackOnly(activity, activity.getResources().getString(R.string.files), R.menu.basic);
            Bgo.goPreviousFragment(activity);
            wipeonfinish=true;
            intercepted = true;
        }
        if(intercepted)
            mode.finish();
        return intercepted;
    }
    @Override
    public void onDestroyActionMode(ActionMode mode) {

        if(!wipeonfinish) {

            //ActionBarManager.setActionBarBackOnlyWithLogo(activity, R.drawable.icon_explore, activity.getResources().getString(R.string.label_files), R.menu.basic, R.color.actionbar_basic);
        } else{
            if(fm!=null)
                fm.getSelectedFiles().clear();
        }
        ListView lv = (ListView) activity.findViewById(R.id.files_list);
        GridView listImages=(GridView)  activity.findViewById(R.id.files_list_grid);
        //FileManager.getSelectedFiles().clear();
        mMode=null;
        isActionModeShowing=false;
        if(lv!=null) {
            lv.invalidateViews();
        }
        if(listImages!=null) {
            listImages.invalidateViews();
        }

    }
}
