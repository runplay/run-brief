package run.brief.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import run.brief.beans.SyncData;
import run.brief.util.Db;
import run.brief.util.DbField;

public class SyncDataTable extends Db {
		
		public SyncDataTable(Context context) {
			
			super("syncdata", 
					new DbField[] {
						new DbField(SyncData.STRING_ACCOUNT_ID,DbField.FIELD_TYPE_TEXT,true,false),
						new DbField(SyncData.STRING_MESSAGE_IN,DbField.FIELD_TYPE_TEXT),
                        new DbField(SyncData.STRING_MESSAGE_OUT,DbField.FIELD_TYPE_TEXT),
						new DbField(SyncData.LONG_IN_DATE,DbField.FIELD_TYPE_INT),
                        new DbField(SyncData.LONG_OUT_DATE,DbField.FIELD_TYPE_INT),
						new DbField(SyncData.INT_TYPE_SYNC,DbField.FIELD_TYPE_INT),
						new DbField(SyncData.INT_SYNC_IN_LAST_RESULT_CODE,DbField.FIELD_TYPE_INT),
                        new DbField(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE,DbField.FIELD_TYPE_INT),
                            new DbField(SyncData.INT_IS_ACTIVE,DbField.FIELD_TYPE_INT)
					}
				,context
				);
			this.context=context;

		}
		public ArrayList<SyncData> getItems() {
			ArrayList<SyncData> items = new ArrayList<SyncData>();
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, null);
			
			if(cur.getCount()>0) {
				cur.moveToFirst();
				do {
					items.add(getDataFromCursor(cur));
				} while(cur.moveToNext());
			
			}
			
			cur.close();
			
			return items;
		}
		public long update(SyncData item) {
			ContentValues values = new ContentValues();
		    //values.put(SyncData.LONG_ACCOUNT_ID, item.getLong(SyncData.LONG_ACCOUNT_ID));
		    values.put(SyncData.STRING_MESSAGE_IN, item.getString(SyncData.STRING_MESSAGE_IN));
            values.put(SyncData.STRING_MESSAGE_OUT, item.getString(SyncData.STRING_MESSAGE_OUT));
		    values.put(SyncData.LONG_IN_DATE, item.getLong(SyncData.LONG_IN_DATE));
            values.put(SyncData.LONG_OUT_DATE, item.getLong(SyncData.LONG_OUT_DATE));
		    values.put(SyncData.INT_TYPE_SYNC, item.getInt(SyncData.INT_TYPE_SYNC));
		    values.put(SyncData.INT_SYNC_IN_LAST_RESULT_CODE, item.getInt(SyncData.INT_SYNC_IN_LAST_RESULT_CODE));
            values.put(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE, item.getInt(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE));
            values.put(SyncData.INT_IS_ACTIVE, item.getInt(SyncData.INT_IS_ACTIVE));
			    
		    long id = db.update(TABLE_NAME, values, SyncData.STRING_ACCOUNT_ID+"=?", new String[]{item.getString(SyncData.STRING_ACCOUNT_ID)});
		     
		    return id;
		}
				
		private static SyncData getDataFromCursor(Cursor cursor) {
			SyncData item = new SyncData();
			item.setString(SyncData.STRING_ACCOUNT_ID, cursor.getString(cursor.getColumnIndex(SyncData.STRING_ACCOUNT_ID)));
			item.setString(SyncData.STRING_MESSAGE_IN, cursor.getString(cursor.getColumnIndex(SyncData.STRING_MESSAGE_IN)));
            item.setString(SyncData.STRING_MESSAGE_OUT, cursor.getString(cursor.getColumnIndex(SyncData.STRING_MESSAGE_OUT)));
			item.setLong(SyncData.LONG_IN_DATE, cursor.getLong(cursor.getColumnIndex(SyncData.LONG_IN_DATE)));
            item.setLong(SyncData.LONG_OUT_DATE, cursor.getLong(cursor.getColumnIndex(SyncData.LONG_OUT_DATE)));
			item.setInt(SyncData.INT_TYPE_SYNC, cursor.getInt(cursor.getColumnIndex(SyncData.INT_TYPE_SYNC)));
			item.setInt(SyncData.INT_SYNC_IN_LAST_RESULT_CODE, cursor.getInt(cursor.getColumnIndex(SyncData.INT_SYNC_IN_LAST_RESULT_CODE)));
            item.setInt(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE, cursor.getInt(cursor.getColumnIndex(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE)));
            item.setInt(SyncData.INT_IS_ACTIVE, cursor.getInt(cursor.getColumnIndex(SyncData.INT_IS_ACTIVE)));
	    	return item;
		}
		
		public boolean hasItem(String id) {
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
					SyncData.STRING_ACCOUNT_ID+"=?", new String[]{id}, null, null, null);
			
			boolean alreadyHasFeed=false;
			
			if(cur!=null && cur.getCount()>0) 
				alreadyHasFeed=true;
			cur.close();
			return alreadyHasFeed;
		}
		
		public long add(SyncData item) {
			if(item!=null) {
				ContentValues values = new ContentValues();
			    //values.put(Note.INT_ID, item.getInt(Note.INT_ID));
                values.put(SyncData.STRING_ACCOUNT_ID, item.getString(SyncData.STRING_ACCOUNT_ID));
                values.put(SyncData.STRING_MESSAGE_IN, item.getString(SyncData.STRING_MESSAGE_IN));
                values.put(SyncData.STRING_MESSAGE_OUT, item.getString(SyncData.STRING_MESSAGE_OUT));
                values.put(SyncData.LONG_IN_DATE, item.getLong(SyncData.LONG_IN_DATE));
                values.put(SyncData.LONG_OUT_DATE, item.getLong(SyncData.LONG_OUT_DATE));
                values.put(SyncData.INT_TYPE_SYNC, item.getInt(SyncData.INT_TYPE_SYNC));
                values.put(SyncData.INT_SYNC_IN_LAST_RESULT_CODE, item.getInt(SyncData.INT_SYNC_IN_LAST_RESULT_CODE));
                values.put(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE, item.getInt(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE));
                values.put(SyncData.INT_IS_ACTIVE, item.getInt(SyncData.INT_IS_ACTIVE));
			    return db.insert(TABLE_NAME, null, values);
			}
		    return -1;
		}
		public void delete(SyncData item) {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+SyncData.STRING_ACCOUNT_ID+"=?", new String[]{item.getString(SyncData.STRING_ACCOUNT_ID)+""});
			
		}
    public void deleteAll() {
        open();
        db.execSQL("DELETE FROM "+TABLE_NAME);

    }

	}