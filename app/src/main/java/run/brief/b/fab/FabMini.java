package run.brief.b.fab;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;

import java.util.ArrayList;

import run.brief.b.B;

/**
 * Created by coops on 08/08/15.
 */
public class FabMini {

    private FloatingMiniActionButton fab;
    private ArrayList<Boolean> lastKnownState=new ArrayList<Boolean>();
    
    public void show() {
        //Validator.calldata();
        if(fab!=null) {
            addTrimLastKnownState(Boolean.TRUE);
            fab.show(true);
            fab.bringToFront();

        }
    }
    public void setStyle(Activity activity, int RidColorNormal, int RidColorPressed, int RidColorRipple, int Rdrawable) {
        if(fab!=null) {
            fab.setColorNormal(activity.getResources().getColor(RidColorNormal));
            fab.setColorPressed(activity.getResources().getColor(RidColorPressed));
            fab.setColorRipple(activity.getResources().getColor(RidColorRipple));
            fab.setImageDrawable(B.getDrawable(activity,Rdrawable));
            //fab.setColorNormalResId();
        }
    }
    public void showHideNavClose() {
        if(fab!=null) {
            if (!lastKnownState.isEmpty() && lastKnownState.get(0) == Boolean.TRUE)
                show();
            else
                hide();
        }
    }
    public FloatingMiniActionButton getFab() {
        return fab;
    }
    public void startAnimation(Animation animate) {
        fab.setAnimation(animate);
    }

    public void bringToFront() {
        if(fab!=null)
            fab.bringToFront();
    }
    public FabMini(View containerView, int Rid, View.OnClickListener click) {

        fab = (FloatingMiniActionButton) containerView.findViewById(Rid);
        fab.setVisibility(View.VISIBLE);
        fab.hide(true);
        fab.setClickable(true);
        //fab.onsc
        fab.setOnClickListener(click);
        fab.bringToFront();
        //fab.setAnimation(new AnimatorSet());
        //fab.setVisibility(View.VISIBLE);
        //fab.show(true);
    }
    public void setOnClickListner(View.OnClickListener click) {
        if(fab!=null)
            fab.setOnClickListener(click);
    }
    public void hide() {
        if(fab!=null) {
            addTrimLastKnownState(Boolean.FALSE);
            fab.hide(true);
        }
    }
    public void addTrimLastKnownState(Boolean bool) {
        lastKnownState.add(bool);
        if(lastKnownState.size()>2)
            lastKnownState.remove(0);
    }
}



/*

		fab = (FloatingActionButton) view.findViewById(R.id.fab);
		fab.attachToListView(list, new ScrollDirectionListener() {
			@Override
			public void onScrollDown() {
				Log.d("ListViewFragment", "onScrollDown()");
			}

			@Override
			public void onScrollUp() {
				Log.d("ListViewFragment", "onScrollUp()");
			}
		}, new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				//Log.d("ListViewFragment", "onScrollStateChanged()");
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				//Log.d("ListViewFragment", "onScroll()");
			}
		});
		//fab.onsc
		fab.setOnClickListener(upDirectoryListener);
		fab.bringToFront();
 */
