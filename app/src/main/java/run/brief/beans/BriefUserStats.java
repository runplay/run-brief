package run.brief.beans;

import run.brief.util.json.JSONObject;

public class BriefUserStats extends BJSONBean {
	public static final String INT_COUNT_LAUNCH="lnch";
	public static final String LONG_DATE_STARTED="star";
	public static final String LONG_DATE_NEWS_LAST="news";
	
	public BriefUserStats(JSONObject obj) {
		this.bean=obj;
	}
	public BriefUserStats() {
		super();
	}
	
}
