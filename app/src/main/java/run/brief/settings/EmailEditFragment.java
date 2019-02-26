package run.brief.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.BriefSendDb;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.SyncData;
import run.brief.email.EmailService;
import run.brief.email.EmailServiceInstance;
import run.brief.service.BriefService;
import run.brief.service.SyncDataDb;
import run.brief.util.Functions;
import run.brief.util.Sf;
public class EmailEditFragment extends BFragment implements BRefreshable {
	private Activity activity;
	private View view;
	private TextView btnserver;
	private TextView accountname;
	//private TextView lastcollect;
	private TextView emailMsgIn;
    private TextView emailMsgInTitle;
    private TextView emailMsgOut;
    private TextView emailMsgOutTitle;
	private Spinner emailrefresh;

    private TextView inDate;
    private TextView outDate;

    private TextView btnDelete;
    private ArrayAdapter<String> dataAdapter;
	
	private Account useaccount;

    private LinearLayout folders;

    private TextView btnSignatures;

    private TextView syncStatus;
    private TextView syncStatusDesc;
    private ImageView syncStatusImage;

    //private boolean foldersLoading=false;

    private TextView refresh;
	Handler ensureSafeDelete = new Handler();
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.activity=getActivity();

		view=inflater.inflate(R.layout.accounts_edit_email,container, false);

        accountname = (TextView) view.findViewById(R.id.accounts_email_show);
        //lastcollect = (TextView) view.findViewById(R.id.accounts_email_last_collect_date);
        emailMsgInTitle = (TextView) view.findViewById(R.id.accounts_email_sync_in_title);
        emailMsgIn = (TextView) view.findViewById(R.id.accounts_email_sync_in);
        emailMsgOutTitle = (TextView) view.findViewById(R.id.accounts_email_sync_out_title);
        emailMsgOut = (TextView) view.findViewById(R.id.accounts_email_sync_out);

        btnserver = (TextView) view.findViewById(R.id.accounts_email_btn_servers);
        emailrefresh = (Spinner) view.findViewById(R.id.accounts_email_choose_refresh);

        inDate=(TextView) view.findViewById(R.id.accounts_email_sync_in_date);
        outDate=(TextView) view.findViewById(R.id.accounts_email_sync_out_date);

        btnDelete = (TextView) view.findViewById(R.id.accounts_remove_now);
        btnDelete.setOnClickListener(deleteListner);

        folders=(LinearLayout) view.findViewById(R.id.accounts_email_folders);

        syncStatus=(TextView) view.findViewById(R.id.account_email_sync_header);
        syncStatusDesc=(TextView) view.findViewById(R.id.account_email_sync_desc);
        syncStatusImage=(ImageView) view.findViewById(R.id.account_email_sync_switch);

        btnSignatures=(TextView) view.findViewById(R.id.accounts_email_btn_signatures);
        btnSignatures.setOnClickListener(signaturesListner);

        B.addStyleBold(accountname,1.2D);
        B.addStyle(new TextView[]{syncStatusDesc,emailMsgIn,btnserver,emailMsgOut,inDate,outDate,btnDelete,btnSignatures});
        B.addStyleBold(new TextView[]{syncStatus,emailMsgInTitle,emailMsgOutTitle});

		return view;

	}
    @Override
    public void onPause() {
        super.onPause();
        if(useaccount!=null) {
            State.addToState(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL, new StateObject(StateObject.LONG_USE_ACCOUNT_ID, useaccount.getLong(Account.LONG_ID)));
        }
        ensureSafeDelete.removeCallbacks(checkSafeDelete);
    }
    private Runnable checkSafeDelete = new Runnable() {
        @Override
        public void run() {

            //try {
                SyncData data = SyncDataDb.getByAccountId(useaccount.getLong(Account.LONG_ID));
                if (BriefService.isAccountActiveSyncing(useaccount.getLong(Account.LONG_ID))) {
                    btnDelete.setAlpha(0.5F);
                    btnDelete.setOnClickListener(null);

                    refresh.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.navigation_refresh, 0);
                    refresh.setText(activity.getString(R.string.label_loading));
                    refresh.setAlpha(0.5f);
                    refresh.setOnClickListener(null);
                } else {
                    btnDelete.setAlpha(1F);
                    btnDelete.setOnClickListener(deleteListner);
                    refresh.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.navigation_refresh, 0);
                    refresh.setText(activity.getString(R.string.accounts_email_sync_reload));
                    refresh.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            goLoadFolder();
                        }
                    });

                }
                showSyncMessages(data);
            //} catch(Exception e) {}
            ensureSafeDelete.postDelayed(checkSafeDelete,200);

        }
    };
	@Override
	public void onResume() {
		super.onResume();
        ensureSafeDelete.postDelayed(checkSafeDelete,100);
		State.setCurrentSection(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL);


		

		List<String> list = new ArrayList<String>();


		list.add(activity.getString(R.string.accounts_sync_very_slow));
        list.add(activity.getString(R.string.accounts_sync_slow));
        list.add(activity.getString(R.string.accounts_sync_fast));
		
		
		
		dataAdapter = new ArrayAdapter<String>(activity,
				android.R.layout.simple_spinner_item, list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		emailrefresh.setAdapter(dataAdapter);
		
		
		if(State.hasStateObject(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL,StateObject.LONG_USE_ACCOUNT_ID)) {
			long uaccount = State.getStateObjectLong(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL,StateObject.LONG_USE_ACCOUNT_ID);
			
			useaccount = AccountsDb.getAccountById(uaccount);
			
	
		}
        State.clearStateObjects(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL);
		if(useaccount!=null) {
			accountname.setText(useaccount.getString(Account.STRING_EMAIL_ADDRESS));

            if(useaccount.getInt(Account.INT_EMAIL_INCOMING_SUBTYPE_)==Account.SUBTYPE_GOOGLEMAIL) {
                btnserver.setVisibility(View.GONE);
            } else {
                btnserver.setVisibility(View.VISIBLE);
                btnserver.setOnClickListener(serversListner);
            }


            syncStatusImage.setTag(useaccount.getLong(Account.LONG_ID));
            syncStatusImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Account account = AccountsDb.getAccountById(Sf.toLong(view.getTag().toString()));
                    if(account!=null) {
                        SyncData data = SyncDataDb.getByAccountId(account.getLong(Account.LONG_ID));
                        //BLog.e("CL", data.isActive() + " - " + view.getTag().toString());

                        if(data.isActive()) {
                            data.setActive(false);
                            ((ImageView) view).setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_inactive));
                        } else {
                            data.setActive(true);
                            ((ImageView) view).setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_active));
                        }
                        SyncDataDb.update(data);
                        refreshData();
                    }
                }
            });


            emailrefresh.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    SyncData sd = SyncDataDb.getByAccountId(useaccount.getLong(Account.LONG_ID));
                    //BLog.e("SSS", "UPD: " + sd.getInt(SyncData.INT_TYPE_SYNC));
                    if (sd.getInt(SyncData.INT_TYPE_SYNC) != position) {
                        switch (position) {
                            case 2:
                                sd.setInt(SyncData.INT_TYPE_SYNC, SyncData.TYPE_FAST);
                                break;
                            case 1:
                                sd.setInt(SyncData.INT_TYPE_SYNC, SyncData.TYPE_MEDIUM);
                                break;
                            default:
                                sd.setInt(SyncData.INT_TYPE_SYNC, SyncData.TYPE_SLOW);
                                break;
                        }
                        //BLog.e("SSS", "UPD: " + sd.getInt(SyncData.INT_TYPE_SYNC));
                        SyncDataDb.update(sd);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    // your code here
                }

            });
            //emailrefresh.setOnItemClickListener(accountChangeListner);
            State.clearStateObjects(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL);

            SyncData data = SyncDataDb.getByAccountId(useaccount.getLong(Account.LONG_ID));
            if(data!=null) {



                emailMsgIn.setText(data.getString(SyncData.STRING_MESSAGE_IN));
                emailMsgIn.setOnClickListener(copyListner);
                //emailMsgIn.setText(useaccount.getInt(Account.INT_SIGNATURE_REKEY)+" = rekey");
                emailMsgIn.setVisibility(View.VISIBLE);

                showSyncMessages(data);
                emailMsgOut.setText(data.getString(SyncData.STRING_MESSAGE_OUT));
                emailMsgOut.setVisibility(View.VISIBLE);
                emailMsgOut.setOnClickListener(copyListner);
                inDate.setText(data.getLongCal(SyncData.LONG_IN_DATE).getDatabaseDate());
                outDate.setText(data.getLongCal(SyncData.LONG_OUT_DATE).getDatabaseDate());

            }


            refresh();
		}
	}
    private void showSyncMessages(SyncData data) {
        if(SyncData.SYNC_LAST_RESULT_FAIL==data.getInt(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE)) {
            emailMsgOut.setCompoundDrawablesWithIntrinsicBounds(R.drawable.i_cross, 0, 0, 0);

        } else if(SyncData.SYNC_LAST_RESULT_NONE==data.getInt(SyncData.INT_SYNC_OUT_LAST_RESULT_CODE)) {
            emailMsgOut.setCompoundDrawablesWithIntrinsicBounds(R.drawable.i_wait, 0, 0, 0);

        }  else {
            emailMsgOut.setCompoundDrawablesWithIntrinsicBounds(R.drawable.i_tick, 0, 0, 0);
        }

        if(SyncData.SYNC_LAST_RESULT_FAIL==data.getInt(SyncData.INT_SYNC_IN_LAST_RESULT_CODE)) {
            emailMsgIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.i_cross, 0, 0, 0);

        } else if(SyncData.SYNC_LAST_RESULT_NONE==data.getInt(SyncData.INT_SYNC_IN_LAST_RESULT_CODE)) {
            emailMsgIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.i_wait, 0, 0, 0);

        } else {
            emailMsgIn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.i_tick, 0, 0, 0);
        }
    }
    protected OnClickListener copyListner = new OnClickListener() {
        @Override
        public void onClick(View view) {
            SyncData sd = SyncDataDb.getByAccountId(useaccount.getLong(Account.LONG_ID));

            StringBuilder sb = new StringBuilder("*******************\n"+activity.getString(R.string.email_in_status_title));
            sb.append(": "+sd.getString(SyncData.INT_SYNC_IN_LAST_RESULT_CODE)+":");
            sb.append(sd.getString(SyncData.STRING_MESSAGE_IN));
            sb.append("\n\n"+activity.getString(R.string.email_out_status_title));
            sb.append(": "+sd.getString(SyncData.INT_SYNC_IN_LAST_RESULT_CODE)+":");
            sb.append(sd.getString(SyncData.STRING_MESSAGE_IN));
            sb.append("\n\n");
            Functions.copyToClipFlashView(activity, emailMsgIn);
            Functions.copyToClipFlashView(activity, emailMsgOut);
            //Functions.copyToClipFlashView(activity, url);

            Device.copyToClipboard(activity, sb.toString());
            Toast.makeText(activity, R.string.copied_to_clip, Toast.LENGTH_SHORT).show();
        }
    };
    public void refreshData() {

        SyncData accsync = SyncDataDb.getByAccountId(useaccount.getLong(Account.LONG_ID));
        emailrefresh.setSelection(accsync.getInt(SyncData.INT_TYPE_SYNC));
        if(accsync.isActive()) {
            syncStatusImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_active));
            syncStatus.setText(activity.getString(R.string.accounts_email_sync_active));
            syncStatusDesc.setText(activity.getString(R.string.accounts_email_sync_active_desc));
            syncStatus.setTextColor(activity.getResources().getColor(R.color.green));
            emailrefresh.setEnabled(true);
            inDate.setAlpha(1f);
            outDate.setAlpha(1f);

            //BLog.e("SSS", "REFRESH UPD: " + syncdata.getInt(SyncData.INT_TYPE_SYNC));

        } else {
            syncStatusImage.setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_inactive));
            syncStatus.setText(activity.getString(R.string.accounts_email_sync_inactive));
            syncStatusDesc.setText(activity.getString(R.string.accounts_email_sync_inactive_desc));
            syncStatus.setTextColor(activity.getResources().getColor(R.color.red));
            emailrefresh.setEnabled(false);
            inDate.setAlpha(0.6f);
            outDate.setAlpha(0.6f);
        }

		folders.removeAllViews();

        //if(accsync.isActive()) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        wlp.gravity=Gravity.CENTER_VERTICAL;
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        slp.gravity=Gravity.CENTER_VERTICAL;
        slp.weight = 1;


        LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rlp.gravity=Gravity.RIGHT|Gravity.CENTER_VERTICAL;
        refresh = new TextView(activity);
        refresh.setLayoutParams(rlp);
        refresh.setText(activity.getString(R.string.accounts_email_sync_folders));
        refresh.setPadding(0, 5, 0, 5);
        refresh.setGravity(Gravity.CENTER_VERTICAL);

        folders.addView(refresh);
        B.addStyle(refresh);

        TextView syncHead = new TextView(activity);
        syncHead.setLayoutParams(lp);
        syncHead.setText(activity.getString(R.string.accounts_email_sync_folders));
        syncHead.setPadding(0, 20, 0, 5);
        folders.addView(syncHead);
        B.addStyleBold(syncHead);


        if (!useaccount.getEmailFolders().isEmpty()) {
            for (String folder : useaccount.getEmailFolders()) {
                LinearLayout frow = new LinearLayout(activity);
                frow.setLayoutParams(lp);
                frow.setOrientation(LinearLayout.HORIZONTAL);
                TextView fntext = new TextView(activity);
                fntext.setLayoutParams(slp);
                fntext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.collections_collection, 0, 0, 0);
                fntext.setText(folder);
                fntext.setPadding(0, 5, 0, 5);
                fntext.setGravity(Gravity.CENTER_VERTICAL);

                B.addStyle(fntext);
                ImageView image = new ImageView(activity);
                image.setTag(folder);
                image.setLayoutParams(wlp);
                if(accsync.isActive()) {
                    image.setImageDrawable(B.getDrawable(activity,R.drawable.sync_active));
                    image.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ImageView iv = (ImageView) view;
                            String tag = (String) view.getTag();
                            List<String> syn = useaccount.getEmailFolders();
                            syn.remove(tag);
                            List<String> oth = useaccount.getEmailFoldersOther();
                            oth.add(tag);
                            useaccount.setEmailFolders(syn);
                            useaccount.setEmailFoldersOther(oth);
                            iv.setImageDrawable(B.getDrawable(activity,R.drawable.sync_inactive));
                            AccountsDb.updateAccount(useaccount);
                            refreshData();
                        }
                    });
                } else {
                    image.setImageDrawable(B.getDrawable(activity,R.drawable.sync_inactive));
                    image.setImageAlpha(50);
                }
                frow.addView(fntext);
                frow.addView(image);
                folders.addView(frow);
            }
        } else {
            TextView warn = new TextView(activity);
            warn.setLayoutParams(lp);
            warn.setText(activity.getString(R.string.accounts_email_sync_no_folders));
            warn.setPadding(0, 20, 0, 5);
            warn.setTextColor(activity.getResources().getColor(R.color.red));
            B.addStyle(warn);
            folders.addView(warn);

        }
        TextView othHead = new TextView(activity);
        othHead.setLayoutParams(lp);
        othHead.setText(activity.getString(R.string.accounts_email_other_folders));
        othHead.setPadding(0, 20, 0, 5);
        folders.addView(othHead);
        B.addStyleBold(othHead);

        if (!useaccount.getEmailFoldersOther().isEmpty()) {
            for (String folder : useaccount.getEmailFoldersOther()) {
                if (!folder.equals("[Gmail]")) {
                    LinearLayout frow = new LinearLayout(activity);
                    frow.setLayoutParams(lp);
                    frow.setOrientation(LinearLayout.HORIZONTAL);
                    TextView fntext = new TextView(activity);
                    fntext.setLayoutParams(slp);
                    fntext.setCompoundDrawablesWithIntrinsicBounds(R.drawable.collections_collection, 0, 0, 0);
                    fntext.setText(folder);
                    fntext.setPadding(0, 5, 0, 5);
                    fntext.setGravity(Gravity.CENTER_VERTICAL);

                    B.addStyle(fntext);
                    ImageView image = new ImageView(activity);
                    image.setTag(folder);
                    image.setLayoutParams(wlp);
                    if(accsync.isActive()) {
                        image.setImageDrawable(B.getDrawable(activity,R.drawable.sync_inactive));
                        image.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {

                                ImageView iv = (ImageView) view;
                                String tag = (String) view.getTag();
                                List<String> syn = useaccount.getEmailFolders();
                                syn.add(tag);
                                List<String> oth = useaccount.getEmailFoldersOther();
                                oth.remove(tag);
                                useaccount.setEmailFolders(syn);
                                useaccount.setEmailFoldersOther(oth);
                                AccountsDb.updateAccount(useaccount);
                                iv.setImageDrawable(B.getDrawable(activity,R.drawable.sync_active));
                                refreshData();

                            }
                        });
                    } else {
                        image.setImageDrawable(B.getDrawable(activity,R.drawable.sync_inactive));
                        image.setImageAlpha(50);
                    }
                    frow.addView(fntext);
                    frow.addView(image);
                    folders.addView(frow);
                }
            }
        } else {
            TextView warn = new TextView(activity);
            warn.setLayoutParams(lp);
            warn.setText(activity.getString(R.string.accounts_email_sync_no_folders_other));
            warn.setPadding(0, 20, 0, 5);
            warn.setTextColor(activity.getResources().getColor(R.color.red));
            B.addStyle(warn);
            folders.addView(warn);
        }

        //}
	}
    private void goLoadFolder() {
        if(loadFolders==null || loadFolders.getStatus()==LoadFoldersTask.Status.FINISHED) {
            //foldersLoading=true;

            showTaskLoading();
            loadFolders = new LoadFoldersTask();
            loadFolders.execute(true);
        }
    }
    private void showTaskLoading() {
        if(refresh!=null) {
            refresh.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.navigation_refresh, 0);
            refresh.setText(activity.getString(R.string.label_loading));
            refresh.setAlpha(0.5f);
        }
    }
    LoadFoldersTask loadFolders;

    private class LoadFoldersTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {

            BriefService.loadFoldersNoAsync(activity, useaccount);
            //emsi.loadFoldersNoAsync();

            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            //foldersLoading=false;
            refreshData();
        }

    }

	public void refresh() {
		ActionBarManager.setActionBarBackOnly(activity,activity.getString(R.string.accounts_edit_email), R.menu.accounts,R.color.actionbar_email);
        refreshData();
	}
    protected OnClickListener signaturesListner = new OnClickListener() {
        @Override
        public void onClick(View view) {
            State.clearStateObjects(State.SECTION_EMAIL_SIGNATURES);
            State.addToState(State.SECTION_EMAIL_SIGNATURES,new StateObject(StateObject.LONG_USE_ACCOUNT_ID,useaccount.getLong(Account.LONG_ID)));

            Bgo.openFragment(activity, EmailEditSignaturesFragment.class);
        }
    };
	protected OnClickListener serversListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			State.clearStateObjects(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS);
			State.addToState(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS,new StateObject(StateObject.LONG_USE_ACCOUNT_ID,useaccount.getLong(Account.LONG_ID)));
			
			Bgo.openFragment(activity, EmailEditServerFragment.class);
		}
	};
    protected OnClickListener deleteListner = new OnClickListener() {
        @Override
        public void onClick(View view) {


            new AlertDialog.Builder(activity)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.account_email_delete)
                    .setMessage(R.string.account_email_delete_confirm)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            EmailServiceInstance ems = EmailService.getService(activity, useaccount);
                            ems.disConnect();
                            ems.clearAllDbData();
                            SyncDataDb.delete(SyncDataDb.getByAccountId(useaccount.getLong(Account.LONG_ID)));
                            BriefSendDb.deleteFromAccount(useaccount);
                            AccountsDb.deleteAccount(activity,useaccount);

                            //ActionBarManager.refresh(activity);

                            Bgo.goPreviousFragment(getActivity());

                        }

                    })
                    .setNegativeButton(R.string.label_cancel, null)
                    .show();




        }
    };
}
