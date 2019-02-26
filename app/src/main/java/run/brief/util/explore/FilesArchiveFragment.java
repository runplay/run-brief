package run.brief.util.explore;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.fab.Fab;
import run.brief.service.BriefService;
import run.brief.util.Zip;
import run.brief.util.explore.fm.FileManagerDisk;
import run.brief.util.json.JSONArray;

public class FilesArchiveFragment extends BFragment implements BRefreshable {
	private View view;
	private LinearLayout options;
	private ViewGroup container;
	private LayoutInflater inflater;
	
	private Activity activity=null;
	private FileExploreSelectedFilesAdapter adapter;
	private ListView list;
	private RelativeLayout head;

	private ImageView chooseFolder;


	private FileManagerDisk fm;//=new FileManager();
    private List<FileItem> stopfiles;
    private ArrayList<FileItem> selectedFiles=new ArrayList<FileItem>();
    private TextView zipfilename;
    //private TextView zipfileerror;
    //private String zipfolder;
    private TextView zipfolder;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		this.container=container;
		this.inflater=inflater;
		this.activity= getActivity();
		
		
		
		view=inflater.inflate(R.layout.file_explore_archive,container, false);
		//fileExplorerHandler.postDelayed(fileExplorerRunner, 10);
		return view;
		

	}
	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_FILE_CREATE_ARCHIVE);
		fm=(FileManagerDisk) State.getCachedFileManager(FileManagerDisk.class);
		if(fm==null) {
			fm = new FileManagerDisk(State.getStateObjectString(State.SECTION_FILE_CREATE_ARCHIVE, StateObject.STRING_FILE_PATH));
            String chahcedSelected=State.getStateObjectString(State.SECTION_FILE_CREATE_ARCHIVE, StateObject.STRING_BJSON_OBJECT);
            if(chahcedSelected!=null) {
                JSONArray jar = new JSONArray(chahcedSelected);
                if (jar.length() > 0) {
                    for (int i = 0; i < jar.length(); i++) {
                        fm.addSelectedFile(new FileItem(jar.getString(i)));
                    }
                }
            }
		}
		String changeFolder=State.getStateObjectString(State.SECTION_POP_FOLDER_CHOOSER, StateObject.STRING_VALUE);
		if(changeFolder!=null)
			fm.setCurrentDirectory(activity,changeFolder);

		State.clearStateObjects(State.SECTION_FILE_CREATE_ARCHIVE);
		State.clearStateObjects(State.SECTION_POP_FOLDER_CHOOSER);

		Fab.hide();
		chooseFolder=(ImageView) view.findViewById(R.id.file_explore_choose_folder);
		chooseFolder.setClickable(true);
		chooseFolder.setOnClickListener(changeFolderListner);
		refresh();
	}
	public void onPause() {
        State.addToState(State.SECTION_FILE_CREATE_ARCHIVE, new StateObject(StateObject.STRING_FILE_PATH, fm.getCurrentDirectory().getAbsolutePath()));
        State.addToState(State.SECTION_FILE_CREATE_ARCHIVE, new StateObject(StateObject.STRING_BJSON_OBJECT, fm.getSelectedFilesAsJSONArray().toString()));
		super.onPause();

	}
	public void refresh() {
/*		amb = new ActionModeBack(getActivity(), getActivity().getString(R.string.label_archive)
				,R.menu.basic
				, new run.brief.b.ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),getActivity().getString(R.string.label_archive),R.menu.basic,R.color.browse_brand);




		loadFileList();

	}
	public void refreshData() {
		
	}
	private void loadFileList() {
		head = (RelativeLayout) view.findViewById(R.id.files_header);
		TextView zipfileerror;
		Button archivenow = (Button) view.findViewById(R.id.file_explore_archive_now);
        zipfilename = (TextView) view.findViewById(R.id.zip_file_name);
        zipfileerror = (TextView) view.findViewById(R.id.zip_error);
        zipfolder = (TextView) view.findViewById(R.id.zip_folder);

		B.addStyle(zipfilename,zipfileerror,zipfolder);

        selectedFiles.clear();
        Iterator<String> it= fm.getSelectedFiles().keySet().iterator();
        while(it.hasNext()) {
            FileItem fi= fm.getSelectedFiles().get(it.next());
            if(fi!=null)
                selectedFiles.add(fi);
        }

		archivenow.setOnClickListener(archiveNowListener);

		list=(ListView) view.findViewById(R.id.archive_files_list);
        stopfiles = Zip.getStopZipFiles(selectedFiles);
		adapter=new FileExploreSelectedFilesAdapter(getActivity(),fm,stopfiles);
		
        list.setAdapter(adapter);

        if(zipfilename.getText().length()==0) {
            FileItem sfile=Zip.getSuggestedZipFile(selectedFiles);
            zipfilename.setText(sfile.getName().replaceAll(".zip",""));
            zipfolder.setText(sfile.getParentFile().getAbsolutePath());
        }

        if(!stopfiles.isEmpty()) {
            zipfileerror.setText(activity.getString(R.string.zip_duplicates));
            archivenow.setVisibility(View.GONE);
        } else {
            zipfileerror.setText("");
            archivenow.setVisibility(View.VISIBLE);
        }

		head.setVisibility(View.VISIBLE);
		list.setVisibility(View.VISIBLE);

	}

	private void goPreviousNow() {
		head.setVisibility(View.GONE);
		list.setVisibility(View.GONE);
		//deleteing.setVisibility(View.GONE);
		//completed.setVisibility(View.VISIBLE);
		fm.getSelectedFiles().clear();
        //Log.e("ARCHIVE","Finished ARCHIVE, goPreviousFragment()");
		Bgo.goPreviousFragment(activity);
	}
    public OnClickListener changeFolderListner = new OnClickListener() {
        @Override
        public void onClick(View v) {
            //Log.e("FOLDER","open popchoosefolder");

            State.addToState(State.SECTION_POP_FOLDER_CHOOSER, new StateObject(StateObject.STRING_FILE_PATH, fm.getCurrentDirectory().getAbsolutePath()));
            Bgo.openFragmentBackStack(activity, FolderChooseFragment.class);
        }
    };
	public OnClickListener archiveNowListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			Log.e("ARCHIVE","archive now clicked");

        if(stopfiles.isEmpty() && zipfolder.getText().length()>0 && zipfilename.getText().length()>0) {
            String fn = zipfilename.getText().toString().replaceAll(".zip","")+".zip";
            BriefService.ArchiveFiles(zipfolder.getText().toString(), fn, adapter.getSelectedFiles());
            goPreviousNow();
        }

			//deleteHandler.postDelayed(completedReturnPrevious, 1000);
		}
	};
	@Override
	public void onCreateOptionsMenu (Menu menu, MenuInflater inflater) {
		//Log.e("FE","FileExplore oncreateoptionsmenu");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.e("ARCHIVE", "onOptionsItemSelected()");
		boolean callrefresh=true;
		boolean intercept=true;
		switch(item.getItemId()) {


		}
		if(callrefresh)
			refreshData();
		return intercept;
	}
}