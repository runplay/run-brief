package run.brief.news;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import run.brief.beans.Brief;
import run.brief.beans.RssItem;
import run.brief.beans.RssPage;
import run.brief.beans.RssUserFeed;
import run.brief.util.UrlImage;
import run.brief.util.log.BLog;


public class NewsItemsDb {
	
	private static final NewsItemsDb NEWS = new NewsItemsDb();

	private int favouritesCount;

	private static final String DB_DEFAULT_ITEMS="news_items";

	private static final int COLLECT_COUNT_START=30;
    private static final int COLLECT_COUNT=30;
	
	private int countNew;
	private ArrayList<RssItem> data;
    //private ArrayList<RssItem>dataDisclude;
	//private ArrayList<RssUserFeed> userdata;
	private NewsItemsDbTable database;
	private boolean isLoaded=false;
	public AsyncTask<RssUserFeed, Void, Integer> doload;
	
	public NewsItemsDb() {
		//Load();
	}
	public static void clearNewCount() {
		NEWS.countNew=0;
	}
	public static int getNewCount() {
		return NEWS.countNew;
	}
	public static void addNewCount(int countNew) {
		NEWS.countNew=NEWS.countNew+countNew;
	}

	public static int getFavouritesCount() {
		return NEWS.favouritesCount;
	}
	public static Brief getAsBrief(int index) {
		if(NEWS.data!=null && NEWS.data.size()>index){
			Brief b=new Brief(NEWS.data.get(index),index);

			return b;

		}
		return null;
	}
	public void updateFavouriteRead(RssItem item) {
		NEWS.database.updateFavouriteRead(item);
		if(item.getInt(RssItem.INT_FAVOURITE)>0)
			NEWS.favouritesCount++;
		else
			NEWS.favouritesCount--;

	}
	public static final NewsItemsDbTable getItemsDatabase() {
		return NEWS.database;
	}

    public static boolean isEmpty() {
        if(NEWS.data!=null)
            return NEWS.data.isEmpty();
        return true;
    }
	public synchronized static void init(Context context) {
		if(NEWS.database==null) {
			NEWS.database=new NewsItemsDbTable(context);
			NEWS.data=NEWS.database.getItems(0, COLLECT_COUNT_START);
            //NEWS.dataDisclude=
			SortNews();
			NEWS.favouritesCount=NEWS.database.getFavouritesCount();
		}
	}
	public static void refreshData() {
		NEWS.data=NEWS.database.getItems(0, COLLECT_COUNT_START);
	}
    public static ArrayList<RssItem> getItemsForPublisher(String publisher) {
        return NewsItemsDb.getItemsDatabase().getItemsForPublisher(publisher, 0, 40);
    }
	public static ArrayList<RssItem> search(ArrayList<String> terms) {
		return NEWS.database.searchKeywords(terms,0,50);
	}
    public static boolean noMoreHistory=false;

	public static ArrayList<RssItem> getItemsFiltered() {
		ArrayList<RssItem> filtered = new ArrayList<RssItem>();
		for(RssItem it: NEWS.data) {
			if(it.getBoolean(RssItem.BOOL_SHOW_BRIEF))
				filtered.add(it);
		}
		return filtered;
	}

    public static synchronized boolean fetchMore() {
        if(!noMoreHistory) {
            List<RssItem> more = NEWS.database.getItems(NEWS.data.size(), NEWS.data.size() + COLLECT_COUNT);
            //BLog.e("MORE","is empty: "+more.isEmpty());
            if (more.isEmpty()) {
                noMoreHistory = true;
            } else {
                for (RssItem item : more) {
                    NEWS.data.add(item);
                }
                return true;
            }
        }
        return false;
    }
	public synchronized static void reIndexNewsWithFilters() {
		int count=0;
		for(RssItem item: NEWS.data) {
			boolean old = item.getBoolean(RssItem.BOOL_SHOW_BRIEF);
			if(NewsFiltersDb.canShowFeed(item)) {
				item.setBoolean(RssItem.BOOL_SHOW_BRIEF,true);
			} else {
				item.setBoolean(RssItem.BOOL_SHOW_BRIEF,false);
			}
			if(item.getBoolean(RssItem.BOOL_SHOW_BRIEF)!=old) {
				BLog.e("UPDNEWS", "updating canshow: " + item.getBoolean(RssItem.BOOL_SHOW_BRIEF));
				NEWS.database.update(item);
			}
			if(++count>200)
				break;
		}


	}
	public static int refreshNewsFromFeed(RssUserFeed feed) {
		int count=0;
		
		if(NEWS.database!=null && feed!=null && feed.has(RssUserFeed.STRING_URL)) {
			
			//String url = feed.getString(RssUserFeed.STRING_URL);

			RssPage items = Rss.getRssFromFeed(feed);
			if(!items.getItems().isEmpty()) {
				//BLog.e("REFRESH","has items item: "+feed.getString(RssUserFeed.STRING_URL));
				for(RssItem item: items.getItems()) {
					//BLog.e("NEWSR", item.getString(RssItem.STRING_URL));
					if(!NEWS.database.hasItem(item.getString(RssItem.STRING_URL))) {

                        long id=NEWS.database.add(item);
                        item.setLong(RssItem.LONG_ID,id);
                        NEWS.data.add(0, item);


						count++;
						String strurl=item.getString(RssItem.STRING_IMG_URL);
						if(count<3 && strurl!=null && strurl.length()>3)
							new UrlImage().get(strurl);
					}
				}
				SortNews();
			}
		}
		return count;
	}
	public static void SortNews() {
		Collections.sort(NEWS.data, new Comparator<RssItem>() {
            public int compare(RssItem m1, RssItem m2) {
                return (new Date(m2.getLong(RssItem.LONG_DATE)).compareTo(new Date(m1.getLong(RssItem.LONG_DATE))));
            }
        });
	}
	public final static ArrayList<RssItem> getAllItems() {
		return NEWS.data;
	}
	public final static int size() {
		return NEWS.data.size();

	}
	public static RssItem get(int index) {
		if(NEWS.data!=null && NEWS.data.size()>index)
			return NEWS.data.get(index);
		else 
			return null;
	}
	public static long getSizeOnDisk() {
		return NEWS.database.getSizeOnDisk();
	}
	public static long getRowsCount() {
		return NEWS.database.getRowsSize();
	}
    //public static RssItem getFromDbWithId(int id) {
   ///     return  NEWS.database.getItem(id);
   // }
    public static RssItem getById(long id) {
        if(NEWS.data!=null) {
            for(RssItem item: NEWS.data) {
                //BLog.e("GRSS",item.getInt(RssItem.INT_ID)+" - "+id);
                if(item.getLong(RssItem.LONG_ID)==id)
                    return item;
            }

        }

        return getItemsDatabase().getItem(id);
    }

	public static void deleteAll() {
		NEWS.database.deleteAll();
		NEWS.data.clear();
	}
	public synchronized static boolean remove(RssItem item) {
		if(item!=null) {
			NEWS.data.remove(item);
			NEWS.database.delete(item);
			return true;
		} else 
			return false;
		
	}
	public synchronized static void add(RssItem item) {
		if(item!=null) {
			
			NEWS.data.add(0,item);
			NEWS.database.add(item);
		}
		
	}
	public static boolean has(String url) {
		if(NEWS.database.hasItem(url))
			return true;
		return false;
		
	}

}
