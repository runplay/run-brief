package run.brief.notes;


import run.brief.beans.Brief;
import run.brief.beans.Note;
import run.brief.util.Sf;
import run.brief.util.ViewManagerText;
import run.brief.BriefManager;
import run.brief.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class NotesAdapter extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
 
    public NotesAdapter(Activity a) {
        activity = a;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return NotesDb.size();
    }
 
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(vi==null)
            vi = inflater.inflate(R.layout.brief_list_item, parent, false);
 
        vi.setTag(Integer.valueOf(position));
        Note t = NotesDb.getByIndex(position);
		Brief brief = new Brief(t,position);
		//else
			//holder = (ViewHolder) convertView.getTag();
		TextView text = (TextView)vi.findViewById(R.id.brief_item_text); 
		
        if(brief!=null) {
        	//BriefRating br = 
        	String add=brief.getMessage().length()>400?"...":"";
        	text.setText(Sf.restrictLength(brief.getMessage(), 400)+ add);
        	
        	
        	BriefManager.styleViewWith(activity,vi,brief);
        	ViewManagerText.manageTextView(activity, text);
        }
        /*
        
        TextView summary = (TextView)vi.findViewById(R.id.notes_list_summary); 
        TextView date = (TextView)vi.findViewById(R.id.notes_list_date); 
 

        Note t = NotesDb.get(position);
 
        // Setting all values in listview
        if(t!=null) {
	        summary.setText(Sf.restrictLength(t.getString(Note.INT_ID)+" - "+t.getString(Note.STRING_TEXT), 140));
	        ViewManagerText.manageTextView(activity, summary);
	        Cal c=new Cal(t.getLong(Note.LONG_DATE_CREATED));
	        String wr = c.friendlyReadDate();//+" - "+t.getFileCount()+" files";
	        
	        int filesc=0;
	        try {
	        	filesc=t.getJSONArray(Note.JSONARRAY_FILES).length();
	        } catch(Exception e){}
	        
	        if(c!=null)
	        	date.setText(wr+" - "+filesc+" files");
	        
        } else {
        	BLog.add("NotesAdapter.getView() item is NULL");
        }
        */
        return vi;
    }

}
