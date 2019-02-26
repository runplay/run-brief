package run.brief;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import run.brief.beans.BJSONBean;
import run.brief.secure.Encrypt;
import run.brief.secure.Validator;
import run.brief.util.Base64;
import run.brief.util.Cal;
import run.brief.util.FileReadTask;
import run.brief.util.FileWriteTask;
import run.brief.util.Files;
import run.brief.util.JSONUrlReader;
import run.brief.util.UrlStore;
import run.brief.util.json.JSONException;
import run.brief.util.json.JSONObject;

/**
 * Created by coops on 10/01/15.
 */
public final class HomeFarm {

    private static final HomeFarm HOME = new HomeFarm();

    private BriefPermissions permissions;

    private static final String memberCode="8R13F";

    private boolean isFirstTime=false;

    private PermissionsDb permissionsDb = new PermissionsDb();
    // first time

    private boolean isCommunicating =false;
    private RegisterTask registerTask;
    private HelloTask helloTask;



    private Context context;

    public static boolean isFirstTime() {
        return HOME.isFirstTime;
    }
    public static void finishedWithFirstTime() {
        HOME.isFirstTime=false;
    }

    public static boolean isAlive() {
        if(HOME.permissions!=null)
            return true;
        return false;
    }

    public static void init(Context context) {
        HOME.context = context;
        //BLog.e("HPATH","init()");
        if(HOME.permissions==null) {
            //BLog.e("HPATH",Files.HOME_PATH_APP);

            HOME.permissionsDb.init();
            HOME.permissions=HOME.permissionsDb.getPermissions();

            if(!HOME.permissionsDb.isRegistered()) {
                register();
            } else {
                HOME.isFirstTime=false;
                if(HOME.permissions.getLong(BriefPermissions.LONG_NEXT_CONTACT)<Cal.getUnixTime())
                    sayHello();

            }
        }

    }

    public static String getGoogleOAuthKey() {
        if(Validator.isValidCaller()) {
            return HOME.permissions.getString(BriefPermissions.STRING_GOOGLE_OAUTH_KEY);
        }
        return null;
    }
    public static final long getPlusMemberExpireDate() {
        if(HOME.permissions!=null) {
            return HOME.permissions.getLong(BriefPermissions.LONG_MEMBER_EXPIRE_DATE);
        }
        return Cal.getUnixTime()-100000;
    }
    public static final boolean isRegistered() {
        if(HOME.permissions!=null) {
            return HOME.permissionsDb.isRegistered();
        }
        return false;
    }
    public static final boolean isAppExpired() {
        if(HOME.permissions!=null) {
            if(HOME.permissions.getInt(BriefPermissions.INT_EXPIRE_LEVEL)==BriefPermissions.EXPIRE_LOCK)
                return true;
        }
        return false;
    }
    public static final boolean isSubscriber() {
        if(HOME.permissions!=null) {
            if(memberCode.equals(HOME.permissions.getString(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE))) {
                return true;
            }
        }
        return false;
    }


    private BriefPermissions createFirstTimePermissions(Context context) {
        BriefPermissions pem = new BriefPermissions(new JSONObject());

        String deviceId=getUniqueDeviceId(context);
        String androidId=getAndroidId(context);
        String telephonyId=getUniqueNumId(context);
        String phoneId=getMyPhoneNumber(context);
        String publicId=Encrypt.getRandomKey(Encrypt.KEYLENGTH_32);

        //BLog.e("LK",lockerKey);
        pem.setString(BriefPermissions.STRING_PUBLIC_ID,publicId);
        pem.setString(BriefPermissions.STRING_PROTECTED_DEVICE_ID,deviceId);
        pem.setString(BriefPermissions.STRING_PROTECTED_NUMBER_ID,phoneId);
        pem.setString(BriefPermissions.STRING_PROTECTED_ANDROID_ID,androidId);
        pem.setString(BriefPermissions.STRING_PROTECTED_DEVICE_ID_RAW,telephonyId);
        pem.setString(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE,memberCode);
        try {
            String versionName = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0).versionName;
            pem.setString(BriefPermissions.STRING_VERION,versionName);
        } catch(Exception e) {}

        return pem;
    }

    Handler homeCommHandler= new Handler();
    private synchronized static void sayHello() {
        if(!HOME.isCommunicating) {
            HOME.isCommunicating =true;
            HOME.helloTask=HOME.new HelloTask();
            HOME.homeCommHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HOME.helloTask.execute(true);
                }
            },1000);

        }
    }
    public synchronized static void register() {
        if(!HOME.isCommunicating) {
            HOME.isCommunicating =true;
            HOME.registerTask=HOME.new RegisterTask();
            HOME.homeCommHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HOME.registerTask.execute(true);
                }
            },1000);

        }
    }
    private String createStringUrl() {
        return "";
    }


    private String pemKey;
    public static final String getPemKey() {
        if(HOME.pemKey!=null)
            return HOME.pemKey;
        if(Validator.isValidCaller()) {
            Holder hold=HOME.new Holder();
            HOME.pemKey=getUniqueDeviceId(HOME.context)+"Br13fR0ck5"+getAndroidId(HOME.context);
            return HOME.pemKey;
        }
        return null;
    }
    private String lockerKey;
    public static final String getLocKey(Context context) {
        if(HOME.lockerKey!=null)
            return HOME.lockerKey;
        if(Validator.isValidCaller()) {
            Holder hold=HOME.new Holder();
            BriefPermissions bp = HOME.permissionsDb.getPermissions();
            HOME.lockerKey=getLockerKey(context,bp.getString(BriefPermissions.STRING_PROTECTED_ANDROID_ID),bp.getString(BriefPermissions.STRING_PROTECTED_DEVICE_ID));
            return HOME.lockerKey;
        }
        return null;
    }

    private class HelloTask extends AsyncTask<Boolean, Void, Boolean> {

        //private Activity activity;

        public HelloTask() {

        }
        @Override
        protected Boolean doInBackground(Boolean... params) {

            if(HOME.permissions!=null) {

                //BLog.e("PERM",HOME.permissions.toString());

                BriefPermissions sendpem = new BriefPermissions(new JSONObject());
                sendpem.setString(BriefPermissions.STRING_PUBLIC_ID,HOME.permissions.getString(BriefPermissions.STRING_PUBLIC_ID));
                sendpem.setString(BriefPermissions.STRING_PROTECTED_DEVICE_ID,HOME.permissions.getString(BriefPermissions.STRING_PROTECTED_DEVICE_ID));


                SendDataPacket send = new SendDataPacket();
                send.setString(SendDataPacket.STRING_REQUEST_CODE, CODE_SEND_REGISTER);
                send.setString(SendDataPacket.STRING_DATA, sendpem.toString());



                send.Encrypt(HOME.permissions.getFirstTimeKey());

                try {
                    String sendUri = UrlStore.URL_FARM_SEND + "?in=" + URLEncoder.encode(send.getSendData(), "UTF-8");

                    //BLog.e("SENDING------------------",sendUri);
                    JSONObject response = JSONUrlReader.readJsonFromUrl(sendUri);

                    //BLog.e("----RESPONCE", response.toString());
                    //BLog.e("----RESPONCE", response.optString("resp"));
                    String rdata = URLDecoder.decode(response.optString("resp"), "UTF-8");
                    ReceiveDataPacket rdp = new ReceiveDataPacket(rdata);
                    rdp.Decrypt(HOME.permissions.getFirstTimeKey());

                    if(HomeFarm.CODE_RESPOND_ERROR.equals(rdp.getString(ReceiveDataPacket.STRING_DATA))) {
                        HOME.permissions.setLong(BriefPermissions.LONG_NEXT_CONTACT, Cal.getUnixTime() + Cal.DAYS_7_IN_MILLIS);
                    } else {
                        BriefPermissions tmp = new BriefPermissions(new JSONObject(rdp.getString(ReceiveDataPacket.STRING_DATA)));
                        HOME.permissions.setLong(BriefPermissions.LONG_NEXT_CONTACT, Cal.getUnixTime() + tmp.getLong(BriefPermissions.LONG_NEXT_CONTACT));
                        if (tmp.has(BriefPermissions.INT_EXPIRE_LEVEL)) {
                            //BLog.e("expre", "level is: " + tmp.getInt(BriefPermissions.INT_EXPIRE_LEVEL));
                            HOME.permissions.setInt(BriefPermissions.INT_EXPIRE_LEVEL, tmp.getInt(BriefPermissions.INT_EXPIRE_LEVEL));
                        }
                        if (tmp.has(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE)) {
                            HOME.permissions.setString(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE,tmp.getString(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE));
                        }
                        if (tmp.has(BriefPermissions.LONG_MEMBER_EXPIRE_DATE)) {
                            HOME.permissions.setLong(BriefPermissions.LONG_MEMBER_EXPIRE_DATE,tmp.getLong(BriefPermissions.LONG_MEMBER_EXPIRE_DATE));
                        }
                        if (tmp.has(BriefPermissions.STRING_GOOGLE_OAUTH_KEY)) {
                            HOME.permissions.setString(BriefPermissions.STRING_GOOGLE_OAUTH_KEY, tmp.getString(BriefPermissions.STRING_GOOGLE_OAUTH_KEY));
                        }
                    }
                    HOME.permissionsDb.Update(HOME.permissions);
                    HOME.permissionsDb.Save();
                    //rdp.Decrypt(HOME.masterKey);
                } catch (Exception e) {
                    HOME.permissions.setLong(BriefPermissions.LONG_NEXT_CONTACT, Cal.getUnixTime() + Cal.HOURS_24_IN_MILLIS);
                    HOME.permissionsDb.Update(HOME.permissions);
                    HOME.permissionsDb.Save();
                   // BLog.e("EX", "ex: " + e.getMessage());
                }


            } else {
               // BLog.e("EX---", "HOME.permissions is NULL");
            }
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {

        }

    }
    private class RegisterTask extends AsyncTask<Boolean, Void, Boolean> {

        //private Activity activity;

        public RegisterTask() {

        }
        @Override
        protected Boolean doInBackground(Boolean... params) {

            if(HOME.permissions!=null) {

                //BLog.e("PERM",HOME.permissions.toString());

                BriefPermissions sendpem = new BriefPermissions(new JSONObject());
                sendpem.setString(BriefPermissions.STRING_PUBLIC_ID,HOME.permissions.getString(BriefPermissions.STRING_PUBLIC_ID));
                sendpem.setString(BriefPermissions.STRING_PROTECTED_DEVICE_ID,HOME.permissions.getString(BriefPermissions.STRING_PROTECTED_DEVICE_ID));


                SendDataPacket send = new SendDataPacket();
                send.setString(SendDataPacket.STRING_REQUEST_CODE, CODE_SEND_REGISTER);
                send.setString(SendDataPacket.STRING_DATA, HOME.permissions.toString());



                send.Encrypt(HOME.permissions.getFirstTimeKey());

                try {
                    String sendUri = UrlStore.URL_FARM_SEND + "?in=" + URLEncoder.encode(send.getSendData(), "UTF-8");

                    //BLog.e("SENDING------------------",sendUri);
                    JSONObject response = JSONUrlReader.readJsonFromUrl(sendUri);

                    //BLog.e("----RESPONCE", response.toString());
                    //BLog.e("----RESPONCE", response.optString("resp"));
                    String rdata = URLDecoder.decode(response.optString("resp"), "UTF-8");
                    ReceiveDataPacket rdp = new ReceiveDataPacket(rdata);
                    rdp.Decrypt(HOME.permissions.getFirstTimeKey());

                    BriefPermissions tmp = new BriefPermissions(new JSONObject(rdp.getString(ReceiveDataPacket.STRING_DATA)));
                    //BLog.e("----RESULT", tmp.getString(BriefPermissions.STRING_PUBLIC_CHALLENGE_ID) + " *******   " + rdp.getString(ReceiveDataPacket.STRING_DATA) + "  ****** ");

                    HOME.permissions.setLong(BriefPermissions.LONG_NEXT_CONTACT, Cal.getUnixTime()+tmp.getLong(BriefPermissions.LONG_NEXT_CONTACT));
                    if(tmp.has(BriefPermissions.INT_EXPIRE_LEVEL)) {
                        //BLog.e("expre","level is: "+tmp.getInt(BriefPermissions.INT_EXPIRE_LEVEL));
                        HOME.permissions.setInt(BriefPermissions.INT_EXPIRE_LEVEL,tmp.getInt(BriefPermissions.INT_EXPIRE_LEVEL));
                    }
                    if (tmp.has(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE)) {
                        HOME.permissions.setString(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE,tmp.getString(BriefPermissions.STRING_PROTECTED_MEMBER_LEVEL_CODE));
                    }
                    if (tmp.has(BriefPermissions.LONG_MEMBER_EXPIRE_DATE)) {
                        HOME.permissions.setLong(BriefPermissions.LONG_MEMBER_EXPIRE_DATE,tmp.getLong(BriefPermissions.LONG_MEMBER_EXPIRE_DATE));
                    }
                    HOME.permissions.setString(BriefPermissions.STRING_GOOGLE_OAUTH_KEY, tmp.getString(BriefPermissions.STRING_GOOGLE_OAUTH_KEY));
                    HOME.permissions.setString(BriefPermissions.STRING_PUBLIC_CHALLENGE_ID, tmp.getString(BriefPermissions.STRING_PUBLIC_CHALLENGE_ID));

                    HOME.permissionsDb.Update(HOME.permissions);
                    HOME.permissionsDb.Save();


                    //rdp.Decrypt(HOME.masterKey);
                } catch (JSONException je) {
                    //Log.e("EX", "js: " + je.getMessage());
                } catch (IOException ie) {
                    //Log.e("EX", "io: " + ie.getMessage());
                } catch (Exception e) {

                    //BLog.e("EX", "ex: " + e.getMessage());
                }


            } else {
                //BLog.e("EX---", "HOME.permissions is NULL");
            }
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {

        }

    }


    public static class BaseEncode {
        private static final String baseChars =
                "br13fly58r";


        public static String encode(long encodeId) {
            String encStr = Long.toString(encodeId);
            StringBuilder sb=new StringBuilder();
            for(int i=0; i<encStr.length(); i++)
                sb.append(baseChars.charAt(Integer.parseInt(String.valueOf(encStr.charAt(i)))));
            return sb.toString();
        }
        public static long decode(String decodeString) {
            StringBuilder sb = new StringBuilder();
            int index=0;
            while(index<=(decodeString.length()-1)) {
                sb.append(getCharIndex(decodeString.charAt(index)));
                index++;
            }
            Long ret=null;
            try {
                ret=Long.valueOf(sb.toString());
            } catch(Exception e) {}
            if(ret!=null)
                return ret;
            else
                return 0;
        }
        private static int getCharIndex(char c) {
            int index=0;
            for(int i=0; i<baseChars.length(); i++) {
                if(baseChars.charAt(i)==c) {
                    index=i;
                    break;
                }
            }
            return index;
        }
    }




    private String getMyPhoneNumber(Context context){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
        return mTelephonyMgr.getLine1Number();
        } catch(SecurityException e) {}
        return "";
    }

    private static String getUniqueNumId(Context context) {
        final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        long hashc=0;
        long hashcs=0;
        try {
            tmDevice = "" + tm.getDeviceId();
            tmSerial = "" + tm.getSimSerialNumber();
            hashc=(long)tmDevice.hashCode();
            hashcs=tmSerial.hashCode();
        } catch(SecurityException e) {}
        androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), (hashc << 32) | hashcs);
        return deviceUuid.toString();
    }

    private static final String APP_PARTNER="GP";

    private static String getLockerKey(Context context, String androidId, String deviceId) {
        String m_szAndroidID="";
        String m_szWLANMAC="";
        String m_szImei="";
        String m_szDevIDShort="";
        String m_szUniqueID = "";
        try {
            //1 compute IMEI
            TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
            m_szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
        } catch(SecurityException e) {
            // do nothing, yet ensure user does not get a force close if for some reason a error hits
        }

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
        m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        //6 CONCAT THE IDs
        String m_szLongID = m_szImei + m_szDevIDShort+deviceId + m_szAndroidID+uuid1+ m_szWLANMAC+androidId+uuid2;


        return m_szLongID;
/*
        BLog.e("HHF",m_szLongID);
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
        }
        byte p_md5Data[]=null;
        if(m==null) {
            p_md5Data= Base64.encodeToByte(m_szLongID.getBytes(),false);
        } else {
            m.update(m_szLongID.getBytes(),0,m_szLongID.length());
            p_md5Data= m.digest();
        }



        for (int i=0;i<p_md5Data.length;i++) {
            int b =  (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF) m_szUniqueID+="0";
            // add number to string
            m_szUniqueID+=Integer.toHexString(b);
        }
        m_szUniqueID = m_szUniqueID.toUpperCase();

        return m_szUniqueID;
*/
    }

    /*
    private static String getLockerKey(Context context, String androidId, String deviceId, String telephonyId) {
        String m_szAndroidID="";
        String m_szWLANMAC="";
        String m_szImei="";
        String m_szDevIDShort="";
        String m_szUniqueID = "";
        try {
            //1 compute IMEI
            TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
            m_szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
        } catch(Exception e) {
            // do nothing, yet ensure user does not get a force close if for some reason a error hits
        }

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
        m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

        //4 wifi manager, read MAC address - requires  android.permission.ACCESS_WIFI_STATE or comes as null
        WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        m_szWLANMAC = wm.getConnectionInfo().getMacAddress();


        //6 CONCAT THE IDs
        String m_szLongID = m_szImei +telephonyId+ m_szDevIDShort+deviceId + m_szAndroidID+uuid1+ m_szWLANMAC+androidId+uuid2+telephonyId;
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
        }
        byte p_md5Data[]=null;
        if(m==null) {
            p_md5Data= Base64.encodeToByte(m_szLongID.getBytes(),false);
        } else {
            m.update(m_szLongID.getBytes(),0,m_szLongID.length());
            p_md5Data= m.digest();
        }



        for (int i=0;i<p_md5Data.length;i++) {
            int b =  (0xFF & p_md5Data[i]);
            // if it is a single digit, make sure it have 0 in front (proper padding)
            if (b <= 0xF) m_szUniqueID+="0";
            // add number to string
            m_szUniqueID+=Integer.toHexString(b);
        }
        m_szUniqueID = m_szUniqueID.toUpperCase();

        return m_szUniqueID;

    }
    */
    private static String getUniqueDeviceId(Context context) {
        String m_szAndroidID="";
        String m_szWLANMAC="";
        String m_szImei="";
        String m_szDevIDShort="";
        String m_szUniqueID = "";
        try {
            //1 compute IMEI
            TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
            m_szImei = TelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE
        } catch(SecurityException e) {
            // do nothing, yet ensure user does not get a force close if for some reason a error hits
        }

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
            m_szAndroidID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);

            //4 wifi manager, read MAC address - requires  android.permission.ACCESS_WIFI_STATE or comes as null
            WifiManager wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            m_szWLANMAC = wm.getConnectionInfo().getMacAddress();


            //6 CONCAT THE IDs
            String m_szLongID = m_szImei + m_szDevIDShort + m_szAndroidID+ m_szWLANMAC;
            MessageDigest m = null;
            try {
                m = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                //e.printStackTrace();
            }
            byte p_md5Data[]=null;
            if(m==null) {
                p_md5Data= Base64.encodeToByte(m_szLongID.getBytes(),false);
            } else {
                m.update(m_szLongID.getBytes(),0,m_szLongID.length());
                p_md5Data= m.digest();
            }



            for (int i=0;i<p_md5Data.length;i++) {
                int b =  (0xFF & p_md5Data[i]);
                // if it is a single digit, make sure it have 0 in front (proper padding)
                if (b <= 0xF) m_szUniqueID+="0";
                // add number to string
                m_szUniqueID+=Integer.toHexString(b);
            }
            m_szUniqueID = m_szUniqueID.toUpperCase();

        return m_szUniqueID+APP_PARTNER;

    }




    private static String getAndroidId(Context context) {
        return android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }










    private class PermissionsDb {

        private static final String dbArrayName="p3m15510n5";

        private BriefPermissions permissions;
        private boolean isLoaded=false;

        private FileWriteTask fwt;
        private FileReadTask frt;

        private boolean exists=false;

        protected PermissionsDb() {

        }

        protected boolean isLoaded() {
            return isLoaded;
        }
        protected boolean isRegistered() {
            if(permissions!=null)
                return permissions.has(BriefPermissions.STRING_PUBLIC_CHALLENGE_ID);
            return false;
        }

        protected boolean exists() {
            frt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_PERMISSIONS);
            return frt.exists();
        }
        protected BriefPermissions getPermissions() {
            return permissions;
        }

        protected void Update(BriefPermissions permissions) {
            this.permissions=permissions;
        }

        protected boolean Save() {

            try {
                //BLog.e("SAVE", "" + permissions.getBean().toString());
                fwt=new FileWriteTask(Files.HOME_PATH_APP, Files.FILENAME_PERMISSIONS, permissions.getBean().toString());
                return fwt.WriteSecureToSd();

            } catch(Exception e) {
               // BLog.e("BDP.error", e.getMessage());

            }

            return false;
        }

        protected void init() {

            if(!isLoaded) {

                frt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_PERMISSIONS);

                if(frt.ReadSecureFromSd()) {
                    //BLog.e("perms", "--" + frt.getFileContent());
                    if(frt.getFileContent()!=null && !frt.getFileContent().isEmpty()) {
                        try {
                            JSONObject db = new JSONObject(frt.getFileContent());
                            if(db!=null) {
                                permissions = new BriefPermissions(db);

                                if(permissions.has(BriefPermissions.STRING_PUBLIC_ID)) {
                                    isLoaded=true;
                                } else {
                                   //BLog.e("Pb.json().no create",""+permissions.toString());
                                }
                            }
                        } catch(Exception e) {
                            //if(e.getMessage()!=null)
                            //BLog.e("Pb.init().bad data",e.getMessage());
                        }
                    } else {
                        //BLog.e("Pb.init().empty",frt.getStatusMessage());
                    }
                } else {

                    //BLog.e("Pb.init().no read",frt.getStatusMessage());
                }
            }

            if(!isLoaded) {
                //BLog.e("Pb.init().no read","FIRST TIME !!!!!!!!");
                HOME.isFirstTime=true;
                permissions = createFirstTimePermissions(context);
                Save();
                //DB.stats.setLong(BriefUserStats.LONG_DATE_STARTED, (new Date()).getTime());
                //DB.permissions.setInt(BriefUserStats.INT_COUNT_LAUNCH, 0);
                isLoaded=true;

            }

        }

    }









    //  KEYS SHARED WITH SERVERS.. DO NOT CHANGE


    private class ReceiveDataPacket extends BJSONBean {
        private static final String STRING_RESULT_CODE ="rc";
        private static final String STRING_DATA="d";
        private boolean isDecrypted=false;
        private String decData;

        public ReceiveDataPacket(String decData) {
            super();
            this.decData=decData;
            //setString(STRING_DATA, encData);
        }
        public void Decrypt(String withKey) {
            if(!isDecrypted) {
                //BLog.e("DDD","BOOL:   "+isDecrypted+" -- "+decData);
                byte[] raw = Encrypt.decodeFromEncriptionRaw(decData);
                if(raw!=null) {
                    //BLog.e("DDD","RAW:   "+new String(raw));
                    //System.out.println("D--DEC--DATA:   "+raw);
                    Encrypt enc = null;
                    try {
                        enc=new Encrypt(withKey);
                        if(enc!=null) {

                            byte[] decrypted = enc.decrypt(raw);
                            String decStr=new String(decrypted);

                            //BLog.e("DDD","D--DEC----P:   "+decStr);

                            JSONObject job = new JSONObject(decStr);

                            setString(STRING_RESULT_CODE,job.getString(STRING_RESULT_CODE));
                            setString(STRING_DATA,job.getString(STRING_DATA));

                            //BLog.e("DDD","D--DEC----P:   "+job.toString());


                            isDecrypted=true;
                        }
                    } catch(Exception e) {
                        //BLog.e("Farm-s","ex: "+e.getMessage());
                        //System.out.println("EX: "+e.getMessage());
                    }
                }
            }
        }
    }


    private class SendDataPacket extends BJSONBean {
        private static final String STRING_REQUEST_CODE="rc";
        private static final String STRING_DATA="d";

        private String sendEncData;

        public String getSendData() {
            return sendEncData;
        }

        private boolean isEncrypted=false;
        public SendDataPacket() {
            super();
        }
        public void Encrypt(String withKey) {
            String data = getBean().toString();
            if(data!=null && !data.isEmpty()) {
                Encrypt enc = null;
                try {
                    //BLog.e("usekey",withKey);
                    enc=new Encrypt(withKey);
                    if(enc!=null) {

                        byte[] encrypted = enc.encrypt(data.getBytes("UTF-8"));
                        String edata=Encrypt.encodeForEncriptionRaw(encrypted);
                        //BLog.e("EDATA2---",edata);
                        sendEncData=edata;//new String(edata);
                        isEncrypted=true;
                    }
                } catch(Exception e) {
                    //BLog.e("EXC","e: "+e.getMessage());
                }
            }
        }
    }




    public static final String CODE_SEND_REGISTER="regt";
    public static final String CODE_RESPOND_OK="201";
    public static final String CODE_RESPOND_ERROR="69";


    private class BriefPermissions extends BJSONBean {

        private static final String STRING_PUBLIC_ID="pubid";
        private static final String STRING_PUBLIC_CHALLENGE_ID="chaid";
        private static final String STRING_PROTECTED_MEMBER_LEVEL_CODE="isasub";
        private static final String LONG_MEMBER_EXPIRE_DATE="dteexp";

        private static final String STRING_PROTECTED_DEVICE_ID="devid";
        private static final String STRING_PROTECTED_DEVICE_ID_RAW="devidraw";
        private static final String STRING_PROTECTED_ANDROID_ID="andid";
        private static final String STRING_PROTECTED_NUMBER_ID="numid";
        private static final String STRING_VERION="versid";
        private static final String LONG_NEXT_CONTACT="nxtcon";

        private static final String STRING_GOOGLE_OAUTH_KEY="googauth";



        private static final String INT_EXPIRE_LEVEL="expl";

        private static final int EXPIRE_NONE=0;
        private static final int EXPIRE_WARN=3;
        private static final int EXPIRE_LOCK=6;

        //private static final String STRING_PRIVATE_LOCKER_KEY="lockey";


        private static final String INT_FAILED_ATTEMPTS="pfail";

        public BriefPermissions(JSONObject job) {
            super(job);
        }

        public String getFirstTimeKey() {
            Holder h=new Holder();
            return h.FT_KEY;
        }

        public boolean isSubscriber() {
            return false;
        }

    }
    // never change
    private static String uuid1="fuvx43QmIHyrlTxKt_DYnRrATjtZS3nSsosSBOWa9u69tnRr";
    private static String uuid2="XUiCn6wOtTlnDJaDnmTB61soBECrn_5NZR_aWczCVl4owi61";

    // end
    private class Holder {
        private String k0="dg2rssoDYElC_67g6y";  private String k18="BMqwKt_DYnRrA4aadF";  private String k36="fuvx43QmIHyrlTxTjt";  private String k54="ybKTfO7_Hf5QEqcgg5";  private String k72="0oCGNKrZ8L9SnLQtBt";  private String k90="gaXg0DLDPFQGVDLOXS";  private String k108="yAWosuZXR4IO4RY5Q8";  private String k126="A8OW5RnsYe0onVsVB4";  private String k144="FNjlyZWykE3DqNuTgd";  private String k162="ZS3nSsosSBOWa9u69t";  private String k180="8XFyv0LG1u4499jbTd";  private String k198="t5ouXIzQgJ9BV7ZPMD";  private String k216="vFiNEMtPkhyyjiyXGp";  private String k234="8XMImd7WG_CKBzE6LY";  private String k252="hjRNXroHxuV5eEUuwE";  private String k270="CWIa33ci3geL5YsTdT";  private String k288="w0O24dViJVRmWVGbgu";  private String k306="fhgVmOyTzSnk27r6QL";  private String k324="gVXCyLMHm8PSU4iix7";  private String k342="Be58evGDfBWpe07ORv";  private String k360="TBYOGwjEbfrxAenWQS";  private String k378="1dBsgG7Ms6TaAW28Se";  private String k396="cngrjbITLSILV3NUSo";  private String k414="X76PVyly_pTUF2Ui6h";  private String k432="o18f2Frgh0L_4CZ485";  private String k450="G5g3iIOu75QaxuBJ7q";  private String k468="26fDqBvG7bssW_7p9H";  private String k486="pkU_j_BDhwNc1Oci91";  private String k504="Ut9wgj1";

        private String FT_KEY = k0 + k18 + k36 + k54 + k72 + k90 + k108 + k126 + k144 + k162 + k180 + k198 + k216 + k234 + k252 + k270 + k288 + k306 + k324 + k342 + k360 + k378 + k396 + k414 + k432 + k450 + k468 + k486 + k504;

    }
    private class DeviceHolder {

        private String kdv0="mTB61soBECrn_5NZR_";  private String kdv18="Ma3ihORlH5chrYyezg";  private String kdv36="FsaWczCVl4owi6xc54";  private String kdv54="rsKpdpTXdQ4OYuQUQI";  private String kdv72="8XUiCn6wOtTlnDJaDn";  private String kdv90="MM_3RJN9ihM_yi6oSx";  private String kdv108="2Wckh9GJOSgl5KGXLM";  private String kdv126="uS_ieKPInARTvnetRj";  private String kdv144="LGf69e9t1asf298eKL";  private String kdv162="_ZzGr5kDj00mEpszzl";  private String kdv180="VkQwnHmDZxtoqTPTOA";  private String kdv198="qCo29rMeRDAK4EZ96E";  private String kdv216="Q9GwuwgIQRagA6YuK5";  private String kdv234="WNwSNPOsm_5cjxMG4E";  private String kdv252="loFaCR3hlohWoox35Z";  private String kdv270="mWrFUeKqxYsBZ4hRjA";  private String kdv288="L4ZSjRfMW59vDCO9nz";  private String kdv306="dcFu4rWLsd6VD1enn9";  private String kdv324="JBEfBqUbAKkiwUeIhG";  private String kdv342="J_vYlj7fZbkh0CjUR8";  private String kdv360="Xsy68o3bWZHAkVtI7C";  private String kdv378="Drwnl5hdKfBBFj2kDk";  private String kdv396="nTWZt6gGy9cHM4spGQ";  private String kdv414="m1PrqnKpmjJn_MB2gQ";  private String kdv432="IxuD396o2e0HryGRth";  private String kdv450="muiSs418b_FD3bYsZV";  private String kdv468="SmokaSGpfJ4Bl9__NZ";  private String kdv486="4KbYRiitR3uIQNZ5bW";  private String kdv504="vrqEoK_";

        private String FT_KEY = kdv0 + kdv18 + kdv36 + kdv54 + kdv72 + kdv90 + kdv108 + kdv126 + kdv144 + kdv162 + kdv180 + kdv198 + kdv216 + kdv234 + kdv252 + kdv270 + kdv288 + kdv306 + kdv324 + kdv342 + kdv360 + kdv378 + kdv396 + kdv414 + kdv432 + kdv450 + kdv468 + kdv486 + kdv504;

    }

    //  END -- KEYS SHARED WITH SERVERS.. DO NOT CHANGE
}
