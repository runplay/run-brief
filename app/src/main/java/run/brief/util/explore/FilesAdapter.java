package run.brief.util.explore;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import run.brief.b.B;
import run.brief.R;
import run.brief.util.Cal;
import run.brief.util.Files;
import run.brief.util.Num;
 
public class FilesAdapter extends BaseAdapter {
 
    private Activity activity;
    private List<String> files;
    private DecimalFormat df = new DecimalFormat( "#0.00" );
    private FilePopListener listen;

    private int TYPE_=0;
    public static final int TYPE_VIEW=0;
    public static final int TYPE_EDIT=1;
    public String selectedFile;



    //private JSONArray data;
    private static LayoutInflater inflater=null;
 
    public FilesAdapter(Activity a,List<String> files,int TYPE_,FilePopListener listner) {
        activity = a;
        this.files=files;
        this.listen=listner;
        if(TYPE_==TYPE_EDIT)
            this.TYPE_=TYPE_;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return files.size();
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
            vi = inflater.inflate(R.layout.files_adapter_item, null);
 
        TextView path = (TextView)vi.findViewById(R.id.file_text_path);
        TextView name = (TextView)vi.findViewById(R.id.file_text_name);
        TextView summary = (TextView)vi.findViewById(R.id.file_text_extra);
        ImageView img = (ImageView) vi.findViewById(R.id.file_type_image);
        String t = files.get(position);

        B.addStyle(path, summary);
        B.addStyleBold(name);
        
        //BLog.e("NTE", t);
        if(t!=null) {
        	File f = new File(t);
        	StringBuilder sum=new StringBuilder();
        	if(f.exists()) {
        		sum.append("ok - "+Num.btyesToFileSizeString(f.length())+" - last mod: "+(new Cal(f.lastModified()).friendlyReadDate()));
        	} else {
                //BLog.e("FN",f.getAbsolutePath());
        		sum.append(activity.getString(R.string.file_invalid));
        	}
        	String fn=Files.removeBriefFileExtension(Files.getFileNameFromPath(t));
        	String pn=Files.getPathLessFileName(t);
        	path.setText(pn);
        	name.setText(fn);
        	summary.setText(sum.toString());
        	img.setBackground(Files.getFileIcon(activity, fn));
            vi.setTag(t);
            //BLog.e("FM",""+position);
            vi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    PopupMenu popupMenu = new PopupMenu(activity, view);
                    //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());
                    popupMenu.getMenu().add(activity.getResources().getString(R.string.label_open));
                    if(TYPE_==TYPE_EDIT)
                        popupMenu.getMenu().add(activity.getResources().getString(R.string.label_remove));
                    selectedFile=(String) view.getTag();
                    //listen.setFilePos((Integer) view.getTag());
                    popupMenu.setOnMenuItemClickListener(listen);
/*
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {

                            return true;
                        }
                    });
*/
                    popupMenu.show();
                }
            });
        }
        // Setting all values in listview
        
        

        return vi;
    }
}
