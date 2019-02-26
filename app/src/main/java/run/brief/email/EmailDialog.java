package run.brief.email;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import run.brief.b.B;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.BriefManager;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.BriefSettings;
import run.brief.beans.Email;
import run.brief.beans.Note;
import run.brief.notes.NotesDb;
import run.brief.notes.NotesEditFragment;
import run.brief.util.Cal;


public class EmailDialog extends Dialog {
	private Dialog thisDialog;
	private Email useemail;
	private Account account;
	private Activity activity;
	int dbindex;
	private BRefreshable refreshFragment;
	public static boolean shouldRefresh=false;
	public Email getUseemail() {
		return useemail;
	}
	public EmailDialog(Activity activity, Account acc, int dbIndex, BRefreshable refreshFragment) {
		super(activity);
		
		this.refreshFragment=refreshFragment;
		
		this.account=acc;
		this.dbindex=dbIndex;
		this.activity=activity;
		EmailServiceInstance emsi = EmailService.getService(activity, account);
		this.useemail = emsi.getEmail(dbIndex);
		/*try {
		    Field[] fields = this.getClass().getDeclaredFields();
		    for (Field field : fields) {
		        if ("mPopup".equals(field.getName())) {
		            field.setAccessible(true);
		            Object menuPopupHelper = field.get(this);
		            Class<?> classPopupHelper = Class.forName(menuPopupHelper
		                    .getClass().getName());
		            Method setForceIcons = classPopupHelper.getMethod(
		                    "setForceShowIcon", boolean.class);
		            setForceIcons.invoke(menuPopupHelper, true);
		            break;
		        }
		    }
		} catch (Exception e) {
		    //BLog.e("ERR", "Problem create Dialog");
		}
		*/
		this.setContentView(R.layout.email_dialog);
        TextView dialogTitle = (TextView) this.findViewById(android.R.id.title);
        dialogTitle.setTypeface(B.getTypeFace());
        dialogTitle.setText(B.getStyledWithTypeFaceName(activity, activity.getResources().getString(R.string.email_menu_options),State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE),1.2f));
        //((TextView) this.findViewById(R.id.text)).setTypeface(B.getTypeFaceBold());
        //this.setTitle(B.getStyledWithTypeFaceName(activity, activity.getResources().getString(R.string.email_menu_options),State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE),1.2f));
		Button bim = (Button) this.findViewById(R.id.dialog_cancel);
		bim.setOnClickListener(onCloseClick);
		Button eim = (Button) this.findViewById(R.id.email_dialog_reply);
		eim.setOnClickListener(onReplyClick);
		Button del = (Button) this.findViewById(R.id.email_dialog_delete);
		del.setOnClickListener(onDeleteClick);
		Button email = (Button) this.findViewById(R.id.email_dialog_save_note);   
		email.setOnClickListener(onSaveNoteClick);
		thisDialog=this;
		this.setOnDismissListener(onDismiss);
		Button copy = (Button) this.findViewById(R.id.dialog_copy);
		copy.setOnClickListener(onCopyClick);

        B.addStyle(new TextView[]{copy, email, del, eim, bim});
		//context.getMenuInflater().inflate(R.menu.notes_home_popup, popupMenu.getMenu());
	}
	public OnDismissListener onDismiss = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface intf) {
			//BLog.e("DISMISS", "Called");
			if(shouldRefresh)
				refreshFragment.refresh();
		}
	};
	public Button.OnClickListener onSaveNoteClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			
			//Email em = useemail;
			Note note = new Note();
			StringBuilder sb = new StringBuilder("> "+useemail.getString(Email.STRING_FROM));
			sb.append("\n");
			sb.append(useemail.getString(Email.STRING_TO));
			sb.append("\n");
			sb.append((new Cal(useemail.getLong(Email.LONG_DATE)).getDatabaseDate()));
			sb.append("\n\n");
			sb.append(useemail.getString(Email.STRING_MESSAGE));
			sb.append("\n");
			if(useemail.has(Email.STRING_ATTACHMENTS)) {
				// do attachments
			}
			note.setString(Note.STRING_TEXT, sb.toString());
			note.setLong(Note.LONG_DATE_CREATED, Cal.getUnixTime());
			long id=NotesDb.add(note);
			//em.setString(Email.STRING_MESSAGE, useemail.getString(Note.STRING_TEXT));
			//String from = 
			//em.setString(Email.STRING_TO, em.getString(Email.STRING_FROM));
			State.clearStateObjects(State.SECTION_NOTES_ITEM);
			State.addToState(State.SECTION_NOTES_ITEM,new StateObject(StateObject.INT_USE_SELECTED_INDEX,id));
			Bgo.openFragmentBackStack(activity, NotesEditFragment.class);
			BriefManager.setDirty(BriefManager.IS_DIRTY_NOTES);
			shouldRefresh=false;
			thisDialog.dismiss();
			
		}
	};
	public Button.OnClickListener onDeleteClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			EmailServiceInstance emsi = EmailService.getService(activity, account);
			
			emsi.deleteEmail(useemail);
            BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);
			shouldRefresh=true;
			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onCloseClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onReplyClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			State.clearStateObjects(State.SECTION_EMAIL_NEW);
			///StateObject sob = new StateObject(StateObject.INT_USE_SELECTED_INDEX,position);
			StateObject soba = new StateObject(StateObject.LONG_USE_ACCOUNT_ID,account.getLong(Account.LONG_ID));
			
			
			State.addToState(State.SECTION_EMAIL_NEW,soba);
			StateObject sob=new StateObject(StateObject.INT_USE_SELECTED_INDEX,dbindex);
			State.addToState(State.SECTION_EMAIL_NEW,sob);
			
			StateObject sobi=new StateObject(StateObject.INT_FORCE_NEW,1);
			State.addToState(State.SECTION_EMAIL_NEW,sobi);
			
		    Bgo.openFragmentBackStack(activity, EmailSendFragment.class);
		    shouldRefresh=false;
		    thisDialog.dismiss();
		}
		
	};
	public Button.OnClickListener onCopyClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			StringBuilder sb = new StringBuilder(useemail.getLongCal(Email.LONG_DATE).getDatabaseDate());
		    sb.append("\nfrom: ");
		    sb.append(useemail.getString(Email.STRING_FROM));
		    sb.append("\nto: "+useemail.getString(Email.STRING_TO));
		    sb.append("\n\n"+useemail.getString(Email.STRING_SUBJECT));
		    sb.append("\n\n");
		    sb.append(useemail.getString(Email.STRING_MESSAGE));
			
		    Device.copyToClipboard(activity, sb.toString());
			shouldRefresh=false;
		    thisDialog.dismiss();
		    
		}
		
	};
}
