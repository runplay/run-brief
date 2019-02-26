package run.brief.service;

import android.content.Context;

import java.util.ArrayList;

import run.brief.R;
import run.brief.beans.Note;
import run.brief.beans.SyncData;


public class SyncDataDb {
	
	private static final SyncDataDb SYNC = new SyncDataDb();
	
	private static final String DB_DEFAULT_ITEMS="syncdata";
	
	private int countNew;
	private ArrayList<SyncData>data;
	//private ArrayList<RssUserFeed> userdata;
	private SyncDataTable database;
	private boolean isLoaded=false;

    public static String WAITING_TEXT;
	//public AsyncTask<RssUserFeed, Void, Integer> doload;
	
	public SyncDataDb() {
		//Load();
	}
	public static void clearNewCount() {
		SYNC.countNew=0;
	}
	public static int getNewCount() {
		return SYNC.countNew;
	}
	public static void addNewCount(int countNew) {
		SYNC.countNew=SYNC.countNew+countNew;
	}
	
	public static final SyncDataTable getItemsDatabase() {
		return SYNC.database;
	}
	
	public synchronized static void init(Context context) {
        WAITING_TEXT=context.getResources().getString(R.string.settings_email_wait_sync);
		if(SYNC.database==null) {
			SYNC.database=new SyncDataTable(context);
			SYNC.data=SYNC.database.getItems();
			//SortNotes();
		}
	}
/*
	public static void SortNotes() {
		Collections.sort(NOTES.data, new Comparator<Note>() {
		    public int compare(Note m1, Note m2) {
		        return (new Date(m2.getLong(Note.LONG_DATE_CREATED)).compareTo(new Date(m1.getLong(Note.LONG_DATE_CREATED))));
		    }
		});
	}
	*/
	public final static ArrayList<SyncData> getAllItems() {
		return SYNC.data;
	}
	public final static int size() {
		return SYNC.data.size();

	}


	public static SyncData getByAccountId(String id) {
		//Note n=null;
		for(SyncData t: SYNC.data) {

			if(t.getString(SyncData.STRING_ACCOUNT_ID).equals(id))
				return t;
		}
        //BLog.e("CRE","NEW sync item");
        SyncData data = SyncData.getNewSyncItem(id);
        //add(data);
        for(SyncData t: SYNC.data) {
            if(t.getString(SyncData.STRING_ACCOUNT_ID).equals(id))
                return t;
        }
		return null;

	}
    public static SyncData getByAccountId(long longid) {
        //Note n=null;
        String id= Long.valueOf(longid).toString();
        for(SyncData t: SYNC.data) {
            if(t.getString(SyncData.STRING_ACCOUNT_ID).equals(id))
                return t;
        }
        SyncData data = SyncData.getNewSyncItem(id);
        //add(data);
        for(SyncData t: SYNC.data) {
            if(t.getString(SyncData.STRING_ACCOUNT_ID).equals(id))
                return t;
        }
        return null;

    }


	public synchronized static long add(SyncData item) {
		if(item!=null) {
			
			long id=SYNC.database.add(item);
			item.setLong(Note.INT_ID, id);
			
			SYNC.data=SYNC.database.getItems();
			return id;
		}
		return -1;
	}
	public synchronized static void update(SyncData item) {
		if(item!=null) {

			SYNC.database.update(item);
			SYNC.data=SYNC.database.getItems();
		}
		
	}
	public static boolean has(String id) {
		if(SYNC.database.hasItem(id))
			return true;
		return false;
		
	}
    public static void deleteAll() {
        SYNC.database.deleteAll();
        SYNC.data.clear();
    }
    public synchronized static void delete(SyncData item) {

        if(item!=null) {

            SYNC.database.delete(item);
            SYNC.data=SYNC.database.getItems();
        }

    }

}
