package run.brief.beans;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import run.brief.HomeFarm;
import run.brief.R;
import run.brief.settings.AccountsDb;
import run.brief.settings.OAuth.GetGoogleAccessToken;
import run.brief.util.Cal;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;


public final class Account extends BJSONBean {
	
	public static final int TYPE_BRIEF=0;
	public static final int TYPE_EMAIL=1;


	public static final int TYPE_TWITTER=5;
	public static final int TYPE_LINKEDIN=6;
	public static final int TYPE_FACEBOOK=7;

	public static final int EMAIL_USE_IMAP=0;
	public static final int EMAIL_USE_POP=1;
	public static final int EMAIL_USE_EXCHANGE=2;
	
	public static final int SUBTYPE_NONE=0;
	public static final int SUBTYPE_SSL=1;
	public static final int SUBTYPE_TLS=2;
    public static final int SUBTYPE_GOOGLEMAIL=5;


	
	public static final String LONG_ID="id";
	public static final String BOOLEAN_ENABLED="en";
	public static final String STRING_NAME="n";
	public static final String STRING_LOGIN_NAME="ln";
	public static final String STRING_LOGIN_PASSWORD="lp";
	public static final String INT_TYPE_="t";
	public static final String INT_EMAIL_USE_="emu";
	//public static final String INT_SUBTYPE_="st";
	public static final String STRING_EMAIL_ADDRESS="ema";
	public static final String INT_SYNC_PERIOD="sync";
	
	public static final String STRING_EMAIL_OUTGOING_SERVER="emouts";
	public static final String INT_EMAIL_OUTGOING_PORT="emoutp";
	public static final String INT_EMAIL_OUTGOING_SUBTYPE_="emoutt";
	public static final String STRING_EMAIL_INCOMING_SERVER="emins";
	public static final String INT_EMAIL_INCOMING_PORT="eminp";
	public static final String INT_EMAIL_INCOMING_SUBTYPE_="emint";
	public static final String INT_LIMIT_ATTACHMENT_SIZE ="elim";
    public static final String STRING_EMAIL_FOLDERS="emfold";
    public static final String STRING_EMAIL_FOLDERS_OTHER="emotfold";
    public static final String INT_OAUTH_TOKEN_FAILS="oauth";
    public static final String INT_SIGNATURE_REKEY="oauthrek";
    public static final String STRING_OAUTH_CODE="atc";
    public static final String STRING_OAUTH_TOKEN_ACCESS ="at";
    public static final String STRING_OAUTH_TOKEN_ID ="atid";

    public static final String LONG_OAUTH_TOKEN_EXPIRE="atexp";

    public static final String STRING_OAUTH_TOKEN_REFRESH="atref";
    public static final String STRING_OAUTH_TOKEN_SECRET="ats";

    public static final String STRING_PROFILE_NAME="pname";
    public static final String STRING_PROFILE_PIC="ppic";
	


    private static final int[] iconemails = new int[]{R.drawable.i_email0,R.drawable.i_email1,R.drawable.i_email2,R.drawable.i_email3,R.drawable.i_email4};
	
	


    public int getAccountRIcon() {
        int id = Long.valueOf(getLong(LONG_ID)%iconemails.length).intValue();
        return iconemails[id];
    }

	public Account(JSONObject account) {
		super(account);
	}
	public Account(int TYPE_) {
		this.bean=new JSONObject();
		this.setInt(INT_TYPE_,TYPE_);
        setLong(LONG_ID,generateAccountId());
	}
	public Account() {

        super();
        setInt(INT_LIMIT_ATTACHMENT_SIZE,50);
        setLong(LONG_ID,generateAccountId());
	}
	public static long generateAccountId() {
		return Cal.getUnixTime();
	}
	public boolean isEnabled() {
		return this.bean.getBoolean(BOOLEAN_ENABLED);
	}
	public void setEnabled(boolean enabled) {
		this.bean.put(BOOLEAN_ENABLED, enabled);
	}

    public String getTokenNoAsync() {

        //return getString(Account.STRING_OAUTH_TOKEN_ACCESS);

        long now=Cal.getUnixTime();
        if((now-30000)>getLong(LONG_OAUTH_TOKEN_EXPIRE)) {

            JSONObject json = null;

            //BLog.e("getTOK", "get new token");
            try {
                String code = getString(Account.STRING_OAUTH_TOKEN_REFRESH);
                // get remote token again "GetAccessToken"
                GetGoogleAccessToken jParser = new GetGoogleAccessToken();

                json = jParser.gettoken(GetGoogleAccessToken.TOKEN_URL, code, HomeFarm.getGoogleOAuthKey(), GetGoogleAccessToken.CLIENT_SECRET, GetGoogleAccessToken.REDIRECT_URI, GetGoogleAccessToken.GRANT_TYPE_REFRESH);

                String tok = json.getString("access_token");
                long expire = json.getLong("expires_in");
                //String refresh = json.getString("refresh_token");
                String idToken = json.getString("id_token");

                setString(Account.STRING_OAUTH_CODE, code);
                setString(Account.STRING_OAUTH_TOKEN_ACCESS, tok);
                setString(Account.STRING_OAUTH_TOKEN_ID, idToken);
                setLong(Account.LONG_OAUTH_TOKEN_EXPIRE, Cal.getUnixTime() + (expire));
                setInt(Account.INT_OAUTH_TOKEN_FAILS, 0);
                setInt(Account.INT_SIGNATURE_REKEY, 0);
                //setString(Account.STRING_OAUTH_TOKEN_REFRESH, refresh);

                BLog.e("GOT","new token from refresh");
                AccountsDb.updateAccount(this);

            } catch(Exception e) {
                BLog.e("ERR","on rfreh t: "+e.getMessage());
            }

            return getString(STRING_OAUTH_TOKEN_ACCESS);

        } else {
            BLog.e("getTOK","use cached token");
            // token still ok use same.

            return getString(STRING_OAUTH_TOKEN_ACCESS);
        }

    }
    public String getSentFolder() {
        String folder=null;
        for(String f:getEmailFolders()) {
            if(f.toLowerCase(Locale.getDefault()).contains("sent"))
                return  f;
        }
        for(String f:getEmailFoldersOther()) {
            if(f.toLowerCase(Locale.getDefault()).equals("sent"))
                return  f;
        }
        return Email.FOLDER_SENT;
    }
    public String getInboxFolder() {
        String folder=null;
        for(String f:getEmailFolders()) {
            if(f.toLowerCase(Locale.getDefault()).contains("inbox"))
                return  f;
        }
        for(String f:getEmailFoldersOther()) {
            if(f.toLowerCase(Locale.getDefault()).equals("inbox"))
                return  f;
        }
        return Email.FOLDER_INBOX;
    }
    public List<String> getEmailFolders() {
        List<String> folders=new ArrayList<String>();
        String foldersStr = getString(STRING_EMAIL_FOLDERS);
        if(foldersStr.length()>0) {
            folders=new ArrayList<String>(Arrays.asList(foldersStr.split(",")));
        }
        return folders;
    }
    public void setEmailFolders(List<String> folders) {
        StringBuilder sb=new StringBuilder();
        for(String folder: folders) {
            if(sb.length()>0)
                sb.append(",");
            sb.append(folder.trim());
        }
        setString(STRING_EMAIL_FOLDERS,sb.toString());
    }
    public List<String> getEmailFoldersOther() {
        List<String> folders=new ArrayList<String>();
        String foldersStr = getString(STRING_EMAIL_FOLDERS_OTHER);
        if(foldersStr.length()>0) {
            folders=new ArrayList<String>(Arrays.asList(foldersStr.split(",")));
        }
        return folders;
    }
    public void setEmailFoldersOther(List<String> folders) {
        StringBuilder sb=new StringBuilder();
        for(String folder: folders) {
            if(sb.length()>0)
                sb.append(",");
            sb.append(folder.trim());
        }
        setString(STRING_EMAIL_FOLDERS_OTHER,sb.toString());
    }
	/*
	public String getTypeAsString() {
		return types[this.bean.getInt(INT_TYPE_)];
	}
	*/
}
