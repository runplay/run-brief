package run.brief.beans;

import java.util.Date;

import run.brief.util.json.JSONObject;

public class Tweet extends BJSONBean {
	
	public static final String STRING_NAME="name";
	public static final String STRING_MSG="msg";
	public static final String LONG_DATE="date";
	
	public Tweet(JSONObject tweet) {
		bean=tweet;
	}
}
