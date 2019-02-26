package run.brief.beans;


import run.brief.util.json.JSONObject;

public class RssCategory extends BJSONBean {
	public static final String CATEGORY_INT_ID="id";
	public static final String CATEGORY_STRING_NAME="fn";
	public static final String CATEGORY_INT_MASTER="fu";
	public static final String CATEGORY_STRING_DESCRIPTION="fi";
	public static final String CATEGORY_STRING_IMG_URL="fc";
	
	public RssCategory(JSONObject feed) {
		super(feed);
	}


}
