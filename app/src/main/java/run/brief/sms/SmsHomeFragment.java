package run.brief.sms;


import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.SmsMsg;
import run.brief.menu.BriefMenu;

public class SmsHomeFragment extends BFragment implements BRefreshable {
	
	private SmsHomeFragment thisFragment;
	private Activity activity;
	private View view;
	private ListView list;
	private SmsListAdapter adapter;
	private Dialog dialog;
	private int firstVis;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		thisFragment=this;
		this.activity=getActivity();
		SmsDb.init(getActivity());
		//messageHandler.postDelayed(showTexts, 10);
		view = inflater.inflate(R.layout.sms,container, false);
		return view;
	}

	@Override
    public void onResume() {
    	super.onResume();
		BriefMenu.ensureMenuOff();
		BriefManager.activateController(activity);
    	State.sectionsClearBackstack();
    	State.setCurrentSection(State.SECTION_SMS);

        list=(ListView) view.findViewById(R.id.sms_message_list);
        list.setClickable(true);
        list.setOnItemClickListener(openListener);
        list.setEmptyView(view.findViewById(R.id.empty_sms));
        list.setOnScrollListener(new SmsScrollListener());
        list.setOnItemLongClickListener(onLongClick);
        if(SmsFunctions.canOperateAsDefaultSms() && !SmsFunctions.isDefaultSmsAppForDevice(activity)) {

            list.addHeaderView(SmsFunctions.getSmsNotDefaultView(activity));
        }
        B.addGoTopTracker(activity,list,R.drawable.gt_sms);
    	refresh();
    	
    }
    @Override
    public void onPause() {
        super.onPause();
        B.removeGoTopTracker();
    }
	public void refreshData() {
        firstVis = list.getFirstVisiblePosition();
        adapter=new SmsListAdapter(getActivity());
        list.setAdapter(adapter);
        list.setSelection(firstVis);
	}
	public void refresh() {
		//BLog.e("sms home", "refreshing");
		/*
		amb = new ActionModeBack(getActivity(), getActivity().getString(R.string.title_sms)
				,R.menu.sms
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),getActivity().getString(R.string.title_sms),R.menu.sms,R.color.actionbar_sms);

		refreshData();

	}
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");
		switch(item.getItemId()) {
			case R.id.action_sms_new:
				State.addToState(State.SECTION_SMS_SEND,new StateObject(StateObject.INT_FORCE_NEW,1));
				Bgo.openFragmentBackStack(activity,SmsSendFragment.class);
				break;
		}	
		return false;
	}
	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			int headercount=list.getHeaderViewsCount();
			State.clearStateObjects(State.SECTION_SMS_SEND);
			
			StateObject sob = new StateObject(StateObject.INT_USE_SELECTED_INDEX,Integer.valueOf(position-headercount));
			State.addToState(State.SECTION_SMS_SEND,sob);
			Bgo.openFragmentBackStackAnimate(activity, SmsSendFragment.class);

		}
	};
	public OnItemLongClickListener onLongClick = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

			SmsMsg msg=SmsDb.get(position);

			if(msg !=null) {
				dialog = new SmsDialog(activity,msg,thisFragment );
				dialog.show();
				
			}
			
			return true;
			
		}
	};

    public class SmsScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public SmsScrollListener() {
        }
        public SmsScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        private boolean working=false;
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                if (!working) {
                    synchronized (this) {
                        working = true;
                        //BLog.e("LOAD", "more");
                        SmsDb.getMoreHistory(activity);

                        refreshData();

                        working = false;
                    }
                }
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
}
