package run.brief.sms;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import run.brief.R;
import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Person;
import run.brief.beans.SmsMsg;
import run.brief.contacts.ContactsDb;
import run.brief.secure.Validator;
import run.brief.service.SmsReceiver;
import run.brief.settings.SettingsHomeTabbedFragment;
import run.brief.util.Cal;
import run.brief.util.Sf;


public final class SmsFunctions {
	
	public static final int TYPE_OUTGOING_MESSAGE = 1;
    public static final int TYPE_INCOMING_MESSAGE = 2;
    
    private static final int LIMIT_TO=30;
    public static final String CONTENT_PROVIDER_SMS_SENT="content://sms/sent";
    public static final String CONTENT_PROVIDER_SMS ="content://sms";
    private static String lastSentMessage="";
    //private static final long FETCH_TIME_PERIOD= 86400000*1; // 24 hours * days


    public static boolean SendSmsMessage(Context context, String phoneNumber, String message)  {
    	boolean sent=false;
        if(Validator.isValidCaller()) {
            try {
                SmsManager sms = SmsManager.getDefault();
                ArrayList<String> smsMessageText = sms.divideMessage(message);
                PendingIntent sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SmsReceiver.ACTION_SMS_SENT), 0);
                PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(SmsReceiver.ACTION_SMS_DELIVER), 0);
                //BLog.e("SMS", "SENT IS: "+sent);
                sms.sendTextMessage(phoneNumber, null, message, sentPI, deliveredPI);
                sent = true;
            } catch (Exception e) {
                //BLog.e("NOSMS", ""+e.getMessage());
            }

            if (sent) {

                Uri resultUri = addToSmsSent(context, phoneNumber, message);

            }
        }
    	return sent;
    }

    public static View getSmsNotDefaultView(final Activity activity) {
        View notDefaultView = activity.getLayoutInflater().inflate(R.layout.sms_not_default,null);
        TextView txt1=(TextView) notDefaultView.findViewById(R.id.sms_not_default_text);
        TextView txt2=(TextView) notDefaultView.findViewById(R.id.sms_not_default_desc);
        TextView btn=(TextView) notDefaultView.findViewById(R.id.sms_not_default_btn);
        B.addStyle(txt1);
        B.addStyle(txt2,B.FONT_SMALL);
        B.addStyle(btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                State.addToState(State.SECTION_SETTINGS, new StateObject(StateObject.STRING_VALUE, activity.getResources().getString(R.string.label_home)));
                Bgo.openFragmentBackStack(activity, SettingsHomeTabbedFragment.class);

            }
        });
        return notDefaultView;
    }
	private static Uri addToSmsSent(Context context, String phoneNumber, String message) {
		ContentValues values = new ContentValues();
		values.put("address", phoneNumber);
		values.put("body", message);
		values.put("date", Cal.getCal().getTimeInMillis());
		values.put("read", 1);

        Person p = ContactsDb.getWithTelephone(context, phoneNumber);
        if(p!=null) {
            values.put("Person", p.getLong(Person.LONG_ID));
        }

		return context.getContentResolver().insert(Uri.parse(CONTENT_PROVIDER_SMS_SENT), values);
	}

    public static boolean canOperateAsDefaultSms() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return true;
        }
        return false;
    }
    public static boolean isDefaultSmsAppForDevice(Context context) {
        String mDefaultSmsApp;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mDefaultSmsApp = Telephony.Sms.getDefaultSmsPackage(context);

            if (context.getPackageName().equals(mDefaultSmsApp)) {
                return true;
            }
        }
        return false;
    }
	public static Uri addToSmsReceived(Context context,SmsMsg message) {
		ContentValues values = new ContentValues();
		
		
		values.put("address", message.getMessageNumber());
		values.put("body", message.getMessageContent());
		values.put("date", Cal.getCal().getTimeInMillis());
		values.put("type", 1);
		values.put("read", 0);
		values.put("service_center", message.getServiceCenter());
		values.put("status", 1);
		values.put("reply_path_present", message.getReplyPathPresent());
		values.put("Person", message.getPerson());
		values.put("locked", message.getLocked());
		values.put("protocol", message.getProtocol());
		values.put("subject", message.getSubject());
        values.put("thread_id",message.getThreadId());

		return context.getContentResolver().insert(Uri.parse(CONTENT_PROVIDER_SMS), values);
	}
    public static SmsMsg fetchSms(Context context, Uri uri) {
        SmsMsg message=null;

        Cursor cursorsent = context.getContentResolver().query(uri,new String[] { "_id", "address", "date", "body","status","type", "read","thread_id" }, null, null,null);

        if (cursorsent != null) {

            //cursor.moveToLast();
            if (cursorsent.moveToFirst()) {

                //do {
                    String date =  cursorsent.getString(cursorsent.getColumnIndex("date"));
                    long timestamp = Sf.toLong(date);
                    Cal cal = new Cal(timestamp);
                    //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                    message = new SmsMsg();
                    //message.setMessageNumber(Device.FixPhoneNumber(context, cursorsent.getString(cursorsent.getColumnIndex("address"))));
                    message.setMessageNumber(cursorsent.getString(cursorsent.getColumnIndex("address")));
                    message.setMessageContent(cursorsent.getString(cursorsent.getColumnIndex("body")));
                    message.setType(cursorsent.getInt(cursorsent.getColumnIndex("type")));
                    message.setId(cursorsent.getString(cursorsent.getColumnIndex("_id")));
                    message.setRead(cursorsent.getInt(cursorsent.getColumnIndex("read")));
                    message.setStatus(cursorsent.getInt(cursorsent.getColumnIndex("status")));
                    message.setMessageDate(cal);
                    message.setMine(true);

            }
            cursorsent.close();
        }

        return message;

    }
    public static List<SmsMsg> fetchSms(Context context, int limitStart, int limitEnd) {
        List<SmsMsg> messages = new ArrayList<SmsMsg>();

        HashMap<Long,SmsMsg> hashMessages = new HashMap<Long,SmsMsg>();
        SortedSet<Long> sortednews=null;
        //Date now = new Date();

        List<SmsMsg> usemessages = new ArrayList<SmsMsg>();
        usemessages.addAll(fetchInboxSms(context,limitStart,limitEnd));
        usemessages.addAll(fetchSentSms(context,limitStart,limitEnd));
        //BLog.e("SMSLOAD","sms sent loaded: "+sentMsgs.size());
        //startTime= Cal.getUnixTime();
        for(SmsMsg msg: usemessages) {
            //if(msg.getMessageNumber().endsWith("699")) {
                //BLog.e("thid: "+msg.getMessageNumber()+"----t:"+msg.getMessageDate().getTimeInMillis()+"=id:"+msg.getId()+"--"+msg.getThreadId());
            //}
            hashMessages.put(msg.getMessageDate().getTimeInMillis(),msg);
        }


        sortednews= new TreeSet<Long>(hashMessages.keySet()).descendingSet();
        Iterator<Long> it = sortednews.iterator();
        int count=0;
        while(it.hasNext()) {
            messages.add(hashMessages.get(it.next()));
        }

        return messages;

    }
    public static List<SmsMsg> fetchSentSms(Context context, int limitStart, int limitEnd) {
        ArrayList<SmsMsg> smsMessages = new ArrayList<SmsMsg>();

        Uri uriSmsSent = Uri.parse(CONTENT_PROVIDER_SMS_SENT);

        Cursor cursorsent = context.getContentResolver()
                .query(uriSmsSent,
                        new String[] { "_id", "address", "date", "body","status",
                                "type", "read","thread_id" }, null, null,
                        "_id" + " DESC LIMIT "+limitStart+","+limitEnd);
        if (cursorsent != null) {
            cursorsent.moveToFirst();
            //cursor.moveToLast();
            if (cursorsent.getCount() > 0) {

                do {
                    String date =  cursorsent.getString(cursorsent.getColumnIndex("date"));
                    long timestamp = Sf.toLong(date);
                    Cal cal = new Cal(timestamp);
                    //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                    SmsMsg message = new SmsMsg();
                    //message.setMessageNumber(Device.FixPhoneNumber(context, cursorsent.getString(cursorsent.getColumnIndex("address"))));
                    //BLog.e("LLL",cursorsent.getString(cursorsent.getColumnIndex("address")));
                    message.setMessageNumber(cursorsent.getString(cursorsent.getColumnIndex("address")));
                    message.setMessageContent(cursorsent.getString(cursorsent.getColumnIndex("body")));
                    message.setType(cursorsent.getInt(cursorsent.getColumnIndex("type")));
                    message.setId(cursorsent.getString(cursorsent.getColumnIndex("_id")));
                    message.setRead(cursorsent.getInt(cursorsent.getColumnIndex("read")));
                    message.setStatus(cursorsent.getInt(cursorsent.getColumnIndex("status")));
                    message.setMessageDate(cal);
                    message.setMine(true);
                    message.setThreadId(cursorsent.getString(cursorsent.getColumnIndex("thread_id")));

                    smsMessages.add(message);
                    //sentMsgs.put(timestamp,message);

                    //BLog.e("SMSLOAD","sent first loaded: "+message.getMessageContent());

                } while (cursorsent.moveToNext());
            }
            cursorsent.close();
        }

        return smsMessages;

    }
    public static List<SmsMsg> fetchInboxSms(Context context, int limitStart, int limitEnd) {
        List<SmsMsg> smsInbox = new ArrayList<SmsMsg>();

        
        Uri uriSms = Uri.parse(CONTENT_PROVIDER_SMS);


        Cursor cursor = context.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "address", "date", "body","status",
                                "type", "read","thread_id" }, null, null,
                        "_id" + " DESC LIMIT "+limitStart+","+limitEnd);
        if (cursor != null) {
        	cursor.moveToFirst();
            if (cursor.getCount() > 0) {

                do {
                    String date =  cursor.getString(cursor.getColumnIndex("date"));
                    long timestamp = Sf.toLong(date);
                    Cal cal = new Cal(timestamp);
                    //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                    SmsMsg message = new SmsMsg();
                    //message.setMessageNumber(Device.FixPhoneNumber(context, cursor.getString(cursor.getColumnIndex("address"))));
                    message.setMessageNumber(cursor.getString(cursor.getColumnIndex("address")));
                    message.setMessageContent(cursor.getString(cursor.getColumnIndex("body")));
                    message.setType(cursor.getInt(cursor.getColumnIndex("type")));
                    message.setId(cursor.getString(cursor.getColumnIndex("_id")));
                    //message.setThreadId(cursor.getString(cursor.getColumnIndex("thread_id")));
                    message.setRead(cursor.getInt(cursor.getColumnIndex("read")));
                    message.setMessageDate(cal);
                    //message.setServiceCenter(cursor.getString(cursor.getColumnIndex("service_center")));
                    message.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                    message.setMine(false);
                    message.setThreadId(cursor.getString(cursor.getColumnIndex("thread_id")));
                    smsInbox.add(message);
                    //message.setReplyPathPresent(cursor.getString(cursor.getColumnIndex("reply_path_present")));
                    //message.setProtocol(cursor.getInt(cursor.getColumnIndex("protocol")));
                    //message.setPerson(cursor.getString(cursor.getColumnIndex("person")));
                    //message.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
                    //message.setLocked(cursor.getInt(cursor.getColumnIndex("locked")));
                    //smsMessages.put(timestamp,message);
                    //BLog.e("SMSLOAD","sent first loaded: "+message.getMessageContent());

                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        return smsInbox;

    }
    public static ArrayList<SmsMsg> fetchSmsForNumber(Context context, final String phonenumber,  int limitStart, int limitEnd) {
        //String orNumber = Device.FixPhoneNumber(context,phone);
        //BLog.e("FORNUM",""+phone+"---"+orNumber);

        String phone=Device.FixPhoneNumber(context,phonenumber);
        ArrayList<SmsMsg> smsInbox = new ArrayList<SmsMsg>();

        HashMap<Long,SmsMsg> sentMsgs = new HashMap<Long,SmsMsg>();
        SortedSet<Long> sortednews=null;
        Date now = new Date();

        Uri uriSms = Uri.parse(CONTENT_PROVIDER_SMS);


        String where = null;
        List<String> avals = new ArrayList<String>();
        if(phone.startsWith("+")) {
            avals.add(phone);
            avals.add(phone.replaceAll(" ",""));
            where="address = ? or address=?";
        } else {
            avals.add(phone);
            avals.add(phone.replaceAll(" ",""));

            String inter=Device.phoneNumgerAsInternationl(context,phone);
            avals.add(inter);
            avals.add(inter.replaceAll(" ",""));


            where="address = ? or address=? or address = ? or address=?";
        }

        String[] vals = new String[avals.size()];
        for(int i=0; i<avals.size(); i++) {
            vals[i]=avals.get(i);
        }

        Cursor cursor = context.getContentResolver()
                .query(uriSms,
                        new String[] { "_id", "address", "date", "body","status",
                                "type", "read","thread_id" }, where, vals,
                        "_id" + " DESC LIMIT "+limitStart+","+limitEnd);
        if (cursor != null) {
            cursor.moveToFirst();
            if (cursor.getCount() > 0) {

                do {
                    String date =  cursor.getString(cursor.getColumnIndex("date"));
                    long timestamp = Sf.toLong(date);
                    Cal cal = new Cal(timestamp);
                    //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                    SmsMsg message = new SmsMsg();
                    message.setMessageNumber(Device.FixPhoneNumber(context, cursor.getString(cursor.getColumnIndex("address"))));
                    message.setMessageContent(cursor.getString(cursor.getColumnIndex("body")));
                    message.setType(cursor.getInt(cursor.getColumnIndex("type")));
                    message.setId(cursor.getString(cursor.getColumnIndex("_id")));
                    //message.setThreadId(cursor.getString(cursor.getColumnIndex("thread_id")));
                    message.setRead(cursor.getInt(cursor.getColumnIndex("read")));
                    message.setMessageDate(cal);
                    //message.setServiceCenter(cursor.getString(cursor.getColumnIndex("service_center")));
                    message.setStatus(cursor.getInt(cursor.getColumnIndex("status")));
                    message.setThreadId(cursor.getString(cursor.getColumnIndex("thread_id")));

                    //message.setReplyPathPresent(cursor.getString(cursor.getColumnIndex("reply_path_present")));
                    //message.setProtocol(cursor.getInt(cursor.getColumnIndex("protocol")));
                    //message.setPerson(cursor.getString(cursor.getColumnIndex("person")));
                    //message.setSubject(cursor.getString(cursor.getColumnIndex("subject")));
                    //message.setLocked(cursor.getInt(cursor.getColumnIndex("locked")));
                    sentMsgs.put(timestamp,message);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        Uri uriSmsSent = Uri.parse(CONTENT_PROVIDER_SMS_SENT);

        Cursor cursorsent = context.getContentResolver()
                .query(uriSmsSent,
                        new String[] { "_id", "address", "date", "body","status",
                                "type", "read","thread_id" }, where, vals,
                        "_id" + " DESC LIMIT "+limitStart+","+limitEnd);
        if (cursorsent != null) {
            cursorsent.moveToFirst();
            //cursor.moveToLast();
            if (cursorsent.getCount() > 0) {

                do {
                    String date =  cursorsent.getString(cursorsent.getColumnIndex("date"));
                    Long timestamp = Long.parseLong(date);
                    Cal cal = new Cal(timestamp);
                    //DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");
                    SmsMsg message = new SmsMsg();
                    message.setMessageNumber(Device.FixPhoneNumber(context, cursorsent.getString(cursorsent.getColumnIndex("address"))));
                    message.setMessageContent(cursorsent.getString(cursorsent.getColumnIndex("body")));
                    message.setType(cursorsent.getInt(cursorsent.getColumnIndex("type")));
                    message.setId(cursorsent.getString(cursorsent.getColumnIndex("_id")));
                    message.setRead(cursorsent.getInt(cursorsent.getColumnIndex("read")));
                    message.setStatus(cursorsent.getInt(cursorsent.getColumnIndex("status")));
                    message.setThreadId(cursorsent.getString(cursorsent.getColumnIndex("thread_id")));
                    message.setMessageDate(cal);
                    message.setMine(true);
                    sentMsgs.put(timestamp,message);

                } while (cursorsent.moveToNext());
            }
            cursorsent.close();
        }


        sortednews= new TreeSet<Long>(sentMsgs.keySet()).descendingSet();
        Iterator<Long> it = sortednews.iterator();
        int count=0;
        while(it.hasNext()) {
            smsInbox.add(sentMsgs.get(it.next()));
        }


        return smsInbox;

    }
}
