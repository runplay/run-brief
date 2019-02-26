package run.brief.d2d;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import run.brief.beans.P2PChat;
import run.brief.util.Db;
import run.brief.util.DbField;
import run.brief.util.json.JSONArray;

public class P2PChatDbTable extends Db {
		
		public P2PChatDbTable(Context context) {
			
			super("P2PChats", 
					new DbField[] {
						new DbField(P2PChat.INT_ID,DbField.FIELD_TYPE_INT,true,false),
						new DbField(P2PChat.STRING_TEXT,DbField.FIELD_TYPE_TEXT),
						new DbField(P2PChat.STRING_PERSON_ID,DbField.FIELD_TYPE_TEXT),
						new DbField(P2PChat.STRING_GROUP_ID,DbField.FIELD_TYPE_TEXT),
						new DbField(P2PChat.LONG_DATE_CREATED,DbField.FIELD_TYPE_INT),
						new DbField(P2PChat.LONG_DATE_DELIVERED,DbField.FIELD_TYPE_INT),
						new DbField(P2PChat.JSONARRAY_FILES,DbField.FIELD_TYPE_TEXT),
						new DbField(P2PChat.INT_STATUS,DbField.FIELD_TYPE_INT)
					}
				,context
				);
			this.context=context;

		}
		public ArrayList<P2PChat> getItems(int limitStart, int limitEnd) {
			ArrayList<P2PChat> items = new ArrayList<P2PChat>();
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, P2PChat.INT_ID+" DESC LIMIT "+limitStart+","+limitEnd);
			
			if(cur.getCount()>0) {
				cur.moveToFirst();
				do {
					items.add(getP2PChatFromCursor(cur));
				} while(cur.moveToNext());
			
			}
			
			cur.close();
			
			return items;
		}
		public void update(P2PChat item) {
			ContentValues values = new ContentValues();
		    values.put(P2PChat.INT_ID, item.getInt(P2PChat.INT_ID));
		    values.put(P2PChat.STRING_PERSON_ID, item.getString(P2PChat.STRING_PERSON_ID));
		    values.put(P2PChat.STRING_GROUP_ID, item.getString(P2PChat.STRING_GROUP_ID));
		    values.put(P2PChat.STRING_TEXT, item.getString(P2PChat.STRING_TEXT));
		    values.put(P2PChat.LONG_DATE_CREATED, item.getLong(P2PChat.LONG_DATE_CREATED));
		    values.put(P2PChat.LONG_DATE_DELIVERED, item.getLong(P2PChat.LONG_DATE_DELIVERED));
		    values.put(P2PChat.JSONARRAY_FILES, item.getJSONArray(P2PChat.JSONARRAY_FILES).toString());
		    values.put(P2PChat.INT_STATUS, item.getInt(P2PChat.INT_STATUS));
			    
		    long id = db.update(TABLE_NAME, values, P2PChat.INT_ID+"=?", new String[]{""+item.getInt(P2PChat.INT_ID)});
		     
		}
				
		private static P2PChat getP2PChatFromCursor(Cursor cursor) {
			P2PChat item = new P2PChat();
			item.setInt(P2PChat.INT_ID, cursor.getInt(cursor.getColumnIndex(P2PChat.INT_ID)));
			item.setString(P2PChat.STRING_TEXT, cursor.getString(cursor.getColumnIndex(P2PChat.STRING_TEXT)));
			item.setString(P2PChat.STRING_PERSON_ID, cursor.getString(cursor.getColumnIndex(P2PChat.STRING_PERSON_ID)));
			item.setString(P2PChat.STRING_GROUP_ID, cursor.getString(cursor.getColumnIndex(P2PChat.STRING_GROUP_ID)));
			item.setLong(P2PChat.LONG_DATE_CREATED, cursor.getLong(cursor.getColumnIndex(P2PChat.LONG_DATE_CREATED)));
			item.setLong(P2PChat.LONG_DATE_DELIVERED, cursor.getLong(cursor.getColumnIndex(P2PChat.LONG_DATE_DELIVERED)));
			item.setJSONArray(P2PChat.JSONARRAY_FILES, new JSONArray(cursor.getString(cursor.getColumnIndex(P2PChat.JSONARRAY_FILES))));
			item.setInt(P2PChat.INT_STATUS, cursor.getInt(cursor.getColumnIndex(P2PChat.INT_STATUS)));
			//item.setString(P2PChat.JSONARRAY_RAW, cursor.getString(cursor.getColumnIndex(P2PChat.JSONARRAY_FILES)));
			//BLog.e("PUB","-"+ cursor.getString(cursor.getColumnIndex(P2PChat.STRING_PUBLISHER)));
	    	return item;
		}
		
		public boolean hasItem(int P2PChatid) {
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
					P2PChat.INT_ID+"=?", new String[]{P2PChatid+""}, null, null, null);
			
			boolean alreadyHasFeed=false;
			
			if(cur!=null && cur.getCount()>0) 
				alreadyHasFeed=true;
			cur.close();
			return alreadyHasFeed;
		}
		
		public long add(P2PChat item) {
			
			if(item!=null) {
				ContentValues values = new ContentValues();
			    //values.put(P2PChat.INT_ID, item.getInt(P2PChat.INT_ID));
				values.put(P2PChat.STRING_PERSON_ID, item.getString(P2PChat.STRING_PERSON_ID));
				values.put(P2PChat.STRING_GROUP_ID, item.getString(P2PChat.STRING_GROUP_ID));
			    values.put(P2PChat.STRING_TEXT, item.getString(P2PChat.STRING_TEXT));
			    values.put(P2PChat.LONG_DATE_CREATED, item.getLong(P2PChat.LONG_DATE_CREATED));
			    values.put(P2PChat.LONG_DATE_DELIVERED, item.getLong(P2PChat.LONG_DATE_DELIVERED));
			    values.put(P2PChat.JSONARRAY_FILES, item.getString(P2PChat.JSONARRAY_FILES));
			    values.put(P2PChat.INT_STATUS, item.getInt(P2PChat.INT_STATUS));
			    return db.insert(TABLE_NAME, null, values);
			}
		    return -1;
		}
		public void delete(P2PChat item) {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+P2PChat.INT_ID+" = "+item.getString(P2PChat.INT_ID));
			
		}
		public void deleteAll() {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME);  
			
		}
	}