package run.brief.phone;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import run.brief.beans.Brief;
import run.brief.beans.Phonecall;
import run.brief.contacts.ContactViewFragment;

public class PhoneHomeFragment extends BFragment implements BRefreshable {
	
	private Activity activity;
	private View view;
	private ListView list;
	private PhoneListAdapter adapter;
    private int firstVis;
	//private Handler messageHandler = new Handler();
	//private static ArrayList<SmsMessage> textMessages=null;
	
	@Override
    public void onPause() {
        super.onPause();
        B.removeGoTopTracker();
    }
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		this.activity=getActivity();
		//this.container=container;
		//this.inflater=inflater;

		PhoneDb.init(getActivity());
		//messageHandler.postDelayed(showTexts, 10);
		view = inflater.inflate(R.layout.phone,container, false);
		return view;
	}
	@Override
    public void onResume() {
    	super.onResume();

    	State.sectionsClearBackstack();
    	State.setCurrentSection(State.SECTION_PHONE);
    	
		list=(ListView)getActivity().findViewById(R.id.phone_message_list);
		list.setClickable(true);
		list.setOnItemClickListener(openListener);
        list.setOnScrollListener(new PhoneScrollListener());
        B.addGoTopTracker(activity,list,R.drawable.gt_phone);
		refresh();

	}
    public void refreshData() {
        firstVis = list.getFirstVisiblePosition();
        adapter=new PhoneListAdapter(getActivity());
        list.setAdapter(adapter);
        list.setSelection(firstVis);
    }
	public void refresh() {
    	
		BriefManager.activateController(activity);
/*
        amb = new ActionModeBack(activity, activity.getString(R.string.title_phone)
                ,R.menu.phone
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.title_phone),R.menu.phone,R.color.actionbar_phone);

        refreshData();
	}
	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//Phonecall call = adapter.getItem(position);
            Phonecall call = PhoneDb.get(position);
            Brief brief = new Brief(activity,call,position);
			State.clearStateObjects(State.SECTION_CONTACTS_ITEM);
			StateObject bsob = new StateObject(StateObject.STRING_ID,brief.getPersonId());
			State.addToState(State.SECTION_CONTACTS_ITEM,bsob);
			Bgo.openFragmentBackStack(activity, ContactViewFragment.class);
		}
	};

    public class PhoneScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public PhoneScrollListener() {
        }
        public PhoneScrollListener(int visibleThreshold) {
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
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                if (!working) {
                    synchronized (this) {
                        working = true;
                        //BLog.e("LOAD", "more");
                        PhoneDb.getMoreHistory(activity);

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
