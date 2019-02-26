package run.brief.twitter;


import java.util.ArrayList;
import java.util.HashMap;
 
import run.brief.beans.Tweet;
import run.brief.util.Cal;
import run.brief.R;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class TweetsAdapter extends BaseAdapter {
 
    private Activity activity;
    private ArrayList<Tweet> data;
    private static LayoutInflater inflater=null;
 
    public TweetsAdapter(Activity a, ArrayList<Tweet> data) {
        activity = a;
        this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return data.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.contacts_names, null);
 
        TextView name = (TextView)vi.findViewById(R.id.tweetname); 
        TextView text = (TextView)vi.findViewById(R.id.tweettext); 
        TextView date = (TextView)vi.findViewById(R.id.tweetdate); 
 
        Tweet t = data.get(position);
 
        // Setting all values in listview
        name.setText(t.getString(Tweet.STRING_NAME));
        text.setText(t.getString(Tweet.STRING_MSG));
        Cal c=new Cal(t.getLong(Tweet.LONG_DATE));
        date.setText(c.friendlyReadDate());

        return vi;
    }
}
