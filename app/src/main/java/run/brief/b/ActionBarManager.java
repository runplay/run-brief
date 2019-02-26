package run.brief.b;


import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import run.brief.NavigationDrawerFragment;
import run.brief.R;

//import android.view.ActionMode;


public class ActionBarManager {
	private static final ActionBarManager ACT=new ActionBarManager();
	private static List<bMenuItem> MENU_ITEMS=new ArrayList<bMenuItem>();
	private static Menu menuTabs;

    private ColorDrawable backgroundAlpha50;
    private ColorDrawable backgroundAlpha100;

    private View actionbarTitle;
    private TextView actionbarTitleText;
    Activity activity;
    private boolean stopOtions=false;

	private RelativeLayout storedTitle;

	private static RelativeLayout getStoredTitle(Activity activity,String title) {
		//if(ACT.storedTitle==null) {
			ACT.storedTitle=new RelativeLayout(activity);
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
			ACT.storedTitle.setLayoutParams(lp);
			ACT.storedTitle.setGravity(Gravity.CENTER_HORIZONTAL);
			TextView txt = new TextView(activity);
			txt.setLayoutParams(lp);
			txt.setId(R.id.actionbar_title);
			txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);

			ACT.storedTitle.addView(txt);
		//}
		//TextView txt = (TextView) ACT.storedTitle.findViewById(R.id.actionbar_title);
		B.addStyleBold(txt);
		txt.setText(title);
		return ACT.storedTitle;
	}

    public static boolean stopOptions() {
        if(ACT.stopOtions) {
            ACT.stopOtions=false;
            return true;
        }
        return false;
    }
    public static void setStopOptions(boolean stop) {
        ACT.stopOtions=stop;
    }
	//private static ActionMode mode;
	public static void hide(Activity activity) {
		((AppCompatActivity)activity).getSupportActionBar().hide();

	}
	public static void show(Activity activity) {

		((AppCompatActivity)activity).getSupportActionBar().show();
	}
    public static void restart(Activity activity) {
        ACT.activity=activity;
        ACT.actionbarTitle=null;
        ACT.actionbarTitleText=null;
    }
    public static ColorDrawable getAlphaBackground100(Activity activity) {
        if(ACT.backgroundAlpha100 ==null) {
            ACT.backgroundAlpha100 = new ColorDrawable(activity.getResources().getColor(R.color.white));
            ACT.backgroundAlpha100.setAlpha(0);
        }
        return ACT.backgroundAlpha100;
    }
    public static ColorDrawable getAlphaBackground50(Activity activity) {
        if(ACT.backgroundAlpha50 ==null) {
            ACT.backgroundAlpha50 = new ColorDrawable(activity.getResources().getColor(R.color.white));
            ACT.backgroundAlpha50.setAlpha(80);
        }
        return ACT.backgroundAlpha50;
    }
    private static ColorDrawable getBackground(Activity activity,int Rcolor) {
        ColorDrawable cd= new ColorDrawable(activity.getResources().getColor(Rcolor));
        return cd;

    }
/*
    public static void hideActionBarUnderlayer(Activity activity) {
        View briefActionBar = activity.findViewById(R.id.brief_actionbar);
        briefActionBar.setVisibility(View.GONE);

    }
    public static void showActionBarUnderlayer(Activity activity) {
        View briefActionBar = activity.findViewById(R.id.brief_actionbar);
        briefActionBar.setVisibility(View.VISIBLE);

    }
*/
	public static Menu getMenu() {
		return menuTabs;
	}
	
	public class bMenuItem {
		public int rMenu;
		public String title;
		public int STATE_SECTION_;
		public Drawable img;
		public String subtext;
		public bMenuItem(int rMenu, int STATE_SECTION_, String title, Drawable img, String subtext) {
			this.rMenu=rMenu; this.title=title; this.STATE_SECTION_=STATE_SECTION_; this.img=img; this.subtext=subtext;
		}

	}
	public bMenuItem makeMenu(int rMenu, int STATE_SECTION_, String title, Drawable img, String subtext) {
		return new bMenuItem(rMenu,STATE_SECTION_,title, img, subtext);
	}

	
	private int CURRENT=-1;
	
	public static int getCurrent() {
		return ACT.CURRENT;
	}


	public static final bMenuItem getMenuItem(int index) {
		if(!MENU_ITEMS.isEmpty())
			return MENU_ITEMS.get(index);
		return null;
	}
	public static boolean isMenuSet() {
		if(ACT.CURRENT==-1)
			return false;
		else
			return true;
	}
	public static void setActionBarBackOnly(Activity activity, String title, int R_MENU_) {
		setActionBarBackOnly(activity, title, R_MENU_, R.color.actionbar_basic);
	}

    public static void setActionBarBackOnlyTransparent(Activity activity, String title, int R_MENU_) {
        setActionBarBackOnly(activity, title, R_MENU_, getAlphaBackground100(activity));
        //showActionBarUnderlayer(activity);
    }
    public static void setActionBarBackOnlyTransparentNoUnderlay(Activity activity, String title, int R_MENU_) {
        setActionBarBackOnly(activity, title, R_MENU_, getAlphaBackground100(activity));
        //hideActionBarUnderlayer(activity);
    }
    public static void setActionBarBackOnlyTransparent50NoUnderlay(Activity activity, String title, int R_MENU_) {
        setActionBarBackOnly(activity, title, R_MENU_, getAlphaBackground50(activity));
        //hideActionBarUnderlayer(activity);
    }

    public static void setActionBarBackOnly(Activity activity, String title, int R_MENU_, int Rcolor) {
        setActionBarBackOnly(activity, title, R_MENU_,getBackground(activity,Rcolor));
        //showActionBarUnderlayer(activity);
    }
	/*
    private static void ensureActionbarUnderlay(Activity activity) {
        View briefActionBar = activity.findViewById(R.id.brief_actionbar);

        BriefManager.setActionBarHeight(activity,activity.getActionBar().getHeight());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, BriefManager.getActionBarHeight());
        briefActionBar.setLayoutParams(lp);
    }
	*/
	private static void setActionBarBackOnlyWithLogo(Activity activity, String title,int R_MENU_, ColorDrawable color) {
		ACT.CURRENT= R_MENU_;
		final AppCompatActivity apact = (AppCompatActivity) activity;
		ActionBar ab = apact.getSupportActionBar();
		ab.setBackgroundDrawable(color);
		//ab.setTitle(title);

		ab.setCustomView(getStoredTitle(activity,title));
		ab.setDisplayShowCustomEnabled(true);
		//setTitleFont(activity,ab);
		ab.setDisplayShowHomeEnabled(false);
		ab.setDisplayHomeAsUpEnabled(true);


		//ab.setHomeButtonEnabled(true);


		NavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);

		apact.supportInvalidateOptionsMenu();
		ab.invalidateOptionsMenu();
		ab.show();

	}
	private static void setTitleFont(Activity activity, ActionBar ab) {
		int titleId = activity.getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView) activity.findViewById(titleId);
		if(abTitle!=null)
			B.addStyle(abTitle);
		//abTitle.setTextColor(colorId);
	}
	private static void setActionBarTextFont(ActionBar actionBar) {
		TextView titleTextView = null;

		try {
			Field f = actionBar.getClass().getDeclaredField("mTitleTextView");
			f.setAccessible(true);
			titleTextView = (TextView) f.get(actionBar);
			titleTextView.setTypeface(B.getTypeFace());
		} catch (NoSuchFieldException e) {
		} catch (IllegalAccessException e) {
		}
		//return titleTextView;
	}
	private static void setActionBarBackOnly(Activity activity, String title, int R_MENU_, ColorDrawable color) {
		setActionBarBackOnlyWithLogo(activity, title, R_MENU_,getAlphaBackground50(activity));
	}

	public static void setActionBarBackOnlyWithLogo(Activity activity, String title, int R_MENU_,int Rcolor) {
		ACT.CURRENT= R_MENU_;

		final AppCompatActivity apact = (AppCompatActivity) activity;
		ActionBar ab = apact.getSupportActionBar();

		//ab.setTitle(title);

		ab.setCustomView(getStoredTitle(activity,title));
		ab.setDisplayShowCustomEnabled(true);
		//setTitleFont(activity,ab);

		ab.setBackgroundDrawable(getBackground(activity,Rcolor));
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			activity.getWindow().setStatusBarColor(activity.getResources().getColor(Rcolor));
		}
		ab.setDisplayHomeAsUpEnabled(true);
		ab.setHomeButtonEnabled(true);
		NavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(false);
		apact.supportInvalidateOptionsMenu();

		ab.show();

	}

	public static void setActionBarMenu(Activity activity,String title, int R_MENU_) {
		setActionBarMenu(activity,title,R_MENU_,R.color.actionbar_general);
	}
    public static void setActionBarMenu(Activity activity, String title, int R_MENU_, int Rcolor) {
        setActionBarMenuWithIcon(activity,title,R_MENU_,Rcolor);
    }
	public static void setActionBarMenuWithIcon(Activity activity, String title, int R_MENU_, int Rcolor) {
		if(activity!=null) {

			ACT.CURRENT=R_MENU_;
			final AppCompatActivity apact = (AppCompatActivity) activity;

			ActionBar actionBar = apact.getSupportActionBar();

			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setBackgroundDrawable(getBackground(activity,Rcolor));
			if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
				activity.getWindow().setStatusBarColor(activity.getResources().getColor(Rcolor));
			}
			actionBar.setDisplayShowCustomEnabled(false);

			actionBar.setDisplayShowTitleEnabled(true);

			//ab.setTitle(title);

			actionBar.setCustomView(getStoredTitle(activity,title));
			actionBar.setDisplayShowCustomEnabled(true);
			//setTitleFont(activity,ab);

			actionBar.setLogo(null);
			NavigationDrawerFragment.mDrawerToggle.setDrawerIndicatorEnabled(true);
			apact.supportInvalidateOptionsMenu();
			actionBar.invalidateOptionsMenu();
			actionBar.show();

		}
	}


	public static void showMenu(Activity activity, Menu menu) {
		menuTabs=menu;
		if(ACT.CURRENT!=-1 && menu!=null)
			activity.getMenuInflater().inflate(ACT.CURRENT, menu);
	}



}
