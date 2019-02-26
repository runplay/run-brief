package run.brief.beans;

public class BriefObject {
	
	public static final int TYPE_FILE_SD=0;
	
	public static final int TYPE_FILE_DROPBOX=5;
	public static final int TYPE_FILE_GOOGLEDRIVE=6;
	
	private String uri;
	private int type;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}
}
