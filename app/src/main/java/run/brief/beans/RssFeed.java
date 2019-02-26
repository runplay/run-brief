package run.brief.beans;


import java.util.ArrayList;

import android.util.Log;


import run.brief.util.Cal;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;

public class RssFeed extends BJSONBean {
	
	
	
	public static final String INT_ID="id";
	//public static final String INT_ACTIVE_="isa";
	public static final String STRING_NAME="fn";
	public static final String STRING_URL="fu";
	public static final String STRING_IMG_URL="fi";
	public static final String INT_CATEGORY="fc";
	public static final String FEED_TITLE="fti";
	public static final String FEED_DESCRIPTION="fds";
	public static final String FEED_LANGUAGE="fla";
	public static final String FEED_COPYRIGHT="fla";
	
	public RssFeed(JSONObject feed) {
		bean=feed;
	}

}
