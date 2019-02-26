package run.brief.news;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import run.brief.beans.RssItem;
import run.brief.beans.RssUserFeed;
import run.brief.util.Db;
import run.brief.util.DbField;
import run.brief.util.Sf;

public class NewsItemsDbTable extends Db {
		
		public NewsItemsDbTable(Context context) {
			
			super("store_news", 
					new DbField[] {
						new DbField(RssItem.LONG_ID, DbField.FIELD_TYPE_INT,true,false),
						new DbField(RssItem.STRING_URL, DbField.FIELD_TYPE_TEXT,false,true),
						new DbField(RssItem.STRING_PUBLISHER, DbField.FIELD_TYPE_TEXT),
						new DbField(RssItem.STRING_HEAD, DbField.FIELD_TYPE_TEXT),
						new DbField(RssItem.STRING_IMG_URL, DbField.FIELD_TYPE_TEXT),
                        new DbField(RssItem.STRING_MEDIA_URL, DbField.FIELD_TYPE_TEXT),
						new DbField(RssItem.STRING_TEXT, DbField.FIELD_TYPE_TEXT),
						new DbField(RssItem.LONG_DATE, DbField.FIELD_TYPE_INT),
                        new DbField(RssItem.BOOL_SHOW_BRIEF, DbField.FIELD_TYPE_INT),
							new DbField(RssItem.INT_FAVOURITE, DbField.FIELD_TYPE_INT),
							new DbField(RssItem.INT_READ, DbField.FIELD_TYPE_INT)
					}
				,context
				);
			this.context=context;

		}

	//item.setString(RssItem.STRING_PUBLISHER, cursor.getString(cursor.getColumnIndex(RssItem.STRING_PUBLISHER)));
	public long getRowsSize() {
		long count=0;
		Cursor cur = db.rawQuery("SELECT count(*) AS rc FROM "+TABLE_NAME,null);

		if(cur.getCount()>0) {
			cur.moveToFirst();

			count =cur.getLong(cur.getColumnIndex("rc"));
			//items.add(0, getRssItemFromCursor(cur));


		}

		cur.close();

		return count;
	}
	public int getFavouritesCount() {
		int count=0;
		Cursor cur = db.rawQuery("SELECT count(*) AS rc FROM "+TABLE_NAME+" WHERE "+RssItem.INT_FAVOURITE+" >0",null);

		if(cur.getCount()>0) {
			cur.moveToFirst();

			count =cur.getInt(cur.getColumnIndex("rc"));
			//items.add(0, getRssItemFromCursor(cur));


		}

		cur.close();

		return count;
	}
	public ArrayList<RssItem> searchKeywords(ArrayList<String> keywords, int limitStart, int limitEnd) {
		ArrayList<RssItem> items = new ArrayList<RssItem>();
		Cursor cur = null;

		for(String keyword: keywords) {

			cur=db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+RssItem.STRING_HEAD+" LIKE '%"+ Sf.makeDbSafe(keyword)+"%' OR "+RssItem.STRING_HEAD+" LIKE '%"+ Sf.makeDbSafe(keyword)+"%' COLLATE NOCASE ORDER BY "+ RssItem.LONG_DATE+" DESC LIMIT "+limitStart+","+limitEnd,null);


			//b.query(TABLE_NAME, this.getFieldNames(), RssItem.STRING_HEAD + "=?", new String[]{publisher}, null, null, "id DESC LIMIT " + limitStart + "," + limitEnd);
//BLog.e("SEARCH: "+cur.getCount());
			if (cur.getCount() > 0) {
				cur.moveToFirst();
				do {
					//BLog.e("RRSI","has");
					items.add(getRssItemFromCursor(cur));
				} while (cur.moveToNext());

			}

		}
		cur.close();

		return items;
	}

        public ArrayList<RssItem> getItemsForPublisher(String publisher, int limitStart, int limitEnd) {
            ArrayList<RssItem> items = new ArrayList<RssItem>();
            Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),RssItem.STRING_PUBLISHER+"=?", new String[]{publisher}, null, null, RssItem.LONG_DATE+" DESC LIMIT "+limitStart+","+limitEnd);

            if(cur.getCount()>0) {
                cur.moveToFirst();
                do {
                    //BLog.e("RRSI","has");
                    items.add(getRssItemFromCursor(cur));
                } while(cur.moveToNext());

            }

            cur.close();

            return items;
        }
    public RssItem getItem(long id) {
        ArrayList<RssItem> items = new ArrayList<RssItem>();
        Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),RssItem.LONG_ID+"=?", new String[]{""+id}, null, null, "id DESC");
        RssItem retItem=null;
        if(cur.getCount()>0) {
            cur.moveToFirst();
            do {
                //BLog.e("RRSI","has");
                retItem= getRssItemFromCursor(cur);
                break;
            } while(cur.moveToNext());

        }

        cur.close();

        return retItem;
    }
	public ArrayList<RssItem> getItemsStarred(int limitStart, int limitEnd) {
		ArrayList<RssItem> items = new ArrayList<RssItem>();
		Cursor cur = db.rawQuery("SELECT * FROM "+TABLE_NAME+" WHERE "+RssItem.INT_FAVOURITE+" =1 ORDER BY "+RssItem.LONG_DATE+" DESC LIMIT "+limitStart+","+limitEnd,null);

		if(cur.getCount()>0) {
			cur.moveToFirst();
			do {
				//BLog.e("RRSI","has");
				items.add(0,getRssItemFromCursor(cur));
			} while(cur.moveToNext());

		}

		cur.close();

		return items;
	}
		public ArrayList<RssItem> getItems(int limitStart, int limitEnd) {
			ArrayList<RssItem> items = new ArrayList<RssItem>();
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, RssItem.LONG_DATE+" DESC LIMIT "+limitStart+","+limitEnd);
			
			if(cur.getCount()>0) {
				cur.moveToFirst();
				do {
                    //BLog.e("RRSI","has");
					items.add(0,getRssItemFromCursor(cur));
				} while(cur.moveToNext());
			
			}
			
			cur.close();
			
			return items;
		}
	public void updateFavouriteRead(RssItem item) {
		ContentValues values = new ContentValues();
		values.put(RssItem.INT_FAVOURITE, item.getInt(RssItem.INT_FAVOURITE));
		values.put(RssItem.INT_READ, item.getInt(RssItem.INT_READ));
		long id = db.update(TABLE_NAME, values, RssItem.STRING_URL+"=?", new String[]{item.getString(RssUserFeed.STRING_URL)});

	}
		public void update(RssItem item) {
			ContentValues values = new ContentValues();
		    //values.put(RssItem.INT_ID, item.getInt(RssItem.INT_ID));
		    values.put(RssItem.LONG_DATE, item.getLong(RssItem.LONG_DATE));
		    values.put(RssItem.STRING_HEAD, item.getString(RssItem.STRING_HEAD));
		    values.put(RssItem.STRING_URL, item.getString(RssItem.STRING_URL));
		    values.put(RssItem.STRING_TEXT, item.getString(RssItem.STRING_TEXT));
		    values.put(RssItem.STRING_IMG_URL, item.getString(RssItem.STRING_IMG_URL));
            values.put(RssItem.STRING_MEDIA_URL, item.getString(RssItem.STRING_MEDIA_URL));
            values.put(RssItem.BOOL_SHOW_BRIEF, item.getBoolean(RssItem.BOOL_SHOW_BRIEF) ? 1 : 0);
			values.put(RssItem.INT_FAVOURITE, item.getInt(RssItem.INT_FAVOURITE));
			values.put(RssItem.INT_READ, item.getInt(RssItem.INT_READ));
			//values.put(RssItem.INT_ERROR_COLLECT_COUNT, item.getInt(RssItem.INT_ERROR_COLLECT_COUNT));

		    long id = db.update(TABLE_NAME, values, RssItem.STRING_URL+"=?", new String[]{item.getString(RssUserFeed.STRING_URL)});
		    
		}

				
		private static RssItem getRssItemFromCursor(Cursor cursor) {
			RssItem item = new RssItem();
			item.setLong(RssItem.LONG_ID, cursor.getLong(cursor.getColumnIndex(RssItem.LONG_ID)));
			item.setLong(RssItem.LONG_DATE, cursor.getLong(cursor.getColumnIndex(RssItem.LONG_DATE)));
			item.setString(RssItem.STRING_HEAD, cursor.getString(cursor.getColumnIndex(RssItem.STRING_HEAD)));
			item.setString(RssItem.STRING_URL, cursor.getString(cursor.getColumnIndex(RssItem.STRING_URL)));
			item.setString(RssItem.STRING_TEXT, cursor.getString(cursor.getColumnIndex(RssItem.STRING_TEXT)));
			item.setString(RssItem.STRING_IMG_URL, cursor.getString(cursor.getColumnIndex(RssItem.STRING_IMG_URL)));
            item.setString(RssItem.STRING_MEDIA_URL, cursor.getString(cursor.getColumnIndex(RssItem.STRING_MEDIA_URL)));
			item.setString(RssItem.STRING_PUBLISHER, cursor.getString(cursor.getColumnIndex(RssItem.STRING_PUBLISHER)));
			//item.setInt(RssItem.INT_ERROR_COLLECT_COUNT, cursor.getInt(cursor.getColumnIndex(RssItem.INT_ERROR_COLLECT_COUNT)));
            item.setBoolean(RssItem.BOOL_SHOW_BRIEF, cursor.getInt(cursor.getColumnIndex(RssItem.BOOL_SHOW_BRIEF)) == 1 ? true : false);
			item.setInt(RssItem.INT_FAVOURITE, cursor.getInt(cursor.getColumnIndex(RssItem.INT_FAVOURITE)));
			item.setInt(RssItem.INT_READ, cursor.getInt(cursor.getColumnIndex(RssItem.INT_READ)));
			//BLog.e("GOT PUB", "-" + cursor.getString(cursor.getColumnIndex(RssItem.STRING_PUBLISHER)));
	    	return item;
		}
		
		public boolean hasItem(String itemUrl) {
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
					RssItem.STRING_URL+"=?", new String[]{itemUrl}, null, null, null);
			
			boolean alreadyHasFeed=false;
			//BLog.e("HAS","IS: "+cur.getCount());
			if(cur!=null && cur.getCount()>0)
				alreadyHasFeed=true;
			cur.close();
			return alreadyHasFeed;
		}
		
		public long add(RssItem item) {
            long id=0;
			if(!hasItem(item.getString(RssItem.STRING_URL))) {
                //BLog.e("RRSI","add: "+item.getInt(RssItem.BOOL_SHOW_BRIEF));
				ContentValues values = new ContentValues();
			    values.put(RssItem.LONG_DATE, item.getLong(RssItem.LONG_DATE));
			    values.put(RssItem.STRING_HEAD, item.getString(RssItem.STRING_HEAD));
			    values.put(RssItem.STRING_URL, item.getString(RssItem.STRING_URL));
			    values.put(RssItem.STRING_PUBLISHER, item.getString(RssItem.STRING_PUBLISHER));
			    values.put(RssItem.STRING_TEXT, item.getString(RssItem.STRING_TEXT));
			    values.put(RssItem.STRING_IMG_URL, item.getString(RssItem.STRING_IMG_URL));
                values.put(RssItem.STRING_MEDIA_URL, item.getString(RssItem.STRING_MEDIA_URL));
                values.put(RssItem.BOOL_SHOW_BRIEF, item.getBoolean(RssItem.BOOL_SHOW_BRIEF)?1:0);
				values.put(RssItem.INT_FAVOURITE, item.getInt(RssItem.INT_FAVOURITE));
				values.put(RssItem.INT_READ, item.getInt(RssItem.INT_READ));
				//values.put(RssItem.INT_ERROR_COLLECT_COUNT, item.getInt(RssItem.INT_ERROR_COLLECT_COUNT));
			    id = db.insert(TABLE_NAME, null, values);
			}
		    return id;
		}
		public void delete(RssItem item) {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+RssItem.STRING_URL+" = '"+item.getString(RssUserFeed.STRING_URL)+"'");
			
		}
	public void deleteOlderThan(Long timeinmillis) {
		open();
		db.execSQL("DELETE FROM " + TABLE_NAME + " WHERE " + RssItem.LONG_DATE + " < " + timeinmillis);

	}
		public void deleteAll() {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME);  
			
		}
	}