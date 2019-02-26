package run.brief.news;


import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import run.brief.R;
import run.brief.b.B;
import run.brief.beans.RssCategory;
import run.brief.beans.RssFeed;
import run.brief.beans.RssUserFeed;
import run.brief.util.Cal;
//import run.brief.util.ViewManagerText;
import run.brief.util.json.JSONObject;

public class AdapterChooseFeeds extends BaseExpandableListAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
    private Drawable acceptDrawable;
    private Drawable cancelDrawable;

    public static final String EMO_TICK_BOX="☑ ";
    private static String CUTOM_ADDED_TITLE;
    private static String CUTOM_ADDED_SUB_TITLE;
 
    private List<RssUserFeed> userfeeds=new ArrayList<RssUserFeed>();
    
    public AdapterChooseFeeds(Activity a) {
    	
        activity = a;
          
        //this.data=data;
        acceptDrawable=activity.getResources().getDrawable(R.drawable.navigation_accept);
        cancelDrawable=activity.getResources().getDrawable(R.drawable.navigation_cancel);
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        

        
        HashMap<String,RssUserFeed> mapuser=NewsFeedsDb.getUserFeeds();
        if(mapuser!=null && !mapuser.isEmpty()){
        	Iterator<String> it = mapuser.keySet().iterator();
        	while(it.hasNext()) {
        		RssUserFeed feed=mapuser.get(it.next());
        		//BLog.e("USER_FEEDS","FEED: "+feed.getInt(RssUserFeed.INT_CUSTOM)+" - "+feed.getString(RssUserFeed.STRING_URL));
        		if(feed.getInt(RssUserFeed.INT_CUSTOM)!=0)
        			userfeeds.add(feed);
        	}
        }
        
        CUTOM_ADDED_TITLE = activity.getResources().getString(R.string.news_custom_added_title)+ " ("+userfeeds.size()+")";
        CUTOM_ADDED_SUB_TITLE = activity.getResources().getString(R.string.news_custom_added_sub_title);
        //BLog.e("USER_FEEDS","USER_FEEDS: "+ mapuser.size()+" -- "+userfeeds.size());
    }
 
    public List<RssUserFeed> getUserCustomFeeds() {
    	return userfeeds;
    }
 
    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
         
        View v = convertView;
         
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)activity.getSystemService
                      (Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.news_feeds_item_row, parent, false);
        }
         
        TextView itemName = (TextView) v.findViewById(R.id.news_feed_item_name);
        TextView itemDescr = (TextView) v.findViewById(R.id.news_feed_item_url);
        ImageView img = (ImageView) v.findViewById(R.id.img_is_selected);
        TextView sync=(TextView) v.findViewById(R.id.news_feed_item_sync);
        View syncpod= v.findViewById(R.id.news_feed_item_sync_pod);

        B.addStyle(itemName);
        B.addStyle(itemDescr);
        B.addStyle(sync);
        itemDescr.setPaintFlags(itemDescr.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        sync.setVisibility(View.GONE);
        if(groupPosition==0) {
        	RssUserFeed ufeed = userfeeds.get(childPosition);
        	

        	if(ufeed.getInt(RssUserFeed.INT_ACTIVE)==0) {
        		img.setImageDrawable(cancelDrawable);
            	img.setAlpha(0.2F);
                if(ufeed.has(RssUserFeed.LONG_LAST_UPDATE)) {
                    sync.setText(Cal.getCal(ufeed.getLong(RssUserFeed.LONG_LAST_UPDATE)).friendlyReadDate());
                    syncpod.setVisibility(View.VISIBLE);
                }
        	} else {
        		img.setImageDrawable(acceptDrawable);
            	img.setAlpha(1F);
                syncpod.setVisibility(View.GONE);
        	}



	    	itemName.setText(ufeed.getString(RssUserFeed.STRING_PUBLISHER));
	    	itemDescr.setText(ufeed.getString(RssUserFeed.STRING_URL));
        } else {
        	RssFeed news = NewsFeedsDb.getFeed(groupPosition-1, childPosition); 
            
            String url = news.getString(RssFeed.STRING_URL);
            
            RssUserFeed ufeed =  NewsFeedsDb.getUserFeed(news); //NewsItemsDb.getUserFeed(url);
            
            if(ufeed!=null) {
            	img.setImageDrawable(acceptDrawable);
            	img.setAlpha(1F);
                if(ufeed.has(RssUserFeed.LONG_LAST_UPDATE)) {
                    sync.setText(Cal.getCal(ufeed.getLong(RssUserFeed.LONG_LAST_UPDATE)).friendlyReadDate());
                    syncpod.setTag(new RssUserFeed(new JSONObject(ufeed.toString())));

                    //sync.setVisibility(View.VISIBLE);
                    syncpod.setVisibility(View.VISIBLE);
                    syncpod.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            RssUserFeed ufeed =  (RssUserFeed) view.getTag();
                            NewsPop popupMenu = new NewsPop(activity, view);
                            popupMenu.setFeedAndShow(ufeed);
                            //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());

                        }
                    });
                }
            } else {
            	img.setImageDrawable(cancelDrawable);
            	img.setAlpha(0.2F);
                syncpod.setVisibility(View.GONE);
            }

            itemName.setText(news.getString(RssFeed.STRING_NAME));
            itemDescr.setText(url);
        }

         
        return v;
        
         
    }

    private class NewsPop extends PopupMenu {
        private List<MenuItem> menuitems = new ArrayList<MenuItem>();
        RssUserFeed useFeed;
        private NewsPop(Activity activity, View view) {
            super(activity,view);
        }
        private void setFeedAndShow(RssUserFeed feed) {
            useFeed=feed;

            int collect=useFeed.getInt(RssUserFeed.INT_COLLECT_);
            MenuItem fast=null;
            MenuItem medium=null;
            MenuItem slow=null;
            if(collect==RssUserFeed.COLLECT_FAST) {
                fast=getMenu().add(EMO_TICK_BOX + " " + activity.getResources().getString(R.string.news_feeds_sync_fast));
            } else {
                fast=getMenu().add(activity.getResources().getString(R.string.news_feeds_sync_fast));
            }
            if(collect==RssUserFeed.COLLECT_REGULAR)
                medium=getMenu().add(EMO_TICK_BOX+" "+activity.getResources().getString(R.string.news_feeds_sync_medium));
            else
                medium=getMenu().add(activity.getResources().getString(R.string.news_feeds_sync_medium));

            if(collect==RssUserFeed.COLLECT_SLOW)
                slow=getMenu().add(EMO_TICK_BOX+" "+activity.getResources().getString(R.string.news_feeds_sync_slow));
            else
                slow=getMenu().add(activity.getResources().getString(R.string.news_feeds_sync_slow));

            menuitems.clear();
            menuitems.add(fast);
            menuitems.add(medium);
            menuitems.add(slow);

            setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int current=useFeed.getInt(RssUserFeed.INT_COLLECT_);
                    int use=0;
                    if(item==menuitems.get(0)) {
                        use=RssUserFeed.COLLECT_FAST;
                    } else if(item==menuitems.get(1)) {
                        use=RssUserFeed.COLLECT_REGULAR;
                    } else {
                        use=RssUserFeed.COLLECT_SLOW;
                    }

                    if(use!=current) {
                        useFeed.setInt(RssUserFeed.INT_COLLECT_,use);
                        NewsFeedsDb.updateUserFeed(useFeed);
                    }

                    return true;
                }
            });

            show();
        }
    }
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
     
        View v = convertView;
        if (v == null) {
            LayoutInflater inflater = (LayoutInflater)activity.getSystemService
                      (Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.news_feeds_head_row, parent, false);
        }

        TextView itemName = (TextView) v.findViewById(R.id.news_feed_head_name);
        TextView itemDescr = (TextView) v.findViewById(R.id.news_feed_head_description);
        ImageView image=(ImageView) v.findViewById(R.id.feeds_has_children);
        B.addStyle(itemName);
        B.addStyle(itemDescr);
        if(groupPosition==0) {
        	
	        itemName.setText(CUTOM_ADDED_TITLE);
	        itemDescr.setText(CUTOM_ADDED_SUB_TITLE);
            image.setVisibility(View.GONE);
        } else {
        
	        RssCategory news = NewsFeedsDb.getCategory(groupPosition - 1);
            if(news!=null) {
                ArrayList<RssFeed> feeds = NewsFeedsDb.getFeeds(news.getInt(RssCategory.CATEGORY_INT_ID));
                int children = 0;
                for (RssFeed feed : feeds) {
                    if (NewsFeedsDb.getUserFeed(feed) != null)
                        children++;
                }
                if (children == 0)
                    image.setVisibility(View.GONE);
                else
                    image.setVisibility(View.VISIBLE);
                itemName.setText(news.getString(RssCategory.CATEGORY_STRING_NAME));
                itemDescr.setText(news.getString(RssFeed.STRING_IMG_URL));
            }
        }
        return v;
     
    }
 
	@Override
	public Object getChild(int groupPosition, int childPosition) {

		if(groupPosition==0)
			return userfeeds.get(childPosition);
		else
			return NewsFeedsDb.getFeed(groupPosition,childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {

		return childPosition;
	}

	@Override
	public int getChildrenCount(int groupPosition) {

		if(groupPosition==0)
			return userfeeds.size();
		else
			return NewsFeedsDb.FeedsSize(groupPosition-1);
	}

	@Override
	public Object getGroup(int groupPosition) {

		return NewsFeedsDb.getCategory(groupPosition-1);
	}

	@Override
	public int getGroupCount() {

		return NewsFeedsDb.CategoriesSize()+1;
	}

	@Override
	public long getGroupId(int groupPosition) {

		return groupPosition;
	}

	@Override
	public boolean hasStableIds() {

		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {

		return true;
	}
}
