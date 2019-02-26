package run.brief;

import java.util.HashMap;
import java.util.Map;


import run.brief.beans.BriefBlock;
import run.brief.util.Db;
import run.brief.util.DbField;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;


public class BriefBlockDb {
	private static final BriefBlockDb DB = new BriefBlockDb();
	
	//private static final String DB_DEFAULT_ITEMS="notes";
	
	private int countNew;
	private Map<String,BriefBlock>data;
	//private ArrayList<RssUserFeed> userdata;
	private RatingDbTable database;
	//private boolean isLoaded=false;

	
	public BriefBlockDb() {
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
	
	public static final RatingDbTable getItemsDatabase() {
		return DB.database;
	}

	public synchronized static void init(Context context) {
		if(DB.database==null) {
			DB.database= DB.new RatingDbTable(context);
			DB.data=DB.database.getItems(0, 200);
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
	public final static Map<String,BriefBlock> getAllItems() {
		return DB.data;
	}
	public final static int size() {
		return DB.data.size();

	}
	public static BriefBlock get(int index) {
		if(DB.data!=null && DB.data.size()>index)
			return DB.data.get(index);
		else 
			return null;
	}

	public static BriefBlock get(String ratingIdentifier) {
		if(DB.data!=null)
			return DB.data.get(ratingIdentifier);
		else 
			return null;
	}
	
	public static void deleteAll() {
		DB.database.deleteAll();
		DB.data.clear();
	}
	public synchronized static boolean remove(BriefBlock item) {
		if(item!=null) {
			DB.data.remove(item);
			DB.database.delete(item);
			return true;
		} else 
			return false;
		
	}
	public synchronized static void add(BriefBlock item) {
		if(item!=null) {
			
			DB.database.add(item);
			DB.data=DB.database.getItems(0, 200);
		}
		
	}
	public synchronized static void update(BriefBlock item) {
		if(item!=null) {
			
			DB.database.update(item);
			DB.data=DB.database.getItems(0, 200);
		}
		
	}
	public static boolean has(int id) {
		if(DB.database.hasItem(id))
			return true;
		return false;
		
	}

	

	public class RatingDbTable extends Db {
			
			public RatingDbTable(Context context) {
				
				super("blocks", 
						new DbField[] {
							new DbField(BriefBlock.STRING_PERSON_ID,DbField.FIELD_TYPE_TEXT,true,false),
							new DbField(BriefBlock.STRING_BLOCK_EMAIL_PHONE,DbField.FIELD_TYPE_TEXT,false,true)
						}
					,context
					);
				this.context=context;

			}
			public Map<String,BriefBlock> getItems(int limitStart, int limitEnd) {
				final Map<String,BriefBlock> items = new HashMap<String,BriefBlock>();
				Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, BriefBlock.STRING_PERSON_ID+" DESC LIMIT "+limitStart+","+limitEnd);
				
				if(cur.getCount()>0) {
					cur.moveToFirst();
					do {
						BriefBlock rating=getRatingFromCursor(cur);
						items.put(rating.getString(BriefBlock.STRING_PERSON_ID),rating);
					} while(cur.moveToNext());
				
				}
				
				cur.close();
				
				return items;
			}
			public int update(BriefBlock item) {
				ContentValues values = new ContentValues();
			    values.put(BriefBlock.STRING_BLOCK_EMAIL_PHONE, item.getString(BriefBlock.STRING_BLOCK_EMAIL_PHONE));
				    
			    int id = db.update(TABLE_NAME, values, BriefBlock.STRING_PERSON_ID+"=?", new String[]{item.getString(BriefBlock.STRING_BLOCK_EMAIL_PHONE)});
			    return id;
			}
					
			private BriefBlock getRatingFromCursor(Cursor cursor) {
				BriefBlock item = new BriefBlock();
				item.setString(BriefBlock.STRING_PERSON_ID, cursor.getString(cursor.getColumnIndex(BriefBlock.STRING_PERSON_ID)));
				item.setString(BriefBlock.STRING_BLOCK_EMAIL_PHONE, cursor.getString(cursor.getColumnIndex(BriefBlock.STRING_BLOCK_EMAIL_PHONE)));
				
		    	return item;
			}
			
			public boolean hasItem(int noteid) {
				Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
						BriefBlock.STRING_PERSON_ID+"=?", new String[]{noteid+""}, null, null, null);
				
				boolean alreadyHasFeed=false;
				
				if(cur!=null && cur.getCount()>0) 
					alreadyHasFeed=true;
				cur.close();
				return alreadyHasFeed;
			}
			
			public long add(BriefBlock item) {
				
				if(item!=null) {
					ContentValues values = new ContentValues();
				    //values.put(Note.INT_ID, item.getInt(Note.INT_ID));
				    values.put(BriefBlock.STRING_PERSON_ID, item.getString(BriefBlock.STRING_PERSON_ID));
				    values.put(BriefBlock.STRING_BLOCK_EMAIL_PHONE, item.getString(BriefBlock.STRING_BLOCK_EMAIL_PHONE));

				    long id = db.insert(TABLE_NAME, null, values);
				    return id;
				}
				return -1;
			    
			}
			public void delete(BriefBlock item) {
				open();
				db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+BriefBlock.STRING_PERSON_ID+" = "+item.getString(BriefBlock.STRING_PERSON_ID));
				
			}
			public void deleteAll() {
				open();
				db.execSQL("DELETE FROM "+TABLE_NAME);  
				
			}
		}
	
	
}
