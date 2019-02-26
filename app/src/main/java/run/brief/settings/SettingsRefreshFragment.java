package run.brief.settings;


import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import run.brief.b.B;
import run.brief.b.BRefreshable;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.beans.BriefSettings;
import run.brief.util.Sf;
import run.brief.util.log.BLog;

public class SettingsRefreshFragment extends Fragment implements BRefreshable {
	private View view;

	private GridView timeGrid;
    private boolean hasAlertTimeChanges=false;
    private AlertTimeAdapter adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view=inflater.inflate(R.layout.settings_refresh,container, false);

		return view;

	}

    @Override
    public void onPause() {
        super.onPause();
        if(hasAlertTimeChanges) {
            BriefSettings bset = State.getSettings();
            //BLog.e("PAUSE",adapter.getTimesSaveString());
            bset.setString(BriefSettings.STRING_ALERT_TIMES,adapter.getTimesSaveString());
            bset.save();
        }
    }
	@Override
	public void onResume() {
		super.onResume();

        //if(!Validator.isNativeStart()) {
            //BLog.e("SET","refresh frag");
        TextView s1 = (TextView) view.findViewById(R.id.settings_alert_head);
        TextView s2 = (TextView) view.findViewById(R.id.settings_alert_sms_head);
        TextView s3 = (TextView) view.findViewById(R.id.settings_alert_email_head);
        TextView s4 = (TextView) view.findViewById(R.id.settings_alert_news_head);
        TextView s5 = (TextView) view.findViewById(R.id.settings_alert_repeat_head);

        TextView s6 = (TextView) view.findViewById(R.id.refresh_atime_none);
        TextView s7 = (TextView) view.findViewById(R.id.refresh_atime_vibrate);
        TextView s8 = (TextView) view.findViewById(R.id.refresh_atime_both);

        TextView s9 = (TextView) view.findViewById(R.id.alert_schedule);
        TextView s10 = (TextView) view.findViewById(R.id.alert_schedule_desc);


        CheckBox alertSmsSound = (CheckBox) view.findViewById(R.id.settings_alert_sms_sound);
        CheckBox alertNewsSound = (CheckBox) view.findViewById(R.id.settings_alert_news_sound);
        CheckBox alertEmailSound = (CheckBox) view.findViewById(R.id.settings_alert_email_sound);


        CheckBox alertSmsVibrate = (CheckBox) view.findViewById(R.id.settings_alert_sms_vibrate);
        CheckBox alertNewsVibrate = (CheckBox) view.findViewById(R.id.settings_alert_news_vibrate);
        CheckBox alertEmailVibrate = (CheckBox) view.findViewById(R.id.settings_alert_email_vibrate);
        CheckBox repeatVibrate = (CheckBox) view.findViewById(R.id.settings_alert_repeat_vibrate);

        timeGrid=(GridView) view.findViewById(R.id.refresh_alert_time);
        adapter=new AlertTimeAdapter();
        timeGrid.setAdapter(adapter);



        if (!Device.hasPhone()) {
            s2.setVisibility(View.GONE);
            alertSmsSound.setVisibility(View.GONE);
            alertSmsVibrate.setVisibility(View.GONE);
        }

        B.addStyle(new TextView[]{repeatVibrate,alertSmsVibrate,alertNewsVibrate,alertEmailVibrate, alertSmsSound, alertNewsSound, alertEmailSound});

        B.addStyleBold(new TextView[]{ s2, s3, s4,s5,s6,s7,s8,s10});
        B.addStyleBold(s1,B.FONT_LARGE);
        B.addStyleBold(s9,B.FONT_LARGE);

        setCheckBoxOptions(alertSmsSound, BriefSettings.BOOL_NOTIFY_SMS_SOUND, onSmsSoundClicked);
        setCheckBoxOptions(alertSmsVibrate,BriefSettings.BOOL_NOTIFY_SMS_VIBRATE,onSmsVibrateClicked);
        setCheckBoxOptions(alertEmailSound,BriefSettings.BOOL_NOTIFY_EMAIL_SOUND,onEmailSoundClicked);
        setCheckBoxOptions(alertEmailVibrate,BriefSettings.BOOL_NOTIFY_EMAIL_VIBRATE,onEmailVibrateClicked);
        setCheckBoxOptions(alertNewsSound,BriefSettings.BOOL_NOTIFY_NEWS_SOUND,onNewsSoundClicked);
        setCheckBoxOptions(alertNewsVibrate,BriefSettings.BOOL_NOTIFY_NEWS_VIBRATE,onNewsVibrateClicked);
        setCheckBoxOptions(repeatVibrate,BriefSettings.BOOL_NOTIFY_REPEAT_VIBRATE,onRepeatVibrateClicked);

        refresh();



        //}

	}
    private void setCheckBoxOptions(CheckBox checkbox,String BOOL_NOTIFY_, OnClickListener listener) {

        boolean checked = State.getSettings().getBoolean(BOOL_NOTIFY_);
        if(checked)
            checkbox.setChecked(true);
        else
            checkbox.setChecked(false);
        checkbox.setOnClickListener(listener);
    }
	public void refreshData() {
		
	}
	@Override
	public void refresh() {
        //BLog.e("CALL", "settings refresh");
		//ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),R.drawable.icon_settings,getActivity().getResources().getString(R.string.action_settings), R.menu.settings,R.color.actionbar_general);
	}	
	public OnClickListener onSmsSoundClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_NOTIFY_SMS_SOUND,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_NOTIFY_SMS_SOUND,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};
    public OnClickListener onSmsVibrateClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            BriefSettings settings = State.getSettings();
            if(checked) {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_SMS_VIBRATE,Boolean.TRUE);
            } else {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_SMS_VIBRATE,Boolean.FALSE);
            }
            settings.save();
            State.setSettings(settings);
        }
    };
    public OnClickListener onEmailSoundClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            BriefSettings settings = State.getSettings();
            if(checked) {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_EMAIL_SOUND,Boolean.TRUE);
            } else {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_EMAIL_SOUND,Boolean.FALSE);
            }
            settings.save();
            State.setSettings(settings);

        }
    };
    public OnClickListener onEmailVibrateClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            BriefSettings settings = State.getSettings();
            if(checked) {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_EMAIL_VIBRATE,Boolean.TRUE);
            } else {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_EMAIL_VIBRATE,Boolean.FALSE);
            }
            settings.save();
            State.setSettings(settings);
        }
    };
    public OnClickListener onNewsSoundClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            BriefSettings settings = State.getSettings();
            if(checked) {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_NEWS_SOUND,Boolean.TRUE);
            } else {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_NEWS_SOUND,Boolean.FALSE);
            }
            settings.save();
            State.setSettings(settings);

        }
    };
    public OnClickListener onNewsVibrateClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            BriefSettings settings = State.getSettings();
            if(checked) {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_NEWS_VIBRATE,Boolean.TRUE);
            } else {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_NEWS_VIBRATE,Boolean.FALSE);
            }
            settings.save();
            State.setSettings(settings);
        }
    };

    public OnClickListener onRepeatVibrateClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            BriefSettings settings = State.getSettings();
            if(checked) {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_REPEAT_VIBRATE,Boolean.TRUE);
            } else {
                settings.setBoolean(BriefSettings.BOOL_NOTIFY_REPEAT_VIBRATE,Boolean.FALSE);
            }
            settings.save();
            State.setSettings(settings);
        }
    };

	public static class TimePickerFragment extends DialogFragment
    		implements TimePickerDialog.OnTimeSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current time as the default values for the picker
			final Calendar c = Calendar.getInstance();
			int hour = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);
			
			// Create a new instance of TimePickerDialog and return it
			return new TimePickerDialog(getActivity(), this, hour, minute,
			DateFormat.is24HourFormat(getActivity()));
		}
		
		public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
			// Do something with the time chosen by the user
		}
	}


    public class AlertTimeAdapter extends BaseAdapter {

        private List<Integer> times = new ArrayList<Integer>();
        private BriefSettings bset;

        public AlertTimeAdapter() {
            bset=State.getSettings();
            times.clear();
            String[] splits = bset.getString(BriefSettings.STRING_ALERT_TIMES).split(",");
            if(splits==null || splits.length!=48) {

                bset.setString(BriefSettings.STRING_ALERT_TIMES,BriefSettings.deftimes);
                splits =BriefSettings.deftimes.split(",");
                BLog.e("ERROR", BriefSettings.deftimes);
            }
            for(String sp: splits) {
                times.add(Sf.toInt(sp));
            }
        }

        public String getTimesSaveString() {
            StringBuilder sb=new StringBuilder();
            for(Integer s: times) {
                if(sb.length()>0)
                    sb.append(",");
                sb.append(s);
            }
            return sb.toString();
        }
        public int getCount() {
            return times.size();
        }

        public Object getItem(int position) {
            return times.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            TextView vi=(TextView) convertView;
            if(convertView==null) {
                vi = new TextView(getActivity());
                AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.MATCH_PARENT);

                vi.setLayoutParams(lp);
                vi.setGravity(Gravity.CENTER_HORIZONTAL);
            }
            vi.setTextSize(12f);
            B.addStyle(vi);

            final Integer a = times.get(position);
            // Setting all values in listview
            if(a!=null) {
                vi.setText(BriefSettings.tztimes.get(position));
                vi.setTag(position);

                switch(a) {
                    case 1: vi.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.alert_vibrate,0,0);
                        //vi.setCompoundDrawables(null,getActivity().getResources().getDrawable(R.drawable.alert_vibrate),null,null);
                        break;
                    case 2: vi.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.alert_both,0,0);
                        //vi.setCompoundDrawables(null,getActivity().getResources().getDrawable(R.drawable.alert_both),null,null);
                        break;
                    default: vi.setCompoundDrawablesWithIntrinsicBounds(0,R.drawable.alert_none,0,0);
                        //vi.setCompoundDrawables(null,getActivity().getResources().getDrawable(R.drawable.alert_none),null,null);
                        break;
                }
                vi.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        hasAlertTimeChanges=true;
                        TextView thisview=(TextView) view;
                        int pos = (Integer) view.getTag();
                        final Integer a = times.get(pos);
                        int nval = a.intValue()+1;
                        if(nval>2)
                            nval=0;
                        switch(nval) {
                            case 1: thisview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.alert_vibrate, 0, 0);
                                break;
                            case 2: thisview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.alert_both, 0, 0);
                                break;
                            default: thisview.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.alert_none, 0, 0);
                                break;
                        }
                        times.set(pos,nval);
                        //bset.setString(BriefSettings.STRING_ALERT_TIMES, TextUtils.join(",", times));
                    }
                });
            }
            return vi;
        }


    }


}
