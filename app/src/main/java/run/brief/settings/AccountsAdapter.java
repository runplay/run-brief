package run.brief.settings;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.beans.SyncData;
import run.brief.service.SyncDataDb;
import run.brief.util.Sf;

public class AccountsAdapter extends BaseAdapter {
 
    private Activity activity;
    //private JSONArray data;
    private static LayoutInflater inflater=null;
 
    public AccountsAdapter(Activity a) {
        activity = a;
        //this.data=data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
 
    public int getCount() {
        return AccountsDb.Size();
    }
 
    public Object getItem(int position) {
        return AccountsDb.getAccount(position);
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.account_list_item, null);
        else
            vi=convertView;

        TextView type = (TextView)vi.findViewById(R.id.account_type); 
        TextView name = (TextView)vi.findViewById(R.id.account_name);
        ImageView image=(ImageView) vi.findViewById(R.id.account_image);
        ProgressBar progress = (ProgressBar) vi.findViewById(R.id.account_is_sync);
        progress.setVisibility(View.GONE);

        ImageView accountSync = (ImageView) vi.findViewById(R.id.account_sync_switch);


        B.addStyle(type);
        B.addStyleBold(name);

        Account a = AccountsDb.getAccount(position);
        accountSync.setTag(a.getLong(Account.LONG_ID)+"");
        vi.setTag(a.getLong(Account.LONG_ID)+"");
        // Setting all values in listview
        if(a!=null) {
            SyncData accsync = SyncDataDb.getByAccountId(a.getLong(Account.LONG_ID));
            if(a.getInt(Account.INT_SIGNATURE_REKEY)>0) {
                accountSync.setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_rekey));
                accountSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //BLog.e("CL",view.getTag().toString());
                        Account account = AccountsDb.getAccountById(Sf.toLong(view.getTag().toString()));
                        if(account!=null) {
                            State.addToState(State.SECTION_OAUTH_GOOGLE, new StateObject(StateObject.STRING_VALUE, account.getString(Account.STRING_EMAIL_ADDRESS)));
                            Bgo.openFragmentBackStack(activity, GmailAddFragment.class);
                        }
                    }
                });

            } else {
                if(accsync.isActive()) {
                    accountSync.setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_active));

                } else {
                    accountSync.setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_inactive));
                }
                accountSync.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //BLog.e("CL",view.getTag().toString());
                        Account account = AccountsDb.getAccountById(Sf.toLong(view.getTag().toString()));
                        if(account!=null) {
                            SyncData data = SyncDataDb.getByAccountId(account.getLong(Account.LONG_ID));
                            //BLog.e("CL",data.isActive()+" - "+view.getTag().toString());

                            if(data.isActive()) {
                                data.setActive(false);
                                ((ImageView) view).setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_inactive));
                            } else {
                                data.setActive(true);
                                ((ImageView) view).setImageDrawable(activity.getResources().getDrawable(R.drawable.sync_active));
                            }
                            SyncDataDb.update(data);
                        }
                    }
                });
            }

        	if(a.has(Account.STRING_EMAIL_ADDRESS))
        		name.setText(a.getString(Account.STRING_EMAIL_ADDRESS));
        	if(a.has(Account.INT_TYPE_)) {
                switch(a.getInt(Account.INT_TYPE_)) {
                    case Account.TYPE_EMAIL :
                        type.setText(activity.getString(R.string.label_email));
                        image.setImageDrawable(activity.getResources().getDrawable(a.getAccountRIcon()));
                }
            }

	        
        }
        return vi;
    }
}
