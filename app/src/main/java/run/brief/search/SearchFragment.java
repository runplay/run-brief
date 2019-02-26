package run.brief.search;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BCallback;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.beans.SearchResult;
import run.brief.menu.BriefMenu;

public class SearchFragment extends BFragment implements BRefreshable {
	private View view;

	private Activity activity=null;
	private EditText searchText;
	private static SearchAdapter adapter;
	private ListView list;
	View updating;
	View start;

    @Override
    public void onPause() {
        super.onPause();

    }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		activity=getActivity();
		view=inflater.inflate(R.layout.search,container, false);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		State.setCurrentSection(State.SECTION_SEARCH);
		start = (View) view.findViewById(R.id.search_start);
		updating = (View) view.findViewById(R.id.search_updating);
		

        list=(ListView) activity.findViewById(R.id.search_list);

		refresh();
		

	}
	public void refreshData() {
		
	}
	public void refresh() {
		BriefMenu.hideMenu();
		/*
		amb = new ActionModeBack(activity, activity.getString(R.string.label_search)
				,R.menu.basic
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
		*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.label_search),R.menu.basic,R.color.brand);


		adapter=new SearchAdapter(getActivity()); 
        list.setAdapter(adapter);
        list.setOnItemClickListener(openListener);
			
        run.brief.b.bButton searchnow=(run.brief.b.bButton) activity.findViewById(R.id.search_btn);
        searchnow.setClickable(true);
        searchnow.setOnClickListener(newSearchListener);
        
		searchText=(EditText) activity.findViewById(R.id.search_text);
        B.addStyle(searchText);

		Device.setKeyboard(activity,searchText, true);
		//view.requestFocus();
	}
	private BCallback searchCallback = new BCallback() {
		@Override
		public void callback() {
			// do it
			updating.setVisibility(View.GONE);
			refresh();
		}
	};
		
	
	public OnClickListener newSearchListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			start.setVisibility(View.GONE);
			updating.setVisibility(View.VISIBLE);
			Searcher.doSearch(activity,searchText.getText().toString(),searchCallback);

			list.setAdapter(new SearchAdapter(getActivity()));
			Device.hideKeyboard(activity);
		}
	};
	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            SearchResult sr = Searcher.get(position);
            //BLog.e("SSr","sr: "+sr.getDBid());
            BriefManager.openBriefItem(activity,sr,true);

		}
	};
}
