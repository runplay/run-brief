package run.brief.util.explore;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.fab.Fab;
import run.brief.util.BriefActivityManager;
import run.brief.util.Files;
import run.brief.util.explore.fm.FileManagerList;

public class ImagesSliderFragment extends BFragment implements BRefreshable {
	private View view;

	private ViewGroup container;
	private LayoutInflater inflater;
	
	private Activity activity;
	private FileManagerList fm;
	private ImageSliderPager pager;
	//private Toolbar toolbar;
    private List<FileItem> imgfiles;

    private ImageView btnBack;
    private ImageView btnShare;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		this.container=container;
		this.inflater=inflater;
		this.activity=getActivity();

		
		view=inflater.inflate(R.layout.images_slider,container, false);

		//toolbar = (Toolbar) activity.findViewById(R.id.toolbar);

		//fileExplorerHandler.postDelayed(fileExplorerRunner, 10);
		return view;
		

	}
	@Override
	public void onResume() {
		super.onResume();
		Fab.hide();
		/*
		amb = new ActionModeBack(getActivity(), getActivity().getString(R.string.choose_folder)
				,R.menu.basic
				, new run.brief.b.ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
*/
		//ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),R.color.actionbar_basic);
		ActionBarManager.setActionBarBackOnlyTransparent50NoUnderlay(activity,getActivity().getString(R.string.choose_folder),R.menu.basic);

		fm=(FileManagerList)State.getCachedFileManager(FileManagerList.class);
		if(fm==null) {
			//fm = new FileManagerDisk(State.getStateObjectString(State.SECTION_IMAGES_SLIDER, StateObject.STRING_FILE_PATH));
			fm.setStartAtPosition(State.getStateObjectInt(State.SECTION_IMAGES_SLIDER, StateObject.INT_VALUE));
		}
		State.setCurrentSection(State.SECTION_IMAGES_SLIDER);

		imgfiles=fm.getDirectory(activity);
		for(int i=imgfiles.size()-1; i>=0; i--) {
			FileItem imfile= imgfiles.get(i);
			if(!Files.isImage(Files.removeBriefFileExtension(imfile.getName())))
				imgfiles.remove(i);
		}
		WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);



		pager = (ImageSliderPager) view.findViewById(R.id.pager);

		pager.setViewWidth(size.x);
		pager.setOffscreenPageLimit(0);
		pager.setAdapter(new ImageSliderAdapter(activity, imgfiles));

        pager.setCurrentItem(fm.getStartAtPosition());


		refresh();
	}
	public void onPause() {
		super.onPause();
//		State.addToState(State.SECTION_IMAGES_SLIDER,new StateObject(StateObject.STRING_FILE_PATH,fm.getCurrentDirectory().getAbsolutePath()));
		State.addToState(State.SECTION_IMAGES_SLIDER,new StateObject(StateObject.INT_VALUE,fm.getStartAtPosition()));

        pager.removeAllViews();
        pager=null;

	}
	public void refresh() {

		//ActionBarManager.hide(activity);

		refreshData();

	}


	public void refreshData() {


	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		boolean intercept=false;
		switch(item.getItemId()) {
			case R.id.action_share:
				int pos = pager.getCurrentItem();
				File fi = fm.getDirectoryItemAsFile(pos);
				if (fi != null) {
					//this.done();
					BriefActivityManager.shareExternalFile(activity, fi.getAbsolutePath());
				}
				intercept=true;
				break;

		}

		return intercept;
	}


}