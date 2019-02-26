package run.brief;


import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.Device;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.email.EmailSendFragment;
import run.brief.notes.NotesEditFragment;
import run.brief.settings.AccountsDb;
import run.brief.settings.AccountsManager;
import run.brief.sms.SmsSendFragment;
import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.util.BriefActivityManager;
import run.brief.b.State;
import run.brief.util.camera.CameraFragment;

public class NewActionAdapter extends BaseAdapter {
 
    private Activity activity;
    private List<ViewHolder> data=new ArrayList<ViewHolder>();
    private LayoutInflater inflater=null;
    
    //public static HashMap<String,Person> selectedPersons=new HashMap<String,Person>();
    
    
    public NewActionAdapter(Activity a) {
        activity = a;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(Device.hasPhone()) {
			ViewHolder tmp = new ViewHolder();
			tmp.title=activity.getString(R.string.title_phone);
			tmp.img=R.drawable.i_phone;
			tmp.id=ID_PHONE;
			data.add(tmp);
		}

		if(Device.hasContentProviderSmsSent()) {
			ViewHolder tmp = new ViewHolder();
			tmp.title=activity.getString(R.string.title_sms);
			tmp.id=ID_SMS;
			tmp.img=R.drawable.i_sms;
			data.add(tmp);
		}
		if(AccountsManager.countEmailAccounts()>0) {
			List<Account> emailaccs = AccountsDb.getAllEmailAccounts();
			for(Account acc: emailaccs) {
				
				ViewHolder tmp = new ViewHolder();
				tmp.account=acc;
				tmp.title=activity.getString(R.string.title_email)+": "+acc.getString(Account.STRING_EMAIL_ADDRESS);
				tmp.id=ID_EMAIL;
				tmp.img=R.drawable.i_email;
				data.add(tmp);
			}
			
		}
		ViewHolder tmp = new ViewHolder();
		tmp.title=activity.getString(R.string.title_notes);
		tmp.id=ID_NOTE;
		tmp.img=R.drawable.i_note;
		data.add(tmp);

        ViewHolder ctmp = new ViewHolder();
        ctmp.title=activity.getString(R.string.label_camera);
        ctmp.id=ID_CAMERA;
        ctmp.img=R.drawable.device_access_camera;
        data.add(ctmp);
		

        
    }

    public int getCount() {
        return data.size();
    }
    
    public Object getItem(int position) {
        return position;
    }
 
    public long getItemId(int position) {
        return position;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if (vi == null) {
        	vi = inflater.inflate(R.layout.action_new_list_item, null);
        }
        ImageView img = (ImageView) vi.findViewById(R.id.new_action_icon);
        TextView title = (TextView) vi.findViewById(R.id.new_action_title);

        B.addStyle(title);

        ViewHolder tmp = data.get(position);
        if(tmp!=null) {
        	title.setText(tmp.title);
        	img.setImageDrawable(activity.getResources().getDrawable(tmp.img));
        }
        return vi;
    }
 
    public void goItem(int position) {
    	ViewHolder tmp = data.get(position);
    	if(tmp!=null) {
    		
    		//State.sectionsGoBackstack();
    		
	    	
	    	StateObject sob = new StateObject(StateObject.INT_FORCE_NEW,1);
	    	switch(tmp.id) {
	    		case ID_PHONE:
	    			//State.clearStateObjects(State.SECTION_);
	    			BriefActivityManager.openPhone(activity);
	    			break;
	    		case ID_SMS:
	    			State.clearStateObjects(State.SECTION_SMS_SEND);
	    			State.addToState(State.SECTION_SMS_SEND,sob);
	    			Bgo.openFragmentBackStack(activity, SmsSendFragment.class);
	    			break;
	    		case ID_EMAIL:
	    			State.clearStateObjects(State.SECTION_EMAIL_NEW);
					State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.LONG_USE_ACCOUNT_ID,tmp.account.getLong(Account.LONG_ID)));
					
					State.addToState(State.SECTION_EMAIL_NEW,sob);
					Bgo.openFragmentBackStack(activity,EmailSendFragment.class);
	    			break;
	    		case ID_NOTE:
                    State.clearStateObjects(State.SECTION_NOTES_ITEM);
                    State.addToState(State.SECTION_NOTES_ITEM,sob);
                    Bgo.openFragmentBackStack(activity,NotesEditFragment.class);
                    break;
                case ID_CAMERA:
                    Bgo.openFragmentBackStackAnimate(activity, CameraFragment.class);
                    break;
	    	}
	    	
    	}
    }
    
    private static final int ID_PHONE=0;
    private static final int ID_SMS=1;
    private static final int ID_EMAIL=2;
    private static final int ID_NOTE=3;
    private static final int ID_CAMERA=4;

    
    private class ViewHolder {
        String title;
        int id;
        int img;
        Account account;
    }
}
