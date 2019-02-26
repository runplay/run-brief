package run.brief.beans;

import java.util.Date;

import run.brief.service.SyncDataDb;
import run.brief.util.Cal;

public class SyncData extends BJSONBean {

	public static final String STRING_ACCOUNT_ID="accid";
	public static final String LONG_IN_DATE ="date";
    public static final String LONG_OUT_DATE ="dateout";
	public static final String INT_TYPE_SYNC="type";
	public static final String INT_SYNC_IN_LAST_RESULT_CODE ="res";
	public static final String STRING_MESSAGE_IN="msg";
    public static final String INT_SYNC_OUT_LAST_RESULT_CODE ="resOut";
    public static final String STRING_MESSAGE_OUT="msgOut";
    public static final String INT_IS_ACTIVE ="active";

    //private static final String INT_IS_WORKING ="working";

    public static final int SYNC_LAST_RESULT_NONE =0;
	public static final int SYNC_LAST_RESULT_SUCCESS =1;
	public static final int SYNC_LAST_RESULT_FAIL =2;
	
	public static final int TYPE_FAST =2;
	public static final int TYPE_MEDIUM =1;
	public static final int TYPE_SLOW =0;
	
	public static final long MILLIS_FAST =600000;   // 10 min
	public static final long MILLIS_MEDIUM =3600000; // 1 hour
	public static final long MILLIS_SLOW =3600000*4; // 4 hour

    //public boolean isActiveWorking() {
    //    return getInt(INT_IS_WORKING)==0?false:true;
    //}
    //public void setActiveWorking(boolean active) {
    //    setInt(INT_IS_WORKING,active?1:0);
    //}
    public boolean isActive() {
        return getInt(INT_IS_ACTIVE)==0?false:true;
    }
    public void setActive(boolean active) {
        setInt(INT_IS_ACTIVE,active?1:0);
    }

    public static SyncData getNewSyncItem(String accountId) {

        SyncData sync = new SyncData();
        sync.setString(SyncData.STRING_ACCOUNT_ID, accountId);
        sync.setLong(SyncData.LONG_IN_DATE, (new Cal()).getTimeInMillis()-SyncData.MILLIS_SLOW-200);
        sync.setInt(SyncData.INT_TYPE_SYNC, SyncData.TYPE_MEDIUM);
        sync.setInt(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE,SyncData.SYNC_LAST_RESULT_NONE);
        sync.setInt(SyncData.INT_SYNC_IN_LAST_RESULT_CODE,SyncData.SYNC_LAST_RESULT_NONE);
        sync.setString(SyncData.STRING_MESSAGE_IN,SyncDataDb.WAITING_TEXT);
        sync.setString(SyncData.STRING_MESSAGE_OUT,SyncDataDb.WAITING_TEXT);
        SyncDataDb.add(sync);
        return sync;
    }

    public String getFriendlyNextSync() {
        Date nowTime = new Date();
        long refreshTime = MILLIS_SLOW;
        Date lastsync = new Date(getLong(LONG_IN_DATE));
        switch(getInt(INT_TYPE_SYNC)) {
            case TYPE_FAST:
                refreshTime=MILLIS_FAST;
                break;
            case TYPE_MEDIUM:
                refreshTime=MILLIS_MEDIUM;
                break;
            default:
                refreshTime=MILLIS_SLOW;
                break;
        }
        return Cal.friendlyComebackDate(lastsync,nowTime,refreshTime);
    }
	public boolean shouldSyncData(Date nowTime) {
		boolean shouldsync = false;

		if(isActive() && nowTime!=null) {
            long useMillis=0;
			long lastsync = getLong(LONG_IN_DATE);
			switch(getInt(INT_TYPE_SYNC)) {
				case TYPE_FAST:
					if(nowTime.getTime()>lastsync+MILLIS_FAST)
						shouldsync=true;
                        useMillis=MILLIS_FAST;
					break;
				case TYPE_MEDIUM:
					if(nowTime.getTime()>lastsync+MILLIS_MEDIUM)
						shouldsync=true;
                        useMillis=MILLIS_MEDIUM;
					break;
                default:
                    if(nowTime.getTime()>lastsync+MILLIS_SLOW)
                        shouldsync=true;
                        useMillis=MILLIS_SLOW;
                    break;
			}
                //BLog.e("SYNCNOW", getInt(INT_TYPE_SYNC)+" -S: " + nowTime.getTime()+ " -- " +((lastsync+useMillis)-nowTime.getTime())+ " -- " + lastsync + " -- " + MILLIS_SLOW);
		}

		return shouldsync;
	}
	public static void updateSyncInJustCompleted( Account account, int SYNC_LAST_RESULT_, String message) {
		if(account!=null) {
            //BLog.e("SYNCUPOD","In accointid: "+account.getLong(Account.LONG_ID));
			SyncData sync = SyncDataDb.getByAccountId( account.getLong(Account.LONG_ID));

			if(sync!=null) {
				sync.setLong(SyncData.LONG_IN_DATE, (new Date()).getTime());
				if(message==null)
					message="ERROR: SYN1012";
				sync.setString(SyncData.STRING_MESSAGE_IN, message);
				sync.setInt(SyncData.INT_SYNC_IN_LAST_RESULT_CODE, SYNC_LAST_RESULT_);
				
				SyncDataDb.update(sync);
			}
			
		} else {
            //BLog.e("SYNCUPOD","accointid is null");
        }
	}
    public static void updateSyncOutJustCompleted(Account account, int SYNC_LAST_RESULT_, String message) {

        if(account!=null) {
            //BLog.e("SYNCUPOD","Out accointid: "+account.getLong(Account.LONG_ID));
            SyncData sync = SyncDataDb.getByAccountId( account.getLong(Account.LONG_ID));

            if(sync!=null) {
                sync.setLong(SyncData.LONG_OUT_DATE, (new Date()).getTime());
                if(message==null)
                    message="ERROR: SYN1013";
                sync.setString(SyncData.STRING_MESSAGE_OUT, message);
                sync.setInt(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE, SYNC_LAST_RESULT_);

                SyncDataDb.update(sync);
            }

        } else {
            //BLog.e("SYNCUPOD","Out accointid is null");
        }
    }
}
