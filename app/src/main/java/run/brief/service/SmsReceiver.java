package run.brief.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsMessage;

import java.util.ArrayList;
import java.util.HashMap;

import run.brief.BriefManager;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.beans.Brief;
import run.brief.beans.BriefSettings;
import run.brief.beans.SmsMsg;
import run.brief.sms.SmsDb;
import run.brief.sms.SmsFunctions;
import run.brief.util.Cal;


public final class SmsReceiver extends BroadcastReceiver {

    private static final ArrayList<SmsMsg> liveSms=new ArrayList<SmsMsg>();
    private static Context context;

    public static final String ACTION_SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String ACTION_SMS_SENT = "android.provider.Telephony.SMS_SENT";
    public static final String ACTION_SMS_DELIVER = "android.provider.Telephony.SMS_DELIVER";
    private Handler delayedRefresh=new Handler();
    private static Handler newMessageCompile=new Handler();
    //private Context context;

    private static final String DEBUG_TAG="MSgTelReceiver";



    @Override
    public void onReceive(Context context, Intent intent) {

            this.context=context;

            BriefService.ensureStartups(context);

            String action = intent.getAction();
            String type = intent.getType();

            //BLog.e("BriefService - OnSmsReceiver","action: "+action);

            if(action.equals(ACTION_SMS_DELIVER)) {
                if(State.getSettings().getBoolean(BriefSettings.BOOL_OVERRIDE_SMS_PROVIDER)) {
                    Bundle bundle = intent.getExtras();
                    SmsMessage[] msgs = null;
                    if (bundle != null) {
                        //BLog.e("BriefService - OnSmsReceiver", "onReceive().Bundle not null");
                        //---retrieve the SMS message received---
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        if(pdus!=null) {
                            msgs = new SmsMessage[pdus.length];
                            for (int i = 0; i < msgs.length; i++) {

                                msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                                SmsMsg msg = new SmsMsg();
                                //msgs[i].ge
                                msg.setMessageNumber(msgs[i].getOriginatingAddress());
                                msg.setMessageContent(msgs[i].getMessageBody().toString());
                                msg.setMessageDate(new Cal(msgs[i].getTimestampMillis()));
                                msg.setMine(false);
                                msg.setServiceCenter(msgs[i].getServiceCenterAddress());
                                msg.setStatus(msgs[i].getStatus());
                                msg.setProtocol(msgs[i].getProtocolIdentifier());
                                msg.setSubject(msgs[i].getDisplayOriginatingAddress());


                                liveSms.add(msg);

                                newMessageCompile.removeCallbacks(doAddLive);
                                newMessageCompile.postDelayed(doAddLive,1200);


                                //Uri didAdd = SmsFunctions.addToSmsReceived(context, msg);
/*
                                StringBuilder head = new StringBuilder("Sms: ");
                                Person p = ContactsDb.getWithTelephoneConcatEnd(context, msg.getMessageNumber());
                                if (p != null) {
                                    head.append(p.getString(Person.STRING_NAME));
                                } else {
                                    p = ContactsDb.getWithTelephoneConcatEnd(context, msg.getMessageNumber());
                                    if (p != null)
                                        head.append(p.getString(Person.STRING_NAME));
                                    else
                                        head.append(msg.getMessageNumber());
                                }
                                */
                                //Brief brief=new Brief(context,msg,0);
                                //BriefService.notify(context,  brief);
                                //BLog.e("DIDADD", "" + didAdd.toString());

                            }
                        }
                        //SmsDb.refresh(context);
                        //BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
                        //Bgo.tryRefreshCurrentFragment();
                        //this.abortBroadcast();

                    }
                } else {
                    delayedRefresh.postDelayed(refresher,1000);

                    //BLog.e("BriefService - OnSmsReceiver","DO NOT OVERIDE");
                }
            } else  if(action.equals(ACTION_SMS_SENT)){
                //do something with the sended sms
                delayedRefresh.postDelayed(refresher, 1000);

                //BLog.e("BriefService - OnSmsReceiver","----ACTION_SMS_SENT");
            } else  if(action.equals(ACTION_SMS_RECEIVED)){
                //do something with the sended sms
                delayedRefresh.postDelayed(refresher, 1000);

                //BLog.e("BriefService - OnSmsReceiver","----ACTION_SMS_RECEIVED");
            }

        //BriefService.init(context);
    }

    private Runnable doAddLive = new Runnable() {
        @Override
        public void run() {
            synchronized (this) {
                if (!liveSms.isEmpty()) {
                    if (liveSms.size() == 1) {
                        SmsFunctions.addToSmsReceived(context, liveSms.get(0));

                    } else {
                        HashMap<String, SmsMsg> compile = new HashMap<String, SmsMsg>();
                        for (SmsMsg msg : liveSms) {
                            SmsMsg already = compile.get(msg.getMessageNumber());
                            if (already != null) {
                                already.setMessageContent(already.getMessageContent() + msg.getMessageContent());
                            } else {
                                compile.put(msg.getMessageNumber(),msg);
                            }
                        }
                        for(String numkey: compile.keySet()) {
                            SmsMsg msg=compile.get(numkey);
                            SmsFunctions.addToSmsReceived(context, msg);
                            Brief brief=new Brief(context,msg,0);
                            BriefService.notify(context,  brief);
                        }

                    }
                    liveSms.clear();
                    SmsDb.refresh(context);
                    BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
                    Bgo.tryRefreshCurrentFragment();


                }
            }
        }
    };

    private Runnable refresher = new Runnable() {
        @Override
        public void run() {
            SmsDb.refresh(context);
            BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
            Bgo.tryRefreshCurrentFragment();
        }
    };

}
