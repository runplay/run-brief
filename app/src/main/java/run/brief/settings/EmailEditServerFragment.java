package run.brief.settings;

import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;

import run.brief.b.ActionBarManager;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.email.EmailService;
import run.brief.email.EmailServiceInstance;
import run.brief.util.log.BLog;
import run.brief.service.BriefService;

public class EmailEditServerFragment extends EmailAddFragment {
	Account account;
	@Override
	public void onResume() {

        super.onResume();
        State.replaceCurrentSection(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS);



        long editIndex= State.getStateObjectLong(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS, StateObject.LONG_USE_ACCOUNT_ID);
        //BLog.e("acc", "sel index "+editIndex);
        account = AccountsDb.getAccountById(editIndex);
        if(account!=null) {
            populateFieldsWithAccount(account);
        }

        State.clearStateObjects(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS);

		ActionBarManager.setActionBarBackOnly(activity,activity.getString(R.string.accounts_edit_email), R.menu.accounts,R.color.actionbar_email);
		
		//btnDelete.setVisibility(View.VISIBLE);
		btnSave.setVisibility(View.VISIBLE);
		btnSave.setOnClickListener(saveListner);
		//btnDelete.setOnClickListener(deleteListner);
		btnNext.setVisibility(View.GONE);


		//layConfirm.setVisibility(View.VISIBLE);
		layServers.setVisibility(View.VISIBLE);
		layLogin.setVisibility(View.VISIBLE);
		 isEditMode=true;
	}
    @Override
    public void onPause() {
        super.onPause();
        if(account!=null) {
            State.addToState(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL_SERVERS, new StateObject(StateObject.LONG_USE_ACCOUNT_ID, account.getLong(Account.LONG_ID)));
        }
    }
	protected OnClickListener saveListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			layConfirm.setVisibility(View.VISIBLE);
			btnSave.setVisibility(View.GONE);
			//btnDelete.setVisibility(View.GONE);
			layConfirmSuccess.setVisibility(View.GONE);
			layConfirmTest.setVisibility(View.VISIBLE);
			layConfirmFailed.setVisibility(View.GONE);
			layChoose.setVisibility(View.GONE);
			layServers.setVisibility(View.GONE);
			layLogin.setVisibility(View.GONE);
			btnFinish.setOnClickListener(finishListner);
			new testConnectEditEmail().execute(Boolean.TRUE);
		
		}
	};	

	protected class testConnectEditEmail extends AsyncTask<Boolean, Void, Boolean> {
		EmailServiceInstance emailService;
		Account uaccount;
		@Override
		protected Boolean doInBackground(Boolean... params) {
			uaccount = createAccount();
            uaccount.setLong(Account.LONG_ID, account.getLong(Account.LONG_ID));
			emailService = EmailService.getService(activity, uaccount);
            if(emailService.isConnected())
                emailService.disConnect();
            emailService = new EmailServiceInstance(activity,uaccount);
            //emailService.connect();
		    return emailService.isConnected();
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
			BLog.e("REP","bool: "+result);
			if(emailService.isConnected()) {
				
				AccountsDb.updateAccount(uaccount);
				//ActionBarManager.refresh(activity);
				
				layConfirmSuccess.setVisibility(View.VISIBLE);
				layConfirmTest.setVisibility(View.GONE);
				layConfirmFailed.setVisibility(View.GONE);

                BriefService.runEmailFirstTimeTask(activity, account);
				
			} else {
				layConfirmFailed.setVisibility(View.VISIBLE);
				layConfirmTest.setVisibility(View.GONE);
                StringBuilder showtext = new StringBuilder(activity.getResources().getString(R.string.error_instantiate_email_server));
                if(emailService.getConnectError()!=null) {
                    showtext.append("\n");
                    showtext.append(emailService.getConnectError());
                }
				errorConfirmFailed.setText(showtext.toString());
                //emailService.get
				btnNext.setVisibility(View.GONE);
			}
		}
	
	 
	}
}
