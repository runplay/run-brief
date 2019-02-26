package run.brief.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.State;

public class UpgradeFragment extends BFragment implements BRefreshable {
	private View view;
	private ViewGroup container;
	private LayoutInflater inflater;
	private Activity activity=null;
	
	private LinearLayout isSubscriber;
	private LinearLayout notSubscriber;
	//private ListView list;
	//private Handler accountsHandler = new Handler();
	
//String callbackURL="http://www.runplay.com";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
			
		this.container=container;
		this.inflater=inflater;
		activity=getActivity();
		
		// ensure Twitter accounts Db is initialised
		AccountsDb.init();
		//AccountsDb.deleteAllAccounts();

		view=inflater.inflate(R.layout.account_upgrade,container, false);
		
		

		return view;

	}
	
	//private Runnable showAccounts = new Runnable() {
	//	public void run() {
	//		showAllAccounts();
	//	}
	//};
	public void refreshData() {
		
	}
	public void refresh() {
		//BLog.e("refresh", "accounts home");
		//list.removeAllViews();

		State.setCurrentSection(State.SECTION_ACCOUNTS);
		ActionBarManager.setActionBarBackOnly(getActivity(),getActivity().getResources().getString(R.string.action_accounts), R.menu.accounts);
		
		if(HomeFarm.isSubscriber()) {
			isSubscriber.setVisibility(View.VISIBLE);
			notSubscriber.setVisibility(View.GONE);
		} else {
			isSubscriber.setVisibility(View.GONE);
			notSubscriber.setVisibility(View.VISIBLE);
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();

		isSubscriber = (LinearLayout) view.findViewById(R.id.subscribe_is_subscriber);
		notSubscriber = (LinearLayout) view.findViewById(R.id.subscribe_not_subscriber);
		
		
		
		refresh();
		
		
	}
	
	
	public OnClickListener subscribeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {

			
		}
	};	

}
