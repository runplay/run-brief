package run.brief.settings;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.SyncData;
import run.brief.email.DefaultProperties;
import run.brief.secure.BOAUTHFragment;
import run.brief.service.BriefService;
import run.brief.service.SyncDataDb;
import run.brief.settings.OAuth.GetGoogleAccessToken;
import run.brief.settings.OAuth.OAuthHelper;
import run.brief.util.Cal;
import run.brief.util.json.JSONException;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;


public class GmailAddFragment extends BOAUTHFragment {
	
	//private Handler twitterHandler = new Handler();
	
	View view;
	Activity activity=null;


    //Use your own client id

    //Change the Scope as you need
    public static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private WebView web;
    private TextView auth;
    private String useCode;
    private String useEmail;
    private ProgressBar progress;
    private Dialog auth_dialog;
    private CookieManager cookieManager;

    private String forEmail;

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity=getActivity();


		// ensure Twitter accounts Db is initialised
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(activity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .build();
        //Scopes.
		//TwitterAccountsDb.init();
		
		view=inflater.inflate(R.layout.accounts_add_google,container, false);

        progress=(ProgressBar) view.findViewById(R.id.google_progress_bar);
        auth = (TextView) view.findViewById(R.id.settings_google_accounts_overlay_text);
        B.addStyle(auth);
		//web = (WebView)view.findViewById(R.id.account_oauth_webview);
		
		
		return view;
		
	}
	@Override
	public void onResume() {
		super.onResume();
        State.setCurrentSection(State.SECTION_OAUTH_GOOGLE);

        ActionBarManager.setActionBarBackOnly(activity, activity.getString(R.string.label_email), R.menu.basic,R.color.actionbar_email);

        if(State.hasStateObject(State.SECTION_OAUTH_GOOGLE, StateObject.STRING_VALUE)) {
            forEmail=State.getStateObjectString(State.SECTION_OAUTH_GOOGLE, StateObject.STRING_VALUE);
            State.clearStateObjects(State.SECTION_OAUTH_GOOGLE);
        } else {
            forEmail=null;
        }

        view.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(auth_dialog==null || !auth_dialog.isShowing()) {
                    if(HomeFarm.isRegistered()) {

                        goGoogleAuth();
                    }
                }
            }
        });

        if(HomeFarm.isRegistered()) {
            goGoogleAuth();
            auth.setText(R.string.accounts_add_email);
        } else {
            HomeFarm.register();
            waitcount=0;
            waitmo.postDelayed(waitrun,2200);
        }

		//goTwitterSetup();
	}
    private Runnable waitrun =new Runnable() {
        @Override
        public void run() {
            if(HomeFarm.isRegistered()) {
                goGoogleAuth();
                auth.setText(R.string.accounts_add_email);
            } else {
                auth.setText(activity.getString(R.string.email_google_wait));
                if(waitcount<3)
                    waitmo.postDelayed(waitrun,2200);

            }
        }
    };
    int waitcount=0;
    Handler waitmo = new Handler();
    @SuppressWarnings("deprecation")
    private void goGoogleAuth() {

        progress.setVisibility(View.GONE);

        auth_dialog = new Dialog(activity);

        //auth_dialog.
        auth_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        auth_dialog.setContentView(R.layout.accounts_oauth_google);

        auth_dialog.getWindow().setLayout(400, WindowManager.LayoutParams.WRAP_CONTENT);
        web = (WebView)auth_dialog.findViewById(R.id.account_oauth_webview);
        web.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT <= 18) {
            // this is depreciated
            web.getSettings().setSavePassword(false);
        }
        //web.getSettings().setSavePassword(false);
        String sendUrl=GetGoogleAccessToken.OAUTH_URL + "?redirect_uri=" + GetGoogleAccessToken.REDIRECT_URI + "&response_type=code&client_id=" + HomeFarm.getGoogleOAuthKey() + "&scope=" + GetGoogleAccessToken.OAUTH_SCOPE;

        web.loadUrl(sendUrl);
        //web.loadUrl(GetGoogleAccessToken.OAUTH_URL + "?client_id=" + HomeFarm.getGoogleOAuthKey() + "&scope=" + GetGoogleAccessToken.OAUTH_SCOPE);

        TextView txt=(TextView) auth_dialog.findViewById(R.id.account_oauth_text);
        B.addStyle(txt);
        if(forEmail!=null) {
            txt.setText(forEmail);
            txt.setVisibility(View.VISIBLE);
        } else {
            txt.setVisibility(View.GONE);
        }
        //webview.setWebViewClient(new HelloWebViewClient(Webview.this));
        //CookieSyncManager.createInstance(MySocialNetworks.this);


        //if(cookieManager==null) {
        try {
            CookieSyncManager.createInstance(web.getContext());
            android.webkit.CookieManager.getInstance().removeAllCookie();
        } catch(Exception e) {}

        //cookieManager.setAcceptCookie(true);

        //web.getSettings().setZ
        web.setWebViewClient(new WebViewClient() {
            boolean authComplete = false;
            Intent resultIntent = new Intent();
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);
            }
            String authCode;

            @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                //Log.e("ERROR",description);
                auth_dialog.dismiss();
                String html="";
                web.loadUrl("file:///android_asset/network_error.html");
                auth.setText(activity.getString(R.string.email_google_error));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    //BLog.e("URLRES",url);
                    authCode = uri.getQueryParameter("code");
                    //BLog.e("", "-CODE : " + authCode);
                    authComplete = true;



                    resultIntent.putExtra("code", authCode);
                    activity.setResult(Activity.RESULT_OK, resultIntent);
                    activity.setResult(Activity.RESULT_CANCELED, resultIntent);
                    //SharedPreferences.Editor edit = pref.edit();
                    useCode=authCode;

                    auth_dialog.dismiss();
                    new TokenGet().execute();
                    progress.setVisibility(View.VISIBLE);
                    auth.setText(activity.getString(R.string.email_google_registering));
                    //Toast.makeText(activity, "Authorization Code is: " + authCode, Toast.LENGTH_SHORT).show();
                }else if(url.contains("error=access_denied")){
                    //BLog.e("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                    activity.setResult(Activity.RESULT_CANCELED, resultIntent);
                    //Toast.makeText(activity, "Error Occured", Toast.LENGTH_SHORT).show();
                    auth_dialog.dismiss();
                    auth.setText(activity.getString(R.string.email_google_error));
                }
            }
        });
        auth_dialog.show();

        try {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(auth_dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;

            auth_dialog.getWindow().setAttributes(lp);
        } catch(Exception e) {

        }
        //auth_dialog.setTitle("Brief");
        auth_dialog.setCancelable(true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            if (resultCode == Activity.RESULT_OK) {
                //googleoverlay.setVisibility(View.VISIBLE);
                useEmail=data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        } else if(requestCode== OAuthHelper.MY_ACTIVITYS_AUTH_REQUEST_CODE) {

        }
        // Later, more code will go here to handle the result from some exceptions...
    }
    private void makeGoogleAccount(String mEmail, String profileName, String profilePic, String code, String idToken, String token, long expire, String refresh) {

        Account pickaccount = DefaultProperties.makeGmailOAuth();

        pickaccount.setInt(Account.INT_TYPE_, Account.TYPE_EMAIL);
        pickaccount.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_GOOGLEMAIL);
        pickaccount.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_GOOGLEMAIL);
        pickaccount.setString(Account.STRING_EMAIL_ADDRESS, mEmail);
        pickaccount.setString(Account.STRING_LOGIN_NAME, mEmail);

        pickaccount.setString(Account.STRING_OAUTH_CODE,code);
        pickaccount.setString(Account.STRING_OAUTH_TOKEN_ACCESS,token);
        pickaccount.setString(Account.STRING_OAUTH_TOKEN_ID,idToken);
        pickaccount.setLong(Account.LONG_OAUTH_TOKEN_EXPIRE, Cal.getUnixTime() + (expire*1000)- 20000);
        pickaccount.setString(Account.STRING_OAUTH_TOKEN_REFRESH,refresh);


        pickaccount.setInt(Account.INT_OAUTH_TOKEN_FAILS, 0);

        //pickaccount.setString(Account.STRING_ACCESS_TOKEN, token);

        List<String> addFolders = new ArrayList<String>();
        addFolders.add("[Gmail]/Sent Mail");
        addFolders.add("INBOX");
        pickaccount.setEmailFolders(addFolders);
        Account already=AccountsDb.getEmailAccount(mEmail);
        if(already!=null) {

            //OAuthHelper.removeAuthToken(activity,already);
            //AccountsDb.deleteAccount(activity,already);
            SyncData sd=SyncDataDb.getByAccountId(already.getLong(Account.LONG_ID));
            SyncDataDb.delete(sd);
            pickaccount.setLong(Account.LONG_ID, already.getLong(Account.LONG_ID));
            pickaccount.setInt(Account.INT_SYNC_PERIOD, already.getInt(Account.INT_SYNC_PERIOD));
            pickaccount.setString(Account.STRING_EMAIL_FOLDERS,already.getString(Account.STRING_EMAIL_FOLDERS));
            pickaccount.setString(Account.STRING_EMAIL_FOLDERS_OTHER,already.getString(Account.STRING_EMAIL_FOLDERS_OTHER));
            pickaccount.setInt(Account.INT_OAUTH_TOKEN_FAILS, 0);
            pickaccount.setInt(Account.INT_SIGNATURE_REKEY, 0);

            AccountsDb.updateAccount(pickaccount);

            SyncData data = SyncDataDb.getByAccountId(pickaccount.getLong(Account.LONG_ID));
            data.setActive(true);
            SyncDataDb.update(data);
            BriefService.checkEmailsFor(activity, pickaccount, true);
        } else {

            AccountsDb.addAccount(pickaccount);
            Account newacc = AccountsDb.getEmailAccount(mEmail);
            if(newacc!=null) {
                SyncData data = SyncDataDb.getByAccountId(newacc.getLong(Account.LONG_ID));
                data.setActive(true);
                SyncDataDb.update(data);
                BriefService.checkEmailsFor(activity, newacc, true);

            }
        }
        //Bgo.goPreviousFragment(activity);

    }
    private class TokenGet extends AsyncTask<String, String, JSONObject> {
        //private ProgressDialog pDialog;
        //String Code;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            /*
            BLog.e("PRE","pre-ex called");
            pDialog = new ProgressDialog(activity);
            pDialog.setMessage("Contacting Google ...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            Code = useCode;//pref.getString("Code", "");
            pDialog.show();
            */
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            JSONObject json=null;
            try {
                //BLog.e("PRE","do in background called");
                GetGoogleAccessToken jParser = new GetGoogleAccessToken();

                json = jParser.gettoken(GetGoogleAccessToken.TOKEN_URL,useCode,HomeFarm.getGoogleOAuthKey(),GetGoogleAccessToken.CLIENT_SECRET,GetGoogleAccessToken.REDIRECT_URI,GetGoogleAccessToken.GRANT_TYPE_AUTH);
                String tok = json.getString("access_token");
                long expire = json.getLong("expires_in");
                String refresh = json.getString("refresh_token");
                String idToken = json.getString("id_token");


                String data = makeRequest(tok);
                JSONObject userdata=new JSONObject(data);

                String email = userdata.getString("email");
                String pname = userdata.getString("name");
                String ppic = userdata.getString("picture");

                makeGoogleAccount(email,pname,ppic,useCode,idToken,tok,expire,refresh);

                //BLog.e("userdata", ""+data);
            } catch (JSONException e) {
                json=null;
                BLog.e("G-OAuth",""+e.getMessage());
            }
            //BLog.e("Token Access", new String(Base64.decode(idToken)));





            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {

            if (json != null){
                //pDialog.dismiss();

                    //BLog.e("Token Access", tok);
                    //BLog.e("Expire", expire);
                    //BLog.e("Refresh", refresh);
                    //auth.setText("Authenticated");
                Bgo.clearBackStack(activity);
                State.sectionsClearBackstack();
                Bgo.openFragment(activity, AccountsHomeFragment.class);
                    //Access.setText("Access Token:"+tok+"nExpires:"+expire+"nRefresh Token:"+refresh);

            }else{
                Toast.makeText(activity, "Network Error", Toast.LENGTH_SHORT).show();
                useCode=null;
                useEmail=null;
                //pDialog.dismiss();
            }
        }
    }


    public String makeRequest(String access_token) {
        String data="";
        try {

            URL url = new URL("https://www.googleapis.com/oauth2/v2/userinfo");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", "Bearer "+access_token);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + conn.getResponseCode());
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(
                    (conn.getInputStream())));

            String output;
            System.out.println("REST call made. Output from Server .... \n");
            while ((output = br.readLine()) != null) {
                data+=output+"\n";
            }

            conn.disconnect();

        } catch (MalformedURLException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();

        }
        return data;
    }

}
