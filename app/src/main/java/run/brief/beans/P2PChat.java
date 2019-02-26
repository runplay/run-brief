package run.brief.beans;


import java.util.ArrayList;

import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;

public class P2PChat extends BJSONBean {
	public static final String INT_ID="ntid";
	public static final String STRING_PERSON_ID="pid";
	public static final String STRING_GROUP_ID="gid";
	public static final String STRING_TEXT="ntt";
	public static final String LONG_DATE_CREATED="ntd";
	public static final String LONG_DATE_DELIVERED="ntdd";
	public static final String INT_STATUS="status";
	public static final String JSONARRAY_FILES="f";
	//private JSONObject note;
	public P2PChat(JSONObject note) {
		super(note);
	}
	public P2PChat() {
		bean=new JSONObject();
		bean.put(JSONARRAY_FILES, new JSONArray());
		bean.put(STRING_TEXT, "");
		//bean.put(INT_STATUS, Brief.STATE_SENDNG);
	}
	
	public void addFile(String filePath) {
		((JSONArray) this.getJSONArray(JSONARRAY_FILES)).put(filePath);
	}
	public int getFileCount() {
		return ((JSONArray) this.getJSONArray(JSONARRAY_FILES)).length();
	}
	public ArrayList<String> getFiles() {
		ArrayList<String> files = new ArrayList<String>();
		JSONArray flist=(JSONArray) this.getJSONArray(JSONARRAY_FILES);
		if(flist!=null) {
		for(int i=0; i<flist.length(); i++) {
			files.add(flist.getString(i));
		}
		}
		return files;
	}
	public boolean isEmpty() {
		if(getString(P2PChat.STRING_TEXT).isEmpty())
			return true;
		return false;
	}

}
