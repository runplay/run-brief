package run.brief;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import run.brief.beans.Brief;

public class BriefPodAdapterList extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
    private List<Brief> briefs;
    private List<View> headers=new ArrayList<View>();
    //public static int currentSelectedPosition;

    public void clearHeaderViews() {
        headers.clear();
    }
    public void addHeaderView(View headerView) {
        headers.add(headerView);
    }
    public int getHeadersCount() {
        return headers.size();
    }
    public BriefPodAdapterList(Activity a,List<Brief> usebriefs) {
        activity = a;
        this.briefs=usebriefs;
        //BriefManager.init(activity);
        //BriefManager.refresh(a,RATING_);
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return briefs.size()+headers.size();
    }
 
    public Object getItem(int position) {
        if(position<headers.size()) {
            return headers.get(position);
        }

        return briefs.get(position-headers.size());
    }
 
    public long getItemId(int position) {
        return position;
    }
    public View getView(int position, View convertView, ViewGroup parent) {
        if(position<headers.size()) {
            return headers.get(position);
        } else {
            return BriefManager.getView(activity, convertView, briefs.get(position-headers.size()));
        }

    }
    
}
