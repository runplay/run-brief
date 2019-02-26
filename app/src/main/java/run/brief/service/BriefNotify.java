package run.brief.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import run.brief.b.Device;
import run.brief.Main;
import run.brief.R;
import run.brief.b.State;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.BriefSettings;
import run.brief.beans.Person;
import run.brief.contacts.ContactsDb;
import run.brief.news.NewsFiltersDb;
import run.brief.settings.AccountsDb;
import run.brief.settings.SettingsDb;
import run.brief.util.Cal;
import run.brief.util.Sf;
import run.brief.util.log.BLog;

public final class BriefNotify implements MediaPlayer.OnPreparedListener {

    private static final BriefNotify BN=new BriefNotify();
	private Context context;

	//String notifyText;
	String notifyHead;
    private NotificationCompat.Builder mBuilder;
    private int TYPE_ = R.id.brief_notify;
    private NotificationManager mNotificationManager;

    private Handler handler = new Handler();

    private List<NotifyBrief> briefs=new ArrayList<NotifyBrief>();

    private class LastNotify {
        int countEmail;
        int countSms;
        int countNews;
    }

    private class NotifyBrief {
        long date;
        boolean isSeen;
        Brief brief;
    }
    private LastNotify lastNotify;


    private List<NotifyBrief> getUnseen() {
        List<NotifyBrief> unseen=new ArrayList<NotifyBrief>();
        for(NotifyBrief bn: BN.briefs) {
            if(!bn.isSeen)
                unseen.add(bn);
        }
        return unseen;
    }


    public static void addNotifyFor(Context context, Brief brief) {
        BN.context=context;
        NotifyBrief nb = BN.new NotifyBrief();
        nb.date=Cal.getUnixTime();
        nb.brief=brief;
        BN.briefs.add(nb);
        BN.handler.removeCallbacks(BN.goRun);
        BN.handler.postDelayed(BN.goRun,10);

    }
    public static void addNotifyFor(Context context, Brief brief,boolean slowAlert) {
        BN.context=context;
        NotifyBrief nb = BN.new NotifyBrief();
        nb.date=Cal.getUnixTime();
        nb.brief=brief;
        BN.briefs.add(nb);
        BN.handler.removeCallbacks(BN.goRun);
        if(slowAlert)
            BN.handler.postDelayed(BN.goRun,2000);
        else
            BN.handler.postDelayed(BN.goRun,10);

    }
    public static void clearNotifications() {
        for(NotifyBrief nb: BN.briefs) {
            if(!nb.isSeen)
                nb.isSeen=true;
        }
        for (int i=BN.briefs.size()-1; i>=0; i--) {
            NotifyBrief bn = BN.briefs.get(i);
            if(bn.isSeen && bn.date<Cal.getUnixTime()-Cal.HOUR*4)
                BN.briefs.remove(i);
        }
        //BN.briefs=new ArrayList<NotifyBrief>();
        if(BN.mNotificationManager!=null)
            BN.mNotificationManager.cancelAll();
        BN.lastNotify=BN.new LastNotify();
    }



    private Runnable goRun = new Runnable() {
        @Override
        public void run() {

            if(BN.lastNotify==null)
                BN.lastNotify= BN.new LastNotify();
            String notifyHead=null;

            ContactsDb.init(context);
            NewsFiltersDb.init();
            //String notifyText=null;
            //String notifyFullText=null;
            int countEmail=0;
            int countSms=0;
            int countNews=0;


            Bitmap licon=null;
            StringBuilder summary=new StringBuilder();

            String emailStr = "✉ ";//BN.context.getString(R.string.label_email);
            String smsStr = "💬 "; // BN.context.getString(R.string.title_sms);

            if(BN.briefs.isEmpty())
                return;


            BN.mBuilder = new NotificationCompat.Builder(BN.context)
                    .setSmallIcon(R.drawable.i_notify).setAutoCancel(true);
            //.setLargeIcon()

            NotificationCompat.InboxStyle inboxStyle =
                    new NotificationCompat.InboxStyle();
            String[] events = new String[6];
            // Sets a title for the Inbox style big view

            List<String> lines=new ArrayList<String>();

            Intent intent = new Intent(BN.context, Main.class);

            ContactsDb.init(context);

            List<NotifyBrief> usebriefs=getUnseen();
            if(usebriefs.size()==1) {
                NotifyBrief bnotify=usebriefs.get(0);
                Person p=getPerson(bnotify.brief);
                if(p.getLong(Person.LONG_ID)>0)
                    licon=p.getThumbnail(BN.context);
                else
                    licon=BitmapFactory.decodeResource(context.getResources(), R.drawable.social_person_notify);


                if (bnotify.brief.getWITH_() == Brief.WITH_EMAIL) {
                    countEmail++;
                    notifyHead=emailStr+p.getString(Person.STRING_NAME);
                    intent.setData(Uri.parse("mailto:"+p.getMainEmail()));
                    Account acc= AccountsDb.getAccountById(bnotify.brief.getAccountId());
                    if(acc!=null)
                        summary.append(acc.getString(Account.STRING_EMAIL_ADDRESS));
                    String msg=bnotify.brief.getMessage();
                    if(msg.indexOf("\n")!=-1) {
                        String h = msg.substring(0,msg.indexOf("\n"));
                        String b = msg.substring(msg.indexOf("\n"),msg.length()-1);
                        lines.add(Sf.restrictLength(h,200));
                        lines.add(Sf.restrictLength(b,200));
                    } else {
                        lines.add(Sf.restrictLength(msg,200));
                    }

                } else if((bnotify.brief.getWITH_()==Brief.WITH_SMS)) {
                    countSms++;
                    intent.setData(Uri.parse("tel:"+p.getMainPhone()));
                    notifyHead=smsStr+p.getString(Person.STRING_NAME);
                    lines.add(Sf.restrictLength(bnotify.brief.getMessage(),200));
                    summary.append(smsStr);
                    summary.append(" ");
                    summary.append(Cal.getCal(bnotify.brief.getTimestamp()).getTimeHHMM());
                    //notifyText= Sf.restrictLength(brief.getMessage(), 20)+(brief.getMessage().length()>20?"...":"");
                } else if((bnotify.brief.getWITH_()==Brief.WITH_NEWS)) {
                    countNews++;
                    intent.setData(Uri.parse("news:"+bnotify.brief.getDBid()));
                    notifyHead=bnotify.brief.getSubject();
                    lines.add(Sf.restrictLength(bnotify.brief.getMessage(),400));
                    if(!bnotify.brief.getBriefOuts().isEmpty()) {
                        summary.append(bnotify.brief.getBriefOuts().get(0).getFrom());
                        summary.append(" ");
                    }

                    summary.append(Cal.getCal(bnotify.brief.getTimestamp()).getTimeHHMM());
                    //notifyText= Sf.restrictLength(brief.getMessage(), 20)+(brief.getMessage().length()>20?"...":"");
                }


            } else {
                intent.setData(Uri.parse("launch:home"));
                licon= BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
                notifyHead=usebriefs.size()+" "+BN.context.getString(R.string.notify_new_messages);

                boolean small=false;
                if(usebriefs.size()>2)
                    small=true;

                boolean ok=true;
                for(NotifyBrief bnotify: usebriefs) {
                    if(countEmail+countSms>4)
                        ok=false;

                    //StringBuilder from=new StringBuilder(BN.context.getString(R.string.notify_from));
                    String msg=bnotify.brief.getMessage();
                    Person p=getPerson(bnotify.brief);
                    String name =null;
                    if(bnotify.brief.getWITH_()==Brief.WITH_EMAIL) {
                        countEmail++;

                        Account acc= AccountsDb.getAccountById(bnotify.brief.getAccountId());
                        if(acc!=null && !summary.toString().contains(acc.getString(Account.STRING_EMAIL_ADDRESS))) {
                            if(summary.length()>0)
                                summary.append(", ");
                            summary.append(acc.getString(Account.STRING_EMAIL_ADDRESS));
                        }

                        if(ok && !small && msg.indexOf("\n")!=-1) {

                            String h = msg.substring(0,msg.indexOf("\n"));
                            String b = msg.substring(msg.indexOf("\n"),msg.length()-1);
                            lines.add(Sf.restrictLength(h,200));
                            lines.add(Sf.restrictLength(b,200));
                        } else if(ok) {
                            lines.add(Sf.restrictLength(msg,200));
                        }
                    } else if(bnotify.brief.getWITH_()==Brief.WITH_SMS) {
                        countSms++;
                        lines.add(smsStr+bnotify.brief.getMessage());
                    } else if(bnotify.brief.getWITH_()==Brief.WITH_NEWS) {
                        countNews++;
                        if(countNews<3) {
                            lines.add(Sf.restrictLength(bnotify.brief.getSubject(),200));
                            String source=null;
                            if(!bnotify.brief.getBriefOuts().isEmpty()) {
                                source=bnotify.brief.getBriefOuts().get(0).getFrom();
                                if(!summary.toString().contains(source)) {
                                    if (summary.length() > 0) {
                                        summary.append(", ");
                                    }
                                    summary.append(source);
                                }
                            }

                        }
                    }

                    //from.append(p.getString(Person.STRING_NAME));
                    if(lines.size()>8)
                        break;

                }
                if(countSms>0) {
                    if(summary.length()>0)
                        summary.append(", ");
                    summary.append(smsStr);
                }


            }

            BN.mBuilder.setContentTitle(notifyHead).setLargeIcon(licon);
            if(!lines.isEmpty()) {
                BN.mBuilder.setContentText(lines.get(0));
            }
            BN.mBuilder.setContentInfo(context.getString(R.string.app_name));

            inboxStyle.setBigContentTitle(notifyHead);

            inboxStyle.setSummaryText(summary.toString());
            for(String line: lines) {
                inboxStyle.addLine(line);
            }

            PendingIntent emptypIntent = PendingIntent.getActivity(BN.context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            BN.mBuilder.setContentIntent(emptypIntent);
            BN.mBuilder.setStyle(inboxStyle);


            if(BN.mNotificationManager==null)
                BN.mNotificationManager = (NotificationManager) BN.context.getSystemService(Context.NOTIFICATION_SERVICE);


            //BLog.e("NOTIFY LAUNCH","data: "+intent.getData().toString());
            //notification.flags|= Notification.FLAG_NO_CLEAR;


            boolean sound=false;
            boolean vibrate=false;

            SettingsDb.init();
            BriefSettings settings = SettingsDb.getSettings();

            if(countSms-BN.lastNotify.countSms>0) {
                if(BN.lastNotify.countSms!=0) {
                    if(settings.getBoolean(BriefSettings.BOOL_NOTIFY_REPEAT_VIBRATE))
                        vibrate=true;
                } else {
                    if(settings.getBoolean(BriefSettings.BOOL_NOTIFY_SMS_SOUND))
                        sound=true;
                    if(settings.getBoolean(BriefSettings.BOOL_NOTIFY_SMS_VIBRATE))
                        vibrate=true;
                }

            }
            if(countEmail-BN.lastNotify.countEmail>0) {
                if(BN.lastNotify.countEmail!=0) {
                    if(settings.getBoolean(BriefSettings.BOOL_NOTIFY_REPEAT_VIBRATE))
                        vibrate=true;
                } else {
                    if (settings.getBoolean(BriefSettings.BOOL_NOTIFY_EMAIL_SOUND))
                        sound = true;
                    if (settings.getBoolean(BriefSettings.BOOL_NOTIFY_EMAIL_VIBRATE))
                        vibrate = true;
                }
            }
            if(countNews-BN.lastNotify.countNews>0) {
                if(BN.lastNotify.countNews!=0) {
                    if(settings.getBoolean(BriefSettings.BOOL_NOTIFY_REPEAT_VIBRATE))
                        vibrate=true;
                } else {
                    if (settings.getBoolean(BriefSettings.BOOL_NOTIFY_NEWS_SOUND))
                        sound = true;
                    if (settings.getBoolean(BriefSettings.BOOL_NOTIFY_NEWS_VIBRATE))
                        vibrate = true;
                }
            }
            BN.lastNotify.countEmail=countEmail;
            BN.lastNotify.countSms=countSms;
            BN.lastNotify.countNews=countNews;

            int allowedAlertType=0;
            Cal now = new Cal();
            String timeSlot=now.getTimeSlotHHMM();
            allowedAlertType = State.getSettings().getTimeSlotSetting(timeSlot);

            //BLog.e("NOTIFY","allowed alert: "+allowedAlertType);

            if(sound && allowedAlertType==2 && !BriefService.isAppStarted()) {
                playDefaultSound(BN.context);
            }
            if(vibrate && allowedAlertType>=1) {
                Device.vibrate(context);
            }
            Notification notification=BN.mBuilder.build();
            BN.mNotificationManager.notify(TYPE_, notification);

        }
    };


    private static Person getPerson(Brief brief) {
        Person p= ContactsDb.getWithPersonId(BN.context,brief.getPersonId());

        if(p==null) {
            String email = null;
            String phone = null;
            if (brief.getWITH_() == Brief.WITH_EMAIL) {
                try {
                    email = brief.getBriefOuts().get(0).getFrom();
                } catch (Exception e) {}
            } else if((brief.getWITH_()==Brief.WITH_SMS)) {
                try {
                    phone = brief.getBriefOuts().get(0).getFrom();
                } catch (Exception e) {}
            }
            p=Person.getNewUnknownPerson(BN.context,phone,email);
        }
        return p;
    }
    public static void playDefaultSound(Context context) {


        Uri defaultRingtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(context, defaultRingtoneUri);
            mp.setOnPreparedListener(BN);
            mp.prepareAsync();
            /*
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);
            mediaPlayer.prepareAsync();
            mediaPlayer.setVolume(1f, 1f);
            */
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.release();
                }
            });
            //mp.start();
        }  catch (SecurityException e) {
            BLog.e("1: "+e.getMessage());
        } catch (IllegalStateException e) {
            BLog.e("1: "+e.getMessage());
        } catch (IOException e) {
            BLog.e("1: "+e.getMessage());
        }catch (IllegalArgumentException e) {
            BLog.e("1: "+e.getMessage());
            //e.printStackTrace();
        }


    }
    //mp.setDataSource(url);


    public void onPrepared(MediaPlayer player) {
        player.start();
    }
    /*
    public void showPop() {

        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=inflater.inflate(R.layout.b_info, null);
        //LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
//              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                      | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Load Average");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        //wm.addView(mView, params);
        wm.addView(view, params);


    }
    public void hidePop() {
        if(view != null)
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(view);
            view = null;
        }
    }
    */

    // Receivers
}
