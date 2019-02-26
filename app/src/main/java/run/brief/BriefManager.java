package run.brief;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.bMaxMinLinearLayout;
import run.brief.b.fab.Fab;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.BriefObject;
import run.brief.beans.BriefOut;
import run.brief.beans.BriefRating;
import run.brief.beans.BriefSend;
import run.brief.beans.BriefSettings;
import run.brief.beans.Email;
import run.brief.beans.Person;
import run.brief.beans.PersonFull;
import run.brief.beans.Phonecall;
import run.brief.beans.RssItem;
import run.brief.beans.SearchResult;
import run.brief.contacts.ContactViewFragment;
import run.brief.contacts.ContactsDb;
import run.brief.email.EmailService;
import run.brief.email.EmailServiceInstance;
import run.brief.email.EmailViewFragment;
import run.brief.menu.BriefMenu;
import run.brief.news.NewsFeedsDb;
import run.brief.news.NewsItemsDb;
import run.brief.news.ViewNewsItemFragment;
import run.brief.notes.NotesDb;
import run.brief.notes.NotesEditFragment;
import run.brief.phone.PhoneDb;
import run.brief.secure.Validator;
import run.brief.settings.AccountsDb;
import run.brief.sms.SmsDb;
import run.brief.sms.SmsSendFragment;
import run.brief.util.Cal;
import run.brief.util.Files;
import run.brief.util.Sf;
import run.brief.util.ViewManagerText;

public final class BriefManager {

	//public static final String DIRTY_SMS="sms";
	
	
	private static final BriefManager BM = new BriefManager();
	private static final int GET_COUNT=100;
	
	public static final String IS_DIRTY_SMS="dirty_sms";
	public static final String IS_DIRTY_EMAIL="dirty_email";
	public static final String IS_DIRTY_PHONE="dirty_phone";
	public static final String IS_DIRTY_NOTES="dirty_notes";
	public static final String IS_DIRTY_P2P="dirty_p2p";
	public static final String IS_DIRTY_NEWS="dirty_news";
	public static final String IS_DIRTY_RATINGS="dirty_rating";
	public static final String IS_DIRTY_SEND_ITEMS="dirty_send";
	
	private HashMap<String,Integer> dirtyBriefs = new HashMap<String,Integer>();
	private HashMap<Long,Brief> briefssms=new HashMap<Long,Brief>();
	private HashMap<Long,Brief> briefsnews=new HashMap<Long,Brief>();
	private HashMap<Long,Brief> briefsemail=new HashMap<Long,Brief>();
	private HashMap<Long,Brief> briefsphone=new HashMap<Long,Brief>();
	private HashMap<Long,Brief> briefsp2p=new HashMap<Long,Brief>();
	private HashMap<Long,Brief> briefsnotes=new HashMap<Long,Brief>();
	private HashMap<Long,Brief> briefsends=new HashMap<Long,Brief>();
	private boolean showSendItems=false;

    private BriefHomeFragment briefHome;
    //private int actionBarHeight;

    private int currentRating=0;
    private static int countPositive=0;
    private static Handler controlRefreshHandle;
    private boolean isStarted=false;

    private Object[] briefsSorted=null;
    //private HashMap<String, Long> dirtyDbs = new HashMap<String,Long>();
    //private HashMap<Long,Brief> briefs = new HashMap<Long,Brief>();
    private List<Brief> briefs=new ArrayList();
    private LinkedHashMap<PersonFull,ArrayList<Long>> personsBriefs = new LinkedHashMap<PersonFull,ArrayList<Long>>();

    private ListView listBriefs;
    private BriefPodAdapterList homeListViewAdapter;

/*
	public static void clearSmsBriefs() { BM.briefssms.clear(); }
	public static void clearEmailBriefs() { BM.briefsemail.clear(); }
	public static void clearPhoneBriefs() { BM.briefsphone.clear(); }
	public static void clearNewsBriefs() { BM.briefsnews.clear(); }
	public static void clearNoteBriefs() { BM.briefsnotes.clear(); }
*/
    public static ListView getListBriefs() {
        return BM.listBriefs;
    }
    public static BriefPodAdapterList getListBriefsAdapter() {
        return BM.homeListViewAdapter;
    }
    public static void setHomeListAndAdapter(Activity activity,View briefHomeView) {
        if(BM.listBriefs==null) {
            BM.listBriefs = (ListView) briefHomeView.findViewById(R.id.brief_home_list);

            BM.homeListViewAdapter = new BriefPodAdapterList(activity, BM.briefs);
        }
    }

    public static void refreshBriefsIfDirty(Activity activity) {
        if(hasDirtyItems()) {
            refresh();
            BM.homeListViewAdapter = new BriefPodAdapterList(activity, BM.briefs);
        }
    }



    public static int getCountPositive() {
        return countPositive;
    }


	//private static boolean isDirty=false;
    public static BriefManager getBriefManager() {
        if(Validator.isValidCaller())
            return BM;
        return
                null;
    }
/*
    public static void setActionBarHeight(Activity activity, int h) {
        int ch=48;//android.R.attr.actionBarSize;//Functions.dpToPx(30,activity);

        TypedValue tv = new TypedValue();
        if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
        {
            ch = TypedValue.complexToDimensionPixelSize(tv.data,activity.getResources().getDisplayMetrics());
        }
        if(h<ch)
            BM.actionBarHeight=ch;
        else
            BM.actionBarHeight=h;
        //BLog.e("ACTBH","actionbar height: "+BM.actionBarHeight);
    }
    public static int getActionBarHeight() {
        return BM.actionBarHeight;
    }
	*/
	private Context context;
	private Activity activity;


	public static void init(Context context) {
		BM.context=context;

		BriefRatingsDb.init(context);
		BriefBlockDb.init(context);

	}

    public static void clearController(Activity activity) {
    	BM.activity=activity;
		Fab.hide();

    }

	public static void activateController(Activity activity) {
		Fab.set(activity,null,null,controlClick);
		Fab.setStyle(activity,R.color.brand,R.color.brand_light,R.color.brand_light,R.drawable.content_new);
		Fab.show();
	}

	private static OnClickListener controlClick = new OnClickListener() {
		@Override
		public void onClick(View view) {
			
			if(!BriefMenu.isMenuShowing()) {
				//BLog.e("CONTROL", "click activate menu SHOW");
				BriefMenu.showMenu(true);
			} else {
				//BLog.e("CONTROL", "click activate menu HIDEO");
				BriefMenu.hideMenu();
			}
		}
	};	
	private static OnClickListener showSendsClick = new OnClickListener() {
		@Override
		public void onClick(View view) {

			if(BM.showSendItems) {
				BM.showSendItems=false;
				
				
			} else {
				BM.showSendItems=true;
			}
			BriefManager.setDirty(BriefManager.IS_DIRTY_SEND_ITEMS);

			if(BM.briefHome!=null) {
                BM.briefHome.refresh();
            }
		}
	};	

	
	public static int getShowRating() {
		return BM.currentRating;
	}
	public static void setShowRating(int RATING_) {
		if(RATING_<BriefRating.RATING_IGNORE)
			RATING_=BriefRating.RATING_IGNORE;
		else if(RATING_>BriefRating.RATING_STAR_DOUBLE)
			RATING_=BriefRating.RATING_STAR_DOUBLE;
		
		if(BM.currentRating!=RATING_) {
			//BLog.e("SETD",BM.currentRating+"seeting dirrty 1"+RATING_);
			BriefManager.setDirtyAllItems();
		}
		BM.currentRating=RATING_;
	}

	public static void setDirty(String IS_DIRTY_) {
		BM.dirtyBriefs.put(IS_DIRTY_, 1);
	}
    public static void setDirtyClear() {
        BM.dirtyBriefs.clear();
    }
    public static boolean hasDirtyItems() {return BM.dirtyBriefs.isEmpty()?false:true;}
	public static void setDirtyAllItems() {
		BM.dirtyBriefs.put(IS_DIRTY_SMS, 1);
		BM.dirtyBriefs.put(IS_DIRTY_EMAIL, 1);
		BM.dirtyBriefs.put(IS_DIRTY_PHONE, 1);
		BM.dirtyBriefs.put(IS_DIRTY_NOTES, 1);
		BM.dirtyBriefs.put(IS_DIRTY_P2P, 1);
		BM.dirtyBriefs.put(IS_DIRTY_NEWS, 1);
		BM.dirtyBriefs.put(IS_DIRTY_SEND_ITEMS, 1);
	}
	public static void setDirtyAllItemsIncludingRatings() {
		setDirtyAllItems();
		BM.dirtyBriefs.put(IS_DIRTY_RATINGS, 1);
	}
	public static Brief get(int index) {
		if(index>=0 || index<BM.briefs.size())
            return BM.briefs.get(index);

		return null; 
	}
    public static final List<Brief> getBriefs() {
        return  BM.briefs;
    }
	public static int size() {
		return BM.briefs.size();
	}
	public static boolean refresh() {
		return refresh(BM.context,BriefRating.RATING_NONE);
	}
    public static boolean refresh(int RATING_) {
        return refresh(BM.context,RATING_);
    }

    private static void ensureInits(Context context) {
        SmsDb.init(context);
        //if(!NewsItemsDb.isEmpty()) {
            NewsFeedsDb.init(context);
            NewsItemsDb.init(context);
        //}

        if(NotesDb.isEmpty()) {
            NotesDb.init(context);
        }
        if(PhoneDb.size()==0)
            PhoneDb.init(context);
    }

	private int limit;
	public static void setLimit(int limit) {
		BM.limit=limit;
	}
	public static int getLimit() {
		return BM.limit;
	}

	@SuppressLint("UseSparseArrays")
	public synchronized static boolean refresh(Context context, int RATING_) {
        BM.isStarted=true;
		if(BM.isStarted) {
			if(BM.context==null) {
				init(context);
			}
            ensureInits(context);
            boolean hasPhone = Device.hasPhone();

            long withinTime = Cal.getUnixTime()-(Cal.HOURS_24_IN_MILLIS*7);

			int countP=0;
			boolean haschanges=false;
			if(BM.dirtyBriefs.get(IS_DIRTY_RATINGS)!=null) {
				BriefRatingsDb.refresh(context);
				//BM.dirtyBriefs.remove(IS_DIRTY_RATINGS);
				//BLog.e("HASCHANGE", "1");
				haschanges=true;
			}

			if(BM.dirtyBriefs.get(IS_DIRTY_SEND_ITEMS)!=null) {
				BM.briefsends.clear();
                //BM.showSendItems=true;
                haschanges = true;
			}
            //if(BM.showSendItems) {
			if(true) {
                BriefSendDb.init(context);
                ArrayList<BriefSend> briefsends = BriefSendDb.getAllItems();
                //BLog.e("BSENDS", "brief sends count: "+briefsends.size());
                if (!briefsends.isEmpty() && RATING_ == BriefRating.RATING_NONE) {
                    long key = Cal.getUnixTime();
                    BM.briefsends.clear();
                    for (BriefSend bs : briefsends) {
                        Brief b = bs.getAsBrief(context);
                        if (b != null) {
                            BM.briefsends.put(key, b);
                            key++;
                        } else {
                            //BriefSendDb.remove(bs);
                        }
                    }


                    haschanges = true;
                } else {
                    BM.showSendItems=false;
                }
            }
			//BLog.e("REFBRIEF", "ratings: "+BriefRatingsDb.size()+"   rating vale: "+RATING_);
			//BLog.e("DBBB", ""+BM.dirtyBriefs.get(IS_DIRTY_SMS));
			//
			if(BM.dirtyBriefs.get(IS_DIRTY_SMS)!=null) {
				BM.briefssms.clear();

				haschanges = true;
				if (hasPhone && State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_SMS)) {
					//BLog.e("DBBB", "" + BM.dirtyBriefs.get(IS_DIRTY_SMS));


					Map<String, ArrayList<Brief>> sms = new HashMap<String, ArrayList<Brief>>();
					//for(int i=0; i<SmsDb.size(); i++) {
					int limit = SmsDb.size();
					if (BM.limit > 0 && BM.limit < limit)
						limit = BM.limit;
					for (int i = 0; i < limit; i++) {
						Brief b = SmsDb.getAsBrief(context, i);
						//Brief next=SmsDb.getAsBrief(context,i+1);
						if (b != null && b.getTimestamp() > withinTime) {

							ArrayList<Brief> tmp = sms.get(b.getPersonId());
							if (tmp == null)
								tmp = new ArrayList<Brief>();
							tmp.add(b);
							sms.put(b.getPersonId(), tmp);

						} else
							break;
					}
					//BLog.e("BHM", "sms size: "+sms.size());
					Iterator<String> it = sms.keySet().iterator();
					while (it.hasNext()) {
						String next = it.next();
						//BLog.e("BHM", "pid: "+next);
						ArrayList<Brief> tmp = sms.get(next);
						if (tmp != null && !tmp.isEmpty()) {
							Brief base = tmp.get(0);
							if (tmp.size() > 1) {
								for (int i = 1; i < tmp.size(); i++) {
									if (i > 3)
										break;
									Brief tb = tmp.get(i);
									ArrayList<Brief> tmpb = base.getMessageChain();
									if (tmpb == null)
										tmpb = new ArrayList<Brief>();
									tmpb.add(tb);
									base.setMessageChain(tmpb);
								}
							}

							BriefRating br = BriefRatingsDb.get(base.getRatingsIdentifier());
							if (br != null) {
								if (br.getInt(BriefRating.INT_RATING) == BriefRating.RATING_STAR)
									countP++;
								if (br.getInt(BriefRating.INT_RATING) == RATING_) {
									BM.briefssms.put(base.getTimestamp(), base);
								}
							} else if (RATING_ == BriefRating.RATING_NONE) {
								BM.briefssms.put(base.getTimestamp(), base);
							}

							//nbriefs.put(base.getTimestamp(), base);
						}
					}

				}
			}
			if(BM.dirtyBriefs.get(IS_DIRTY_NEWS)!=null) {
				BM.briefsnews.clear();
				haschanges = true;

				if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_NEWS)) {


					if (!NewsItemsDb.isEmpty()) {
						int limit = NewsItemsDb.size();
						if (BM.limit > 0 && BM.limit < limit)
							limit = BM.limit;
						for (int i = 0; i < limit; i++) {
							RssItem ri = NewsItemsDb.get(i);

							Brief b = NewsItemsDb.getAsBrief(i);
							//if (ri != null)
							//   BLog.e("BRFSHOW", "news can show: " + ri.getBoolean(RssItem.BOOL_SHOW_BRIEF));
							if (b != null && ri != null && b.getTimestamp() > withinTime) {
								if (ri.getBoolean(RssItem.BOOL_SHOW_BRIEF)) {
									BriefRating br = BriefRatingsDb.get(b.getRatingsIdentifier());
									if (br != null) {
										if (br.getInt(BriefRating.INT_RATING) == BriefRating.RATING_STAR)
											countP++;
										if (br.getInt(BriefRating.INT_RATING) == RATING_) {
											BM.briefsnews.put(b.getTimestamp(), b);
										}
									} else if (RATING_ == BriefRating.RATING_NONE) {
										BM.briefsnews.put(b.getTimestamp(), b);
									}
								}
							}

						}

					}



				}
			}
			if(BM.dirtyBriefs.get(IS_DIRTY_NOTES)!=null) {
				BM.briefsnotes.clear();
				haschanges = true;
				if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_NOTES)) {


					if (!NotesDb.isEmpty()) {
						int limit = NotesDb.size();
						if (BM.limit > 0 && BM.limit < limit)
							limit = BM.limit;
						for (int i = 0; i < limit; i++) {
							Brief b = NotesDb.getAsBrief(i);
							if (b != null && b.getTimestamp() > withinTime) {
								BriefRating br = BriefRatingsDb.get(b.getRatingsIdentifier());
								if (br != null) {
									if (br.getInt(BriefRating.INT_RATING) == BriefRating.RATING_STAR)
										countP++;
									if (br.getInt(BriefRating.INT_RATING) == RATING_) {
										BM.briefsnotes.put(b.getTimestamp(), b);
									}
								} else if (RATING_ == BriefRating.RATING_NONE) {
									BM.briefsnotes.put(b.getTimestamp(), b);
								}
							} else
								break;
						}


					}

				}
			}
			if(BM.dirtyBriefs.get(IS_DIRTY_PHONE)!=null) {
				BM.briefsphone.clear();
				haschanges = true;

				if (hasPhone && State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_PHONE)) {

					int limit = PhoneDb.size();
					if (BM.limit > 0 && BM.limit < limit)
						limit = BM.limit;
					for (int i = 0; i < limit; i++) {
						Phonecall call = PhoneDb.get(i);
						if (call != null && call.getInt(Phonecall.INT_DURATION) >= 0) {
							Brief b = new Brief(context, call, i);
							if (b != null && b.getTimestamp() > withinTime) {
								BriefRating br = BriefRatingsDb.get(b.getRatingsIdentifier());
								if (br != null) {
									if (br.getInt(BriefRating.INT_RATING) == BriefRating.RATING_STAR)
										countP++;
									if (br.getInt(BriefRating.INT_RATING) == RATING_) {
										BM.briefsphone.put(b.getTimestamp(), b);
									}
								} else if (RATING_ == BriefRating.RATING_NONE) {
									BM.briefsphone.put(b.getTimestamp(), b);
								}
							} else
								break;
						}

					}

				}
			}
			if(BM.dirtyBriefs.get(IS_DIRTY_EMAIL)!=null) {
				BM.briefsemail.clear();
				haschanges = true;

				if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_EMAIL)) {

					ArrayList<Account> emaccounts = AccountsDb.getAllEmailAccounts();

					if (emaccounts != null) {
						for (Account account : emaccounts) {

							//if(EmailService.hasEmailService(account)) {
							EmailServiceInstance emservice = EmailService.getService(context, account);
							ArrayList<Email> emails = emservice.getEmails();
							int TMP_GET = GET_COUNT > emails.size() ? emails.size() : GET_COUNT;

							int limit = emails.size();
							if (BM.limit > 0 && BM.limit < limit)
								limit = BM.limit;

							for (int i = 0; i < limit; i++) {
								Brief b = emservice.getAsBrief(i);
								if (b != null && b.getTimestamp() > withinTime) {
									b.setAccountId(account.getLong(Account.LONG_ID));
									BriefRating br = BriefRatingsDb.get(b.getRatingsIdentifier());
									if (br != null) {
										if (br.getInt(BriefRating.INT_RATING) == BriefRating.RATING_STAR)
											countP++;
										if (br.getInt(BriefRating.INT_RATING) == RATING_) {
											BM.briefsemail.put(b.getTimestamp(), b);
										}
									} else if (RATING_ == BriefRating.RATING_NONE) {
										BM.briefsemail.put(b.getTimestamp(), b);
									}
								} else
									break;
							}
							//}
						}


					}
					//BM.dirtyBriefs.remove(IS_DIRTY_EMAIL);
					//BLog.e("HASCHANGE", "email");


				}
			}
			boolean returnChanges=haschanges?true:false;
			if(haschanges) {
				if(BM.limit==0)
					BM.dirtyBriefs.clear();
				//BLog.e("BRIEFREFRESH", "brief has changes");
				
				final HashMap<Long,Brief> nbriefs=new HashMap<Long,Brief>(BM.briefsemail);
				if(RATING_==BriefRating.RATING_NONE) {
					nbriefs.putAll(BM.briefsends);
				}
				nbriefs.putAll(BM.briefssms);
				nbriefs.putAll(BM.briefsphone);
				nbriefs.putAll(BM.briefsnews);
				nbriefs.putAll(BM.briefsnotes);
				nbriefs.putAll(BM.briefsp2p);
				
				
				BM.briefsSorted= new TreeSet<Long>(nbriefs.keySet()).descendingSet().toArray();

				BM.briefs.clear();
                for(Object ind: BM.briefsSorted) {
                    BM.briefs.add(nbriefs.get((Long)ind));

                }

				//BM.briefs=nbriefs;
				countPositive=countP;
				haschanges=false;
			}
			return returnChanges;
		} else {
			BM.isStarted=true;
			return true;
		}
	}
    public static View getView(Activity activity, View vi, Brief brief) {
    	//View vi=convertView;
        if(vi==null) {
        	LayoutInflater inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.brief_list_item, null);

        }  else {
            View tst = vi.findViewById(R.id.brief_item_heading);
            if(tst==null) {
                LayoutInflater inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                vi = inflater.inflate(R.layout.brief_list_item, null);
            }
        }
    	if(brief!=null) {

    	        vi.setTag(brief.getRatingsIdentifier());

                if(brief.getState()==Brief.STATE_SENDNG) {
                    vi.setBackgroundColor(activity.getResources().getColor(R.color.black_alpha_light));
                    vi.setPadding(vi.getPaddingLeft(),vi.getPaddingTop(),120,vi.getPaddingBottom());
                } else if(brief.getState()==Brief.STATE_ERROR) {
                    vi.setBackgroundColor(activity.getResources().getColor(R.color.red_alpha));
                    vi.setPadding(vi.getPaddingLeft(),vi.getPaddingTop(),120,vi.getPaddingBottom());
                } else {
                    vi.setBackgroundColor(0);
                    vi.setPadding(vi.getPaddingLeft(),vi.getPaddingTop(),0,vi.getPaddingBottom());
                }
    	        //vi.setVisibility(View.VISIBLE);
    	        //TextView head = (TextView)vi.findViewById(R.id.brief_item_head); 
    	        TextView text = (TextView)vi.findViewById(R.id.brief_item_text); 

    	        // Setting all values in listview
    	        View bihead = vi.findViewById(R.id.brief_item_heading);
    	        bihead.setVisibility(View.VISIBLE);
    			ImageView withimage = (ImageView) vi.findViewById(R.id.brief_item_with);
    			withimage.setVisibility(View.VISIBLE);
    			
    	        if(brief!=null) {
    	        	//BriefRating br = 
    	        	String add=brief.getMessage().length()>200?"...":"";
    	        	text.setText(Sf.restrictLength(brief.getMessage(), 200)+ add);
    	        	ViewManagerText.manageTextView(activity, text);
    	        	
    	        	styleViewWith(activity,vi,brief);
    	        	bMaxMinLinearLayout showChain = (bMaxMinLinearLayout) vi.findViewById(R.id.brief_show_chain);


    	        	showChain.removeAllViews();
    	        	if(brief.getMessageChain()!=null && !brief.getMessageChain().isEmpty()) {
                        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                        lp.addRule(RelativeLayout.BELOW, R.id.brief_item_pod_bg);
    	        		if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        } else {
                            lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        }
                        showChain.setLayoutParams(lp);
                        showChain.setMinMaxWidth(200, 800);

    	        		for(Brief b: brief.getMessageChain()) {
    	        			View addview = getChainView(activity,null,b);
    	        			View bih = addview.findViewById(R.id.brief_item_heading);
    	        			//bih.setVisibility(View.GONE);
    	        			ImageView withimg = (ImageView) addview.findViewById(R.id.brief_item_with);
    	        			//withimg.setVisibility(View.GONE);
    	        			addview.setAlpha(0.75F);
    	        			showChain.addView(addview);
    	        		}
    	        	}
    	        }
    			
    			
    			
    			
    			

    	}
    	
    	
    	
        

        //addview.setAlpha(1F);

		
        return vi;
    }
    private static final int restrictChainViewText=50;
    public static View getChainView(Activity activity, View convertView, Brief brief) {
        View vi=convertView;
        if(convertView==null) {
            LayoutInflater inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	vi = inflater.inflate(R.layout.brief_list_item_chain, null);

        }

        
        //vi.setVisibility(View.VISIBLE);
        //TextView head = (TextView)vi.findViewById(R.id.brief_item_head); 
        TextView text = (TextView)vi.findViewById(R.id.brief_item_text);

        B.addStyle(text, B.FONT_SMALL);

        // Setting all values in listview
        if(brief!=null) {
        	//BriefRating br = 
        	String add=brief.getMessage().length()>restrictChainViewText?"...":"";
        	text.setText(Sf.restrictLength(brief.getMessage(), restrictChainViewText)+ add);
        	ViewManagerText.manageTextView(activity, text);
        	
        	styleViewWith(activity,vi,brief);
        }
        //addview.setAlpha(1F);
        View bihead = vi.findViewById(R.id.brief_item_heading);
        bihead.setVisibility(View.GONE);
		ImageView withimage = (ImageView) vi.findViewById(R.id.brief_item_with);
		withimage.setVisibility(View.GONE);
		
        return vi;
    }
    public static View getView(Activity activity, View convertView, SearchResult result) {
        View vi=convertView;
        if(convertView==null) {
            LayoutInflater inflater=(LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        	vi = inflater.inflate(R.layout.brief_list_item, null);
            
        }
        //vi.setVisibility(View.VISIBLE);
        TextView head = (TextView)vi.findViewById(R.id.brief_item_head); 
        TextView text = (TextView)vi.findViewById(R.id.brief_item_text); 

        if(result!=null) {
        	//result.
        	head.setText(result.getSubject());
        	text.setText(result.getMessage());

        	styleViewWith(activity,vi,result);
        		
        }
        return vi;
    }
    public static void setRateItemIgnore(int position) {
		Brief brief=BriefManager.get(position);
		if(brief!=null) {
			String identifier = BriefRating.makeRatingsIdentifier(brief.getWITH_(), brief.getDBid(),brief.getAccountId());
			BriefRating br = BriefRatingsDb.get(identifier);
			if(br==null) {
				br = new BriefRating();
				br.setString(BriefRating.STRING_ITEM_IDENTIFIER,identifier);
				br.setInt(BriefRating.INT_MAX_HIT_RATING,BriefRating.RATING_NONE);
				br.setInt(BriefRating.INT_RATING,BriefRating.RATING_IGNORE);
				br.setLong(BriefRating.LONG_DATE,brief.getTimestamp());
				BriefRatingsDb.add(br);
			} else {
				if(br.getInt(BriefRating.INT_RATING)==BriefRating.RATING_STAR)
					countPositive--;
				br.setInt(BriefRating.INT_RATING,BriefRating.RATING_IGNORE);
				BriefRatingsDb.update(br);
			}
		}
    }
    public static void setRateItemStar(int position) {
		Brief brief=BriefManager.get(position);
		if(brief!=null) {
			String identifier = BriefRating.makeRatingsIdentifier(brief.getWITH_(), brief.getDBid(),brief.getAccountId());
			BriefRating br = BriefRatingsDb.get(identifier);
			if(br==null) {
				br = new BriefRating();
				br.setString(BriefRating.STRING_ITEM_IDENTIFIER,identifier);
				br.setInt(BriefRating.INT_MAX_HIT_RATING,BriefRating.RATING_STAR);
				br.setInt(BriefRating.INT_RATING,BriefRating.RATING_STAR);
				br.setLong(BriefRating.LONG_DATE,brief.getTimestamp());
				BriefRatingsDb.add(br);
				countPositive++;
			} else {
				br.setInt(BriefRating.INT_RATING,BriefRating.RATING_STAR);
				BriefRatingsDb.update(br);
				countPositive++;
			}
		}
    }
    public static void setRateItemNone(int position) {
		Brief brief=BriefManager.get(position);
		if(brief!=null) {
			String identifier = BriefRating.makeRatingsIdentifier(brief.getWITH_(), brief.getDBid(),brief.getAccountId());
			BriefRating br = BriefRatingsDb.get(identifier);
			if(br==null) {
				br = new BriefRating();
				br.setString(BriefRating.STRING_ITEM_IDENTIFIER,identifier);
				br.setInt(BriefRating.INT_MAX_HIT_RATING,BriefRating.RATING_NONE);
				br.setInt(BriefRating.INT_RATING,BriefRating.RATING_NONE);
				br.setLong(BriefRating.LONG_DATE,brief.getTimestamp());
				BriefRatingsDb.add(br);
			} else {
				br.setInt(BriefRating.INT_RATING,BriefRating.RATING_STAR);
				BriefRatingsDb.update(br);
			}
		}
    }
	public static void openBriefItem(Activity activity, int position) {
        Brief brief = BriefManager.get(position);
        openBriefItem(activity,brief);

    }
    public static void openBriefItem(Activity activity, Brief brief) {

        openBriefItem(activity,brief,false);
    }
    public static void openBriefItem(Activity activity, Brief brief, boolean backstack) {

		if(brief!=null) {
            //BLog.e("SSr", "sr: " + brief.getDBid());
			StateObject sob = new StateObject(StateObject.INT_USE_SELECTED_INDEX,Integer.valueOf(brief.getDBIndex()));
			StateObject dsob = new StateObject(StateObject.STRING_USE_DATABASE_ID,brief.getDBid());
			switch(brief.getWITH_()) {
				case Brief.WITH_EMAIL:
					State.clearStateObjects(State.SECTION_EMAIL_VIEW);
					StateObject soba = new StateObject(StateObject.LONG_USE_ACCOUNT_ID,Long.valueOf(brief.getAccountId()));
					//State.addToState(State.SECTION_EMAIL_VIEW,sob);
                    State.addToState(State.SECTION_EMAIL_VIEW,dsob);
					State.addToState(State.SECTION_EMAIL_VIEW,soba);
                    if(backstack)
					    Bgo.openFragmentBackStackAnimate(activity, EmailViewFragment.class);
                    else
                        Bgo.openFragmentAnimate(activity, EmailViewFragment.class);
					break;
				case Brief.WITH_SMS:
					State.clearStateObjects(State.SECTION_SMS_SEND);
                    State.addToState(State.SECTION_SMS_SEND,dsob);
                    if(backstack)
                        Bgo.openFragmentBackStackAnimate(activity, SmsSendFragment.class);
                    else
					    Bgo.openFragmentAnimate(activity, SmsSendFragment.class);
					break;
				case Brief.WITH_PHONE:
					State.clearStateObjects(State.SECTION_CONTACTS_ITEM);
                    StateObject sobz = new StateObject(StateObject.STRING_ID,brief.getPersonId());
                    State.addToState(State.SECTION_CONTACTS_ITEM,sobz);
                    if(backstack)
                        Bgo.openFragmentBackStackAnimate(activity, ContactViewFragment.class);
                    else
                        Bgo.openFragmentAnimate(activity, ContactViewFragment.class);
					break;
				case Brief.WITH_NOTES:
					State.clearStateObjects(State.SECTION_NOTES_ITEM);
                    State.addToState(State.SECTION_NOTES_ITEM,dsob);
                    if(backstack)
                        Bgo.openFragmentBackStackAnimate(activity, NotesEditFragment.class);
                    else
                        Bgo.openFragmentAnimate(activity, NotesEditFragment.class);
					break;
				case Brief.WITH_NEWS:

					State.clearStateObjects(State.SECTION_NEWS_VIEW);
                    State.addToState(State.SECTION_NEWS_VIEW,dsob);
                    if(backstack)
                        Bgo.openFragmentBackStackAnimate(activity, ViewNewsItemFragment.class);
                    else
                        Bgo.openFragmentAnimate(activity, ViewNewsItemFragment.class);
					//showPreviewScreen(position);
					//previewWithNews(); 
					break;
                case Brief.WITH_PERSON:

                    State.clearStateObjects(State.SECTION_CONTACTS_ITEM);
                    State.addToState(State.SECTION_CONTACTS_ITEM,new StateObject(StateObject.STRING_ID,brief.getPersonId()));
                    if(backstack)
                        Bgo.openFragmentBackStackAnimate(activity, ContactViewFragment.class);
                    else
                        Bgo.openFragmentAnimate(activity, ContactViewFragment.class);
                    break;
			}
		} else {
			// item null show error
		}

	}
    
    
    public static void styleViewWith(Activity activity, View vi,SearchResult result) {

    	styleViewWith(activity, vi, (Brief) result);
    }
    public static void styleViewWith(Activity activity, View vi, Brief brief) {
    	View pod=(View) vi.findViewById(R.id.brief_item_pod_holder);
    	RelativeLayout poditem=(RelativeLayout) vi.findViewById(R.id.brief_item_pod);
    	TextView date=(TextView) vi.findViewById(R.id.brief_item_date);
    	ImageView personicon = (ImageView) vi.findViewById(R.id.brief_item_person);
    	//ImageView timg = (ImageView) vi.findViewById(R.id.brief_item_type);
    	ImageView wimg = (ImageView) vi.findViewById(R.id.brief_item_with);
    	TextView head = (TextView)vi.findViewById(R.id.brief_item_head);
        B.addStyleBold(head);
        View leftPod=vi.findViewById(R.id.checkbox_pod);

    	View heading = vi.findViewById(R.id.brief_item_heading);
        LinearLayout podBg = (LinearLayout) vi.findViewById(R.id.brief_item_pod_bg);
    	//LinearLayout objHolder = (LinearLayout) vi.findViewById(R.id.brief_objects_holder);
    	//ImageView status = (ImageView) vi.findViewById(R.id.brief_status_img);
    	date.setText("");
    	
    	int style = State.getSettings().getInt(BriefSettings.INT_STYLE_LIST);
    	
    	if(brief!=null) {
    		
    		Drawable statusImage=null;  
    		switch(brief.getState()) {
    		case Brief.STATE_ERROR:
    			statusImage = activity.getResources().getDrawable(R.drawable.i_cross);

    			break;
    		case Brief.STATE_SENDNG:
    			statusImage = activity.getResources().getDrawable(R.drawable.i_wait);
    			break;
    		default:
    			//statusImage = activity.getResources().getDrawable(R.drawable.i_tick);
    			break;
    		}
    		TextView itext = (TextView)vi.findViewById(R.id.brief_item_text);
            B.addStyle(itext);
            itext.setVisibility(View.VISIBLE);

            itext.setGravity(Gravity.LEFT);
            itext.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);

    		if(statusImage!=null) {
    			itext.setCompoundDrawablesWithIntrinsicBounds(null, null, statusImage, null);
    		} else {
                itext.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            }
    		
    		Person p=null;
    		ArrayList<BriefOut> outs=brief.getBriefOuts();
    		if(brief.getPersonId()!=null && brief.getPersonId().length()>0) {
    			p=ContactsDb.getWithPersonId(activity, brief.getPersonId());

    		} 

        	if(p==null) {
                //if()
				p= Person.getNewUnknownPerson(activity, brief.getSubject(), null);

			}
			personicon.setVisibility(View.VISIBLE);    
			personicon.setImageBitmap(p.getThumbnail(activity));
			personicon.refreshDrawableState();
			
			Cal cal=new Cal(brief.getTimestamp());
			date.setText(Cal.friendlyReadDate(cal)+"\n"+cal.getTimeHHMM());
            date.setVisibility(View.VISIBLE);
			//LinearLayout briefobjectsholder = (LinearLayout) vi.findViewById(R.id.brief_objects_files);
			//briefobjectsholder.setVisibility(View.GONE);
			//briefobjectsholder.removeAllViews();
			clearBriefObjectsInView(vi);
			heading.setVisibility(View.VISIBLE);
            head.setPadding(60,0,0,0);

			switch(brief.getWITH_()) {
				case Brief.WITH_EMAIL:
                    if(brief.getTYPE_()==Brief.TYPE_IN) {
                        head.setText(p.getString(Person.STRING_NAME));
                    } else {
                        head.setText(brief.getBriefOuts().get(0).getTo());
                    }

					showBriefObjectsInView(vi,brief);
					break;
				case Brief.WITH_SMS:
					head.setText(p.getString(Person.STRING_NAME));
                    //ViewManagerText.reziseEmojii(activity,new SpannableString(itext.getText()));

                    break;
					//head.setVisibility(View.GONE);
				case Brief.WITH_PHONE:
					if(p!=null)
						head.setText(p.getString(Person.STRING_NAME));
                    itext.setGravity(Gravity.RIGHT);
                    itext.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.i_wait,0);
					break;
				case Brief.WITH_NEWS:
					BriefOut o = brief.getBriefOuts().get(0);
					//date.setText(o.getTo());
					head.setText(brief.getSubject());
                    personicon.setVisibility(View.GONE);
                    head.setPadding(0,0,0,0);
                    if(State.getSettings().getInt(BriefSettings.INT_BRIEF_SHOW_NEWS_STYLE)==1)
                        itext.setVisibility(View.GONE);
					break;
				case Brief.WITH_NOTES:
					showBriefObjectsInView(vi,brief);
					heading.setVisibility(View.GONE);
					break;
				default:
					heading.setVisibility(View.GONE);
					break;

			}


	        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                poditem.setPadding(80, 0, 0, 0);
                poditem.setGravity(Gravity.RIGHT);
                RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                podBg.setLayoutParams(lp);

	        } else {
                poditem.setPadding(0, 0, 60, 0);
                poditem.setGravity(Gravity.LEFT);
                RelativeLayout.LayoutParams lp=new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                podBg.setLayoutParams(lp);

	        }
	    	//((bMaxMinLinearLayout) podBg).setMinMaxWidth(400, 600);
	    	((bMaxMinLinearLayout) podBg).setMinMaxHeight(20, 200);
	    	((bMaxMinLinearLayout) podBg).setMinMaxWidth(200, 600);

            leftPod.setBackgroundColor(activity.getResources().getColor(R.color.transparent));

	    	switch(brief.getWITH_()) {
                case Brief.WITH_EMAIL:
                    if(style==BriefSettings.STYLE_LIST_PODS) {
                        if(brief.getTYPE_()==Brief.TYPE_IN) {
                            podBg.setBackground(B.getDrawable(activity,R.drawable.pod_email_in));
                        } else {
                            podBg.setBackground(B.getDrawable(activity,R.drawable.pod_email_out));
                        }
                        //pod.setVisibility(View.GONE);
                        //txt.setScaleType(ScaleType.FIT_XY);
                    } else if(style==BriefSettings.STYLE_LIST_COLOR) {
                        vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_email));
                        if(brief.getTYPE_()==Brief.TYPE_IN) {
                            pod.setBackgroundResource(R.drawable.brief_item_email);
                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_email_out);
                        }

                    } else {
                        leftPod.setBackgroundColor(activity.getResources().getColor(R.color.white_alpha_vlight));
                        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            pod.setBackgroundResource(R.drawable.brief_item_plain_out);

                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_plain);
                        }
                    }
                    Account account = AccountsDb.getAccountById(brief.getAccountId());
                    if(account!=null)
                        wimg.setImageDrawable(B.getDrawable(activity,account.getAccountRIcon()));
                    else
                        wimg.setImageDrawable(B.getDrawable(activity,R.drawable.i_email));
                    break;
                case Brief.WITH_NEWS:
                    if(style==BriefSettings.STYLE_LIST_PODS) {
                        //if(brief.getTYPE_()==Brief.TYPE_IN) {
                        podBg.setBackground(B.getDrawable(activity,R.drawable.pod_news_in));

                    } else if(style==BriefSettings.STYLE_LIST_COLOR) {
                        vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_news));

                        pod.setBackgroundResource(R.drawable.brief_item_news);
                    } else {
                        leftPod.setBackgroundColor(activity.getResources().getColor(R.color.white_alpha_vlight));
                        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            pod.setBackgroundResource(R.drawable.brief_item_plain_out);

                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_plain);
                        }
                    }
                    wimg.setImageDrawable(B.getDrawable(activity,R.drawable.i_news));
                    break;
                case Brief.WITH_SMS:
                    if(style==BriefSettings.STYLE_LIST_PODS) {
                        if(brief.getTYPE_()==Brief.TYPE_IN) {
                            podBg.setBackground(B.getDrawable(activity,R.drawable.pod_sms_in));
                        } else {
                            podBg.setBackground(B.getDrawable(activity,R.drawable.pod_sms_out));
                        }
                        //pod.setVisibility(View.GONE);
                    } else if(style==BriefSettings.STYLE_LIST_COLOR) {
                        vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_sms));
                        //pod.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_sms_pod));
                        if(brief.getTYPE_()==Brief.TYPE_IN) {
                            pod.setBackgroundResource(R.drawable.brief_item_sms);
                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_sms_out);
                        }

                    } else {
                        leftPod.setBackgroundColor(activity.getResources().getColor(R.color.white_alpha_vlight));
                        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            pod.setBackgroundResource(R.drawable.brief_item_plain_out);

                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_plain);
                        }
                    }
                    wimg.setImageDrawable(B.getDrawable(activity,R.drawable.i_sms));
                    break;
                case Brief.WITH_NOTES:
                    if(style==BriefSettings.STYLE_LIST_PODS) {
                        podBg.setBackground(B.getDrawable(activity,R.drawable.pod_note_out));
                        //pod.setVisibility(View.GONE);
                    } else if(style==BriefSettings.STYLE_LIST_COLOR) {
                        vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_notes));
    //					/pod.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_notes_pod));
                        pod.setBackgroundResource(R.drawable.brief_item_note_created);
                    } else {
                        leftPod.setBackgroundColor(activity.getResources().getColor(R.color.white_alpha_vlight));
                        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            pod.setBackgroundResource(R.drawable.brief_item_plain_out);

                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_plain);
                        }
                    }
                    wimg.setImageDrawable(B.getDrawable(activity,R.drawable.i_note));
                    break;
                case Brief.WITH_PHONE:
                    if(style==BriefSettings.STYLE_LIST_PODS) {
                        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            podBg.setBackground(B.getDrawable(activity,R.drawable.pod_phone_out));

                        } else {
                            podBg.setBackground(B.getDrawable(activity,R.drawable.pod_phone_in));
                        }
                        //pod.setVisibility(View.GONE);
                        //txt.setScaleType(ScaleType.FIT_XY);
                    } else if(style==BriefSettings.STYLE_LIST_COLOR) {
                        vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_phone));
                        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            pod.setBackgroundResource(R.drawable.brief_item_phone_out);

                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_phone);
                        }

                    } else {
                        leftPod.setBackgroundColor(activity.getResources().getColor(R.color.white_alpha_vlight));
                        if(brief.getTYPE_()==Brief.TYPE_OUT) {
                            pod.setBackgroundResource(R.drawable.brief_item_plain_out);

                        } else {
                            pod.setBackgroundResource(R.drawable.brief_item_plain);
                        }
                    }
                    if(brief.getTYPE_()==Brief.TYPE_MISSED)
                        wimg.setImageDrawable(B.getDrawable(activity,R.drawable.i_direct_off));
                    else
                        wimg.setImageDrawable(B.getDrawable(activity,R.drawable.i_phone));
                    break;
                case Brief.WITH_PERSON:
                    if(p!=null) {
                        if (style == BriefSettings.STYLE_LIST_PODS) {
                            podBg.setBackground(B.getDrawable(activity,R.drawable.pod_phone_out));

                        } else if (style == BriefSettings.STYLE_LIST_COLOR) {
                            vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_phone));
                            pod.setBackgroundResource(R.drawable.brief_item_phone_out);

                        } else {

                        }
                        date.setVisibility(View.GONE);
                        wimg.setImageDrawable(B.getDrawable(activity,R.drawable.i_social));
                    }
                    break;
                default:
                    vi.setBackgroundColor(activity.getResources().getColor(R.color.actionbar_general));
                    wimg.setImageDrawable(B.getDrawable(activity,R.drawable.action_help));
                    break;
	    	}

    	} else {
    		//date.setText("err2 - None");
    	}
    	
    	

    }
	public static int getActionBarHeight(Activity activity) {
		return ((AppCompatActivity) activity).getSupportActionBar().getHeight();
	}
    private static void clearBriefObjectsInView(View vi) {
    	View objpod=vi.findViewById(R.id.brief_objects);
    	objpod.setVisibility(View.GONE);
    	LinearLayout l = (LinearLayout) vi.findViewById(R.id.brief_objects_files);
    	l.removeAllViews();
    	l.setVisibility(View.GONE);
    }
    private static void showBriefObjectsInView(View vi, Brief b) {
    	LinearLayout l = (LinearLayout) vi.findViewById(R.id.brief_objects_files);
    	l.removeAllViews();
    	View objpod=vi.findViewById(R.id.brief_objects);
    	if(!b.getBriefObjects().isEmpty()) {
    		//BLog.e("BOB", "HAS_A_BOB");
    		l.setVisibility(View.VISIBLE);
    		objpod.setVisibility(View.VISIBLE);
    		for(int i=0; i<b.getBriefObjects().size() && i<4;i++) {
    			BriefObject bob= b.getBriefObjects().get(i);
    			if(bob.getUri().length()>2 && bob.getType()==BriefObject.TYPE_FILE_SD) {
    				ImageView bimg = new ImageView(vi.getContext());
    				//BLog.e("BOB", bob.getUri());
                    //String uri=bob.getUri();
                    //if(uri.startsWith(Files.HOME_PATH_FILES)) {
                    //    uri=Files.removeBriefFileExtension(uri);
                    //}
    				File f = new File(bob.getUri());
    				if(f!=null) {
    					bimg.setBackground(Files.getFileIcon(vi.getContext(), bob.getUri()));
    				}
    				bimg.setLayoutParams(new LayoutParams(40,40));
    				l.addView(bimg);
    			}
    		}
            if(b.getBriefObjects().size()>4) {
                TextView txtx = new TextView(vi.getContext());
                txtx.setText("+");
                l.addView(txtx);
            }
    	} else {
    		l.setVisibility(View.GONE);
    		objpod.setVisibility(View.GONE);
    	}
    	
    }
}
