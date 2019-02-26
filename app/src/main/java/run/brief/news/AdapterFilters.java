package run.brief.news;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import run.brief.R;
import run.brief.b.B;
import run.brief.util.json.JSONArray;

/**
 * Created by coops on 19/12/14.
 */
public class AdapterFilters extends BaseAdapter {

    private Context context;
    private JSONArray filterValues;
    //private LayoutInflater inflater=null;

    private LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private LinearLayout.LayoutParams wlp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

    public AdapterFilters(Context context, JSONArray filterValues) {
        //activity = a;
        this.context=context;
        this.filterValues=filterValues;
        //inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return filterValues.length();
    }
    /*
       public static void setSelectedPersons(HashMap<String,Person> tos) {
           selectedPersons=tos;
       }
       */
    public Object getItem(int position) {
        return filterValues.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        RelativeLayout view=null;

        if(convertView==null) {
            view=new RelativeLayout(context);
            AbsListView.LayoutParams rlp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);
            view.setLayoutParams(rlp);
            view.setPadding(3,3,3,3);
        } else {
            view=(RelativeLayout) convertView;
            view.removeAllViews();
        }
        TextView text=new TextView(context);
        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT);

        B.addStyle(text);

        text.setLayoutParams(lp);
        text.setBackgroundColor(context.getResources().getColor(R.color.grey));
        text.setTextColor(context.getResources().getColor(R.color.white));
        text.setPadding(4,2,4,2);
        //RelativeLayout citem = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.contacts_clipboard_item, parent, false);
        String value  = (String) filterValues.get(position); //ContactsSelectedClipboard.get(it.next());
        text.setText(value);

        view.addView(text);



        return view;


    }

}