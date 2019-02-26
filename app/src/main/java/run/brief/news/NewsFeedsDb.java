package run.brief.news;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import run.brief.beans.RssCategory;
import run.brief.beans.RssFeed;
import run.brief.beans.RssUserFeed;
import run.brief.util.DbField;
import run.brief.util.FileReadTask;
import run.brief.util.FileWriteTask;
import run.brief.util.Files;
import run.brief.util.TextFile;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;


public class NewsFeedsDb {
	
	private static final NewsFeedsDb NEWS = new NewsFeedsDb();
	
	
	public static final String NEWS_DEFAULT_FEEDS="rss";
	public static final String NEWS_DEFAULT_CATEGORY="cats";
	public static final String NEWS_DEFAULT_LAST_REFRESH="reflast";
	
	public static final int STATE_ERROR=0;
	public static final int STATE_OK=1;
	public static final int STATE_LOADING_FROM_REMOTE=2;
	public static final int STATE_LOADING=3;
	
	private int STATE;
	private HashMap<Integer,ArrayList<RssFeed>> feeds;
	private UserFeedDbTable userfeedsdb;
	//private long lastServerRefesh;
	
	private ArrayList<RssCategory> cats;
	private HashMap<String,RssUserFeed> userfeeds;
	
	private boolean isLoaded=false;
	private Context context;
	public DoLoadFromHost doload;
	
	private static FileWriteTask fwt;
	private static FileReadTask frt;
	private static FileWriteTask cwt;
	private static FileReadTask crt;
	
	
	private static final DbField[] TABLE_FIELDS={
		new DbField(RssUserFeed.STRING_URL, DbField.FIELD_TYPE_TEXT,true,false),
		new DbField(RssUserFeed.LONG_LAST_UPDATE, DbField.FIELD_TYPE_INT),
		new DbField(RssUserFeed.INT_ARTICLE_READ_COUNT, DbField.FIELD_TYPE_INT)
	};
	
	private static FileWriteTask getFeedsFwt() {
		return fwt;
	}

	private static FileReadTask getFeedsFrt() {
		return frt;
	}
	private static FileWriteTask getCategoryFwt() {
		return cwt;
	}

	private static FileReadTask getCategoryFrt() {
		return crt;
	}
	public NewsFeedsDb() {
		//Load();
	}
	public static int getUserFeedsSize() {
		return NEWS.userfeeds.size();
	}
	public static void updateUserFeed(RssUserFeed feed) {
		NEWS.userfeeds.put(feed.getString(RssUserFeed.STRING_URL), feed);
		NEWS.userfeedsdb.update(feed);
	}
	public static void saveUserFeed(RssUserFeed feed) {
		if(feed!=null && feed.has(RssUserFeed.STRING_URL)) {
			
			NEWS.userfeeds.put(feed.getString(RssUserFeed.STRING_URL), feed);
			NEWS.userfeedsdb.add(feed);
		}
	}
	public static void updateErrorCollectCount(RssUserFeed item) {
		//ContentValues values = new ContentValues();
		//values.put(RssUserFeed.INT_ERROR_COLLECT_COUNT, item.getInt(RssUserFeed.INT_ERROR_COLLECT_COUNT));

		NEWS.userfeedsdb.updateErrorCollectCount(item);//.update(TABLE_NAME, values, RssItem.STRING_URL+"=?", new String[]{item.getString(RssUserFeed.STRING_URL)});

	}
	public static void resetErrorCollectCount(RssUserFeed item) {
		//ContentValues values = new ContentValues();
		//values.put(RssUserFeed.INT_ERROR_COLLECT_COUNT, 0);

		NEWS.userfeedsdb.resetErrorCollectCount(item);//TABLE_NAME, values, RssItem.STRING_URL+"=?", new String[]{item.getString(RssUserFeed.STRING_URL)});

	}
	public static void removeUserFeed(RssUserFeed feed) {
		if(feed!=null && feed.has(RssUserFeed.STRING_URL)) {
			NEWS.userfeeds.remove(feed.getString(RssUserFeed.STRING_URL));
			NEWS.userfeedsdb.delete(feed);
		}
	}
	public static RssUserFeed getUserFeed(RssFeed feed) {
			return NEWS.userfeeds.get(feed.getString(RssFeed.STRING_URL));
	}
	
	public static void setState(int STATE_) {
		NEWS.STATE=STATE_;
	}
	
	//public static ArrayList<RssFeed> getAllFeeds(int category) {
	//	return NEWS.feeds;
	//}
	public static final ArrayList<RssFeed> getFeeds(int category) {
		//BLog.e("current feeds size: "+NEWS.feeds.size());
		ArrayList<RssFeed> cfeeds = NEWS.feeds.get(category);
		if(cfeeds==null)
			cfeeds=new ArrayList<RssFeed>();
		return cfeeds;
	}
	public static ArrayList<RssCategory> getAllCategories() {
		return NEWS.cats;
	}
	public static HashMap<String,RssUserFeed> getUserFeeds() {
		return NEWS.userfeeds;
	}
	public static ArrayList<RssUserFeed> getUserFeedsArray() {
		ArrayList<RssUserFeed> userfeeds = new ArrayList<RssUserFeed>();
		HashMap<String,RssUserFeed> mapuser= NewsFeedsDb.getUserFeeds();
		if(mapuser!=null && !mapuser.isEmpty()){
			Iterator<String> it = mapuser.keySet().iterator();
			while(it.hasNext()) {
				RssUserFeed feed=mapuser.get(it.next());
				//BLog.e("USER_FEEDS", "FEED: " + feed.getInt(RssUserFeed.INT_CUSTOM) + " - " + feed.getString(RssUserFeed.STRING_URL));
				//if(feed.getInt(RssUserFeed.INT_CUSTOM)!=0)
				userfeeds.add(feed);
			}
		}
		return userfeeds;
	}
	public static int FeedsSize(int category) {
		if(NEWS.isLoaded) {
			ArrayList<RssFeed> cfeeds = NEWS.feeds.get(category);
			if(cfeeds!=null)
				return cfeeds.size();
		}
		return 0;
	}
	public static int CategoriesSize() {
		if(NEWS.isLoaded)
			return NEWS.cats.size();
		else 
			return 0;
	}
	public static RssFeed getFeed(int category, int index) {
		if(NEWS.isLoaded) {
			RssFeed feed = null;
			try {
				feed = NEWS.feeds.get(category).get(index);
				return feed;
			} catch(Exception e) {}
		}
		return null;
	}

	public static RssCategory getCategory(int index) {
		if(NEWS.isLoaded) {
			RssCategory cat = null;
			try {
				cat = NEWS.cats.get(index);
				return cat;
			} catch(Exception e) {}
		}
		return null;
	}

	public static void setFeeds(HashMap<Integer,ArrayList<RssFeed>> feeds) {
		NEWS.feeds= feeds;
	}
	public static void setFeeds(int category, ArrayList<RssFeed> feeds) {
		NEWS.feeds.put(Integer.valueOf(category), feeds);
	}
	public static void setCategories(ArrayList<RssCategory> categories) {
		NEWS.cats=categories;
	}
	public static boolean Save() {
		if(NEWS.isLoaded) {
			JSONObject db = new JSONObject();
			try {
				//db.put(NEWS_DEFAULT_LAST_REFRESH,Long.valueOf(NEWS.lastServerRefesh));
				JSONArray array = new JSONArray();
				
				Set<Integer> keys = NEWS.feeds.keySet();
				for(Integer key: keys) {
				//for(int i=0; i<NEWS.feeds.size(); i++) {
					ArrayList<RssFeed> feeds = NEWS.feeds.get(key);
					if(feeds!=null && !feeds.isEmpty()) {
						for(RssFeed feed: feeds) {
							array.put(feed.getBean());
						}
					}
				}
				

				db.put(NEWS_DEFAULT_FEEDS, array);
				
				fwt=new FileWriteTask(Files.HOME_PATH_APP, Files.FILENAME_RSS_FEEDS, db.toString());
				fwt.WriteSecureToSd();

			} catch(Exception e) {
				BLog.add("fwt secure: " + e.getMessage());
			}
			JSONObject cdb = new JSONObject();
			try {
				JSONArray array = new JSONArray();
				for(RssCategory cat: NEWS.cats) {
					array.put(cat.getBean());
				}
				cdb.put(NEWS_DEFAULT_FEEDS, array);
				cwt=new FileWriteTask(Files.HOME_PATH_APP, Files.FILENAME_RSS_CATEGORY, cdb.toString());
				cwt.WriteSecureToSd();

			} catch(Exception e) {
				BLog.add("crt secure: " + e.getMessage());
			} 
			
			if(cwt.getStatus()== FileWriteTask.STATUS_WRITE_OK && fwt.getStatus()== FileWriteTask.STATUS_WRITE_OK)
				return true;		
		}
		return false;
	}
	public static int getState() {
		return NEWS.STATE;
	}
	public static synchronized int init(Context context) {
        NewsFiltersDb.init();
		NEWS.context=context;
		if(NEWS.userfeedsdb==null) {
			NEWS.userfeedsdb=new UserFeedDbTable(context);
			NEWS.userfeeds=NEWS.userfeedsdb.getUserFeedsHash();
		}
		if(NEWS.feeds==null) {
			goLoad(context);
			NEWS.isLoaded=true;
			/*
			BLog.e("RssFeedsDb.init()1.5","feeds IS NULL");
			NEWS.STATE=STATE_LOADING;
			frt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_RSS_FEEDS);
			if(frt.ReadSecureFromSd()) {
				boolean fetchfromremote=false;
				try {
					JSONObject db = new JSONObject(frt.getFileContent());
					JSONArray feeds = db.getJSONArray(NEWS_DEFAULT_FEEDS);
					if(feeds!=null) {
						//BLog.e("RssFeedsDb.init()1.5","has json object");
						populateWithFeeds(feeds);
						NEWS.STATE=STATE_LOADING;


						NEWS.isLoaded=true;
					} else {
						//BLog.e("RssFeedsDb.init()1.5","FAILED has json object");
						fetchfromremote=true;
					}
				} catch(JSONException e) {
					
					fetchfromremote=true;
					
					
				}
				if(fetchfromremote) {
					BLog.e("RssFeedsDb.init()1.5","Fetch from remote");
					DoLoadFromHost.goLoad(context);
					//NewsFeedsDb.FetchNewsFromRemote();
					NEWS.STATE = STATE_LOADING_FROM_REMOTE;
				}
			} else {
				BLog.e("Rss", frt.getStatusMessage());
			}
			
			if(NEWS.STATE==STATE_LOADING) {
			
			crt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_RSS_CATEGORY);
			if(crt.ReadSecureFromSd()) {
				try {
					JSONObject db = new JSONObject(crt.getFileContent());
					if(db!=null) {
						NEWS.cats = new ArrayList<RssCategory>();
						JSONArray cats = db.getJSONArray(NEWS_DEFAULT_FEEDS);
						if(cats!=null) {
							for(int i=0; i<cats.length(); i++) {
								NEWS.cats.add(new RssCategory(cats.getJSONObject(i)));
							}
						}
						//NEWS.isLoaded=true;
					}
				} catch(JSONException e) {
					BLog.e("Rss", e.getMessage());
				}
			} else {
				BLog.e("Rss", crt.getStatusMessage());
			}
			if(crt.getStatus()== FileWriteTask.STATUS_WRITE_OK && frt.getStatus()== FileWriteTask.STATUS_WRITE_OK) {
				NEWS.isLoaded=true;
				NEWS.STATE=STATE_OK;
			}

			
			}
			
			*/
			// load any existing feed stats
			
			

		} else {
			//Files.setAppHomePath(context);

			//BLog.e("RssFeedsDb","news feeds loaded already");
			NEWS.isLoaded=true;
			//NEWS.STATE=STATE_OK;
		}
		if(!NEWS.isLoaded) {
			NEWS.feeds = new HashMap<Integer,ArrayList<RssFeed>>();
			NEWS.cats = new ArrayList<RssCategory>();
			NEWS.isLoaded=true;
		} else {

		}
		return NEWS.STATE;
	}
	private static JSONObject acats;
	private static JSONObject afeeds;
	public static void goLoad(Context context) {
		boolean catload=false;
		boolean feedload=false;

		loadNews(context);

		NEWS.feeds=new HashMap<Integer,ArrayList<RssFeed>>();
		NEWS.cats=new ArrayList<RssCategory>();
		//ArrayList<RssCategory> acats=new ArrayList<RssCategory>();
		try {
			JSONArray cl = acats.getJSONArray(NewsFeedsDb.NEWS_DEFAULT_CATEGORY);
			if(cl!=null) {
				for(int i=0; i<cl.length(); i++) {
					//BLog.e("ok: "+cl.length());
					NEWS.cats.add(new RssCategory(cl.getJSONObject(i)));
					//DB.cats.add(new RssCategory(cl.getJSONObject(i)));
				}
			}
			catload=true;
		} catch(Exception e) {
			BLog.e("DoLoadFromHost.onPostExecute()",e.getMessage());

		}
		//if(feeds!=null) {
		try {
			//DB.feeds.clear();
			JSONArray fl = afeeds.getJSONArray(NewsFeedsDb.NEWS_DEFAULT_FEEDS);
			if(fl!=null) {

				for(int i=0; i<fl.length(); i++) {
					RssFeed feed = new RssFeed(fl.getJSONObject(i));
					ArrayList<RssFeed> feedlist = NEWS.feeds.get(feed.getInt(RssFeed.INT_CATEGORY));
					if(feedlist==null)
						feedlist=new ArrayList<RssFeed>();
					feedlist.add(feed);
					//BLog.e("ok: " + feedlist.size());

					NEWS.feeds.put(feed.getInt(RssFeed.INT_CATEGORY), feedlist);

				}

			}
			feedload=true;
		} catch(Exception e) {
			BLog.e("DoLoadFromHost.onPostExecute()",e.getMessage());

			// load from loacal file instead

			//BLog.e("NEWS FROMFILE", "from local"+fd);
		}

		if(catload && feedload) {
			//NewsFeedsDb.setCategories(acats);
			//NewsFeedsDb.setFeeds(afeeds);

			NewsFeedsDb.Save();

		}
		NewsFeedsDb.setState(NewsFeedsDb.STATE_OK);
		//}
		//}
	}

	private static Boolean loadNews(Context context) {
		try {
			//cats = JSONUrlReader.readJsonFromUrlPlainText(UrlStore.URL_RSS_CATGORIES_MASTER);
			//feeds = JSONUrlReader.readJsonFromUrlPlainText(UrlStore.URL_RSS_FEEDS_MASTER);

		} catch(Exception e) {}
		//BLog.e("DLFH", "cats: "+cats.toString());
		//if(acats==null || afeeds==null) {
			if(context!=null) {

				String fd = TextFile.getAssetFileContent(context, "def_news_cats.json");
				//BLog.e("DLFH", "load from file: "+fd);
				//BLog.e("DLFH", "fd: "+fd);
				//BLog.e("DLFH", " ");
				//BLog.e("DLFH", " ");
				//BLog.e("DLFH", " ");
				//try {
					acats = new JSONObject(fd);
				//} catch(Exception e) {
					//BLog.e("Brief", "" + e.getMessage());
				//}
				//BLog.e("DLFH", "load from file");
				String ffd = TextFile.getAssetFileContent(context,"def_news_feeds.json");
				//BLog.e("DLFH", "load from file: "+ffd);
				try{
					afeeds = new JSONObject(ffd);
				} catch(Exception e) {
					BLog.e("Brief", "" + e.getMessage());
				}
			}
			//feeds = new JSONObject(ffd);
		//}




		return Boolean.TRUE;
	}
	private static void populateWithFeeds(JSONArray feeds) {
		NEWS.feeds = new HashMap<Integer,ArrayList<RssFeed>>();


		for(int i=0; i<feeds.length(); i++) {
			RssFeed feed = new RssFeed(feeds.getJSONObject(i));
			//BLog.e("FEED",feed.getString(RssFeed.S))
			int category=feed.getInteger(RssFeed.INT_CATEGORY);
			ArrayList<RssFeed> feedlist = NEWS.feeds.get(category);
			if(feedlist==null)
				feedlist=new ArrayList<RssFeed>();
			feedlist.add(feed);

			NEWS.feeds.put(category, feedlist);
			BLog.e("lf: "+i);
			//NEWS.feeds.add();
		}
	}

	public static void FetchNewsFromRemote() {
		//DoLoadFromHost dlfh = 
		NEWS.doload=new DoLoadFromHost();
		NEWS.doload.setActivity(NEWS.context);
		NEWS.doload.execute(true);

	}

}
