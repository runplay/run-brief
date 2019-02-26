package run.brief;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import run.brief.b.ActionBarManager;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.State;
import run.brief.menu.BriefMenu;


public class NewActionFragment extends BFragment implements BRefreshable {

	private View view;
	private ListView actions;
	private NewActionAdapter adapter;
    private Activity activity;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        this.activity=getActivity();
		this.view=inflater.inflate(R.layout.action_new,container, false);
		return view;
	}
	@Override
	public void onResume() {
		super.onResume();
        BriefMenu.ensureMenuOff();
        BriefManager.clearController(activity);
		//State.clearStateObjects();State.sectionsClearBackstack();
		State.setCurrentSection(State.SECTION_NEW_ACTION);
		//State.setCurrentSection(State.SECTION_BRIEF);

		refresh();
	}
	public void refreshData() {
		
	}
	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			adapter.goItem(position);

		}
	};

	public void refresh() {
		//BLog.e("NAF", "new action frag");
		BriefMenu.hideMenu();
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.title_new_action),R.menu.basic,R.color.actionbar_general);
		/*
		amb = new ActionModeBack(activity, activity.getResources().getString(R.string.title_new_action)
				,R.menu.basic
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
*/

		
		actions = (ListView) view.findViewById(R.id.new_action_list);
		adapter = new NewActionAdapter(getActivity());
		actions.setAdapter(adapter);
		actions.setOnItemClickListener(openListener);
	}
}
