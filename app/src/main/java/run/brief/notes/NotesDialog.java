package run.brief.notes;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.B;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.BriefSettings;
import run.brief.beans.Email;
import run.brief.beans.Note;
import run.brief.email.EmailSendFragment;
import run.brief.settings.AccountsDb;
import run.brief.util.BriefActivityManager;
import run.brief.util.Cal;
import run.brief.util.json.JSONArray;


public class NotesDialog extends Dialog {
	private Dialog thisDialog;
	private Note usenote;
	private Activity activity;
	public static boolean shouldRefresh=false;
	private BRefreshable refreshFragment;
	public Note getUsenote() {
		return usenote;
	}
	public NotesDialog(Activity activity,Note usenote, BRefreshable refreshFragment) {
		super(activity);
		this.refreshFragment=refreshFragment;
		
		this.usenote = usenote;
		
		this.activity=activity;

		this.setContentView(R.layout.notes_dialog);

        TextView dialogTitle = (TextView) this.findViewById(android.R.id.title);
        dialogTitle.setTypeface(B.getTypeFace());
        dialogTitle.setText(B.getStyledWithTypeFaceName(activity, activity.getResources().getString(R.string.note_menu_options),State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE),1.2f));


		Button bim = (Button) this.findViewById(R.id.dialog_cancel);
		bim.setOnClickListener(onCloseClick);
		Button eim = (Button) this.findViewById(R.id.note_dialog_edit_note);
		eim.setOnClickListener(onEditClick);
		Button del = (Button) this.findViewById(R.id.note_dialog_delete);
		del.setOnClickListener(onDeleteClick);
		Button email = (Button) this.findViewById(R.id.note_dialog_send_email);   
		email.setOnClickListener(onSendEmailClick);
		if(!AccountsDb.hasEmailAccounts()) {
			email.setVisibility(View.GONE);
		}
		Button share = (Button) this.findViewById(R.id.note_dialog_share);
		share.setOnClickListener(onShareClick);

		thisDialog=this;
		this.setOnDismissListener(onDismiss);
		
		Button copy = (Button) this.findViewById(R.id.dialog_copy);
		copy.setOnClickListener(onCopyClick);

        B.addStyle(new TextView[]{copy, email, del, eim, bim,share});
		//context.getMenuInflater().inflate(R.menu.notes_home_popup, popupMenu.getMenu());
	}
	public Button.OnClickListener onSendEmailClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			
			Email em = new Email();
			em.setString(Email.STRING_MESSAGE, usenote.getString(Note.STRING_TEXT));
			if(usenote.has(Note.JSONARRAY_FILES)) {
				JSONArray files = usenote.getJSONArray(Note.JSONARRAY_FILES);
				if(files!=null && files.length()>0) {
					for(int i =0; i<files.length(); i++) {
						em.addAttachment(files.getString(i));
					}
				}
			}
			//State.addToState(State.SECTION_EMAIL_NEW, new StateObject(StateObject.INT_FORCE_NEW,Integer.valueOf(1)));
			State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_BJSON_OBJECT,em.toString()));
			Bgo.openFragmentBackStack(activity, EmailSendFragment.class);
			shouldRefresh=false;
			
			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onShareClick = new Button.OnClickListener() {
		@Override
		public void onClick(View view) {

			String share=usenote.getString(Note.STRING_TEXT);
			String file=null;
			if(usenote.has(Note.JSONARRAY_FILES)) {
				JSONArray files = usenote.getJSONArray(Note.JSONARRAY_FILES);
				if(files!=null && files.length()>0) {
					file=files.getString(0);
				}
			}
			//if(file!=null) {
			//	BriefActivityManager.shareExternal(activity,share,file);
			//} else {
				BriefActivityManager.shareExternal(activity,share);
			//}

			shouldRefresh=false;

			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onDeleteClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			NotesDb.remove(usenote);
			shouldRefresh=true;
			BriefManager.setDirty(BriefManager.IS_DIRTY_NOTES);
			thisDialog.dismiss();
		}
	};
	public OnDismissListener onDismiss = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface intf) {
			//BLog.e("DISMISS", "Called");
			if(NotesDialog.shouldRefresh)
				refreshFragment.refresh();
		}
	};
	public Button.OnClickListener onCloseClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			shouldRefresh=false;
			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onEditClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			State.clearStateObjects(State.SECTION_NOTES_ITEM);
			StateObject sob=new StateObject(StateObject.STRING_USE_DATABASE_ID,usenote.getInt(Note.INT_ID)+"");
			State.addToState(State.SECTION_NOTES_ITEM,sob);
			
		    Bgo.openFragmentBackStack(activity, NotesEditFragment.class);
		    shouldRefresh=false;
		    thisDialog.dismiss();
		    
		}
		
	};
	public Button.OnClickListener onCopyClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {
			
			StringBuilder sb = new StringBuilder((new Cal(usenote.getLong(Note.LONG_DATE_CREATED)).getDatabaseDate()));
		    sb.append("\n");
		    sb.append(usenote.getString(Note.STRING_TEXT));
		    sb.append("\n\n");
		    Device.copyToClipboard(activity, sb.toString());
			shouldRefresh=false;
		    thisDialog.dismiss();
		    
		}
		
	};
}
