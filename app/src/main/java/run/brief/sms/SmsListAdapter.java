package run.brief.sms;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.beans.Brief;
import run.brief.beans.SmsMsg;
import run.brief.util.ViewManagerText;

public class SmsListAdapter extends BaseAdapter {
 
    private Activity activity;
    //private ArrayList<SmsMessage> data;
    private static LayoutInflater inflater=null;
    //Drawable sent;
 
    public SmsListAdapter(Activity a) {
        activity = a;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //sent = activity.getResources().getDrawable(R.drawable.social_send_now);
		//sent.setColorFilter( 0xff00ff00, Mode.MULTIPLY );
		//sent.setAlpha(80);
    }
 
    public int getCount() {
        return SmsDb.size();
    }
 
    public SmsMsg getItem(int position) {
        return SmsDb.get(position);
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
		if(convertView == null)
		{
			//holder = new ViewHolder();
			convertView = LayoutInflater.from(activity).inflate(R.layout.brief_list_item, parent, false);
			
			//convertView.setTag(holder);
		}
		SmsMsg sms = SmsDb.get(position);
        //BLog.e("SMS", "thr: " + sms.getThreadId());
		Brief brief = new Brief(activity,sms,position);
		//else
			//holder = (ViewHolder) convertView.getTag();
		TextView text = (TextView)convertView.findViewById(R.id.brief_item_text); 
		
        if(brief!=null) {
        	//BriefRating br = 
        	text.setText(brief.getMessage());
        	ViewManagerText.manageTextView(activity, text);
        	
        	BriefManager.styleViewWith(activity,convertView,brief);
        }
        
        /*
        TextView name = (TextView) convertView.findViewById(R.id.field_phone_number); 
        TextView message = (TextView) convertView.findViewById(R.id.field_message); 
        ImageView image=(ImageView) convertView.findViewById(R.id.image_contact);
        LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.sms_row);
        
        
        SmsMsg sms = SmsDb.get(position);
        
        message.setText(sms.getMessageContent());
        ViewManagerText.manageTextView(activity, message);
        //holder.message = message;
		//holder.message.setText(sms.getMessageContent());
		//holder.message.setTextSize(FontSizes.SMALL);
		
		name.setText(sms.getMessageNumber());
		
		LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) layout.getLayoutParams();
		//check if it is a status message then remove background, and change text color.
		//if(txt.isStatusMessage())
		//{
		//	holder.message.setBackgroundDrawable(null);
		//	lp.gravity = Gravity.LEFT;
		//	holder.message.setTextColor(R.color.textFieldColor);
		//}
		//else
		//{		
			//Check whether message is mine to show green background and align to right
		
			if(sms.isMine())
			{
				name.setVisibility(View.GONE);
				image.setVisibility(View.INVISIBLE);
				
				message.setCompoundDrawablesWithIntrinsicBounds(sent,null,null,null);
				//layout.setBackgroundResource(R.drawable.brief_pod_sent);
				//holder.message.setBackgroundResource(R.drawable.brief_out);
				lp.gravity = Gravity.RIGHT;
				layout.setGravity(Gravity.RIGHT);
				
			}
			//If not mine then it is from sender to show orange background and align to left
			else
			{
				name.setVisibility(View.VISIBLE);
				image.setVisibility(View.VISIBLE);
				//layout.setBackgroundResource(R.drawable.brief_pod);
				message.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
				//holder.message.setBackgroundResource(R.drawable.brief_in);
				lp.gravity = Gravity.LEFT;
				layout.setGravity(Gravity.LEFT);
			}
			layout.setLayoutParams(lp);
			//holder.message.setLayoutParams(lp);
			//holder.message.setTextColor(activity.getResources().getColor(R.color.black));	
		//}
    	
        */
        return convertView;
    }
}
