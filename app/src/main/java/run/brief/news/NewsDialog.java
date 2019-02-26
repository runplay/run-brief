package run.brief.news;

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
import run.brief.beans.Email;
import run.brief.beans.RssItem;
import run.brief.email.EmailSendFragment;
import run.brief.settings.AccountsDb;
import run.brief.sms.SmsSendFragment;
import run.brief.util.BriefActivityManager;
import run.brief.util.Cal;
import run.brief.util.Files;
import run.brief.util.Sf;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;


public class NewsDialog extends Dialog {
	private Dialog thisDialog;
	private RssItem usenews;
	private Activity activity;
	public static boolean shouldRefresh=false;
	private BRefreshable refreshFragment;
	public RssItem getUseNews() {
		return usenews;
	}
	public NewsDialog(Activity activity,RssItem usenews, BRefreshable refreshFragment) {
		super(activity);
		this.refreshFragment=refreshFragment;

		this.usenews = usenews;

		this.activity=activity;
		this.setContentView(R.layout.news_dialog);
		this.setTitle(activity.getResources().getString(R.string.news_menu_options));
		Button bim = (Button) this.findViewById(R.id.dialog_cancel);
		bim.setOnClickListener(onCloseClick);
		Button del = (Button) this.findViewById(R.id.news_dialog_delete);
		del.setOnClickListener(onDeleteClick);
		Button email = (Button) this.findViewById(R.id.news_dialog_send_email);
		email.setOnClickListener(onSendEmailClick);
		if(!AccountsDb.hasEmailAccounts()) {
			email.setVisibility(View.GONE);
		}

		Button sms = (Button) this.findViewById(R.id.news_dialog_send_sms);
		sms.setOnClickListener(onSendSmsClick);
		if(!Device.hasPhone()) {
			sms.setVisibility(View.GONE);
		}
		Button share = (Button) this.findViewById(R.id.news_dialog_share);
		share.setOnClickListener(onShareClick);

		thisDialog=this;
		this.setOnDismissListener(onDismiss);

		Button copy = (Button) this.findViewById(R.id.dialog_copy);
		copy.setOnClickListener(onCopyClick);

		B.addStyle(new TextView[]{copy, email, del, bim,share});
		//context.getMenuInflater().inflate(R.menu.notes_home_popup, popupMenu.getMenu());
	}
	public Button.OnClickListener onSendSmsClick = new Button.OnClickListener() {
		@Override
		public void onClick(View view) {

			JSONObject messaged=new JSONObject();
			messaged.put("text", Sf.restrictLength(usenews.getString(RssItem.STRING_HEAD)+"\n"+usenews.getString(RssItem.STRING_URL)+"\n"+usenews.getString(RssItem.STRING_TEXT),140));
			//em.setString(Email.STRING_SUBJECT, usenews.getString(RssItem.STRING_HEAD));
			//em.setString(Email.STRING_MESSAGE, usenews.getString(RssItem.STRING_HEAD)+"\n"+usenews.getString(RssItem.STRING_TEXT)+"\n\n"+usenews.getString(RssItem.STRING_URL));
			//State.clearStateObjects(State.SECTION_EMAIL_NEW);
			//if(usenews.getString(RssItem.STRING_IMG_URL).contains("http:"))
				//em.addAttachment(Files.createFilePathFromUrl(usenews.getString(RssItem.STRING_IMG_URL)));
			//State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.INT_FORCE_NEW,Integer.valueOf(1)));
			State.addToState(State.SECTION_SMS_SEND,new StateObject(StateObject.STRING_BJSON_OBJECT,messaged.toString()));
			Bgo.openFragmentBackStack(activity, SmsSendFragment.class);
			shouldRefresh=false;

			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onShareClick = new Button.OnClickListener() {
		@Override
		public void onClick(View view) {

			String share=usenews.getString(RssItem.STRING_HEAD)+"\n"+usenews.getString(RssItem.STRING_URL)+"\n"+usenews.getString(RssItem.STRING_TEXT);
			BriefActivityManager.shareExternal(activity,share);
			shouldRefresh=false;

			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onSendEmailClick = new Button.OnClickListener() {
		@Override
		public void onClick(View view) {

			Email em = new Email();
			em.setString(Email.STRING_SUBJECT, usenews.getString(RssItem.STRING_HEAD));
			em.setString(Email.STRING_MESSAGE, usenews.getString(RssItem.STRING_HEAD)+"\n"+usenews.getString(RssItem.STRING_TEXT)+"\n\n"+usenews.getString(RssItem.STRING_URL));
			State.clearStateObjects(State.SECTION_EMAIL_NEW);
			if(usenews.getString(RssItem.STRING_IMG_URL).contains("http:"))
				em.addAttachment(Files.createFilePathFromUrl(usenews.getString(RssItem.STRING_IMG_URL)));
			//State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.INT_FORCE_NEW,Integer.valueOf(1)));
			State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_BJSON_OBJECT,em.toString()));
			Bgo.openFragmentBackStack(activity, EmailSendFragment.class);
			shouldRefresh=false;

			thisDialog.dismiss();
		}
	};
	public Button.OnClickListener onDeleteClick = new Button.OnClickListener() {
		@Override
		public void onClick(View view) {

			NewsItemsDb.remove(usenews);
			shouldRefresh=true;
			BriefManager.setDirty(BriefManager.IS_DIRTY_NEWS);
			thisDialog.dismiss();
		}
	};
	public OnDismissListener onDismiss = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface intf) {
			BLog.e("DISMISS", "Called");
			Bgo.refreshCurrentFragment(activity);
		}
	};
	public Button.OnClickListener onCloseClick = new Button.OnClickListener() {
		@Override
		public void onClick(View view) {
			shouldRefresh=false;
			thisDialog.dismiss();
		}
	};

	public Button.OnClickListener onCopyClick = new Button.OnClickListener() {
		@Override
		public void onClick(View v) {

			StringBuilder sb = new StringBuilder((new Cal(usenews.getLong(RssItem.LONG_DATE)).getDatabaseDate()));
			sb.append("\n");
			sb.append(usenews.getString(RssItem.STRING_URL));
			sb.append("\n\n");
			sb.append(usenews.getString(RssItem.STRING_HEAD));
			sb.append("\n");
			sb.append(usenews.getString(RssItem.STRING_TEXT));
			sb.append("\n\n");
			Device.copyToClipboard(activity, sb.toString());
			shouldRefresh=false;
			thisDialog.dismiss();

		}

	};
}
