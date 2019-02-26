package run.brief.email;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import run.brief.b.B;
import run.brief.R;
import run.brief.beans.Account;
import run.brief.settings.AccountsDb;
 
public class EmailAccountsAdapter extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
    private ArrayList<Account> emailAccounts;
 
    public EmailAccountsAdapter(Activity a) {
        activity = a;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        refresh();
    }
    public void refresh() {
    	emailAccounts=AccountsDb.getAllEmailAccounts();
    }
    public int getCount() {
        return emailAccounts.size();
    }
 
    public Object getItem(int position) {
        return emailAccounts.get(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.account_list_item, null);
 
        TextView type = (TextView)vi.findViewById(R.id.account_type); 
        TextView name = (TextView)vi.findViewById(R.id.account_name);
        ImageView image = (ImageView) vi.findViewById(R.id.account_image);

        B.addStyle( new TextView[]{name,type});

        Account a = emailAccounts.get(position);
        // Setting all values in listview
        if(a!=null) {
        	if(a.has(Account.STRING_EMAIL_ADDRESS))
        		name.setText(a.getString(Account.STRING_EMAIL_ADDRESS));
        	if(a.has(Account.INT_TYPE_))
        		type.setText("type");
            image.setImageDrawable(activity.getResources().getDrawable(a.getAccountRIcon()));
	        
        }
        return vi;
    }
}
