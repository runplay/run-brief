package run.brief.email;

import android.app.Activity;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import run.brief.R;
import run.brief.beans.Account;
import run.brief.beans.SignatureBean;
import run.brief.news.NewsFiltersDb;
import run.brief.util.Cal;
import run.brief.util.FileReadTask;
import run.brief.util.FileWriteTask;
import run.brief.util.Files;
import run.brief.util.Sf;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONException;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;

public final class SignatureDb {
	
	private static final SignatureDb SIG= new SignatureDb();
	
	
	private static final String SIGNATURE_DEFAULT="sigs";

	
	private List<SignatureBean> signatures=new ArrayList<SignatureBean>();
	
	private boolean isLoaded=false;
	private Context context;
	
	
	private static FileWriteTask fwt;
	private static FileReadTask frt;



	
	private static FileWriteTask getFeedsFwt() {
		return fwt;
	}

	private static FileReadTask getFeedsFrt() {
		return frt;
	}

	public SignatureDb() {
		//Load();
	}
	public static int size() {
		return SIG.signatures.size();
	}

	public String getDefaultSignature(Activity activity) {
        return activity.getString(R.string.email_signature)+ Sf.NEW_LINE+activity.getString(R.string.email_signature_url);
    }

	//public static ArrayList<RssFeed> getAllFeeds(int category) {
	//	return SIG.feeds;
	//}
    public static void addSignature(SignatureBean signature) {
        signature.setLong(SignatureBean.LONG_ID, Cal.getCal().getTimeInMillis());
        SIG.signatures.add(signature);
        Save();
    }
    public static void deleteSignature(SignatureBean signature) {
        SIG.signatures.remove(signature);
        Save();
    }
    public static void updateSignature(SignatureBean signature) {
        for(int i=0; i<SIG.signatures.size(); i++) {
            SignatureBean si = SIG.signatures.get(i);
            //BLog.e("COMP","compare for update: "+si.getLong(SignatureBean.LONG_ID)+"="+signature.getLong(SignatureBean.LONG_ID));
            if(si.getLong(SignatureBean.LONG_ID)==signature.getLong(SignatureBean.LONG_ID))
                SIG.signatures.set(i,signature);
        }
        Save();
    }
    public static SignatureBean getSignature(long signatureId) {
        List<SignatureBean> asig = new ArrayList<SignatureBean>();
        for(SignatureBean si: SIG.signatures) {
            if(si.getLong(SignatureBean.LONG_ID)==signatureId)
                return si;
        }
        return null;
    }
	public static List<SignatureBean> getSignatures(Account account) {
        List<SignatureBean> asig = new ArrayList<SignatureBean>();
        for(SignatureBean si: SIG.signatures) {
            if(si.getLong(SignatureBean.LONG_ACCOUNT_ID)==account.getLong(Account.LONG_ID))
                asig.add(si);
        }
        return asig;
	}




	public static boolean Save() {
		if(SIG.isLoaded) {
			JSONObject db = new JSONObject();
			try {
				//db.put(SIG_DEFAULT_LAST_REFRESH,Long.valueOf(SIG.lastServerRefesh));
				JSONArray array = new JSONArray();
				

				for(SignatureBean sb: SIG.signatures) {
                    array.put(sb.getBean());
				}
				

				db.put(SIGNATURE_DEFAULT, array);

                //BLog.e("SIGS","__"+db.toString());

				fwt=new FileWriteTask(Files.HOME_PATH_APP, Files.FILENAME_FILE_SIGNATURES, db.toString());
				fwt.WriteToSd();
				//return Files.WriteToSd(Files.HOME_PATH, Files.FILENAME_NOTES_DATABASE, db.toString());
			} catch(Exception e) {
				BLog.add("fwt secure: "+e.getMessage());
			}
			
			if(fwt.getStatus()==FileWriteTask.STATUS_WRITE_OK)
				return true;		
		}
		return false;
	}

	public static synchronized boolean init(Context context) {
        NewsFiltersDb.init();
		SIG.context=context;
		if(!SIG.isLoaded) {
			//BLog.e("RssFeedsDb.init()1.5","feeds IS NULL");
			frt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_FILE_SIGNATURES);
			if(frt.ReadFromSd()) {
                //BLog.e("RssFeedsDb.init()1.5","has json object");
				try {
					JSONObject db = new JSONObject(frt.getFileContent());
					JSONArray jsignatures = db.getJSONArray(SIGNATURE_DEFAULT);
					if(jsignatures!=null) {

						SIG.signatures.clear();
						

						//if(feeds!=null) {
                        for(int i=0; i<jsignatures.length(); i++) {
                            //RssFeed feed = new RssFeed(feeds.getJSONObject(i));
                            SignatureBean si = new SignatureBean(jsignatures.getJSONObject(i));

                            SIG.signatures.add(si);
                            //SIG.feeds.add();
                        }
                        //BLog.e("SIGS","__"+SIG.signatures.toString());
						SIG.isLoaded=true;
					}
				} catch(JSONException e) {

				}
			} else {
				BLog.e("Rss",frt.getStatusMessage());
			}


		} else {
			//BLog.e("RssFeedsDb","news feeds loaded already");
			SIG.isLoaded=true;
			//SIG.STATE=STATE_OK;
		}
		if(!SIG.isLoaded) {
			SIG.isLoaded=true;
		} else {

		}
		return true;
	}
	

	
	/*
	private class UserFeedsDb extends Db {

		
		public UserFeedsDb(Activity activity) {
			super("news_stats",NewsFeedsDb.TABLE_FIELDS,activity);
			this.activity=activity;
			//this.TABLE_NAME=TABLE_NAME;
			//this.TABLE_FIELDS=TABLE_FIELDS;
			ensureTable(activity);
			
			
		}
		//public HashMap<String,RssFeedStats> getFeedStatsForStore() {
			
		//}
		public RssUserFeed getUserFeed(String feedUrl) {
			RssUserFeed stats=null;
				
			Cursor cursor = db.query(TABLE_NAME, getFieldNames(), null, null, null, null, null);
	        if (cursor != null) {
	        	cursor.moveToFirst();
	            //cursor.moveToLast();
	            if (cursor.getCount() > 0) {
	            	stats = getUserFeedFromCursor(cursor);
	            }
	            cursor.close();
	        }

			return stats;
		}
		public boolean updateUserFeed(RssUserFeed feedStats) {
			open();
			ContentValues values = new ContentValues();
		    values.put(RssUserFeed.STRING_URL, feedStats.getString(RssUserFeed.STRING_URL));
		    values.put(RssUserFeed.INT_ARTICLE_READ_COUNT, feedStats.getInt(RssUserFeed.INT_ARTICLE_READ_COUNT));
		    values.put(RssUserFeed.LONG_LAST_UPDATE, feedStats.getLong(RssUserFeed.LONG_LAST_UPDATE));
		    
		    long id = db.update(TABLE_NAME, values, RssUserFeed.STRING_URL+ "=?",new String[]{feedStats.getString(RssUserFeed.STRING_URL)});
		    
		    if(id!=0)
		    	return true;
		    else 
		    	return false;
		} 
		public boolean addUserFeed(RssUserFeed feedStats) {
			open();
			ContentValues values = new ContentValues();
		    values.put(RssUserFeed.STRING_URL, feedStats.getString(RssUserFeed.STRING_URL));
		    values.put(RssUserFeed.INT_ARTICLE_READ_COUNT, feedStats.getInt(RssUserFeed.INT_ARTICLE_READ_COUNT));
		    values.put(RssUserFeed.LONG_LAST_UPDATE, feedStats.getLong(RssUserFeed.LONG_LAST_UPDATE));
		    
		    long id = db.insert(TABLE_NAME, null, values);
		    
		    if(id!=0)
		    	return true;
		    else 
		    	return false;
		} 
		private RssUserFeed getUserFeedFromCursor(Cursor cursor) {
			RssUserFeed stats = new RssUserFeed(new JSONObject());
			stats.setString(RssUserFeed.STRING_URL, cursor.getString(cursor.getColumnIndex(RssUserFeed.STRING_URL)));
			stats.setLong(RssUserFeed.LONG_LAST_UPDATE, cursor.getLong((cursor.getColumnIndex(RssUserFeed.LONG_LAST_UPDATE))));
			stats.setInt(RssUserFeed.INT_ARTICLE_READ_COUNT, cursor.getInt(cursor.getColumnIndex(RssUserFeed.INT_ARTICLE_READ_COUNT)));
			
			return stats;
		}
	}
	*/

}
