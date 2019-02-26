package run.brief.beans;

import java.io.File;
import java.util.ArrayList;

import run.brief.util.json.JSONObject;

public class Email extends BJSONBean {
	public static final String LONG_ID="id";
	public static final String STRING_UUID="uuid";
    public static final String STRING_XBRIEFID="xbid";
	public static final String STRING_FROM="efrom";
	public static final String STRING_FOLDER="folder";
	public static final String STRING_TO="eto";
	public static final String STRING_SUBJECT="subject";
	public static final String STRING_MESSAGE="message";
	public static final String STRING_MESSAGE_HTML="messageHtml";
	public static final String STRING_ATTACHMENTS="attach";
	public static final String LONG_DATE="msgdate";
	public static final String LONG_MSG_SIZE="msgsize";
	public static final String INT_COLLECTED="collected";
	public static final String INT_STATE="state";
	public static final String INT_PRIORITY="priority";
    public static final String INT_DELETED="del";



    public static final String BOOL_IS_MINE_NO_SAVE="ismine";

    public static final String LONG_REPY_TO_INDEX_NO_SAVE="repltoid";
	
	public static String FOLDER_INBOX="INBOX";
	public static String FOLDER_SENT="SENT";
	
	public Email() {
		super();
	}
	
	public Email(JSONObject job) {
		super(job);
	}
	
	public boolean okForSend() {
		if(this.bean.has(STRING_TO)
			&& this.bean.has(STRING_SUBJECT)
			&& this.bean.has(STRING_MESSAGE)
			) {
			return true;
		}
		return false;
	}
	public void addAttachment(String filepath) {
        if(filepath!=null && filepath.length()>2) {
            String files = this.getString(Email.STRING_ATTACHMENTS);
            if (files == null || files.length() < 1)
                files = "[" + filepath + "]";
            else
                files += ",[" + filepath + "]";
            this.setString(Email.STRING_ATTACHMENTS, files);
        }
	}
	public ArrayList<File> getAttachmentsAsFiles() {
		ArrayList<File> files = new ArrayList<File>();
        String attachstr=this.getString(Email.STRING_ATTACHMENTS);
        if(attachstr!=null && attachstr.length()>0) {
            String[] fnames = attachstr.split(",");
			if(fnames!=null && fnames.length>0) {
				for(int i=0; i<fnames.length; i++) {
					String fname = fnames[i].length()>2?fnames[i].substring(1,fnames[i].length()-1):"";
					//BLog.e("STRIPFNAME", "fname: "+fname);
					if(fname.length()>0) {
						File f = new File(fname);
						if(f.exists()) {
							files.add(f);
						}
					}
				}
			}
			
		}
		
		return files;
	}
	public ArrayList<String> getAttachments() {
		ArrayList<String> files = new ArrayList<String>();
        String attachstr=this.getString(Email.STRING_ATTACHMENTS);
		if(attachstr!=null && attachstr.length()>0) {
			String[] fnames = attachstr.split(",");
			if(fnames!=null && fnames.length>0) {
				for(int i=0; i<fnames.length; i++) {
					String fname = fnames[i].length()>2?fnames[i].substring(1,fnames[i].length()-1):"";
					files.add(fname);

				}
			}
			
		}
		
		return files;
	}
}
