package run.brief.util.explore;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import run.brief.HomeFarm;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.bButton;
import run.brief.b.fab.Fab;
import run.brief.secure.delete.SecureDeleteFile;
import run.brief.util.PlusMember;
import run.brief.util.explore.fm.FileManagerDisk;



public class FilesDeleteFragment extends BFragment implements BRefreshable {
	private View view;
	private LinearLayout options;
	private ViewGroup container;
	private LayoutInflater inflater;

	private Activity activity=null;
	private FileExploreSelectedFilesAdapter adapter;
	private ListView list;
	private RelativeLayout head;
	private View upgrade;
	private LinearLayout deleteing;
	private TextView progressText;
	private LinearLayout completed;
	private Runnable completedp;

    private Button deletenow;
    //bButton cancel = (bButton) view.findViewById(R.id.file_explore_delete_cancel);
    private Button safeDelete;

	private FileManagerDisk fm =null;

	private Handler deleteHandler = new Handler();
	//private ActionModeBack amb;
	//private static final int OPTIONS_WITH=150;


	//private File path = new File(Environment.getExternalStorageDirectory() + "");


	@Override
	public void onPause() {
		super.onPause();


	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.container=container;
		this.inflater=inflater;
		this.activity=getActivity();

		fm=(FileManagerDisk) State.getCachedFileManager(FileManagerDisk.class);

		view=inflater.inflate(R.layout.file_explore_delete,container, false);
		//fileExplorerHandler.postDelayed(fileExplorerRunner, 10);
		return view;


	}
	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_FILE_EXPLORE_DELETE);


        head = (RelativeLayout) view.findViewById(R.id.files_header);
        upgrade = PlusMember.getPlusMemberUpgradeView(activity,view,cancelUpgradeListener); //(RelativeLayout) view.findViewById(R.id.file_explore_delete_upgrade_message);
        deleteing = (LinearLayout) view.findViewById(R.id.file_explore_deleteing);
        completed = (LinearLayout) view.findViewById(R.id.file_explore_deleteing_completed);
        progressText = (TextView) view.findViewById(R.id.file_explore_progress_text);
		safeDelete = (Button) view.findViewById(R.id.file_explore_safe_delete_now);
        deletenow = (Button) view.findViewById(R.id.file_explore_delete_now);

		B.addStyle(progressText,safeDelete,deletenow,progressText);

		refresh();
	}
	public void refresh() {
		/*
		amb = new ActionModeBack(getActivity(), getActivity().getString(R.string.label_delete)
				,R.menu.basic
				, new run.brief.b.ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),getActivity().getString(R.string.label_delete),R.menu.basic,R.color.browse_brand);


		refreshData();
		Fab.hide();
	}
	public void refreshData() {


        deletenow.setOnClickListener(deleteNowListener);
		safeDelete.setOnClickListener(safeDeleteListener);

		list=(ListView) view.findViewById(R.id.delete_files_list);
		adapter=new FileExploreSelectedFilesAdapter(getActivity(),fm);

        list.setAdapter(adapter);


		head.setVisibility(View.VISIBLE);
		list.setVisibility(View.VISIBLE);
		upgrade.setVisibility(View.GONE);
		deleteing.setVisibility(View.GONE);
		completed.setVisibility(View.GONE);
	}



	public OnClickListener deleteNowListener = new OnClickListener() {
		@Override
		public void onClick(View view) {

			ArrayList<FileItem> fileitems = adapter.getSelectedFiles();
			if(fileitems!=null && !fileitems.isEmpty()) {
				for(FileItem f: fileitems) {
					((File) f).delete();
				}
			}
			deleteHandler.postDelayed(completedReturnPrevious, 1000);
		}
	};

	public OnClickListener safeDeleteListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(!HomeFarm.isSubscriber()) {
				head.setVisibility(View.GONE);
				list.setVisibility(View.GONE);
				upgrade.setVisibility(View.VISIBLE);
				deleteing.setVisibility(View.GONE);
				completed.setVisibility(View.GONE);
			} else {
				head.setVisibility(View.GONE);
				list.setVisibility(View.GONE);
				upgrade.setVisibility(View.GONE);
				deleteing.setVisibility(View.VISIBLE);
				completed.setVisibility(View.GONE);
				progressText.setText(activity.getResources().getString(R.string.files_delete_safe_now));
				new SafeDeleteFiles().execute(true);
			}
		}
	};
	public OnClickListener cancelUpgradeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {

			head.setVisibility(View.VISIBLE);
			list.setVisibility(View.VISIBLE);
			upgrade.setVisibility(View.GONE);
			deleteing.setVisibility(View.GONE);
			completed.setVisibility(View.GONE);
		}
	};
	public OnClickListener returnPreviousListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			goPreviousNow();
		}
	};
	private void goPreviousNow() {
		head.setVisibility(View.GONE);
		list.setVisibility(View.GONE);
		upgrade.setVisibility(View.GONE);
		deleteing.setVisibility(View.GONE);
		completed.setVisibility(View.VISIBLE);
		fm.getSelectedFiles().clear();

		Bgo.goPreviousFragment(activity);
	}
 	private Runnable completedReturnPrevious = new Runnable() {
		public void run() {
			goPreviousNow();
		}
	};
	private class SafeDeleteFiles extends AsyncTask<Boolean, Void, Boolean> {

		ArrayList<FileItem> okdelete=new ArrayList<FileItem>();
		ArrayList<FileItem> nodelete=new ArrayList<FileItem>();

		public SafeDeleteFiles() {
			// TODO Auto-generated constructor stub
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			boolean completedOK=true;
			ArrayList<FileItem> fileitems = adapter.getSelectedFiles();
			if(fileitems!=null && !fileitems.isEmpty()) {
				for(FileItem f: fileitems) {
					if(SecureDeleteFile.delete((File) f)) {
						okdelete.add(f);
					} else {
						nodelete.add(f);
					}
				}
			}

			return true;

		}

		@Override
		protected void onPostExecute(Boolean result) {
			head.setVisibility(View.GONE);
			list.setVisibility(View.GONE);
			upgrade.setVisibility(View.GONE);
			deleteing.setVisibility(View.GONE);
			completed.setVisibility(View.VISIBLE);

			bButton b = (bButton) view.findViewById(R.id.file_explore_complete_button);
			b.setOnClickListener(returnPreviousListener);

			TextView comp = (TextView) view.findViewById(R.id.file_explore_complete_text_details);

			StringBuilder sb = new StringBuilder();
			if(nodelete.isEmpty()) {
				sb.append(activity.getResources().getString(R.string.files_delete_all_ok));
			} else {
				sb.append(activity.getResources().getString(R.string.files_delete_error));
				sb.append("\n");
				for(FileItem f: adapter.getSelectedFiles()) {
					sb.append("\n");
					sb.append(f.getAbsolutePath());
				}
			}
			comp.setTag(sb.toString());
			deleteHandler.postDelayed(completedReturnPrevious, 3000);

		}

		@Override
		protected void onPreExecute() {
		}


	}
}