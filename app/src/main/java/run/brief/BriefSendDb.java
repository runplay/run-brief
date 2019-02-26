package run.brief;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import run.brief.beans.Account;
import run.brief.beans.BriefSend;
import run.brief.secure.Validator;
import run.brief.util.Db;
import run.brief.util.DbField;


public final class BriefSendDb {
	private static final BriefSendDb DB = new BriefSendDb();
	
	//private static final String DB_DEFAULT_ITEMS="notes";
	
	private int countNew;
	private ArrayList<BriefSend>data=new ArrayList<BriefSend>();
	//private ArrayList<RssUserFeed> userdata;
	private BriefSendDbTable database;
	//private boolean isLoaded=false;

	
	public BriefSendDb() {
		//Load();
	}
	public static void clearNewCount() {
		DB.countNew=0;
	}
	public static int getNewCount() {
		return DB.countNew;
	}
	public static void addNewCount(int countNew) {
		DB.countNew=DB.countNew+countNew;
	}
	
	public static final BriefSendDbTable getItemsDatabase() {
		return DB.database;
	}

	public synchronized static void init(Context context) {
		if(DB.database==null) {
			DB.database= DB.new BriefSendDbTable(context);
			DB.data=DB.database.getItems();
			//SortNotes();
		}
	}
    public synchronized static void refreshData() {
        if(DB.database!=null)
            DB.data=DB.database.getItems();
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
	public final static ArrayList<BriefSend> getAllItems() {
		return DB.data;
	}
	public final static ArrayList<BriefSend> getItems(int WITH_) {
		return DB.database.getItems(WITH_);
	}
	public final static int size() {
		return DB.data.size();

	}
	public static BriefSend get(int index) {
		if(DB.data!=null && DB.data.size()>index)
			return DB.data.get(index);
		else 
			return null;
	}

	public static BriefSend get(long id) {
		if(DB.data!=null) {
			for(BriefSend bs: DB.data) {
				if(id==bs.getLong(BriefSend.LONG_ID))
					return bs;
			}
		}
		return null;
	}
	
	public static void deleteAll() {
		DB.database.deleteAll();
		DB.data.clear();
	}
    public static void deleteFromAccount(run.brief.beans.Account account) {
        long accountId = account.getLong(Account.LONG_ID);
        for(BriefSend send: DB.data) {
            if(send.getLong(BriefSend.LONG_ACCOUNT_ID)==accountId) {
                DB.database.delete(send);
            }
        }
        refreshData();
    }
	public synchronized static boolean remove(BriefSend item) {
		if(item!=null) {
			DB.database.delete(item);
            DB.data=DB.database.getItems();
			return true;
		} else 
			return false;
		
	}
	public synchronized static long add(BriefSend item) {
		long id=0;
		if(Validator.isValidCaller() && item!=null) {
			
			id=DB.database.add(item);
			DB.data=DB.database.getItems();
		}
		return id;
	}
	public synchronized static void update(BriefSend item) {
		if(item!=null) {
			
			DB.database.update(item);
			DB.data=DB.database.getItems();
		}
		
	}
	public synchronized static void incrementAttempts(BriefSend item) {
		if(item!=null) {
			
			DB.database.incrementAttempts(item);
			DB.data=DB.database.getItems();
		}
	}

	public static boolean has(int id) {
		if(DB.database.hasItem(id))
			return true;
		return false;
		
	}

	

	public class BriefSendDbTable extends Db {
			
			public BriefSendDbTable(Context context) {
				
				super("briefsend", 
						new DbField[] {
							new DbField(BriefSend.LONG_ID,DbField.FIELD_TYPE_INT,true,false),
							new DbField(BriefSend.LONG_ACCOUNT_ID,DbField.FIELD_TYPE_INT,false,false),
							new DbField(BriefSend.INT_BRIEF_WITH,DbField.FIELD_TYPE_INT,false,false),
                            new DbField(BriefSend.INT_STATUS,DbField.FIELD_TYPE_INT,false,false),
							new DbField(BriefSend.INT_ATTEMPTS,DbField.FIELD_TYPE_INT,false,false),
							new DbField(BriefSend.STRING_BJSON_BEAN,DbField.FIELD_TYPE_TEXT,false,false)
						}
					,context
					);
				this.context=context;

			}
			private BriefSend getRatingFromCursor(Cursor cursor) {
				BriefSend item = new BriefSend();
				item.setLong(BriefSend.LONG_ID, cursor.getLong(cursor.getColumnIndex(BriefSend.LONG_ID)));
				item.setLong(BriefSend.LONG_ACCOUNT_ID, cursor.getLong(cursor.getColumnIndex(BriefSend.LONG_ACCOUNT_ID)));
				item.setString(BriefSend.STRING_BJSON_BEAN, cursor.getString(cursor.getColumnIndex(BriefSend.STRING_BJSON_BEAN)));
				item.setInt(BriefSend.INT_ATTEMPTS, cursor.getInt(cursor.getColumnIndex(BriefSend.INT_ATTEMPTS)));
				item.setInt(BriefSend.INT_BRIEF_WITH, cursor.getInt(cursor.getColumnIndex(BriefSend.INT_BRIEF_WITH)));
                item.setInt(BriefSend.INT_STATUS, cursor.getInt(cursor.getColumnIndex(BriefSend.INT_STATUS)));
		    	return item;
			}
			public ArrayList<BriefSend> getItems(int WITH_) {
				final ArrayList<BriefSend> items = getItems();
				ArrayList<BriefSend> got = new ArrayList<BriefSend>();
				for(BriefSend send: items) {
					if(send.getInt(BriefSend.INT_BRIEF_WITH)==WITH_)
						got.add(send);
				}
				
				return got;
			}
			public ArrayList<BriefSend> getItems() {
				final ArrayList<BriefSend> items = new ArrayList<BriefSend>();
				Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, null);
				
				if(cur.getCount()>0) {
					cur.moveToFirst();
					do {
						items.add(getRatingFromCursor(cur));
					} while(cur.moveToNext());
				
				}
				
				cur.close();
				
				return items;
			}
			public int update(BriefSend item) {
				ContentValues values = new ContentValues();
				values.put(BriefSend.LONG_ACCOUNT_ID, item.getLong(BriefSend.LONG_ACCOUNT_ID));
			    values.put(BriefSend.STRING_BJSON_BEAN, item.getString(BriefSend.STRING_BJSON_BEAN));
			    values.put(BriefSend.INT_ATTEMPTS, item.getInt(BriefSend.INT_ATTEMPTS));
			    values.put(BriefSend.INT_BRIEF_WITH, item.getInt(BriefSend.INT_BRIEF_WITH));
                values.put(BriefSend.INT_STATUS, item.getInt(BriefSend.INT_STATUS));
			    int id = db.update(TABLE_NAME, values, BriefSend.LONG_ID+"=?", new String[]{""+item.getLong(BriefSend.LONG_ID)});
			    return id;
			}
			public int incrementAttempts(BriefSend item) {
				ContentValues values = new ContentValues();
			    values.put(BriefSend.INT_ATTEMPTS, item.getInt(BriefSend.INT_ATTEMPTS)+1);
			    int id = db.update(TABLE_NAME, values, BriefSend.LONG_ID+"=?", new String[]{""+item.getLong(BriefSend.LONG_ID)});
			    return id;
			}	

			public boolean hasItem(long id) {
				Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
						BriefSend.LONG_ID+"=?", new String[]{id+""}, null, null, null);
				
				boolean alreadyHasFeed=false;
				
				if(cur!=null && cur.getCount()>0) 
					alreadyHasFeed=true;
				cur.close();
				return alreadyHasFeed;
			}
			
			public long add(BriefSend item) {
				
				if(item!=null) {
					ContentValues values = new ContentValues();
					//BLog.e("DBADD", "accid: "+item.getLong(BriefSend.LONG_ACCOUNT_ID));
					values.put(BriefSend.LONG_ACCOUNT_ID, item.getLong(BriefSend.LONG_ACCOUNT_ID));
				    values.put(BriefSend.STRING_BJSON_BEAN, item.getString(BriefSend.STRING_BJSON_BEAN));
				    values.put(BriefSend.INT_ATTEMPTS, item.getInt(BriefSend.INT_ATTEMPTS));
				    values.put(BriefSend.INT_BRIEF_WITH, item.getInt(BriefSend.INT_BRIEF_WITH));
                    values.put(BriefSend.INT_STATUS, item.getInt(BriefSend.INT_STATUS));
				    long id = db.insert(TABLE_NAME, null, values);
				    return id;
				}
				return -1;
			    
			}
			public void delete(BriefSend item) {
				open();
				db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+BriefSend.LONG_ID+" = "+item.getLong(BriefSend.LONG_ID));
				
			}
			public void deleteAll() {
				open();
				db.execSQL("DELETE FROM "+TABLE_NAME);  
				
			}
		}
	
	
}
