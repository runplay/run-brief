package run.brief.settings;

import run.brief.beans.BriefUserStats;
import run.brief.util.log.BLog;
import run.brief.util.FileReadTask;
import run.brief.util.FileWriteTask;
import run.brief.util.Files;
import run.brief.util.json.JSONObject;

public final class UserStatsDb {
	
	private static final UserStatsDb DB = new UserStatsDb();
	
	private static final String dbArrayName="stats";
	
	private BriefUserStats stats;
	private boolean isLoaded=false;
	
	private static FileWriteTask fwt;
	private static FileReadTask frt;
	

	public static FileWriteTask getFwt() {
		return fwt;
	}

	public static FileReadTask getFrt() {
		return frt;
	}

	private UserStatsDb() {
		//Load();
	}
	
	public static BriefUserStats getUserStats() {
		return DB.stats;
	}

	public static void Update(BriefUserStats stats) {
		if(stats!=null)
			DB.stats=stats;
	}
	
	public static boolean Save() {
		if(DB.isLoaded) {
		try {

			fwt=new FileWriteTask(Files.HOME_PATH_APP, Files.FILENAME_USER_STATS, DB.stats.getBean().toString());
			return fwt.WriteSecureToSd();

		} catch(Exception e) {
			BLog.e("SAVE",e.getMessage());
			
		}
		}
		return false;
	}
	public static void init() {
		
		if(DB.stats==null) {
			
			frt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_USER_STATS);
			if(frt.ReadSecureFromSd()) {
				//BLog.e("SETTINGS","--"+frt.getFileContent());
				if(frt.getFileContent()!=null && !frt.getFileContent().isEmpty()) {
				try {
					JSONObject db = new JSONObject(frt.getFileContent());
					if(db!=null) {
						DB.stats = new BriefUserStats(db); 
						DB.isLoaded=true;  
					}
				} catch(Exception e) {
					//if(e.getMessage()!=null)
						//BLog.e("SettingsDb.init()",e.getMessage());
				}
				} else {
					//BLog.e("SettingsDb.init().empty",frt.getStatusMessage());
				}
			} else {
				//BLog.e("SettingsDb.init().no read",frt.getStatusMessage());
			}
		}
		if(!DB.isLoaded) {
			DB.stats = new BriefUserStats();
			//DB.stats.setLong(BriefUserStats.LONG_DATE_STARTED, (new Date()).getTime());
			DB.stats.setInt(BriefUserStats.INT_COUNT_LAUNCH, 0);
			DB.isLoaded=true;
			
		}
		DB.stats.setInt(BriefUserStats.INT_COUNT_LAUNCH, DB.stats.getInt(BriefUserStats.INT_COUNT_LAUNCH)+1);
		Save();
		
	}
}
