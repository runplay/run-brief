package run.brief.email;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.lang.reflect.Method;

import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.BriefSettings;
import run.brief.beans.Email;
import run.brief.beans.Person;
import run.brief.contacts.ContactViewFragment;
import run.brief.contacts.ContactsDb;
import run.brief.util.BriefActivityManager;
import run.brief.util.log.BLog;

public class MessageWebView extends WebView {


    private Activity mActivity;
    private int color;
    private int size;

    public static final int COLOR_WHITE_ON_BLACK=0;
    public static final int COLOR_BLACK_ON_WHITE=1;

    public static final int SIZE_NORMAL=0;
    public static final int SIZE_LARGE=1;
    public static final int SIZE_XLARGE=2;
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


    public MessageWebView(Context context) {
        super(context);
        mActivity=(Activity) context;
        configure();
    }

    public MessageWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mActivity=(Activity) context;
        configure();
    }

    public MessageWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mActivity=(Activity) context;
        configure();
    }


    /**
     * Configure a web view to load or not load network data. A <b>true</b> setting here means that
     * network data will be blocked.
     * @param shouldBlockNetworkData True if network data should be blocked, false to allow network data.
     */
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


    /**
     * Configure a {@link android.webkit.WebView} to display a Message. This method takes into account a user's
     * preferences when configuring the view. This message is used to view a message and to display a message being
     * replied to.
     */
    public void configure() {
        this.setVerticalScrollBarEnabled(true);
        this.setVerticalScrollbarOverlay(true);
        this.setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        this.setLongClickable(true);


        final WebSettings webSettings = this.getSettings();

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        webSettings.setDefaultZoom(WebSettings.ZoomDensity.FAR);
		this.setInitialScale(95);
        this.setScaleX(1.0f);
        this.setScaleY(1.0f);
        disableDisplayZoomControls();


        webSettings.setJavaScriptEnabled(false);
        webSettings.setLoadsImagesAutomatically(true);
/*
        BriefSettings bset = State.getSettings();
        size = bset.getInt(BriefSettings.INT_WEBVIEW_SIZE);
        if(size==SIZE_LARGE) {
            this.setScaleX(1.2f);
            this.setScaleY(1.0f);
        } else if(size==SIZE_XLARGE) {
            this.setScaleX(1.4f);
            this.setScaleY(1.0f);
        }
        */
        BriefSettings bset = State.getSettings();
        color= bset.getInt(BriefSettings.INT_WEBVIEW_COLOR);
        //webSettings.setRenderPriority(WebSettings.RenderPriority.HIGH);
/*
        if (isSingleColumnLayoutSupported() && B.mobileOptimizedLayout()) {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        } else {
            webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NARROW_COLUMNS);
        }
*/
        disableOverscrolling();

        blockNetworkData(true);


        this.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }
            public void onPageFinished(WebView view, String url) {
                view.setInitialScale((int)(100*view.getScaleX()));
            }
            // Catch every http get, unable to catch http posts (daft)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //boolean hasReturn=false;
                if (url.startsWith("tel")) {

                    String number = url.replace("tel:", "");
                    if(number!=null && number.matches("[\\d]+") && number.length()>6) {
                        Person person = ContactsDb.getWithTelephone(mActivity, number);
                        if (person == null)
                            person = Person.getNewUnknownPerson(mActivity, number, null);
                        //StateObject sobz = new StateObject(StateObject.STRING_ID, person.getString(Person.LONG_ID) + "");
                        StateObject sobz = new StateObject(StateObject.STRING_BJSON_OBJECT, person.toString());
                        State.addToState(State.SECTION_CONTACTS_ITEM, sobz);
                        Bgo.openFragmentAnimate(mActivity, ContactViewFragment.class);
                        //hasReturn = true;
                    }
                    /*
                    Person person = ContactsDb.getWithTelephone(mActivity, number);
                    if (person == null)
                        person = Person.getNewUnknownPerson(mActivity, number, null);
                    StateObject sobz = new StateObject(StateObject.STRING_ID, person.getString(Person.LONG_ID) + "");
                    State.addToState(State.SECTION_CONTACTS_ITEM, sobz);
                    Bgo.openFragmentAnimate(mActivity, new ContactViewFragment());
                    */
                    return true;

                } else if (url.startsWith("mailto")) {
                    String address = url.replace("mailto:", "");
                    Email email = new Email();
                    email.setString(Email.STRING_TO, address);
                    State.addToState(State.SECTION_EMAIL_NEW, new StateObject(StateObject.STRING_BJSON_OBJECT, email.toString()));
                    Bgo.openFragmentAnimate(mActivity, EmailSendFragment.class);
                    return true;
                } else if(url.startsWith("http")) {
                    BriefActivityManager.openAndroidBrowserUrl(mActivity, url);
                    return true;
                }
                return false;
            }



            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {

            }

        });


    }

    private void disableDisplayZoomControls() {
    	/*
        if (Build.VERSION.SDK_INT >= 11) {
            PackageManager pm = getContext().getPackageManager();
            boolean supportsMultiTouch =
                    pm.hasSystemFeature(PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH) ||
                    pm.hasSystemFeature(PackageManager.FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT);

            getSettings().setDisplayZoomControls(!supportsMultiTouch);
        }
        */
        getSettings().setDisplayZoomControls(true);
    }

    @TargetApi(9)
    private void disableOverscrolling() {
        if (Build.VERSION.SDK_INT >= 9) {
            setOverScrollMode(OVER_SCROLL_NEVER);
        }
    }

    public void setText(String text, String contentType) {
        String content = text;
        //if (B.getK9MessageViewTheme() == B.Theme.DARK)  {
            // It's a little wrong to just throw in the <style> before the opening <html>
            // but it's less wrong than trying to edit the html stream
/*
        content= Sf.cleanHtml(content);
        content = Sf.htmlWrap(content);

        String fontsize = "font-size: 12px;";
        //if(size==SIZE_LARGE)
        //    fontsize = "font-size: 15px;";
        //if(size==SIZE_XLARGE)
        //    fontsize = "font-size: 18px;";
        if(color!=COLOR_BLACK_ON_WHITE) {
            replace = "<style>html,body{width:100% ! important} * { background: black ! important; "+fontsize+" color: white !important }" +
                    ":link, :link * { color: #CCFF33 !important }" +
                    ":visited, :visited * { color: #CCFF33 !important } table{width:100%}</style> "
                    ;
        } else {
            replace = "<style>html,body{width:100% ! important} * { background: white ! important; "+fontsize+" color: #111111 !important }" +
                    ":link, :link * { color: #006600 !important }" +
                    ":visited, :visited * { color: #006600 !important } table{width:100%}</style> "
                    ;
        }
        */
        String fontsize = "";
        String replace=null;
        if(color!=COLOR_BLACK_ON_WHITE) {
            replace = "<style>* { background: black ! important; "+fontsize+" color: white !important }" +
                    ":link, :link * { color: #CCFF33 !important }" +
                    ":visited, :visited * { color: #CCFF33 !important } table{width:100%}</style> "
                    ;
        } else {
            replace = "<style>* { background: white ! important; "+fontsize+" color: #111111 !important }" +
                    ":link, :link * { color: #006600 !important }" +
                    ":visited, :visited * { color: #006600 !important } table{width:100%}</style> "
                    ;
        }
        content=content.replace("<body",replace+"<body");

        //BLog.e("WEB",content);
        configure();
        loadDataWithBaseURL("http://", content, contentType, "utf-8", null);
    }

    /*
     * Emulate the shift key being pressed to trigger the text selection mode
     * of a WebView.
    
    @Override
    public void emulateShiftHeld() {
        try {

            KeyEvent shiftPressEvent = new KeyEvent(0, 0, KeyEvent.ACTION_DOWN,
                                                    KeyEvent.KEYCODE_SHIFT_LEFT, 0, 0);
            shiftPressEvent.dispatch(this, null, null);

        } catch (Exception e) {
            BLog.add("Exception in emulateShiftHeld()", e);
        }
    }
	 */
}
