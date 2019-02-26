package run.brief.beans;

import run.brief.util.json.JSONObject;

public class DataPacket extends BJSONBean {
	
	public static final int TYPE_MESSAGE=5;
	public static final int TYPE_FILE=50;
	
	
	public static final String INT_TYPE_="dpid";
	
	public static final String STRING_FROM_USERKEY="dpfid";
	public static final String STRING_AUTHKEY="dpfau";
	public static final String STRING_MSGBODY="dpfb";
	public static final String JSONOB_FILES="dpp";
	
	public class DataPacketFile extends BJSONBean {
		public static final String STRING_FILENAME="dpfn";
		public static final String STRING_DATA="dpfd";
	}
}
