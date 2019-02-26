package run.brief.locker;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import run.brief.b.B;
import run.brief.R;
import run.brief.b.bImageViewLoading;
import run.brief.beans.LockerItem;
import run.brief.util.Cal;
import run.brief.util.Files;
import run.brief.util.Num;

public class LockerItemsAdapter extends BaseAdapter {

	    private Activity activity;
	    private bImageViewLoading ivFlip;
	    //private Animation animation1;
	    //private Animation animation2;
	    //private int checkedCount = 0;
	    //public static ActionMode mMode;
	    //public static boolean isActionModeShowing;
	    
	    //private JSONArray data;
	    private static LayoutInflater inflater=null;
	    
	 
	    public LockerItemsAdapter(Activity a) {
	        activity = a;
	        //this.data=data;
	        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//animation1 = AnimationUtils.loadAnimation(activity, R.anim.to_middle);
			//animation2 = AnimationUtils.loadAnimation(activity, R.anim.from_middle);

			//isActionModeShowing = false;
	    }

	 
		@Override
		public int getCount() {
			 return LockerManager.getLockerItems().size();
		}
	 
		@Override
	    public Object getItem(int position) {
	        return position;
	    }
	 
		@Override
	    public long getItemId(int position) {
	        return position;
	    }
	    @Override
	    public View getView(final int position, View view, ViewGroup parent) {
	    	//view = inflater.inflate(android.R.layout.simple_list_item_1, null);
	        //LayoutInflater inflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (view == null) {
                view = inflater.inflate(R.layout.files_adapter_item, null);
                //convertView = inflater.inflate(R.layout.list_item, null);

            }

            TextView path = (TextView)view.findViewById(R.id.file_text_path);
            TextView name = (TextView)view.findViewById(R.id.file_text_name);
            TextView summary = (TextView)view.findViewById(R.id.file_text_extra);
            ImageView img = (ImageView) view.findViewById(R.id.file_type_image);
            B.addStyle( new TextView[]{path,name,summary});
            LockerItem item=LockerManager.getLockerItem(position);

            if(item!=null) {
                //File f = new File(item.getString(LockerItem.STRING_LOCKER_FILE_NAME));
                StringBuilder sum=new StringBuilder();
                if(item.exists()) {
                    sum.append("ok - "+Num.btyesToFileSizeString(item.getLockerFileSize())+" - last mod: "+(new Cal(item.getLockerFileLoastModified()).friendlyReadDate()));
                } else {
                    sum.append(activity.getString(R.string.file_invalid));
                }
                //File f = item.getF;
                String fn=Files.getFileNameFromPath(item.getString(LockerItem.STRING_ORIGIN_PATH));
                //String pn=Files.getPathLessFileName(f.getPath());
                path.setText(Files.getPathLessFileName(item.getString(LockerItem.STRING_ORIGIN_PATH)));
                name.setText(fn);
                summary.setText(sum.toString());
                img.setBackgroundResource(Files.getFileRIcon(fn));
            }

	        return view;
	 
	    }
	 


	}
