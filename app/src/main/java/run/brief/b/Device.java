package run.brief.b;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import run.brief.R;
import run.brief.beans.BriefSettings;
import run.brief.sms.SmsFunctions;
import run.brief.util.Files;
import run.brief.util.Functions;
import run.brief.util.ViewManagerText;
import run.brief.util.log.BLog;

public final class Device {
	private static final Device D = new Device();
	
	
	public static final int CONNECTION_TYPE_NONE=0;
	public static final int CONNECTION_TYPE_MOBILE=2;
	public static final int CONNECTION_TYPE_WIFI=1;
	
	public static final int CONNECTION_STATE_NONE=0;
	public static final int CONNECTION_STATE_CONNECTING=1;
	public static final int CONNECTION_STATE_CONNECTED=2;
	
	private boolean hasPhone=false;;
	private boolean hasContentProviderSmsSent=false;
	private boolean hasBluetooth=false;
	
	private static int rotation;
	
	private static int CONNECTION_TYPE; 
	private static int CONNECTION_STRENGTH;   
	private static int CONNECTION_STATE;
	
	private String phoneNumber;
	private boolean isInitialised=false;
	
	private int keyboardHeight;	
	private int previousHeightDiffrence = 0;
	private boolean isKeyBoardVisible;
	
	//private AudioManager mAudioManager;
	
	private static int P2P_OK=0;
	public static final int P2P_OK_NOT_TESTED=0;
	public static final int P2P_OK_NONE=2;
	public static final int P2P_OK_HAS=1;

    private TelephonyManager    telMgr;
    private String              curLocale;
    private PhoneNumberUtil phoneUtil;


    public static String imeiSIM1;
    public static String imeiSIM2;

    public static boolean isSIM1Ready;
    public static boolean isSIM2Ready;

    boolean isDualSIM;


    public static boolean hasActiveSim(Context context) {
        TelephonyManager telMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        int simState = telMgr.getSimState();
        switch (simState) {
            case TelephonyManager.SIM_STATE_READY:
                return true;
            /*
            case TelephonyManager.SIM_STATE_ABSENT:
                // do something
                break;
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                // do something
                break;
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                // do something
                break;

            case TelephonyManager.SIM_STATE_UNKNOWN:
                // do something
                break;
            */
        }
        return false;
    }

	public static boolean isTablet(Context context) {
	    return (context.getResources().getConfiguration().screenLayout
	            & Configuration.SCREENLAYOUT_SIZE_MASK)
	            >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}
	public static boolean isMediaMounted() {
		return android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
	}

	public static void rotationLock(Activity activity) {
		/*
		updateRotation(activity);
		if(rotation==ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
				|| rotation==ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
				|| rotation==ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
				) {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		*/
	    final int orientation = activity.getResources().getConfiguration().orientation;
	    final int rotation = activity.getWindowManager().getDefaultDisplay().getOrientation();

	    // Copied from Android docs, since we don't have these values in Froyo 2.2
	    int SCREEN_ORIENTATION_REVERSE_LANDSCAPE = 8;
	    int SCREEN_ORIENTATION_REVERSE_PORTRAIT = 9;

	    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.FROYO)
	    {
	        SCREEN_ORIENTATION_REVERSE_LANDSCAPE = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
	        SCREEN_ORIENTATION_REVERSE_PORTRAIT = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
	    }

	    if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_90)
	    {
	        if (orientation == Configuration.ORIENTATION_PORTRAIT)
	        {
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	        }
	        else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
	        {
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        }
	    }
	    else if (rotation == Surface.ROTATION_180 || rotation == Surface.ROTATION_270) 
	    {
	        if (orientation == Configuration.ORIENTATION_PORTRAIT) 
	        {
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
	        }
	        else if (orientation == Configuration.ORIENTATION_LANDSCAPE) 
	        {
	            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
	        }
	    }
		//activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
		
	}
	public static void rotationUnLock(Activity activity) {
		//if(rotation==ActivityInfo.SCREEN_ORIENTATION_NOSENSOR)
			//updateRotation(activity);
		
		//activity.setRequestedOrientation(rotation);
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}
	public static void updateRotation(Activity activity) {
		
		int tmprotation = activity.getRequestedOrientation();
		//BLog.e("ROT", tmprotation+"");
		if(tmprotation!=ActivityInfo.SCREEN_ORIENTATION_NOSENSOR) {
			rotation=tmprotation;
		} else {
			rotation=ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		}
		
		
		
        //Display display = ((WindowManager) activity.getSystemService(Activity.WINDOW_SERVICE)).getDefaultDisplay();
        //rotation = display.getRotation(); //.getOrientation();
        //BLog.e("ROT", rotation+"");

	}
	public static int getRotation() {
		return rotation;
	}
	

	public static boolean isKeyboardVisible() {
		return D.isKeyBoardVisible;
	}
	
	public static void checkKeyboardHeight(final View parentLayout, final PopupWindow popupWindow,final LinearLayout emoticonsCover) {

		parentLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						
						Rect r = new Rect();
						parentLayout.getWindowVisibleDisplayFrame(r);
						
						int screenHeight = parentLayout.getRootView()
								.getHeight();
						int heightDifference = screenHeight - (r.bottom);
						
						if (D.previousHeightDiffrence - heightDifference > 50) {							
							popupWindow.dismiss();
						}
						
						D.previousHeightDiffrence = heightDifference;
						if (heightDifference > 100) {

							D.isKeyBoardVisible = true;
							Device.changeKeyboardHeight(heightDifference,emoticonsCover);

						} else {

							D.isKeyBoardVisible = false;
							
						}

					}
				});

	}

	/**
	 * change height of emoticons keyboard according to height of actual
	 * keyboard
	 * 
	 * @param height
	 *            minimum height by which we can make sure actual keyboard is
	 *            open or not
	 */
	public static void changeKeyboardHeight(int height, LinearLayout emoticonsCover) {

		if (height > 100) {
			D.keyboardHeight = height;
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, D.keyboardHeight);
			emoticonsCover.setLayoutParams(params);
		}

	}
	
	public static boolean hasP2P() {
		/*
		int bversion=0;
		try {
			bversion=Integer.valueOf(android.os.Build.VERSION.SDK);
		} catch(Exception e) {}
		if(android.os.Build.VERSION.SDK_INT>bversion)
			bversion=android.os.Build.VERSION.SDK_INT;
		//BLog.e("DEVICE", "build version: "+bversion);
		if(P2P_OK==P2P_OK_NOT_TESTED) {
			if(android.os.Build.VERSION.SDK_INT>=16)
				P2P_OK=P2P_OK_HAS;
			else
				P2P_OK=P2P_OK_NONE;
		}
		if(P2P_OK==P2P_OK_HAS)
			return true;
			*/
		return false;
	}
	public static void setProxyOn() {
		BriefSettings bean = State.getSettings();
		if(bean!=null && bean.has(BriefSettings.BOOL_PROXY) && bean.getBoolean(BriefSettings.BOOL_PROXY)==Boolean.TRUE) {
			//System.setProperty("http.proxyHost", bean.getString(BriefSettings.STRING_PROXY_IP));
			System.setProperty("proxyHost", bean.getString(BriefSettings.STRING_PROXY_IP));
			System.setProperty("proxyPort", Integer.valueOf(bean.getInt(BriefSettings.INT_PROXY_PORT)).toString());
			if(bean.has(BriefSettings.BOOL_PROXY_AUTH) && bean.getBoolean(BriefSettings.BOOL_PROXY_AUTH)==Boolean.TRUE) {
				System.setProperty("proxyUser", bean.getString(BriefSettings.STRING_PROXY_USER));
				System.setProperty("proxyPassword", bean.getString(BriefSettings.STRING_PROXY_PASSWORD));
			}
		} 
	}
	public static void setProxyOff() {
		System.clearProperty("proxyHost");
		System.clearProperty("proxyPort");
		System.clearProperty("proxyUser");
		System.clearProperty("proxyPassword");

	}

	public static void copyToClipboardFlashView(Activity activity, View view, String text) {
		if(view!=null)
			Functions.copyToClipFlashView(activity, view);
		copyToClipboard(activity, text);

	}
	public static void copyToClipboard(Activity activity, String text) {
	    ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE); 
	    ClipData clip = ClipData.newPlainText(B.NAME,text);
	    clipboard.setPrimaryClip(clip);
	    
	}
	

	public static int getCONNECTION_TYPE() {
		return CONNECTION_TYPE;
	}
	public static int getCONNECTION_STRENGTH() {
		return CONNECTION_STRENGTH;
	}
	public static int getCONNECTION_STATE() {
		return CONNECTION_STATE;
	}	
	
	public static boolean isQuietModeOn(Context context) {
		BriefSettings settings = State.getSettings();
		return settings.getBoolean(BriefSettings.BOOL_QUIET_MODE);
	}
    /*
	public static void resetQuietMode(Context context) {
		BriefSettings settings = State.getSettings();
		if(settings.getBoolean(BriefSettings.BOOL_QUIET_MODE)) {
			if(settings.getBoolean(BriefSettings.BOOL_QUIET_MODE_FLIGHT)) {
				setFlightMode(context, true);
			} else {
				setRingMode(context, settings.getInt(BriefSettings.INT_QUIET_MODE_RINGER));
			}
		} else {
			// do nothing
		}
	}

	private void setFlashLight(Context context, boolean isOn) {
		if(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
			
		}
	}
	*/
	public static String getInternalIP () {
		String address=null;
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress.getHostAddress().toString().contains(".")) {
                    	address= inetAddress.getHostAddress().toString();
                    	//BLog.e("INET", address);
                    }
                }
            }
        } catch (SocketException ex) {
            //Log.i("externalip", ex.toString());
        }
        return address;
	}
    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isFlightModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }
    /*
	public static boolean isFlightModeOn(Context context) {
        return Settings.Global.getInt(context.getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
		//return Settings.System.getInt(context.getContentResolver(),
          //      Settings.System.AIRPLANE_MODE_ON, 0) == 1;
	}
	*/
	private static void setFlightMode(Context context, boolean isOn) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Settings.System.putInt(
                    context.getContentResolver(),
                    Settings.System.AIRPLANE_MODE_ON, isOn ? 0 : 1);
        } else {
            Settings.Global.putInt(
                    context.getContentResolver(),
                    Settings.Global.AIRPLANE_MODE_ON, isOn ? 0 : 1);
        }
		Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intent.putExtra("state", !isOn);
		context.sendBroadcast(intent);
	}

	
    public static void hideKeyboard(Activity activity) {
     	InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    	if(imm!=null) {
    		View v = activity.findViewById(R.id.container);
    		if(v!=null)
    			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    		else
    			BLog.e("keyboard", "fail hide 1");
    	} else
			BLog.e("keyboard", "fail hide 2");
    }
    public static void setKeyboard(Activity activity, View editTextView, boolean showKeyboard) {
        editTextView.requestFocus();
     	InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
    	if(imm!=null) {

    			if(showKeyboard) {
                    imm.showSoftInput(editTextView, 0);
                    //if(getKeyboardHeight(activity)==0) {
                        GetSetHeight getSetHeight = D.new GetSetHeight();
                        getSetHeight.setActivity(activity);

                        //getSetHeight.setParentView(parentView);
                        //getSetHeightHandler.removeCallbacks(getSetHeight);
                        getSetHeightHandler.postDelayed(getSetHeight, 1001);
                    //}

                } else {
                    imm.hideSoftInputFromWindow(editTextView.getWindowToken(), 0);
                }

    	} else
			BLog.e("keuboard", "fail hide 2");
    }

    public static InputMethodInfo getKeyboardInput(Activity activity) {
    	
    	String id = Settings.Secure.getString(
    			   activity.getContentResolver(), 
    			   Settings.Secure.DEFAULT_INPUT_METHOD
    			);
    	
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();

        final int N = mInputMethodProperties.size();

        for (int i = 0; i < N; i++) {

            InputMethodInfo imi = mInputMethodProperties.get(i);

            if (imi.getId().equals(Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD))) {

                //imi contains the information about the keyboard you are using
                return imi;
            }
        }
        return null;
    }
	public static void init(Context context) {
		if(!D.isInitialised) {
			
			getPhoneNumber(context);
			
			//BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
			//if (btAdapter != null) {
			//	D.hasBluetooth=true;
			//}
			TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
			if(telephonyManager!=null && telephonyManager.getPhoneType()!=TelephonyManager.PHONE_TYPE_NONE) {
				D.hasPhone=true;
                D.hasContentProviderSmsSent=D.hasContentProvider(context,SmsFunctions.CONTENT_PROVIDER_SMS_SENT);
			}
			/*
			try {
				TelephonyInfo telephonyInfo = TelephonyInfo.getInstance(context);

				D.imeiSIM1 = telephonyInfo.getImeiSIM1();
				D.imeiSIM2 = telephonyInfo.getImeiSIM2();

				D.isSIM1Ready = telephonyInfo.isSIM1Ready();
				D.isSIM2Ready = telephonyInfo.isSIM2Ready();

				D.isDualSIM = telephonyInfo.isDualSIM();
			} catch(Exception e) {}
			*/
			D.isInitialised=true;
		}
	}
    public static String phoneNumgerAsInternationl(Context ctx, String phone) {
        TelephonyManager    telMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
        String              curLocale = telMgr.getNetworkCountryIso().toUpperCase();
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        try
        {
            Phonenumber.PhoneNumber phoneNumberProto = phoneUtil.parse(phone, curLocale);
            String fixed = phoneUtil.format(phoneNumberProto, PhoneNumberFormat.INTERNATIONAL);
            return fixed;
        } catch (NumberParseException e)  { }
        return phone;

    }
    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
    public static String FixPhoneNumber(Context ctx, String rawNumber)
	{
	    String      fixedNumber = "";

	    // get current location iso code
        if(D.telMgr==null) {
            D.telMgr = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
            D.curLocale = D.telMgr.getNetworkCountryIso().toUpperCase();
            D.phoneUtil = PhoneNumberUtil.getInstance();
        }
	    Phonenumber.PhoneNumber     phoneNumberProto;

	    // gets the international dialling code for our current location
	    String              curDCode = String.format("%d", D.phoneUtil.getCountryCodeForRegion(D.curLocale));
	    String              ourDCode = "";
	    
	    if(rawNumber==null)
	    	rawNumber="";
	    
	    //BLog.e("NUMB", "a"+rawNumber+"a");
	    if(rawNumber.startsWith("-")) {   
	    	fixedNumber=ctx.getString(R.string.unknown);
	    } else {

		    if(rawNumber.indexOf("+") == 0)	    {
		    	
		    	
		    	
		        int     bIndex = rawNumber.indexOf("(");
		        int     hIndex = rawNumber.indexOf("-");
		        int     eIndex = rawNumber.indexOf(" ");
	
		        if(bIndex != -1)
		        {
		            ourDCode = rawNumber.substring(1, bIndex);
		        }
		        else if(hIndex != -1) 
		        {               
		            ourDCode = rawNumber.substring(1, hIndex);
		        }
		        else if(eIndex != -1)
		        {
		            ourDCode = rawNumber.substring(1, eIndex);
		        }
		        else
		        {
		        	ourDCode = rawNumber.substring(1, 3);
		            //ourDCode = curDCode;
		        }     
		    }  else  {
		        ourDCode = curDCode;
		    }
		    
		    try 
		    {
		      phoneNumberProto = D.phoneUtil.parse(rawNumber, D.curLocale);
		    } 
	
		    catch (NumberParseException e) 
		    {
		      return rawNumber;
		    }
	
		    //BLog.e("TELR", phoneNumberProto.toString());
		    if(curDCode.compareTo(ourDCode) == 0)
		        fixedNumber = D.phoneUtil.format(phoneNumberProto, PhoneNumberFormat.NATIONAL);
		    else
		        fixedNumber = D.phoneUtil.format(phoneNumberProto, PhoneNumberFormat.INTERNATIONAL);

		}
	    return fixedNumber;//.replace(" ", "");
	}
	
	public static boolean isWifiEnabled(Context context) {
		ConnectivityManager connec =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return wifi.isConnected();
	}
	public static void enableWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
		wifiManager.setWifiEnabled(true);
	}
	public static void disableWifi(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); 
		wifiManager.setWifiEnabled(false);
	}
	public static synchronized boolean CheckInternet(Context context)      {
		boolean hasConnection=false;
		try {
			CONNECTION_TYPE=CONNECTION_TYPE_NONE;
			ConnectivityManager connec =(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			android.net.NetworkInfo wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			android.net.NetworkInfo mobile =connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			if (wifi.isConnected()) {          
				hasConnection= true;     
				CONNECTION_TYPE=CONNECTION_TYPE_WIFI;
				WifiManager wifiMan = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
				CONNECTION_STRENGTH=wifiMan.getConnectionInfo().getRssi();
				if(wifiMan.getWifiState()==WifiManager.WIFI_STATE_ENABLED)
					CONNECTION_STATE=CONNECTION_STATE_CONNECTED;
				else if(wifiMan.getWifiState()==WifiManager.WIFI_STATE_ENABLING)
					CONNECTION_STATE=CONNECTION_STATE_CONNECTING;
				else
					CONNECTION_STATE=CONNECTION_STATE_NONE;
			} else if(mobile.isConnected())  {          
				hasConnection= true;
                CONNECTION_STRENGTH=0;
                // TO DO:  add connection strength for mobiles

				CONNECTION_TYPE=CONNECTION_TYPE_MOBILE;
                TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		        if(TelephonyMgr.getDataState()==TelephonyManager.DATA_CONNECTED)
		        	CONNECTION_STATE = CONNECTION_STATE_CONNECTED;
		        else if(TelephonyMgr.getDataState()==TelephonyManager.DATA_CONNECTING)
		        	CONNECTION_STATE = CONNECTION_STATE_CONNECTING;
		        else
		        	CONNECTION_STATE = CONNECTION_STATE_NONE;
			} 
		} catch(Exception e) {
			// error, report true anyway
			CONNECTION_STATE = CONNECTION_STATE_NONE;
		}
		return hasConnection;
	}
	public static void sendFileVia(Activity activity,File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file) );
		activity.startActivity(intent);
	}
	public static void openFile(Activity activity,File file) {
		//File filesDir = getFilesDir();
		//Scanner input = new Scanner(new File(filesDir, filename));
        //BLog.e("OPENFILE",file.getAbsolutePath());
		if(file!=null) {
			String ext=Files.getExtension(Files.removeBriefFileExtension(file.getAbsolutePath()));
            if(ext!=null) {
                ext = ext.replace(".", "");
                String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());

                if (mime != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), mime);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    //startActivityForResult(intent, 10);
                    //intent.setType("text/plain");
                    //intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file) );
                    activity.startActivity(intent);
                } else {
                    Toast.makeText(activity, activity.getResources().getString(R.string.no_file_format), Toast.LENGTH_SHORT).show();
                }
            }
		}
	}
	
	public static final boolean hasPhone() {
		return D.hasPhone;
	}


	public static final boolean hasContentProviderSmsSent() {
		return D.hasContentProviderSmsSent;
	}

	public static void vibrate(Context context) {
		vibrate(context,500);
	}
	public static void vibrate(Context context, int milliSeconds) {
		if(milliSeconds>0 && milliSeconds<2000) {
			Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
			vibrator.vibrate(milliSeconds);
		}
	}
	public static final String getPhoneNumber() {
		return D.phoneNumber;
	}


	public static boolean isInitialised() {
		return D.isInitialised;
	}


	
	public static boolean hasSmsSent(Activity activity) {
		Device.init(activity);
		return D.hasContentProvider(activity,SmsFunctions.CONTENT_PROVIDER_SMS_SENT);
	}
		
	
	
    private boolean hasContentProvider(Context context, String CONTENT_PROVIDER_) {
    	boolean isSuccess = false;
    	try {
	    	ContentProviderClient pc = context.getContentResolver().acquireContentProviderClient(getContentURI(CONTENT_PROVIDER_));
	    	if (pc != null) {
	    		pc.release();
	    		isSuccess = true;
	    	}
    	} catch(Exception e) {}
    	return isSuccess;
    }
    private static Uri getContentURI(String CONTENT_PROVIDER_) {
    	final Uri CONTENT_URI = Uri.parse(CONTENT_PROVIDER_);
    	return CONTENT_URI;

    }
	
	private static String getPhoneNumber(Context context) {
		if(D.phoneNumber==null) {
	    	try {
		        //1 compute IMEI
		        TelephonyManager TelephonyMgr = (TelephonyManager)context.getSystemService(Activity.TELEPHONY_SERVICE);
		        
		        D.phoneNumber = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
	    	} catch(Exception e) {
	    		// do nothing, yet ensure user does not get a force close if for some reason a error hits
	    	}
		}
    	return D.phoneNumber;
	}


    public static int getKeyboardManualHeight(Activity activity) {
        if(D.keyboardHeight==0)
            return activity.getResources().getDimensionPixelSize(R.dimen.keyboard_height);
        return D.keyboardHeight;
    }

    private static int getKeyboardHeightValue(Activity activity) {
        Rect r = new Rect();
        View rootview = activity.getWindow().getDecorView(); // this = activity
        rootview.getWindowVisibleDisplayFrame(r);
        int remainh=r.bottom;//-r.top;

        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        return height-remainh;
    }
    private static void setFirstTimeKeyboardHeight(int height) {
        //if(!State.getSettings().has(BriefSettings.INT_KEYBOARD_HEIGHT)) {

        BriefSettings set = State.getSettings();
        if(height>set.getInt(BriefSettings.INT_KEYBOARD_HEIGHT)) {
            set.setInt(BriefSettings.INT_KEYBOARD_HEIGHT, height);
            set.save();
        }
        //SettingsDb.Update(set);
        //SettingsDb.Save();
        //}
    }
    private static void setFirstTimeKeyboardHeightLandscape(int height) {
        //if(!State.getSettings().has(BriefSettings.INT_KEYBOARD_HEIGHT)) {

        BriefSettings set = State.getSettings();
        if(height>set.getInt(BriefSettings.INT_KEYBOARD_HEIGHT_LANDSCAPE)) {
            set.setInt(BriefSettings.INT_KEYBOARD_HEIGHT_LANDSCAPE, height);
            set.save();
        }
        //SettingsDb.Update(set);
        //SettingsDb.Save();
        //}
    }
    public static int getKeyboardHeight(Activity activity) {
        if(activity.getResources().getConfiguration().orientation== Configuration.ORIENTATION_LANDSCAPE) {
            return State.getSettings().getInt(BriefSettings.INT_KEYBOARD_HEIGHT_LANDSCAPE);
        }
        return State.getSettings().getInt(BriefSettings.INT_KEYBOARD_HEIGHT);
    }
    private static Handler getSetHeightHandler = new Handler();
    private static Handler doneFirsttime = new Handler();
    private class GetSetHeight implements Runnable {
        private Activity activity;


        //private View parentView;
        public void setActivity(Activity activity) {
            this.activity=activity;
        }

        @Override
        public void run() {
            int val = getKeyboardHeightValue(activity);

            if(activity.getResources().getConfiguration().orientation!= Configuration.ORIENTATION_LANDSCAPE) {

                setFirstTimeKeyboardHeight(val);

            } else {
                setFirstTimeKeyboardHeightLandscape(val);
            }
            if(ViewManagerText.getPopupWindow()!=null && ViewManagerText.getPopupWindow().isShowing() ) {

                ViewManagerText.getPopupWindow().setHeight(val);
            }
            //popupWindow.showAtLocation(parentView, Gravity.BOTTOM, 0, 0);
        }
    }

	public static void openAndroidFile(Activity activity, File file) {
		//File filesDir = getFilesDir();
		//Scanner input = new Scanner(new File(filesDir, filename));
		//BLog.e("OPENFILE",file.getAbsolutePath());
		if(file!=null) {
			String ext=Files.getExtension(Files.removeBriefFileExtension(file.getAbsolutePath()));
			if(ext!=null) {
				ext = ext.replace(".", "");
				String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext.toLowerCase());

				if (mime != null) {
					Intent intent = new Intent();
					intent.setAction(Intent.ACTION_VIEW);
					intent.setDataAndType(Uri.fromFile(file), mime);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					//startActivityForResult(intent, 10);
					//intent.setType("text/plain");
					//intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file) );
					try {
						activity.startActivity(intent);
					} catch (Exception e) {
						Toast.makeText(activity, activity.getResources().getString(R.string.no_file_format), Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(activity, activity.getResources().getString(R.string.no_file_format), Toast.LENGTH_SHORT).show();
				}
			}
		}
	}


    


}
