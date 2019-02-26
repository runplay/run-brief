package run.brief.menu;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

import run.brief.R;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.fab.Fab;
import run.brief.b.fab.FabMini;
import run.brief.contacts.ContactsHomeFragment;
import run.brief.email.EmailHomeFragment;
import run.brief.email.EmailSendFragment;
import run.brief.locker.LockerFragment;
import run.brief.notes.NotesEditFragment;
import run.brief.settings.AccountsDb;
import run.brief.sms.SmsSendFragment;
import run.brief.util.BriefActivityManager;
import run.brief.util.Functions;

public class BriefMenu {

	//private static 
	private static final BriefMenu MENU = new BriefMenu();
	private boolean menuShown = false;
	private View menu;
	//private boolean previewShown = false;
	private View preview;
	//private Brief brief;
	
	//private boolean stopHide=false;
	private RelativeLayout content;
	private RelativeLayout parent;
	private int menuSize;
	private int windowWidth;
	private int windowHeight;
	private int statusHeight = 0;
	private Activity activity;
	private static final int MENU_SIDES_SHIFT=70;
	private static final int MENU_TOP_BOTTOM_SHIFT=0;

	private Handler goFabAnimateHandler;

	private FabMini fabemail;
	private FabMini fabsms;
	private FabMini fabphone;
	private FabMini fabnote;
	private FabMini fablock;
	private FabMini fabcontact;

	List<Double> xcoord = new ArrayList<Double>();
	List<Double> ycoord = new ArrayList<Double>();

    //private List<BriefMenuAdapter.MenuDesc> menulistitems=new ArrayList<BriefMenuAdapter.MenuDesc>();

	
	public static void init(Activity activity) {
	    MENU.activity = activity;
	    boolean fullScreen = (activity.getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) != 0;
	    if(!fullScreen) {
	    	//Rect rectgle = new Rect();
	        //Window window = activity.getWindow();
	        //window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
	        //MENU.statusHeight = rectgle.top;
	        MENU.statusHeight = 0;//getStatusBarHeight2();
	    }
	    Display display = MENU.activity.getWindowManager().getDefaultDisplay();
	    Point size = new Point();
	    display.getSize(size);
	    MENU.windowHeight=size.y;
	    MENU.windowWidth = size.x;//Functions.dpToPx(, MENU.activity);

		for(int i=0;i<360;i++) {
			MENU.xcoord.add(10 * Math.cos(i));
			MENU.ycoord.add(20 * Math.sin(i));
		}


	    //Log.e("WIN_WIDTH",""+MENU.windowWidth);
	}

	public static boolean isMenuShowing() {
		return MENU.menuShown;
	}
	//call this in your onCreate() for screen rotation
	public static void ensureMenuOff() {
	    if(MENU.menuShown)
	        hideMenu();
	    MENU.menuShown = false;
	}
	public static void showMenu() {
	//get the height of the status bar
	    //if(statusHeight == 0) {
	        //Rect rectgle = new Rect();
	        //Window window = activity.getWindow();
	        //window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
	        //statusHeight = rectgle.top;
	        //statusHeight = activity.getWindow().;
	     //   }
	    showMenu(true);
	}

    /*
	public OnClickListener silentModeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(Device.isRingerSilentMode(MENU.activity)) {
				Device.setRingerModeOn(MENU.activity,false);
				//btnVolumemode.setImageDrawable(MENU.activity.getResources().getDrawable(R.drawable.device_access_mic_muted));
			} else {
				Device.setRingerModeOn(MENU.activity,false);
				//btnVolumemode.setImageDrawable(MENU.activity.getResources().getDrawable(R.drawable.device_access_mic));
			}
			//Bgo.openFragment(activity, new BriefHomeFragment());	
			//hideMenu();
		}
	};
    */

	public static void fillMenu() {



	}

	private boolean canclick=true;
	private Handler canClickHandler;

	public static void hideMenu() {

		if(MENU.canclick && MENU.menu!=null) {
			if(MENU.menuShown==true) {

				MENU.canclick=false;
				MENU.canClickHandler=new Handler();
				MENU.canClickHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						MENU.canclick=true;
					}
				},200);

				MENU.menuShown = false;
				TranslateAnimation ta = new TranslateAnimation(0, 0,0, MENU.windowHeight);
				ta.setDuration(200);

				MENU.menu.startAnimation(ta);
				MENU.parent.removeView(MENU.menu);


				View bg=(View) MENU.activity.findViewById(R.id.container);
				bg.setAlpha(1F);


				RelativeLayout.LayoutParams parm = (RelativeLayout.LayoutParams) MENU.content.getLayoutParams();
				parm.setMargins(0, 0, 0, 0);
				MENU.content.setLayoutParams(parm);

			} else {
				MENU.content.clearAnimation();
			}
			Fab.setStyle(MENU.activity,R.color.brand,R.color.brand_light,R.color.brand_light,R.drawable.content_new);

		}
	}

	public static void showMenu(boolean animate) {

		if(MENU.canclick) {
			Device.hideKeyboard(MENU.activity);
			MENU.canclick=false;
			MENU.canClickHandler=new Handler();
			MENU.canClickHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					MENU.canclick=true;
				}
			},200);

			if (MENU.menuSize == 0)
				MENU.menuSize = Functions.dpToPx(280, MENU.activity);

			View hl = (View) MENU.activity.findViewById(R.id.home_layout);
			if (hl != null) {

				MENU.content = ((RelativeLayout) MENU.activity.findViewById(R.id.home_layout).getParent());


				RelativeLayout.LayoutParams parm = (RelativeLayout.LayoutParams) MENU.content.getLayoutParams();
				MENU.content.setLayoutParams(parm);
				//MENU.content.setX(MENU_TOP_BOTTOM_SHIFT);

				View bg = (View) MENU.activity.findViewById(R.id.container);

				MENU.parent = (RelativeLayout) MENU.content.getParent();
				LayoutInflater inflater = (LayoutInflater) MENU.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

				RelativeLayout.LayoutParams lays = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

				MENU.menu = inflater.inflate(R.layout.brief_menu, null);
				MENU.menu.setLayoutParams(lays);
				MENU.parent.addView(MENU.menu);
				//}

				TranslateAnimation ta = new TranslateAnimation(0, 0, MENU.windowHeight, 0);
				ta.setDuration(150);

				if (animate) {
					//MENU.content.startAnimation(tac);
					MENU.menu.startAnimation(ta);
				}
				View overlay = MENU.menu.findViewById(R.id.menu_overlay);
				View overlaybg = MENU.menu.findViewById(R.id.menu_background);
				overlaybg.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						//BLog.e("OVERLAY CLICKED");
						//BriefMenu.hideMenu();
					}
				});
				overlay.setBackgroundColor(MENU.activity.getResources().getColor(R.color.menu_overlay));

				//Functions.enableDisableViewGroup(MENU.parent, false);
				MENU.menuShown = true;
				fillMenu();

			}

			MENU.goFabAnimateHandler = new Handler();
			MENU.goFabAnimateHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					goFabAnimate();
				}
			}, 30);
			Fab.setStyle(MENU.activity,R.color.grey,R.color.brand,R.color.brand,R.drawable.navigation_expand);

		}
	}
	private OnClickListener emailClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			//BLog.e("FAB CLICKED");
			BriefMenu.hideMenu();

			Bgo.openFragmentAnimate(MENU.activity,EmailSendFragment.class);

		}
	};
	private OnClickListener emailNoneClicked = new OnClickListener() {
		@Override
		public void onClick(View v) {
			///BLog.e("FAB CLICKED");
			BriefMenu.hideMenu();
			Bgo.openFragmentAnimate(MENU.activity,EmailHomeFragment.class);

		}
	};
	private OnClickListener smsClicked=new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			//BLog.e("FAB CLICKED");
			BriefMenu.hideMenu();
			Bgo.openFragmentAnimate(MENU.activity,SmsSendFragment.class);

		}
	};

	private OnClickListener phoneClicked=new OnClickListener() {
		@Override
		public void onClick(View v) {
			//BLog.e("FAB CLICKED");
			BriefMenu.hideMenu();
			BriefActivityManager.openPhone(activity, "");

		}
	};
	private OnClickListener notesClicked=new OnClickListener() {
		@Override
		public void onClick(View v) {
			//BLog.e("FAB CLICKED");
			BriefMenu.hideMenu();
			Bgo.openFragmentAnimate(MENU.activity,NotesEditFragment.class);

		}
	};
	private OnClickListener lockerClicked=new OnClickListener() {
		@Override
		public void onClick(View v) {
			//BLog.e("FAB CLICKED");
			BriefMenu.hideMenu();
			Bgo.openFragmentAnimate(MENU.activity,LockerFragment.class);

		}
	};
	private OnClickListener contactsClicked=new OnClickListener() {
		@Override
		public void onClick(View v) {
			//BLog.e("FAB CLICKED");
			BriefMenu.hideMenu();
			Bgo.openFragmentAnimate(MENU.activity,ContactsHomeFragment.class);

		}
	};
	private static void goFabAnimate() {
		//Display display = MENU.activity.getWindowManager().getDefaultDisplay();
		DisplayMetrics metrics = new DisplayMetrics();
		MENU.activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		//Point size = new Point();
		//display.getSize(size);
		//int width = size.x;
		//int height = size.y;
//BLog.e("x: "+metrics.widthPixels+",   y: "+metrics.heightPixels);
		Integer sh1=Functions.dpToPx(75,MENU.activity);
		Integer sh2=Functions.dpToPx(150,MENU.activity);
		Integer sh3=Functions.dpToPx(225,MENU.activity);
		if(metrics.widthPixels>metrics.heightPixels) {
			sh1=Functions.dpToPx(65,MENU.activity);
			sh2=Functions.dpToPx(130,MENU.activity);
			sh3=Functions.dpToPx(195,MENU.activity);
		}


		boolean hasEmail= AccountsDb.hasEmailAccounts();

		MENU.fabcontact=new FabMini(MENU.menu, R.id.fab_contact, MENU.contactsClicked);
		MENU.fablock=new FabMini(MENU.menu, R.id.fab_lock, MENU.lockerClicked);
		MENU.fabnote=new FabMini(MENU.menu, R.id.fab_note, MENU.notesClicked);
		MENU.fabcontact.setStyle(MENU.activity,R.color.brand,R.color.grey,R.color.grey,R.drawable.i_social);
		MENU.fablock.setStyle(MENU.activity,R.color.brand,R.color.grey,R.color.grey,R.drawable.i_lock);
		MENU.fabnote.setStyle(MENU.activity,R.color.actionbar_notes,R.color.actionbar_notes,R.color.grey,R.drawable.i_note);


		if(hasEmail) {
			MENU.fabemail = new FabMini(MENU.menu, R.id.fab_email, MENU.emailClicked);
		} else {
			MENU.fabemail = new FabMini(MENU.menu, R.id.fab_email, MENU.emailNoneClicked);
		}
		MENU.fabemail.setStyle(MENU.activity,R.color.actionbar_email,R.color.grey,R.color.grey,R.drawable.i_email);

		//if(Device.hasPhone()) {
			MENU.fabsms = new FabMini(MENU.menu, R.id.fab_sms, MENU.smsClicked);
			MENU.fabsms.setStyle(MENU.activity, R.color.actionbar_sms, R.color.grey, R.color.grey, R.drawable.i_sms);
			MENU.fabphone = new FabMini(MENU.menu, R.id.fab_phone, MENU.phoneClicked);
			MENU.fabphone.setStyle(MENU.activity, R.color.actionbar_phone, R.color.grey, R.color.grey, R.drawable.i_phone);

			MENU.fabsms.bringToFront();
			MENU.fabphone.bringToFront();
		//}
		MENU.fabemail.bringToFront();

/*
		Animation anim1=new TranslateAnimation(0, 0, 0, sh1);
		anim1.setDuration(200);
		anim1.setFillEnabled(true);
		MyAnimationListener listener=MENU.new MyAnimationListener(MENU.fabsms, 0,sh1,MENU.activity);
		anim1.setAnimationListener(listener);

*/




		if(Device.hasPhone()) {
				MENU.fabemail.getFab().animate().setDuration(150).translationY(-sh1.floatValue());
				MENU.fabsms.getFab().animate().setDuration(200).translationY(-sh2.floatValue());
				MENU.fabphone.getFab().animate().setDuration(250).translationY(-sh3.floatValue());


		} else {
			MENU.fabemail.getFab().animate().setDuration(150).translationY(-sh1.floatValue());
		}
		MENU.fabnote.getFab().animate().setDuration(150).translationX(-sh1.floatValue());
		MENU.fabcontact.getFab().animate().setDuration(200).translationX(-sh2.floatValue());
		MENU.fablock.getFab().animate().setDuration(250).translationX(-sh3.floatValue());


		/*
		ObjectAnimator mover = ObjectAnimator.ofFloat(MENU.fabemail, "translationY", 0, 100);
		mover.start();
		ObjectAnimator mover2 = ObjectAnimator.ofFloat(MENU.fabsms, "translationY", 0, 200);
		mover2.start();
		ObjectAnimator mover3 = ObjectAnimator.ofFloat(MENU.fabphone, "translationY", 0, 300);
		mover3.start();
		*/

	}

/*
	private Handler finishOpen;

	private static void finishOpen() {
		MENU.fabsms.setOnClickListner(MENU.smsClicked);
		MENU.fabemail.setOnClickListner(MENU.emailClicked);
		MENU.fabphone.setOnClickListner(MENU.phoneClicked);
	}
*/
	public class MyAnimationListener implements Animation.AnimationListener {
		FabMini mView;
		int modifierY;
		int modifierX;
		Context mContext;

		public MyAnimationListener(FabMini v, int modifierY, int modifierX, Context c){
			mView=v;
			this.modifierY=modifierY;
			this.modifierX=modifierX;
			mContext=c;
		}
		public void onAnimationEnd(Animation animation) {
			if(modifierY!=0) {
				ObjectAnimator mover = ObjectAnimator.ofFloat(MENU.fabemail, "translationY", 0, modifierY);
				mover.start();
			}
			if(modifierX!=0) {
				ObjectAnimator mover = ObjectAnimator.ofFloat(MENU.fabemail, "translationY", 0, modifierX);
				mover.start();
			}

		}

		public void onAnimationRepeat(Animation animation) {}

		public void onAnimationStart(Animation animation) {}

	}
/*



	public static void fillMenuold() {



		//GridView grid = (GridView) MENU.activity.findViewById(R.id.menu_gridview);
	    //BriefMenuAdapter.MenuDesc[] items = new BriefMenuAdapter.MenuDesc[8];
        MENU.menulistitems=new ArrayList<BriefMenuAdapter.MenuDesc>();
	    //fill the menu-items here

		if(Device.hasPhone()) {
			BriefMenuAdapter.MenuDesc mphone = new BriefMenuAdapter.MenuDesc();
			mphone.icon = R.drawable.i_phone;
			mphone.label = MENU.activity.getResources().getString(R.string.title_phone);
			mphone.id=3;
			MENU.menulistitems.add(mphone);


			BriefMenuAdapter.MenuDesc msms = new BriefMenuAdapter.MenuDesc();
			msms.icon = R.drawable.i_sms;
			msms.label = MENU.activity.getResources().getString(R.string.title_sms);
			msms.id=4;
			MENU.menulistitems.add(msms);

		}
		//ArrayList<Account> emailAccounts = AccountsDb.getAllEmailAccounts();
		//if(!emailAccounts.isEmpty()) {
			BriefMenuAdapter.MenuDesc mem = new BriefMenuAdapter.MenuDesc();
			mem.icon = R.drawable.i_email;
			mem.label = MENU.activity.getResources().getString(R.string.title_email);
			mem.id=8;
			MENU.menulistitems.add(mem);
		//}
		if(!NewsFeedsDb.getUserFeeds().isEmpty()) {
			BriefMenuAdapter.MenuDesc mnews = new BriefMenuAdapter.MenuDesc();
			mnews.icon = R.drawable.i_news;
			mnews.label = MENU.activity.getResources().getString(R.string.title_news);
			mnews.id=9;
			MENU.menulistitems.add(mnews);
		}


        BriefMenuAdapter.MenuDesc mfiles=new BriefMenuAdapter.MenuDesc();
        mfiles.icon=R.drawable.collections_collection;
        mfiles.label=MENU.activity.getResources().getString(R.string.label_files);
        mfiles.id=0;
        MENU.menulistitems.add(mfiles);

        BriefMenuAdapter.MenuDesc mcontacts=new BriefMenuAdapter.MenuDesc();
        mcontacts.icon=R.drawable.i_social;
        mcontacts.label=MENU.activity.getResources().getString(R.string.label_contacts);
        mcontacts.id=1;
        MENU.menulistitems.add(mcontacts);

        BriefMenuAdapter.MenuDesc mlocker=new BriefMenuAdapter.MenuDesc();
	    mlocker.icon=R.drawable.locker;
	    mlocker.label=MENU.activity.getResources().getString(R.string.title_locker);
        mlocker.id=5;
        MENU.menulistitems.add(mlocker);

        BriefMenuAdapter.MenuDesc msettings=new BriefMenuAdapter.MenuDesc();
	    msettings.icon=R.drawable.action_settings;
	    msettings.label=MENU.activity.getResources().getString(R.string.action_settings);
        msettings.id=6;
        MENU.menulistitems.add(msettings);

        BriefMenuAdapter.MenuDesc maccounts=new BriefMenuAdapter.MenuDesc();
        maccounts.icon=R.drawable.content_edit;
        maccounts.label=MENU.activity.getResources().getString(R.string.action_accounts);
        maccounts.id=7;
        MENU.menulistitems.add(maccounts);

		BriefMenuAdapter.MenuDesc mcamera =new BriefMenuAdapter.MenuDesc();
		mcamera.icon=R.drawable.device_access_camera;
		mcamera.label=MENU.activity.getResources().getString(R.string.label_camera);
		mcamera.id=2;
		MENU.menulistitems.add(mcamera);

		BriefMenuAdapter.MenuDesc mnote =new BriefMenuAdapter.MenuDesc();
		mnote.icon=R.drawable.i_note;
		mnote.label=MENU.activity.getResources().getString(R.string.label_note);
		mnote.id=10;
		MENU.menulistitems.add(mnote);

        BriefMenuAdapter.MenuDesc[] items = new BriefMenuAdapter.MenuDesc[MENU.menulistitems.size()];
        for(int i=0; i<MENU.menulistitems.size(); i++) {
            items[i] = MENU.menulistitems.get(i);
        }


	    BriefMenuAdapter adap = new BriefMenuAdapter(MENU.activity, items);
	    //grid.setAdapter(adap);
		Fab.show();
		//BriefManager.activateController(MENU.activity,MENU.);
	}
	public static void showMenuold(boolean animate) {

		if(MENU.menuSize==0)
			MENU.menuSize = Functions.dpToPx(280, MENU.activity);

		View hl=(View) MENU.activity.findViewById(R.id.home_layout);
		if(hl!=null) {
			//MENU.activity.getActionBar().hide();
		//MENU.content = ((LinearLayout) MENU.activity.findViewById(android.R.id.content).getParent());
			MENU.content = ((RelativeLayout) MENU.activity.findViewById(R.id.home_layout).getParent());


			RelativeLayout.LayoutParams parm = (RelativeLayout.LayoutParams) MENU.content.getLayoutParams();

		    //parm.setMargins(MENU.menuSize-MENU_SIDES_SHIFT, MENU_TOP_BOTTOM_SHIFT, -(MENU.menuSize+MENU_SIDES_SHIFT), -MENU_TOP_BOTTOM_SHIFT);
		    MENU.content.setLayoutParams(parm);
		    //MENU.content.setX(MENU_TOP_BOTTOM_SHIFT);

		    View bg=(View) MENU.activity.findViewById(R.id.container);


		    MENU.parent = (RelativeLayout) MENU.content.getParent();
		    //if(menu==null) {
		    LayoutInflater inflater = (LayoutInflater) MENU.activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		    RelativeLayout.LayoutParams lays = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
		    //lays.setMargins(0,MENU.statusHeight, 0, 0);

		    //lays.setMargins(0, MENU.activity.getActionBar().getHeight() , 0, 0);

		    MENU.menu = inflater.inflate(R.layout.brief_menu, null);
		    MENU.menu.setLayoutParams(lays);
		    MENU.parent.addView(MENU.menu);
		    //}



	TranslateAnimation ta = new TranslateAnimation(0, 0, MENU.windowHeight, 0);
	ta.setDuration(150);

	if(animate) {
		//MENU.content.startAnimation(tac);
		MENU.menu.startAnimation(ta);
	}
	View backg = MENU.menu.findViewById(R.id.menu_background);
	backg.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
			BriefMenu.hideMenu();
		}
	});
	//backg.setBackgroundColor(MENU.activity.getResources().getColor(R.color.menu_overlay));

	//Functions.enableDisableViewGroup(MENU.parent, false);
	MENU.menuShown = true;
	fillMenu();
} else {
		//MENU.activity.getActionBar().show();
		//BLog.e("NO", "no home_layout");
		}
		}

public static void hideMenuold() {

		if(MENU.menu!=null) {
		if(MENU.menuShown==true) {
		//if(MENU.activity!=null)
		//	MENU.activity.getActionBar().show();
		MENU.menuShown = false;
		TranslateAnimation ta = new TranslateAnimation(0, 0,0, MENU.windowHeight);
		ta.setDuration(200);
		//MENU.menu.findViewById(R.id.menu_overlay).setAlpha(0F);
		MENU.menu.startAnimation(ta);

		MENU.parent.removeView(MENU.menu);


		View bg=(View) MENU.activity.findViewById(R.id.container);
		//MENU.content.setBackgroundColor(MENU.activity.getResources().getColor(R.color.background));
		bg.setAlpha(1F);


		RelativeLayout.LayoutParams parm = (RelativeLayout.LayoutParams) MENU.content.getLayoutParams();
		parm.setMargins(0, 0, 0, 0);
		MENU.content.setLayoutParams(parm);
		//Functions.enableDisableViewGroup(MENU.parent, true);

		} else {
		MENU.content.clearAnimation();
		//MENU.menu.clearAnimation();
		}


		}
		}
	//just a simple adapter
	public static class BriefMenuAdapter extends android.widget.ArrayAdapter<BriefMenuAdapter.MenuDesc> {
	    Activity activity;
	    
	    
	    BriefMenuAdapter.MenuDesc[] items;
	    class MenuItem {

	        public TextView label;
	        public ImageView icon;
	    }
	    static class MenuDesc {
            public int id;
	        public int icon;
	        public String label;
	    }
	    public BriefMenuAdapter(Activity activity, BriefMenuAdapter.MenuDesc[] items) {
	        super(activity, R.layout.brief_menu_list, items);
	        this.activity = activity;
	        this.items = items;
	    }
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	        View rowView = convertView;
	        if (rowView == null) {
	            LayoutInflater inflater = activity.getLayoutInflater();
	            rowView = inflater.inflate(R.layout.brief_menu_list, null); 
	            MenuItem viewHolder = new MenuItem();
	            viewHolder.label = (TextView) rowView.findViewById(R.id.menu_label);
	            viewHolder.icon = (ImageView) rowView.findViewById(R.id.menu_icon);
	            rowView.setTag(viewHolder);
                B.addStyle(viewHolder.label);
	        }

	        MenuItem holder = (MenuItem) rowView.getTag();

	        String s = items[position].label;
	        holder.label.setText(s);
	        holder.icon.setImageResource(items[position].icon);
	        holder.icon.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));

	        return rowView;
	    }
	}




	public static void goMenu(int position) {
		BriefMenu.hideMenu();

        BriefMenuAdapter.MenuDesc item = MENU.menulistitems.get(position);

    	switch(item.id) {
			case 0:
				State.setFileExploreState(State.FILE_EXPLORE_STATE_STANDALONE);
				Bgo.openFragment(MENU.activity, FileExploreFragment.class);
				break;
			case 1:
				State.setContactsMode(State.CONTACT_MODE_VIEW);

				Bgo.openFragment(MENU.activity, ContactsHomeFragment.class);
				break;
			case 2:
				Bgo.openFragment(MENU.activity, CameraFragment.class);
				//BriefActivityManager.openCamera(MENU.activity);
				break;
			case 3:
				Bgo.openFragment(MENU.activity, PhoneHomeFragment.class);

				break;
			case 4:
				//BriefMenu.hideMenu();
				Bgo.openFragment(MENU.activity, SmsHomeFragment.class);
				break;
			case 5:
				Bgo.openFragment(MENU.activity, LockerFragment.class);
				//hideMenu();
				break;
			case 6:
				Bgo.openFragment(MENU.activity, SettingsHomeTabbedFragment.class);
				break;
            case 7:
                Bgo.openFragment(MENU.activity, AccountsHomeFragment.class);
                //hideMenu();
                break;
			case 8:
				Bgo.openFragment(MENU.activity, EmailHomeFragment.class);
				//hideMenu();
				break;
			case 9:
				Bgo.openFragment(MENU.activity, NewsHomeFragment.class);
				//hideMenu();
				break;
			case 10:
				Bgo.openFragment(MENU.activity, NotesHomeFragment.class);
				//hideMenu();
				break;

    	}

    	//hideMenu();
	}
*/
}
