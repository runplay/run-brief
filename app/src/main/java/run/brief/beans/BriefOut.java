package run.brief.beans;

import java.util.ArrayList;

public class BriefOut extends BriefWith {


	
	public static final int STATUS_NOT_SENT=0;
	public static final int STATUS_NOT_SENT_ERROR=1;
	public static final int STATUS_SENT_OK=2;
	
	
	private String to;
	
	private String from;
	private int with;
	private int status;
	private String statusMessage;
	private boolean confirmed;
	
	public BriefOut(int WITH_, String from, String to) {
		this.to=to;
		this.from=from;
		this.with=WITH_;
	}
	
	public boolean isConfirmed() {
		return confirmed;
	}

	public void setConfirmed(boolean confirmed) {
		this.confirmed = confirmed;
	}


	
	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}


	
	public String getTo() {
		return to;
	}


	public String getFrom() {
		return from;
	}


	public int getWith() {
		return with;
	}



}
