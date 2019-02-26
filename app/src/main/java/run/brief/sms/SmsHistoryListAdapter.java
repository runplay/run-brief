package run.brief.sms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import run.brief.BriefManager;
import run.brief.BriefSendDb;
import run.brief.R;
import run.brief.beans.Brief;
import run.brief.beans.BriefSend;
import run.brief.beans.Person;
import run.brief.beans.SmsMsg;
import run.brief.beans.SmsSend;
import run.brief.contacts.ContactsDb;
import run.brief.util.Cal;
import run.brief.util.ViewManagerText;
import run.brief.util.json.JSONObject;

public class SmsHistoryListAdapter extends BaseAdapter {
 
    private Activity activity;
    //private ArrayList<SmsMessage> data;
    private LayoutInflater inflater=null;
    //Drawable sent;
    ArrayList<SmsMsg> history=new ArrayList<SmsMsg>();
    //private Drawable sent;
    private SmsMsg fromSms;
    private Person person;
    private boolean orderbottomUp=true;
 
    public SmsHistoryListAdapter(Activity a,SmsMsg historySms,boolean bottomUp) {
        activity = a;
        //this.data=data;
        this.orderbottomUp=bottomUp;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        setHistory(historySms);
		
    }

    
    public void setHistory(SmsMsg historySms) {
    	history.clear();
    	//BLog.e("SMS", historySms==null?"history null":historySms.getMessageContent());
    	if(historySms!=null) {
	    	fromSms=historySms;
	    	person=ContactsDb.getWithTelephoneConcatEnd(activity, fromSms.getMessageNumber());
	    	if(person==null)
	    		person= Person.getNewUnknownPerson(activity, fromSms.getMessageNumber(), null);
	    	
	    	ArrayList<BriefSend> sendsms = BriefSendDb.getItems(Brief.WITH_SMS);
	    	if(!sendsms.isEmpty()) {
	    		for(BriefSend bs: sendsms) {
	    			SmsSend sms = new SmsSend(new JSONObject(bs.getString(BriefSend.STRING_BJSON_BEAN)));
	    			if(fromSms.getMessageNumber().equals(sms.getString(SmsSend.STRING_TO_NUMBER))) {
	    				SmsMsg smsg = new SmsMsg();
	    				smsg.setMessageContent(sms.getString(SmsSend.STRING_MESSAGE));
	    				smsg.setMessageNumber(sms.getString(SmsSend.STRING_TO_NUMBER));
	    				smsg.setStatus(Brief.STATE_SENDNG);
	    				smsg.setMessageDate(new Cal());
                        smsg.setMine(true);
	    				history.add(smsg);
	    			}
	    		}
	    	}
/*
            List<String> fetchNumbers = new ArrayList<String>();
            for(int i=0; i<SmsDb.size(); i++) {

                SmsMsg msg=SmsDb.get(i);
                if(msg!=null) {
                    if(msg.getMessageNumber().equals(fromSms.getMessageNumber())) {
                        history.add(msg);
                    }
                }
            }
*/
            ArrayList<SmsMsg> messages = SmsFunctions.fetchSmsForNumber(activity,historySms.getMessageNumber(),0,20);
	    	for(int i=0; i<messages.size(); i++) {
	    		
	    		SmsMsg msg=messages.get(i);
	    		if(msg!=null) {
	    		    history.add(msg);
	    		}
	    	}
    	}
    	if(orderbottomUp)
    		Collections.reverse(history);
    }
    
    public int getCount() {
        return history.size();
    }
 
    public SmsMsg getItem(int position) {
        return history.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
	//private static class ViewHolder
	///{
	//	TextView message;
	//}

    public View getView(int position, View convertView, ViewGroup parent) {

        //BLog.e("bpv", "smsmsmsmsm  1");
        //ViewHolder holder;
        if (convertView == null) {
            //holder = new ViewHolder();
            convertView = LayoutInflater.from(activity).inflate(R.layout.brief_list_item, parent, false);

            //convertView.setTag(holder);
        }
        SmsMsg sms = history.get(position);
        //BLog.e("SMS", "thr: " + sms.getThreadId());
        Brief brief = new Brief(activity, sms, position);
        //else
        //holder = (ViewHolder) convertView.getTag();
        TextView text = (TextView) convertView.findViewById(R.id.brief_item_text);

        if (brief != null) {
            //BriefRating br =
            text.setText(brief.getMessage());
            ViewManagerText.manageTextView(activity, text);

            BriefManager.styleViewWith(activity, convertView, brief);
            View bihead = convertView.findViewById(R.id.brief_item_heading);
            if(sms.isMine()) {
                bihead.setVisibility(View.GONE);
            } else {
                bihead.setVisibility(View.VISIBLE);
            }
        }
        return convertView;
    }
 
}
