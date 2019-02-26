package run.brief.beans;

import run.brief.util.json.JSONObject;

public class Phonecall extends BJSONBean {
	public static final String STRING_ID="id";
	public static final String STRING_NAME="name";
	public static final String LONG_DATE="dte";
	public static final String STRING_NUMBER="num";
	public static final String INT_TYPE="inout";
	public static final String INT_DURATION="dur";

	public static final int TYPE_IN=0;
	public static final int TYPE_OUT=2;
	public static final int TYPE_MISSED=3;


}