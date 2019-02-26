package run.brief.settings;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.List;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.SyncData;
import run.brief.email.DefaultProperties;
import run.brief.service.BriefService;
import run.brief.service.SyncDataDb;
import run.brief.settings.OAuth.OAuthHelper;
import run.brief.util.Sf;
import run.brief.util.log.BLog;


public class AccountsHomeFragment extends BFragment implements BRefreshable {
	private View view;
	private ViewGroup container;
	private LayoutInflater inflater;
	private Activity activity=null;
	private ListView list;
    private View googleoverlay;

	//private Handler accountsHandler = new Handler();
	
//String callbackURL="http://www.runplay.com";
	private Handler synchandler=new Handler();
    private Runnable syncCheck=new Runnable() {
        @Override
        public void run() {
            List<Long> syning = new ArrayList<Long>();
            for(Account account: AccountsDb.getAllAccounts()) {
                long aid=account.getLong(Account.LONG_ID);
                if(BriefService.isAccountActiveSyncing(aid)) {
                    syning.add(aid);
                }
                if(list!=null) {
                    for(int i=0; i<list.getCount(); i++) {
                        View v = list.getChildAt(i);
                        TextView type = (TextView)v.findViewById(R.id.account_type);
                        ProgressBar progress = (ProgressBar) v.findViewById(R.id.account_is_sync);


                        ImageView accountSync = (ImageView) v.findViewById(R.id.account_sync_switch);
                        if(syning.contains(Sf.toLong((String)v.getTag()))) {
                            if(progress.getVisibility()==View.GONE) {
                                type.setText(activity.getString(R.string.label_syncing));
                                progress.setVisibility(View.VISIBLE);
                                accountSync.setVisibility(View.GONE);
                            }
                        } else {
                            if(progress.getVisibility()==View.VISIBLE) {
                                type.setText(activity.getString(R.string.label_email));
                                progress.setVisibility(View.GONE);
                                accountSync.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            }
            synchandler.postDelayed(syncCheck,1000);
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        synchandler.removeCallbacks(syncCheck);
    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
			
		this.container=container;
		this.inflater=inflater;
		activity=getActivity();
		
		// ensure Twitter accounts Db is initialised
		AccountsDb.init();
		//AccountsDb.deleteAllAccounts();

		view=inflater.inflate(R.layout.accounts,container, false);
		
		

		return view;

	}
	public void refreshData() {
		
	}

	public void refresh() {
        /*
        amb = new ActionModeBack(activity, activity.getString(R.string.action_accounts)
                ,R.menu.accounts
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.action_accounts),R.menu.accounts,R.color.brand);
		BriefManager.clearController(activity);
		AccountsAdapter adapter=new AccountsAdapter(getActivity());
		adapter.notifyDataSetChanged();
		list.refreshDrawableState();
        list.setAdapter(adapter);

        if(AccountsDb.getAllAccounts().size()>2) {
            view.findViewById(R.id.accounts_add_pod).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.accounts_add_pod).setVisibility(View.VISIBLE);
        }
        
	}
	
	@Override
	public void onResume() {
		super.onResume();

        if(State.hasStateObject(State.SECTION_ACCOUNTS,StateObject.STRING_FROM_EMAIL)) {
            String inEmail=State.getStateObjectString(State.SECTION_ACCOUNTS, StateObject.STRING_FROM_EMAIL);
            Account already=AccountsDb.getEmailAccount(inEmail);
            if(already==null) {
                makeGoogleAccount(inEmail);
            }
            State.clearStateObjects(State.SECTION_ACCOUNTS);
            //BLog.e("ACTRES", "onresume() make account here");
        }

		State.setCurrentSection(State.SECTION_ACCOUNTS);
		ActionBarManager.setActionBarBackOnly(getActivity(),getActivity().getResources().getString(R.string.action_accounts), R.menu.accounts);
		//BLog.e("refresh", "accounts home.....");
		googleoverlay = view.findViewById(R.id.settings_google_accounts_overlay);
        googleoverlay.setVisibility(View.GONE);


		list=(ListView) view.findViewById(R.id.accounts_list);
        list.setOnItemClickListener(openListener);
			
		list.refreshDrawableState();
		
		view.requestFocus();
		
		TextView email=(TextView) view.findViewById(R.id.accounts_add_email);
		email.setClickable(true);
		email.setOnClickListener(newAccountListener);

        TextView googlemail=(TextView) view.findViewById(R.id.accounts_add_google);
        if( GooglePlayServicesUtil.isGooglePlayServicesAvailable(activity)== ConnectionResult.SUCCESS) {
            googlemail.setClickable(true);
            googlemail.setOnClickListener(newGoogleListener);
        } else {
            googlemail.setVisibility(View.GONE);
        }


        TextView text = (TextView) view.findViewById(R.id.accounts_add_account_text);
        TextView gtext =(TextView) view.findViewById(R.id.settings_google_accounts_overlay_text);

        B.addStyle(new TextView[]{email,googlemail,gtext});
        B.addStyleBold(text);


		refresh();

        synchandler.postDelayed(syncCheck,100);
	}

    public OnClickListener newGoogleListener = new OnClickListener() {
        @Override
        public void onClick(View view) {

            Bgo.openFragmentAnimate(activity,GmailAddFragment.class);
            //pickUserAccount();


        }
    };
    public OnClickListener newAccountListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			switch(view.getId()) {
				case R.id.accounts_add_email:
					Bgo.openFragmentBackStack(activity, EmailAddFragment.class);
					break;
				default: break;
			}
			
		}
	};	
	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


			Account acc=AccountsDb.getAccount(position);
			if(acc!=null) {
				switch(acc.getInt(Account.INT_TYPE_)) {
					case Account.TYPE_EMAIL:
						State.clearStateObjects(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL);
						StateObject sob=new StateObject(StateObject.INT_USE_SELECTED_INDEX,position);
						State.addToState(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL,sob);
						
						
						if(acc!=null) {
							State.addToState(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL,new StateObject(StateObject.LONG_USE_ACCOUNT_ID,acc.getLong(Account.LONG_ID)));
						}
						Bgo.openFragmentBackStack(activity,EmailEditFragment.class);
						break;
					case Account.TYPE_TWITTER:
						//Bgo.openFragment(activity, new TwitterAddFragment());
						break;
				}
			}
			//BLog.e("Accounts", "called open: "+view.getId());
		}
	};



    // ADD GOOGLE ACCOUNT STUFF BELOW

    private String callbackURL;

    private AuthTokenTask authTask;

    public static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private void pickUserAccount() {
        googleoverlay.setVisibility(View.VISIBLE);
        String[] accountTypes = new String[]{"com.google"};
        Intent intent = AccountPicker.newChooseAccountIntent(null, null,
                accountTypes, false, null, null, null, null);
        startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //BLog.e("ACTRESULT", "vals: " + requestCode + ", " + resultCode + ", ");
        if(data!=null) {
            BLog.e("ACTRESULT", "with data: " + data.toString()+" - ");
        }
       if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
            // Receiving a result from the AccountPicker
            if (resultCode == Activity.RESULT_OK) {
                googleoverlay.setVisibility(View.VISIBLE);
                authTask=new AuthTokenTask(data);
                authTask.execute(true);
                // With the account name acquired, go get the auth token
                //getUsername();
            } else if (resultCode == Activity.RESULT_CANCELED) {

            }
        } else if(requestCode==OAuthHelper.MY_ACTIVITYS_AUTH_REQUEST_CODE) {
           BLog.e("ACTRES","make account here");
           //makeAccount(mEmail);
           //if (resultCode == Activity.RESULT_OK) {
               //googleoverlay.setVisibility(View.VISIBLE);
               //authTask=new AuthTokenTask(data);
               //authTask.execute(true);
               // With the account name acquired, go get the auth token
               //getUsername();
           //} else if (resultCode == Activity.RESULT_CANCELED) {

           //}
       }
        // Later, more code will go here to handle the result from some exceptions...
    }

    private String mEmail;

    private class AuthTokenTask extends AsyncTask<Boolean, Void, Boolean> {
        private Intent data;
        public AuthTokenTask(Intent data) {
            this.data=data;
        }
        @Override
        protected Boolean doInBackground(Boolean... params) {
            mEmail = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);

            if(Device.getCONNECTION_TYPE()!=Device.CONNECTION_TYPE_WIFI) {
                BLog.e("CONNECT","3g force try");
                B.forceTryConnection(activity);
                //tryConnect=true;
            }
            //B.forceTryConnection();
            String token = OAuthHelper.getAndUseAuthTokenBlocking(activity, mEmail);

            if(token!=null) {
                if(token.equals("extra")) {
                    BLog.e("TOKEN", "Set State to wait for auth");
                    State.addToState(State.SECTION_ACCOUNTS,new StateObject(StateObject.STRING_FROM_EMAIL,mEmail));
                } else {
                    BLog.e("TOKEN", "not null creating account");
                    makeGoogleAccount(mEmail);
                    return Boolean.TRUE;
                }

            }
            return Boolean.FALSE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(result) {
                Bgo.refreshCurrentFragment(activity);
            } else {

                BLog.e("TOKEN","IS NULL more needed, await googleplay auth");
                //Toast.makeText(activity, activity.getString(R.string.email_add_google_bad_internet), Toast.LENGTH_SHORT);
            }
        }

    }

    private void makeGoogleAccount(String mEmail) {

        Account pickaccount = DefaultProperties.makeGmailOAuth();

        pickaccount.setInt(Account.INT_TYPE_, Account.TYPE_EMAIL);
        pickaccount.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_GOOGLEMAIL);
        pickaccount.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_GOOGLEMAIL);
        pickaccount.setString(Account.STRING_EMAIL_ADDRESS, mEmail);
        pickaccount.setString(Account.STRING_LOGIN_NAME, mEmail);
        //pickaccount.setString(Account.STRING_ACCESS_TOKEN,token);
        pickaccount.setInt(Account.INT_OAUTH_TOKEN_FAILS, 0);

        //pickaccount.setString(Account.STRING_ACCESS_TOKEN, token);

        List<String> addFolders = new ArrayList<String>();
        addFolders.add("[Gmail]/Sent Mail");
        addFolders.add("INBOX");
        pickaccount.setEmailFolders(addFolders);
        Account already=AccountsDb.getEmailAccount(mEmail);
        if(already!=null) {

            //OAuthHelper.removeAuthToken(activity,already);
            //AccountsDb.deleteAccount(activity,already);
            pickaccount.setLong(Account.LONG_ID,already.getLong(Account.LONG_ID));
            AccountsDb.updateAccount(pickaccount);
        } else {

            AccountsDb.addAccount(pickaccount);
            Account newacc = AccountsDb.getEmailAccount(mEmail);
            if(newacc!=null) {
                SyncData data = SyncDataDb.getByAccountId(newacc.getLong(Account.LONG_ID));
                data.setActive(true);
                SyncDataDb.update(data);
                BriefService.checkEmailsFor(activity, newacc, true);

            }
        }


    }



}
