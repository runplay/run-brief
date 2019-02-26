package run.brief.settings;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import run.brief.R;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.beans.BriefSettings;
import run.brief.service.BriefService;
import run.brief.util.log.BLog;

public class SettingsCommsFragment extends BFragment implements BRefreshable {
	private View view;
	private Activity activity;
	//private ListView list;
	
	//private View showDetails;
	private View showIsWorking;
	//private View messageUuserIntervention;
	//private View messageConnectWifi;
	//private View messageEnableLiveComms;
	//View commsIsOn;
	
	private View proxyExpand;
	private View proxyExpandAuth;
	
	private CheckBox proxyCheck;
	private CheckBox proxyAuth;

	private EditText proxyIp;
	private EditText proxyPort;
	private EditText proxyUser;
	private EditText proxyPassword;
	private TextView proxyError;
	//private List<String> proxyErros=new ArrayList<String>();
	
	//private TextView connectedInfo;
	//private RefreshTask task;
	//private Handler accountsHandler = new Handler();
	
//String callbackURL="http://www.runplay.com";
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
			
		//this.container=container;
		//this.inflater=inflater;
		activity=getActivity();

		view=inflater.inflate(R.layout.settings_comms,container, false);

		return view;

	}
	
	//private Runnable showAccounts = new Runnable() {
	//	public void run() {
	//		showAllAccounts();
	//	}
	//};
 
	public void refresh() {
		//ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),R.drawable.icon_settings,getActivity().getResources().getString(R.string.action_settings), R.menu.settings,R.color.actionbar_general);
        //BLog.e("CALL","settings comms");
		//ActionBarManager.setActionBarBackOnly(getActivity(),getActivity().getResources().getString(R.string.action_settings), R.menu.basic);
		//BLog.e("refresh", "accounts home.....");
		
		//list=(ListView) view.findViewById(R.id.network_list);

		/*
		CheckBox usep2p=(CheckBox) view.findViewById(R.id.settings_use_p2p_comms);
		usep2p.setOnClickListener(p2pListner);
		
		CheckBox usep2pContacts=(CheckBox) view.findViewById(R.id.settings_use_p2p_only_contacts);
		usep2pContacts.setOnClickListener(p2pContactListner);
		
		CheckBox usep2pEncrypt=(CheckBox) view.findViewById(R.id.settings_use_p2p_encrypt);
		usep2pEncrypt.setOnClickListener(p2pEncryptListner);
		
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_DIRECT))
			usep2p.setChecked(true);
		else
			usep2p.setChecked(false);
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_P2P_CONTACTS))
			usep2pContacts.setChecked(true);
		else
			usep2pContacts.setChecked(false);
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_P2P_ENCRYPT))
			usep2pEncrypt.setChecked(true);
		else
			usep2pEncrypt.setChecked(false);
		*/
		

		
		//bButton refresh = (bButton) view.findViewById(R.id.comms_refresh);
		//refresh.setOnClickListener(refreshListner);
		
		//showDetails=(View) view.findViewById(R.id.settings_comms_show);
		//showIsWorking=(View) view.findViewById(R.id.settings_comms_working);
		
		//messageUuserIntervention = (View) view.findViewById(R.id.comms_message_user_intervention);
		//messageConnectWifi = (View) view.findViewById(R.id.comms_message_wifi);
		//messageEnableLiveComms = (View) view.findViewById(R.id.comms_is_off);
		//commsIsOn = (View) view.findViewById(R.id.comms_is_on);
		
		//connectedInfo = (TextView) view.findViewById(R.id.comms_connected_info);
		
		//connectedInfo.setText(R.string.comms_info_disconnected);
		//connectedInfo.setCompoundDrawablesWithIntrinsicBounds(activity.getResources().getDrawable(R.drawable.device_comms_disconnect), null, null, null);
		/*
		GatewayPortMappingEntryAdapter adapter=new GatewayPortMappingEntryAdapter(getActivity());
		adapter.notifyDataSetChanged();
		list.refreshDrawableState();
        list.setAdapter(adapter);
        */
        hideAll();
        
		//TextView gatewayname = (TextView) view.findViewById(R.id.comms_network);   
		//gatewayname.setText(Gateway.getGatewayName());
		//messageUuserIntervention.setVisibility(View.GONE);

        
		BriefSettings settings = State.getSettings();
		if(settings.has(BriefSettings.BOOL_PROXY) && settings.getBoolean(BriefSettings.BOOL_PROXY).equals(Boolean.TRUE)) {
			
			if(settings.has(BriefSettings.STRING_PROXY_IP)) {
				proxyIp.setText(settings.getString(BriefSettings.STRING_PROXY_IP));
			}
			if(settings.has(BriefSettings.INT_PROXY_PORT)) {
				proxyPort.setText(settings.getInt(BriefSettings.INT_PROXY_PORT));
			}
			if(settings.has(BriefSettings.STRING_PROXY_USER)) {
				proxyUser.setText(settings.getString(BriefSettings.STRING_PROXY_USER));
			}
			if(settings.has(BriefSettings.STRING_PROXY_PASSWORD)) {
				proxyPassword.setText(settings.getString(BriefSettings.STRING_PROXY_PASSWORD));
			}
			
			proxyExpand.setVisibility(View.VISIBLE);
			proxyCheck.setChecked(true);
			if(settings.has(BriefSettings.BOOL_PROXY_AUTH) && settings.getBoolean(BriefSettings.BOOL_PROXY_AUTH).equals(Boolean.TRUE)) {
				proxyExpandAuth.setVisibility(View.VISIBLE);
				proxyAuth.setChecked(true);
			} else {
				proxyExpandAuth.setVisibility(View.GONE);
				proxyAuth.setChecked(false);
			}
		} else {
			proxyExpand.setVisibility(View.GONE);
			proxyExpandAuth.setVisibility(View.GONE);
			proxyCheck.setChecked(false);
			proxyAuth.setChecked(false);
		}
		
		
	}
	public void refreshData() {
		
	}
	public void setProxyMessage(String msg) {
		if(msg!=null && msg.length()>0) {
			proxyError.setText(msg);
			proxyError.setTextColor(activity.getResources().getColor(R.color.red));
		} else {
			proxyError.setText(activity.getString(R.string.settings_proxy_desc));
			proxyError.setTextColor(activity.getResources().getColor(R.color.black));
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
        //if(!Validator.isNativeStart()) {
            //BLog.e("SET","comms frag");
		State.setCurrentSection(State.SECTION_SETTINGS_NETWORK);
            proxyExpand = (View) view.findViewById(R.id.settings_proxy_expand);
            proxyExpandAuth = (View) view.findViewById(R.id.settings_proxy_expand_auth);

            proxyCheck = (CheckBox) view.findViewById(R.id.settings_proxy_check);
            proxyAuth = (CheckBox) view.findViewById(R.id.settings_proxy_auth_check);
            proxyIp = (EditText) view.findViewById(R.id.settings_proxy_ip);
            proxyPort = (EditText) view.findViewById(R.id.settings_proxy_ip_port);
            proxyUser = (EditText) view.findViewById(R.id.settings_proxy_user);
            proxyPassword = (EditText) view.findViewById(R.id.settings_proxy_password);

            proxyCheck.setOnClickListener(onProxyClicked);
            proxyAuth.setOnClickListener(onProxyAuthClicked);

            proxyError = (TextView) view.findViewById(R.id.settings_proxy_desc);


            //TextView s1 = (TextView) view.findViewById(R.id.settings_wifi);
           // TextView s2 = (TextView) view.findViewById(R.id.settings_wifi_privacy);


            B.addStyle(new TextView[]{  proxyCheck, proxyAuth, proxyIp, proxyPort, proxyUser, proxyPassword, proxyError});
            refresh();

        //}

	}
	@Override
	public void onPause() {
		super.onPause();
		//BLog.e("NP", "Network settings onPause called");
		State.getSettings().save();
		BriefSettings bean = State.getSettings();
		if(bean!=null && bean.has(BriefSettings.BOOL_PROXY) && bean.getBoolean(BriefSettings.BOOL_PROXY)==Boolean.TRUE) {
			Device.setProxyOn();
		} else {
			Device.setProxyOff();
		}
	}
	public void hideAll() {
		
		//commsIsOn.setVisibility(View.GONE);
		//messageUuserIntervention.setVisibility(View.GONE);
		//messageConnectWifi.setVisibility(View.GONE);
		//messageEnableLiveComms.setVisibility(View.GONE);
		
		// always ensure visible
		//showDetails.setVisibility(View.VISIBLE);
	}
	
	

	
	public TextWatcher ipWatcher = new TextWatcher() {

		   public void afterTextChanged(Editable s) {
			   //BLog.e("TXT", "change to Text for to: ");
			   synchronized(this) {
				   String st=s.toString(); 
				   String es = activity.getString(R.string.settings_proxy_desc);
				   if(st.length()>3 && st.indexOf(".")!=-1) {
					   State.getSettings().setString(BriefSettings.STRING_PROXY_IP, st);
				   } else {
					   //proxyErrors.add("");
				   }
				   //--
			   }
		   }

		   public void beforeTextChanged(CharSequence s, int start,
		     int count, int after) {
		   }

		   public void onTextChanged(CharSequence s, int start,
		     int before, int count) {

		   }
	};
	
	
	public OnClickListener onProxyClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	proxyExpand.setVisibility(View.VISIBLE);
		    	settings.setBoolean(BriefSettings.BOOL_PROXY,Boolean.TRUE);
		    } else {
		    	proxyExpand.setVisibility(View.GONE);
		    	settings.setBoolean(BriefSettings.BOOL_PROXY,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
		    refresh();
			
		}
	};	
	
	public OnClickListener onProxyAuthClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	proxyExpandAuth.setVisibility(View.VISIBLE);
		    	settings.setBoolean(BriefSettings.BOOL_PROXY_AUTH,Boolean.TRUE);
		    } else {
		    	proxyExpandAuth.setVisibility(View.GONE);
		    	settings.setBoolean(BriefSettings.BOOL_PROXY_AUTH,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
		    refresh();
			
		}
	};	
	

	/*
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
	*/
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
   		
			BLog.e("CALLING", "calling gateway refresh");

            BriefService.goRefreshNetwork();
			BLog.e("CALLING", "done calling gateway refresh");
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
