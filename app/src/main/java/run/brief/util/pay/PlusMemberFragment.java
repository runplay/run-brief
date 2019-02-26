package run.brief.util.pay;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.State;

/**
 * Created by coops on 03/02/15.
 */
public class PlusMemberFragment extends BFragment implements BRefreshable {
    private View view;



    private Activity activity=null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        this.activity=getActivity();
        view=inflater.inflate(R.layout.plus_member,container, false);

        //fileExplorerHandler.postDelayed(fileExplorerRunner, 10);
        return view;


    }
    @Override
    public void onPause() {
        super.onPause();

    }
    @Override
    public void onResume() {
        super.onResume();
        State.setCurrentSection(State.SECTION_PLUS_MEMBER);

        refresh();
    }
    public void refreshData() {

    }
    public void refresh() {
        /*
        amb = new ActionModeBack(getActivity(), activity.getString(R.string.label_plus_member)
                ,R.menu.basic
                , new run.brief.b.ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
*/
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.label_plus_member),R.menu.basic,R.color.actionbar_basic);


    }


}