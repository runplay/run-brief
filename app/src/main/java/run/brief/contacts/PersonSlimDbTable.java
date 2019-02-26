package run.brief.contacts;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import java.util.ArrayList;

import run.brief.beans.Person;
import run.brief.util.Db;
import run.brief.util.DbField;
import run.brief.util.json.JSONArray;

public class PersonSlimDbTable extends Db {

		public PersonSlimDbTable(Context context) {
			
			super("bperson",
					new DbField[] {
						new DbField(Person.LONG_ID,DbField.FIELD_TYPE_INT,true,false),
						new DbField(Person.STRING_PERSON_ID,DbField.FIELD_TYPE_TEXT),
						new DbField(Person.STRING_NAME,DbField.FIELD_TYPE_TEXT),
						new DbField(Person.JSONARRAY_PHONE,DbField.FIELD_TYPE_TEXT),
						new DbField(Person.JSONARRAY_EMAIL,DbField.FIELD_TYPE_TEXT),
						new DbField(Person.LONG_THUMBNAIL_ID,DbField.FIELD_TYPE_INT)
					}
				,context
				);
			this.context=context;

		}
		public ArrayList<Person> getItems() {
			ArrayList<Person> items = new ArrayList<Person>();
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),null, null, null, null, Person.STRING_NAME+" COLLATE NOCASE ASC");
			
			if(cur.getCount()>0) {
				cur.moveToFirst();
				do {
					items.add(getPersonSlimFromCursor(cur));
				} while(cur.moveToNext());
			
			}
			
			cur.close();
			
			return items;
		}
		public void update(Person item) {
			ContentValues values = new ContentValues();
		    values.put(Person.LONG_ID, item.getLong(Person.LONG_ID));
		    values.put(Person.STRING_PERSON_ID, item.getString(Person.STRING_PERSON_ID));
            values.put(Person.STRING_NAME, item.getString(Person.STRING_NAME));
            values.put(Person.JSONARRAY_PHONE, item.getJSONArray(Person.JSONARRAY_PHONE).toString());
            values.put(Person.JSONARRAY_EMAIL, item.getJSONArray(Person.JSONARRAY_EMAIL).toString());
		    values.put(Person.LONG_THUMBNAIL_ID, item.getLong(Person.LONG_THUMBNAIL_ID));

			    
		    long id = db.update(TABLE_NAME, values, Person.LONG_ID+"=?", new String[]{""+item.getLong(Person.LONG_ID)});
		     
		}
				
		private static Person getPersonSlimFromCursor(Cursor cursor) {
			Person item = new Person();
			item.setLong(Person.LONG_ID, cursor.getInt(cursor.getColumnIndex(Person.LONG_ID)));
			item.setString(Person.STRING_PERSON_ID, cursor.getString(cursor.getColumnIndex(Person.STRING_PERSON_ID)));
            item.setString(Person.STRING_NAME, cursor.getString(cursor.getColumnIndex(Person.STRING_NAME)));
            item.setJSONArray(Person.JSONARRAY_PHONE, new JSONArray(cursor.getString(cursor.getColumnIndex(Person.JSONARRAY_PHONE))));
            item.setJSONArray(Person.JSONARRAY_EMAIL, new JSONArray(cursor.getString(cursor.getColumnIndex(Person.JSONARRAY_EMAIL))));
			item.setLong(Person.LONG_THUMBNAIL_ID, cursor.getLong(cursor.getColumnIndex(Person.LONG_THUMBNAIL_ID)));

			//item.setString(PersonSlim.JSONARRAY_RAW, cursor.getString(cursor.getColumnIndex(PersonSlim.JSONARRAY_FILES)));
			//BLog.e("PUB","-"+ cursor.getString(cursor.getColumnIndex(PersonSlim.STRING_PUBLISHER)));
	    	return item;
		}
		
		public boolean hasItem(long PersonSlimid) {
			Cursor cur = db.query(TABLE_NAME, this.getFieldNames(),
					Person.LONG_ID+"=?", new String[]{PersonSlimid+""}, null, null, null);
			
			boolean alreadyHasFeed=false;
			
			if(cur!=null && cur.getCount()>0) 
				alreadyHasFeed=true;
			cur.close();
			return alreadyHasFeed;
		}
		
		public long add(Person item) {
			
			if(item!=null) {
				ContentValues values = new ContentValues();
                values.put(Person.STRING_PERSON_ID, item.getString(Person.STRING_PERSON_ID));
                values.put(Person.STRING_NAME, item.getString(Person.STRING_NAME));
                values.put(Person.JSONARRAY_PHONE, item.getJSONArray(Person.JSONARRAY_PHONE).toString());
                values.put(Person.JSONARRAY_EMAIL, item.getJSONArray(Person.JSONARRAY_EMAIL).toString());
                values.put(Person.LONG_THUMBNAIL_ID, item.getLong(Person.LONG_THUMBNAIL_ID));

			    return db.insert(TABLE_NAME, null, values);
			}
		    return -1;
		}
		public void delete(Person item) {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME+" WHERE "+ Person.LONG_ID+" = "+item.getString(Person.LONG_ID));
			
		}
		public void deleteAll() {
			open();
			db.execSQL("DELETE FROM "+TABLE_NAME);  
			
		}
	}