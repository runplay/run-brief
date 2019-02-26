package run.brief.email;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import run.brief.BriefManager;
import run.brief.BriefSendDb;
import run.brief.R;
import run.brief.beans.Account;
import run.brief.beans.Brief;
import run.brief.beans.BriefSend;
import run.brief.beans.Email;
import run.brief.util.json.JSONObject;

public class EmailListAdapter extends BaseAdapter {
 
    private Activity activity;
    private ArrayList<Email> data;
    private static LayoutInflater inflater=null;
    private Account account;
 
    public EmailListAdapter(Activity a, Account account, ArrayList<Email> usedata) {
        activity = a;
        this.account=account;
        //this.data=data;
        setData(account,usedata);
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    private void setData(Account account, ArrayList<Email> usedata) {
    	data=new ArrayList<Email>();
    	ArrayList<BriefSend> sends = BriefSendDb.getItems(Brief.WITH_EMAIL);
    	if(account !=null && !sends.isEmpty()) {
    		for(BriefSend send: sends) {

    			JSONObject job=new JSONObject(send.getString(BriefSend.STRING_BJSON_BEAN));
    			if(job!=null) {
    				Email em = new Email(job);
    				if(em!=null) {
    					if(account.getString(Account.STRING_EMAIL_ADDRESS).equals(em.getString(Email.STRING_FROM))) {
                            em.setLong(Email.LONG_ID,send.getLong(BriefSend.LONG_ID));
                            em.setString(Email.STRING_XBRIEFID,"-1");
                            em.setString(Email.STRING_FOLDER,account.getSentFolder());
    						data.add(em);
    					}
    				}
    			}
    		}
    	}
    	data.addAll(usedata);
    }
    public int getCount() {
    	if(data!=null)
    		return data.size();
    	else 
    		return 0;
    }
 
    public Email getItem(int position) {
    	if(data!=null && position<data.size())
    		return data.get(position);
    	return null;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	

		if(convertView == null)
		{
			convertView = inflater.inflate(R.layout.brief_list_item, parent, false);
		}
		//ImageView status =(ImageView) convertView.findViewById(R.id.email_status_img);
		//TextView date = (TextView) convertView.findViewById(R.id.email_date);
        //TextView from = (TextView) convertView.findViewById(R.id.email_list_from);
        //TextView subject = (TextView) convertView.findViewById(R.id.email_list_subject);

        //B.addStyle( new TextView[]{subject, date});
        //B.addStyleBold(from);
        TextView text = (TextView)convertView.findViewById(R.id.brief_item_text);

        Email email = getItem(position);
        Brief brief= new Brief(activity,account,email,position);
        
        if(brief!=null) {
            text.setText(brief.getMessage());
            BriefManager.styleViewWith(activity, convertView, brief);

        }
		
		
        
        return convertView;
    }
}
