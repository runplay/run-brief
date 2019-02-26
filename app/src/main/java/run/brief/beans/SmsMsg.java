package run.brief.beans;

import run.brief.util.Cal;

public class SmsMsg {


	private String messageNumber, messageContent;
    private Cal messageDate;
    private String id;
    private String threadId;
    private String serviceCenter;
    private int status;

	private int type;
    private boolean isMine=false;
    private int read=0;

	private int protocol;
    private String person;
    private String replyPathPresent;
    private String subject;
    private int locked;
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
    public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public String getPerson() {
		return person;
	}

	public void setPerson(String person) {
		this.person = person;
	}

	public String getReplyPathPresent() {
		return replyPathPresent;
	}

	public void setReplyPathPresent(String replyPathPresent) {
		this.replyPathPresent = replyPathPresent;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public int getLocked() {
		return locked;
	}

	public void setLocked(int locked) {
		this.locked = locked;
	}
    public String getServiceCenter() {
		return serviceCenter;
	}

	public void setServiceCenter(String serviceCenter) {
		this.serviceCenter = serviceCenter;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getRead() {
		return read;
	}

	
	public String getThreadId() {
		return threadId;
	}

	public void setThreadId(String threadId) {
		this.threadId = threadId;
	}
	public boolean isRead() {
		return read==0?false:true;
	}

	public void setRead(int read) {
		this.read = read;
	}

	public boolean isMine() {
		return isMine;
	}

	public void setMine(boolean isMine) {
		this.isMine = isMine;
	}
    public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	public String getMessageNumber() {
		return messageNumber;
	}

	public void setMessageNumber(String messageNumber) {
		this.messageNumber = messageNumber;
	}

	public String getMessageContent() {
		return messageContent;
	}

	public void setMessageContent(String messageContent) {
		this.messageContent = messageContent;
	}

	public Cal getMessageDate() {
		return messageDate;
	}

	public void setMessageDate(Cal messageDate) {
		this.messageDate = messageDate;
	}


}
