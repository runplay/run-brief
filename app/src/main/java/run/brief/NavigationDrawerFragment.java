package run.brief;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.contacts.ContactsHomeFragment;
import run.brief.email.EmailHomeFragment;
import run.brief.locker.LockerFragment;
import run.brief.news.NewsHomeFragment;
import run.brief.notes.NotesHomeFragment;
import run.brief.phone.PhoneHomeFragment;
import run.brief.settings.AccountsHomeFragment;
import run.brief.settings.HelpFragment;
import run.brief.settings.SettingsHomeTabbedFragment;
import run.brief.sms.SmsHomeFragment;
import run.brief.util.Cal;
import run.brief.util.explore.FileExploreFragment;


/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment implements OnScrollListener  {
    private NavigationDrawerCallbacks mCallbacks;
    public static ActionBarDrawerToggle mDrawerToggle;
    private Activity activity;

    public DrawerLayout mDrawerLayout;
    private View mDrawerView;
    //private GridView mDrawerListView;

    private View mFragmentContainerView;
    private TextView btnHelp;
    private ImageView btnAlert;
    private ImageView btnSettings;


    private ListView newsSources;
    private NavItemsAdapter newsAdapter;
    private static List<bMenuItem> MENU_ITEMS=new ArrayList<bMenuItem>();
    //private List<RssUserFeed> userfeeds;

    @Override
    public void onResume() {
        super.onResume();

        List<bMenuItem> MI=new ArrayList<bMenuItem>();

        MI.add(makeMenu(State.SECTION_BRIEF,activity.getString(R.string.title_brief), B.getDrawable(activity,R.drawable.i_brief),activity.getString(R.string.title_brief_sub)));
        //MI.add(makeMenu(R.menu.direct_send,State.SECTION_DIRECT_SEND,activity.getString(R.string.title_direct_send),activity.getResources().getDrawable(R.drawable.i_direct_off),activity.getString(R.string.title_direct_send_sub)));
        if(Device.hasPhone()) {
            MI.add(makeMenu(State.SECTION_PHONE,activity.getString(R.string.title_phone),B.getDrawable(activity,R.drawable.i_phone),activity.getString(R.string.title_phone_sub)));
        }
        if(Device.hasContentProviderSmsSent()) {
            MI.add(makeMenu(State.SECTION_SMS,activity.getString(R.string.title_sms),B.getDrawable(activity,R.drawable.i_sms),activity.getString(R.string.title_sms_sub)));
        }
        //if(AccountsManager.countEmailAccounts()>0) {
        MI.add(makeMenu(State.SECTION_EMAIL,activity.getString(R.string.title_email),B.getDrawable(activity,R.drawable.i_email),activity.getString(R.string.title_email_sub)));
        //}

        if(Device.hasP2P()) {
            MI.add(makeMenu(State.SECTION_D2D,activity.getString(R.string.title_d2d),B.getDrawable(activity,R.drawable.comms_item),activity.getString(R.string.title_d2d_sub)));
        }
        MI.add(makeMenu(State.SECTION_FILE_EXPLORE,activity.getString(R.string.label_files),B.getDrawable(activity,R.drawable.collections_collection),activity.getString(R.string.title_files_sub)));
        MI.add(makeMenu(State.SECTION_NEWS,activity.getString(R.string.title_news),B.getDrawable(activity,R.drawable.i_news),activity.getString(R.string.title_news_sub)));
        MI.add(makeMenu(State.SECTION_NOTES,activity.getString(R.string.title_notes),B.getDrawable(activity,R.drawable.i_note),activity.getString(R.string.title_notes_sub)));
        MI.add(makeMenu(State.SECTION_LOCKER,activity.getString(R.string.title_locker),B.getDrawable(activity,R.drawable.locker),activity.getString(R.string.locker_desc_short)));
        MI.add(makeMenu(State.SECTION_CONTACTS,activity.getString(R.string.contacts),B.getDrawable(activity,R.drawable.i_social),activity.getString(R.string.contacts_summary)));
        MI.add(makeMenu(State.SECTION_ACCOUNTS,activity.getString(R.string.action_accounts),B.getDrawable(activity,R.drawable.content_edit),activity.getString(R.string.accounts_overview)));

        MENU_ITEMS=MI;



        btnHelp = (TextView) mDrawerLayout.findViewById(R.id.btn_content_help);
        btnHelp.setOnClickListener(toHelpListener);

        B.addStyle(btnHelp);

        btnAlert = (ImageView) mDrawerLayout.findViewById(R.id.btn_content_alert);
        btnAlert.setOnClickListener(toAlertListener);


        btnSettings = (ImageView) mDrawerLayout.findViewById(R.id.btn_settings);
        btnSettings.setOnClickListener(toSettingsListener);
    }
    public class bMenuItem {
        public String title;
        public int STATE_SECTION_;
        public Drawable img;
        public String subtext;
        public bMenuItem(int STATE_SECTION_, String title, Drawable img, String subtext) {
            this.title=title; this.STATE_SECTION_=STATE_SECTION_; this.img=img; this.subtext=subtext;
        }

    }
    public bMenuItem makeMenu(int STATE_SECTION_, String title, Drawable img, String subtext) {
        return new bMenuItem(STATE_SECTION_,title, img, subtext);
    }
    
    
    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.activity=getActivity();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mDrawerView = inflater.inflate(R.layout.navigation_drawer, container, false);



        return mDrawerView;
    }

    public void closeDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
    }
    public void openDrawer() {
        if (mDrawerLayout != null) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }
    }


    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }
    private View.OnClickListener toAlertListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeDrawer();
            State.addToState(State.SECTION_SETTINGS,new StateObject(StateObject.STRING_VALUE,activity.getString(R.string.settings_notify_tab)));
            Bgo.openFragmentBackStackAnimate(getActivity(), SettingsHomeTabbedFragment.class);
        }
    };
    private View.OnClickListener toHelpListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeDrawer();
            Bgo.openFragmentBackStackAnimate(getActivity(), HelpFragment.class);

            //State.addToState(State.SECTION_NEWS_CHOOSE, new StateObject(StateObject.INT_VALUE, 1));
            //Bgo.openFragmentBackStackAnimate(getActivity(), new NewsChooseFeedsFragment());
            //Bgo.openFragment(activity,new NewsChooseFeedsFragment());
            //showView(showFilters);
        }
    };
    private View.OnClickListener toSettingsListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeDrawer();
            Bgo.openFragmentBackStackAnimate(getActivity(), SettingsHomeTabbedFragment.class);
            //Bgo.openFragment(activity,new NewsChooseFeedsFragment());
            //showView(showFilters);
        }
    };
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;


        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener



        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //Fab.showHideNavClose();
                if (!isAdded()) {
                    return;
                }

                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //Fab.hide();
                if (!isAdded()) {
                    return;
                }

                //mDrawerView.setVisibility(View.GONE);
                getActivity().supportInvalidateOptionsMenu(); // calls onPrepareOptionsMenu()

                reloadLastItems();
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            Log.e("Callback","callback navigation");
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            //Log.e("Callback","callback navigation - attached");
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            //inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //Log.e("TOOLBAR", "onOptionsItemSelected called");

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if(mDrawerLayout!=null)
            mDrawerLayout.bringToFront();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        //ActionBar actionBar = getActionBar();
        //actionBar.setDisplayShowTitleEnabled(true);
        //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {

        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }


    public class Holder {
        public Holder(int Rid,String txt) {this.Rid=Rid; this.txt=txt;}
        public int Rid;
        public String txt;

    }




    private void reloadLastItems() {

        //userfeeds = NewsFeedsDb.getUserFeedsArray();
//BLog.e("feeds size: "+userfeeds.size());

        newsSources = (ListView) mDrawerView.findViewById(R.id.list_sources);
        newsAdapter = new NavItemsAdapter(activity);
        newsSources.setAdapter(newsAdapter);



    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if(newsAdapter!=null)
            newsAdapter.notifyDataSetChanged();

    }
    private long lastscroll;
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
        long now= Cal.getUnixTime();

        //lastfilesadapter.notifyDataSetChanged();

        //BLog.e("SCR SPEED: " + (now-lastscroll));
        lastscroll=now;

    }

    public class NavItemsAdapter extends BaseAdapter {
        private Activity activity;


        public NavItemsAdapter(Activity c) {
            this.activity = c;


        }

        public int getCount() {
            return MENU_ITEMS.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return position;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {

            RelativeLayout lay;
            if (convertView == null) {
                lay=(RelativeLayout) activity.getLayoutInflater().inflate(R.layout.navigation_drawer_item,null);

            } else {
                lay= (RelativeLayout) convertView;
            }

            bMenuItem item = MENU_ITEMS.get(position);

            ImageView image = (ImageView) lay.findViewById(R.id.btn_nav_item_img);
            TextView text = (TextView) lay.findViewById(R.id.btn_nav_item_txt);
            TextView extra = (TextView) lay.findViewById(R.id.btn_nav_item_txt_extra);

            text.setText(item.title);
            extra.setText(item.subtext);
            lay.setTag(item.STATE_SECTION_);
            image.setImageDrawable(item.img);

            B.addStyle(text,extra);
            lay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setBackgroundColor(activity.getResources().getColor(R.color.grey_light));
                    Integer tag=(Integer) v.getTag();
                    if(tag!=null) {
                        goNavItem(tag.intValue());
                    }
                }
            });

            return lay;
        }
    }

    private void goNavItem(final int SECTION) {
        Class<? extends BFragment> fragment=null;
        switch(SECTION) {
            case State.SECTION_EMAIL:
                fragment = EmailHomeFragment.class;
                break;
            case State.SECTION_PHONE:
                fragment = PhoneHomeFragment.class;
                break;
            case State.SECTION_SMS:
                fragment = SmsHomeFragment.class;
                break;
            case State.SECTION_NEWS:
                fragment = NewsHomeFragment.class;
                break;
            case State.SECTION_NOTES:
                fragment = NotesHomeFragment.class;
                break;
            case State.SECTION_FILE_EXPLORE:
                fragment= FileExploreFragment.class;
                break;
            case State.SECTION_LOCKER:
                fragment= LockerFragment.class;
                break;
            case State.SECTION_CONTACTS:
                fragment= ContactsHomeFragment.class;
                break;
            case State.SECTION_ACCOUNTS:
                fragment= AccountsHomeFragment.class;
                break;
            default:
                fragment = BriefHomeFragment.class;
                break;

        }
        if(fragment!=null) {
            closeDrawer();
            Bgo.openFragment(activity, fragment);
        }
    }
}
