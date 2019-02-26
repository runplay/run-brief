package run.brief;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.BriefSettings;
import run.brief.beans.Email;
import run.brief.beans.Person;
import run.brief.contacts.ContactsDb;
import run.brief.email.EmailSendFragment;
import run.brief.email.EmailService;
import run.brief.menu.BriefMenu;
import run.brief.news.NewsItemsDb;
import run.brief.notes.NotesDb;
import run.brief.phone.PhoneDb;
import run.brief.service.BriefNotify;
import run.brief.service.BriefService;
import run.brief.service.SyncDataDb;
import run.brief.settings.AccountsDb;
import run.brief.settings.AccountsHomeFragment;
import run.brief.settings.OAuth.OAuthHelper;
import run.brief.settings.SettingsGeneralFragment;
import run.brief.settings.UserStatsDb;
import run.brief.sms.SmsDb;
import run.brief.sms.SmsSendFragment;
import run.brief.util.camera.CameraFragment;
import run.brief.util.json.JSONArray;
import run.brief.util.log.BLog;
import run.brief.util.misc.PRNGFixes;



//import android.annotation.SuppressLint;

public final class Main extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

	//private static boolean isStarted=false;
    //public static boolean isFromRestore=false;
	//private boolean stopOptions=false;

    private NavigationDrawerFragment mNavigationDrawerFragment;
	private static Bundle savedInstanceState;
	private static Handler checkMediaMountedHandler;
    private static Bitmap resizedBitmap;
    private Main activity;

    //private View loadview;
    private static final int TYPE_LAUNCH=0;
    private static final int TYPE_SMS=1;
    private static final int TYPE_EMAIL=2;
    private static int LAUNCH_TYPE=TYPE_LAUNCH;
    private String launchUri;

    private View firsttime;
    private boolean shouldShowWelcome;

    private boolean isCreatedStart=false;
    //private boolean useActivityResult=false;

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //BLog.e("ACTRESULT","Main vals: "+requestCode+", "+resultCode+", ");
		switch (requestCode) {
            case OAuthHelper.MY_ACTIVITYS_AUTH_REQUEST_CODE:
                State.sectionsClearBackstack();
                State.setCurrentSection(State.SECTION_ACCOUNTS);
                break;
		    case CameraFragment.ACTION_TAKE_PHOTO_B:
			//State.addToState(new StateObject(StateObject.STRING_FILE_PATH,"photo"));
			//BLog.e("YO","TAKE PHOTO: "+State.getCameraLastPhoto());
			    break;
            case SettingsGeneralFragment.RESULTCODE_SMS:

                break;
            case AccountsHomeFragment.REQUEST_CODE_PICK_ACCOUNT:
                //BLog.e("GRRRR","should not see this");
                break;
        }
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {

        BriefService.ensureStartups(this);
        BriefSettings settings = State.getSettings();
        if(settings!=null && settings.getBoolean(BriefSettings.BOOL_STYLE_DARK)==Boolean.FALSE) {
            setTheme(R.style.AppBaseTheme);
            //Log.e("THEME","Theme is LIGHT");
        } else {
            setTheme(R.style.AppBaseTheme);
        }
		super.onCreate(savedInstanceState);
        BLog.e("Started");
        //Tim.start();
        isCreatedStart=true;

        activity=this;
        this.savedInstanceState=savedInstanceState;
/*
        String languageToLoad  = "en"; // your language
        Locale locale = new Locale(languageToLoad);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
*/

        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
		//getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

		//getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,WindowManager.LayoutParams.FLAG_SEC        BriefService.ensureStartups(activity);
        //BriefService.setLocale(this);URE);

        setContentView(R.layout.main);
        if (android.os.Build.VERSION.SDK_INT >= 19) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            toolbar.setBackgroundColor(getResources().getColor(R.color.brand));
            setSupportActionBar(toolbar);
        }


        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.


        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
	}


    private void showLockedScreen() {
        View locked = activity.findViewById(R.id.main_lock_app);
        locked.setVisibility(View.VISIBLE);
        locked.bringToFront();
        BriefMenu.ensureMenuOff();
        ActionBarManager.setActionBarBackOnly(activity,this.getString(R.string.app_name),R.menu.basic);
        getSupportActionBar().setHomeButtonEnabled(false);
    }
    private void showWelcomeScreen() {
        if(shouldShowWelcome) {
            //BLog.e("HSET","showing welcome now");
            firsttime = activity.findViewById(R.id.main_firsttime_app);
            firsttime.setVisibility(View.VISIBLE);
            firsttime.bringToFront();
            BriefMenu.ensureMenuOff();
            ActionBarManager.setActionBarBackOnly(activity, this.getString(R.string.app_name), R.menu.basic);
            getSupportActionBar().setHomeButtonEnabled(false);
            Handler h = new Handler();
            h.postDelayed(new Runnable() {
                @Override
                public void run() {
                    shouldShowWelcome = false;
                    doOnResume();
                }
            }, 200);
        }
    }
    private void hideIfWelcomeScreen() {

        if(firsttime!=null) {
            //BLog.e("CALL start", "hide welcome screen ******************");
            firsttime.setVisibility(View.GONE);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }
    @Override
    public void onStart() {
        //BLog.e("CALL start", "ON STRT called ******************");
        //Tim.printTime("onStart()");
        super.onStart();
        //BriefService.ensureStartups(activity);

        State.setUserStats(UserStatsDb.getUserStats());

        BriefManager.init(activity);
        BriefManager.setLimit(6);
//this.getPackageManager().

        if(HomeFarm.isAppExpired()) {

        } else {

            if (!BriefService.isAppStarted()) {
                if(HomeFarm.isFirstTime()) {
                    //BLog.e("HSET","setting to shoud show welcome");
                    shouldShowWelcome = true;
                }
                //BLog.e("B-CRATE", "call onCreatePlay()");
                onCreatePlay();
            } else {
                //BLog.e("B-CRATE", "--------skip onCreatePlay()");
            }
        }

    }
	@Override
    public void onRestart() {
        super.onRestart();
        BriefService.setIsAppStarted(true);

    }
    private void clearIntent() {
        getIntent().removeExtra("key");
        getIntent().setData(Uri.parse("launch:home"));
    }
	@Override
    public void onResume() {
        super.onResume();

        doOnResume();
    }
    public void doOnResume() {
        //Tim.printTime("onResume()");
        if(HomeFarm.isAppExpired()) {

            // locked
            showLockedScreen();

        } else if(shouldShowWelcome) {
            showWelcomeScreen();
        } else {


            Intent intent = getIntent();
            Uri data = intent.getData();
            //String action = intent.getAction();
            //String type = intent.getType();
            LAUNCH_TYPE = TYPE_LAUNCH;
            if (data != null) {
                //BLog.e("COMPOSE", " -- " + data.toString());
                launchUri = data.toString();
                if (launchUri != null) {
                    if (launchUri.startsWith("mailto:")) {
                        LAUNCH_TYPE = TYPE_EMAIL;
                    } else if (launchUri.startsWith("tel:")) {
                        LAUNCH_TYPE = TYPE_SMS;
                    }
                }

            }


            onResumePlayGeneral();


            if (LAUNCH_TYPE == TYPE_EMAIL) {
                if (!BriefService.isAppStarted()) {
                    //BLog.e("B-RESUME", "Email startup");
                    startHandler();
                } else {
                    B.addBackgroundImage(activity, false);
                    //BLog.e("B-RESUME", "Email startup show Current");
                    clearIntent();

                    Bgo.clearBackStack(activity);
                    Email email = new Email();
                    email.setString(Email.STRING_TO, launchUri.replaceFirst("mailto:", ""));
                    State.clearStateObjects(State.SECTION_EMAIL_NEW);
                    State.addToState(State.SECTION_EMAIL_NEW, new StateObject(StateObject.STRING_BJSON_OBJECT, email.toString()));

                    Bgo.openFragment(activity, EmailSendFragment.class);
                    setShowLoading(false);
                    BriefService.setIsAppRestore(false);
                    BriefService.setIsAppStarted(true);
                    launchUri = null;
                }
                //Bgo.openFragment(this,);
            } else if (LAUNCH_TYPE == TYPE_SMS) {
                if (!BriefService.isAppStarted()) {
                    //BLog.e("B-RESUME", "Sms startup");
                    startHandler();
                } else {
                    B.addBackgroundImage(activity, false);
                    //BLog.e("B-RESUME", "Sms startup show Current");
                    clearIntent();

                    JSONArray jar = new JSONArray();
                    String phonenumber = launchUri.replaceFirst("tel:", "");
                    Person p = ContactsDb.getWithTelephone(activity, phonenumber);
                    if (p == null) {
                        p = Person.getNewUnknownPerson(activity, phonenumber, null);
                    }
                    jar.put(p.getBean().toString());
                    State.addToState(State.SECTION_SMS_SEND, new StateObject(StateObject.STRING_CONTACTS, jar.toString()));

                    Bgo.openFragment(activity, SmsSendFragment.class);
                    setShowLoading(false);
                    BriefService.setIsAppRestore(false);
                    BriefService.setIsAppStarted(true);
                    launchUri = null;
                }
                //Bgo.openFragment(this,new SmsSendFragment());
            } else {

                //if (!BriefService.isAppStarted()) {
                if (!BriefService.isAppStarted()) {
                    //BLog.e("B-RESUME","Regular startup, is restore: "+BriefService.isAppRestore());

                    startHandler();

                } else {
                    hideIfWelcomeScreen();
                    B.addBackgroundImage(activity, false);
                     //BLog.e("B-RESUME","Regular startup show Current, is createcalled: "+isCreatedStart);

                        //launchUri = null;
                    if(!BriefManager.hasDirtyItems())
                        BriefManager.setDirtyAllItems();
                    onResumePlay();
                    BriefService.setIsAppRestore(false);
                    BriefService.setIsAppStarted(true);
                    setShowLoading(false);

                }
            }

        }
        //Tim.printTime("onResume() finished");
    }

    private void onCreatePlay() {

        checkSdCard();

        setTheme(B.getTheme());
        if (!BriefService.isBriefServiceRunning(activity)) {
            Intent service = new Intent(activity, BriefService.class);
            activity.startService(service);

        }
        if(State.getSettings()==null) BLog.e("SETTINGS IS STILL NULL");
        B.initTypeface(activity, State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE));

        BriefSendDb.init(activity);
        //BriefSendDb.deleteAll();


        SyncDataDb.init(activity);
        //SyncDataDb.deleteAll();
        //ActionBarManager.refresh(activity);

        BriefNotify.clearNotifications();
        Device.hideKeyboard(activity);

        BriefService.doSmsDefaultsCheck(activity);

    }
    private void onResumePlayGeneral() {

        ContactsDb.init(activity);
        BriefMenu.init(activity);
        BriefMenu.hideMenu();
        Device.hideKeyboard(activity);
        Device.updateRotation(activity);

        TextView txt = (TextView) activity.findViewById(R.id.main_start_text);
        B.addStyle(txt);

		
    }
    private void onResumePlay() {


        if (savedInstanceState != null) {
            //useActivityResult=false;
            State.sectionsClearBackstack();
            State.loadState(savedInstanceState);
            //BLog.e("Resume()","Load state size: "+State.getSectionsSize());
            savedInstanceState=null;
        }
        //BLog.e("Resume()Play","Load state size: "+State.getSectionsSize()+" - called state: "+State.getCurrentSection());
        Bgo.openCurrentState(activity);


    }
    private void setShowLoading(boolean show) {
        View loadview = activity.findViewById(R.id.main_loading);
        if(show) {
            loadview.setVisibility(View.VISIBLE);
            loadview.bringToFront();
        } else
            loadview.setVisibility(View.GONE);
    }

/*
    private void ensureActionbarUnderlay() {
        View briefActionBar = activity.findViewById(R.id.brief_actionbar);

        BriefManager.setActionBarHeight(activity,activity.getActionBar().getHeight());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, BriefManager.getActionBarHeight());
        briefActionBar.setLayoutParams(lp);
    }
    */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

        //ensureActionbarUnderlay();
	    return true;
	}
	@Override
    public void onPause() {
    	super.onPause();
        //BLog.e("B-PAUSE","pasing app");
    	if(checkMediaMountedHandler==null)
			checkMediaMountedHandler=new Handler();
		checkMediaMountedHandler.removeCallbacks(runSdCard);
        BriefService.setIsAppStarted(false);

    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //BLog.e("B-DETACT","detached from window");
    }

    @Override
    public void onStop() {
    	super.onStop();
        //BLog.e("B-STOP","******************* STOPPING app");
	    BriefSettings settings = State.getSettings();
	    settings.setInt(BriefSettings.INT_COUNT_LAUNCH, settings.getInt(BriefSettings.INT_COUNT_LAUNCH)+1);
	    settings.save();
	    BLog.SaveIfNeeded();

    }
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

		State.sectionsClearBackstack();
		State.loadState(savedInstanceState);

        ActionBarManager.restart(this);
        resizedBitmap=null;
        ActionBarManager.setStopOptions(true);

        BriefService.setIsAppRestore(true);
        //BLog.e("B-RESUME","-------------------------RESTORE STATE");
        BriefService.setIsAppStarted(true);
		BriefMenu.hideMenu();
		Device.hideKeyboard(this);
		Device.updateRotation(this);
        //ensureActionbarUnderlay();

	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
		//BLog.e("SAVE", "SAVE instance state");
		outState.clear();
		State.saveState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        //super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		if(!ActionBarManager.isMenuSet())
			ActionBarManager.setActionBarMenu(this, activity.getString(R.string.label_home), R.menu.brief_home);
		ActionBarManager.showMenu(this, menu);
		//BLog.e("ACTB", "ACTIONBAR SHOW CALLED");
		return true;
	}
	@Override
	public void onBackPressed() {
        ActionBarManager.setStopOptions(true);
        //BLog.e("BACK PRESS","item: "+State.getSectionsSize());
		if(State.getSectionsSize()==0) {
			if(State.getCurrentSection()!=State.SECTION_BRIEF)
				Bgo.openFragment(activity, BriefHomeFragment.class);
			else {
                Bgo.clearBackStack(this);

                State.clearStateAllObjects();
                //BLog.e("EXIT", "APP");
                //this.get
                super.onBackPressed();
            }
		} else {
			Bgo.goPreviousFragment(this);
		}
		
	}
	@Override()
	public void onUserInteraction()  {
		//View.OnTouchListener
		// use this for snapchat style features
		//this.on
		//Log.e("US_INTER", "USER INTERACTION");
	}	
	/*
	@Override()
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		//View.OnTouchListener
		// use this for snapchat style features
		
		boolean stop=false;
	    if (keyCode != KeyEvent.KEYCODE_BACK) {
	    	stop=true;
	    	//closeAppDialog(null,false);
	    }
	    //Log.e("app","keyDown");
	    if(stop)
	    	return true;
	    else
	    	return super.onKeyDown(keyCode, event);
	}
	*/
	@Override()
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		//BLog.e("OPTIONS","menu item: "+(item.getItemId()==android.R.id.home));
		if(item.getItemId()==android.R.id.home) {
			BLog.e("OPTION","home");
            //ActionBarManager.setStopOptions(true);
			if(State.getCurrentSection()==State.SECTION_BRIEF) {

                if(mNavigationDrawerFragment.isDrawerOpen()) {
                    mNavigationDrawerFragment.closeDrawer();
                } else {
                    mNavigationDrawerFragment.openDrawer();
                }
                /*
				if(!BriefMenu.isMenuShowing())
					BriefMenu.showMenu(true);
				else
					BriefMenu.hideMenu();
                */
			} else {
                ActionBarManager.setStopOptions(true);
				Bgo.goPreviousFragment(this);
                delaycanceloptions.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        ActionBarManager.setStopOptions(false);
                    }
                },900);
                //onNavigationItemSelected(0,0);

			}
            //ActionBarManager.setStopOptions(false);
			return true;
		} else {

			Bgo.action(this, item);
			return false;
		}

	}
	private Handler delaycanceloptions = new Handler();

    @Override
    public void onNavigationDrawerItemSelected(int position) {

    }
 /*

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		ActionBarManager.bMenuItem mi = ActionBarManager.getMenuItem(position);
		//BLog.e("Main","onNavigationItemSelected: "+position);
		//if(position!=0 || !ActionBarManager.stopOptions()) {
        if(position!=0 || !ActionBarManager.stopOptions()) {
            ActionBarManager.setStopOptions(false);
			//BLog.e("Main","onNavigationItemSelected SELECTED: "+position);
			State.sectionsClearBackstack();
			//BLog.e("NAV","SELECTED: "+mi.STATE_SECTION_);
			if(mi!=null) {
                Class<? extends Fragment> fragment=null;
				switch(mi.STATE_SECTION_) {
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
					case State.SECTION_LOGS:
						fragment = LogFragment.class;
						break; 
					case State.SECTION_FILE_EXPLORE:
						fragment= FileExploreFragment.class;
						break;
                    case State.SECTION_LOCKER:
                        fragment= LockerFragment.class;
                        break;
					default:
                        fragment = BriefHomeFragment.class;
						break;
	
				} 
				if(fragment!=null)
					Bgo.openFragment(this,fragment);
			}
		} else {
            ActionBarManager.setStopOptions(false);
        }
		return false;
	}
    */


















    // start and background functions

	private void checkSdCard() {
		View v = this.findViewById(R.id.main_no_sd_card);
		if(v!=null) {
			if(Device.isMediaMounted()) {
				v.setVisibility(View.GONE);
                ActionBarManager.show(this);
			} else {

				v.setVisibility(View.VISIBLE);
                ActionBarManager.hide(this);
				v.bringToFront();
			}

		}
		if(checkMediaMountedHandler==null)
			checkMediaMountedHandler=new Handler();
		checkMediaMountedHandler.removeCallbacks(runSdCard);
		checkMediaMountedHandler.postDelayed(runSdCard,3000);
	}
	private Runnable runSdCard = new Runnable() {
		@Override
		public void run() {
			checkSdCard();
		}
	};



    long startTime;
    long nowTime;

    private boolean isStartHandlerRunning=false;
    private void startHandler() {
        runStartHandler.postDelayed(startHandlerLoadData,10);
    }
    private Runnable startHandlerLoadData = new Runnable() {
        @Override
        public void run() {
            //Tim.printTime("startHandler()");
            //BLog.e("Main", "---------------startHandlerLoadData");

/*
            Root r = new Root();
            if(r.isDeviceRooted()) {
                // device is rooted
            }
            startTime= Cal.getUnixTime();
*/
            //startTime= Cal.getUnixTime();

            //BriefHomeFragment.showLoading=true;

            ContactsDb.init(activity);
            if(ContactsDb.size()==0) {
                ContactsDb.loadContactsFull(activity);
                ContactsDb.mapContactsFullToContacts(activity);
            }


            //Bgo.refreshCurrentFragmentIfBrief(activity);
            //BLog.e("Timer","BM refresh: "+(Cal.getUnixTime()-startTime)+" millis");
            //startTime= Cal.getUnixTime();
            new InitialiseStartHandler().execute(true);


        }
    };

    private Handler runStartHandler = new Handler();

	private class InitialiseStartHandler extends AsyncTask<Boolean, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			//BLog.e("POINT", "1");
            //Tim.printTime("InitialiseStartHandler()");

            PRNGFixes.apply();
/*
			Root r = new Root();
			if(r.isDeviceRooted()) {
				BLog.e("ROOT","device IS rooted");
			}
*/
            //BLog.e("Timer","contacts: "+(Cal.getUnixTime()-startTime)+" millis");
            //startTime= Cal.getUnixTime();

            SmsDb.init(activity);
            //BLog.e("Timer","sms: "+(Cal.getUnixTime()-startTime)+" millis");
            //startTime= Cal.getUnixTime();

            PhoneDb.init(activity);
            //BLog.e("Timer","phone: "+(Cal.getUnixTime()-startTime)+" millis");
            //startTime= Cal.getUnixTime();



            ArrayList<Account> emaccounts = AccountsDb.getAllEmailAccounts();
            if(emaccounts!=null) {
                for(Account account: emaccounts) {
                    EmailService.getService(activity, account);
                }
            }
            //BLog.e("Timer","email: "+(Cal.getUnixTime()-startTime)+" millis");
            //startTime= Cal.getUnixTime();

            NewsItemsDb.init(activity);

            //BLog.e("Timer","news: "+(Cal.getUnixTime()-startTime)+" millis");
            //startTime= Cal.getUnixTime();

            NotesDb.init(activity);
            //BLog.e("Timer","notes: "+(Cal.getUnixTime()-startTime)+" millis");
            //startTime= Cal.getUnixTime();

            //Tim.printTime("InitialiseStartHandler() finished");

		    return Boolean.TRUE;
	
		}      
		@Override
		protected void onPostExecute(Boolean result) {
            BriefManager.setDirty(BriefManager.IS_DIRTY_RATINGS);
            BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
            BriefManager.setDirty(BriefManager.IS_DIRTY_PHONE);

            BriefManager.setDirty(BriefManager.IS_DIRTY_NEWS);
            BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);
            BriefManager.setDirty(BriefManager.IS_DIRTY_NOTES);

            //BLog.e("B-RESUME","FINISHED START-HANDLER()");
            BriefService.setIsAppStarted(true);
            setShowLoading(false);
            //BLog.e("B-INITALISE", "Init finished startup");
			doOnResume();

            checkSdCard();

            if(BriefSendDb.size()>0)
                BriefService.sendServiceGo(activity);
            //HomeFarm.outDev();

		}
	 
	}
    private Handler runFinishHandler = new Handler();
}
