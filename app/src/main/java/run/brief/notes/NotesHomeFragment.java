package run.brief.notes;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Note;
import run.brief.menu.BriefMenu;

public class NotesHomeFragment extends BFragment implements BRefreshable {
	
	private View view;
	private static NotesHomeFragment thisFragment;
	private ListView list;
	private NotesAdapter adapter;
	private Activity activity;
    private NotesDialog popupMenu;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.activity=getActivity();
		NotesDb.init(getActivity());
		
		view=inflater.inflate(R.layout.notes,container, false);
		thisFragment=this;
		return view;

	}
    @Override
    public void onPause() {
        super.onPause();
        if(popupMenu!=null) {
            popupMenu.cancel();
            popupMenu=null;
        }
        B.removeGoTopTracker();
        //ViewManagerText.clearManager();
    }
	@Override
	public void onResume() {
		super.onResume();
		BriefMenu.ensureMenuOff();
		BriefManager.activateController(activity);
		Bgo.clearBackStack(activity);
		//State.sectionsClearBackstack();
		State.setCurrentSection(State.SECTION_NOTES);
		
		list=(ListView) view.findViewById(R.id.notes_list);


		list.setEmptyView(view.findViewById(R.id.empty_notes));

        TextView tvh = (TextView) view.findViewById(R.id.notes_empty_head);
		TextView tv = (TextView) view.findViewById(R.id.notes_empty_new);
		tv.setClickable(true);
		tv.setOnClickListener(openNewListener);
		//BriefManager.activateController(getActivity());
		//BLog.e("notes", "Notes RESUME is called");
        B.addStyle(tv);
        B.addStyle(tvh);
        B.addGoTopTracker(activity,list,R.drawable.gt_notes);
		refresh();
	}
	public void refresh() {
		thisFragment=this;
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.label_note),R.menu.notes,R.color.actionbar_notes);
		/*
		amb = new ActionModeBack(activity, activity.getString(R.string.label_note)
				,R.menu.notes
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
*/
		
		displayNotes();
		//BLog.e("notes", "Notes refresh is called");
	}
	public void refreshData() {
		
	}
	@Override
	public boolean onOptionsItemSelected (MenuItem item) {
		switch(item.getItemId()) {
			case R.id.action_notes_add_new:
				openNewNote();
				break;
		}	
		return false;
	}
	private void openNewNote() {
		State.clearStateObjects(State.SECTION_NOTES_ITEM);
		StateObject sob = new StateObject(StateObject.INT_FORCE_NEW, 1);
		State.addToState(State.SECTION_NOTES_ITEM,sob);
		Bgo.openFragmentBackStack(activity, NotesEditFragment.class);
	}
	private void displayNotes() {
		
		

		adapter=new NotesAdapter(getActivity());
        list.setAdapter(adapter);
        list.setOnItemClickListener(openListener);
        list.setOnItemLongClickListener(onLongClick);
	}
	public OnItemLongClickListener onLongClick = new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			//BriefMenu.showPreview(position);

			Note t = NotesDb.getByIndex(position);
			
			if(t !=null) {
				
				popupMenu = new NotesDialog(getActivity(),t,thisFragment);

				popupMenu.show();
				popupMenu.setOnDismissListener(new OnDismissListener() {
					@Override
					public void onDismiss(DialogInterface intf) {
						//BLog.e("DISMISS", "Called");
						if(NotesDialog.shouldRefresh)
							refresh();
					}
				});
			}
			
			return true;
			
		}
	};
	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			//BriefMenu.showPreview(position);
			State.clearStateObjects(State.SECTION_NOTES_ITEM);
			StateObject sob=new StateObject(StateObject.INT_USE_SELECTED_INDEX,position);
			State.addToState(State.SECTION_NOTES_ITEM,sob);
			
		    Bgo.openFragmentBackStack(getActivity(), NotesEditFragment.class);
			
			
		}
	};
	public OnClickListener openNewListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			openNewNote();
			
			
		}
	};
}
