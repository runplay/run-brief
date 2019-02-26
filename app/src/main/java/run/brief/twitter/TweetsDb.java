package run.brief.twitter;



import run.brief.util.json.JSONArray;

public class TweetsDb {
	private static final TweetsDb DB = new TweetsDb();
	
	private JSONArray data;
	private boolean isInitialised=false;
	
	private static final String DB_ROOT="twitter";
	
	public TweetsDb() {
		
	}
	
	public static boolean isInitialised() {
		return DB.isInitialised;
	}
	public static boolean hasAccounts() {
		return DB.data.length()>0;
	}
	/*
	public static boolean Save() {
		if(DB.data.length()!=0) {
		JSONObject db = new JSONObject();
		try {
			db.put(DB_ROOT, DB.data);
			Files.WriteSecureToSd(Files.HOME_PATH_APP, Files.FILENAME_TWITTER_ACCOUNTS, db.toString());
			return true;
		} catch(Exception e) {}
		}
		return false;
	}
	public static void init() {
		if(DB.data==null) {
			String file = Files.ReadSecureFromSd(Files.HOME_PATH_APP, Files.FILENAME_TWITTER_ACCOUNTS);
			if(file!=null && file.length()>0) {
				try {
					JSONObject db = new JSONObject(file);
					if(db!=null) {
						DB.data = db.getJSONArray(DB_ROOT);
						DB.isInitialised=true;
					}
				} catch(JSONException e) {}
			}
		}
		if(!DB.isInitialised) {
			DB.data = new JSONArray();
			DB.isInitialised=true;
		}
	}

	public static JSONArray getAllAccounts() {
		return DB.data;
	}
	public static int Size() {
		if(DB.isInitialised)
			return DB.data.length();
		else 
			return 0;
	}
	public static TwitterAccount getAccount(int index) {
		if(DB.isInitialised) {
			TwitterAccount account = null;
			try {
				account = (TwitterAccount) DB.data.get(index);
				return account;
			} catch(Exception e) {}
		}
		return null;
	}
	public synchronized static boolean updateAccount(int index, TwitterAccount account) {
		if(DB.isInitialised) {
		if(DB.data.length()<index) {
			// out of index, return a did not update of -1
			return false;			
		}
		try {
			DB.data.put(index,account);
			return Save();
		} catch(Exception e) {}
		}
		return true;
	}
	public synchronized static int addAccount(TwitterAccount account) {
		if(DB.isInitialised) {
		try {
			DB.data.put(DB.data.length(),account);
			Save();
			return DB.data.length()-1;
		} catch(Exception e) {}
		}
		return -1;
	}
		*/
}
