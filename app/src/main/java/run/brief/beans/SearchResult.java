package run.brief.beans;

import android.content.Context;

public class SearchResult extends Brief {
	public SearchResult(Context context, SmsMsg sms, int itemDbIndex) {
		super(context,sms,itemDbIndex);
	}
	public SearchResult(RssItem news, int itemDbIndex) {
		super(news,itemDbIndex);
	}
	public SearchResult(Note note, int itemDbIndex) {
		super(note,itemDbIndex);
	}
    public SearchResult(Context context, Account account, Email email, int itemDbIndex) {
        super(context,account,email,itemDbIndex);
    }
	public SearchResult() {

	}
	/*
	private int WITH_;
	private int TYPE_;
	private int index;
	private String key;
	private String resultText;
	private String resultHead;
	public int getWITH_() {
		return WITH_;
	}
	public void setWITH_(int wITH_) {
		WITH_ = wITH_;
	}
	public int getTYPE_() {
		return TYPE_;
	}
	public void setTYPE_(int tYPE_) {
		TYPE_ = tYPE_;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getResultText() {
		return resultText;
	}
	public void setResultText(String resultText) {
		this.resultText = resultText;
	}
	public String getResultHead() {
		return resultHead;
	}
	public void setResultHead(String resultHead) {
		this.resultHead = resultHead;
	}

	*/
}
