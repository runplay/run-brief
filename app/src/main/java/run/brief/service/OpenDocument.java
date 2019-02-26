package run.brief.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import run.brief.b.Bgo;
import run.brief.BriefManager;
import run.brief.util.log.BLog;
import run.brief.sms.SmsDb;


public class OpenDocument extends BroadcastReceiver {


    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String ACTION_SMS_SENT = "android.provider.Telephony.SMS_SENT";
    public static final String ACTION_SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER";
    private Handler delayedRefresh=new Handler();
    private Context context;

    private static final String DEBUG_TAG="MSgTelReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {

            this.context=context;


            String action = intent.getAction();
            String type = intent.getType();

            BLog.e("BriefService - OnSmsReceiver","action: "+action);

            if(action.equals(ACTION_SMS_DELIVER)) {

            } else  if(action.equals(ACTION_SMS_SENT)){
                //do something with the sended sms
                delayedRefresh.postDelayed(refresher, 500);

                BLog.e("BriefService - OnSmsReceiver","----SMS_SENT");
            } else  if(action.equals(ACTION_SMS_RECEIVED)){
                //do something with the sended sms
                delayedRefresh.postDelayed(refresher, 500);

                BLog.e("BriefService - OnSmsReceiver","----SMS_DELIVER");
            }

        //BriefService.init(context);
    }

    private Runnable refresher = new Runnable() {
        @Override
        public void run() {
            SmsDb.refresh(context);
            BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
            Bgo.tryRefreshCurrentFragment();
        }
    };

}
