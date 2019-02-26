package run.brief.email;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import run.brief.BriefManager;
import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.bToPersonsView;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.Email;
import run.brief.beans.Person;
import run.brief.beans.SignatureBean;
import run.brief.contacts.ContactsSelectedClipboard;
import run.brief.service.BriefService;
import run.brief.settings.AccountsDb;
import run.brief.util.PlusMember;
import run.brief.util.Sf;
import run.brief.util.ViewManagerText;
import run.brief.util.explore.FileExploreFragment;
import run.brief.util.explore.FilePopListener;
import run.brief.util.explore.FilesAdapter;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;

public final class EmailSendFragment extends BFragment implements BRefreshable {
	
	//private Handler contactsHandler = new Handler();
	//private static ArrayList<Person> contacts=null;
	//ImapService imap;

	private View view;
	private Activity activity;
	
	//private static TextView viewfrom;
	private EditText viewsubject;
	//private static EditText viewto;
	private EditText viewcontent;
	private MessageWebView viewrepltohtml;
	private TextView viewreplytotext;
	private CheckBox signaturecheck;
	private CheckBox includecheck;
	private Spinner emailfrom;
	private GridView attachfiles;
	private FilesAdapter adapter;
	
	private RelativeLayout emailView;
	private View upgradeView;
	   
	private bToPersonsView toPeopleView;
	private Email replytoemail;
	private Email email;
	
	private Account acc;
	private EmailServiceInstance es;

    //private int replyToIndex;
    private PopListner listen;

    private ImageView emailSigPop;

    private SignatureBean useSignature;
	//EmailService emails;
	
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//this.setHasOptionsMenu(true);

		activity=getActivity();
        AccountsDb.init();
        SignatureDb.init(activity);
		view=inflater.inflate(R.layout.email_new,container, false);

        attachfiles = (GridView) view.findViewById(R.id.email_attach_files);

        viewsubject = (EditText) view.findViewById(R.id.email_field_subject);
        viewcontent = (EditText) view.findViewById(R.id.email_content_text);




        viewrepltohtml = (MessageWebView) view.findViewById(R.id.email_content_replyto_webview);
        viewreplytotext = (TextView) view.findViewById(R.id.email_content_replyto);

        signaturecheck=(CheckBox) view.findViewById(R.id.email_signature_check);
        signaturecheck.setOnClickListener(signatureListener);
        includecheck = (CheckBox) view.findViewById(R.id.email_include_original);
        includecheck.setChecked(false);


        B.addStyle(new TextView[]{includecheck,signaturecheck,viewreplytotext,viewsubject,viewcontent});

        emailfrom = (Spinner) view.findViewById(R.id.email_field_from_select);

        emailSigPop = (ImageView) view.findViewById(R.id.email_signature_pop);

		//ContactsSelectedClipboard.clear();
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		State.setCurrentSection(State.SECTION_EMAIL_NEW);

		
		upgradeView = PlusMember.getPlusMemberUpgradeView(activity, view, cancelUpgradeListener);
		emailView = (RelativeLayout) view.findViewById(R.id.email_content);


        toPeopleView = (bToPersonsView) view.findViewById(R.id.email_show_to);
        toPeopleView.setSearchParent(emailView);
        toPeopleView.setContext(activity);
        toPeopleView.setMaxHeight(250);
        toPeopleView.setViewMeasureTop(emailfrom);
        toPeopleView.setMode(bToPersonsView.MODE_VIEW_ADD);
        toPeopleView.setContactsType(ContactsSelectedClipboard.CONTACTS_TYPE_EMAIL);

		refresh();
	}

	public void refresh() {
        //Validator.calldata();
		BriefManager.clearController(activity);
        /*
        amb = new ActionModeBack(activity, activity.getResources().getString(R.string.email_send)
                ,R.menu.email_new
                , new ActionModeCallback() {
            @Override
            public void onActionMenuItem(ActionMode mode, MenuItem item) {
                onOptionsItemSelected(item);
            }
        });
        */
        ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.email_send),R.menu.email_new,R.color.actionbar_email);

		State.setContactsMode(State.CONTACT_MODE_SELECT_EMAIL);


        if(State.hasStateObject(State.SECTION_EMAIL_NEW,StateObject.INT_FORCE_NEW)) {
            State.clearStateObject(State.SECTION_EMAIL_NEW,StateObject.STRING_BJSON_OBJECT);
            replytoemail=null;
            email=new Email();
            ContactsSelectedClipboard.clear();
        }
        if(State.hasStateObject(State.SECTION_EMAIL_NEW,StateObject.LONG_USE_ACCOUNT_ID)) {
            long accid= State.getStateObjectLong(State.SECTION_EMAIL_NEW,StateObject.LONG_USE_ACCOUNT_ID);
            acc = AccountsDb.getAccountById(accid);
            //BLog.e("GOTCOUNT", "and set it");
            //BLog.e("SEND", ""+accid);
        }
        if(State.hasStateObject(State.SECTION_EMAIL_NEW,StateObject.STRING_BJSON_OBJECT)) {
            JSONObject em = new JSONObject(State.getStateObjectString(State.SECTION_EMAIL_NEW,StateObject.STRING_BJSON_OBJECT));
            //BLog.e("LOADING","------");
            if(em!=null) {
                email = new Email(em);
                //BLog.e("LOADING",email.getString(Email.STRING_TO));

                if(!ContactsSelectedClipboard.isEmpty()) {
                    //populateEmailFromFields();
                } else {
                    ContactsSelectedClipboard.addFromEmailSummary(activity, email.getString(Email.STRING_TO));
                    //BLog.e("LOADING","solo contat");
                }

                //populateFieldsFromEmail();
                long replyId=0;
                if(email.has(Email.LONG_REPY_TO_INDEX_NO_SAVE))
                    replyId= email.getLong(Email.LONG_REPY_TO_INDEX_NO_SAVE);

                viewsubject.setText(email.getString(Email.STRING_SUBJECT));
                viewcontent.setText(email.getString(Email.STRING_MESSAGE));
                //BLog.e("LOAD REPLY TO","contacts size: "+ContactsSelectedClipboard.size());
                //email=new Email();
                if(acc!=null) {
                    es = EmailService.getService(activity, acc);

                    if(es!=null) {
                        replytoemail = es.getEmailById(replyId);

                    } else {
                        //BLog.e("EM_V", "No email service");
                    }

                }
                //String sob = State.getStateObjectString(StateObject.STRING_EMAIL_SUMMARY);
            }

        }




        if(State.hasStateObject(State.SECTION_EMAIL_NEW,StateObject.STRING_USE_DATABASE_ID)) {
            populateUsingDatabaseId();

        } else if(State.hasStateObject(State.SECTION_EMAIL_NEW,StateObject.INT_USE_SELECTED_INDEX)) {
            populateUsingSelectedIndex();

        } else if(State.hasStateObject(State.SECTION_FILE_EXPLORE,StateObject.STRING_FILE_PATH)) {
            //BLog.e("NTE", state.getObjectAsString());
            try {
                JSONArray jarr = new JSONArray(State.getStateObjectString(State.SECTION_FILE_EXPLORE,StateObject.STRING_BJSON_ARRAY));
                for(int i=0; i<jarr.length(); i++) {
                    email.addAttachment(jarr.getString(i));
                    //emailfrom.
                    //note.addFile(jarr.getString(i));
                }
                State.clearStateObject(State.SECTION_FILE_EXPLORE,StateObject.STRING_BJSON_ARRAY);
                showAttachedFiles();
            } catch(Exception e) {}
        }


        List<String> list = new ArrayList<String>();
        int selPos=0;
        ArrayList<Account> accounts=AccountsDb.getAllEmailAccounts();
        for(int i=0; i<accounts.size(); i++ ) {
            Account tacc=accounts.get(i);
            if(acc!=null && tacc.getString(Account.STRING_EMAIL_ADDRESS).equals(acc.getString(Account.STRING_EMAIL_ADDRESS)))
                selPos=i;
            list.add(tacc.getString(Account.STRING_EMAIL_ADDRESS));

        }
        ArrayAdapter<String> dataAdapter = new SpinnerArrayAdapter(activity,android.R.layout.simple_spinner_item, list);
        //ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(activity,android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        emailfrom.setAdapter(dataAdapter);
        emailfrom.setSelection(selPos);


        toPeopleView.invalidate();
        toPeopleView.refreshData();
        showAttachedFiles();
        State.clearStateObjects(State.SECTION_EMAIL_NEW);
	    	//toPeopleView.re
            //populateEmailFromFields();
            //populateFieldsFromEmail();
	    	
        if(replytoemail!=null) {
            String message= replytoemail.getString(Email.STRING_MESSAGE);
            if(message!=null) {

                if(Sf.isHtml(message)) {

                    message = Sf.htmlWrap(message);

                    viewrepltohtml.getSettings().setJavaScriptEnabled(false);
                    viewrepltohtml.loadDataWithBaseURL("", message, "text/html", "UTF-8", "");
                    viewrepltohtml.setVisibility(View.VISIBLE);
                    viewreplytotext.setVisibility(View.GONE);
                } else {
                    viewreplytotext.setText(message);
                    viewreplytotext.setVisibility(View.VISIBLE);
                    viewrepltohtml.setVisibility(View.GONE);
                }
            }
            includecheck.setVisibility(View.VISIBLE);
            viewrepltohtml.setVisibility(View.VISIBLE);
        } else {
            includecheck.setVisibility(View.GONE);
            viewrepltohtml.setVisibility(View.GONE);
        }
        refreshData();

	}
    public void refreshData() {
        refreshSignature();
    }

    private void showUpgrade() {
        upgradeView.setVisibility(View.VISIBLE);
        emailView.setVisibility(View.GONE);
    }

    private void refreshSignature() {
        useSignature=null;
        Account forAccount = getSendAccount(emailfrom.getSelectedItem().toString());
        if(HomeFarm.isSubscriber()) {

            List<SignatureBean> signatures = SignatureDb.getSignatures(forAccount);

            for(SignatureBean sb: signatures) {
                if(sb.getInt(SignatureBean.INT_USE)>0) {
                    useSignature = sb;

                }
            }
            signaturecheck.setEnabled(true);
            emailSigPop.setOnClickListener(signatureOnClick);
        } else {
            signaturecheck.setEnabled(false);
            emailSigPop.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    showUpgrade();
                }
            });
        }
        if(useSignature==null)
            useSignature=SignatureBean.getDefaultSignature(activity,forAccount);

        signaturecheck.setText(useSignature.getString(SignatureBean.STRING_SIGNATURE));
    }
    private void populateUsingDatabaseId() {
        long dbid= Sf.toLong(State.getStateObjectString(State.SECTION_EMAIL_NEW,StateObject.STRING_USE_DATABASE_ID));
        //BLog.e("LOAD REPLY TO", "dbid: " + dbid);
        //email=new Email();
        if(acc!=null) {
            es = EmailService.getService(activity, acc);

            if(es!=null) {
                replytoemail = es.getEmailById(dbid);

            }
        }
        if(replytoemail!=null) {
            String subject=replytoemail.getString(Email.STRING_SUBJECT);
            if(!subject.startsWith("re"))
                subject="re: "+subject;
            viewsubject.setText(subject);
            ArrayList<Person> persons = null;
            if(replytoemail.getBoolean(Email.BOOL_IS_MINE_NO_SAVE)) {
                persons=Sf.stripdownEmails(replytoemail.getString(Email.STRING_TO));
            } else {
                persons=Sf.stripdownEmails(replytoemail.getString(Email.STRING_FROM));
            }

            for(Person p:persons) {
                //BLog.e("ELSE", "P: "+p.getString(Person.STRING_NAME));
                ContactsSelectedClipboard.addPerson(p);
            }

            //viewcontent.setText(replytoemail.getString(Email.STRING_MESSAGE));
        }
    }
    private OnClickListener signatureOnClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Account forAccount = getSendAccount(emailfrom.getSelectedItem().toString());
            List<SignatureBean> signatures = SignatureDb.getSignatures(forAccount);

            PopupMenu popupMenu = new PopupMenu(activity, view);
            //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());
            boolean hasUse=false;
            int count = 1;
            for(SignatureBean sig: signatures) {
                MenuItem mi= popupMenu.getMenu().add(0,count,count,sig.getString(SignatureBean.STRING_SIGNATURE));
                count++;
                if(sig.getInt(SignatureBean.INT_USE)>0) {
                    mi.setTitle(ViewManagerText.EMO_TICK_BOX+mi.getTitle());
                    hasUse=true;
                }
            }
            MenuItem mi= popupMenu.getMenu().add(0,0,0,SignatureBean.getDefaultSignature(activity));
            if(!hasUse) {
                mi.setTitle(ViewManagerText.EMO_TICK_BOX+mi.getTitle());

            }

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    Account forAccount = getSendAccount(emailfrom.getSelectedItem().toString());
                    List<SignatureBean> signatures = SignatureDb.getSignatures(forAccount);
                    int pos=item.getItemId();
                    //BLog.e("ITMID","itemid: "+pos);
                    if(pos==0) {
                        for(SignatureBean sb: signatures) {
                            if(sb.getInt(SignatureBean.INT_USE)>0) {
                                sb.setInt(SignatureBean.INT_USE,0);
                                sb.setInt(SignatureBean.INT_CHECKED,0);
                                SignatureDb.updateSignature(sb);
                            }
                        }
                    } else {
                        pos--;

                        for(SignatureBean sb: signatures) {
                            if(sb.getInt(SignatureBean.INT_USE)>0) {
                                sb.setInt(SignatureBean.INT_USE,0);
                                sb.setInt(SignatureBean.INT_CHECKED,0);
                                SignatureDb.updateSignature(sb);
                            }
                        }
                        SignatureBean usb = signatures.get(pos);
                        usb.setInt(SignatureBean.INT_USE,1);
                        usb.setInt(SignatureBean.INT_CHECKED,1);
                        SignatureDb.updateSignature(usb);
                    }

                    refreshData();
                    return true;
                }
            });

            popupMenu.show();
        }
    };
    private void populateUsingSelectedIndex() {
        int replyToIndex= State.getStateObjectInt(State.SECTION_EMAIL_NEW,StateObject.INT_USE_SELECTED_INDEX);
        //BLog.e("LOAD REPLY TO", "index: " + replyToIndex);
        //email=new Email();
        if(acc!=null) {
            es = EmailService.getService(activity, acc);

            if(es!=null) {
                //es.getE
                //BLog.e("EM_V", "account: "+acc.getString(Account.STRING_EMAIL_ADDRESS));
                replytoemail = es.getEmail(replyToIndex);

            } else {
                //BLog.e("EM_V", "No email service");
            }
        } else {
            //BLog.e("EM_V", "No account found id: ");
        }
        if(replytoemail!=null) {
            //BLog.e("EM_V", "replytoemail");
            //viewto.setText(replytoemail.getString(Email.STRING_FROM));
            viewsubject.setText("re: "+replytoemail.getString(Email.STRING_SUBJECT));
            ArrayList<Person> persons = null;
            if(replytoemail.getBoolean(Email.BOOL_IS_MINE_NO_SAVE)) {
                persons=Sf.stripdownEmails(replytoemail.getString(Email.STRING_TO));
            } else {
                persons=Sf.stripdownEmails(replytoemail.getString(Email.STRING_FROM));
            }

            for(Person p:persons) {
                //BLog.e("ELSE", "P: "+p.getString(Person.STRING_NAME));
                ContactsSelectedClipboard.addPerson(p);
            }

            //viewcontent.setText(replytoemail.getString(Email.STRING_MESSAGE));
        }
    }


    private class SpinnerArrayAdapter<String> extends ArrayAdapter<java.lang.String> {

        List<java.lang.String> list;
        public SpinnerArrayAdapter(Context context, int textViewResourceId, List<java.lang.String> list) {
            super(context, textViewResourceId,list);
            this.list=list;
        }

        public TextView getView(int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            //String str=;
            acc=AccountsDb.getEmailAccount(list.get(position));
            v.setText(list.get(position));
            v.setCompoundDrawablesWithIntrinsicBounds(acc.getAccountRIcon(), 0, 0, 0);
            v.setGravity(Gravity.CENTER_VERTICAL);
            v.setPadding(5, 0, 0, 0);
            B.addStyle(v);
            return v;
        }

        public TextView getDropDownView(int position, View convertView, ViewGroup parent) {
            TextView v = (TextView) super.getView(position, convertView, parent);
            acc=AccountsDb.getEmailAccount(list.get(position));
            v.setText(list.get(position));
            v.setCompoundDrawablesWithIntrinsicBounds(acc.getAccountRIcon(),0,0,0);
            v.setPadding(5,5,5,5);
            v.setGravity(Gravity.CENTER_VERTICAL);
            B.addStyle(v);
            return v;
        }

    }
	@Override
	public void onPause() {
		super.onPause();
		populateEmailFromFields();
        if(replytoemail!=null)
            email.setLong(Email.LONG_REPY_TO_INDEX_NO_SAVE,replytoemail.getLong(Email.LONG_ID));

        //BLog.e("SAVE",email.toString());
		State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_BJSON_OBJECT,email.toString()));
        State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.LONG_USE_ACCOUNT_ID,acc.getLong(Account.LONG_ID)));
        //if(replyToIndex!=0)
        //    State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.INT_USE_SELECTED_INDEX,replyToIndex));
		//State.addToState(new StateObject(StateObject.STRING_EMAIL_SUMMARY,email.getString(Email.STRING_TO)));
	}

	public void showAttachedFiles() {
		if(email==null)
            email=new Email();

        listen=new PopListner();
        adapter=new FilesAdapter(getActivity(),email.getAttachments(),FilesAdapter.TYPE_EDIT,listen);
		//adapter=new FilesAdapter(getActivity(),email.getAttachments());
		attachfiles.setAdapter(adapter);
		if(adapter.getCount()>1) {
			//View vi=adapter.getView(0, null, attachfiles);
			//Rect outRect=new Rect();
			//vi.refreshDrawableState();
			//vi.getDrawingRect(outRect);
			//BLog.e("RECTH", ""+outRect.height());

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,140*adapter.getCount());
			
			attachfiles.setLayoutParams(params);
		}
		//adapter.notifyDataSetChanged();
	}
    public class PopListner extends FilePopListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(menuItem.getTitle().equals(activity.getResources().getString(R.string.label_open))) {
                File f = new File(adapter.selectedFile);
                Device.openFile(activity, f);

            } else {

                ArrayList<String> files=email.getAttachments();
                for(int i=0; i<files.size(); i++) {
                    if(files.get(i).equals(adapter.selectedFile)) {
                        files.remove(i);
                        break;
                    }
                }

                email.setString(Email.STRING_ATTACHMENTS, "");
                for(String file: files) {
                    email.addAttachment(file);
                }
                showAttachedFiles();

            }
            return false;
        }
    }
	public OnClickListener signatureListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			
			if(HomeFarm.isSubscriber()) {
				
			} else {
				signaturecheck.setChecked(true);
				upgradeView.setVisibility(View.VISIBLE);
				emailView.setVisibility(View.GONE);
				//Bgo.openFragmentAnimate(activity, new );
			}
		
			
		}
	};
	
	public void populateEmailFromFields() {
		if(email==null)
			email=new Email();
		//BLog.e("INEMAIL", "IN: "+ContactsSelectedClipboard.size()+" -- "+ContactsSelectedClipboard.getEmailSummary());
		email.setString(Email.STRING_TO,ContactsSelectedClipboard.getEmailSummary());
        if(emailfrom!=null)
		    email.setString(Email.STRING_FROM, emailfrom.getSelectedItem().toString());
		email.setString(Email.STRING_SUBJECT, viewsubject.getText().toString());
		email.setString(Email.STRING_MESSAGE, viewcontent.getText().toString());
		email.setInt(Email.INT_STATE, Brief.STATE_SENDNG);
		email.setString(Email.STRING_UUID, "tmp");
	}
	public void populateFieldsFromEmail() {
		if(email!=null) {
			viewsubject.setText(email.getString(Email.STRING_SUBJECT));
			viewcontent.setText(email.getString(Email.STRING_MESSAGE));
			
			showAttachedFiles();

		}
	}
	

	public OnClickListener cancelUpgradeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			signaturecheck.setChecked(true);
			upgradeView.setVisibility(View.GONE);
			emailView.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai frag");
	    //inflater.inflate(R.menu.email_new, menu);
	    //return super.onCreateOptionsMenu(menu,inflater);
	}
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_email_attach:
				State.setFileExploreState(State.FILE_EXPLORE_STATE_SELECTFILE);
				Bgo.openFragmentBackStack(activity,FileExploreFragment.class);
				break;
			case R.id.action_email_send:
				sendEmailNow();
				break;
		}	
		return true;
	}

    private Account getSendAccount(String fromEmail) {
        //String femail = email.getString(Email.STRING_FROM);
        for(Account sacc: AccountsDb.getAllEmailAccounts()) {
            if(fromEmail.equals(sacc.getString(Account.STRING_EMAIL_ADDRESS)))
                return sacc;
        }
        return null;
    }

	private void sendEmailNow() {
		populateEmailFromFields();
		
		//EmailServiceInstance emsi = EmailService.getService(activity, acc);
        String to=email.getString(Email.STRING_TO);
        if(to==null || to.length()<2) {
            Toast.makeText(activity,activity.getString(R.string.email_send_error_noto),Toast.LENGTH_SHORT).show();
            return;
        }
        String subj=email.getString(Email.STRING_SUBJECT);
        String body=email.getString(Email.STRING_MESSAGE);
        if((subj==null && subj.length()<1) || (body==null && body.length()<1)) {
            Toast.makeText(activity,activity.getString(R.string.email_send_error_notext),Toast.LENGTH_SHORT).show();
            return;
        }

		email.setInt(Email.INT_STATE, Brief.STATE_SENDNG);
		email.setLong(Email.LONG_DATE, (new Date()).getTime());

        if(signaturecheck.isChecked()) {
            email.setString(Email.STRING_MESSAGE,email.getString(Email.STRING_MESSAGE)+"\n\n"+signaturecheck.getText());
        }
        if(includecheck.isChecked() && replytoemail!=null) {
            email.setString(Email.STRING_MESSAGE,email.getString(Email.STRING_MESSAGE)+"\n\n\n"+replytoemail.getString(Email.STRING_MESSAGE));

        }


        String femail = email.getString(Email.STRING_FROM);


        if(femail!=null) {
            Account tacc = null;//getSendAccount();
            for(Account sacc: AccountsDb.getAllEmailAccounts()) {
                if(femail.equals(sacc.getString(Account.STRING_EMAIL_ADDRESS)))
                    tacc= sacc;
            }

			
			if(tacc!=null) {
				BriefService.addToSendServiceQue(activity,tacc, Brief.WITH_EMAIL, email.getBean());
				//BLog.e("SEM", "Email added to SendQue");
				BriefManager.setDirty(BriefManager.IS_DIRTY_SEND_ITEMS);
                int lastSection = State.getPreviousSection();
                if(lastSection==State.SECTION_EMAIL_VIEW) {
                    BLog.e("EXTRA","BACKSTACK called");
                    State.sectionsGoBackstack();
                }
                Bgo.goPreviousFragment(activity);
				
			} else {
				//BLog.e("SEM", "account is null");
			}
		} else {
			//BLog.e("SEM", "from email is null");
		}
		
		//email.setLong(Email., value);
		//email = emsi.addEmail(email);

	}

	

}
