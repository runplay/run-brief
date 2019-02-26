package run.brief.beans;


import java.util.ArrayList;

import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;

public class Note extends BJSONBean {
	public static final String INT_ID="ntid";
    public static final String INT_TYPE="type";
	public static final String STRING_TEXT="ntt";
	public static final String LONG_DATE_CREATED="ntd";
	public static final String LONG_DATE_ALERT="ntda";
	public static final String JSONARRAY_FILES="f";
	public static final String INT_IS_ACTIONED="act";
	//private JSONObject note;
	public Note(JSONObject note) {
		super(note);
	}
	public Note() {
		bean=new JSONObject();
		bean.put(JSONARRAY_FILES, new JSONArray());
		bean.put(STRING_TEXT, "");
		bean.put(LONG_DATE_CREATED, 0L);
	}
	
	public void addFile(String filePath) {
        if(getJSONArray(JSONARRAY_FILES)==null)
            setJSONArray(JSONARRAY_FILES,new JSONArray());
		setJSONArray(JSONARRAY_FILES,getJSONArray(JSONARRAY_FILES).put(filePath));
		//BLog.e("ADDFILE", "count file is: "+getJSONArray(JSONARRAY_FILES).length());
	}
	public int getFileCount() {
		return getJSONArray(JSONARRAY_FILES).length();
	}
	public ArrayList<String> getFiles() {
		ArrayList<String> files = new ArrayList<String>();
		JSONArray flist=getJSONArray(JSONARRAY_FILES);
		
		if(flist!=null) {
			//BLog.e("GETFILES", "count file is: "+flist.length()+" - jarr: "+getJSONArray(JSONARRAY_FILES).length());
		for(int i=0; i<flist.length(); i++) {
			files.add(flist.getString(i));
		}
		}
		return files;
	}
	public boolean isEmpty() {
		if(getString(Note.STRING_TEXT).isEmpty())
			return true;
		return false;
	}
	/*
	public void setText(String noteText) {
		try {
			bean.put(STRING_TEXT, noteText);
		} catch(Exception e) {}
	}
	public void setDate(long datetime) {
		try {
			bean.put(LONG_DATE_CREATED, datetime);
		} catch(Exception e) {}
	}
	public String getText() {
		try {
			return bean.getString(STRING_TEXT);
		} catch(Exception e) {}
		return null;
	}
	public Cal getDate() {
		try {
			return new Cal(bean.getLong(LONG_DATE_CREATED));
		} catch(Exception e) {}
		return null;
	}
	public boolean isEmpty() {
		if( (getText()==null || getText().length()==0)
			&& getFiles().isEmpty())
			return true;
		return false;
	}
	public void addFile(String filePath) {
		((JSONArray) bean.get(JSONARRAY_FILES)).put(filePath);
	}
	public int getFileCount() {
		return ((JSONArray) bean.get(JSONARRAY_FILES)).length();
	}
	public ArrayList<String> getFiles() {
		ArrayList<String> files = new ArrayList<String>();
		JSONArray flist=(JSONArray) bean.get(JSONARRAY_FILES);
		for(int i=0; i<flist.length(); i++) {
			files.add(flist.getString(i));
		}
		return files;
	}
	/*
	public Note(JSONObject note) {
		this.note=note;
	}
	
	public String toString() {
		return note.toString();
	}
	
	public Note() {
		note=new JSONObject();
		note.put(NOTE_FILES, new JSONArray());
		note.put(NOTE_TEXT, "");
		note.put(NOTE_DATE, 0L);
	}
	public boolean isEmpty() {
		if( (getText()==null || getText().length()==0)
			&& getFiles().isEmpty())
			return true;
		return false;
	}
	public void addFile(String filePath) {
		((JSONArray) note.get(NOTE_FILES)).put(filePath);
	}
	public int getFileCount() {
		return ((JSONArray) note.get(NOTE_FILES)).length();
	}
	public ArrayList<String> getFiles() {
		ArrayList<String> files = new ArrayList<String>();
		JSONArray flist=(JSONArray) note.get(NOTE_FILES);
		for(int i=0; i<flist.length(); i++) {
			files.add(flist.getString(i));
		}
		return files;
	}
	public String getText() {
		try {
			return note.getString(NOTE_TEXT);
		} catch(Exception e) {}
		return null;
	}
	public void setText(String noteText) {
		try {
			note.put(NOTE_TEXT, noteText);
		} catch(Exception e) {}
	}
	public void setDate(long datetime) {
		try {
			note.put(NOTE_DATE, datetime);
		} catch(Exception e) {}
	}
	public Cal getDate() {
		try {
			return new Cal(note.getLong(NOTE_DATE));
		} catch(Exception e) {}
		return null;
	}
	*/
}
