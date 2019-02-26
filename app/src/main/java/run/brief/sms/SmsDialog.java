package run.brief.sms;

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
import run.brief.beans.BriefSettings;
import run.brief.beans.Note;
import run.brief.beans.SmsMsg;
import run.brief.notes.NotesDb;
import run.brief.notes.NotesEditFragment;
import run.brief.util.Cal;


public class SmsDialog extends Dialog {
	private Dialog thisDialog;
	private SmsMsg usesms;
	//private Account account;
	private Activity activity;
	//private SmsMsg smsmsg;
	private BRefreshable refreshFragment;
	public static boolean shouldRefresh=false;
	public SmsMsg getUseSms() {
		return usesms;
	}
	public SmsDialog(Activity activity, SmsMsg smsmsg, BRefreshable refreshFragment) {
		super(activity);
		
		this.refreshFragment=refreshFragment;
		
		//this.account=acc;
		//this.dbindex=dbIndex;
		this.usesms=smsmsg;
		this.activity=activity;
		//EmailServiceInstance emsi = EmailService.getService(activity, account);
		//this.useemail = emsi.getEmail(dbIndex);
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
		this.setContentView(R.layout.sms_dialog);
        //((TextView) this.findViewById(R.id.text)).setTypeface(B.getTypeFaceBold());
		//this.setTitle(B.getStyledWithTypeFaceName(activity, activity.getResources().getString(R.string.sms_menu_options),State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE),1.2f));

        TextView dialogTitle = (TextView) this.findViewById(android.R.id.title);
        dialogTitle.setTypeface(B.getTypeFace());
        dialogTitle.setText(B.getStyledWithTypeFaceName(activity, activity.getResources().getString(R.string.sms_menu_options), State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE), 1.2f));

        Button bim = (Button) this.findViewById(R.id.dialog_cancel);
		bim.setOnClickListener(onCloseClick);
		Button eim = (Button) this.findViewById(R.id.sms_dialog_reply);
		eim.setOnClickListener(onReplyClick);
		Button del = (Button) this.findViewById(R.id.sms_dialog_delete);
        //del.setVisibility(View.GONE);
        if(SmsFunctions.isDefaultSmsAppForDevice(activity)) {
            del.setVisibility(View.VISIBLE);
            del.setOnClickListener(onDeleteClick);
        } else {
            del.setVisibility(View.GONE);
        }

		Button email = (Button) this.findViewById(R.id.sms_dialog_save_note);   
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
			StringBuilder sb = new StringBuilder("> "+usesms.getMessageNumber());
			sb.append("\n");
			sb.append(usesms.getMessageDate().getDatabaseDate());
			sb.append("\n\n");
			sb.append(usesms.getMessageContent());
			sb.append("\n");

			note.setString(Note.STRING_TEXT, sb.toString());
			note.setLong(Note.LONG_DATE_CREATED, Cal.getUnixTime());
			long id=NotesDb.add(note);
			//em.setString(Email.STRING_MESSAGE, useemail.getString(Note.STRING_TEXT));
			//String from = 
			//em.setString(Email.STRING_TO, em.getString(Email.STRING_FROM));
			State.addToState(State.SECTION_NOTES_ITEM,new StateObject(StateObject.INT_USE_SELECTED_INDEX,id));
			Bgo.openFragmentBackStack(activity, NotesEditFragment.class);

			BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
			shouldRefresh=false;
			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onDeleteClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			SmsDb.deleteMessage(activity, usesms);
            //SmsDb.reload(activity);
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
			
			State.clearStateObjects(State.SECTION_SMS_SEND);
			StateObject sob = new StateObject(StateObject.STRING_USE_DATABASE_ID,usesms.getId());
			State.addToState(State.SECTION_SMS_SEND,sob);
			//Bgo.openFragmentBackStackAnimate(activity, new SmsSendFragment());
			
		    Bgo.openFragmentBackStack(activity, SmsSendFragment.class);
		    shouldRefresh=false;
		    thisDialog.dismiss();
		}
		
	};
	public Button.OnClickListener onCopyClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			StringBuilder sb = new StringBuilder(usesms.getMessageDate().getDatabaseDate());
		    sb.append("\n");
		    sb.append(usesms.getMessageNumber());
		    sb.append("\n\n");
		    sb.append(usesms.getMessageContent());
			
		    Device.copyToClipboard(activity, sb.toString());
			shouldRefresh=false;
		    thisDialog.dismiss();
		    
		}
		
	};
}
