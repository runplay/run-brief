package run.brief.settings;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import run.brief.b.B;
import run.brief.b.BRefreshable;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.beans.BriefSettings;
import run.brief.service.BriefService;
import run.brief.sms.SmsFunctions;

public class SettingsGeneralFragment extends Fragment implements BRefreshable {
	private View view;
    public static final int RESULTCODE_SMS = 0;
	private CheckBox smscheck;

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view=inflater.inflate(R.layout.settings,container, false);

		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
        //if(!Validator.isNativeStart()) {
            //BLog.e("SET","general frag");
            //Validator.calldata();
            CheckBox unlockopen = (CheckBox) view.findViewById(R.id.settings_brief_open_unlock);
            unlockopen.setOnClickListener(onUnlockOpenClicked);
            if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_OPEN_ON_PRESENT))
                unlockopen.setChecked(true);
            else
                unlockopen.setChecked(false);

            CheckBox emocheck = (CheckBox) view.findViewById(R.id.settings_emo_check);
            emocheck.setOnClickListener(onEmoCheckboxClicked);
            if (State.getSettings().getBoolean(BriefSettings.BOOL_USE_EMOTICONS))
                emocheck.setChecked(true);
            else
                emocheck.setChecked(false);

            smscheck = (CheckBox) view.findViewById(R.id.settings_sms_check);
            if(SmsFunctions.canOperateAsDefaultSms()) {
                if (Device.hasPhone()) {


                    if (State.getSettings().getBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER)) {
                        smscheck.setChecked(true);
                        if (SmsFunctions.canOperateAsDefaultSms())
                            smscheck.setEnabled(false);
                    } else {
                        smscheck.setChecked(false);
                        smscheck.setOnClickListener(onSmsCheckboxClicked);
                    }

                } else {
                    View smsview = (View) view.findViewById(R.id.settings_sms_pod);
                    smsview.setVisibility(View.GONE);
                }
            } else {
                View smsview = (View) view.findViewById(R.id.settings_sms_pod);
                smsview.setVisibility(View.GONE);
            }

            TextView s1 = (TextView) view.findViewById(R.id.settings_home_warn);
            TextView s2 = (TextView) view.findViewById(R.id.settings_home_emo);
            TextView s3 = (TextView) view.findViewById(R.id.settings_home_sms);

            B.addStyle(new TextView[]{s1, s2, s3, emocheck, smscheck, unlockopen});


            refresh();

        //}
	}
	@Override
	public void refresh() {
		//BLog.e("CALL","settings general");
		//ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(), R.drawable.icon_settings, getActivity().getResources().getString(R.string.action_settings), R.menu.settings, R.color.actionbar_general);
	}	
	public void refreshData() {
		
	}
	public OnClickListener onEmoCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_USE_EMOTICONS,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_USE_EMOTICONS,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onSmsCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {

		    boolean checked = smscheck.isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {

                try {
                    if (!SmsFunctions.isDefaultSmsAppForDevice(getActivity())) {
                        Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                        intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getActivity().getPackageName());
                        startActivityForResult(intent, RESULTCODE_SMS);
                    }
                } catch(Exception e) {}
		    	settings.setBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER,Boolean.TRUE);
                smscheck.setEnabled(false);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
		    BriefService.refreshSmsReceiver();
		}
	};	

	public OnClickListener onBriefNotesCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NOTES,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NOTES,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onBriefNewsCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NEWS,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NEWS,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onBriefTwitterCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_TWITTER,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_TWITTER,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onBriefEmailCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_EMAIL,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_EMAIL,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onBriefChatCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_CHAT,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_CHAT,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onUnlockOpenClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_OPEN_ON_PRESENT,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_OPEN_ON_PRESENT,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};




}
