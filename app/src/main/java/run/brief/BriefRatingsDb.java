package run.brief;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

import run.brief.beans.BriefRating;
import run.brief.util.Db;
import run.brief.util.DbField;

public class BriefRatingsDb {
	private static final BriefRatingsDb DB = new BriefRatingsDb();
	
	private static final String DB_DEFAULT_ITEMS="notes";
	
	private int countNew;
	private Map<String,BriefRating>data;
	//private ArrayList<RssUserFeed> userdata;
	private RatingDbTable database;
	private boolean isLoaded=false;

	
	public BriefRatingsDb() {
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
			refresh(context);
			//SortNotes();
		}
	}
	public synchronized static void refresh(Context context) {
		DB.database= DB.new RatingDbTable(context);
		DB.data=DB.database.getItems(0, 200);
	}
    public static synchronized void deleteRatingsWith(int WITH_,long accountId) {
        DB.database.delete(WITH_,accountId);
        DB.data=DB.database.getItems(0, 200);
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
	public final static Map<String,BriefRating> getAllItems() {
		return DB.data;
	}
	public final static int size() {
		return DB.data.size();

	}
	public static BriefRating get(int index) {
		if(DB.data!=null && DB.data.size()>index)
			return DB.data.get(index);
		else 
			return null;
	}

	public static BriefRating get(String ratingIdentifier) {
		if(DB.data!=null)
			return DB.data.get(ratingIdentifier);
		else 
			return null;
	}
	
	public static void deleteAll() {
		DB.database.deleteAll();
		DB.data.clear();
	}
	public synchronized static boolean remove(String ratingIdentifier) {
		if(ratingIdentifier!=null) {
			
			BriefRating br=get(ratingIdentifier);
			if(br!=null) {
				return remove(br);
			} else {
				return false;
			}
		} else 
			return false;
		
	}
	public synchronized static boolean remove(BriefRating item) {
		if(item!=null) {
			DB.data.remove(item);
			DB.database.delete(item);
			return true;
		} else 
			return false;
		
	}
	public synchronized static void add(BriefRating item) {
		if(item!=null) {
			
			DB.database.add(item);
			DB.data=DB.database.getItems(0, 200);
		}
		
	}
	public synchronized static void update(BriefRating item) {
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
				
				super("ratings", 
						new DbField[] {
							new DbField(BriefRating.STRING_ITEM_IDENTIFIER,DbField.FIELD_TYPE_TEXT,true,false),
							new DbField(BriefRating.INT_RATING,DbField.FIELD_TYPE_INT),
							new DbField(BriefRating.LONG_DATE,DbField.FIELD_TYPE_INT),
							new DbField(BriefRating.INT_MAX_HIT_RATING,DbField.FIELD_TYPE_INT)
						}
					,context
					);
				this.context=context;

			}
			public Map<String,BriefRating> getItems(int limitStart, int limitEnd) {
				final Map<String,BriefRating> items = new HashMap<String,BriefRating>();
				Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, BriefRating.STRING_ITEM_IDENTIFIER+" DESC LIMIT "+limitStart+","+limitEnd);
				
				if(cur.getCount()>0) {
					cur.moveToFirst();
					do {
						BriefRating rating=getRatingFromCursor(cur);
						items.put(rating.getString(BriefRating.STRING_ITEM_IDENTIFIER),rating);
					} while(cur.moveToNext());
				
				}
				
				cur.close();
				
				return items;
			}
			public int update(BriefRating item) {
				ContentValues values = new ContentValues();
			    values.put(BriefRating.INT_RATING, item.getInt(BriefRating.INT_RATING));
			    values.put(BriefRating.LONG_DATE, item.getLong(BriefRating.LONG_DATE));
			    values.put(BriefRating.INT_MAX_HIT_RATING, item.getInt(BriefRating.INT_MAX_HIT_RATING));
				    
			    int id = db.update(TABLE_NAME, values, BriefRating.STRING_ITEM_IDENTIFIER+"=?", new String[]{item.getString(BriefRating.STRING_ITEM_IDENTIFIER)});
			    return id;
			}
					
			private BriefRating getRatingFromCursor(Cursor cursor) {
				BriefRating item = new BriefRating();
				item.setString(BriefRating.STRING_ITEM_IDENTIFIER, cursor.getString(cursor.getColumnIndex(BriefRating.STRING_ITEM_IDENTIFIER)));
				item.setInt(BriefRating.INT_RATING, cursor.getInt(cursor.getColumnIndex(BriefRating.INT_RATING)));
				item.setInt(BriefRating.INT_MAX_HIT_RATING, cursor.getInt(cursor.getColumnIndex(BriefRating.INT_MAX_HIT_RATING)));
				item.setLong(BriefRating.LONG_DATE, cursor.getLong(cursor.getColumnIndex(BriefRating.LONG_DATE)));
		    	return item;
			}
			
			public boolean hasItem(int noteid) {
				Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
						BriefRating.STRING_ITEM_IDENTIFIER+"=?", new String[]{noteid+""}, null, null, null);
				
				boolean alreadyHasFeed=false;
				
				if(cur!=null && cur.getCount()>0) 
					alreadyHasFeed=true;
				cur.close();
				return alreadyHasFeed;
			}
			
			public void add(BriefRating item) {
				
				if(item!=null) {
					ContentValues values = new ContentValues();
				    //values.put(Note.INT_ID, item.getInt(Note.INT_ID));
				    values.put(BriefRating.STRING_ITEM_IDENTIFIER, item.getString(BriefRating.STRING_ITEM_IDENTIFIER));
				    values.put(BriefRating.INT_RATING, item.getInt(BriefRating.INT_RATING));
				    values.put(BriefRating.LONG_DATE, item.getLong(BriefRating.LONG_DATE));
				    long id = db.insert(TABLE_NAME, null, values);
				}
			    
			}
        public void delete(int WITH_,long accountId) {
            open();
            db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+BriefRating.STRING_ITEM_IDENTIFIER+" = \""+WITH_+""+accountId+"-%\"");

        }
			public void delete(BriefRating item) {
				open();
				db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+BriefRating.STRING_ITEM_IDENTIFIER+" = \""+item.getString(BriefRating.STRING_ITEM_IDENTIFIER)+"\"");
				
			}
			public void deleteAll() {
				open();
				db.execSQL("DELETE FROM "+TABLE_NAME);  
				
			}
		}
	
	
}
