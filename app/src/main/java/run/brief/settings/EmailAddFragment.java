package run.brief.settings;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.bButton;
import run.brief.beans.Account;
import run.brief.email.DefaultProperties;
import run.brief.email.EmailServiceInstance;
import run.brief.service.BriefService;
import run.brief.util.Sf;

public class EmailAddFragment extends BFragment {
	
	protected ViewGroup container;
	protected LayoutInflater inflater;
	protected Activity activity;
	
	protected static final int PORT_DEFAULT_IMAP=143;
	protected static final int PORT_DEFAULT_SMTP=25;
	protected static final int PORT_DEFAULT_POP=110;
	
	//protected static final String regExpEmailValidate = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)+";
	protected static final String regExpServerValidate = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)+";
	
	protected int STAGE;
	//protected int TYPE_ACCOUNT;
	protected View view;
	protected boolean isEditMode;
	
	protected LinearLayout layChoose;
	protected LinearLayout layServers;
	//LinearLayout layImap;
	protected LinearLayout layLogin;
	protected LinearLayout layConfirm;
	
	protected LinearLayout layConfirmTest;
	protected LinearLayout layConfirmSuccess;
	protected LinearLayout layConfirmFailed;
	
	protected bButton btnNext;
	protected bButton btnBack;
	//protected bButton btnDelete;
	protected bButton btnSave;
	
	protected bButton btnFinish;
	
	protected EditText editUsername;
	protected EditText editPassword;
	protected EditText editEmail;
	protected EditText editServerOutgoing;
	protected EditText editServerOutgoingPort;
	protected EditText editServerIncoming;
	protected EditText editServerIncomingPort;
	
	protected TextView errorConfirmFailed;
	protected TextView errorIncomingServer;
	protected TextView errorIncomingPort;
	protected TextView errorOutgoingServer;
	protected TextView errorOutgoingPort;
	
	protected RadioButton radioOutgoingSsl;
	protected RadioButton radioOutgoingTls;
	protected RadioButton radioOutgoingNone;
	protected RadioButton radioIncomingNone;
	protected RadioButton radioIncomingSsl;
	protected RadioButton radioIncomingTls;
	
	protected RadioButton radioIncomingUsePop;
	protected RadioButton radioIncomingUseImap;
	
	protected CheckBox useEmailCheck;
	protected TextView alertMessage;

    private Handler removeAlertMessage=new Handler();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.container=container;
		this.inflater=inflater;
		this.activity=getActivity();

		view=inflater.inflate(R.layout.accounts_add_email,container, false);
	
		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_ACCOUNTS_ADD_EDIT_EMAIL);
		ActionBarManager.setActionBarBackOnly(activity,activity.getString(R.string.accounts_add_email), R.menu.accounts,R.color.actionbar_email);
		
		
		btnNext = (bButton) view.findViewById(R.id.account_email_btn_next);
		btnBack = (bButton) view.findViewById(R.id.account_email_btn_back);
		btnBack.setVisibility(View.GONE);
		btnNext.setVisibility(View.GONE);
		
		//btnDelete = (bButton) view.findViewById(R.id.account_email_btn_delete);
		btnSave = (bButton) view.findViewById(R.id.account_email_btn_save);
		//btnDelete.setVisibility(View.GONE);
		btnSave.setVisibility(View.GONE);
		
		btnFinish = (bButton) view.findViewById(R.id.account_email_btn_finish);
		
		layChoose=(LinearLayout) view.findViewById(R.id.layout_email_add_choose);
		layServers=(LinearLayout) view.findViewById(R.id.layout_email_add_outgoing);
		layLogin=(LinearLayout) view.findViewById(R.id.layout_email_add_login);
		layConfirm=(LinearLayout) view.findViewById(R.id.layout_email_add_confirm);
		
		editEmail=(EditText) view.findViewById(R.id.account_field_email);
		editServerOutgoing=(EditText) view.findViewById(R.id.account_field_server_outgoing);
		editServerOutgoingPort=(EditText) view.findViewById(R.id.account_field_port_outgoing);
		editServerIncoming=(EditText) view.findViewById(R.id.account_field_server_incoming);
		editServerIncomingPort=(EditText) view.findViewById(R.id.account_field_port_incoming);
		
		editUsername=(EditText) view.findViewById(R.id.account_field_login_user);
		editPassword=(EditText) view.findViewById(R.id.account_field_login_password);
		
		radioOutgoingSsl=(RadioButton) view.findViewById(R.id.account_radio_outgoing_enc_ssl);
		radioOutgoingTls=(RadioButton) view.findViewById(R.id.account_radio_outgoing_enc_tls);
		radioOutgoingNone=(RadioButton) view.findViewById(R.id.account_radio_outgoing_enc_none);
		
		radioIncomingNone=(RadioButton) view.findViewById(R.id.account_radio_incoming_enc_none);
		radioIncomingSsl=(RadioButton) view.findViewById(R.id.account_radio_incoming_enc_ssl);
		radioIncomingTls=(RadioButton) view.findViewById(R.id.account_radio_incoming_enc_tls);
		
		radioIncomingUsePop=(RadioButton) view.findViewById(R.id.account_radio_incoming_use_pop);
		radioIncomingUseImap=(RadioButton) view.findViewById(R.id.account_radio_incoming_use_imap);
		radioIncomingUseImap.setChecked(true);
		
		errorIncomingServer=(TextView) view.findViewById(R.id.account_error_incoming_server);
        errorIncomingPort=(TextView) view.findViewById(R.id.account_error_incoming_port);
		errorOutgoingServer=(TextView) view.findViewById(R.id.account_error_outgoing_server);
		errorOutgoingPort=(TextView) view.findViewById(R.id.account_error_outgoing_port);
		
		layConfirmTest=(LinearLayout) view.findViewById(R.id.layout_email_add_confirm_test);
		layConfirmSuccess=(LinearLayout) view.findViewById(R.id.layout_email_add_confirm_sucess);
		layConfirmFailed=(LinearLayout) view.findViewById(R.id.layout_email_add_confirm_failed);
		errorConfirmFailed=(TextView) view.findViewById(R.id.layout_email_add_confirm_failed_message);
		
		useEmailCheck=(CheckBox) view.findViewById(R.id.account_use_email_check);
		useEmailCheck.setOnClickListener(onUseEmailCheckboxClicked);
		
		alertMessage=(TextView) activity.findViewById(R.id.alert_message);

        TextView ft1 = (TextView) view.findViewById(R.id.account_email_f_title);
        TextView ft2 = (TextView) view.findViewById(R.id.account_email_f_heading);
        TextView ft3 = (TextView) view.findViewById(R.id.account_email_f_ip);
        TextView ft4 = (TextView) view.findViewById(R.id.account_email_f_outport);
        TextView ft5 = (TextView) view.findViewById(R.id.account_email_f_encrypt);
        TextView ft6 = (TextView) view.findViewById(R.id.account_email_f_incoming);
        TextView ft7 = (TextView) view.findViewById(R.id.account_email_f_inc_type);
        TextView ft8 = (TextView) view.findViewById(R.id.account_email_f_add_ip);
        TextView ft9 = (TextView) view.findViewById(R.id.account_email_f_inport);
        TextView ft10 = (TextView) view.findViewById(R.id.account_email_f_enc);
        TextView ft11 = (TextView) view.findViewById(R.id.account_email_f_login);
        TextView ft12 = (TextView) view.findViewById(R.id.account_email_f_login_store);
        TextView ft13 = (TextView) view.findViewById(R.id.account_email_f_test);
        TextView ft14 = (TextView) view.findViewById(R.id.account_email_f_test_fail);
        TextView ft15 = (TextView) view.findViewById(R.id.account_email_f_test_add);

        B.addStyle(new TextView[]{ft1,ft2,ft3,ft4,ft5,ft6,ft7,ft8,ft9,ft10,ft11,ft12,ft13,ft14,ft15});

        B.addStyle(new TextView[]{alertMessage,errorConfirmFailed,errorOutgoingPort,errorOutgoingServer,errorIncomingPort,errorIncomingServer});
        B.addStyle(new EditText[]{editEmail,editServerOutgoing,editServerOutgoingPort,editServerIncoming,editServerIncomingPort,editUsername,editPassword});

		hideLayers();
		
		if(STAGE==0) {
			
			goLayerChoose();
			
		} else if(STAGE==1) {
			goLayerServer();

			
		} else if(STAGE==2) {
			goLayerLogin();
			
		} else if(STAGE==3) {
			goLayerLogin();
			
		}
		
	}
	protected void hideLayers() {
		layChoose.setVisibility(View.GONE);
		layServers.setVisibility(View.GONE);
		layLogin.setVisibility(View.GONE);
		layConfirm.setVisibility(View.GONE);
		hideAlert();
	}
	protected void showAlert(String message) {
		if(alertMessage!=null) {
			alertMessage.setText(message);
			alertMessage.setVisibility(View.VISIBLE);
            removeAlertMessage.postDelayed(new Runnable() {
                public void run() {
                    hideAlert();
                }
            },4000);
		}
	}
	protected void hideAlert() {
		if(alertMessage!=null) {
			alertMessage.setText("");
			alertMessage.setVisibility(View.GONE);
		}
	}
	protected void goLayerChoose() {
		STAGE=0;
		hideLayers();
		btnBack.setVisibility(View.INVISIBLE);
		btnNext.setVisibility(View.VISIBLE);
		layChoose.setVisibility(View.VISIBLE);
		btnNext.setOnClickListener(detectListner);
		//editEmail.setText("pcooperuk@smtp.live.com");
	}
	protected void goLayerServer() {
		STAGE=1;
		hideLayers();
		layServers.setVisibility(View.VISIBLE);
		
		btnBack.setEnabled(true);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setOnClickListener(backListner);
		
		btnNext.setEnabled(true);
		btnNext.setVisibility(View.VISIBLE);
		btnNext.setOnClickListener(serversListner);
	}

	protected void goLayerLogin() {
		STAGE=2;
		hideLayers();
		layLogin.setVisibility(View.VISIBLE);
		
		btnBack.setEnabled(true);
		btnBack.setVisibility(View.VISIBLE);
		btnBack.setOnClickListener(backListner);
		
		btnNext.setAlpha(1.0F);
		btnNext.setEnabled(true);
		btnNext.setOnClickListener(loginListner);
		
		if(useEmailCheck.isChecked()) {
			editUsername.setText(editEmail.getText().toString());
			editUsername.setEnabled(false);
		} else {
			editUsername.setEnabled(true);
		}
		
		
	}
	protected void goLayerConfirm() {
		STAGE=3;
		
		
		hideLayers();
		layConfirmSuccess.setVisibility(View.GONE);
		layConfirmTest.setVisibility(View.VISIBLE);
		layConfirmFailed.setVisibility(View.GONE);
		//--
		//btnBack.setEnabled(true);
		btnBack.setVisibility(View.GONE);
		btnBack.setOnClickListener(backListner);
        btnNext.setVisibility(View.GONE);
		
		btnFinish.setOnClickListener(finishListner);
		
		layConfirm.setVisibility(View.VISIBLE);
		
		new testConnectEmail().execute(Boolean.TRUE);

	}
	
	public OnClickListener onUseEmailCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(useEmailCheck.isChecked()) {
				editUsername.setText(editEmail.getText().toString());
				editUsername.setEnabled(false);
			} else {
				editUsername.setEnabled(true);
			}
		}
	};	
	protected OnClickListener finishListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			
			Bgo.goPreviousFragment(activity);
		}
	};	
	protected OnClickListener detectListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			clearErrors();
			String email=editEmail.getText().toString();
			if(isValidEmail(email)) {
				if(AccountsDb.getEmailAccount(email)==null) {
					Account account=DefaultProperties.getDetectAccountFromEmail(email);
					if(account!=null) {
						populateFieldsWithAccount(account);

						account.setString(Account.STRING_EMAIL_ADDRESS, email);
					}
					goLayerServer();
					Device.hideKeyboard(activity);
				} else {
					showAlert(activity.getString(R.string.alert_already_email));
				}
			} else {
				showAlert(activity.getString(R.string.alert_valid_email));
			}
						
		}
	};	
	public void populateFieldsWithAccount(Account account) {
		if(account.has(Account.STRING_EMAIL_ADDRESS))
			editEmail.setText(account.getString(Account.STRING_EMAIL_ADDRESS));
		editServerOutgoing.setText(account.getString(Account.STRING_EMAIL_OUTGOING_SERVER));
		editServerOutgoingPort.setText(Integer.valueOf(account.getInt(Account.INT_EMAIL_OUTGOING_PORT)).toString());
		editServerIncoming.setText(account.getString(Account.STRING_EMAIL_INCOMING_SERVER));
		editServerIncomingPort.setText(Integer.valueOf(account.getInt(Account.INT_EMAIL_INCOMING_PORT)).toString());
		
		int use = account.getInt(Account.INT_EMAIL_USE_);
		switch(use) {
		case Account.EMAIL_USE_POP: radioIncomingUsePop.setChecked(true); break;
		default: radioIncomingUseImap.setChecked(true); break;
		}
		
		int inc=account.getInt(Account.INT_EMAIL_INCOMING_SUBTYPE_);
		switch(inc) {
			case Account.SUBTYPE_NONE:  radioIncomingNone.setChecked(true); break;
			case Account.SUBTYPE_SSL:  radioIncomingSsl.setChecked(true); break;
			case Account.SUBTYPE_TLS:  radioIncomingTls.setChecked(true); break;
		}
		int out=account.getInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_);
		switch(out) {
			case Account.SUBTYPE_NONE:  radioOutgoingNone.setChecked(true); break;
			case Account.SUBTYPE_SSL:  radioOutgoingSsl.setChecked(true); break;
			case Account.SUBTYPE_TLS:  radioOutgoingTls.setChecked(true); break;
		}
		
		editUsername.setText(account.getString(Account.STRING_LOGIN_NAME));
		editPassword.setText(account.getString(Account.STRING_LOGIN_PASSWORD));
	}
	protected OnClickListener backListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			clearErrors();
			if(STAGE<=1) {
				goLayerChoose();
			} else if(STAGE==2) {
				goLayerServer();
			} else if(STAGE==3)
				goLayerLogin();
		}
	};	
	protected OnClickListener loginListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(editUsername.getText().length()>1
				&& editPassword.getText().length()>1) {

				goLayerConfirm();
				
			} else {
				showAlert(activity.getString(R.string.label_invalid_login));
				
			}
			
		}
	};	
	protected void clearErrors() {
		showAlert(" ");
		errorIncomingServer.setText(" ");
		errorOutgoingServer.setText(" ");
		errorIncomingPort.setText(" ");
		errorOutgoingPort.setText(" ");
	}
	protected OnClickListener serversListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			boolean validInServer=isValidServer(editServerIncoming.getText().toString());
			boolean validOutServer=isValidServer(editServerOutgoing.getText().toString());
			
			boolean validInPort=isValidPort(Sf.toInt(editServerIncomingPort.getText().toString()));
			boolean validOutPort=isValidPort(Sf.toInt(editServerOutgoingPort.getText().toString()));
					
			if(validInServer && validOutServer && validInPort && validOutPort) {

				goLayerLogin();

			} else {
				clearErrors();
				showAlert(activity.getString(R.string.label_errors_below));
				if(!validInServer)
					errorIncomingServer.setText(R.string.label_invalid_server);
				if(!validOutServer)
					errorOutgoingServer.setText(R.string.label_invalid_server);
				if(!validInPort)
					errorIncomingPort.setText(R.string.label_invalid_port);
				if(!validOutPort)
					errorOutgoingPort.setText(R.string.label_invalid_port);
			}
			
		}
	};	
	protected Account createAccount() {
		Account account=new Account(Account.TYPE_EMAIL);
		account.setLong(Account.LONG_ID, Account.generateAccountId());
		account.setString(Account.STRING_EMAIL_ADDRESS, editEmail.getText().toString());
		account.setString(Account.STRING_EMAIL_OUTGOING_SERVER, editServerOutgoing.getText().toString());
		account.setString(Account.STRING_EMAIL_INCOMING_SERVER, editServerIncoming.getText().toString());
		account.setInt(Account.INT_EMAIL_OUTGOING_PORT, Sf.toInt(editServerOutgoingPort.getText().toString()));
		account.setInt(Account.INT_EMAIL_INCOMING_PORT, Sf.toInt(editServerIncomingPort.getText().toString()));

        //BLog.e("ACCW","POP  : "+radioOutgoingSsl.isChecked());
        //BLog.e("ACCW","IMAP  : "+radioOutgoingTls.isChecked());

		if(radioIncomingUseImap.isChecked())
			account.setInt(Account.INT_EMAIL_USE_, Account.EMAIL_USE_IMAP);
		else
			account.setInt(Account.INT_EMAIL_USE_, Account.EMAIL_USE_POP);
		
		if(radioOutgoingSsl.isChecked())
			account.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_SSL);
		else if(radioOutgoingTls.isChecked())
			account.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_TLS);
		else
			account.setInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_, Account.SUBTYPE_NONE);
		
		if(radioIncomingSsl.isChecked())
			account.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_SSL);
		else if(radioIncomingTls.isChecked())
			account.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_TLS);
		else
			account.setInt(Account.INT_EMAIL_INCOMING_SUBTYPE_, Account.SUBTYPE_NONE);
		
		

		account.setString(Account.STRING_LOGIN_NAME, editUsername.getText().toString());
		account.setString(Account.STRING_LOGIN_PASSWORD, editPassword.getText().toString());

        //BLog.e("ACCW","EM_USE_  : "+account.getInt(Account.INT_EMAIL_USE_));
        //BLog.e("ACCW","EM_USE_SUB_  : "+account.getInt(Account.INT_EMAIL_OUTGOING_SUBTYPE_));
        //BLog.e("ACCW","EM_IN_PORT_  : "+account.getInt(Account.INT_EMAIL_INCOMING_PORT));

		return account;
		
	}


	protected boolean isValidEmail(String emailAddress)   {
        
        if(emailAddress!=null && Sf.isValidEmail(emailAddress))
            return true;
        else
            return false;
    }
    protected boolean isValidServer(String serverdomain)   {
        
        if(serverdomain!=null && serverdomain.matches(regExpServerValidate))
            return true;
        else
            return false;
    }
    protected boolean isValidPort(int port) {
    	if(port!=0 && port<65535)
    		return true;
    	return false;
    }

	protected class testConnectEmail extends AsyncTask<Boolean, Void, Boolean> {
		EmailServiceInstance emailService;
		Account account;
		@Override
		protected Boolean doInBackground(Boolean... params) {
			account = createAccount();
			emailService = new EmailServiceInstance(activity, account,true);
			emailService.doConnect(activity);
		    return emailService.isConnected();
	
		}      
	
		@Override
		protected void onPostExecute(Boolean result) {
			
			if(result) {



				AccountsDb.addAccount(account);

				//ActionBarManager.refresh(activity);
				
				layConfirmSuccess.setVisibility(View.VISIBLE);
				layConfirmTest.setVisibility(View.GONE);
				layConfirmFailed.setVisibility(View.GONE);
				btnNext.setVisibility(View.GONE);
				btnBack.setVisibility(View.GONE);

                Account created = AccountsDb.getEmailAccount(account.getString(Account.STRING_EMAIL_ADDRESS));

                BriefService.runEmailFirstTimeTask(activity,created);

				
			} else {
				layConfirmFailed.setVisibility(View.VISIBLE);
				layConfirmTest.setVisibility(View.GONE);
				layConfirmSuccess.setVisibility(View.GONE);
                StringBuilder showtext = new StringBuilder(activity.getResources().getString(R.string.error_instantiate_email_server));

                //    if error message contains
                //   http:     google.com   *   answer=78754
                //   launch google oauth
                // see twitter for oath implement.


                // also add texts about what to do if cant connect, contact admin, check server setting and requirments, the contact direct to brief @ emaail



                if(emailService.getConnectError()!=null) {
                    showtext.append("\n");
                    showtext.append(emailService.getConnectError());
                }
                errorConfirmFailed.setText(showtext.toString());
				btnNext.setVisibility(View.GONE);
			}
		}
	
	 
	}
}
