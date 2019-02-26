package run.brief.news;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import run.brief.beans.RssCategory;
import run.brief.beans.RssFeed;
import run.brief.util.TextFile;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;

public class DoLoadFromHost extends AsyncTask<Boolean, Void, Boolean> {
	
	private static JSONObject cats;
	private static JSONObject feeds;
	private final String IS_LOADED_TRUE="true";
	private final String IS_LOADED_FALSE="false";
	private Context context;
	
	public void setActivity(Context a) {
		context=a;
	}
	
	public DoLoadFromHost() {
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Boolean doInBackground(Boolean... params) {
    
	    return loadNews(context);

	}

	@Override
	protected void onPostExecute(Boolean result) {
		synchronized(this) {
		boolean catload=false;
		boolean feedload=false;
		if(cats!=null) {
			
			ArrayList<RssCategory> acats=new ArrayList<RssCategory>();
			HashMap<Integer,ArrayList<RssFeed>> afeeds=new HashMap<Integer,ArrayList<RssFeed>>();

			try {
				JSONArray cl = cats.getJSONArray(NewsFeedsDb.NEWS_DEFAULT_CATEGORY);
				if(cl!=null) {
					for(int i=0; i<cl.length(); i++) {
						acats.add(new RssCategory(cl.getJSONObject(i)));
						//DB.cats.add(new RssCategory(cl.getJSONObject(i)));
					}
				}
				catload=true;
			} catch(Exception e) {
			}
			if(feeds!=null) {
				try {
				//DB.feeds.clear();
				JSONArray fl = feeds.getJSONArray(NewsFeedsDb.NEWS_DEFAULT_FEEDS);
				if(fl!=null) {
					for(int i=0; i<fl.length(); i++) {
						RssFeed feed = new RssFeed(fl.getJSONObject(i));
						ArrayList<RssFeed> feedlist = afeeds.get(feed.getInt(RssFeed.INT_CATEGORY));
						if(feedlist==null)
							feedlist=new ArrayList<RssFeed>();
						feedlist.add(feed);
						
						afeeds.put(feed.getInt(RssFeed.INT_CATEGORY), feedlist);

					}
					
				}
				feedload=true;
				} catch(Exception e) {
					//BLog.e("DoLoadFromHost.onPostExecute()",e.getMessage());
					
					// load from loacal file instead
					
					//BLog.e("NEWS FROMFILE", "from local"+fd);
				}
				
				if(catload && feedload) {
					NewsFeedsDb.setCategories(acats);
					NewsFeedsDb.setFeeds(afeeds);
					
					NewsFeedsDb.Save();

				}
				NewsFeedsDb.setState(NewsFeedsDb.STATE_OK);
			}
		}
		//BLog.e("FIRSTLOAD","feeds and cats checking... finished");
		}
	}

	@Override
	protected void onPreExecute() {
	}

	public static void goLoad(Context context) {
		boolean catload=false;
		boolean feedload=false;

		loadNews(context);
		//if(cats!=null) {

			ArrayList<RssCategory> acats=new ArrayList<RssCategory>();
			HashMap<Integer,ArrayList<RssFeed>> afeeds=new HashMap<Integer,ArrayList<RssFeed>>();

			try {
				JSONArray cl = cats.getJSONArray(NewsFeedsDb.NEWS_DEFAULT_CATEGORY);
				if(cl!=null) {
					for(int i=0; i<cl.length(); i++) {
						acats.add(new RssCategory(cl.getJSONObject(i)));
						//DB.cats.add(new RssCategory(cl.getJSONObject(i)));
					}
				}
				catload=true;
			} catch(Exception e) {
			}
			//if(feeds!=null) {
				try {
					//DB.feeds.clear();
					JSONArray fl = feeds.getJSONArray(NewsFeedsDb.NEWS_DEFAULT_FEEDS);
					if(fl!=null) {
						for(int i=0; i<fl.length(); i++) {
							RssFeed feed = new RssFeed(fl.getJSONObject(i));
							ArrayList<RssFeed> feedlist = afeeds.get(feed.getInt(RssFeed.INT_CATEGORY));
							if(feedlist==null)
								feedlist=new ArrayList<RssFeed>();
							feedlist.add(feed);

							afeeds.put(feed.getInt(RssFeed.INT_CATEGORY), feedlist);

						}

					}
					feedload=true;
				} catch(Exception e) {
					//BLog.e("DoLoadFromHost.onPostExecute()",e.getMessage());

					// load from loacal file instead

					//BLog.e("NEWS FROMFILE", "from local"+fd);
				}

				if(catload && feedload) {
					NewsFeedsDb.setCategories(acats);
					NewsFeedsDb.setFeeds(afeeds);

					NewsFeedsDb.Save();

				}
				NewsFeedsDb.setState(NewsFeedsDb.STATE_OK);
			//}
		//}
	}

	private static Boolean loadNews(Context context) {

		if(cats==null || feeds==null) {
			if(context!=null) {

				String fd = TextFile.getAssetFileContent(context,"def_news_cats.json");
				try {
					cats = new JSONObject(fd);
				} catch(Exception e) {
					BLog.e("Brief", "" + e.getMessage());
				}
				//BLog.e("DLFH", "load from file");
				String ffd = TextFile.getAssetFileContent(context,"def_news_feeds.json");
				BLog.e("DLFH", "load from file: "+ffd);
				try{
				feeds = new JSONObject(ffd);
				} catch(Exception e) {
					BLog.e("Brief", "" + e.getMessage());
				}
			}
			//feeds = new JSONObject(ffd);
		}


		
		
		return Boolean.TRUE;
	}
}
