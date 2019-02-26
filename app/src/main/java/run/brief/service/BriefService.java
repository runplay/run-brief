package run.brief.service;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.FileObserver;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import run.brief.BriefHomeFragment;
import run.brief.BriefManager;
import run.brief.BriefSendDb;
import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.BriefSend;
import run.brief.beans.BriefSettings;
import run.brief.beans.Email;
import run.brief.beans.EmailFolder;
import run.brief.beans.LockerItem;
import run.brief.beans.RssUserFeed;
import run.brief.beans.SmsSend;
import run.brief.beans.SyncData;
import run.brief.email.DefaultProperties;
import run.brief.email.EmailHomeFragment;
import run.brief.email.EmailSendService;
import run.brief.email.EmailService;
import run.brief.email.EmailServiceInstance;
import run.brief.news.DoLoadForUserFeed;
import run.brief.news.NewsFeedsDb;
import run.brief.news.NewsHomeFragment;
import run.brief.news.NewsItemsDb;
import run.brief.phone.PhoneDb;
import run.brief.settings.AccountsDb;
import run.brief.settings.SettingsDb;
import run.brief.sms.SmsDb;
import run.brief.sms.SmsFunctions;
import run.brief.sms.SmsSendFragment;
import run.brief.util.BriefActivityManager;
import run.brief.util.Cal;
import run.brief.util.Files;
import run.brief.util.UnZip;
import run.brief.util.Zip;
import run.brief.util.explore.FileItem;
import run.brief.util.explore.Indexer;
import run.brief.util.explore.IndexerDb;
import run.brief.locker.LockerFragment;
import run.brief.locker.LockerManager;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;
import run.brief.util.upnp.Gateway;


public final class BriefService extends Service {
	
	private static BriefService SERVICE;//=new BriefService();
    private Activity activity;
    //private NotificationManager mNM;
    private Gateway gateway;
    private Handler syncDataHandler = new Handler();
    private Handler checkInternetHandler = new Handler();
    private Handler checkNewsHandler = new Handler();
    private Handler startSyncsHandler = new Handler();
    
    private Handler sendSeviceQueHandler = new Handler();
    private ProcessSendQue processSendQue;
    
    private SyncDataThread syncDataThread;
    private RefreshInternet refreshInternet;

    public static final String SWITCH_EVENT="brfsrv_switch";
    //private static Handler notifyHandler = new Handler();
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();
    
    private static OnAlarmReceiver areceiver;
    private static OnAlarmReceiver aoreceiver;
    private static SmsReceiver freceiver;
    private static OnCallReceiver treceiver;
    private static OnBootReceiver receiver;
    private static OnBootReceiver breceiver;
    private static OnUserPresentReceiver ubreceiver;
    private static OnPcConnectReceiver pconreceiver;
    private static OnPcDisconnectReceiver pdisconreceiver;
    //private static OnPcDisconnectReceiver pdisconreceiver;
    private static FileObserver fileObserver;

    private RunIndexingTask runIndexTask;

    private Locale currLocale;
/*
    public static void setLocale(Context context) {
        SERVICE.currLocale=context.getResources().getConfiguration().locale;
    }
    public static Locale getLocale() {
        return SERVICE.currLocale;
    }
*/
    private Map<Long,Boolean> activeSyncAccounts = new ConcurrentHashMap<Long,Boolean>(50);//new HashMap<Long,Boolean>();

    public static boolean isAccountActiveSyncing(long accountId) {

        if(SERVICE.activeSyncAccounts.get(accountId)!=null) {

            if(SERVICE.activeSyncAccounts.get(accountId)==Boolean.TRUE) {
                //BLog.e("BSERV","check: "+SERVICE.activeSyncAccounts.get(accountId));
                return true;
            }
        } else {
            //BLog.e("BSERV","nothing to check ------------- ");
        }
        return false;
    }
    public static void setAccountActiveSyncing(long accountId, boolean isactive) {
        SERVICE.activeSyncAccounts.put(accountId,isactive);
    }

    public static boolean shouldReloadContacts=false;
    
    private static long LAST_REFRESH;
    private boolean PC_CONNECTED;
    private int TelephonyManagerSTATE;
    
    private boolean activeCall=false;

    private static final long MILLIS_SYNC_DATA = 120000; // every 5 mins
    private static final long MILLIS_SEND_QUE = 30000; // 30 seconds
    
    private static final int PRIORITY_SMS_INTERCEPT=999;

    private static final int PRIORITY_SMS_LOW=10;

    private static boolean isAppStarted=false;
    private static boolean isAppRefstore=false;

    public static void setIsAppStarted(boolean isStarted) {
        isAppStarted=isStarted;
    }
    public static boolean isAppStarted() {
        return isAppStarted;
    }
    public static void setIsAppRestore(boolean isRefstore) {
        isAppRefstore=isRefstore;
    }
    public static boolean isAppRestore() {
        return isAppRefstore;
    }

    private int NOTIFICATION = R.string.accounts_add_email; 

    //public static void setActivity(Activity activity) {
    //    SERVICE.activity=activity;
    //}

    private List<NotifyObject> notifyAlerts = new ArrayList<NotifyObject>();

    private class NotifyObject {
        public String text;
        public int Ricon;
        public String head;
    }
    public static void notify(Context context, Brief brief) {
        BriefNotify.addNotifyFor(context, brief);
    }



    public class LocalBinder extends Binder {
        public BriefService getService() {
            return BriefService.this;
        }
    }

    @Override
    public void onCreate() {
    	super.onCreate();
        //mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

    }



    public static void ensureStartups(Context context) {
        Files.setAppHomePath(context);
        //BLog.e("ENSURE","--ensure startups.....");
        HomeFarm.init(context);
        SettingsDb.init();
        State.setSettings(SettingsDb.getSettings());
        //}
        Device.init(context);
        AccountsDb.init();
        IndexerDb.init(context);
        //Device.init(SERVICE);
        //AccountsDb.init();
    }
    public static void doSmsDefaultsCheck(Context context) {
        if(SmsFunctions.canOperateAsDefaultSms()) {
            BriefSettings settings = State.getSettings();
            if (SmsFunctions.isDefaultSmsAppForDevice(context)) {
                //BLog.e("SMSDEF","is defailt");
                if (!settings.getBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER)) {
                    BLog.e("SMSDEF","is defailt, setting now");
                    settings.setBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER, Boolean.TRUE);
                    settings.save();
                    BriefService.refreshSmsReceiver();
                }
            } else {
                //BLog.e("SMSDEF","is not defailt");
                if (settings.getBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER)) {
                    //if(SmsFunctions.isDefaultSmsAppForDevice(context)) {
                    BLog.e("SMSDEF","is not defailt, settings now");
                    // double checked SmsFunctions.isDefaultSmsAppForDevice(context)
                    settings.setBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER, Boolean.FALSE);
                    settings.save();
                    BriefService.refreshSmsReceiver();
                    //}
                }
            }
        }

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
    	if(SERVICE==null) {
    		SERVICE=this;
    	//doFirstTimeCheck();
    		
    		
    	
    	BLog.e("SERVICE","BriefService started");
        
    	//runner = new BriefServiceRunner();
    	//runner.i    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        ensureStartups(getBaseContext());

        
    	Intent service = new Intent(this, OnAlarmReceiver.class);
    	PendingIntent pintent = PendingIntent.getService(this, 0, service, 0);

    	AlarmManager alarm = (AlarmManager) this.getSystemService(Service.ALARM_SERVICE);
    	// Start every 30 seconds
    	alarm.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,SystemClock.elapsedRealtime(), 30*1000, pintent); 
    	
    	refreshRegisterSmsReceiver();
    	
		IntentFilter afilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		areceiver = new OnAlarmReceiver();
		
		IntentFilter aofilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		aoreceiver = new OnAlarmReceiver();
		
		
		IntentFilter tfilter = new IntentFilter("android.intent.action.PHONE_STATE");
		treceiver = new OnCallReceiver();
		
		IntentFilter filter = new IntentFilter(Intent.ACTION_BOOT_COMPLETED);
		receiver = new OnBootReceiver();
		
		IntentFilter bfilter = new IntentFilter("android.intent.action.QUICKBOOT_POWERON");
		breceiver = new OnBootReceiver();

		IntentFilter ubfilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
		ubreceiver=new OnUserPresentReceiver();
		//ubreceiver = new OnAlarmReceiver();
		
		IntentFilter pconfilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
		pconreceiver = new OnPcConnectReceiver();

		IntentFilter pdisconfilter = new IntentFilter(Intent.ACTION_POWER_DISCONNECTED);
		pdisconreceiver = new OnPcDisconnectReceiver();
/*
            fileObserver = new FileObserver(Environment.getExternalStorageDirectory().toString()+File.separator+"Pictures") { // set up a file observer to watch this directory on sd card

                @Override
                public void onEvent(int event, String file) {
                    //if(event == FileObserver.CREATE && !file.equals(".probe")){ // check if its a "create" and not equal to .probe because thats created every time camera is launched




                    //                   event=8 finish write file...

                    BLog.e("FO", "File created ["+event+" : " +Environment.getExternalStorageDirectory().toString()+File.separator+ file + "]");

                    //Toast.makeText(getBaseContext(), file + " was saved!", Toast.LENGTH_LONG);
                    //}
                }
            };
            fileObserver.startWatching();
      */
		//IntentFilter powerconfilter = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
		//powerconreceiver = new OnPowerConnectReceiver();
		
		registerReceiver(pconreceiver, pconfilter);
		registerReceiver(pdisconreceiver, pdisconfilter);
		//registerReceiver(powerconreceiver, powerconfilter);
		
		registerReceiver(areceiver, afilter);
		registerReceiver(aoreceiver, aofilter);
		
		registerReceiver(treceiver, tfilter);
		registerReceiver(receiver, filter);
		registerReceiver(breceiver, bfilter);
		registerReceiver(ubreceiver, ubfilter);

        //    switchButtonListener = new SwitchButtonListener();
        //    registerReceiver(switchButtonListener, new IntentFilter(SWITCH_EVENT));
		
		ContactsObserver contentObserver = new ContactsObserver();
		this.getContentResolver().registerContentObserver (ContactsContract.Contacts.CONTENT_URI, true, contentObserver);


            //BriefService.doSmsDefaultsCheck(getBaseContext());
		
		//startService(new Intent(this, P2pChatConnection.class));
		//chatService = new Intent(BriefService.this, P2pChatService.class);
		//startService(chatService);
		//this.stopService(service);
		//service.
		//this.startService(chatService);
            startSyncsHandler.postDelayed(new Runnable() {
                public void run() {
                    startInternetRefresh();
                    BriefService.startRegularRefresh();
                    sendSeviceQueHandler.removeCallbacks(sendQueRunner);
                    sendSeviceQueHandler.postDelayed(sendQueRunner, MILLIS_SEND_QUE);

                    syncDataHandler.postDelayed(syncDataThread, 10000);
                }
            },2000);


            if(runIndexTask==null && Device.isMediaMounted()) {
                runIndexTask=new RunIndexingTask();
                runIndexTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
            }

    	}


    	return START_STICKY;
    }
/*
    private SwitchButtonListener switchButtonListener;
    public class SwitchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("TAG", "test");
        }

    }
*/

    private class ContactsObserver extends ContentObserver {

        public ContactsObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            //BLog.e("SERVICE", "contacts changed !!");
            shouldReloadContacts=true;
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
        	super.onChange(selfChange,uri);
            shouldReloadContacts=true;
        	//BLog.e("CONTACT_CHANGE", selfChange+", "+uri.toString());
        } 
    }
    public static void refreshSmsReceiver() {
    	if(SERVICE!=null)
    		SERVICE.refreshRegisterSmsReceiver();
    }
    private void refreshRegisterSmsReceiver() {
        //BLog.e("SERVICE","---IS RUNNING: "+BriefService.isBriefServiceRunning(getBaseContext()));
    	//ensureStartups(getBaseContext());
    	if(freceiver!=null)
    		unregisterReceiver(freceiver);
		IntentFilter ffilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        if(State.getSettings()!=null) {
            if (State.getSettings().getBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER))
                ffilter.setPriority(PRIORITY_SMS_INTERCEPT);
            else
                ffilter.setPriority(PRIORITY_SMS_LOW);
        } else {
            //BLog.e("SETNULL","Settings IS NULL !!!!!!!!!!!!!!");
            ffilter.setPriority(PRIORITY_SMS_INTERCEPT);
        }
		freceiver = new SmsReceiver();
		registerReceiver(freceiver, ffilter);

        //AppOpsManager manager=new AppOpsManager();
        //appOps.setMode(AppOpsManager.OP_WRITE_SMS, applicationData.mUid,
        //        applicationData.mPackageName, AppOpsManager.MODE_ALLOWED);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    	BLog.e("SERVICE","BriefService stopped");
        // Cancel the persistent notification.
        //mNM.cancel(NOTIFICATION);

        
        //ReceiverManager manager = ReceiverManager.getSingleton(this);
		unregisterReceiver(areceiver);
		unregisterReceiver(aoreceiver);
		unregisterReceiver(treceiver);
		unregisterReceiver(receiver);
		unregisterReceiver(breceiver);
		unregisterReceiver(ubreceiver);
        unregisterReceiver(freceiver);


        unregisterReceiver(pconreceiver);
        unregisterReceiver(pdisconreceiver);
		Intent pservice = new Intent(this, OnAlarmReceiver.class);
        fileObserver.stopWatching();
		//this.startService(service);
		stopService(pservice);
		
		//stopService(chatService);
		
		checkInternetHandler=null;
		syncDataHandler=null;
        //BriefService.finish(this);
        // Tell the user we stopped.
        //Toast.makeText(this, NOTIFICATION, Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

	
    public static boolean isBriefServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (BriefService.class.getName().equals(service.service.getClassName())) {
            	//BLog.e("TESTSERVICE","BRIEF SERVICE TEST = IS NOT RUNNING");
                return true;
            }
        }
        return false;
    }
    




    private class OnBootReceiver extends BroadcastReceiver {
    	  
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		//BLog.e("BriefService - OnBootReceiver","onReceive()");
    		if(!BriefService.isBriefServiceRunning(context)) {
    			Intent service = new Intent(context, BriefService.class);
    			//this.stopService(service);
    			context.startService(service);
    			
    		}
    	
    	}
    }
    
    private class OnPcConnectReceiver extends BroadcastReceiver {
    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	    synchronized(this) {
    	    	SERVICE.PC_CONNECTED=true;
    		}
    	
    	}
    }

    private class OnPcDisconnectReceiver extends BroadcastReceiver {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	    synchronized(this) {
    	    	SERVICE.PC_CONNECTED=false;
    	    	//BLog.e("BriefService - OnPcDisconnectReceiver","onReceive()");

    		}
    	
    	}
    }
    private class OnAlarmReceiver extends BroadcastReceiver {
    	    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	    synchronized(this) {
    	    	//BLog.e("BriefService - OnAlarmReceiver","onReceive()");
                startInternetRefresh();
                BriefService.startRegularRefresh();
    		}
    	
    	}
    }
    private class OnUserPresentReceiver extends BroadcastReceiver {
    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
    	    synchronized(this) {
    	    	//BLog.e("BriefService - OnUserPresentReceiver","onReceive()");
    	    	BriefSettings settings = State.getSettings();
    	    	if(settings.getBoolean(BriefSettings.BOOL_BRIEF_OPEN_ON_PRESENT) && !isAppStarted) {
    	    		BriefActivityManager.openBriefApp(context);
    	    	}
                if(SERVICE!=null) {

                    startRegularRefresh();


                } else {
                    BLog.e("BS","12- service is null");
                }

            }
    	
    	}
    }
    private class OnCallReceiver extends BroadcastReceiver {
    	  
    	  @Override
    	  public void onReceive(Context context, Intent intent) {
    	      TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
   	       	  telephonyManager.listen(new BriefPhoneListener(context), PhoneStateListener.LISTEN_CALL_STATE);
    	  }
    }



    private class BriefPhoneListener extends PhoneStateListener {

        //private static final String TAG = "PhoneStateChanged";
        Context context; //Context to make Toast if required 
        public BriefPhoneListener(Context context) {
            super();
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            super.onCallStateChanged(state, incomingNumber);
            SERVICE.TelephonyManagerSTATE = state;
            switch (state) {
	            case TelephonyManager.CALL_STATE_IDLE:
	                //when Idle i.e no call
	            	if(activeCall) {
	            		PhoneDb.refresh(SERVICE.getApplicationContext());
                        BriefManager.setDirty(BriefManager.IS_DIRTY_PHONE);
	            		activeCall=false;
	            	}

	                break;
	            case TelephonyManager.CALL_STATE_OFFHOOK:
	            	activeCall=true;
                    PhoneDb.refresh(SERVICE.getApplicationContext());
                    BriefManager.setDirty(BriefManager.IS_DIRTY_PHONE);
	                break;
	            case TelephonyManager.CALL_STATE_RINGING:

	                break;
	            default:
	                break;
            }

        }
    }
    


    

    private void startInternetRefresh() {
    	if(refreshInternet==null)
    		refreshInternet=new RefreshInternet();
    	if(checkInternetHandler!=null) {
	    	checkInternetHandler.removeCallbacks(refreshInternet);
	    	checkInternetHandler.postDelayed(refreshInternet, 3000);
    	}
    	

    }

    //private int currentConnectionType=Device.CONNECTION_TYPE_NONE;

    private class RefreshInternet implements Runnable {  

    	@Override
    	public void run() {
            int oldcurrentConnectionType=Device.getCONNECTION_TYPE();

    		Device.CheckInternet(getBaseContext());
    		if(oldcurrentConnectionType!=Device.getCONNECTION_TYPE()) {
                Log.e("CHANGE_INTERNET","connection has changed, old: "+oldcurrentConnectionType+", now: "+Device.getCONNECTION_TYPE());
                if(SERVICE!=null) {

                    startRegularRefresh();


                } else {
                    BLog.e("BS","12- service is null");
                }

            }
            //currentConnectionType=Device.getCONNECTION_TYPE();

            startInternetRefresh();

    	}

    }

    public static synchronized void goRefreshNetwork() {
        if(SERVICE!=null) {
            SERVICE.startInternetRefresh();
        }
    }


    private boolean isNewsRefreshing=false;
    private static final String syncNewsOnID = "sync.new.str.82519809332";
    private Activity refreshNewNowActivity;
    private NewsNowTask newsNowTask;

    public static boolean isNewsRefreshing() {
        return SERVICE.isNewsRefreshing;
    }
    public static void refrehNewsNow(Activity activity, boolean force) {
        SERVICE.refreshNewNowActivity=activity;
        SERVICE.newsNowTask = SERVICE.new NewsNowTask(force);
        SERVICE.newsNowTask.execute(true);

    }
    private class NewsNowTask extends AsyncTask<Boolean, Void, Boolean> {
        private boolean force;
        public NewsNowTask(boolean force) {
            this.force=force;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            refreshNews(SERVICE.refreshNewNowActivity,force);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            Bgo.refreshCurrentIfFragment(SERVICE.refreshNewNowActivity, NewsHomeFragment.class);
        }
    }
    private static void refreshNews(Context context,boolean force) {
        //int totalNew=0;
        if(!SERVICE.isNewsRefreshing) {
            synchronized (syncNewsOnID) {
                SERVICE.isNewsRefreshing = true;
                NewsFeedsDb.init(context);
                NewsItemsDb.init(context);
                HashMap<String, RssUserFeed> feeds = NewsFeedsDb.getUserFeeds();
                Set<String> keys = feeds.keySet();

                boolean hasUpdates = false;
                for (String key : keys) {
                    RssUserFeed feed = feeds.get(key);
                    if (feed != null) {
                        long lastUpdate = feed.getLong(RssUserFeed.LONG_LAST_UPDATE);
                        int collect_ = feed.getInt(RssUserFeed.INT_COLLECT_);
                        int active = feed.getInt(RssUserFeed.INT_ACTIVE);
                        long now = Cal.getUnixTime();

                        if (active != 0) {
                            long refreshTime = refreshNewsCollectTime(collect_);

                            if ( (force &&  (lastUpdate + (Cal.MINUTES_1_IN_MILLIS*5)) < now)
                                    || (lastUpdate + refreshTime) < now) {
                                // ok refresh news feed
                                BLog.e("BS.REFRESH", "collect: " + lastUpdate + "," + refreshTime + "," + now + " -- for: " + feed.getString(RssUserFeed.STRING_URL));
                                hasUpdates = true;
                                feed.setLong(RssUserFeed.LONG_LAST_UPDATE, Cal.getUnixTime());
                                NewsFeedsDb.updateUserFeed(feed);
                                new DoLoadForUserFeed(context).execute(feed);
                                BriefManager.setDirty(BriefManager.IS_DIRTY_NEWS);

                            }

                        }

                    }
                }
                SERVICE.isNewsRefreshing = false;
            }
        }
    }
    private static long refreshNewsCollectTime(int COLLECT_) {
        switch(COLLECT_) {
            case RssUserFeed.COLLECT_SLOW:
                return RssUserFeed.TIME_SLOW_MILLIS;
            case RssUserFeed.COLLECT_FAST:
                return RssUserFeed.TIME_FAST_MILLIS;
            default:
                return RssUserFeed.TIME_MEDIUM_MILLIS;
        }
    }




    public static void startRegularRefresh() {
        if(SERVICE.syncDataHandler!=null)
            SERVICE.syncDataHandler.removeCallbacks(SERVICE.syncDataThread);
    	if(SERVICE.syncDataThread==null) {
            SERVICE.syncDataThread=SERVICE.new SyncDataThread();
            //SERVICE.syncDataThread.run();
    	}
        if(!SERVICE.isrefreshing) {
            BLog.e("SERVICE","start syncDataHandler called");
            SERVICE.syncDataHandler.postDelayed(SERVICE.syncDataThread, 10000);
        }

    }

 
    
    private boolean isrefreshing=false;
    
    private class SyncDataThread  implements Runnable {  
    	@Override
    	public void run() {
            synchronized (this) {
                if (!isrefreshing) {

                    isrefreshing = true;
                    new SyncDataTask().execute(false);
                }
            }
    	}

    }
	private class SyncDataTask extends AsyncTask<Boolean, Void, Boolean> {
		
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected Boolean doInBackground(Boolean... params) {


            ensureStartups(getBaseContext());
            try {
                checkEmails(getBaseContext());
            } catch (Exception e) {}
            refreshNews(getBaseContext(),false);

			return true;
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
			isrefreshing=false;
            SERVICE.syncDataHandler.removeCallbacks(SERVICE.syncDataThread);
            SERVICE.syncDataHandler.postDelayed(SERVICE.syncDataThread, MILLIS_SYNC_DATA);

		}

	 
	}

	private static void checkEmails(Context context) {
		ArrayList<Account> emails = AccountsDb.getAllEmailAccounts();

		if(emails!=null && !emails.isEmpty()) {
			for(Account acc: emails) {
                checkEmailsFor(context,acc,false);
				
			}
			
			
		}
	}


    public static void checkEmailsFor(Context context, Account account, boolean force) {

        Date nowTime=new Date();
        //HomeFarm.init(context);
        ensureStartups(context);
        SyncDataDb.init(context);
        //BLog.e("SYNCSIZE","size: "+SyncDataDb.size());
        SyncData sync = SyncDataDb.getByAccountId( account.getLong(Account.LONG_ID));

        if(Device.CheckInternet(context)) {


            //synchronized ("acc.[]."+account.getLong(Account.LONG_ID)) {
            boolean active = isAccountActiveSyncing(account.getLong(Account.LONG_ID));
            //BLog.e("CHK", "test: " + !sync.isActive() + ", " + (sync.isActive() || force));
            if (account.getInt(Account.INT_SIGNATURE_REKEY) < 1 || force) {
                if (!active && (sync.isActive() || force)) {

                    //BLog.e("CHK", "test: " + sync.shouldSyncData(nowTime) + ", " + force);
                    if (sync.shouldSyncData(nowTime) || force) {

                        BLog.e("SYNC", "called for email: " + isAccountActiveSyncing(account.getLong(Account.LONG_ID)) + " - " + account.getString(Account.STRING_EMAIL_ADDRESS));
                        EmailServiceInstance es = EmailService.getService(context, account);
                        if (es != null) {
                            //BLog.e("EMS", "EMS instance not null, trying fetchlatestemails()");
                            es.fetchLatestEmails(context);


                        } else {
                            //BLog.e("EMS", "EMS instance IS null, failed fetchlatestemails()");
                            sync.setString(SyncData.STRING_MESSAGE_IN, SERVICE.getApplicationContext().getString(R.string.sync_msg_email_no_server));

                            SyncDataDb.update(sync);
                        }

                    }

                } else {
                    //BLog.e("SYNC", "!!!!!!!!!! IS ACTIVE OR WORKING - is active: "+sync.isActive()+"  for: " + account.getString(Account.STRING_EMAIL_ADDRESS));
                }
            }
        }
        //}
    }



    private Activity sendQueActivity;
    boolean servicequerunning=false;
    private Runnable sendQueRunner = new Runnable() {
        @Override
        public void run() {
            runServiceQueProcess();
        }
    };

    public static void addToSendServiceQue(Activity activity, Account account, int WITH_, JSONObject data) {
        SERVICE.sendQueActivity=activity;
        BriefSend bs = new BriefSend();
        if(account!=null) {
            //BLog.e("QUE", "added -- acc id: "+account.getLong(Account.LONG_ID));
            bs.setLong(BriefSend.LONG_ACCOUNT_ID, account.getLong(Account.LONG_ID));
        } else {
            bs.setLong(BriefSend.LONG_ACCOUNT_ID, -1);
        }
        bs.setString(BriefSend.STRING_BJSON_BEAN, data.toString());
        bs.setInt(BriefSend.INT_ATTEMPTS, 0);
        bs.setInt(BriefSend.INT_BRIEF_WITH, WITH_);
        long id=BriefSendDb.add(bs);

        //sendServiceQue.add(SERVICE.new SendServiceQueItem(account,dbindex));
        SERVICE.sendSeviceQueHandler.removeCallbacks(SERVICE.sendQueRunner);
        SERVICE.sendSeviceQueHandler.postDelayed(SERVICE.sendQueRunner, 200);
    }

    public static void sendServiceGo(Activity activity) {
        SERVICE.sendQueActivity=activity;
        SERVICE.sendSeviceQueHandler.removeCallbacks(SERVICE.sendQueRunner);
        SERVICE.sendSeviceQueHandler.postDelayed(SERVICE.sendQueRunner, 100);
    }

	private static void runServiceQueProcess() {
		synchronized("s:ervicer:unningq:ue") {
			if(!SERVICE.servicequerunning) {
                HomeFarm.init(SERVICE);
				SERVICE.servicequerunning=true;

				SERVICE.processSendQue=SERVICE.new ProcessSendQue();
				//SERVICE.processSendQue.setActivity(SERVICE.getApplicationContext());
				SERVICE.processSendQue.execute(true);
			}
		}
		
	}

    private static final int MAX_QUICK_SEND_ATTEMPT_AT=2;
    public static final int MAX_MEDIUM_SEND_ATTEMPT_AT=4;
    //private static final int MAX_LONG_SEND_ATTEMPT_AT=10;

	private class ProcessSendQue extends AsyncTask<Boolean, Void, Boolean> {
		//Context context;
        List<Class> tryClasses=new ArrayList<Class>();
        //boolean shouldRefreshBrief=false;
		//public void setActivity(Context context) {
		//	this.context=context;
		//}
		//Twitter mTwitter = new TwitterFactory().getInstance();
		@Override
		protected Boolean doInBackground(Boolean... params) {
            //BriefSendDb.deleteAll();
            if(SERVICE.sendQueActivity != null) {
                ArrayList<BriefSend> sends = BriefSendDb.getAllItems();
                List<BriefSend> sentok = new ArrayList<BriefSend>();

                if (sends != null && !sends.isEmpty()) {


                    //for(int i=0; i<sendServiceQue.size(); i++) {
                    for (BriefSend item : sends) {
                        //BLog.e("BRIEFSEND","attempts: "+item.getInt(BriefSend.INT_ATTEMPTS));
                        if (item.getInt(BriefSend.INT_ATTEMPTS) < MAX_MEDIUM_SEND_ATTEMPT_AT) {

                            JSONObject data = new JSONObject(item.getString(BriefSend.STRING_BJSON_BEAN));
                            if (item.getInt(BriefSend.INT_BRIEF_WITH) == Brief.WITH_SMS && Device.hasPhone()) {
                                //BLog.e("QUE", "item sms sending");
                                boolean isFlightMode=Device.isFlightModeOn(SERVICE.getBaseContext());

                                if(!isFlightMode && Device.hasActiveSim(SERVICE.getBaseContext())) {
                                    SmsSend sms = new SmsSend(data);
                                    //BLog.e("CALLSMS", "to sent trying now");
                                    boolean sent = SmsFunctions.SendSmsMessage(SERVICE.sendQueActivity, sms.getString(SmsSend.STRING_TO_NUMBER), sms.getString(SmsSend.STRING_MESSAGE));

                                    if (sent) {
                                        sentok.add(item);
                                        SmsDb.refresh(SERVICE.sendQueActivity);
                                        BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
                                        //BriefManager.refresh();
                                        //shouldRefreshBrief = true;
                                        tryClasses.add(SmsSendFragment.class);
                                    } else {
                                        BriefSendDb.incrementAttempts(item);
                                    }
                                } else {
                                    BriefSendDb.incrementAttempts(item);
                                }

                            } else if (item.getInt(BriefSend.INT_BRIEF_WITH) == Brief.WITH_EMAIL) {

                                Account acc = AccountsDb.getAccountById(item.getLong(BriefSend.LONG_ACCOUNT_ID));
                                if (acc != null) {
                                    Email em = new Email(data);
                                    if (item.getInt(BriefSend.INT_STATUS) == BriefSend.STATUS_SEND) {
                                        BLog.e("SYNC", "send email with: " + acc.getString(Account.STRING_EMAIL_ADDRESS));
                                        //BLog.e("SEM","status send, try sending....");
                                        //Email sendemail = inst.getEmail(Sf.toInt(item.dbindex));

                                        EmailServiceInstance emsi = EmailService.getService(SERVICE.sendQueActivity, acc);
                                        EmailSendService ems = new EmailSendService(acc, em);
                                        String retXBrief = ems.doSend(emsi, SERVICE.sendQueActivity);
                                        if (retXBrief != null) {
                                            //BLog.e("SEM","was sent");
                                            em.setString(Email.STRING_XBRIEFID, retXBrief);
                                            data = em.getBean();
                                            item.setString(BriefSend.STRING_BJSON_BEAN, data.toString());
                                            item.setInt(BriefSend.INT_STATUS, BriefSend.STATUS_CONFIRM);
                                            item.setInt(BriefSend.INT_ATTEMPTS,0);
                                            BriefSendDb.update(item);

                                            //BLog.e("QUE", "item email did send ok");
                                        } else {
                                            //BLog.e("SEM","was not sent");
                                            BriefSendDb.incrementAttempts(item);

                                        }

                                    }
                                    if (item.getInt(BriefSend.INT_STATUS) == BriefSend.STATUS_CONFIRM) {
                                        //BLog.e("SEM","status confirm, try confirming....");
                                        EmailServiceInstance inst = EmailService.getService(SERVICE.sendQueActivity, acc);
                                        Email confirmed=inst.confirmSentEmail(em);
                                        if(confirmed!=null) {
                                            sentok.add(item);
                                            BLog.e("SEM","confirmed sent OK :)");
                                            confirmed.setString(Email.STRING_ATTACHMENTS, em.getString(Email.STRING_ATTACHMENTS));

                                            Email added = inst.addEmail(confirmed);
                                            inst.loadEmails();

                                            BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);
                                            //BriefManager.refresh();

                                            tryClasses.add(EmailHomeFragment.class);
                                        } else {
                                            if(item.getInt(BriefSend.INT_ATTEMPTS)>2)
                                                sentok.add(item);
                                            else
                                                BriefSendDb.incrementAttempts(item);
                                        }
                                    }
                                    //shouldRefreshBrief = true;
                                }
                            }
                        }
                    }


                    // remove sent ok items
                    for (BriefSend in : sentok) {
                        BriefManager.setDirty(BriefManager.IS_DIRTY_SEND_ITEMS);
                        //BLog.e("REMOVE","brief send remove: "+in.toString());
                        BriefSendDb.remove(in);
                    }


                }
            }
			return true;
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
            SERVICE.servicequerunning=false;
            SERVICE.sendSeviceQueHandler.removeCallbacks(SERVICE.sendQueRunner);
			//BLog.e("bs", "size: "+BriefSendDb.size()+"--"+BriefSendDb.getAllItems().size());
            if(!tryClasses.isEmpty()) {
                BriefManager.setDirty(BriefManager.IS_DIRTY_SEND_ITEMS);
                tryClasses.add(BriefHomeFragment.class);
                shouldRefreshFragment(tryClasses);

            }
            //SERVICE.sendQueActivity=null;
			if(BriefSendDb.size()>0) {
                boolean hasQuickRetry=false;
                boolean hasMediumRetry=false;
                for(BriefSend send: BriefSendDb.getAllItems()) {
                    int attempts = send.getInt(BriefSend.INT_ATTEMPTS);
                    if(attempts<MAX_QUICK_SEND_ATTEMPT_AT)
                        hasQuickRetry=true;
                    else if(attempts<MAX_MEDIUM_SEND_ATTEMPT_AT)
                        hasMediumRetry=true;
                }

                if(hasQuickRetry)
                    SERVICE.sendSeviceQueHandler.postDelayed(SERVICE.sendQueRunner, MILLIS_SEND_QUE);
                else if(hasMediumRetry)
                    SERVICE.sendSeviceQueHandler.postDelayed(SERVICE.sendQueRunner, MILLIS_SEND_QUE*8);
                else
                    SERVICE.sendSeviceQueHandler.postDelayed(SERVICE.sendQueRunner, MILLIS_SEND_QUE*20);

            }
		}
	
		@Override
		protected void onPreExecute() {
		}
	
		@Override
		protected void onProgressUpdate(Void... values) {
			
		}
	 
	}
    private static void shouldRefreshFragment(List<Class> tryClasses) {
        if(SERVICE.sendQueActivity!=null) {
            for(Class c: tryClasses) {
                Bgo.refreshCurrentIfFragment(SERVICE.sendQueActivity, c);
            }
        }
    }


    public static void runLockerAddFilesTask(Activity activity, List<File> addFiles) {
        if(!SERVICE.isLockerAddTaskActive) {
            //BLog.e("TSK","START");
            SERVICE.addLockerItems = addFiles;
            //SERVICE.refreshLocker=refreshFragment;
            SERVICE.lockerAcivity = activity;
            SERVICE.lockerAddFilesTask=SERVICE.new LockerAddFilesTask();
            SERVICE.lockerAddFilesTask.execute(true);
        }
    }
    public static boolean isRunLockerAddFilesTaskActive() {
        return SERVICE.isLockerAddTaskActive;
    }

    private List<File> addLockerItems;
    private boolean isLockerAddTaskActive=false;
    //private BRefreshable refreshLocker;
    private Activity lockerAcivity;
    private LockerAddFilesTask lockerAddFilesTask;
    private class LockerAddFilesTask extends AsyncTask<Boolean, Void, Boolean> {

        private int added=0;
        @Override
        protected Boolean doInBackground(Boolean... params) {
            synchronized(this) {
                if(SERVICE.addLockerItems!=null) {
                    //BLog.e("TSK","ADD");
                    SERVICE.isLockerAddTaskActive=true;
                    added = LockerManager.addFilesToLocker(SERVICE.lockerAcivity,SERVICE.addLockerItems);
                    SERVICE.addLockerItems.clear();


                }
            }
            System.gc();
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            SERVICE.isLockerAddTaskActive=false;
            if(SERVICE.lockerAcivity!=null) {
                //BLog.e("TSK","FINISH REFRESH, added: "+added);
                Bgo.refreshCurrentIfFragment(SERVICE.lockerAcivity, LockerFragment.class);

            }
            SERVICE.lockerAcivity=null;
            SERVICE.addLockerItems=null;

        }

    };


    public static void runLockerRemoveFilesTask(Activity activity, List<LockerItem> removeFiles) {
        if(!SERVICE.isLockerRemoveTaskActive) {
            //BLog.e("TSK","START");
            SERVICE.removeLockerItems = removeFiles;
            //SERVICE.refreshLocker=refreshFragment;
            SERVICE.lockerAcivity = activity;
            SERVICE.lockerRemoveFilesTask = SERVICE.new LockerRemoveFilesTask();
            SERVICE.lockerRemoveFilesTask.execute(true);
        }
    }
    public static boolean isRunLockerRemoveFilesTaskActive() {
        return SERVICE.isLockerRemoveTaskActive;
    }

    private List<LockerItem> removeLockerItems;
    private boolean isLockerRemoveTaskActive=false;
    private LockerRemoveFilesTask lockerRemoveFilesTask;
    private class LockerRemoveFilesTask extends AsyncTask<Boolean, Void, Boolean> {

        private int removed=0;
        @Override
        protected Boolean doInBackground(Boolean... params) {
            synchronized(this) {
                if(SERVICE.removeLockerItems!=null) {
                    //BLog.e("TSK","ADD");
                    SERVICE.isLockerRemoveTaskActive=true;
                    for(LockerItem li: SERVICE.removeLockerItems) {
                        LockerManager.removeFileFromLocker(SERVICE.lockerAcivity,li);
                        removed++;
                    }
                    SERVICE.removeLockerItems.clear();


                }
            }
            System.gc();
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            SERVICE.isLockerRemoveTaskActive=false;
            if(SERVICE.lockerAcivity!=null) {
                //BLog.e("TSK","FINISH REFRESH, added: "+added);
                Bgo.refreshCurrentIfFragment(SERVICE.lockerAcivity, LockerFragment.class);

            }
            SERVICE.lockerAcivity=null;
            SERVICE.removeLockerItems=null;

        }

    };





    public static void runEmailFirstTimeTask(Activity activity, Account account) {
            SERVICE.emailFirstTimeAccount=account;
            SERVICE.emailFirstActivity=activity;
            //SERVICE.refreshLocker=refreshFragment;
            SERVICE.emailFirstTimeSetupTask = SERVICE.new EmailFirstTimeSetupTask();
            SERVICE.emailFirstTimeSetupTask.execute(true);

    }

    private EmailFirstTimeSetupTask emailFirstTimeSetupTask;
    private Account emailFirstTimeAccount;
    private Activity emailFirstActivity;

    private class EmailFirstTimeSetupTask extends AsyncTask<Boolean, Void, Boolean> {

        private int removed=0;
        @Override
        protected Boolean doInBackground(Boolean... params) {
            SyncData sync = SyncDataDb.getByAccountId(SERVICE.emailFirstTimeAccount.getLong(Account.LONG_ID));
            sync.setActive(true);
            SyncDataDb.update(sync);
            //sync.setActiveWorking(true);
            setAccountActiveSyncing(emailFirstTimeAccount.getLong(Account.LONG_ID), true);
            List<String> addFolders = new ArrayList<String>();

            if(SERVICE.emailFirstTimeAccount.getInt(Account.INT_EMAIL_USE_)==Account.EMAIL_USE_IMAP) {

                EmailServiceInstance es = EmailService.getService(SERVICE.emailFirstActivity,SERVICE.emailFirstTimeAccount);

                es.doConnect(SERVICE.emailFirstActivity);

                es.loadFoldersNoAsync();
                List<EmailFolder> folders = es.getLoadFolders();


                if (folders != null && !folders.isEmpty()) {
                    String testInbox = Email.FOLDER_INBOX.toLowerCase();
                    String testSent = Email.FOLDER_SENT.toLowerCase();
                    if (DefaultProperties.isGmail(SERVICE.emailFirstTimeAccount.getString(Account.STRING_EMAIL_ADDRESS))) {
                        addFolders.add("[Gmail]/Sent Mail");
                        addFolders.add("INBOX");
                    } else {
                        for (EmailFolder folder : folders) {
                            if (testInbox.equals(folder.getString(EmailFolder.STRING_FOLDERNAME).toLowerCase())) {
                                addFolders.add(folder.getString(EmailFolder.STRING_FOLDERNAME));
                            } else if (testSent.equals(folder.getString(EmailFolder.STRING_FOLDERNAME).toLowerCase())) {
                                addFolders.add(folder.getString(EmailFolder.STRING_FOLDERNAME));
                            }
                        }
                    }
                } else {
                    //es.disConnect();

                    // fail collect folder use defaults
                    if (DefaultProperties.isGmail(SERVICE.emailFirstTimeAccount.getString(Account.STRING_EMAIL_ADDRESS))) {
                        addFolders.add("[Gmail]/Sent Mail");
                        addFolders.add("INBOX");
                    } else {
                        addFolders.add(Email.FOLDER_INBOX);
                        addFolders.add(Email.FOLDER_SENT);
                    }
                    //es.connect(context);
                }
            } else {
                addFolders.add(Email.FOLDER_INBOX);
            }

            emailFirstTimeAccount.setEmailFolders(addFolders);
            AccountsDb.updateAccount(emailFirstTimeAccount);

            setAccountActiveSyncing(emailFirstTimeAccount.getLong(Account.LONG_ID), false);

            //--

            BriefService.checkEmailsFor(SERVICE.emailFirstActivity,SERVICE.emailFirstTimeAccount,true);



            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {


        }

    };


    public static void addEmailHistoryCollectFor(Activity activity, Account account) {
        GetMoreEmailHistoryTask task = SERVICE.new GetMoreEmailHistoryTask(activity,account);
        SERVICE.emailHistoryTasks.add(task);
        task.execute(true);
    }

    public static boolean isEmailHistoryTaskRunning() {
        if(SERVICE!=null) {
            for (GetMoreEmailHistoryTask task : SERVICE.emailHistoryTasks) {
                if (task.getStatus() == AsyncTask.Status.RUNNING) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void cleanUpTasks() {
        for(int i=SERVICE.emailHistoryTasks.size()-1; i>=0; i--) {
            GetMoreEmailHistoryTask task = SERVICE.emailHistoryTasks.get(i);
            if(task.getStatus() == AsyncTask.Status.PENDING){
                // My AsyncTask has not started yet
                task.execute(true);
            }
            if(task.getStatus() == AsyncTask.Status.FINISHED){
                SERVICE.emailHistoryTasks.remove(i);
            }

        }
    }
    private List<GetMoreEmailHistoryTask> emailHistoryTasks = new ArrayList<GetMoreEmailHistoryTask>();

    private class GetMoreEmailHistoryTask extends AsyncTask<Boolean, Void, Boolean> {

        private Account account;
        private Activity activity;
        public GetMoreEmailHistoryTask(Activity activity,Account account) {
            this.account=account;
            this.activity=activity;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
//BLog.e("FROMREM","history remote start");
            setAccountActiveSyncing(account.getLong(Account.LONG_ID),true);
            EmailServiceInstance es = EmailService.getService(activity, account);
            if (es != null) {
                es.checkHistoryNoAsync();//.fetchLatestEmails();

            }
            setAccountActiveSyncing(account.getLong(Account.LONG_ID),false);
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            Bgo.refreshCurrentFragment(activity);
            cleanUpTasks();
        }

    };
    public static boolean loadFoldersNoAsync(Activity activity, Account useaccount) {
        setAccountActiveSyncing(useaccount.getLong(Account.LONG_ID),true);
        EmailServiceInstance emsi = EmailService.getService(activity,useaccount);
        emsi.loadFoldersNoAsync();
        setAccountActiveSyncing(useaccount.getLong(Account.LONG_ID),false);
        return true;
    }



    /*

    File manager service tasks

     */


    private class RunIndexingTask extends AsyncTask<Boolean, Void, Boolean> {

        private Context context;

        private void setActivity(Context context) {
            this.context=context;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            BLog.e("RunIndexingTask activated");
            IndexerDb.init(SERVICE);
            //IndexerDb.getDb().deleteAll();
            //State.getSettings().setInt();

            Indexer.refresh(SERVICE);


            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {

        }

    }


    // move files
    private boolean isMoving=false;
    private RunMovingTask moveTask;
    public static boolean isMoving() {
        if(SERVICE==null)
            return false;
        return SERVICE.isMoving;
    }
    public static boolean MoveFiles(Activity activity, List<FileItem>files, String moveToPath) {
        SERVICE.activity=activity;
        if(SERVICE.isMoving)
            return false;

        //SERVICE.zipfile = new FileItem(archiveFilePath+File.separator+archiveFilename);
        SERVICE.isMoving=true;
        SERVICE.moveTask = SERVICE.new RunMovingTask();
        SERVICE.moveTask.setMoveToPath(moveToPath);
        SERVICE.moveTask.setList(files);
        SERVICE.moveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        return true;
    }

    private class RunMovingTask extends AsyncTask<Boolean, Void, Boolean> {

        //private FileItem zipfile;
        List<FileItem>files;
        private String moveToPath;

        private void setList(List<FileItem>files) {
            this.files=files;
        }
        private void setMoveToPath(String path) {
            moveToPath=path;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            if(files!=null && !files.isEmpty()) {
                for (FileItem file : files) {
                    File renamed = new File(Files.getAvailableIncrementedFilePath(moveToPath+File.separator+file.getName()));
                    file.renameTo(renamed);
                }

            }
            //Indexer.refresh(SERVICE);


            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {

            SERVICE.isMoving=false;
            Bgo.refreshCurrentFragment(SERVICE.activity);
        }

    }



    // paste

    private boolean isPasting=false;
    private RunPastingTask pasteTask;
    public static boolean isPasting() {
        if(SERVICE==null)
            return false;
        return SERVICE.isPasting;
    }
    public static boolean PasteFiles(Activity activity, List<FileItem>files, String pasteToPath) {
        SERVICE.activity=activity;
        if(SERVICE.isPasting)
            return false;

        //SERVICE.zipfile = new FileItem(archiveFilePath+File.separator+archiveFilename);
        SERVICE.isPasting=true;
        SERVICE.pasteTask = SERVICE.new RunPastingTask();
        SERVICE.pasteTask.setPasteToPath(pasteToPath);
        SERVICE.pasteTask.setList(files);
        SERVICE.pasteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        return true;
    }

    private class RunPastingTask extends AsyncTask<Boolean, Void, Boolean> {

        //private FileItem zipfile;
        List<FileItem>files;
        private String pasteToPath;

        private void setList(List<FileItem>files) {
            this.files=files;
        }
        private void setPasteToPath(String path) {
            pasteToPath=path;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            if(files!=null && !files.isEmpty()) {
                InputStream inStream = null;
                OutputStream outStream = null;
                for (FileItem file : files) {

                    try{

                        //File afile =new File("C:\\folderA\\Afile.txt");
                        File bfile =new File(Files.getAvailableIncrementedFilePath(pasteToPath+File.separator+file.getName()));
                        bfile.createNewFile();
                        inStream = new FileInputStream(file);
                        outStream = new FileOutputStream(bfile);

                        byte[] buffer = new byte[1024];

                        int length;
                        //copy the file content in bytes
                        while ((length = inStream.read(buffer)) > 0){

                            outStream.write(buffer, 0, length);

                        }

                        inStream.close();
                        outStream.close();

                    }catch(IOException e){
                        BLog.e("paste path: " + e.getMessage());
                    }
                }




            }
            //Indexer.refresh(SERVICE);


            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            SERVICE.isPasting=false;
            Bgo.refreshCurrentFragment(SERVICE.activity);
        }

    }



    private boolean isUnArchiving=false;
    private RunUnArchivingTask unArchiveTask;
    private int untotalfiles;
    private int uncurrentarchivecount;
    private HashMap<String,Boolean> unzippedfiles;
    //private FileItem unzipfile;

    public static void clearUnzippedFiles() {
        SERVICE.unzippedfiles=null;
    }
    public static final boolean isUnzipCompleteFor(String filename) {
        if(SERVICE.unzippedfiles!=null)
            return SERVICE.unzippedfiles.get(filename);
        return false;
    }
    public static boolean isUnArchiving() {
        if(SERVICE==null)
            return false;
        return SERVICE.isUnArchiving;
    }
    public static int getCurrentUnArchiveCount() {
        return SERVICE.uncurrentarchivecount;
    }
    public static int getCurrentUnArchiveTotal() {
        return SERVICE.untotalfiles;
    }
    public static void setCurrentUnArchivedCount(int count) {
        SERVICE.uncurrentarchivecount=count;
    }

    public static boolean unArchiveFiles(String archiveFilePath, String toFolder) {
        if(isUnArchiving())
            return false;

        SERVICE.isUnArchiving=true;
        SERVICE.unArchiveTask = SERVICE.new RunUnArchivingTask();
        SERVICE.unArchiveTask.setZipFile(new FileItem(archiveFilePath));
        SERVICE.unArchiveTask.setToFolder(toFolder);
        SERVICE.unArchiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        return true;
    }

    private class RunUnArchivingTask extends AsyncTask<Boolean, Void, Boolean> {

        private FileItem zipfile;
        private String unzipfolder;

        private void setZipFile(FileItem zipfile) {
            this.zipfile=zipfile;
        }
        private void setToFolder(String unzipfolder) {
            this.unzipfolder=unzipfolder;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            SERVICE.unzippedfiles=new HashMap<String,Boolean>();
            UnZip.extract(zipfile.getAbsolutePath(), unzipfolder,SERVICE.unzippedfiles);
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            SERVICE.isUnArchiving=false;
        }

    }





    private boolean isArchiving=false;
    private RunArchivingTask archiveTask;
    private int totalfiles;
    private int currentarchivecount;
    private FileItem zipfile;


    public static final FileItem getCurrentArhiveFileItem() {
        return SERVICE.zipfile;
    }
    public static boolean isArchiving() {
        if(SERVICE==null)
            return false;
        return SERVICE.isArchiving;
    }
    public static int getCurrentArchiveCount() {
        return SERVICE.currentarchivecount;
    }
    public static int getCurrentArchiveTotal() {
        return SERVICE.totalfiles;
    }
    public static void setCurrentArchivedCount(int count) {
        SERVICE.currentarchivecount=count;
    }

    public static boolean ArchiveFiles(String archiveFilePath, String archiveFilename, List<FileItem>files) {
        if(SERVICE.isArchiving())
            return false;

        SERVICE.zipfile = new FileItem(archiveFilePath+File.separator+archiveFilename);
        SERVICE.isArchiving=true;
        SERVICE.archiveTask = SERVICE.new RunArchivingTask();
        SERVICE.archiveTask.setZipFile(SERVICE.zipfile);
        SERVICE.archiveTask.setList(files);
        SERVICE.archiveTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true);
        return true;
    }

    private class RunArchivingTask extends AsyncTask<Boolean, Void, Boolean> {

        private FileItem zipfile;
        List<FileItem>files;

        private void setZipFile(FileItem zipfile) {
            this.zipfile=zipfile;
        }
        private void setList(List<FileItem>files) {
            this.files=files;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            //BLog.e("POINT", "1");
            //IndexerDb.init(SERVICE);
            //Log.e("DB", "DB SIZE: " + IndexerDb.getDb().getSizeOnDisk());
            try {
                wait(1000);
            } catch(Exception e) {}
            Zip.compress(zipfile.getParent(),zipfile.getName(),files);
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            SERVICE.isArchiving=false;
        }

    }
}
