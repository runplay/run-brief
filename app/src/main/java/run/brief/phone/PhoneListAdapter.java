package run.brief.phone;

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
import run.brief.beans.Phonecall;

public class PhoneListAdapter extends BaseAdapter {
 
    private Activity activity;
    //private ArrayList<SmsMessage> data;
    private LayoutInflater inflater=null;
    //private static String bandcolor;
    //Drawable sent;
 
    public PhoneListAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return PhoneDb.size();
    }
 
    public Phonecall getItem(int position) {
        return PhoneDb.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	
       
		//ViewHolder holder; 
		if(convertView == null) {
			//holder = new ViewHolder();
			convertView = inflater.inflate(R.layout.brief_list_item, parent, false);
		}

        TextView text = (TextView)convertView.findViewById(R.id.brief_item_text);
        Phonecall call = PhoneDb.get(position);
        Brief brief = new Brief(activity,call,position);
        
        if(brief!=null) {


            text.setText(brief.getMessage());
            //text.setGravity(Gravity.RIGHT);
            //text.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.i_wait,0);
            BriefManager.styleViewWith(activity, convertView, brief);

        
        }

        return convertView;
    }
}
