package run.brief.email;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import run.brief.BriefManager;
import run.brief.BriefSendDb;
import run.brief.BriefSendDialog;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.SwipeRefreshLayout;
import run.brief.b.SwipeRefreshLayout.OnRefreshListener;
import run.brief.b.bButton;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.BriefSend;
import run.brief.beans.Email;
import run.brief.menu.BriefMenu;
import run.brief.service.BriefService;
import run.brief.settings.AccountsDb;
import run.brief.settings.AccountsHomeFragment;
import run.brief.util.Sf;
import run.brief.util.log.BLog;

public class EmailHomeFragment extends BFragment implements BRefreshable {
	//ImapService imap;
	private EmailHomeFragment thisFragment;
	private Handler showHideAccount=new Handler();
	private Handler showHideOpenAccount=new Handler();
	private EmailListAdapter adapter;
	private ListView list;
	private View view;
	private Activity activity;
	private EmailServiceInstance emailService;
	private ArrayList<Account> emailAccounts;
	private static Account currentAccount;
	private EmailDialog popupMenu;
	
	private RelativeLayout emailScreenAccount;
	private RelativeLayout emailScreenNone;
	private RelativeLayout emailScreenChoose;
	private SwipeRefreshLayout mSwipeRefreshLayout;
	private ListView chooseList;
	private EmailAccountsAdapter eAccounts;
	
	public static final int RESULT_REFESH=150;
	private CollectLatestMessages collectMessages=null;

    private RelativeLayout flay;

    private int lastPosition;
    private Dialog dialog;
    private Handler checkEmailHandler = new Handler();
    private View checkEmailSync;

    private boolean syncShowing=false;
	//EmailService emails;
    private Runnable isSyncingCheck = new Runnable() {
        @Override
        public void run() {
            if(currentAccount!=null) {

                if(BriefService.isAccountActiveSyncing(currentAccount.getLong(Account.LONG_ID))) {
                    showEmailSync();
                } else {
                    hideEmailSync();
                }
                checkEmailHandler.postDelayed(isSyncingCheck,100);
            }
        }
    };
	private void hideEmailSync() {
        if(syncShowing) {
            syncShowing=false;
            checkEmailSync.setVisibility(View.GONE);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, 0);
            checkEmailSync.setLayoutParams(lp);
            Animation ani = AnimationUtils.loadAnimation(activity, R.anim.slide_out_to_top);
            checkEmailSync.setAnimation(ani);
            checkEmailSync.startAnimation(ani);
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }
    private void showEmailSync() {
        if(!syncShowing) {
            syncShowing=true;
            checkEmailSync.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            checkEmailSync.setLayoutParams(lp);
            Animation ani = AnimationUtils.loadAnimation(activity, R.anim.slide_in_from_top);
            checkEmailSync.setAnimation(ani);
            checkEmailSync.startAnimation(ani);
            checkEmailSync.bringToFront();
        }
    }
	public static void restart() {
		currentAccount=null;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		
		thisFragment=this;
        BriefService.ensureStartups(activity);
		activity=getActivity();
		view=inflater.inflate(R.layout.email,container, false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		BriefMenu.ensureMenuOff();
		BriefManager.activateController(activity);
		Bgo.clearBackStack(activity);

		State.setCurrentSection(State.SECTION_EMAIL);
		
		emailAccounts = AccountsDb.getAllEmailAccounts();
		
		emailScreenAccount = (RelativeLayout) view.findViewById(R.id.email_screen_account); 
		emailScreenNone = (RelativeLayout) view.findViewById(R.id.email_screen_noaccounts);
		emailScreenChoose = (RelativeLayout) view.findViewById(R.id.email_screen_choose);

        checkEmailSync=view.findViewById(R.id.syncing);
        checkEmailSync.setVisibility(View.GONE);
        B.addStyleBold((TextView) checkEmailSync.findViewById(R.id.syncing_text));

		list=(ListView)view.findViewById(R.id.mail_list);
		
		list.setOnScrollListener(new EmailScrollListener());
		list.setOnItemClickListener(openListener);
		list.setOnItemLongClickListener(onLongClick);
        //list.addHeaderView(checkEmailSync);
		
		
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setRefreshing(false);
        mSwipeRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
		
			@Override
			public void onRefresh() {

                if(!BriefService.isAccountActiveSyncing(currentAccount.getLong(Account.LONG_ID))) {
                    collectMessages = new CollectLatestMessages();
                    collectMessages.execute(true);
                } else {
                    BLog.e("SYNC","already syncing");
                }
			}
		});
        //mSwipeRefreshLayout.
        mSwipeRefreshLayout.setColorScheme(
                R.color.swipe_color_1, R.color.swipe_color_2,
                R.color.swipe_color_3, R.color.swipe_color_4);


        //TextView noacctxt=(TextView) view.findViewById(R.id.email_noaccounts_text);
        bButton noaccbtn=(bButton) view.findViewById(R.id.email_noaccounts_btn);
        noaccbtn.setOnClickListener(openNewAccountListener);


        setHistoryLoading();
		refresh();
        //showEmailSync();
	}

    @Override
    public void onPause() {
        super.onPause();
        if(list!=null) {
            lastPosition=list.getFirstVisiblePosition();
            State.addToState(State.SECTION_EMAIL,new StateObject(StateObject.INT_VALUE,lastPosition));
        }
        checkEmailHandler.removeCallbacks(isSyncingCheck);
        B.removeGoTopTracker();
    }
	public void refreshData() {
		
	}
	public void refresh() {
		if(activity!=null) {

			mSwipeRefreshLayout.setRefreshing(false);
			//list.onRefreshComplete();
			B.removeGoTopTracker();

			emailScreenAccount.setVisibility(View.GONE);
			emailScreenNone.setVisibility(View.GONE);
			emailScreenChoose.setVisibility(View.GONE);
			if(emailAccounts.isEmpty()) {
                /*
                amb = new ActionModeBack(activity, activity.getString(R.string.label_email)
                        ,R.menu.email_none
                        , new ActionModeCallback() {
                    @Override
                    public void onActionMenuItem(ActionMode mode, MenuItem item) {
                        onOptionsItemSelected(item);
                    }
                });
                */
                ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.label_email),R.menu.email_none,R.color.actionbar_email);


				emailScreenNone.setVisibility(View.VISIBLE);

			} else if(emailAccounts.size()==1) {

                B.addGoTopTracker(activity,list,R.drawable.gt_email);

				currentAccount=emailAccounts.get(0);
				openAccount();
			} else {
				if(currentAccount!=null) {

                    B.addGoTopTracker(activity,list,R.drawable.gt_email);
					openAccount();
				} else {

					chooseAccount();
				}
			}
			if(popupMenu!=null)
				popupMenu.dismiss();
            if(lastPosition!=0)
                list.setSelection(lastPosition);
            if(State.hasStateObject(State.SECTION_EMAIL,StateObject.INT_VALUE)) {
                lastPosition=State.getStateObjectInt(State.SECTION_EMAIL,StateObject.INT_VALUE);
                list.setSelection(lastPosition);

            }
            State.clearStateObjects(State.SECTION_EMAIL);
		}
	}
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");

	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai home");
		switch(item.getItemId()) {
			case R.id.action_email_new:
                if(currentAccount!=null) {
                    State.clearStateObjects(State.SECTION_EMAIL_NEW);
                    State.addToState(State.SECTION_EMAIL_NEW, new StateObject(StateObject.LONG_USE_ACCOUNT_ID, currentAccount.getLong(Account.LONG_ID)));

                    State.addToState(State.SECTION_EMAIL_NEW, new StateObject(StateObject.INT_FORCE_NEW, 1));
                    Bgo.openFragmentBackStack(activity, EmailSendFragment.class);
                }
				break;
			case R.id.action_select_account:
				EmailHomeFragment.restart();
				refresh();
				break;
		}	
		return false;
	}
	
	public OnItemLongClickListener onLongClick = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		popupMenu = new EmailDialog(getActivity(),currentAccount,position,thisFragment);
		popupMenu.show();
			
		return true;
			
		}
	};
	private void chooseAccount() {
        /*
        amb = new ActionModeBack(activity, activity.getString(R.string.label_email)
                ,R.menu.email_none
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.label_email),R.menu.email_none,R.color.actionbar_email);


		emailScreenAccount.setVisibility(View.GONE);
		emailScreenNone.setVisibility(View.GONE);
		emailScreenChoose.setVisibility(View.VISIBLE);
		chooseList=(ListView) activity.findViewById(R.id.mail_choose);
		eAccounts = new EmailAccountsAdapter(activity);
		chooseList.setAdapter(eAccounts);
		chooseList.setClickable(true); 
		chooseList.setOnItemClickListener(openAccountListener);
		
	}
	private void openAccount() {
        //Validator.calldata();
		emailScreenAccount.setVisibility(View.VISIBLE);
		emailScreenNone.setVisibility(View.GONE);
		emailScreenChoose.setVisibility(View.GONE);
		if(currentAccount!=null) {
            String showEmail=currentAccount.getString(Account.STRING_EMAIL_ADDRESS);
            if(showEmail.length()>20)
                showEmail = Sf.restrictLength(showEmail,17)+"...";
            if(AccountsDb.getAllEmailAccounts().size()>1) {
                /*
                amb = new ActionModeBack(activity, showEmail
                        ,R.menu.email
                        , new ActionModeCallback() {
                    @Override
                    public void onActionMenuItem(ActionMode mode, MenuItem item) {
                        onOptionsItemSelected(item);
                    }
                });
                */
                ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),showEmail,R.menu.email,R.color.actionbar_email);

            } else {
                /*
                amb = new ActionModeBack(activity, showEmail
                        ,R.menu.email_single
                        , new ActionModeCallback() {
                    @Override
                    public void onActionMenuItem(ActionMode mode, MenuItem item) {
                        onOptionsItemSelected(item);
                    }
                });
                */
                ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),showEmail,R.menu.email_single,R.color.actionbar_email);

            }
			//BLog.e("ACC", "is open setting listview");
            int setPos=0;
            if(list.getAdapter()!=null) {
                setPos=list.getFirstVisiblePosition();
            }
			//Account account = AccountsDb.getAccount(0);
			emailService = EmailService.getService(activity, currentAccount);
            //BLog.e("EMV","loading account size: "+emailService.getEmails().size());
			adapter=new EmailListAdapter(activity, currentAccount,emailService.getEmails());

			list.setAdapter(adapter);
            list.invalidate();
            if(list.getCount()>setPos)
                list.setSelection(setPos);
			//list.setClickable(true); 

            //emailService.getLoadFolders();


            checkEmailHandler.postDelayed(isSyncingCheck,100);
		} else {
            //BLog.e("EMV","currentAccount is null");
        }
	}

    private LoadFoldersTask loadTask;
    private class LoadFoldersTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            //if(es.getLoadFolders().isEmpty())
            //    es.loadFolders();
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            //mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            //mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            refresh();
        }

    }
	//es.loadFolders(this);
	
	private BRefreshable getThisFragment() {
		return this;
	}
	
	private void refreshMail() {
		if(getActivity()!=null) {
			//BLog.e("CALLED","refresh mail");
			list.invalidate();
			//list=(PullToRefreshListView)getActivity().findViewById(R.id.mail_list);
			//adapter=new EmailListAdapter(getActivity(), EmailDb);
			list.setAdapter(adapter);
			
			list.setClickable(true); 
			list.setOnItemClickListener(openListener);
			view.requestFocus();
		}
	}
	
	

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		refreshMail();

	}

    protected OnClickListener openNewAccountListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Bgo.openFragmentBackStackAnimate(activity, AccountsHomeFragment.class);
            //openAccount();
        }
    };
	protected OnItemClickListener openAccountListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			currentAccount=(Account) eAccounts.getItem(position);
			refresh();
			//openAccount();
		}
	};
	protected OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			///BLog.e("CLICK", "openlistner click received");
            Email email = adapter.getItem(position);

            if(email.getString(Email.STRING_XBRIEFID)=="-1") {
                long bsid=email.getLong(Email.LONG_ID);  // ignore fact is dbid, hacked this in there

                BriefSend bs = BriefSendDb.get(bsid);
                if(bs!=null) {
                    Brief b=new Brief(activity,currentAccount,email,position);
                    dialog = new BriefSendDialog(activity,b,bs,thisFragment);
                    dialog.show();
                }
            } else {

                State.clearStateObjects(State.SECTION_EMAIL_VIEW);
                StateObject sob = new StateObject(StateObject.STRING_USE_DATABASE_ID, email.getLong(Email.LONG_ID)+"");
                StateObject soba = new StateObject(StateObject.LONG_USE_ACCOUNT_ID, currentAccount.getLong(Account.LONG_ID));
                StateObject sobi = new StateObject(StateObject.INT_FORCE_NEW, 1);
                State.addToState(State.SECTION_EMAIL_VIEW, sobi);
                State.addToState(State.SECTION_EMAIL_VIEW, sob);
                State.addToState(State.SECTION_EMAIL_VIEW, soba);
                Bgo.openFragmentBackStackAnimate(activity, EmailViewFragment.class);
            }
		}
	};

	protected class CollectLatestMessages extends AsyncTask<Boolean, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Boolean... params) {
            //BLog.e("emailhomecheck","ndnjndjndjndjnjdnjdnjdnjdsnjndsjnd");
            BriefService.checkEmailsFor(activity,currentAccount,true);
		    return Boolean.TRUE;
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
            mSwipeRefreshLayout.setRefreshing(false);
            //refresh();
			//list.invalidate();
		}
	
	 
	}
	protected OnClickListener deleteListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			emailService.clearAllDbData();
			refreshMail();
		}
	};	
	protected OnClickListener historyListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			emailService.fetchHistoryEmails(getThisFragment());
			//refreshMail();
		}
	};	


    public class EmailScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;
        private boolean isFirstTime=true;

        public EmailScrollListener() {
        }
        public EmailScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        private boolean working=false;
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if(!isFirstTime) {
                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                        currentPage++;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {

                    synchronized (this) {
                        if (!working) {
                            working = true;
                            boolean didload = emailService.loadEmailsHistory();
                            if (!didload) {
                                if (!BriefService.isEmailHistoryTaskRunning()) {
                                    //BLog.e("LOAD","remote");
                                    BriefService.addEmailHistoryCollectFor(activity, currentAccount);
                                    setHistoryLoading();
                                    loading = true;
                                }
                            } else {
                                //BLog.e("LOAD","more loaded ok");
                                lastPosition = firstVisibleItem;
                                //if (lastPosition > 0) {
                                    refresh();
                                //}
                            }

                        }
                        working = false;

                    }

                    //new LoadGigsTask().execute(currentPage + 1);


                }
            }
            isFirstTime=false;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
    }
    private void setHistoryLoading() {
        if(flay==null) {
            AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            flay = new RelativeLayout(activity);
            flay.setGravity(Gravity.CENTER_HORIZONTAL);
            flay.setLayoutParams(lp);
            TextView text = new TextView(activity);
            text.setGravity(Gravity.CENTER_HORIZONTAL);
            text.setLayoutParams(lp);
            text.setText(activity.getString(R.string.label_loading));
            text.setCompoundDrawablesWithIntrinsicBounds(R.drawable.navigation_refresh, 0, 0, 0);
            flay.addView(text);
            list.addFooterView(flay);
        }

        if(!BriefService.isEmailHistoryTaskRunning()) {
            flay.setVisibility(View.VISIBLE);
            //list.foot
        } else {
            flay.setVisibility(View.GONE);
        }
        //refresh();

    }
}
