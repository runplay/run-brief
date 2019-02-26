package run.brief.email;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Account;
import run.brief.settings.AccountsDb;

public class EmailFoldersFragment extends BFragment implements BRefreshable {
	
	//private Handler contactsHandler = new Handler();
	//private static ArrayList<Person> contacts=null;
	//ImapService imap;

	private View view;
	private Activity activity;
	
	private static ListView list;
    Account account;
	
	//private static Account acc;
	private static EmailServiceInstance es;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		//this.setHasOptionsMenu(true);
		AccountsDb.init();
		activity=getActivity();
		view=inflater.inflate(R.layout.email_folders,container, false);
		//es=EmailService.getLastCalledService();
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		
		State.setCurrentSection(State.SECTION_EMAIL_FOLDER);

		if(State.hasStateObject(State.SECTION_EMAIL_FOLDER,StateObject.LONG_USE_ACCOUNT_ID)) {
			long selectedAccid=State.getStateObjectLong(State.SECTION_EMAIL_FOLDER,StateObject.LONG_USE_ACCOUNT_ID);
			account = AccountsDb.getAccountById(selectedAccid);
			es=EmailService.getService(activity,account);
		}

		State.clearStateObjects(State.SECTION_EMAIL_FOLDER);
		refresh();
	}
	public void refreshData() {
		
	}
	public void refresh() {
		if(activity!=null && es!=null) {
			/*
			amb = new ActionModeBack(activity, activity.getResources().getString(R.string.label_email)
					,R.menu.basic
					, new ActionModeCallback() {
				@Override
				public void onActionMenuItem(ActionMode mode, MenuItem item) {
					onOptionsItemSelected(item);
				}
			});
			*/
			ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.label_email),R.menu.basic,R.color.actionbar_email);

			list=(ListView) view.findViewById(R.id.mail_folder_list);
			list.setAdapter(new EmailFoldersAdapter(activity,es.getAccount(),es.getLoadFolders()));
			list.setEmptyView(view.findViewById(R.id.email_folder_loading));
            if(loadTask==null) {
                loadTask = new LoadFoldersTask();
                loadTask.execute(true);
            }
		}
	}

    private LoadFoldersTask loadTask;
    private class LoadFoldersTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            if(es.getLoadFolders().isEmpty())
                es.loadFolders();
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            //mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            //mOrientation = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            refresh();
        }

    }


}
