package run.brief.news;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.HashMap;

import run.brief.beans.RssUserFeed;
import run.brief.util.Db;
import run.brief.util.DbField;

public class UserFeedDbTable extends Db {
		
		public UserFeedDbTable(Context context) {
			
			super("user_news", 
					new DbField[] {
						new DbField(RssUserFeed.STRING_URL, DbField.FIELD_TYPE_TEXT,true,false),
						new DbField(RssUserFeed.STRING_PUBLISHER, DbField.FIELD_TYPE_TEXT),
						new DbField(RssUserFeed.STRING_PUBLISHER_IMAGE, DbField.FIELD_TYPE_TEXT),
						new DbField(RssUserFeed.INT_COLLECT_, DbField.FIELD_TYPE_INT),
						new DbField(RssUserFeed.INT_ARTICLE_READ_COUNT, DbField.FIELD_TYPE_INT),
						new DbField(RssUserFeed.LONG_LAST_UPDATE, DbField.FIELD_TYPE_INT),
						new DbField(RssUserFeed.INT_CUSTOM, DbField.FIELD_TYPE_INT),
						new DbField(RssUserFeed.INT_ACTIVE, DbField.FIELD_TYPE_INT),
						new DbField(RssUserFeed.INT_ERROR_COLLECT_COUNT, DbField.FIELD_TYPE_INT)
					}
				,context
				);
			this.context=context;
			//this.ensureTable(context);

		}
		
		public void update(RssUserFeed feed) {
			ContentValues values = new ContentValues();
		    values.put(RssUserFeed.INT_ARTICLE_READ_COUNT, feed.getInt(RssUserFeed.INT_ARTICLE_READ_COUNT));
		    values.put(RssUserFeed.INT_COLLECT_, feed.getInt(RssUserFeed.INT_COLLECT_));
		    values.put(RssUserFeed.LONG_LAST_UPDATE, feed.getLong(RssUserFeed.LONG_LAST_UPDATE));
		    values.put(RssUserFeed.INT_ACTIVE, feed.getInt(RssUserFeed.INT_ACTIVE));
		    values.put(RssUserFeed.INT_CUSTOM, feed.getInt(RssUserFeed.INT_CUSTOM));
            values.put(RssUserFeed.STRING_PUBLISHER, feed.getString(RssUserFeed.STRING_PUBLISHER));
			values.put(RssUserFeed.STRING_PUBLISHER_IMAGE, feed.getString(RssUserFeed.STRING_PUBLISHER_IMAGE));
			values.put(RssUserFeed.INT_ERROR_COLLECT_COUNT, feed.getInt(RssUserFeed.INT_ERROR_COLLECT_COUNT));
		    long id = db.update(TABLE_NAME, values, RssUserFeed.STRING_URL+"=?", new String[]{feed.getString(RssUserFeed.STRING_URL)});
		    
		}
		public void updateReadCount(RssUserFeed feed) {
			ContentValues values = new ContentValues();
		    values.put(RssUserFeed.INT_ARTICLE_READ_COUNT, feed.getInt(RssUserFeed.INT_ARTICLE_READ_COUNT));
		    long id = db.update(TABLE_NAME, values, RssUserFeed.STRING_URL+"=?", new String[]{feed.getString(RssUserFeed.STRING_URL)});
		    
		}
		public ArrayList<RssUserFeed> getUserFeeds() {
			ArrayList<RssUserFeed> feeds = new ArrayList<RssUserFeed>();
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, null);
			
			if(cur.getCount()>0) {
				cur.moveToFirst();
				do {
					feeds.add(getRssUserFeedCursor(cur));
				} while(cur.moveToNext());
			
			}
			
			cur.close();
			
			return feeds;
		}
	public void updateErrorCollectCount(RssUserFeed item) {
		ContentValues values = new ContentValues();
		values.put(RssUserFeed.INT_ERROR_COLLECT_COUNT, item.getInt(RssUserFeed.INT_ERROR_COLLECT_COUNT));

		long id = db.update(TABLE_NAME, values, RssUserFeed.STRING_URL+"=?", new String[]{item.getString(RssUserFeed.STRING_URL)});

	}
	public void resetErrorCollectCount(RssUserFeed item) {
		ContentValues values = new ContentValues();
		values.put(RssUserFeed.INT_ERROR_COLLECT_COUNT, 0);

		long id = db.update(TABLE_NAME, values, RssUserFeed.STRING_URL+"=?", new String[]{item.getString(RssUserFeed.STRING_URL)});

	}
		public HashMap<String,RssUserFeed> getUserFeedsHash() {
			HashMap<String,RssUserFeed> feeds = new HashMap<String,RssUserFeed>();
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, null);
			
			if(cur.getCount()>0) {
				cur.moveToFirst();
				do {
					RssUserFeed f = getRssUserFeedCursor(cur);
					feeds.put(f.getString(RssUserFeed.STRING_URL),f);
				} while(cur.moveToNext());
			
			}
			
			cur.close();
			
			return feeds;
		}
		
		private static RssUserFeed getRssUserFeedCursor(Cursor cursor) {
			RssUserFeed feed = new RssUserFeed(cursor.getString(cursor.getColumnIndex(RssUserFeed.STRING_PUBLISHER)),cursor.getString(cursor.getColumnIndex(RssUserFeed.STRING_URL)));
			feed.setString(RssUserFeed.STRING_PUBLISHER_IMAGE, cursor.getString(cursor.getColumnIndex(RssUserFeed.STRING_PUBLISHER_IMAGE)));
			feed.setInt(RssUserFeed.INT_COLLECT_, cursor.getInt(cursor.getColumnIndex(RssUserFeed.INT_COLLECT_)));
			feed.setInt(RssUserFeed.INT_ARTICLE_READ_COUNT, cursor.getInt(cursor.getColumnIndex(RssUserFeed.INT_ARTICLE_READ_COUNT)));
			feed.setInt(RssUserFeed.INT_CUSTOM, cursor.getInt(cursor.getColumnIndex(RssUserFeed.INT_CUSTOM)));
            feed.setLong(RssUserFeed.LONG_LAST_UPDATE, cursor.getLong(cursor.getColumnIndex(RssUserFeed.LONG_LAST_UPDATE)));
			feed.setInt(RssUserFeed.INT_ERROR_COLLECT_COUNT, cursor.getInt(cursor.getColumnIndex(RssUserFeed.INT_ERROR_COLLECT_COUNT)));
            //feed.setString(RssUserFeed.STRING_PUBLISHER, cursor.getString(cursor.getColumnIndex(RssUserFeed.STRING_PUBLISHER)));
            //values.put(RssUserFeed.STRING_PUBLISHER, feed.getString(RssUserFeed.STRING_PUBLISHER));
	    	return feed;
		}
		
		public void add(RssUserFeed feed) {
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
							RssUserFeed.STRING_URL+"=?", new String[]{feed.getString(RssUserFeed.STRING_URL)}, null, null, null);
			boolean alreadyHasFeed=false;
			
			if(cur!=null && cur.getCount()>0) 
				alreadyHasFeed=true;
			
			cur.close();
				
			if(!alreadyHasFeed) {
				ContentValues values = new ContentValues();
			    values.put(RssUserFeed.STRING_URL, feed.getString(RssUserFeed.STRING_URL));
			    values.put(RssUserFeed.INT_CUSTOM, feed.getInt(RssUserFeed.INT_CUSTOM));
			    values.put(RssUserFeed.INT_ACTIVE, feed.getInt(RssUserFeed.INT_ACTIVE));
                values.put(RssUserFeed.STRING_PUBLISHER, feed.getString(RssUserFeed.STRING_PUBLISHER));
				values.put(RssUserFeed.STRING_PUBLISHER_IMAGE, feed.getString(RssUserFeed.STRING_PUBLISHER_IMAGE));
                values.put(RssUserFeed.LONG_LAST_UPDATE, feed.getLong(RssUserFeed.LONG_LAST_UPDATE));
                values.put(RssUserFeed.INT_COLLECT_, feed.getInt(RssUserFeed.INT_COLLECT_));
				values.put(RssUserFeed.INT_ERROR_COLLECT_COUNT, feed.getInt(RssUserFeed.INT_ERROR_COLLECT_COUNT));
			    long id = db.insert(TABLE_NAME, null, values);
			}
		    
		}
		public void delete(RssUserFeed feed) {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+RssUserFeed.STRING_URL+" = '"+feed.getString(RssUserFeed.STRING_URL)+"'");
			
		}
	}