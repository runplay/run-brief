package run.brief.sms;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

import run.brief.BriefManager;
import run.brief.beans.Brief;
import run.brief.beans.SmsMsg;
import run.brief.secure.Validator;



public final class SmsDb {
	
	private static final SmsDb SMS = new SmsDb();
	
	private static final int START_FETCH_SIZE=30;
    private static final int MORE_FETCH_SIZE=40;

	private List<SmsMsg> messages=new ArrayList<SmsMsg>();

	//private Date lastRefresh;

    public static SmsDb getDb() {
        if(Validator.isValidCaller())
            return SMS;
        return
                null;
    }


	/*
	public static Date getLastRefresh() {
		if(SMS.lastRefresh==null)
			return new Date(Cal.getUnixTime()-600000);
		return SMS.lastRefresh;
	}
	*/
	public static int size() {
		return SMS.messages.size();
	}
	
	public static SmsMsg get(int index) {
		if(SMS.messages.size()>index)
			return SMS.messages.get(index);
		return null;
	}
	public static Brief getAsBrief(Context context,int index) {
		if(SMS.messages.size()>index) {
			Brief b=new Brief(context,SMS.messages.get(index),index);
			
			return b;
			
		}
		return null;
	}
	public static void deleteMessage(Context context,SmsMsg msg) {
		if(msg!=null && SmsFunctions.isDefaultSmsAppForDevice(context)) {
            if(msg.isMine())
			    context.getContentResolver().delete(Uri.parse(SmsFunctions.CONTENT_PROVIDER_SMS), "type=2 and _id=? and thread_id=?",new String[]{msg.getId(),msg.getThreadId()});
            else
                context.getContentResolver().delete(Uri.parse(SmsFunctions.CONTENT_PROVIDER_SMS), "type=1 and _id=? and thread_id=?",new String[]{msg.getId(),msg.getThreadId()});
            SmsDb.reload(context);
            BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
		}
	}
    public static void markMessageRead(Context context,SmsMsg msg,boolean isRead) {
    	String id=msg.getId();
    	for(SmsMsg m: SMS.messages) {
    		if(m.getId().equals(id)) {
				ContentValues values = new ContentValues();
				
    			if(isRead) {
    				values.put("read", 1);
    				m.setRead(1);
    			} else {
    				values.put("read", 0);
    				m.setRead(0);
    			}
				if(m.isMine()) {
					context.getContentResolver().update(Uri.parse(SmsFunctions.CONTENT_PROVIDER_SMS_SENT), values,"_id=?",new String[]{m.getId()});
				} else {
					context.getContentResolver().update(Uri.parse(SmsFunctions.CONTENT_PROVIDER_SMS), values,"_id=?",new String[]{m.getId()});
				}
    		}
    	}
    }
    public static SmsMsg getByById(String smsId) {
        for(SmsMsg msg: SMS.messages) {
            if(msg.getId().equals(smsId)) {
                return msg;
            }
        }
        return null;
    }
    public static boolean refreshInboxUpdate(Context context) {
        if(SMS.messages.isEmpty()) {
            SMS.messages=SmsFunctions.fetchSms(context, 0, START_FETCH_SIZE);
        } else {
            // check new only
            long last = SMS.messages.get(0).getMessageDate().getTimeInMillis();

            List<SmsMsg> tmp=SmsFunctions.fetchInboxSms(context,0,3);

            int addAt=0;
            for(SmsMsg sms: tmp) {
                //BLog.e("SMSADD","loaded: "+sms.getMessageContent()+" -- "+last+"--"+sms.getMessageDate().getTimeInMillis());
                if(getByById(sms.getId())==null) {
                    //if(last.getMessageDate().getTimeInMillis()<sms.getMessageDate().getTimeInMillis()) {
                    //BLog.e("SMSADD","adding: "+addAt+" - "+sms.getMessageContent());
                    SMS.messages.add(addAt++, sms);
                }
            }
        }
        //SMS.lastRefresh=new Date();
        return true;
    }
    public static void init(Context context) {
        if(SMS.messages.isEmpty()) {
            SMS.messages=SmsFunctions.fetchSms(context, 0, START_FETCH_SIZE);
        }
    }
    public static void reload(Context context) {
        //if(SMS.messages.isEmpty()) {
            SMS.messages=SmsFunctions.fetchSms(context, 0, START_FETCH_SIZE);
        //}
    }
	public static boolean refresh(Context context) {
		if(SMS.messages.isEmpty()) {
			init(context);
		} else {
			// check new only
            long last = SMS.messages.get(0).getMessageDate().getTimeInMillis();

			List<SmsMsg> tmp=SmsFunctions.fetchSms(context,0,3);

			int addAt=0;
			for(SmsMsg sms: tmp) {
                //BLog.e("SMSADD","loaded: "+sms.getMessageContent()+" -- "+last+"--"+sms.getMessageDate().getTimeInMillis());
                if(getByById(sms.getId())==null) {
				//if(last.getMessageDate().getTimeInMillis()<sms.getMessageDate().getTimeInMillis()) {
                    //BLog.e("SMSADD","adding: "+addAt+" - "+sms.getMessageContent());
                    SMS.messages.add(addAt++, sms);
                }
			}
		}
		//SMS.lastRefresh=new Date();
		return true;
	}
	public static boolean getMoreHistory(Context context) {

        List<SmsMsg> tmp=SmsFunctions.fetchSms(context,SMS.messages.size(),SMS.messages.size()+START_FETCH_SIZE);
        for(SmsMsg sms: tmp) {
            SMS.messages.add(sms);
        }

		//SMS.lastRefresh=new Date();
		return true;
	}
}

