package run.brief.news;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BCallbackInt;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.SwipeRefreshLayout;
import run.brief.b.SwipeRefreshLayout.OnRefreshListener;
import run.brief.beans.RssFeed;
import run.brief.beans.RssItem;
import run.brief.beans.RssUserFeed;
import run.brief.search.SearchFragment;
import run.brief.service.BriefService;
import run.brief.util.Cal;
import run.brief.util.Functions;
import run.brief.util.Sf;
import run.brief.util.UrlImage;
import run.brief.util.UrlStore;
import run.brief.util.log.BLog;

//import run.brief.BriefManager;
//import run.brief.menu.BriefMenu;


public class NewsHomeFragment extends BFragment implements BRefreshable,BCallbackInt {
	
	private View view;
	private static Activity activity;
	private static GridView listnews;
	private static AdapterListNews adapter;
	private static View selectSourceBtn;
	private static SwipeRefreshLayout swipeRefresh;
	private NewsHomeFragment thisFragment;
    private NewsDialog popupMenu;
    private ArrayList<RssFeed> quicksetupFeeds;

    private Handler checkNewsHandler = new Handler();
    private View checkNewsSync;

    private boolean syncShowing=false;
    //private int lastPosition;
    private int firstVis;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		thisFragment=this;
		activity=getActivity();
		
		NewsItemsDb.init(activity);
		NewsFeedsDb.init(activity);

        //NewsItemsDb.deleteAll();
		view=inflater.inflate(R.layout.news,container, false);

		return view;
	}

    private List<String> sources = new ArrayList<String>();

    @Override
    public void onPause() {
        super.onPause();
        if(popupMenu!=null) {
            popupMenu.cancel();
            popupMenu=null;
        }
        //BriefManager.removeGoTopTracker();
        checkNewsHandler.removeCallbacks(isSyncingCheck);

        if(listnews!=null) {
            State.addToState(State.SECTION_NEWS,new StateObject(StateObject.INT_LAST_POS,listnews.getFirstVisiblePosition()));
        }
        B.removeGoTopTracker();
    }
	@Override
	public void onResume() {
		super.onResume();
        BriefManager.activateController(activity);
        State.sectionsClearBackstack();
		State.setCurrentSection(State.SECTION_NEWS);

		swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
		swipeRefresh.setOnRefreshListener(new OnRefreshListener() {

            @Override
            public void onRefresh() {
                BriefService.refrehNewsNow(activity, true);
                checkNewsHandler.postDelayed(isSyncingCheck, 100);
            }
        });
        //mSwipeRefreshLayout.
		swipeRefresh.setColorScheme(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);
	
		listnews = (GridView) view.findViewById(R.id.news_list_view);
		listnews.setOnItemClickListener(newitemListner);
        listnews.setOnItemLongClickListener(onLongClick);

        //BriefManager.addGoTopTracker(activity,listnews,R.drawable.gt_news);


        TextView txt1 = (TextView) view.findViewById(R.id.news_no_pub);
        TextView txt2 = (TextView) view.findViewById(R.id.news_open_select_sources);
        TextView txt3 = (TextView) view.findViewById(R.id.news_text_1);

        B.addStyle(txt1, txt2, txt3);

        checkNewsSync=view.findViewById(R.id.syncing);
        checkNewsSync.setVisibility(View.GONE);
        B.addStyleBold((TextView) checkNewsSync.findViewById(R.id.syncing_text));

		refresh();
  }
    public void callback(int value) {
        BLog.e("Image loaded callback");
        if(listnews!=null) {
/*
            //listnews.getAdapter().notify();//.notifyAll();//.invalidateViews();
            View v=(View)listnews.dra.getItemAtPosition(value);

            BLog.e("2");
            //View v = listnews.getChildAt(value);
            if(v!=null) {
                BLog.e("3");
                ImageView img=(ImageView) v.findViewById(R.id.news_item_image);
                if(img!=null) {
                    Bitmap b = new UrlImage().get((String) img.getTag());
                    BLog.e("Image refreshing: "+b.getByteCount());
                    img.setImageBitmap(b);
                }
            }


            //listnews.getAdapter().notify();
            BLog.e("get child at: "+value);
            for(int i=listnews.getFirstVisiblePosition(); i<=listnews.getLastVisiblePosition(); i++) {
                View v = listnews.getChildAt(i);
                if(v!=null) {
                    BLog.e("invalidate child at: "+i);
                    v.refreshDrawableState();//.invalidate();
                }
            }
*/
            listnews.invalidateViews();
        }
    }
    private void emptyFeedsStartup() {
        ArrayList<RssFeed> feeds=NewsFeedsDb.getFeeds(0);


        GridView grid = (GridView) view.findViewById(R.id.quick_start_feeds);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT,Functions.dpToPx(25,activity));
        LinearLayout.LayoutParams ilp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        //ImageView.Layout
        quicksetupFeeds=new ArrayList<RssFeed>();
        ArrayList<LinearLayout> quickshow = new ArrayList<LinearLayout>();
        for(int i=0; i<4; i++) {

            RssFeed fd = feeds.get(i);
            quicksetupFeeds.add(fd);
            LinearLayout lay = new LinearLayout(activity);
            lay.setLayoutParams(lp);
                /*
                ImageView iv = new ImageView(activity);
                iv.setLayoutParams(ilp);
                Bitmap b = new UrlImage().get(activity,fd.getString(RssFeed.STRING_IMG_URL),NewsHomeFragment.class);
                iv.setImageBitmap(b);
                iv.setMaxWidth(50);
                lay.addView(iv);
                */
            TextView txt = new TextView(activity);
            txt.setLayoutParams(ilp);
            txt.setText(Sf.restrictLength(fd.getString(RssFeed.STRING_NAME), 25));
            lay.addView(txt);
            quickshow.add(lay);

        }

        QuickAdapter qad = new QuickAdapter(quickshow);
        grid.setAdapter(qad);

        selectSourceBtn = (View) view.findViewById(R.id.news_open_select_sources);
        selectSourceBtn.setOnClickListener(chooseFeedsListener);

        Button startQuick = (Button) view.findViewById(R.id.start_quick_btn);
        startQuick.setOnClickListener(quickStartListner);

        Button startDetailed = (Button) view.findViewById(R.id.start_manual_btn);
        startDetailed.setOnClickListener(chooseFeedsListener);


    }

    private class QuickAdapter extends BaseAdapter {
    private List<LinearLayout> lays;
        public QuickAdapter(List<LinearLayout> lays) {
            this.lays=lays;
        }
        public int getCount() {
            return lays.size();
        }

        public Object getItem(int position) {
            return lays.get(position);
        }

        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {


            return lays.get(position);
        }
    }
    @Override
    public void refresh() {
        NewsWebViewManager.clear();
        //BLog.e("FRAG REFRESH", "NewsHomeFragment");
        /*
        amb = new ActionModeBack(activity, activity.getString(R.string.title_news)
                ,R.menu.news
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.title_news),R.menu.news,R.color.brand_bread);


        swipeRefresh.setRefreshing(false);

        View emptyNews=view.findViewById(R.id.empty_news);

        if(NewsFeedsDb.getUserFeeds().isEmpty()) {

            emptyFeedsStartup();
        } else {
            emptyNews.setVisibility(View.GONE);
        }


        if(NewsItemsDb.size()==0)
            listnews.setEmptyView(emptyNews);
        else
            listnews.setEmptyView(view.findViewById(R.id.empty_news_publisher));
        if(AdapterListNews.getShowPublisher()==null)
            listnews.setOnScrollListener(new NewsScrollListener());
        else
            listnews.setOnScrollListener(new NoScrollListener());

        adapter = new AdapterListNews(activity,this);
        listnews.setAdapter(adapter);
        int height=Functions.dpToPx(160,activity);
        int heightl=Functions.dpToPx(220,activity);
        if(Device.isTablet(activity)) {
            //BLog.e("is tablet");
            switch (activity.getResources().getConfiguration().orientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    listnews.setNumColumns(3);
                    adapter.setHeight(heightl);
                    break;
                default:
                    listnews.setNumColumns(2);
                    adapter.setHeight(height);
                    break;

            }
        } else {
            //BLog.e("is mobile");
            switch (activity.getResources().getConfiguration().orientation) {
                case Configuration.ORIENTATION_LANDSCAPE:
                    //BLog.e("is landscape");
                    listnews.setNumColumns(2);
                    adapter.setHeight(heightl);
                    break;
                default:
                    //BLog.e("is portrait");
                    listnews.setNumColumns(1);
                    adapter.setHeight(height);
                    break;

            }
        }

        if(State.hasStateObject(State.SECTION_NEWS, StateObject.INT_LAST_POS)) {
            listnews.setSelection(State.getStateObjectInt(State.SECTION_NEWS, StateObject.INT_LAST_POS));
        }
        refreshData();
        B.addGoTopTracker(activity, listnews, R.drawable.gt_news);
    }
    public void refreshData() {
        //firstVis = listnews.getFirstVisiblePosition();

        listnews.invalidateViews();
        //listnews.setSelection(firstVis);

    }

    @Override
    public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
        //BLog.e("FileExplore oncreateoptionsmenu");
        //this.menu=menu;
        //MenuInflater minflater = activity.getMenuInflater();
        //minflater.inflate(R.menu.news, menu);
        MenuItem mi=menu.findItem(R.id.action_filter_feed);
        if(NewsFeedsDb.getUserFeeds().size()>0) {
            mi.setVisible(true);
        } else {
            mi.setVisible(false);
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //BLog.e("onCreateOptionsMenu at home");
        //boolean callrefresh=true;
        boolean intercept=false;
        switch(item.getItemId()) {
            case R.id.action_search:
                Bgo.openFragmentBackStack(activity,SearchFragment.class);
                intercept=true;
                break;
            case R.id.action_filters:
                State.addToState(State.SECTION_NEWS_CHOOSE, new StateObject(StateObject.INT_VALUE, 1));
                Bgo.openFragmentBackStackAnimate(getActivity(), NewsChooseFeedsFragment.class);
                intercept=true;
                break;
            case R.id.action_add_feeds:
                State.clearStateObject(State.SECTION_NEWS_CHOOSE, StateObject.INT_VALUE);
                Bgo.openFragmentBackStack(activity, NewsChooseFeedsFragment.class);
                break;
            case R.id.action_filter_feed:
                View menuItemView = activity.findViewById(R.id.action_filter_feed); // SAME ID AS MENU ID
                PopupMenu popupMenu = new PopupMenu(activity, menuItemView);
                //popupMenu.inflate(R.menu.counters_overflow);
                //PopupMenu popupMenu = new PopupMenu(activity, view);
                Set<String> sokey = NewsFeedsDb.getUserFeeds().keySet();
                sources.clear();
                int i=0;
                popupMenu.getMenu().add(0, -1, Menu.NONE,activity.getString(R.string.news_all_feeds)).setIcon(R.drawable.i_news);
                popupMenu.getMenu().add(0, -2, Menu.NONE,activity.getString(R.string.starred_news)).setIcon(R.drawable.rate_positive);
                for(String key :sokey ) {
                    RssUserFeed uf = NewsFeedsDb.getUserFeeds().get(key);
                    sources.add(uf.getString(RssUserFeed.STRING_PUBLISHER));

                    popupMenu.getMenu().add(0, i, Menu.NONE,uf.getString(RssUserFeed.STRING_PUBLISHER)).setIcon(R.drawable.i_news);
                    //.setCheckable(true).setChecked(true);
                    i++;
                }
                //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId()==-1) {
                            AdapterListNews.setShowPublisher(null);
                        } else if(item.getItemId()==-1) {
                            AdapterListNews.setShowPublisher("star");
                        } else {
                            AdapterListNews.setShowPublisher(item.getTitle().toString());
                        }

                        //BLog.e("CLICK","items: "+item.getItemId()+" - "+item.getTitle().toString());
                        refresh();
                        return true;
                    }
                });

                popupMenu.show();

                break;
        }

        return intercept;
    }





    private Runnable isSyncingCheck = new Runnable() {
        @Override
        public void run() {
        if(BriefService.isNewsRefreshing()) {
            showNewsSync();
        } else {
            hideNewsSync();
        }
        checkNewsHandler.postDelayed(isSyncingCheck,100);
        }
    };
    private void hideNewsSync() {
        if(syncShowing) {
            syncShowing=false;
            checkNewsSync.setVisibility(View.GONE);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0);
            checkNewsSync.setLayoutParams(lp);
            //Animation ani = AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top);
            //checkNewsSync.setAnimation(ani);
            //checkNewsSync.startAnimation(ani);
        }
    }
    private void showNewsSync() {
        if(!syncShowing) {
            syncShowing=true;
            checkNewsSync.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            checkNewsSync.setLayoutParams(lp);
            //Animation ani = AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top);
            //checkNewsSync.setAnimation(ani);
            //checkNewsSync.startAnimation(ani);
            checkNewsSync.bringToFront();
        }
    }
    private ArrayList<UrlImage> cachem;
    public OnClickListener quickStartListner = new OnClickListener() {
        @Override
        public void onClick(View arg1) {

            if(quicksetupFeeds!=null) {
                //if(Device.CheckInternet(activity)) {
                    Button btn = (Button) arg1;
                    btn.setEnabled(false);
                    btn.setText(activity.getString(R.string.label_working));
                    cachem = new ArrayList<UrlImage>();
                    for (RssFeed feed : quicksetupFeeds) {

                        RssUserFeed ufeed = new RssUserFeed(feed.getString(RssFeed.STRING_NAME), feed.getString(RssFeed.STRING_URL), RssUserFeed.COLLECT_REGULAR, feed.getString(RssFeed.STRING_IMG_URL));
                        UrlImage urli = new UrlImage();
                        Bitmap b = urli.get(activity, UrlStore.URL_NEWS_PUBLISHER_IMAGES + ufeed.getString(RssUserFeed.STRING_PUBLISHER_IMAGE), null);
                        cachem.add(urli);
                        ufeed.setLong(RssUserFeed.LONG_LAST_UPDATE, Cal.getUnixTime());
                        NewsFeedsDb.saveUserFeed(ufeed);
                        DoLoadForUserFeed dluf = new DoLoadForUserFeed(activity);
                        dluf.setDoNotRefresh(false);
                        dluf.execute(ufeed);
                    }

                    refresh();

                //} else {
                    //Toast.makeText(activity,activity.getString(R.string.news_error_load),Toast.LENGTH_SHORT);
                //}

            }
        }
    };

    public OnClickListener chooseFeedsListener = new OnClickListener() {
		@Override
		public void onClick(View arg1) {
			Bgo.openFragmentBackStackAnimate(getActivity(), NewsChooseFeedsFragment.class);
		}
	};	
	protected OnItemClickListener newitemListner = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			State.clearStateObjects(State.SECTION_NEWS_VIEW);
            RssItem t = (RssItem) adapter.getItem(position);
            //BLog.e("bhbh",""+t.getInt(RssItem.INT_ID));
			StateObject sob = new StateObject(StateObject.STRING_USE_DATABASE_ID,""+t.getLong(RssItem.LONG_ID));
			State.addToState(State.SECTION_NEWS_VIEW, sob);
			Bgo.openFragmentBackStackAnimate(activity, ViewNewsItemFragment.class);
		}
	};
	
	public OnItemLongClickListener onLongClick = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

			RssItem t = (RssItem) adapter.getItem(position);//NewsItemsDb.get(position);
			if(t !=null) {
				NewsDialog popupMenu = new NewsDialog(getActivity(),t,thisFragment);

				popupMenu.show();

			}

			return true;
			
		}
	};

    public class NoScrollListener implements AbsListView.OnScrollListener {

        public NoScrollListener() {
        }
        public NoScrollListener(int visibleThreshold) {
        }

        private boolean working=false;
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {

        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
    public class NewsScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public NewsScrollListener() {
        }
        public NewsScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        private boolean working=false;
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // I load the next page of gigs using a background task,
                // but you can call any function here.
                if (!working) {
                    synchronized (this) {
                        working = true;
                        //BLog.e("LOAD", "more");
                        boolean more=NewsItemsDb.fetchMore();
                        if(more)
                            refreshData();

                        working = false;
                    }
                }


            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
}
