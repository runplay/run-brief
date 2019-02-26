
package run.brief.b;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.reflect.Method;

import run.brief.R;
import run.brief.beans.BriefSettings;
import run.brief.beans.Theme;
import run.brief.util.JSONUrlReader;
import run.brief.util.UrlStore;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;


public final class B {

    public static final B THEME = new B();

    private Activity activity;

    public static final boolean LIVE_MODE = true;
    public static final boolean DEBUG = true;

    public static final String NAME="Brief";
    
    private static final FontSizes fontSizes = new FontSizes();

    public static double FONT_MEDIUM=1.5D;
    public static double FONT_LARGE=1.4D;
    public static double FONT_XLARGE=1.3D;
    public static double FONT_SMALL=2.3D;

    private Typeface typeface;
    private Typeface typefaceBold;
    private Bitmap resizedBitmap;
    private float defaultTextSize;



    public static void resetDefaultTextSize() {
        THEME.defaultTextSize=0;
    }


    public static final int APP_STAGE_FIRST_TIME=0;
    public static final int APP_STAGE_UNREGISTERED=1;
    public static final int APP_STAGE_REGISTERED=2;

    public static int getAppStage() {

        //File pemfile = new File(Files.HOME_PATH_APP+File.separator+)

        return APP_STAGE_FIRST_TIME;
    }
    public static AlphaAnimation animateAlphaFlash() {
        AlphaAnimation animation1 = new AlphaAnimation(0.2f, 1.0f);
        animation1.setDuration(200);
        animation1.setStartOffset(50);
        animation1.setFillAfter(true);
        return animation1;
    }
    public static float getSetDefaultTextSize(TextView refrenceTextView) {
        if(THEME.defaultTextSize==0) {
            BriefSettings set = State.getSettings();
            if(!set.has(BriefSettings.STRING_FLOAT_DEF_FONT_SIZE)) {
                set.setString(BriefSettings.STRING_FLOAT_DEF_FONT_SIZE,Float.toString(refrenceTextView.getTextSize()));
                set.save();

            }
            THEME.defaultTextSize= Float.valueOf(set.getString(BriefSettings.STRING_FLOAT_DEF_FONT_SIZE));

        }
        return THEME.defaultTextSize;
    }

    public static Bitmap getThemeBackground() {
        return THEME.resizedBitmap;
    }

    public static Bitmap getThemeBackgroundThumbnail(Activity activity, Theme theme) {
        return getThemeBackgroundThumbnail(activity,theme.name);
    }
    public static Bitmap getThemeBackgroundThumbnail(Activity activity, String themename) {

        int res=0;
        if(themename.equals(BriefSettings.THEME_BLUE_CLOG)) {
            res= R.drawable.bg_thumb_blue_clog;

        } else if(themename.equals(BriefSettings.THEME_GREEN_CLOUD)) {
            res=R.drawable.bg_thumb_cloud_green;

        } else if(themename.equals(BriefSettings.THEME_WORK_DAY)) {
            res=R.drawable.bg_thumb_gray_day;

        } else {
            res=R.drawable.bg_thumb_brief_bubbles;
        }
        return BitmapFactory.decodeResource(activity.getResources(), res);
        /*
        Bitmap bmp= BitmapFactory.decodeResource(activity.getResources(), res);

        int width = THEME.resizedBitmap.getWidth();
        int height = THEME.resizedBitmap.getHeight();
        float scaleWidth = ((float) 100) / width;
        float scaleHeight = ((float) 70) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(THEME.resizedBitmap, 0, 0, width, height, matrix, false);
        */
        //return resizedBitmap;
    }
    public static void addBackgroundImage(Activity activity, boolean forceRefresh) {
        if(forceRefresh || THEME.resizedBitmap==null) {
            //WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
            Display display = activity.getWindowManager().getDefaultDisplay();//wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);

            Bitmap bmp = null;
            //BLog.e("ROT","screen rotation: "+getResources().getConfiguration().orientation);
            System.gc();
            switch (activity.getResources().getConfiguration().orientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    bmp= BitmapFactory.decodeResource(activity.getResources(), getThemeBgRdrawable(Configuration.ORIENTATION_LANDSCAPE));
                    break;
                default:
                    bmp=BitmapFactory.decodeResource(activity.getResources(), getThemeBgRdrawable(Configuration.ORIENTATION_PORTRAIT));
                    break;

            }

            int width = bmp.getWidth();
            int height = bmp.getHeight();
            float scaleWidth = ((float) size.x) / width;
            float scaleHeight = ((float) size.y) / height;

            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);

            THEME.resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, false);

        }
        if(THEME.resizedBitmap!=null) {
            ImageView iv = (ImageView) activity.findViewById(R.id.main_bg_image);
            iv.setScaleType(ImageView.ScaleType.FIT_XY);
            iv.setImageBitmap(THEME.resizedBitmap);
        }
    }
    private static int getThemeBgRdrawable(int configurationOrientation) {
        String theme=State.getSettings().getString(BriefSettings.STRING_THEME);
        if(theme!=null) {
            if(theme.equals(BriefSettings.THEME_BLUE_CLOG)) {
                if(configurationOrientation==Configuration.ORIENTATION_LANDSCAPE) {
                    return R.drawable.bg_land_blue_clog;
                } else {
                    return R.drawable.bg_port_blue_clog;
                }
            } else if(theme.equals(BriefSettings.THEME_GREEN_CLOUD)) {
                if(configurationOrientation==Configuration.ORIENTATION_LANDSCAPE) {
                    return R.drawable.bg_land_cloud_green;
                } else {
                    return R.drawable.bg_port_cloud_green;
                }
            } else if(theme.equals(BriefSettings.THEME_WORK_DAY)) {
                if(configurationOrientation==Configuration.ORIENTATION_LANDSCAPE) {
                    return R.drawable.bg_land_grey_day;
                } else {
                    return R.drawable.bg_port_grey_day;
                }
            } else {
                if(configurationOrientation==Configuration.ORIENTATION_LANDSCAPE) {
                    return R.drawable.bg_land_brief_bubbles;
                } else {
                    return R.drawable.bg_port_brief_bubbles;
                }
            }
        }
        return 0;
    }
    public static Typeface getTypeFace() {
        return THEME.typeface;
    }
    public static Typeface getTypeFaceBold() {
        return THEME.typefaceBold;
    }

    public static SpannableString getStyledWithTypeFaceName(Activity activity, String text, String typeFaceName, float zoom) {
        Typeface tmp = null;
        if(typeFaceName.equals(BriefSettings.FONT_FACE_DEFAULT)) {
            tmp = Typeface.DEFAULT;
        } else {
            tmp = Typeface.createFromAsset(activity.getAssets(), "fonts/" + typeFaceName + ".ttf");
        }
        SpannableString s = new SpannableString(text);
        s.setSpan(tmp, 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new RelativeSizeSpan(zoom), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return s;
        //textView.setTypeface(tmp);
    }
    public static void initTypeface(Context context, String typeFaceName) {
        //if(typeface ==null) {
        if(typeFaceName==null || typeFaceName.equals(BriefSettings.FONT_FACE_DEFAULT)) {
            THEME.typeface = Typeface.DEFAULT;
            THEME.typefaceBold= Typeface.DEFAULT_BOLD;
            return;
        }
        try {
            THEME.typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + typeFaceName + ".ttf");
            THEME.typefaceBold = Typeface.createFromAsset(context.getAssets(), "fonts/" + typeFaceName + "_bold.ttf");
        } catch(Exception e) {}
        if(THEME.typeface==null) {
            THEME.typeface = Typeface.DEFAULT;
            THEME.typefaceBold= Typeface.DEFAULT_BOLD;
        }
        //}
    }
    public static void addStyleBold(EditText editText) {
        if(editText!=null) {
            editText.setTypeface(THEME.typefaceBold);
            //textView.setTextAppearance();
        }
    }
    public static boolean forceTryConnection(Context context) {
        //boolean tryConnect=false;
        JSONObject test=null;
        Device.CheckInternet(context);
        //if(Device.getCONNECTION_TYPE()==Device.CONNECTION_TYPE_NONE) {
            test = JSONUrlReader.readJsonFromUrlPlainText(UrlStore.URL_CHECK_INTERNET);

            if (test != null) {
                BLog.e("JSONtest", Device.getCONNECTION_TYPE() + "," + Device.getCONNECTION_STATE() + " - " + test.toString());
                return true;
            } else {
                //BLog.e("JSONtest", Device.getCONNECTION_TYPE() + " - FAILED" );
                return false;
            }
        //}
        //return true;
    }
    public static int getTheme() {
        int themeID = R.style.FontSizeMedium;
        if(State.getSettings()!=null) {
            String fontSizePref = State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_SIZE); //settings.getString("FONT_SIZE", "Medium");

            if (fontSizePref != null) {
                if (fontSizePref == BriefSettings.FONT_SIZE_SMALL) {
                    themeID = R.style.FontSizeSmall;
                } else if (fontSizePref == BriefSettings.FONT_SIZE_LARGE) {
                    themeID = R.style.FontSizeLarge;
                }
            }
        }
        return themeID;
    }
    public static void addStyleBold(TextView textView,double FONT_) {
        if(textView!=null) {
            textView.setTypeface(THEME.typefaceBold);
            //BLog.e("SCX","scale x: "+textView.getTextSize());
            textView.setTextSize(Double.valueOf(Float.valueOf(getSetDefaultTextSize(textView)).intValue()/FONT_).floatValue());
            //textView.setTextAppearance();
        }
    }
    public static void addStyleBold(TextView textView) {
        if(textView!=null) {
            textView.setTypeface(THEME.typefaceBold);
            //textView.setTextAppearance();
        }
    }
    public static void addStyleBold(TextView ...textViews) {
        if(textViews!=null) {
            for(TextView textView: textViews) {
                textView.setTypeface(THEME.typefaceBold);
            }
        }
    }


    public static void addStyle(EditText editText) {
        if(editText!=null) {
            editText.setTypeface(THEME.typeface);
            //textView.setTextAppearance();
        }
    }
    public static void addStyle(TextView textView) {
        if(textView!=null) {
            textView.setTypeface(THEME.typeface);
            //textView.setTextAppearance();
        }
    }
    public static void addStyle(TextView textView,double FONT_) {
        if(textView!=null) {
            textView.setTypeface(THEME.typefaceBold);
            //BLog.e("SCX","scale x: "+textView.getTextSize());
            textView.setTextSize(Double.valueOf(Float.valueOf(getSetDefaultTextSize(textView)).intValue()/FONT_).floatValue());
            //textView.setTextAppearance();
        }
    }
    public static void addStyle(TextView ...textViews) {
        if(textViews!=null) {
            for(TextView textView: textViews) {
                textView.setTypeface(THEME.typeface);
            }
        }
    }

    public static void addStyle(EditText[] textViews) {
        if(textViews!=null) {
            for(TextView textView: textViews) {
                textView.setTypeface(THEME.typeface);
            }
        }
    }

    public static Method getMethod(Class<?> classObject, String methodName) {
        try {
            return classObject.getMethod(methodName, boolean.class);
        } catch (NoSuchMethodException e) {
            //Log.i(B.LOG_TAG, "Can't get method " +
            //      classObject.toString() + "." + methodName);
        } catch (Exception e) {
            //BLog.add("B() Error while using reflection to get method " + classObject.toString() + "." + methodName, e);
        }
        return null;
    }

    public static FontSizes getFontSizes() {
        return fontSizes;
    }
    public static void fixDrawableLevels(TextView textview) {
    	// Fix level of existing drawables
    	Drawable[] drawables = textview.getCompoundDrawables();
    	for (Drawable d : drawables) if (d != null && d instanceof ScaleDrawable) d.setLevel(1);
    	textview.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
    }
    @SuppressWarnings("deprecation")
    public static Drawable getDrawable(Context context, int Rdrawable) {
        if(android.os.Build.VERSION.SDK_INT>= 21) {
            return context.getDrawable(Rdrawable);
        } else {
            return context.getResources().getDrawable(Rdrawable);
        }
    }




    /*
    GO TOP

     */
    private static RelativeLayout gotopView;
    private static GridView gotopGrid;
    private static ListView gotopList;
    private static Handler gotopHandler=new Handler();
    private static boolean isGotopOpen=false;
    public static void removeGoTopTracker() {
        gotopHandler.removeCallbacks(gotopRunner);
        if(isGotopOpen) {
            isGotopOpen=false;
            //Animation animation = AnimationUtils.loadAnimation(BM.activity, R.anim.slide_out_to_top);
            gotopView.setVisibility(View.GONE);
            //gotopView.startAnimation(animation);
        }
    }
    public static void addGoTopTracker(Activity activity, GridView list) {
        addGoTopTracker(activity,list,R.drawable.gt_brief);
    }
    public static void addGoTopTracker(Activity activity, ListView list) {
        addGoTopTracker(activity,list,R.drawable.gt_brief);
    }
    public static void addGoTopTracker(Activity activity, ListView list, int Rdrawable) {
        THEME.activity=activity;

        gotopGrid=null;
        gotopList=list;
        gotopView=(RelativeLayout) activity.findViewById(R.id.main_gotop);
        if(gotopView!=null) {
            gotopView.setGravity(Gravity.CENTER);
            gotopView.setBackgroundColor(activity.getResources().getColor(R.color.white_alpha));
            View topbtn = activity.findViewById(R.id.main_gotop_btn);
            topbtn.setBackground(getDrawable(THEME.activity,Rdrawable));
            topbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotopList.setSelection(0);
                    isGotopOpen=false;
                    Animation animation = AnimationUtils.loadAnimation(THEME.activity, R.anim.slide_out_to_top);
                    gotopView.setAnimation(animation);
                    gotopView.startAnimation(animation);
                }
            });
            //gotopView.addView(topbtn);
            gotopView.setVisibility(View.GONE);
            gotopHandler.postDelayed(gotopRunner,50);
            //list.addHeaderView(activity.getLayoutInflater().inflate(R.layout.wait, null));
        }
    }
    public static void addGoTopTracker(Activity activity, GridView list, int Rdrawable) {
        THEME.activity=activity;
        //int listYPos=list.getY
        gotopList=null;
        gotopGrid=list;
        gotopView=(RelativeLayout) activity.findViewById(R.id.main_gotop);
        if(gotopView!=null) {
            gotopView.setGravity(Gravity.CENTER);
            gotopView.setBackgroundColor(activity.getResources().getColor(R.color.white_alpha));
            View topbtn = activity.findViewById(R.id.main_gotop_btn);
            topbtn.setBackground(getDrawable(THEME.activity,Rdrawable));
            topbtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotopGrid.setSelection(0);
                    isGotopOpen=false;
                    Animation animation = AnimationUtils.loadAnimation(THEME.activity, R.anim.slide_out_to_top);
                    gotopView.setAnimation(animation);
                    gotopView.startAnimation(animation);
                }
            });
            //gotopView.addView(topbtn);
            gotopView.setVisibility(View.GONE);
            gotopHandler.postDelayed(gotopRunner, 50);
            //list.addHeaderView(activity.getLayoutInflater().inflate(R.layout.wait, null));
        }
    }
    private static Runnable gotopRunner = new Runnable() {
        @Override
        public void run() {

            int pos = 0;
            if(gotopList!=null) {
                pos=gotopList.getFirstVisiblePosition();
            } else if(gotopGrid!=null) {
                pos=gotopGrid.getFirstVisiblePosition();
            }
            if(pos>3) {
                //BLog.e("GT-TRACK","should be open");
                if(!isGotopOpen) {
                    isGotopOpen=true;
                    gotopView.setVisibility(View.VISIBLE);
                    gotopView.bringToFront();
                    Animation animation = AnimationUtils.loadAnimation(THEME.activity, R.anim.slide_in_from_top);
                    gotopView.setAnimation(animation);
                    gotopView.startAnimation(animation);
                    //BLog.e("GT-TRACK", "------ opened");
                }
            } else {
                //BLog.e("GT-TRACK","should be closed: "+pos);
                if(isGotopOpen) {
                    isGotopOpen=false;
                    Animation animation = AnimationUtils.loadAnimation(THEME.activity, R.anim.slide_out_to_top);
                    gotopView.setAnimation(animation);
                    gotopView.startAnimation(animation);
                } else {
                    gotopView.setVisibility(View.GONE);
                }
            }
            gotopHandler.postDelayed(gotopRunner,300);
        }
    };
}
