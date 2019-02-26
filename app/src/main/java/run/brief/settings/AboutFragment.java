package run.brief.settings;


import android.app.Activity;
import android.net.TrafficStats;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.util.Cal;
import run.brief.util.Sf;
import run.brief.util.UrlStore;

public class AboutFragment extends BFragment implements BRefreshable {
	private View view;

	private Activity activity;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

        this.activity=getActivity();

		view=inflater.inflate(R.layout.settings_about,container, false);


		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_SETTINGS_ABOUT);

        TextView txt1 = (TextView) view.findViewById(R.id.about_head_device);
        TextView opensource = (TextView) view.findViewById(R.id.about_desc_inc_soft);
        TextView txt3 = (TextView) view.findViewById(R.id.about_app_name);
        TextView version = (TextView) view.findViewById(R.id.about_version);
        TextView txt4 = (TextView) view.findViewById(R.id.about_by);
        TextView www = (TextView) view.findViewById(R.id.about_www);
        TextView appname = (TextView) view.findViewById(R.id.about_app_name);

        TextView memberHead = (TextView) view.findViewById(R.id.about_head_member);
        TextView memberDesc = (TextView) view.findViewById(R.id.about_desc_member);

        www.setText(UrlStore.DOMAIN_WWW);
        B.addStyleBold(txt3,B.FONT_XLARGE);


        TextView desc = (TextView) view.findViewById(R.id.about_desc_device);
        TextView incsoft = (TextView) view.findViewById(R.id.about_click_inc_soft);

        opensource.setOnClickListener(openSourceClicked);
        incsoft.setOnClickListener(openSourceClicked);
        B.addStyle(new TextView[]{txt1,opensource,desc,txt4,memberDesc});
        B.addStyleBold(version,B.FONT_MEDIUM);
        B.addStyleBold(version,B.FONT_LARGE);
        B.addStyleBold(incsoft);
        B.addStyleBold(www);
        B.addStyleBold(memberHead);

        try {
            String versionName = activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0).versionName;
            version.setText(versionName);
        } catch(Exception e) {}

        StringBuilder sb = new StringBuilder();
        sb.append(Device.getDeviceName());
        sb.append(Sf.NEW_LINE);


        View member = view.findViewById(R.id.about_member_pod);
        if(HomeFarm.isRegistered()) {
            memberHead.setText(activity.getString(R.string.label_plus_member));
            memberDesc.setText(activity.getString(R.string.upgrade_expires)+": "+ Cal.getCal(HomeFarm.getPlusMemberExpireDate()).getDatabaseDate());
        } else {
            member.setVisibility(View.GONE);
        }


        boolean found=false;
        if(Device.imeiSIM1!=null && !Device.imeiSIM1.isEmpty()) {
            sb.append(activity.getString(R.string.about_text_phone));
            sb.append((Sf.COLON_SPACE));
            sb.append(Device.imeiSIM1);
            sb.append((Sf.COLON_SPACE));
            sb.append(Device.isSIM1Ready);
            sb.append(Sf.NEW_LINE);
            found=true;
        }

        if(Device.imeiSIM2!=null && !Device.imeiSIM2.isEmpty()) {
            sb.append(activity.getString(R.string.about_text_phone));
            sb.append((Sf.COLON_SPACE));
            sb.append(Device.imeiSIM2);
            sb.append((Sf.COLON_SPACE));
            sb.append(Device.isSIM2Ready);
            found=true;
        }
/*
        if(!found) {
            String tmp = Device.getPhoneNumber();
            if(tmp!=null && !tmp.isEmpty()) {
                sb.append(activity.getString(R.string.about_text_phone));
                sb.append((Sf.COLON_SPACE));
                sb.append(tmp);
                //sb.append((Sf.COLON_SPACE));
                //sb.append(Device.isSIM2Ready);
            }
        }
        */
        desc.setText(sb.toString());


		refresh();
		  

	}
	public void refreshData() {
		
	}
	@Override
	public void refresh() {
        /*
        amb = new ActionModeBack(getActivity(), getActivity().getString(R.string.label_about)
                ,R.menu.basic
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),getActivity().getString(R.string.label_about),R.menu.basic,R.color.brand);


        long totalbyte = TrafficStats.getTotalRxBytes();
        long mobilenetworkbyte = TrafficStats.getMobileRxBytes();
        String total = Long.toString(totalbyte);
        String mobile = Long.toString(mobilenetworkbyte);
        String wifibyte = (Sf.toLong(total) - Sf.toLong(mobile)) + "kb";
	}	
	public OnClickListener openSourceClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
            State.clearStateObjects(State.SECTION_LEGAL);
            State.addToState(State.SECTION_LEGAL, new StateObject(StateObject.STRING_VALUE, UrlStore.URL_OPEN_SOURCE));
            Bgo.openFragmentBackStackAnimate(activity,LegalFragment.class);
		}
	};	



}
