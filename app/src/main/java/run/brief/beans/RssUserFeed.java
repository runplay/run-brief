package run.brief.beans;

import run.brief.util.Cal;
import run.brief.util.json.JSONObject;

public class RssUserFeed extends BJSONBean {

	//public static final int COLLECT_OFF=0;
	public static final int COLLECT_FAST=1;
	public static final int COLLECT_REGULAR=2;
	public static final int COLLECT_SLOW=0;
	public static final int COLLECT_OFF=5;
	//public static final int COLLECT_DAILY=4;

	public final static long TIME_FAST_MILLIS = Cal.HOURS_1_IN_MILLIS; // 1hr mins
	public final static long TIME_MEDIUM_MILLIS = Cal.HOURS_1_IN_MILLIS*3; // 3 hours
	public final static long TIME_SLOW_MILLIS = Cal.HOURS_1_IN_MILLIS*24; // 24 hours

	public static final String LONG_ID="id";
	public static final String STRING_URL="furl";
	public static final String STRING_PUBLISHER="pub";
	public static final String STRING_PUBLISHER_IMAGE="pubi";
	public static final String LONG_LAST_UPDATE="lupd";
	public static final String INT_ARTICLE_READ_COUNT="arc";
	public static final String INT_COLLECT_="coll";
	public static final String INT_CUSTOM="cust";
	public static final String INT_ACTIVE="actv";
	public static final String INT_ERROR_COLLECT_COUNT="errc";

	public RssUserFeed(JSONObject feed) {
		super(feed);
	}
	public RssUserFeed(String publisher,String url) {
		super();
		this.bean.put(STRING_PUBLISHER, publisher);
		this.bean.put(STRING_URL, url);
		this.bean.put(INT_COLLECT_, url);
		this.bean.put(INT_ACTIVE, 1);
	}
	public RssUserFeed(String publisher,String url, int COLLECT_, String publisherImage) {
		super();
		this.bean.put(STRING_PUBLISHER, publisher);
		this.bean.put(STRING_PUBLISHER_IMAGE, publisherImage);
		this.bean.put(STRING_URL, url);
		this.bean.put(INT_COLLECT_, COLLECT_);
		this.bean.put(INT_ACTIVE, 1);
	}
	public RssUserFeed(String publisher,String url, String publisherImage) {
		super();
		this.bean.put(STRING_PUBLISHER, publisher);
		this.bean.put(STRING_PUBLISHER_IMAGE, publisherImage);
		this.bean.put(STRING_URL, url);
		this.bean.put(INT_ACTIVE, 1);
	}
	public RssUserFeed(String publisher, String url, int COLLECT_) {
		super();
		this.bean.put(STRING_PUBLISHER, publisher);
		this.bean.put(STRING_URL, url);
		this.bean.put(INT_COLLECT_, COLLECT_);
		this.bean.put(INT_ACTIVE, 1);
	}
}
