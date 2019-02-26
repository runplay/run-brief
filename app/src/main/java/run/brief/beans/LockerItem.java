package run.brief.beans;

import java.io.File;

import run.brief.util.Files;
import run.brief.util.json.JSONObject;

public final class LockerItem extends BJSONBean {

	public static final String LONG_DATE_CREATE="dte";
	public static final String STRING_ORIGIN_PATH="opa";
	public static final String STRING_LOCKER_FILE_NAME="lfn";
	public static final String STRING_ENCRYPT_KEY="ek";
	public static final String LONG_DATE_TMP_UNLOCK="tmp";
	public static final String LONG_ORIGN_FILE_SIZE="ofs";
	public static final String INT_ENC_BYTES_="encb";
	
	public static final int ENC_BYTES_32=32;
	public static final int ENC_BYTES_256=256;
	public static final int ENC_BYTES_1024=1024;
	public static final int ENC_BYTES_2048=2048;

    private File file;
	
	public LockerItem(JSONObject lockerItemJson) {
		super(lockerItemJson);
	}
	public LockerItem() {
		super();
	}

    public long getLockerFileSize() {
        if(getFile().exists())
            return getFile().length();
        return 0L;
    }
    public long getLockerFileLoastModified() {
        if(getFile().exists())
            return getFile().lastModified();
        return 0L;
    }
    public boolean exists() {
        //File f = new File());
        if(getFile().exists())
            return true;
        return false;
    }
    private File getFile() {
        if(file==null)
            file=new File(Files.HOME_PATH_LOCKER+File.separator+ Files.FOLDER_LOCKER+File.separator+getString(LockerItem.STRING_LOCKER_FILE_NAME));
        return file;
    }
}
