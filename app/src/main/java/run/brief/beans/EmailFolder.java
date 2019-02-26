package run.brief.beans;

public class EmailFolder extends BJSONBean {

	public static final String STRING_FOLDERNAME="name";
    public static final String BOOLEAN_INCLUDE_SYNC="sync";
	public static final String INT_COUNT_EMAILS="cemail";
	public static final String INT_COUNT_UNREAD="uemail";
	public static final String INT_COUNT_NEW="nemail";
	
	public EmailFolder(String foldername, int emailcount,int unreadcount, int newcount, boolean includeInSync) {
		setString(EmailFolder.STRING_FOLDERNAME, foldername);
		setInt(EmailFolder.INT_COUNT_EMAILS, emailcount);
		setInt(EmailFolder.INT_COUNT_UNREAD, unreadcount);
		setInt(EmailFolder.INT_COUNT_NEW, newcount);
        setBoolean(BOOLEAN_INCLUDE_SYNC,includeInSync);
	}


}
