package run.brief.direct;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.State;
import run.brief.b.bButton;
import run.brief.beans.BriefSettings;
import run.brief.service.BriefService;
import run.brief.settings.GatewayPortMappingEntryAdapter;
import run.brief.util.upnp.Gateway;

public class DirectHomeFragment extends BFragment implements BRefreshable {
	private View view;
	private Activity activity;
	private ListView list;
	
	private View showDetails;
	private View showIsWorking;
	private View messageUuserIntervention;
	private View messageConnectWifi;
	private View messageEnableLiveComms;
	private View commsIsOn;

	private Handler refreshConnectionState;


	private TextView connectedInfo;
	private RefreshTask task;
	//private Handler accountsHandler = new Handler();
	
//String callbackURL="http://www.runplay.com";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
			
		//this.container=container;
		//this.inflater=inflater;
		activity=getActivity();

		view=inflater.inflate(R.layout.direct_home,container, false);
        task= new RefreshTask();
        task.execute(true);
		return view;

	}
	
	//private Runnable showAccounts = new Runnable() {
	//	public void run() {
	//		showAllAccounts();
	//	}
	//};
 
	public void refresh() {
		/*
		amb = new ActionModeBack(activity, activity.getResources().getString(R.string.title_direct)
				,R.menu.basic
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
		*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.title_direct),R.menu.basic,R.color.actionbar_email);

        //BLog.e("CALL","settings comms");
		//ActionBarManager.setActionBarBackOnly(getActivity(),getActivity().getResources().getString(R.string.action_settings), R.menu.basic);
		//BLog.e("refresh", "accounts home.....");
		
		list=(ListView) view.findViewById(R.id.network_list);


		CheckBox usep2p=(CheckBox) view.findViewById(R.id.settings_use_p2p_comms);
		usep2p.setOnClickListener(p2pListner);
		

		
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_DIRECT))
			usep2p.setChecked(true);
		else
			usep2p.setChecked(false);


		

		
		bButton refresh = (bButton) view.findViewById(R.id.comms_refresh);
		refresh.setOnClickListener(refreshListner);
		
		showDetails=(View) view.findViewById(R.id.settings_comms_show);
		showIsWorking=(View) view.findViewById(R.id.settings_comms_working);
		
		messageUuserIntervention = (View) view.findViewById(R.id.comms_message_user_intervention);
		messageConnectWifi = (View) view.findViewById(R.id.comms_message_wifi);
		messageEnableLiveComms = (View) view.findViewById(R.id.comms_is_off);
		commsIsOn = (View) view.findViewById(R.id.comms_is_on);
		
		connectedInfo = (TextView) view.findViewById(R.id.comms_connected_info);
		
		connectedInfo.setText(R.string.comms_info_disconnected);
		connectedInfo.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.device_comms_disconnect), null, null, null);

		GatewayPortMappingEntryAdapter adapter=new GatewayPortMappingEntryAdapter(getActivity());
		adapter.notifyDataSetChanged();
		list.refreshDrawableState();
        list.setAdapter(adapter);

        hideAll();
        
		TextView gatewayname = (TextView) view.findViewById(R.id.comms_network);
		gatewayname.setText(Gateway.getGatewayName());
		messageUuserIntervention.setVisibility(View.GONE);


		refreshConnectionState();
		
		
	}
	public void refreshData() {

	}


	private void refreshConnectionState() {
        refreshRun.run();

	}
	private Runnable refreshRun = new Runnable() {
		@Override
		public void run() {
			if(!State.getSettings().getBoolean(BriefSettings.BOOL_USE_DIRECT)) {
				messageEnableLiveComms.setVisibility(View.VISIBLE);
				showDetails.setVisibility(View.GONE);
			} else {

				showDetails.setVisibility(View.VISIBLE);
				if(Gateway.getState()==Gateway.STATE_WORKING) {
					showDetails.setVisibility(View.GONE);
					showIsWorking.setVisibility(View.VISIBLE);
					connectedInfo.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.device_comms_warn), null, null, null);

				} else if(Gateway.getState()==Gateway.STATE_USER_INTERVENTION) {
					connectedInfo.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.device_comms_warn), null, null, null);
					messageUuserIntervention.setVisibility(View.VISIBLE);

					showIsWorking.setVisibility(View.GONE);
				} else if(Gateway.getState()==Gateway.STATE_CONNECTED) {
					connectedInfo.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.device_comms_connected), null, null, null);
					connectedInfo.setText(R.string.comms_info_connected);
					showDetails.setVisibility(View.VISIBLE);
					showIsWorking.setVisibility(View.GONE);
					commsIsOn.setVisibility(View.VISIBLE);
				}
				refreshConnectionState=new Handler();
				refreshConnectionState.postDelayed(refreshRun,2000);
			}
		}
	};


	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_DIRECT);
        //if(!Validator.isNativeStart()) {
            //BLog.e("SET","comms frag");



		TextView s1 = (TextView) view.findViewById(R.id.settings_wifi);
		TextView s2 = (TextView) view.findViewById(R.id.settings_wifi_privacy);
		CheckBox uselivecomm = (CheckBox) view.findViewById(R.id.settings_use_live_comms);
		uselivecomm.setOnClickListener(onLivecommClicked);
		if (State.getSettings().getBoolean(BriefSettings.BOOL_USE_DIRECT))
			uselivecomm.setChecked(true);
		else
			uselivecomm.setChecked(false);

		B.addStyle(new TextView[]{uselivecomm,   s1, s2});
		refresh();

        //}

	}
	@Override
	public void onPause() {
		super.onPause();
		if(refreshConnectionState!=null)
			refreshConnectionState.removeCallbacks(refreshRun);
		//BLog.e("NP", "Network settings onPause called");
		State.getSettings().save();
		BriefSettings bean = State.getSettings();

	}
	public void hideAll() {
		
		commsIsOn.setVisibility(View.GONE);
		messageUuserIntervention.setVisibility(View.GONE);
		messageConnectWifi.setVisibility(View.GONE);
		messageEnableLiveComms.setVisibility(View.GONE);
		// always ensure visible
		showDetails.setVisibility(View.VISIBLE);
	}
	
	


	public OnClickListener onLivecommClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_USE_DIRECT,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_USE_DIRECT,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
		    refresh();
			
		}
	};	

	public OnClickListener p2pListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_USE_DIRECT,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_USE_DIRECT,Boolean.FALSE);
		    }
		    //BLog.e("P2P setting", "use p2p : "+settings.getBoolean(BriefSettings.BOOL_USE_DIRECT));
		    settings.save();
		    State.setSettings(settings);
		    refresh();
		}
	};
	/*
	public OnClickListener p2pContactListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_USE_P2P_CONTACTS,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_USE_P2P_CONTACTS,Boolean.FALSE);
		    }
		    //BLog.e("P2P setting", "use p2p contacts : "+settings.getBoolean(BriefSettings.BOOL_USE_P2P_CONTACTS));
		    settings.save();
		    State.setSettings(settings);
		    refresh();
		}
	};	
	public OnClickListener p2pEncryptListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_USE_P2P_ENCRYPT,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_USE_P2P_ENCRYPT,Boolean.FALSE);
		    }
		    //BLog.e("P2P setting", "use p2p encrypt : "+settings.getBoolean(BriefSettings.BOOL_USE_P2P_ENCRYPT));
		    settings.save();
		    State.setSettings(settings);
		    refresh();
		}
	};	
	
	*/
	
	public OnClickListener refreshListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//showDetails.setVisibility(View.GONE);
			messageUuserIntervention.setVisibility(View.GONE);
			showIsWorking.setVisibility(View.VISIBLE);
			task= new RefreshTask();
			task.execute(true);

		}
	};	

	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		}
	};
	private class RefreshTask extends AsyncTask<Boolean, Void, Boolean> {
		BriefService bs=null;
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected Boolean doInBackground(Boolean... params) {
   		
			//BLog.e("CALLING", "calling gateway refresh");

            BriefService.goRefreshNetwork();
            Gateway.goRefresh(activity,true);
			//BLog.e("CALLING", "done calling gateway refresh");
			return true;
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
			refresh();
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
}
