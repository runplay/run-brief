package run.brief.util.log;


import run.brief.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
 
public class AdapterListLogs extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
    //public static int currentSelectedPosition;
 
    public AdapterListLogs(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return BLog.size();
    }
 
    public Object getItem(int position) {
        return BLog.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
    

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.logs_item, null);
 
        
        TextView text = (TextView)vi.findViewById(R.id.logs_item_text); 
        
        //ImageView img = (ImageView) vi.findViewById(R.id.news_item_image);

        String l = BLog.get(position);
 
        // Setting all values in listview
        if(l!=null) {
         	text.setText(l);
        	vi.setTag(position);
        }
        return vi;
    }

}
