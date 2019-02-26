package run.brief.beans;

import run.brief.util.json.JSONObject;

public class TwitterAccount extends JSONObject {
	public static final String TWIT_NAME="tn";
	public static final String TWIT_ACCESS_TOKEN="tat";
	public static final String TWIT_ACCESS_TOKEN_SECRET="tats";

	private JSONObject account=new JSONObject();
	
	public String toString() {
		return account.toString();
	}
	
	public String getName() {
		try {
			return account.getString(TWIT_NAME);
		} catch(Exception e) {}
		return null;
	}
	public void setName(String name) {
		try {
			account.put(TWIT_NAME, name);
		} catch(Exception e) {}
	}
	public String getAccessToken() {
		try {
			return account.getString(TWIT_ACCESS_TOKEN);
		} catch(Exception e) {}
		return null;
	}
	public void setAccessToken(String acessToken) {
		try {
			account.put(TWIT_ACCESS_TOKEN, acessToken);
		} catch(Exception e) {}
	}
	public String getAccessTokenSecret() {
		try {
			return account.getString(TWIT_ACCESS_TOKEN_SECRET);
		} catch(Exception e) {}
		return null;
	}
	public void setAccessTokenSecret(String acessTokenSecret) {
		try {
			account.put(TWIT_ACCESS_TOKEN_SECRET, acessTokenSecret);
		} catch(Exception e) {}
	}
}
