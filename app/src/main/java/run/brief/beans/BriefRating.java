package run.brief.beans;

public class BriefRating extends BJSONBean {
	public static String STRING_ITEM_IDENTIFIER="id";
	public static String INT_RATING="rate";
	public static String INT_MAX_HIT_RATING="maxrate";
	public static String LONG_DATE="dte";
	
	public static final int RATING_IGNORE=-1;
	public static final int RATING_NONE=0;
	public static final int RATING_STAR=1;
	public static final int RATING_STAR_DOUBLE=2;
	

	public static String makeRatingsIdentifier(int WITH_,String itemDbId, long accountid) {
		return new StringBuilder().append(WITH_).append("-").append(accountid).append("-").append(itemDbId).toString();
	}
	

}
