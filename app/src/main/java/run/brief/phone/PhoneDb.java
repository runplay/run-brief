package run.brief.phone;

import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import java.util.ArrayList;
import java.util.List;

import run.brief.beans.Brief;
import run.brief.beans.Phonecall;
import run.brief.secure.Validator;
import run.brief.util.Cal;
import run.brief.util.Sf;

public class PhoneDb {

	private static final PhoneDb P = new PhoneDb();
    private static final int START_FETCH_SIZE=30;
    private static final int MORE_FETCH_SIZE=60;
	
	private List<Phonecall> calls=new ArrayList<Phonecall>();


    public static PhoneDb getDb() {
        if(Validator.isValidCaller())
            return P;
        return
                null;
    }

	public static final List<Phonecall> getAllCalls() {
		return P.calls;
	}
	public static final List<Phonecall> getCalls(String number) {
		List<Phonecall> calls=new ArrayList<Phonecall>();
		for(Phonecall call: P.calls) {
			if(number.equals(call.getString(Phonecall.STRING_NUMBER)))
				calls.add(call);
		}
		return P.calls;
	}
	public static Phonecall get(int index) {
		if(index<P.calls.size() && index>=0)
			return P.calls.get(index);
		return null;		
		
	}
	public static Brief getAsBrief(Context context, int index) {
		if(index<P.calls.size() && index>=0)
			return new Brief(context,P.calls.get(index),index);
		return null;		
		
	}
	public static int size() {
		return P.calls.size();
	}

    public static void init(Context context) {
        if(P.calls.isEmpty()) {
            P.calls=fetchAddPhonecalls(context, 0, START_FETCH_SIZE);
        }
        //refresh(context);
    }
	public static void refresh(Context context) {
        if(P.calls.isEmpty()) {
            init(context);
        } else {
            List<Phonecall> tcalls=fetchAddPhonecalls(context, 0, 3);
            Phonecall last = P.calls.get(0);
            int addAt=0;
            for(Phonecall sms: tcalls) {
                if(Cal.getCal(last.getLong(Phonecall.LONG_DATE)).getTimeInMillis()<Cal.getCal(sms.getLong(Phonecall.LONG_DATE)).getTimeInMillis())
                    P.calls.add(addAt++, sms);
                else
                    break;
            }
        }
    }
    public static void getMoreHistory(Context context) {
        List<Phonecall> tcalls=fetchAddPhonecalls(context,P.calls.size(),P.calls.size()+MORE_FETCH_SIZE);
        for(Phonecall call: tcalls) {
            P.calls.add(call);
        }
    }

    private static List<Phonecall> fetchAddPhonecalls(Context context, int limitStart, int limitEnd) {
        List<Phonecall> tcalls=new ArrayList<Phonecall>();
        StringBuffer sb = new StringBuffer();
        Cursor managedCursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI, null,
                CallLog.Calls.NUMBER+"!=-1", null, "date" + " DESC LIMIT "+limitStart+","+limitEnd);
        
        int dbid = managedCursor.getColumnIndex(CallLog.Calls._ID);
        int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int name= managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
        int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);

        while (managedCursor.moveToNext()) {
        	Phonecall call=new Phonecall();
        	call.setString(Phonecall.STRING_NUMBER, Sf.notNull(managedCursor.getString(number)));
        	call.setString(Phonecall.STRING_NAME, Sf.notNull(managedCursor.getString(name)));
        	call.setLong(Phonecall.LONG_DATE, Sf.toLong(managedCursor.getString(date)));
        	call.setInt(Phonecall.INT_DURATION, Sf.toInt(managedCursor.getString(duration)));
        	call.setString(Phonecall.STRING_ID, Sf.notNull(managedCursor.getString(dbid)));
            //String dir = null;
            int dircode = Sf.toInt(managedCursor.getString(type));
            switch (dircode) {
	            case CallLog.Calls.OUTGOING_TYPE:
	                call.setInt(Phonecall.INT_TYPE, Phonecall.TYPE_OUT);
	                break;
	            case CallLog.Calls.INCOMING_TYPE:
	            	call.setInt(Phonecall.INT_TYPE, Phonecall.TYPE_IN);
	                break;
	            case CallLog.Calls.MISSED_TYPE:
	            	call.setInt(Phonecall.INT_TYPE, Phonecall.TYPE_MISSED);
	                break;
            }
            tcalls.add(call);
        }
        managedCursor.close();
        //P.calls=calls;
        return tcalls;
    }
}
