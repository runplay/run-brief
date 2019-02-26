package run.brief.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

public class AppManager {

	private static final String ERROR_IN_GENERATE="500nogenerate";
	
	// Apps that cannot generate the getUniqueDeviceId() are now allowed to run at all, no internet capabilities.
	public boolean IsAllowedApp(Activity activity) {
		boolean isAllowed=true;
		
		if(getUniqueDeviceId(activity).equals(ERROR_IN_GENERATE))
			isAllowed=false;
		return isAllowed;
	}
	
    protected String getUniqueNumId(Activity activity) {

    	String m_szImei="";
    	String m_szUniqueID = null;
    	try {
	        //1 compute IMEI
	        TelephonyManager TelephonyMgr = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
	        
	    	m_szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
	    	
	    	String m_szLongID = m_szImei;
	    	MessageDigest m = null;
			try {
				m = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				//e.printStackTrace();
			} 
			m.update(m_szLongID.getBytes(),0,m_szLongID.length());
			byte p_md5Data[] = m.digest();
			
			
			for (int i=0;i<p_md5Data.length;i++) {
				int b =  (0xFF & p_md5Data[i]);
				// if it is a single digit, make sure it have 0 in front (proper padding)
				if (b <= 0xF) m_szUniqueID+="0";
				// add number to string
				m_szUniqueID+=Integer.toHexString(b); 
			}
			m_szUniqueID = m_szUniqueID.toUpperCase();
    	} catch(Exception e) {
    		// do nothing, yet ensure user does not get a force close if for some reason a error hits
    	}
    	if(m_szUniqueID!=null)
    		return m_szUniqueID;
    	else 
    		return ERROR_IN_GENERATE;
    }
    private String getUniqueDeviceId(Activity activity) {
    	String m_szAndroidID="";
    	String m_szWLANMAC="";
    	String m_szImei="";
    	String m_szDevIDShort="";
    	String m_szUniqueID = "";
    	try {
	        //1 compute IMEI
	        TelephonyManager TelephonyMgr = (TelephonyManager)activity.getSystemService(Context.TELEPHONY_SERVICE);
	    	m_szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
	    	
	        //2 compute DEVICE ID
	        m_szDevIDShort = "35" + //we make this look like a valid IMEI
	        	Build.BOARD.length()%10+ Build.BRAND.length()%10 + 
	        	Build.CPU_ABI.length()%10 + Build.DEVICE.length()%10 + 
	        	Build.DISPLAY.length()%10 + Build.HOST.length()%10 + 
	        	Build.ID.length()%10 + Build.MANUFACTURER.length()%10 + 
	        	Build.MODEL.length()%10 + Build.PRODUCT.length()%10 + 
	        	Build.TAGS.length()%10 + Build.TYPE.length()%10 + 
	        	Build.USER.length()%10 ; //13 digits
	        //3 android ID - unreliable
	        m_szAndroidID = Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID); 
	        
	        //4 wifi manager, read MAC address - requires  android.permission.ACCESS_WIFI_STATE or comes as null
	        WifiManager wm = (WifiManager)activity.getSystemService(Context.WIFI_SERVICE);
	        m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
	
	
	    	//6 CONCAT THE IDs
	    	String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID+ m_szWLANMAC;
	    	MessageDigest m = null;
			try {
				m = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException e) {
				//e.printStackTrace();
			} 
			m.update(m_szLongID.getBytes(),0,m_szLongID.length());
			byte p_md5Data[] = m.digest();
			
			
			for (int i=0;i<p_md5Data.length;i++) {
				int b =  (0xFF & p_md5Data[i]);
				// if it is a single digit, make sure it have 0 in front (proper padding)
				if (b <= 0xF) m_szUniqueID+="0";
				// add number to string
				m_szUniqueID+=Integer.toHexString(b); 
			}
			m_szUniqueID = m_szUniqueID.toUpperCase();
    	} catch(Exception e) {
    		// do nothing, yet ensure user does not get a force close if for some reason a error hits
    	}
    	if(m_szUniqueID!=null)
    		return m_szUniqueID;
    	else 
    		return ERROR_IN_GENERATE;
    }
}
