package run.brief;


import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.BriefRating;
import run.brief.beans.BriefSend;
import run.brief.beans.Note;
import run.brief.beans.RssItem;
import run.brief.beans.SmsMsg;
import run.brief.contacts.ContactViewFragment;
import run.brief.contacts.ContactsSelectedClipboard;
import run.brief.email.EmailDialog;
import run.brief.menu.BriefMenu;
import run.brief.news.NewsDialog;
import run.brief.news.NewsItemsDb;
import run.brief.notes.NotesDb;
import run.brief.notes.NotesDialog;
import run.brief.service.BriefNotify;
import run.brief.service.BriefService;
import run.brief.settings.AccountsDb;
import run.brief.sms.SmsDb;
import run.brief.sms.SmsDialog;
import run.brief.sms.SmsFunctions;
import run.brief.util.Sf;
import run.brief.util.swipe.SwipeDismissListViewTouchListener;

public final class BriefHomeFragment extends BFragment implements BRefreshable {
	private Activity activity;
	private BriefHomeFragment thisFragment;
	private View view;
	private static ListView listBriefs;
	private static BriefPodAdapterList adapter;
	private Dialog dialog;
	private View ratenegative;
	private View ratenone;
	private View ratepositive;
	private ImageView imgratenegative;
	private ImageView imgratenone;
	private ImageView imgratepositive;
	private static int YPOS=0;
	private TextView importantCount;
	private SwipeDismissListViewTouchListener touchListener;
	//private AsyncTask<Boolean, Void, Boolean> gdi;
	private LayoutInflater inflater;
	private ViewGroup container;
	private static int lastViewableListItem;

	private View briefEmptyInbox;
	private View briefEmptyRead;
	private View briefEmptyStar;

    private View firstTime;
    //private List<View> headerViews = new ArrayList<View>();

    //public static boolean showLoading=false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        //Tim.printTime("BH-onCreate()");

        thisFragment=this;
		this.inflater=inflater;
		this.container=container;
		this.view=inflater.inflate(R.layout.brief,container, false);
		activity = getActivity();
        BriefNotify.clearNotifications();



        listBriefs = (ListView) view.findViewById(R.id.brief_home_list);
        listBriefs.setOnItemClickListener(openListener);
        //listBriefs.setOnScrollListener(this);
        listBriefs.setOnItemLongClickListener(onLongClick);




        importantCount = (TextView) view.findViewById(R.id.bview_positive_count);

        ratepositive = (View) view.findViewById(R.id.bview_rate_positive);
        ratepositive.setClickable(true);
        ratepositive.setOnClickListener(ratePositiveListner);

        ratenegative = (View) view.findViewById(R.id.bview_rate_negative);
        ratenegative.setClickable(true);
        ratenegative.setOnClickListener(rateNegativeListner);

        ratenone = (View) view.findViewById(R.id.bview_rate_none);
        ratenone.setClickable(true);
        ratenone.setOnClickListener(rateNoneListner);

        imgratepositive = (ImageView) view.findViewById(R.id.bview_rate_positive_img);
        imgratenegative = (ImageView) view.findViewById(R.id.bview_rate_negative_img);
        imgratenone = (ImageView) view.findViewById(R.id.bview_rate_none_img);

        briefEmptyInbox = view.findViewById(R.id.brief_empty_inbox);
        briefEmptyRead = view.findViewById(R.id.brief_empty_ignore);
        briefEmptyStar = view.findViewById(R.id.brief_empty_star);
        B.addGoTopTracker(activity,listBriefs);

        TextView txt11 = (TextView) view.findViewById(R.id.bh_txt1);
        TextView txt12 = (TextView) view.findViewById(R.id.bh_txt2);
        TextView txt13 = (TextView) view.findViewById(R.id.bh_txt3);
        TextView txt14 = (TextView) view.findViewById(R.id.bh_txt4);
        TextView txt15 = (TextView) view.findViewById(R.id.bh_txt5);
        TextView txt16 = (TextView) view.findViewById(R.id.bh_txt6);
        B.addStyle(new TextView[]{txt12, txt14, txt16});
        B.addStyleBold(new TextView[]{txt11,txt13,txt15});

        //Tim.printTime("BH-onCreate() finished");
		return view;
	}

    private View getFirstTimeView() {
        firstTime=activity.getLayoutInflater().inflate(R.layout.welcome_start,null);
        firstTime.setTag("--2--1");

        TextView txt1=(TextView) firstTime.findViewById(R.id.welcome_head);
        TextView txt2=(TextView) firstTime.findViewById(R.id.welcome_head_desc);
        TextView txt3=(TextView) firstTime.findViewById(R.id.welcome_findway);
        TextView txt4=(TextView) firstTime.findViewById(R.id.welcome_findway_nav);
        TextView txt5=(TextView) firstTime.findViewById(R.id.welcome_findway_control);
        TextView txt6=(TextView) firstTime.findViewById(R.id.welcome_slide);
        TextView txt7=(TextView) firstTime.findViewById(R.id.welcome_slide_left);
        TextView txt8=(TextView) firstTime.findViewById(R.id.welcome_slide_right);



        TextView close=(TextView) firstTime.findViewById(R.id.welcome_close);
        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //listBriefs.removeHeaderView(firstTime);
                HomeFarm.finishedWithFirstTime();
                adapter.clearHeaderViews();
                adapter.notifyDataSetInvalidated();
                listBriefs.setSelection(0);
                //headerViews.clear();
                doFirstTimeSmsCheck();

            }
        });

        B.addStyle(new TextView[]{txt2,txt4,txt5,txt7,txt8});
        B.addStyleBold(new TextView[]{txt3, txt6,close});
        B.addStyle(txt1,B.FONT_LARGE);

        return firstTime;
    }
    private void doFirstTimeSmsCheck() {
        if(SmsFunctions.canOperateAsDefaultSms() && !SmsFunctions.isDefaultSmsAppForDevice(activity)) {
            View sms = SmsFunctions.getSmsNotDefaultView(activity);
            sms.setTag("--2--1");
            adapter.addHeaderView(sms);
            //listBriefs.addHeaderView(sms);
            adapter.notifyDataSetInvalidated();
            listBriefs.invalidate();
            listBriefs.setSelection(0);
        }
    }
	public void refreshData() {
        //Tim.printTime("BH-refreshData()");
        if(BriefManager.hasDirtyItems()) {
            //if(!BriefService.isAppRestore()) {

            new goRefreshData().execute(true);


        }
        BriefManager.setLimit(0);
        //} else {
            //BLog.e("REFRESH", "--STOP Refresh NOT called for Brief home");
        //}
        //Tim.printTime("BH-refreshData() finished");
	}
    private class goRefreshData  extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {

            boolean changed = false;
            if(BriefManager.hasDirtyItems())
                changed=BriefManager.refresh(BriefManager.getShowRating());
            //listBriefs.invalidate();

            return changed;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            briefEmptyInbox.setVisibility(View.GONE);
            briefEmptyRead.setVisibility(View.GONE);
            briefEmptyStar.setVisibility(View.GONE);
            switch(BriefManager.getShowRating()) {

                case BriefRating.RATING_STAR:
                    listBriefs.setEmptyView(null);
                    listBriefs.setEmptyView(briefEmptyStar);
                    break;
                case BriefRating.RATING_IGNORE:
                    listBriefs.setEmptyView(null);
                    listBriefs.setEmptyView(briefEmptyRead);
                    break;
                default:
                    listBriefs.setEmptyView(null);
                    listBriefs.setEmptyView(briefEmptyInbox);
                    break;
            }

            lastViewableListItem=listBriefs.getFirstVisiblePosition();


            if(result) {
                listBriefs.setAdapter(null);

            }


            if(listBriefs.getAdapter()==null) {


                //BLog.e("BHOME", "adapter was null");
                adapter = new BriefPodAdapterList(activity,BriefManager.getBriefs());

                showIfFirstTime();

                listBriefs.setAdapter(adapter);
                importantCount.setText(BriefManager.getCountPositive()+"");
            }


            if(lastViewableListItem!=0)
                listBriefs.setSelection(lastViewableListItem);

            if(BriefManager.getLimit()>0) {
                BriefManager.setLimit(0);
                callRefreshData.removeCallbacks(refDataRun);
                callRefreshData.postDelayed(refDataRun, 20);
            }
        }
    }
    private void showIfFirstTime() {
        if(HomeFarm.isFirstTime() && adapter.getHeadersCount()==0) {
            adapter.addHeaderView(this.getFirstTimeView());
        }
    }
	@Override
	public void onResume() {
        BriefService.setIsAppStarted(true);
		super.onResume();
        //Tim.printTime("BH-onResume()");

        Bgo.clearBackStack(activity);

        //slidemenu = new BriefMenu(this);
        BriefMenu.ensureMenuOff();

        enableTouchListner();

        listBriefs.setSelection(YPOS);

        //BLog.e("RESUME", "Resume called for Brief home section size: "+State.getSectionsSize());
        setRatingNoneSelected(false);
        refresh();

        //Tim.printTime("BH-onResume() finished");
	}

	public void refresh() {


		ActionBarManager.setActionBarMenu(activity, activity.getString(R.string.title_brief), R.menu.brief_home, R.color.brand);

        lastViewableListItem=listBriefs.getFirstVisiblePosition();
        if(State.hasStateObject(State.SECTION_BRIEF, StateObject.INT_VALUE)) {
            lastViewableListItem = State.getStateObjectInt(State.SECTION_BRIEF, StateObject.INT_VALUE);
            State.clearStateObjects(State.SECTION_BRIEF);
        }

        adapter = new BriefPodAdapterList(activity,BriefManager.getBriefs());
        showIfFirstTime();
        listBriefs.setAdapter(adapter);

        if(lastViewableListItem!=0)
            listBriefs.setSelection(lastViewableListItem);



        ContactsSelectedClipboard.clear();
        BriefManager.activateController(activity);
        if(dialog!=null)
            dialog.dismiss();

        //if(BriefManager.hasDirtyItems()) {
            callRefreshData.removeCallbacks(refDataRun);
            callRefreshData.postDelayed(refDataRun, 10);
        //}


	}
    private Handler callRefreshData = new Handler();
    private Runnable refDataRun = new Runnable() {
        @Override
        public void run() {
            refreshData();
        }
    };
	public OnItemLongClickListener onLongClick = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            //BriefMenu.showPreview(position);
            String tag = (String) view.getTag();
            if (tag.contains("--2")) {

            } else {

                    Brief b = BriefManager.get(position - adapter.getHeadersCount());
                    //Note t = NotesDb.get(position);
                    if (touchListener != null) {
                        if (touchListener.isSwiping()) {

                        } else {

                            if (b != null) {
                                //State.cl
                                if (b.getDBIndex() == -1) {    // hack to signify it is a briefSend object
                                    // is a BriefSend item
                                    long bsid = Long.valueOf(b.getDBid());  // ignore fact is dbid, hacked this in there

                                    BriefSend bs = BriefSendDb.get(bsid);
                                    if (bs != null) {
                                        dialog = new BriefSendDialog(activity, b, bs, thisFragment);
                                        dialog.show();
                                    }


                                } else {
                                    dialog = null;
                                    //StateObject bsob = new StateObject(StateObject.INT_USE_SELECTED_BRIEF_INDEX, Integer.valueOf(position));
                                    StateObject sob = new StateObject(StateObject.INT_USE_SELECTED_INDEX, Integer.valueOf(b.getDBIndex()));
                                    switch (b.getWITH_()) {
                                        case Brief.WITH_EMAIL:
                                            Account acc = AccountsDb.getAccountById(b.getAccountId());
                                            dialog = new EmailDialog(activity, acc, b.getDBIndex(), thisFragment);
                                            break;
                                        case Brief.WITH_NOTES:
                                            Note note = NotesDb.getById(Sf.toInt(b.getDBid()));

                                            dialog = new NotesDialog(activity, note, thisFragment);
                                            break;
                                        case Brief.WITH_SMS:
                                            //State.clearStateObjects();
                                            //State.addToState(sob);
                                            SmsMsg msg = SmsDb.getByById(b.getDBid());
                                            dialog = new SmsDialog(activity, msg, thisFragment);
                                            break;
                                        case Brief.WITH_PHONE:
                                            State.clearStateObjects(State.SECTION_CONTACTS_ITEM);
                                            //State.addToState(State.SECTION_CONTACTS_ITEM, bsob);
                                            StateObject sobz = new StateObject(StateObject.STRING_ID, b.getPersonId());
                                            State.addToState(State.SECTION_CONTACTS_ITEM, sobz);
                                            Bgo.openFragmentBackStackAnimate(activity, ContactViewFragment.class);
                                            break;
                                        case Brief.WITH_NEWS:
                                            RssItem rss = NewsItemsDb.getById(Sf.toLong(b.getDBid()));
                                            if(rss!=null) {
                                                dialog = new NewsDialog(activity, rss, thisFragment);
                                            }
                                            /*
                                            State.clearStateObjects(State.SECTION_NEWS_VIEW);
                                            State.addToState(State.SECTION_NEWS_VIEW, sob);
                                            Bgo.openFragmentBackStackAnimate(activity, ViewNewsItemFragment.class);
                                            break;
                                            */
                                    }


                                    if (dialog != null) {

                                        dialog.show();
                                    }
                                }


                            }

                        }
                    }



            }
            return true;
        }

	};
	private void setRatingNoneSelected(boolean withRefresh) {
		YPOS=0;

		BriefManager.setShowRating(BriefRating.RATING_NONE);

		//briefEmptyInbox.setVisibility(View.GONE);
		//briefEmptyRead.setVisibility(View.GONE);
		//briefEmptyStar.setVisibility(View.GONE);
		//BriefHomeManager.refresh();
		ratepositive.setBackgroundResource(R.drawable.border_tab_bottom_noselect);
		ratenone.setBackgroundResource(R.drawable.border_tab_bottom);
		ratenegative.setBackgroundResource(R.drawable.border_tab_bottom_noselect);
		
		imgratenone.setAlpha(1F);
		imgratepositive.setAlpha(0.3F);
		imgratenegative.setAlpha(0.2F);
		touchListener.setDismissLeftRight(true,true);
        if(withRefresh)
            refreshRunnable();

	}
    private void refreshRunnable() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshData();
            }
        },50);
    }
	private void setPositiveSelected() {
		YPOS=0;

		BriefManager.setShowRating(BriefRating.RATING_STAR);

		//briefEmptyInbox.setVisibility(View.GONE);
		//briefEmptyRead.setVisibility(View.GONE);
		//briefEmptyStar.setVisibility(View.GONE);
		
		//BriefHomeManager.refresh();
		ratepositive.setBackgroundResource(R.drawable.border_tab_bottom);
		ratenegative.setBackgroundResource(R.drawable.border_tab_bottom_noselect);
		ratenone.setBackgroundResource(R.drawable.border_tab_bottom_noselect);
		imgratenone.setAlpha(0.3F);
		imgratepositive.setAlpha(1F);
		imgratenegative.setAlpha(0.2F);
		
		touchListener.setDismissLeftRight(true,false);
        refreshRunnable();
	}
	protected OnClickListener ratePositiveListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//riefRatingsDb.refresh(activity);
			setPositiveSelected();
			
		}
	};	
	protected OnClickListener rateNegativeListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//BriefRatingsDb.refresh(activity);
			YPOS=0;

			BriefManager.setShowRating(BriefRating.RATING_IGNORE);


			//BriefManager.setDirtyAllItems();
			//BriefHomeManager.refresh();
			ratepositive.setBackgroundResource(R.drawable.border_tab_bottom_noselect);
			ratenegative.setBackgroundResource(R.drawable.border_tab_bottom);
			ratenone.setBackgroundResource(R.drawable.border_tab_bottom_noselect);
			imgratenone.setAlpha(0.3F);
			imgratepositive.setAlpha(0.3F);
			imgratenegative.setAlpha(1F);
			touchListener.setDismissLeftRight(false,true);
            refreshRunnable();

		}
	};	
	protected OnClickListener rateNoneListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//BriefRatingsDb.refresh(activity);
			setRatingNoneSelected(true);

		}
	};	


	@Override
	public void onPause() {
		super.onPause();
		State.addToState(State.SECTION_BRIEF,new StateObject(StateObject.INT_VALUE,listBriefs.getFirstVisiblePosition()));
        B.removeGoTopTracker();
		//listBriefs.setOnItemClickListener(null);
	}
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_new_brief:
				Bgo.openFragmentBackStack(activity,NewActionFragment.class);
			    break;
		}	
		return false;
	}
	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			lastViewableListItem = listBriefs.getFirstVisiblePosition();
            String tag=(String)view.getTag();
            if(tag.contains("--2")) {

            } else {
                int headersSize=adapter.getHeadersCount();
                Brief b = BriefManager.get(position-headersSize);
                if (b.getDBIndex() == -1) {    // hack to signify it is a briefSend object
                    // is a BriefSend item
                    long bsid = Long.valueOf(b.getDBid());  // ignore fact is dbid, hacked this in there

                    BriefSend bs = BriefSendDb.get(bsid);
                    if (bs != null) {
                        dialog = new BriefSendDialog(activity, b, bs, thisFragment);
                        dialog.show();
                    }
                    view.setBackgroundColor(activity.getResources().getColor(R.color.item_selected_brief));
                    //BitmapDrawable.


                } else {
                    //int less = listBriefs.getHeaderViewsCount();
                    view.setBackgroundColor(activity.getResources().getColor(R.color.item_selected_brief));
                    BriefManager.openBriefItem(activity, position-headersSize);

                }
            }
			
		}
	};

	public void disableTouchListner() {
		listBriefs.setOnTouchListener(null);
	}
	public void enableTouchListner() {
		if(touchListener==null) {
		 touchListener = new SwipeDismissListViewTouchListener(
        		 listBriefs,
                 new SwipeDismissListViewTouchListener.OnDismissCallback() {
                     @Override
                     public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                         for (int position : reverseSortedPositions) {
                        	 if(SwipeDismissListViewTouchListener.isDismissRight()) {
                        		 BriefManager.setRateItemStar(position);
                        	 } else {
                        		 BriefManager.setRateItemIgnore(position);
                        	 }
                        	 importantCount.setText(BriefManager.getCountPositive()+"");
                             BriefManager.setDirtyAllItems();
                             refreshData();
                        	 //listBriefs.removeView(listView.getChildAt(position));
                         }
                         //refresh();
                     }
                 });
		}
		 listBriefs.setOnTouchListener(touchListener);
	}

}
