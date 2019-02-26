package run.brief.news;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.lang.reflect.Method;

import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BCallbackInt;
import run.brief.b.State;
import run.brief.beans.BriefSettings;
import run.brief.util.log.BLog;

public class NewsWebView extends WebView {

    private NewsWebView thisview;

    private ImageView leavefullscreen;
    private Activity mActivity;
    private int color;
    private int size;
    private String assignedUrl;

    public static final int COLOR_WHITE_ON_BLACK=0;
    public static final int COLOR_BLACK_ON_WHITE=1;

    public static final int SIZE_NORMAL=0;
    public static final int SIZE_LARGE=1;
    public static final int SIZE_XLARGE=2;

    private boolean isLoadedAlready=false;
    private boolean isClosed=false;
    private boolean isFullScreen;
    private View mCustomView;
    private RelativeLayout customViewContainer;

    public void setIsClosed(boolean isClosed) {
        this.isClosed=isClosed;
    }

    public void setCustomViewContainer(Activity activity, RelativeLayout view) {
        customViewContainer=view;
        RelativeLayout.LayoutParams flp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        flp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);//= Gravity.RIGHT|Gravity.BOTTOM;//.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        flp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        leavefullscreen = new ImageView(activity);
        leavefullscreen.setBackgroundColor(activity.getResources().getColor(R.color.black_alpha));
        leavefullscreen.setLayoutParams(flp);
        leavefullscreen.setImageDrawable(B.getDrawable(activity, R.drawable.navigation_cancel_white));
        leavefullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWebChromeClient!=null)
                    mWebChromeClient.onHideCustomView();
            }
        });
        view.addView(leavefullscreen);
    }

    public boolean isLoadedAlready() {
        return isLoadedAlready;
    }
    /**
     * We use WebSettings.getBlockNetworkLoads() to prevent the WebView that displays email
     * bodies from loading external resources over the network. Unfortunately this method
     * isn't exposed via the official Android API. That's why we use reflection to be able
     * to call the method.
     */
    /*
    public void setSize(int size) {
        if(size>SIZE_XLARGE)
            this.size=SIZE_NORMAL;
        else
            this.size=size;
    }
    public void incSize() {
        setSize(size++);
    }
    public int getSize() {
        return size;
    }
    */
    public static final Method mGetBlockNetworkLoads = B.getMethod(WebSettings.class, "setBlockNetworkLoads");

    private BCallbackInt backto;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private WebChromeClient mWebChromeClient;
    /**
     * Check whether the single column layout algorithm can be used on this version of Android.
     *
     * <p>
     * Single column layout was broken on Android < 2.2 (see
     * <a href="http://code.google.com/p/android/issues/detail?id=5024">issue 5024</a>).
     * </p>
     *
     * <p>
     * Android versions >= 3.0 have problems with unclickable links when single column layout is
     * enabled (see
     * <a href="http://code.google.com/p/android/issues/detail?id=34886">issue 34886</a>
     * in Android's bug tracker, and
     * <a href="http://code.google.com/p/k9mail/issues/detail?id=3820">issue 3820</a>
     * in K-9 Mail's bug tracker).
     */
    public static boolean isSingleColumnLayoutSupported() {
        return (Build.VERSION.SDK_INT > 7 && Build.VERSION.SDK_INT < 11);
    }
    public boolean inCustomView() {
        return (mCustomView != null);
    }

    public void hideCustomView() {
        mWebChromeClient.onHideCustomView();
    }

    public NewsWebView(Context context) {
        super(context);
        mActivity=(Activity) context;
        configure();
    }

    public NewsWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity=(Activity) context;
        configure();
    }

    public NewsWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mActivity=(Activity) context;
        configure();
    }

    public void loadUrl(String url,BCallbackInt frag) {
        assignedUrl=url;
        this.loadUrl(url);
        this.backto=frag;
    }

    /**
     * Configure a web view to load or not load network data. A <b>true</b> setting here means that
     * network data will be blocked.


    public void blockNetworkData(final boolean shouldBlockNetworkData) {
        // Sanity check to make sure we don't blow up.
        if (getSettings() == null) {
            return;
        }

        // Block network loads.
        if (mGetBlockNetworkLoads != null) {
            try {
                mGetBlockNetworkLoads.invoke(getSettings(), shouldBlockNetworkData);
            } catch (Exception e) {
                BLog.add("blockNetWorkData() - Error on invoking WebSettings.setBlockNetworkLoads()", e);
            }
        }

        getSettings().setBlockNetworkImage(shouldBlockNetworkData);
    }

    private String getUAString(Activity activity) {
        return UrlStore.USER_AGENT_WEBVIEW;//"Mozilla/5.0 (Linux; U; Android "+Build.VERSION.RELEASE+"; "+Locale.getDefault().getLanguage()+"-"+Locale.getDefault().getCountry()+"; "+Build.MODEL+" Build/"+Build.ID+") runPlay google (rf "+getVersionName(activity)+"."+getVersionCode(activity)+")";
    }
     */
    private String getVersionName(Activity activity) {
        try {
            PackageInfo manager=activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return manager.versionName;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return "1";
        }
    }
    private int getVersionCode(Activity activity) {
        try {
            PackageInfo manager=activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
            return manager.versionCode;
        } catch (android.content.pm.PackageManager.NameNotFoundException e) {
            return 1;
        }
    }
    /**
     * Configure a {@link android.webkit.WebView} to display a Message. This method takes into account a user's
     * preferences when configuring the view. This message is used to view a message and to display a message being
     * replied to.
     */
    public void configure() {
        thisview=this;
        thisview.onResume();
        this.setVerticalScrollBarEnabled(true);
        this.setVerticalScrollbarOverlay(true);
        this.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        this.setLongClickable(true);


        final WebSettings webSettings = this.getSettings();

        //webSettings.setUserAgentString(getUAString(mActivity));
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        //webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
        this.setScaleX(1.0f);
        this.setScaleY(1.0f);
		this.setInitialScale(95);

        disableDisplayZoomControls();

        //BriefSettings bset = ;


        webSettings.setJavaScriptEnabled(!State.getSettings().getBoolean(BriefSettings.BOOL_WEBVIEW_DISABLE_JAVASCRIPT));
        webSettings.setLoadsImagesAutomatically(true);
        webSettings.setAppCacheEnabled(true);
        disableOverscrolling();

        this.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            public void onPageFinished(WebView view, String url) {
                if (assignedUrl != null && assignedUrl.equals(url))
                    isLoadedAlready = true;
                if (url.contains("youtube.")) {
                    view.setInitialScale(200);
                } else {
                    view.setInitialScale(95);
                }
                if (backto != null && !isClosed)
                    backto.callback(100);
                backto = null;
                isClosed = false;


            }

            // Catch every http get, unable to catch http posts (daft)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if(url.contains("youtube.")) {
                    webSettings.setJavaScriptEnabled(true);
                } else {
                    webSettings.setJavaScriptEnabled(!State.getSettings().getBoolean(BriefSettings.BOOL_WEBVIEW_DISABLE_JAVASCRIPT));
                }
                if (isLoadedAlready)
                    isLoadedAlready = false;
                return false;
            }


            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                setIsClosed(true);
                loadData("<html><body style=\"background:#fff;padding-top:40px;text-align:center\">" + mActivity.getString(R.string.news_error_load) + "</body>", "text/html", "UTF-8");
            }

        });
        mWebChromeClient = new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final android.webkit.JsResult result) {
                result.cancel();
                //Log.e(".js-al", message);
                return true;
            }

            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final android.webkit.JsResult result) {
                result.cancel();
                //Log.e(".js-conf", message);
                return false;
            }

            @Override
            public boolean onJsPrompt(WebView view, String url, String message, String defaultValue, final android.webkit.JsPromptResult result) {
                result.cancel();
                //Log.e(".js-prompt", message);
                return false;
            }

            public void onProgressChanged(WebView view, int progress) {
                //BLog.e("progchange: " + isClosed);
                if (backto != null && !isClosed)
                    backto.callback(progress);
                //backto=null;

            }


            private Bitmap mDefaultVideoPoster;
            private View mVideoProgressView;


            @Override
            public void onShowCustomView(View view,CustomViewCallback callback) {

                BLog.e("show start");
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ActionBarManager.hide(mActivity);
                    }
                });

                // if a view already exists then immediately terminate the new one
                //if (mCustomView != null) {
                //    callback.onCustomViewHidden();
                //    return;
                //}
                mCustomView = view;
                thisview.setVisibility(View.GONE);
                customViewContainer.setVisibility(View.VISIBLE);
                customViewContainer.addView(view);

                if(leavefullscreen!=null) {
                    leavefullscreen.bringToFront();

                }
                customViewCallback = callback;
                isFullScreen=true;
                BLog.e("show stop");
            }

            @Override
            public View getVideoLoadingProgressView() {

                if (mVideoProgressView == null) {
                    LayoutInflater inflater = mActivity.getLayoutInflater();
                    mVideoProgressView = inflater.inflate(R.layout.video_progress, null);
                }
                return mVideoProgressView;
            }

            @Override
            public void onHideCustomView() {
                synchronized (this) {
                    BLog.e("hide start");

                    ActionBarManager.show(mActivity);
                    super.onHideCustomView();    //To change body of overridden methods use File | Settings | File Templates.
                    if (mCustomView == null)
                        return;

                    thisview.setVisibility(View.VISIBLE);
                    customViewContainer.setVisibility(View.GONE);

                    // Hide the custom view.
                    mCustomView.setVisibility(View.GONE);


                    // Remove the custom view from its container.
                    customViewContainer.removeView(mCustomView);
                    customViewCallback.onCustomViewHidden();

                    mCustomView = null;
                    isFullScreen = false;
                    BLog.e("hide stop");
                }
            }
        };
        this.setWebChromeClient(mWebChromeClient);
    }

    private void disableDisplayZoomControls() {
        getSettings().setSupportZoom(true);
        getSettings().setDisplayZoomControls(true);
    }

    @TargetApi(9)
    private void disableOverscrolling() {
        if (Build.VERSION.SDK_INT >= 9) {
            setOverScrollMode(OVER_SCROLL_NEVER);
        }
    }

}
