package run.brief.beans;

import android.content.Context;
import android.support.v4.app.NotificationCompat;

import java.util.ArrayList;
import java.util.List;

import run.brief.R;
import run.brief.util.Sf;

/**
 * Created by coops on 17/12/14.
 */
public class NotifyAlert {

    private List<AlertItem> items = new ArrayList<AlertItem>();
    private int BRIEF_WITH_;

    public NotifyAlert(int BRIEF_WITH_) {
        this.BRIEF_WITH_=BRIEF_WITH_;
    }

    public void addAlertItem(int Rdrawable, String notifyHead, String notifyText) {
        items.add(new AlertItem(Rdrawable, notifyHead, notifyText));
    }
    public void addAlertItem(Person person, String notifyHead, String notifyText) {
        items.add(new AlertItem(person, notifyHead, notifyText));
    }
    private int getRIcon() {
        switch(BRIEF_WITH_) {
            case Brief.WITH_EMAIL:
            case Brief.WITH_SMS:
                return R.drawable.icon;
            default:
                return R.drawable.icon_locker;
        }
    }
    public NotificationCompat.Builder getNotification(Context context) {
        if(items.isEmpty()) {

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            StringBuilder head = new StringBuilder();
            StringBuilder body = new StringBuilder();
            StringBuilder title = new StringBuilder();
            if(items.size()==1) {
                AlertItem ai = items.get(0);
                if(ai.person.hasImageThumbnail()) {
                    mBuilder.setLargeIcon(ai.person.getThumbnail(context));
                } else {
                    mBuilder.setSmallIcon(getRIcon());
                }
                title.append(context.getResources().getString(R.string.notify_from)+": "+ai.person.getString(Person.STRING_NAME));
                //inboxStyle.setBigContentTitle(context.getResources().getString(R.string.notify_from)+": "+ai.person.getName());
                body.append(ai.notifyText.length()>140?Sf.restrictLength(ai.notifyText,140)+"...":ai.notifyText);
                head.append(ai.person.getString(Person.STRING_NAME));

            } else {
                mBuilder.setSmallIcon(getRIcon());
                title.append("---");
                head.append("------");
                body.append("______");

            }
            inboxStyle.setBigContentTitle(title.toString());
            mBuilder.setContentTitle(head.toString())
                    .setContentText(body.toString());


            /*
                    .setSmallIcon(R.drawable.icon_i)
                    .setContentTitle(notifyHead)
                    .setContentText(notifyText);

            String[] events = new String[6];
            // Sets a title for the Inbox style big view
            inboxStyle.setBigContentTitle("Event tracker details:");

            // Moves events into the big view
            for (int i = 0; i < events.length; i++) {

                inboxStyle.addLine(events[i]);
            }
            */
            // Moves the big view style object into the notification object.
            mBuilder.setStyle(inboxStyle);
            return mBuilder;
        }
        return null;
    }

    private class AlertItem {

        public int Rdrawable;
        public Person person;
        public String notifyHead;
        public String notifyText;


        public AlertItem(int Rdrawable, String notifyHead, String notifyText) {
            this.Rdrawable=Rdrawable;
            this.notifyHead=notifyHead;
            this.notifyText=notifyText;

        }
        public AlertItem(Person person, String notifyHead, String notifyText) {
            this.person=person;
            this.notifyHead=notifyHead;
            this.notifyText=notifyText;

        }
    }

}
