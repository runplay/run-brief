package run.brief.beans;

import android.content.Context;

import run.brief.contacts.ContactsDb;
import run.brief.service.BriefService;
import run.brief.settings.AccountsDb;
import run.brief.util.Cal;
import run.brief.util.json.JSONObject;

public class BriefSend extends BJSONBean {
	public static final String LONG_ID="id";
	public static final String LONG_ACCOUNT_ID="accid";
	public static final String INT_BRIEF_WITH="with";
	public static final String INT_ATTEMPTS="att";
	public static final String STRING_BJSON_BEAN="data";
    public static final String INT_STATUS="stat";

    public static final int STATUS_SEND=0;
    public static final int STATUS_CONFIRM=1;

    private boolean failedSend() {
        if(getInt(INT_ATTEMPTS)>= BriefService.MAX_MEDIUM_SEND_ATTEMPT_AT) {
            return true;
        }
        return false;
    }
	public Brief getAsBrief(Context context) {
		Brief b = null;//new Brief();
		JSONObject job = new JSONObject(getString(STRING_BJSON_BEAN));    
		switch(getInt(INT_BRIEF_WITH)) {
			case Brief.WITH_SMS:
				//BLog.e("BSEND", ""+job.toString());
				SmsSend sms = new SmsSend(job);
				b= new Brief();
				b.setMessage(sms.getString(SmsSend.STRING_MESSAGE));
				b.setWITH_(Brief.WITH_SMS);
				b.setTimestamp(Cal.getUnixTime());
				b.setAccountId(-1);
                if(failedSend())
				    b.setState(Brief.STATE_ERROR);
                else
                    b.setState(Brief.STATE_SENDNG);
				b.setTYPE_(Brief.TYPE_OUT);
				Person p = ContactsDb.getWithTelephone(context, sms.getString(SmsSend.STRING_TO_NUMBER));
				if(p!=null)
					b.setPersonId(p.getString(Person.STRING_PERSON_ID));
				BriefOut bout=new BriefOut(Brief.WITH_SMS,"",sms.getString(SmsSend.STRING_TO_NUMBER));
				b.addBriefOut(bout);
				break;
			case Brief.WITH_EMAIL:
				Email email = new Email(job);
				//BLog.e("HHHH", getLong(LONG_ACCOUNT_ID)+" -- "+job.toString());
				Account acc = AccountsDb.getAccountById(getLong(LONG_ACCOUNT_ID));
				if(acc!=null) {
					b=new Brief(context,acc,email,-1);

                    if(failedSend())
                        b.setState(Brief.STATE_ERROR);
                    else
                        b.setState(Brief.STATE_SENDNG);
					b.setTYPE_(Brief.TYPE_OUT);
				}

				break;
		}
		if(b!=null) {
			b.setDBid(Long.toString(getLong(LONG_ID)));
			b.setDBIndex(-1);

		}
		return b;
	}
}
