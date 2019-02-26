package run.brief.beans;

import android.app.Activity;

import run.brief.R;
import run.brief.util.json.JSONObject;

public class SignatureBean extends BJSONBean {
	public static final String LONG_ID="id";
    public static final String INT_USE="use";
    public static final String INT_CHECKED="chk";
    public static final String LONG_ACCOUNT_ID="aid";
	public static final String STRING_SIGNATURE="sig";

    public SignatureBean() {
        super();
    }
    public SignatureBean(JSONObject job) {
        super(job);
    }
	
	public static String getDefaultSignature(Activity activity) {
		return activity.getString(R.string.email_signature)+"\n"+activity.getString(R.string.email_signature_url);
	}
    public static SignatureBean getDefaultSignature(Activity activity, Account account) {
        SignatureBean defsig=new SignatureBean();
        defsig.setLong(SignatureBean.LONG_ID,0);
        defsig.setString(SignatureBean.STRING_SIGNATURE,SignatureBean.getDefaultSignature(activity));
        defsig.setLong(SignatureBean.LONG_ACCOUNT_ID,account.getLong(Account.LONG_ID));
        return defsig;
    }
}
