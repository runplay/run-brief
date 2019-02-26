package run.brief.beans;

import run.brief.util.json.JSONObject;

public class SmsSend extends BJSONBean {
	public static final String STRING_TO_NUMBER = "to";
	public static final String STRING_MESSAGE = "msg";

	
	public SmsSend() {
		super();
	}
	public SmsSend(JSONObject job) {
		this.bean=job;
	}
	/*
	public SmsMsg getAsSmsMsg() {
		SmsMsg msg = new SmsMsg();
		msg.setMessageContent(getString(STRING_MESSAGE));
		
	}
	*/
}
