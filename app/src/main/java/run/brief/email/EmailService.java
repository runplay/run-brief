package run.brief.email;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;

import run.brief.HomeFarm;
import run.brief.beans.Account;
import run.brief.secure.Validator;
import run.brief.util.Cal;

public class EmailService {
	
	private static final EmailService EMS=new EmailService();
	
	private Map<Account,EmailServiceInstance> services = new HashMap<Account,EmailServiceInstance>();

    public static final String XBriefHeaderField="X-Brief-ID";
	//private EmailServiceInstance currentService;
	
	public EmailService() {

		
		//imap = new ImapService(AccountsDb.getAccount(0));
		//new CheckEmailService().execute("");
	}
    public static EmailService getService() {
        if(Validator.isValidCaller())
            return EMS;
        return
                null;
    }
	public static void init() {
		// anything to startup?
	}
    public static String generateNewXBriefId() {
        return "rxb-"+ HomeFarm.BaseEncode.encode(Cal.getUnixTime())+"-"+Cal.getUnixTime();
    }
    public static String getXBriefId(Message msg) {
        String[] idHeaders=null;
        try {
            idHeaders = msg.getHeader(EmailService.XBriefHeaderField);
            if(idHeaders!=null)
                return idHeaders[0];
        } catch(Exception e) {
            //BLog.e("E4", e.getMessage());
        }

        return "xb-err-"+Cal.getUnixTime();
    }
	//public static EmailServiceInstance getLastCalledService() {
	//	return EMS.currentService;
	//}
	public static boolean hasEmailService(Account account) {
		return EMS.services.containsKey(account);
	}
	public static synchronized EmailServiceInstance getService(Context context, Account account) {
		if(EMS.services.containsKey(account)) {
			//EMS.currentService=EMS.services.get(account);
            //BLog.e("EMS","Already has service");
			return EMS.services.get(account);
		} else {
            //BLog.e("EMS", "Not has, starting new service");
            EmailServiceInstance instance = new EmailServiceInstance(context,account);
			EMS.services.put(account, instance);
			//EMS.currentService=instance;
			return instance;
		}
	}
    public static synchronized void killService(Context context, Account account) {
        if(EMS.services.containsKey(account)) {
            EmailServiceInstance instance=EMS.services.get(account);
            instance.close();
            instance.disConnect();
            EMS.services.remove(account);
            //EMS.currentService=EMS.services.get(account);
            //return EMS.currentService;
        }
    }

}
