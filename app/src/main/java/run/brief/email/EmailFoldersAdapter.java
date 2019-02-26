package run.brief.email;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.B;
import run.brief.R;
import run.brief.beans.Account;
import run.brief.beans.EmailFolder;
import run.brief.util.Sf;

public class EmailFoldersAdapter extends BaseAdapter {
 
    private Activity activity;
    private static LayoutInflater inflater=null;
    private List<EmailFolder> folders=new ArrayList<EmailFolder>();
    public EmailFoldersAdapter(Activity activity, Account account, ArrayList<EmailFolder> infolders) {
        activity = activity;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        List<String> sync = account.getEmailFolders();
        if(infolders.isEmpty()) {
            for(String name: sync) {
                folders.add(new EmailFolder(name,0,0,0,true));
            }
        } else {
            for(EmailFolder in: infolders) {
                for(String name: sync) {
                    if(name.equals(in.getString(EmailFolder.STRING_FOLDERNAME)))
                        in.setBoolean(EmailFolder.BOOLEAN_INCLUDE_SYNC,true);
                }
                folders.add(in);
            }
        }

    }

    public int getCount() {
        return folders.size();
    }
 
    public Object getItem(int position) {
        return folders.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.email_folders_list_item, null);
 
        TextView fname = (TextView)vi.findViewById(R.id.email_folder_name); 
        TextView ctotal = (TextView)vi.findViewById(R.id.email_folder_count_total);
        TextView curead = (TextView)vi.findViewById(R.id.email_folder_count_unread); 
        TextView cnew = (TextView)vi.findViewById(R.id.email_folder_count_new);

        B.addStyle( new TextView[]{fname,ctotal,curead,cnew});

        if(position<folders.size()) {
            EmailFolder a = folders.get(position);
            // Setting all values in listview
            if (a != null) {
                fname.setText(a.getString(EmailFolder.STRING_FOLDERNAME));
                if(Sf.toInt(a.getString(EmailFolder.INT_COUNT_EMAILS))>0) {
                    ctotal.setText(a.getString(EmailFolder.INT_COUNT_EMAILS));
                    curead.setText(a.getString(EmailFolder.INT_COUNT_UNREAD));
                    cnew.setText(a.getString(EmailFolder.INT_COUNT_NEW));
                }
            }
        }
        return vi;
    }
}
