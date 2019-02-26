package run.brief.settings;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TextView;

import java.util.HashMap;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;


public class SettingsHomeTabbedFragment extends BFragment implements BRefreshable, TabHost.OnTabChangeListener, ViewPager.OnPageChangeListener {
 
    private TabHost mTabHost;
    private ViewPager mViewPager;
    private HashMap<String, TabInfo> mapTabInfo = new HashMap<String, SettingsHomeTabbedFragment.TabInfo>();
    private SettingsHomeTabbedAdapter sMessageAdapter;

    //Bundle savedInstanceState;
    private FragmentActivity activity;
 
    private View view;
    boolean isInit=false;
    
    
    private class TabInfo {
         private String tag;
         private Class<?> clss;
         private Bundle args;
         private Fragment fragment;
         TabInfo(String tag, Class<?> clazz,Bundle args) {
             this.tag = tag;
             this.clss = clazz;
             this.args = args;
         }
 
    }
    
    class TabFactory implements TabContentFactory {
 
        private final Context mContext;
 
        /**
         * @param context
         */
        public TabFactory(SettingsHomeTabbedFragment context) {
            mContext = context.getActivity();
        }
        /** (non-Javadoc)
         * @see android.widget.TabHost.TabContentFactory#createTabContent(java.lang.String)
         */
        public View createTabContent(String tag) {
            View v = new View(mContext);
            v.setMinimumWidth(0);
            v.setMinimumHeight(0);
            return v;
        }
 
    }
	public void refreshData() {
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		//frag=this;
		this.activity=(FragmentActivity) getActivity();
		//this.savedInstanceState=savedInstanceState;
		view=inflater.inflate(R.layout.settings_tabbed_frame,container, false);
		//startHandler.postDelayed(initialise, 50);
		return view;
	}
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");
        switch(item.getItemId()) {
            case R.id.action_help:
                //g.setEmailServiceInstance(emailService);
                Bgo.openFragmentBackStackAnimate(activity, HelpFragment.class);
                break;
            case R.id.action_network:
                //g.setEmailServiceInstance(emailService);
                Bgo.openFragmentBackStackAnimate(activity, SettingsCommsFragment.class);
                break;
            case R.id.action_about:
                Bgo.openFragmentBackStackAnimate(activity, AboutFragment.class);
                break;
            case R.id.action_legal:
                State.clearStateObjects(State.SECTION_LEGAL);
                Bgo.openFragmentBackStackAnimate(activity, LegalFragment.class);
                break;
        }
        return true;
    }
	@Override
	public void onResume() {
		super.onResume();
        //BLog.e("RESUME", "Settings resume");
		State.setCurrentSection(State.SECTION_SETTINGS);
		refresh();

	}
    public void refresh() {
        /*
        amb = new ActionModeBack(activity, activity.getString(R.string.action_settings)
                ,R.menu.settings
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.action_settings),R.menu.settings,R.color.brand);

    	BriefManager.clearController(activity);
		//ActionBarManager.setActionBarBackOnly(activity, activity.getResources().getString(R.string.action_settings),R.menu.settings);
        //BLog.e("RESUME", "Settings refresh");
        initialiseTabHost();
        intialiseViewPager();
        if (State.hasStateObject(State.SECTION_SETTINGS,StateObject.STRING_VALUE)) {
            //BLog.e("SETTAG",""+State.getStateObjectString(State.SECTION_SETTINGS,StateObject.STRING_VALUE));
            mTabHost.setCurrentTabByTag(State.getStateObjectString(State.SECTION_SETTINGS,StateObject.STRING_VALUE));
        }
        //if (savedInstanceState != null) {
        //    mTabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        //}

    }
	
	@Override
	public void onPause() {
		super.onPause();
        //BLog.e("SET","pause called");


        //Bgo.removeFragmentFromFragmentManager(activity);
        State.addToState(State.SECTION_SETTINGS,new StateObject(StateObject.STRING_VALUE,mTabHost.getCurrentTabTag()));
        mTabHost.setOnTabChangedListener(null);
        mTabHost.getTabWidget().removeAllViews();
        mTabHost.clearAllTabs();
        mTabHost.setEnabled(false);
        mTabHost=null;

        mViewPager.removeAllViews();
        mViewPager.setOnPageChangeListener(null);
        mViewPager=null;

        sMessageAdapter.notifyDataSetChanged();
        sMessageAdapter=null;

        //tabInfo=null;
        mapTabInfo = new HashMap<String, SettingsHomeTabbedFragment.TabInfo>();

        //Bgo.removeFragmentFromFragmentManager(activity,SettingsGeneralFragment.class.getName());
        //Bgo.removeFragmentFromFragmentManager(activity,SettingsHomeTabbedFragment.class.getName());
        //outState.putString("tab", mTabHost.getCurrentTabTag());
	}
	@Override
	public void onStop() {
		super.onStop();

	}
	
	

    private void intialiseViewPager() {
 
        sMessageAdapter  = new SettingsHomeTabbedAdapter(activity.getSupportFragmentManager());
        //
        mViewPager = (ViewPager)view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(sMessageAdapter);
        mViewPager.setOnPageChangeListener(this);
    }
 
    /**
     * Initialise the Tab Host
     */
    private void initialiseTabHost() {
        //BLog.e("SETTAG","INIT TAB HOST");
        mTabHost = (TabHost)view.findViewById(android.R.id.tabhost);
        //mTabHost = new FragmentTabHost(getActivity());
        //mTabHost.setu.setup(getActivity(), getChildFragmentManager());


        mTabHost.setup();
        mTabHost.clearAllTabs();
        mTabHost.setEnabled(true);
        TabInfo tabInfo = null;
        //BriefMessage.
//        SpannableString briefStr= B.getStyledWithTypeFaceName(activity, activity.getResources().getString(R.string.title_brief), State.getSettings().getString(BriefSettings.STRING_THEME),1f);
        SettingsHomeTabbedFragment.AddTab(this, this.mTabHost, mTabHost.newTabSpec(activity.getResources().getString(R.string.title_brief)).setIndicator(activity.getResources().getString(R.string.title_brief)), (tabInfo = new TabInfo(activity.getResources().getString(R.string.title_brief), SettingsBriefFragment.class, new Bundle())));
        mapTabInfo.put(tabInfo.tag, tabInfo);
        SettingsHomeTabbedFragment.AddTab(this, this.mTabHost, mTabHost.newTabSpec(activity.getResources().getString(R.string.label_home)).setIndicator(activity.getResources().getString(R.string.label_home)), ( tabInfo = new TabInfo(activity.getResources().getString(R.string.label_home), SettingsGeneralFragment.class,new Bundle())));
        mapTabInfo.put(tabInfo.tag, tabInfo);
        SettingsHomeTabbedFragment.AddTab(this, this.mTabHost, mTabHost.newTabSpec(activity.getResources().getString(R.string.label_storage)).setIndicator(activity.getResources().getString(R.string.label_storage)), ( tabInfo = new TabInfo(activity.getResources().getString(R.string.label_storage), SettingsStorageFragment.class,new Bundle())));
        mapTabInfo.put(tabInfo.tag, tabInfo);
        SettingsHomeTabbedFragment.AddTab(this, this.mTabHost, mTabHost.newTabSpec(activity.getResources().getString(R.string.settings_notify_tab)).setIndicator(activity.getResources().getString(R.string.settings_notify_tab)), ( tabInfo = new TabInfo(activity.getResources().getString(R.string.settings_notify_tab), SettingsRefreshFragment.class,new Bundle())));
        mapTabInfo.put(tabInfo.tag, tabInfo);
        
        mTabHost.getTabWidget().getChildAt(0).setBackground(B.getDrawable(activity,R.drawable.tab_bg_selector));
        mTabHost.getTabWidget().getChildAt(1).setBackground(B.getDrawable(activity,R.drawable.tab_bg_selector));
        mTabHost.getTabWidget().getChildAt(2).setBackground(B.getDrawable(activity,R.drawable.tab_bg_selector));
        mTabHost.getTabWidget().getChildAt(3).setBackground(B.getDrawable(activity,R.drawable.tab_bg_selector));
        mTabHost.getTabWidget().setDividerDrawable(R.drawable.tab_bg_divider);

        TextView x2 = (TextView) mTabHost.getTabWidget().getChildAt(0).findViewById(android.R.id.title);
        TextView x3 = (TextView) mTabHost.getTabWidget().getChildAt(1).findViewById(android.R.id.title);
        TextView x4 = (TextView) mTabHost.getTabWidget().getChildAt(2).findViewById(android.R.id.title);
        TextView x5 = (TextView) mTabHost.getTabWidget().getChildAt(3).findViewById(android.R.id.title);

        B.addStyle(new TextView[]{x2,x3,x4,x5});

        mTabHost.setOnTabChangedListener(this);
    }
 

    private static void AddTab(SettingsHomeTabbedFragment activity, TabHost tabHost, TabHost.TabSpec tabSpec, TabInfo tabInfo) {
        // Attach a Tab view factory to the spec
        tabSpec.setContent(activity.new TabFactory(activity));
        tabHost.addTab(tabSpec);
    }
 
    /** (non-Javadoc)
     * @see android.widget.TabHost.OnTabChangeListener#onTabChanged(java.lang.String)
     */
    public void onTabChanged(String tag) {
        int position = mTabHost.getCurrentTab();
        mViewPager.setCurrentItem(position);

    }

    @Override
    public void onPageScrolled(int position, float positionOffset,
            int positionOffsetPixels) {
        // TODO Auto-generated method stub
 
    }

    @Override
    public void onPageSelected(int position) {
        // TODO Auto-generated method stub
    	synchronized(this) {
    		mTabHost.setCurrentTab(position);

    	}
    }
 
    /* (non-Javadoc)
     * @see android.support.v4.view.ViewPager.OnPageChangeListener#onPageScrollStateChanged(int)
     */
    @Override
    public void onPageScrollStateChanged(int state) {
        // TODO Auto-generated method stub
 
    }
}