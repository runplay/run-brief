package run.brief.email;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
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
import run.brief.beans.Brief;
import run.brief.beans.BriefSettings;
import run.brief.beans.Email;
import run.brief.beans.PersonFull;
import run.brief.settings.AccountsDb;
import run.brief.util.CustomLinkMovementMethod;
import run.brief.util.Files;
import run.brief.util.Sf;
import run.brief.util.explore.FilePopListener;
import run.brief.util.explore.FilesAdapter;

public class EmailViewFragment extends BFragment implements BRefreshable {
	
	//private Handler contactsHandler = new Handler();
	private static ArrayList<PersonFull> contacts=null;
	//ImapService imap;

	private View view;
	private Activity activity;
	
	private TextView viewfrom;
	private TextView viewsubject;
	private TextView viewcontent;
	private MessageWebView viewcontenthtml;
	private LinearLayout erroronload;
	private LinearLayout showcontent;

    private  View web;
    private ImageView webWb;
    private ImageView webBw;
    private ImageView webSize;
	
	private Email useEmail;
	private Account useAccount;
	private EmailServiceInstance es;

    LinearLayout viewAttach;
    View viewAttachHolder;
	

	private long selectedAccid;
    private int TYPE_;
    private PopListner listen;
    private FilesAdapter adapter;
	//EmailService emails;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//this.setHasOptionsMenu(true);

		AccountsDb.init();
		activity=getActivity();
		view=inflater.inflate(R.layout.email_view,container, false);
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		
		viewfrom = (TextView) view.findViewById(R.id.email_content_from);
		viewsubject = (TextView) view.findViewById(R.id.email_content_subject);
		viewcontent = (TextView) view.findViewById(R.id.email_content_text);

        viewcontent.setMovementMethod(CustomLinkMovementMethod.getInstance(activity));

		viewcontenthtml = (MessageWebView) view.findViewById(R.id.email_content_webview);


        viewAttach=(LinearLayout) view.findViewById(R.id.email_view_attachments);
        viewAttachHolder=view.findViewById(R.id.email_view_attachments_holder);
        viewAttachHolder.setVisibility(View.GONE);

        web= view.findViewById(R.id.email_web);
        webWb=(ImageView) view.findViewById(R.id.email_web_wb);
        webBw=(ImageView) view.findViewById(R.id.email_web_bw);
        //webSize=(ImageView) view.findViewById(R.id.email_web_size);


        webWb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BriefSettings bset=State.getSettings();
                bset.setInt(BriefSettings.INT_WEBVIEW_COLOR,MessageWebView.COLOR_WHITE_ON_BLACK);
                bset.save();
                String message= useEmail.getString(Email.STRING_MESSAGE);
                viewcontenthtml.configure();
                refresh();
                //viewcontenthtml.setWebColor(MessageWebView.COLOR_WHITE_ON_BLACK);
                //viewcontenthtml.setText(message, "text/html");
                webBw.setAlpha(0.3f);
                webWb.setAlpha(1f);
                //refresh();
            }
        });
        webBw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BriefSettings bset=State.getSettings();
                bset.setInt(BriefSettings.INT_WEBVIEW_COLOR,MessageWebView.COLOR_BLACK_ON_WHITE);
                bset.save();
                String message= useEmail.getString(Email.STRING_MESSAGE);
                //message = Sf.htmlWrap(message);
               // viewcontenthtml.setWebColor(MessageWebView.COLOR_BLACK_ON_WHITE);
                //viewcontenthtml.setText(message, "text/html");
                webBw.setAlpha(1f);
                webWb.setAlpha(0.3f);
                viewcontenthtml.configure();

                refresh();
            }
        });
        B.addStyle(new TextView[]{viewfrom,viewsubject,viewcontent});

        if(State.getSettings().getInt(BriefSettings.INT_WEBVIEW_COLOR)==MessageWebView.COLOR_BLACK_ON_WHITE) {
            //viewcontenthtml.setWebColor(MessageWebView.COLOR_BLACK_ON_WHITE);
            webBw.setAlpha(1f);
            webWb.setAlpha(0.3f);
        } else {
            //viewcontenthtml.setWebColor(MessageWebView.COLOR_WHITE_ON_BLACK);
            webBw.setAlpha(0.3f);
            webWb.setAlpha(1f);
        }

		refresh();
	}
	public void refreshData() {
		
	}
    @Override
    public void onPause() {
        super.onPause();
        if(useEmail !=null) {
            State.addToState(State.SECTION_EMAIL_VIEW, new StateObject(StateObject.STRING_USE_DATABASE_ID, useEmail.getLong(Email.LONG_ID) + ""));
            State.addToState(State.SECTION_EMAIL_VIEW, new StateObject(StateObject.LONG_USE_ACCOUNT_ID, selectedAccid));
        }

    }
	public void refresh() {

		BriefManager.clearController(activity);
		if(activity!=null) {
            /*
            amb = new ActionModeBack(activity, activity.getResources().getString(R.string.email_view)
                    ,R.menu.email_view
                    , new ActionModeCallback() {
                @Override
                public void onActionMenuItem(ActionMode mode, MenuItem item) {
                    onOptionsItemSelected(item);
                }
            });
            */
            ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.email_view),R.menu.email_view,R.color.actionbar_email);

			

			State.setCurrentSection(State.SECTION_EMAIL_VIEW);
            if(State.hasStateObject(State.SECTION_EMAIL_VIEW,StateObject.STRING_USE_DATABASE_ID)) {
                String aid= State.getStateObjectString(State.SECTION_EMAIL_VIEW, StateObject.STRING_USE_DATABASE_ID);
                selectedAccid= State.getStateObjectLong(State.SECTION_EMAIL_VIEW,StateObject.LONG_USE_ACCOUNT_ID);
                useAccount = AccountsDb.getAccountById(selectedAccid);
                if(useAccount !=null) {
                    es = EmailService.getService(activity, useAccount);

                    if (es != null) {
                        useEmail = es.getEmailById(Sf.toLong(aid));
                        if(useEmail.getString(Email.STRING_FROM).equals(useAccount.getString(Account.STRING_EMAIL_ADDRESS))) {
                            TYPE_= Brief.TYPE_OUT;
                        } else {
                            TYPE_=Brief.TYPE_IN;
                        }
                    }
                }


            } else if(State.hasStateObject(State.SECTION_EMAIL_VIEW,StateObject.INT_USE_SELECTED_INDEX)) {
                    int selectedIndex= State.getStateObjectInt(State.SECTION_EMAIL_VIEW,StateObject.INT_USE_SELECTED_INDEX);
                    selectedAccid= State.getStateObjectLong(State.SECTION_EMAIL_VIEW,StateObject.LONG_USE_ACCOUNT_ID);
                    useAccount = AccountsDb.getAccountById(selectedAccid);

                    if(useAccount !=null) {
                        es = EmailService.getService(activity, useAccount);

                        if(es!=null) {
                            useEmail = es.getEmail(selectedIndex);
                            if(useEmail.getString(Email.STRING_FROM).equals(useAccount.getString(Account.STRING_EMAIL_ADDRESS))) {
                                TYPE_= Brief.TYPE_OUT;
                            } else {
                                TYPE_=Brief.TYPE_IN;
                            }
                        } else {
                            //BLog.e("EM_V", "No email service");
                        }
                    } else {
                        //BLog.e("EM_V", "No account found id: "+accid);
                    }

            }
			State.clearStateObjects(State.SECTION_EMAIL_VIEW);
			
			erroronload = (LinearLayout) view.findViewById(R.id.email_error_noemail);
			showcontent = (LinearLayout) view.findViewById(R.id.email_content);
			
			if(useEmail !=null) {
				//BLog.e("EM_V", "hello ok");
				if(useEmail.getInt(Email.INT_COLLECTED)==0) {
					//loadEmail();
				}
				
				showcontent.setVisibility(View.VISIBLE);
				erroronload.setVisibility(View.GONE);
				

				//buttonforward = (bButton) view.findViewById(R.id.btn_email_forward);
				//buttonreply = (bButton) view.findViewById(R.id.btn_email_reply);
				//buttonreply.setOnClickListener(replyListner);
				if(useEmail.getBoolean(Email.BOOL_IS_MINE_NO_SAVE)) {
                    viewfrom.setCompoundDrawablesWithIntrinsicBounds(R.drawable.email_to,0,0,0);
                    viewfrom.setText(useEmail.getString(Email.STRING_TO));
                } else {
                    viewfrom.setCompoundDrawablesWithIntrinsicBounds(R.drawable.email_from,0,0,0);
                    viewfrom.setText(useEmail.getString(Email.STRING_FROM));
                }

				viewsubject.setText(useEmail.getString(Email.STRING_SUBJECT));
				
				
				String message= useEmail.getString(Email.STRING_MESSAGE);
				if(message!=null) {
				
					if(Sf.isHtml(message)) {

						message = Sf.htmlWrap(message);
						
						viewcontenthtml.getSettings().setJavaScriptEnabled(false);
						//viewcontenthtml.loadDataWithBaseURL("", message, "text/html", "UTF-8", "");
						viewcontenthtml.setText(message, "text/html");
						web.setVisibility(View.VISIBLE);
						viewcontent.setVisibility(View.GONE);
					} else {
						viewcontent.setText(message);
						viewcontent.setVisibility(View.VISIBLE);
						web.setVisibility(View.GONE);
					}
				}

                showAttachedFiles();
			} else {
				//BLog.e("EM_V", "hello bad");
				showcontent.setVisibility(View.GONE);
				erroronload.setVisibility(View.VISIBLE);
                /*
                amb = new ActionModeBack(activity, activity.getResources().getString(R.string.email_view)
                        ,R.menu.basic
                        , new ActionModeCallback() {
                    @Override
                    public void onActionMenuItem(ActionMode mode, MenuItem item) {
                        onOptionsItemSelected(item);
                    }
                });
                */
                ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.email_view),R.menu.basic,R.color.actionbar_email);

			}
		}
	}
    public void showAttachedFiles() {


        String attfolder= Files.HOME_PATH_FILES+File.separator+Files.FOLDER_EMAIL_ATTACHMENTS+File.separator;
        List<String> att = new ArrayList<String>();
        for(String attach: useEmail.getAttachments()) {
            if(attach.length()>0) {
                if(attach.startsWith("/"))
                    att.add(attach);
                else
                    att.add(attfolder + attach);

            }
        }
        listen=new PopListner();

        adapter=new FilesAdapter(getActivity(),att,FilesAdapter.TYPE_VIEW,listen);

        //attachfiles.setAdapter(adapter);
        if(adapter.getCount()>0) {
            viewAttachHolder.setVisibility(View.VISIBLE);
            viewAttach.removeAllViews();
            for(int i=0; i<adapter.getCount(); i++) {
                View tmpv = adapter.getView(i,null,(ViewGroup) viewAttach);

                viewAttach.addView(tmpv);
            }
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

                ArrayList<String> files=useEmail.getAttachments();
                for(int i=0; i<files.size(); i++) {
                    if(files.get(i).equals(adapter.selectedFile)) {
                        files.remove(i);
                        break;
                    }
                }

                useEmail.setString(Email.STRING_ATTACHMENTS, "");
                for(String file: files) {
                    useEmail.addAttachment(file);
                }
                showAttachedFiles();

            }
            return false;
        }
    }
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//BLog.e("OPTIONS", "onCreateOptionsMenu at new emai view");
	}
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_email_reply:
            State.clearStateObjects(State.SECTION_EMAIL_NEW);
            State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.INT_FORCE_NEW,1));
			State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.LONG_USE_ACCOUNT_ID, useAccount.getLong(Account.LONG_ID)));
			State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_USE_DATABASE_ID,""+useEmail.getLong(Email.LONG_ID)));
			Bgo.openFragmentBackStack(activity,EmailSendFragment.class);
		    break;
		}	
		return false;
	}


}
