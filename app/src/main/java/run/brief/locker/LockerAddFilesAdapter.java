package run.brief.locker;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import run.brief.R;
import run.brief.b.B;
import run.brief.util.Cal;
import run.brief.util.Files;
import run.brief.util.Num;

public class LockerAddFilesAdapter extends BaseAdapter {

	 
	
	    private Activity activity;

	    
	    //private JSONArray data;
	    private static LayoutInflater inflater=null;
	    private List<File> addFiles;
	    
	 
	    public LockerAddFilesAdapter(Activity a,final List<File> addFiles) {
	        activity = a;
	        this.addFiles=addFiles;
	        //this.data=data;
	        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

	    }

	 
		@Override
		public int getCount() {
			 return addFiles.size();
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
			//ImageView img = (ImageView) view.findViewById(R.id.explore_file_type);

			B.addStyle(path,name,summary);
			// put the image on the text view
			File item=addFiles.get(position);
            summary.setCompoundDrawables(null,null,null,null);
            if(item!=null) {
                String fn=item.getName();
                //String pn=Files.getPathLessFileName(f.getPath());
                path.setText(Files.getPathLessFileName(item.getPath()));
                name.setText(fn);
                StringBuilder sum=new StringBuilder();
                summary.setCompoundDrawables(activity.getResources().getDrawable(R.drawable.warning),null,null,null);
                if(item.exists()) {
                    if(item.getPath().startsWith(Files.HOME_PATH_APP)) {
                        sum.append(activity.getString(R.string.locker_Adding_files_cannot));
                        name.setText(activity.getString(R.string.locker_Adding_files_will_not)+" - "+name.getText());

                    } else {
                        sum.append("ok - " + Num.btyesToFileSizeString(item.length()) + " - last mod: " + (new Cal(item.lastModified()).friendlyReadDate()));
                    }
                } else {
                    sum.append(activity.getString(R.string.file_invalid));
                }
                //File f = item.getF;

                summary.setText(sum.toString());
                img.setBackgroundResource(Files.getFileRIcon(fn));
            }
	

	        return view;
	 
	    }
	 


	}
