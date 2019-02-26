package run.brief.notes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import run.brief.beans.Note;
import run.brief.util.Db;
import run.brief.util.DbField;
import run.brief.util.json.JSONArray;

public class NotesDbTable extends Db {
		
		public NotesDbTable(Context context) {
			
			super("notes", 
					new DbField[] {
						new DbField(Note.INT_ID,DbField.FIELD_TYPE_INT,true,false),
						new DbField(Note.STRING_TEXT,DbField.FIELD_TYPE_TEXT),
						new DbField(Note.LONG_DATE_CREATED,DbField.FIELD_TYPE_INT),
						new DbField(Note.LONG_DATE_ALERT,DbField.FIELD_TYPE_INT),
						new DbField(Note.JSONARRAY_FILES,DbField.FIELD_TYPE_TEXT),
						new DbField(Note.INT_IS_ACTIONED,DbField.FIELD_TYPE_INT),
                            new DbField(Note.INT_TYPE,DbField.FIELD_TYPE_INT)
					}
				,context
				);
			this.context=context;

		}
		public ArrayList<Note> getItems(int limitStart, int limitEnd) {
			ArrayList<Note> items = new ArrayList<Note>();
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, Note.INT_ID+" DESC LIMIT "+limitStart+","+limitEnd);
			
			if(cur.getCount()>0) {
				cur.moveToFirst();
				do {
					items.add(getNoteFromCursor(cur));
				} while(cur.moveToNext());
			
			}
			
			cur.close();
			
			return items;
		}
		public void update(Note item) {
			ContentValues values = new ContentValues();
		    //values.put(Note.INT_ID, item.getInt(Note.INT_ID));
            values.put(Note.INT_TYPE, item.getInt(Note.INT_TYPE));
		    values.put(Note.STRING_TEXT, item.getString(Note.STRING_TEXT));
		    values.put(Note.LONG_DATE_CREATED, item.getLong(Note.LONG_DATE_CREATED));
		    values.put(Note.LONG_DATE_ALERT, item.getLong(Note.LONG_DATE_ALERT));
		    values.put(Note.JSONARRAY_FILES, item.getJSONArray(Note.JSONARRAY_FILES).toString());

			    
		    long id = db.update(TABLE_NAME, values, Note.INT_ID+"=?", new String[]{""+item.getInt(Note.INT_ID)});
		     
		}
				
		private static Note getNoteFromCursor(Cursor cursor) {
			Note item = new Note();
			item.setInt(Note.INT_ID, cursor.getInt(cursor.getColumnIndex(Note.INT_ID)));
            item.setInt(Note.INT_TYPE, cursor.getInt(cursor.getColumnIndex(Note.INT_TYPE)));
			item.setString(Note.STRING_TEXT, cursor.getString(cursor.getColumnIndex(Note.STRING_TEXT)));
			item.setLong(Note.LONG_DATE_CREATED, cursor.getLong(cursor.getColumnIndex(Note.LONG_DATE_CREATED)));
			item.setLong(Note.LONG_DATE_ALERT, cursor.getLong(cursor.getColumnIndex(Note.LONG_DATE_ALERT)));
			item.setJSONArray(Note.JSONARRAY_FILES, new JSONArray(cursor.getString(cursor.getColumnIndex(Note.JSONARRAY_FILES))));
			//item.setString(Note.JSONARRAY_RAW, cursor.getString(cursor.getColumnIndex(Note.JSONARRAY_FILES)));
			//BLog.e("PUB","-"+ cursor.getString(cursor.getColumnIndex(Note.STRING_PUBLISHER)));
	    	return item;
		}
		
		public boolean hasItem(int noteid) {
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
					Note.INT_ID+"=?", new String[]{noteid+""}, null, null, null);
			
			boolean alreadyHasFeed=false;
			
			if(cur!=null && cur.getCount()>0) 
				alreadyHasFeed=true;
			cur.close();
			return alreadyHasFeed;
		}
		
		public long add(Note item) {
			
			if(item!=null) {
				ContentValues values = new ContentValues();
			    //values.put(Note.INT_ID, item.getInt(Note.INT_ID));
                values.put(Note.INT_TYPE, item.getInt(Note.INT_TYPE));
			    values.put(Note.STRING_TEXT, item.getString(Note.STRING_TEXT));
			    values.put(Note.LONG_DATE_CREATED, item.getLong(Note.LONG_DATE_CREATED));
			    values.put(Note.LONG_DATE_ALERT, item.getLong(Note.LONG_DATE_ALERT));
			    values.put(Note.JSONARRAY_FILES, item.getString(Note.JSONARRAY_FILES));
			    return db.insert(TABLE_NAME, null, values);
			}
		    return -1;
		}
		public void delete(Note item) {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+Note.INT_ID+" = "+item.getString(Note.INT_ID));
			
		}
		public void deleteAll() {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME);  
			
		}
	}