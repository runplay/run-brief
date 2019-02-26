package run.brief;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import run.brief.b.BRefreshable;
import run.brief.beans.Brief;
import run.brief.beans.BriefSend;
import run.brief.beans.BriefSettings;
import run.brief.service.BriefService;
import run.brief.b.B;
import run.brief.b.State;


public class BriefSendDialog extends Dialog {
	private Dialog thisDialog;
	private BriefSend usebriefsend;
	private Brief brief;
	private Activity activity;
	
	private LinearLayout preview;
	
	public static boolean shouldRefresh=false;
	private BRefreshable refreshFragment;
	public BriefSend getUsenote() {
		return usebriefsend;
	}
	public BriefSendDialog(Activity activity,Brief brief, BriefSend briefSend, BRefreshable refreshFragment) {
		super(activity);
		this.refreshFragment=refreshFragment;
		this.brief=brief;
		this.usebriefsend = briefSend;
		
		this.activity=activity;
		this.setContentView(R.layout.brief_send_dialog);
		
		
		this.setTitle(B.getStyledWithTypeFaceName(activity, activity.getResources().getString(R.string.bsend_menu_options), State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE), 1));
		Button bim = (Button) this.findViewById(R.id.dialog_cancel);
		bim.setOnClickListener(onCloseClick);

		Button del = (Button) this.findViewById(R.id.bsend_dialog_delete);
		del.setOnClickListener(onDeleteClick);

        Button retry = (Button) this.findViewById(R.id.bsend_dialog_retry);
        retry.setOnClickListener(onRetryClick);

        TextView msg = (TextView) this.findViewById(R.id.bsend_status);

        B.addStyle(retry);
        B.addStyle(del);
        B.addStyleBold(bim);
        B.addStyle(msg,B.FONT_SMALL);



        if(briefSend.getInt(BriefSend.INT_STATUS)==BriefSend.STATUS_CONFIRM) {
            msg.setText(activity.getString(R.string.brief_sent_confirming));
        } else if(briefSend.getInt(BriefSend.INT_ATTEMPTS)>= BriefService.MAX_MEDIUM_SEND_ATTEMPT_AT) {
            if(briefSend.getInt(BriefSend.INT_BRIEF_WITH)==Brief.WITH_EMAIL) {
                msg.setText(activity.getString(R.string.brief_failed_send_email));
            } else {
                msg.setText(activity.getString(R.string.brief_failed_send));
            }
            msg.setTextColor(activity.getResources().getColor(R.color.red));

            retry.setVisibility(View.VISIBLE);
        } else {
            msg.setTextColor(activity.getResources().getColor(R.color.green));
            msg.setText(activity.getString(R.string.brief_sending));
            retry.setVisibility(View.GONE);
        }


		thisDialog=this;
		this.setOnDismissListener(onDismiss);
		
		preview = (LinearLayout) this.findViewById(R.id.bsend_show_brief);
		preview.addView(BriefManager.getView(activity, null, brief));
		//Button copy = (Button) this.findViewById(R.id.dialog_copy);
		//copy.setOnClickListener(onCopyClick);
		//context.getMenuInflater().inflate(R.menu.notes_home_popup, popupMenu.getMenu());
	}

	public Button.OnClickListener onDeleteClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			BriefSendDb.remove(usebriefsend);
			shouldRefresh=true;
			BriefManager.setDirty(BriefManager.IS_DIRTY_SEND_ITEMS);
			thisDialog.dismiss();
		}
	};
    public Button.OnClickListener onRetryClick = new Button.OnClickListener() {
        @Override
        public void onClick(View view) {
            usebriefsend.setInt(BriefSend.INT_ATTEMPTS,0);
            BriefSendDb.update(usebriefsend);
            BriefService.sendServiceGo(activity);
            shouldRefresh=true;
            BriefManager.setDirty(BriefManager.IS_DIRTY_SEND_ITEMS);
            thisDialog.dismiss();
        }
    };
	public OnDismissListener onDismiss = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface intf) {
			//BLog.e("DISMISS", "Called");
			//if(BriefSendDialog.shouldRefresh)
				refreshFragment.refreshData();
		}
	};
	public Button.OnClickListener onCloseClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			shouldRefresh=false;
			thisDialog.dismiss();
		}
	};

}
