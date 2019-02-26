package run.brief.search;


import run.brief.beans.SearchResult;
import run.brief.BriefManager;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class SearchAdapter extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
 
    public SearchAdapter(Activity a) {

        activity = a;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return Searcher.size();
    }
 
    public Object getItem(int position) {
        return Searcher.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
    	SearchResult res=Searcher.get(position);
    	return BriefManager.getView(activity, convertView, res);
    }
}
