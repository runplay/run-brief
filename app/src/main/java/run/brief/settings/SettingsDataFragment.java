package run.brief.settings;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.State;

public class SettingsDataFragment extends BFragment implements BRefreshable {
	private View view;

	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view=inflater.inflate(R.layout.settings_data,container, false);

		return view;

	}

	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_SETTINGS_DATA);
		





		refresh();
		  

	}
	public void refreshData() {
		
	}
	@Override
	public void refresh() {
		/*
		amb = new ActionModeBack(getActivity(), getActivity().getString(R.string.settings_data)
				,R.menu.basic
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
		*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),getActivity().getString(R.string.settings_data),R.menu.basic,R.color.brand);

	}	
	public OnClickListener onEmoCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {

			
		}
	};	



}
