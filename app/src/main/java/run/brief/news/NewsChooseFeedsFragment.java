package run.brief.news;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.bButton;
import run.brief.beans.RssFeed;
import run.brief.beans.RssItem;
import run.brief.beans.RssPage;
import run.brief.beans.RssUserFeed;
import run.brief.util.Cal;
import run.brief.util.Sf;
import run.brief.util.UrlImage;
import run.brief.util.UrlStore;
import run.brief.util.json.JSONArray;

//import run.brief.b.ActionModeBack;
//import run.brief.b.ActionModeCallback;
//import run.brief.bread.NavigationDrawerFragment;

//import run.brief.BriefManager;

public class NewsChooseFeedsFragment extends BFragment implements BRefreshable {
	
	private Activity activity;
	private View view;
    private int ADD_TYPE=0;
	//private ViewGroup container;
	//private LayoutInflater inflater;
	//private Handler newsHandler = new Handler();
	//private static ArrayList<Person> show=null;
	private AdapterChooseFeeds exAdpt;
	private ExpandableListView exList;
	
	private EditText urltext;// = (EditText) view.findViewById(R.id.news_add_own_feed);
	private String editurl;
	private AsyncTask<Boolean, Void, Boolean> checkCreateRss;

    private GridView gridInclude;
    private EditText textInclude;
    private GridView gridDisclude;
    private EditText textDisclude;

    private View showFeeds;
    private View showFilters;

    private bButton btnToFilters;
    private bButton btnToFeeds;


    private Button addbtn;
    private Button addhttp;
    private ImageView addYoutube;
    private ImageView addVimeo;

    private AdapterFilters adapterInclude;
    private AdapterFilters adapterDisclude;

    private boolean hasChanges=false;

    private RadioButton radioInclude;
    private RadioButton radioDisclude;
    //private boolean showingFilters=false;
	//private boolean hasChanges=false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		//this.container=container;
		//this.inflater=inflater;
		this.activity=getActivity();
		NewsFeedsDb.init(activity);
		
		
		view=inflater.inflate(R.layout.news_feeds,container, false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
        BriefManager.clearController(activity);
		State.setCurrentSection(State.SECTION_NEWS_CHOOSE);


        urltext = (EditText) view.findViewById(R.id.news_add_own_feed);

        addhttp=(Button) view.findViewById(R.id.btn_feed_http);
        addhttp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addbtn.setCompoundDrawables(null, null, null, null);
                urltext.setHint(activity.getString(R.string.news_custom_added_note));
                ADD_TYPE=0;
                addhttp.setAlpha(1F); addVimeo.setAlpha(0.5F); addYoutube.setAlpha(0.5F);
            }
        });

        addYoutube = (ImageView) view.findViewById(R.id.btn_video_youtube);
        addYoutube.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addbtn.setCompoundDrawables(null,null,B.getDrawable(activity,R.drawable.logo_youtube),null);
                urltext.setHint(activity.getString(R.string.news_custom_added_youtube));
                ADD_TYPE=1;
                addhttp.setAlpha(0.5F); addVimeo.setAlpha(0.5F); addYoutube.setAlpha(1F);
            }
        });
        addVimeo = (ImageView) view.findViewById(R.id.btn_video_vimeo);
        addVimeo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ADD_TYPE=2;
                addbtn.setCompoundDrawables(null,null,B.getDrawable(activity,R.drawable.logo_vimeo),null);
                urltext.setHint(activity.getString(R.string.news_custom_added_vimeo));
                addhttp.setAlpha(0.5F); addVimeo.setAlpha(1F); addYoutube.setAlpha(0.5F);
            }
        });

        addbtn = (Button) view.findViewById(R.id.news_btn_add);
        addbtn.setOnClickListener(addListner);

        switch(ADD_TYPE) {
            case 1: addbtn.setCompoundDrawables(null, null, B.getDrawable(activity, R.drawable.logo_youtube), null);
                urltext.setHint(activity.getString(R.string.news_custom_added_youtube));
                addhttp.setAlpha(0.5F); addVimeo.setAlpha(0.5F); addYoutube.setAlpha(1F);
                break;
            case 2: addbtn.setCompoundDrawables(null, null, B.getDrawable(activity, R.drawable.logo_vimeo), null);
                urltext.setHint(activity.getString(R.string.news_custom_added_vimeo));

                addhttp.setAlpha(0.5F); addVimeo.setAlpha(1F); addYoutube.setAlpha(0.5F);
                break;

            default: addbtn.setCompoundDrawables(null, null, null, null);
                urltext.setHint(activity.getString(R.string.news_custom_added_note));
                addhttp.setAlpha(1F); addVimeo.setAlpha(0.5F); addYoutube.setAlpha(0.5F);

                break;
        }



        showFeeds=view.findViewById(R.id.news_show_feeds);

        showFilters=view.findViewById(R.id.news_show_filters);

        btnToFilters=(bButton) view.findViewById(R.id.news_btn_to_filters);
        btnToFeeds=(bButton) view.findViewById(R.id.news_btn_to_feeds);
        btnToFeeds.setOnClickListener(toFeedsListener);
        btnToFilters.setOnClickListener(toFiltersListener);

        gridInclude =(GridView) view.findViewById(R.id.news_add_key_grid_include);
        gridInclude.setOnItemClickListener(removeIncludeListener);


        gridDisclude =(GridView) view.findViewById(R.id.news_add_key_grid_disclude);
        gridDisclude.setOnItemClickListener(removeDiscludeListener);

        textInclude =(EditText) view.findViewById(R.id.news_add_key_text_include);
        textInclude.addTextChangedListener(new FiltersWatcher(this,true));
        textDisclude =(EditText) view.findViewById(R.id.news_add_key_text_disclude);
        textDisclude.addTextChangedListener(new FiltersWatcher(this,false));

        radioInclude = (RadioButton) view.findViewById(R.id.news_key_style_include);
        radioInclude.setOnClickListener(radioChecked);
        radioDisclude = (RadioButton) view.findViewById(R.id.news_key_style_disclude);
        radioDisclude.setOnClickListener(radioChecked);


        TextView txt0 = (TextView) view.findViewById(R.id.news_open_select_sources);
        TextView txt1 = (TextView) view.findViewById(R.id.news_filter_text_kw1);
        TextView txt2 = (TextView) view.findViewById(R.id.news_filter_text_kw2);
        TextView txt3 = (TextView) view.findViewById(R.id.news_filter_text_kw3);
        TextView txt4 = (TextView) view.findViewById(R.id.news_filter_text_kw4);
        TextView txt5 = (TextView) view.findViewById(R.id.news_filter_text_kw5);
        TextView txt6 = (TextView) view.findViewById(R.id.news_filter_text_kw6);

        TextView txt7 = (TextView) view.findViewById(R.id.news_filter_text_kw7);
        TextView txt8 = (TextView) view.findViewById(R.id.news_filter_text_kw8);
        TextView txt9 = (TextView) view.findViewById(R.id.news_filter_text_kw9);
        //TextView txt10 = (TextView) view.findViewById(R.id.news_filter_text_kw10);
        TextView txt11 = (TextView) view.findViewById(R.id.news_filter_text_kw11);


        B.addStyle(new TextView[]{textDisclude, textInclude, urltext, txt0, txt1, txt2, txt3, txt4, txt5, txt6, txt7, radioInclude, radioDisclude});

        refresh();
        
		
	}
    @Override
    public void onPause() {
        super.onPause();
        if(hasChanges) {
            View v = view.findViewById(R.id.news_reindexing);
            v.setVisibility(View.VISIBLE);
            B.addStyleBold((TextView) view.findViewById(R.id.news_reindex_text));
            NewsFiltersDb.Save();
            NewsItemsDb.reIndexNewsWithFilters();
            BriefManager.setDirty(BriefManager.IS_DIRTY_NEWS);
            v.setVisibility(View.GONE);
        }
        if(showFilters.getVisibility()== View.VISIBLE)
            State.addToState(State.SECTION_NEWS_CHOOSE, new StateObject(StateObject.INT_VALUE, 1));

    }
    private void showView(int type, View view) {
        showFeeds.setVisibility(View.GONE);
        showFilters.setVisibility(View.GONE);
        String title = activity.getString(R.string.news_feeds_title);
        if(type!=0)
            title = activity.getString(R.string.news_key_title);
/*
        amb = new ActionModeBack(activity, title
                ,R.menu.news_feeds
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),title,R.menu.news_feeds,R.color.brand_bread);


        view.setVisibility(View.VISIBLE);
    }
    private OnClickListener toFeedsListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            showView(0,showFeeds);
        }
    };
    private OnClickListener toFiltersListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            showView(1,showFilters);
        }
    };

	public void refresh() {



		//ActionBarManager.setActionBarBackOnlyWithLogo(activity, R.drawable.icon_news, activity.getResources().getString(R.string.title_news), R.menu.news_feeds, R.color.actionbar_news);
		//BriefManager.clearController(activity);
		//ProgressBar progressBar = new ProgressBar(activity);
        //progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
        //        LayoutParams.WRAP_CONTENT));
        //progressBar.setIndeterminate(true);
		exList = (ExpandableListView) activity.findViewById(R.id.news_feeds_list);
		exAdpt = new AdapterChooseFeeds(activity);
	    exList.setIndicatorBounds(0, 20);
	    exList.setAdapter(exAdpt);
	    exList.setClickable(true);
	    exList.setOnChildClickListener(onOffListner);
	    exList.setOnItemLongClickListener(onLongListner);

        if(NewsFiltersDb.isFiltersStartInclude()) {
            radioInclude.setChecked(true);
        } else {
            radioDisclude.setChecked(true);
        }

	    //exList.setEmptyView(view.findViewById(R.id.empty_news_feeds));
	    View noneview = (View) view.findViewById(R.id.empty_news_feeds);
	    noneview.setVisibility(View.GONE);
	    if(NewsFeedsDb.getAllCategories().isEmpty())
	    	noneview.setVisibility(View.VISIBLE);

        //ScrollView sc = (ScrollView) view.findViewById(R.id.about);
        //sc.setAd
        //ExpandableListView view = listAdapter.getView(0, view, listView);
        /*
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.EXACTLY);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.EXACTLY);
        exList.measure(widthMeasureSpec, heightMeasureSpec);
*/
        refreshData();
        hasChanges=false;

        if(State.hasStateObject(State.SECTION_NEWS_CHOOSE, StateObject.INT_VALUE)) {
            showView(1,showFilters);
        } else {
            showView(0,showFeeds);
        }
        State.clearStateObjects(State.SECTION_NEWS_CHOOSE);
	}

    public void refreshData() {
        //BLog.e("FILT","refreshData");
        adapterInclude=new AdapterFilters(activity, NewsFiltersDb.getIncludeArray());
        gridInclude.setAdapter(adapterInclude);

        adapterDisclude=new AdapterFilters(activity, NewsFiltersDb.getDiscludeArray());
        gridDisclude.setAdapter(adapterDisclude);

        if(adapterInclude.getCount()>2)
            gridInclude.setMinimumHeight(80);
        gridInclude.invalidate();
        if(adapterDisclude.getCount()>2)
            gridDisclude.setMinimumHeight(80);
        gridDisclude.invalidate();

        showFilters.invalidate();
        hasChanges=true;

    }
    protected OnClickListener radioChecked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((RadioButton) view).isChecked();
            switch(view.getId()) {
                case R.id.news_key_style_include:
                    if(checked)
                        NewsFiltersDb.setFilterStartInclude(true);
                    else
                        NewsFiltersDb.setFilterStartInclude(false);
                    break;
                case R.id.news_key_style_disclude:
                    if(checked)
                        NewsFiltersDb.setFilterStartInclude(false);
                    else
                        NewsFiltersDb.setFilterStartInclude(true);
                    break;
            }
        }
    };
    protected OnClickListener addListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			checkAndCrateCustomRssGo();
		}   
	};	
	protected void checkAndCrateCustomRssGo() {
        editurl=urltext.getText().toString().trim();
        urltext.setFocusable(false);
		checkCreateRss=new checkAndCreateCustomRss().execute(true);
    }

    protected void checkAndCreateCustomRssStart() {
        //BLog.e("GET FEED", NewsFeedsDb.getUserFeeds().size() + " -- " + urltext.getText().toString().trim());

	}
	protected void checkAndCreateCustomRssStop() {
		exList.invalidate();
		urltext.setText("");
        urltext.setFocusable(true);//urltext.setFocusableInTouchMode(true);
	}
	private void deleteCustomFeed(int position) {
		final int thispos=position;
		new AlertDialog.Builder(activity)
		    .setIcon(android.R.drawable.ic_dialog_alert)
		    .setTitle(R.string.news_delete_title)
		    .setMessage(R.string.news_delete_custom)
		    .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener() {
		
		        @Override
		        public void onClick(DialogInterface dialog, int which) {
		        	RssUserFeed f=exAdpt.getUserCustomFeeds().get(thispos);
		        	if(f!=null)
		        		NewsFeedsDb.removeUserFeed(f);
		            //Stop the activity
		            //YourClass.this.finish();
		        	refresh();
		        }
		
		    })
		    .setNegativeButton(R.string.label_no, null)
		    .show();
	}
	protected class checkAndCreateCustomRss extends AsyncTask<Boolean, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			boolean success=false;

			if(ADD_TYPE==0) {
                RssPage getRss = Rss.getRssFromFeedUrl(editurl);
                if (!getRss.getItems().isEmpty()) {
                    //BLog.e("GET FEED",NewsFeedsDb.getUserFeeds().size()+" -- "+editurl);
                    if (NewsFeedsDb.getUserFeeds().get(editurl) != null) {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, activity.getResources().getString(R.string.news_custom_rss_already), Toast.LENGTH_SHORT).show();
                            }
                        });

                    } else {

                        addFeedNow(getRss);
                        success=true;

                    }
                } else {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, activity.getResources().getString(R.string.news_custom_rss_invalid), Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            } else {

                // youtube and vimeo add types
                if(!editurl.contains("http") && !editurl.contains("/")) {
                    if(ADD_TYPE==1)
                        editurl = "https://www.youtube.com/feeds/videos.xml?user="+editurl.toLowerCase().trim();
                    else
                        editurl = "https://vimeo.com/channels/"+editurl.toLowerCase().trim()+"/videos/rss";
                    final RssPage getRss = Rss.getRssFromFeedUrl(editurl);
                    if (!getRss.getItems().isEmpty()) {

                        addFeedNow(getRss);
                        success=true;
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(activity, getRss.getErrormessage()+" - "+activity.getResources().getString(R.string.news_custom_rss_invalid), Toast.LENGTH_SHORT).show();
                            }
                        });

                    }


                } else {
                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(activity, activity.getResources().getString(R.string.news_custom_channel_invalid), Toast.LENGTH_SHORT).show();
                        }
                    });

                }


            }
			
		    return success;
	
		}
        private void addFeedNow(RssPage getRss) {
            String publisher = null;
            for (RssItem it : getRss.getItems()) {
                String pub = it.getString(RssItem.STRING_PUBLISHER);
                if (pub != null && pub.length() > 1) {
                    publisher = pub;
                    break;
                }
            }
            if (publisher == null) {
                publisher = Sf.getDomainName(getRss.getUrl());

                if (getRss.getTitle() != null) {
                    publisher = publisher + " - " + getRss.getTitle();
                }

            }
            RssUserFeed feednew = new RssUserFeed(publisher, editurl);
            feednew.setLong(RssUserFeed.LONG_LAST_UPDATE, Cal.getUnixTime() - 9000000);
            feednew.setInt(RssUserFeed.INT_CUSTOM, 1);
            NewsFeedsDb.saveUserFeed(feednew);

            Bitmap b = new UrlImage().get(activity, UrlStore.URL_NEWS_PUBLISHER_IMAGES+feednew.getString(RssUserFeed.STRING_PUBLISHER_IMAGE) , null);

            DoLoadForUserFeed dlf=new DoLoadForUserFeed(activity);
            dlf.setDoNotRefresh(true);
            dlf.execute(feednew);

            //BreadService.refrehNewsNow(activity, false);
        }
		@Override
		protected void onPostExecute(Boolean result) {

            if(result) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        checkAndCreateCustomRssStop();
                    }
                });
                refreshData();

            }
		}
	 
	}

    private int removePosition;
    public AdapterView.OnItemClickListener removeIncludeListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //BriefMenu.showPreview(position);
            removePosition=position;
            String remStr = (String) NewsFiltersDb.getIncludeArray().get(position);
            //BLog.e("REM",""+remStr);
            if(remStr!=null) {
                if(remStr.contains(" "))
                    remStr="\""+remStr+"\"";
                PopupMenu popupMenu = new PopupMenu(activity, view);
                //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());
                popupMenu.getMenu().add(activity.getResources().getString(R.string.label_remove)+ ": " + remStr);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        JSONArray arr = NewsFiltersDb.getIncludeArray();
                        arr.remove(removePosition);
                        NewsFiltersDb.setIncludeArray(arr);

                        refreshData();
                        return true;
                    }
                });

                popupMenu.show();

            }
        }
    };
    public AdapterView.OnItemClickListener removeDiscludeListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //BriefMenu.showPreview(position);
            removePosition=position;
            String remStr = (String) NewsFiltersDb.getDiscludeArray().get(position);
            if(remStr!=null) {
                if(remStr.contains(" "))
                    remStr="\""+remStr+"\"";

                PopupMenu popupMenu = new PopupMenu(activity, view);
                //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());
                popupMenu.getMenu().add(activity.getResources().getString(R.string.label_remove)+ ": " + remStr);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        JSONArray arr = NewsFiltersDb.getDiscludeArray();
                        arr.remove(removePosition);
                        NewsFiltersDb.setDiscludeArray(arr);

                        refreshData();
                        return true;
                    }
                });

                popupMenu.show();


            }
        }
    };
	protected OnItemLongClickListener onLongListner = new OnItemLongClickListener() {
	    @Override
	    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
	        if (ExpandableListView.getPackedPositionType(id) == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
	            int groupPosition = ExpandableListView.getPackedPositionGroup(id);
	            int childPosition = ExpandableListView.getPackedPositionChild(id);
	            
	            if(groupPosition==0) {
	            	deleteCustomFeed(childPosition);
	            }
	            // You now have everything that you would as if this was an OnChildClickListener() 
	            // Add your logic here.

	            // Return true as we are handling the event.
	            return true;
	        }

	        return false;
	    }
	};
	
	protected OnChildClickListener onOffListner = new OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			
			if(groupPosition==0) {
				
				RssUserFeed ufeed = exAdpt.getUserCustomFeeds().get(childPosition);
				if(ufeed!=null) {
					if(ufeed.getInt(RssUserFeed.INT_ACTIVE)==0) {
						ufeed.setInt(RssUserFeed.INT_ACTIVE, 1);
					} else {
						ufeed.setInt(RssUserFeed.INT_ACTIVE, 0);
					}
					NewsFeedsDb.updateUserFeed(ufeed);
					//exAdpt = new AdapterChooseFeeds(activity);
					exAdpt.notifyDataSetChanged();

					//exList.setAdapter(exAdpt);
				}
				
			} else {
				RssFeed news = NewsFeedsDb.getFeed(groupPosition-1, childPosition); 
				//String url = news.getString(RssFeed.STRING_URL);
				RssUserFeed ufeed = NewsFeedsDb.getUserFeed(news); //NewsItemsDb.getUserFeed(url);
				
				if(ufeed==null) {

					ufeed=new RssUserFeed(news.getString(RssFeed.STRING_NAME),news.getString(RssFeed.STRING_URL),RssUserFeed.COLLECT_REGULAR, news.getString(RssFeed.STRING_IMG_URL));
					//ufeed.setLong(RssUserFeed.LONG_LAST_UPDATE, Cal.getUnixTime());
                    ufeed.setLong(RssUserFeed.LONG_LAST_UPDATE, Cal.getUnixTime());


					NewsFeedsDb.saveUserFeed(ufeed);
                    DoLoadForUserFeed dluf = new DoLoadForUserFeed(activity);
                    dluf.setDoNotRefresh(true);
                    dluf.execute(ufeed);
                    BriefManager.setDirty(BriefManager.IS_DIRTY_NEWS);
                    AdapterListNews.setForceChange(true);
                    AdapterListNews.setShowPublisher(null);
					//BreadService.refrehNewsNow(activity,false);
				} else {

					NewsFeedsDb.removeUserFeed(ufeed);
				}
				//v=exAdpt.getChildView(groupPosition, childPosition, isLastChild, convertView, parent)
				exAdpt.notifyDataSetChanged();
			}
			

			return true;
		}
	};

}
