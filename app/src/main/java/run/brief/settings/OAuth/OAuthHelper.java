package run.brief.settings.OAuth;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.io.IOException;
import java.util.Random;

import run.brief.b.Device;
import run.brief.beans.Account;
import run.brief.email.EmailServiceInstance;
import run.brief.secure.Validator;
import run.brief.settings.AccountsDb;
import run.brief.util.log.BLog;


/**
 * Created by coops on 06/01/15.
 */
public class OAuthHelper {

    private static final OAuthHelper OAUTH=new OAuthHelper();
    public static final int MY_ACTIVITYS_AUTH_REQUEST_CODE=10345;
    private static final String SCOPE = "oauth2:https://mail.google.com/";
    //private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/gmail.compose";
    //private static final String SCOPE = "oauth2:https://www.googleapis.com/auth/gmail.modify";
    //private static final String SCOPE = "oauth2:server:client_id:206617980051-drkpf58knaeo34a79scebrn9mj0mbmnq.apps.googleusercontent.com:api_scope:https://www.googleapis.com/auth/gmail.modify";
    //private static final String SCOPE = "oauth2:server:client_id:206617980051-drkpf58knaeo34a79scebrn9mj0mbmnq.apps.googleusercontent.com:api_scope:https://www.googleapis.com/auth/plus.login profile email";
    //private static final String SCOPE = "oauth2:server:client_id:206617980051-qqurc1rb47cmd46ktt5fb7qt9a3neql5.apps.googleusercontent.com:api_scope:https://mail.google.com/";
    //private static final String SCOPE = "oauth2: https://mail.google.com/";
            //"oauth2: https://www.googleapis.com/auth/plus.profile.emails.read " +
            //"https://www.googleapis.com/auth/userinfo.profile " +
            //"https://www.googleapis.com/auth/userinfo.email " +
            //"https://www.googleapis.com/auth/plus.login";
    /**
     * Attempts to retrieve the username.
     * If the account is not yet known, invoke the picker. Once the account is known,
     * start an instance of the AsyncTask to get the auth token and do work with it.
     */

    public static String fetchToken(Context context, EmailServiceInstance ems,Backoff backoff)  {
        if(!Validator.isValidCaller())
            return "";
        String mEmail = ems.getAccount().getString(Account.STRING_EMAIL_ADDRESS);
        BLog.e("EX", Device.getCONNECTION_TYPE()+","+Device.getCONNECTION_STATE()+"- fetch token for: " + mEmail);
        try {
            return GoogleAuthUtil.getToken(context, mEmail, SCOPE);
        } catch (UserRecoverableAuthException userRecoverableException) {
            // GooglePlayServices.apk is either old, disabled, or not present
            // so we need to show the user some UI in the activity to recover.
            //mActivity.handleException(userRecoverableException);
            Account account = AccountsDb.getEmailAccount(mEmail);
            if(account!=null) {
                account.setInt(Account.INT_SIGNATURE_REKEY,1);
                AccountsDb.updateAccount(account);
            }
            //BLog.e("EX", "1-" + userRecoverableException.getMessage());
        } catch (GoogleAuthException fatalException) {
            // Some other type of unrecoverable exception has occurred.
            // Report and log the error as appropriate for your app.
            BLog.e("EX", "2-" + fatalException.getMessage());
            Account account = AccountsDb.getEmailAccount(mEmail);
            if(account!=null) {
                account.setInt(Account.INT_SIGNATURE_REKEY,1);
                AccountsDb.updateAccount(account);
            }
        } catch(IOException e) {
            BLog.e("EX", Device.getCONNECTION_TYPE()+","+Device.getCONNECTION_STATE()+"- try backoff - " + e.getMessage());
            if (backoff.shouldRetry()) {
                backoff.backoff();
                return  fetchToken(context,ems,backoff);
            } else {
                return null;//response = new Response(-1, null, "No response from authorization server.");
            }

            //activity.doExponentialBackoff();

            // do not try mark, network error
            //SyncData.updateSyncInJustCompleted(ems.getAccount(), SyncData.SYNC_LAST_RESULT_FAIL, ems.getAccount().getString(Account.STRING_EMAIL_ADDRESS) + ", OAuth-d error: " + e.getMessage());
        }
        return null;
    }

    public static String getAndUseAuthTokenBlocking(Activity activity, String email) {
        //if(!Validator.isValidCaller())
        //    return "";
        try {
            // Retrieve a token for the given account and scope. It will always return either
            // a non-empty String or throw an exception.

            final String token = GoogleAuthUtil.getToken(activity, email, SCOPE);
            BLog.e("EX", "0-" + token);
            // Do work with token.
            return token;

        } catch (GooglePlayServicesAvailabilityException playEx) {
            Dialog alert = GooglePlayServicesUtil.getErrorDialog(
                    playEx.getConnectionStatusCode(),
                    activity,
                    MY_ACTIVITYS_AUTH_REQUEST_CODE);
            BLog.e("EX","1-"+ Device.getCONNECTION_TYPE()+","+Device.getCONNECTION_STATE()+"- try backoff - " + playEx.getMessage());
        } catch (UserRecoverableAuthException userAuthEx) {
            // Start the user recoverable action using the intent returned by
            // getIntent()
            BLog.e("EX", "2-" + userAuthEx.getMessage());
            activity.startActivityForResult(
                    userAuthEx.getIntent(),
                    MY_ACTIVITYS_AUTH_REQUEST_CODE);
            return "extra";

        } catch (IOException transientEx) {
            // network or server error, the call is expected to succeed if you try again later.
            // Don't attempt to call again immediately - the request is likely to
            // fail, you'll hit quotas or back-off.
            BLog.e("EX", "3-" + transientEx.getMessage());

        } catch (GoogleAuthException authEx) {
            // Failure. The call is not expected to ever succeed so it should not be
            // retried.
            BLog.e("EX", "4-" + authEx.getMessage());
            BLog.add("OUT",authEx);
        }
        return null;
    }
    public static class Backoff {

        private static final long INITIAL_WAIT = 1000 + new Random().nextInt(1000);
        private static final long MAX_BACKOFF = 100 * 1000;

        private long mWaitInterval = INITIAL_WAIT;
        private boolean mBackingOff = true;

        public boolean shouldRetry() {
            return mBackingOff;
        }

        private void noRetry() {
            mBackingOff = false;
        }

        public void backoff() {
            if (mWaitInterval > MAX_BACKOFF) {
                noRetry();
            } else if (mWaitInterval > 0) {
                try {
                    Thread.sleep(mWaitInterval);
                } catch (InterruptedException e) {
                    // life's a bitch, then you die, so said a man
                }
            }

            mWaitInterval = (mWaitInterval == 0) ? INITIAL_WAIT : mWaitInterval * 2;
        }
    }
}
