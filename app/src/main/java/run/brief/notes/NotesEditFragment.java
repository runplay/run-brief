package run.brief.notes;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.Device;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.bButton;
import run.brief.b.bEditText;
import run.brief.b.bImageButton;
import run.brief.beans.Note;
import run.brief.util.Cal;
import run.brief.util.Sf;
import run.brief.util.ViewManagerText;
import run.brief.util.eicon.EmoticonsGridAdapter.KeyClickListener;
import run.brief.util.explore.FileExploreFragment;
import run.brief.util.explore.FilePopListener;
import run.brief.util.explore.FilesAdapter;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;
//import run.brief.util.sound.SoundRecorder;

public class NotesEditFragment extends BFragment implements BRefreshable,KeyClickListener {
	
	private Activity activity;
	private View view;
	//private ViewGroup container;
	//private LayoutInflater inflater;
	private static Note note=new Note();
	
	private static bEditText noteText;
	private ViewManagerText manager;
	
	private static GridView list;
	private static FilesAdapter adapter;
	
	private bButton btnSave;
	private bButton btnUpdate;
	private bButton btnFile;
	private bButton btnRecord;
	//private bButton btnCamera;
	private bButton btnAlert;
	private TextView alertText;
	private bImageButton smsSmile;
	private static boolean isEditMode=false;

    private FilePopListener listen;

	public void clear() {
		note=new Note();
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		this.activity=getActivity();
		NotesDb.init(getActivity());
		view=inflater.inflate(R.layout.notes_item,container, false);
		
		return view;
	}
	public void refreshData() {
		
	}
	public void keyClickedIndex(final String index) {
		
		int cursorPosition = noteText.getSelectionStart();		
		noteText.getText().insert(cursorPosition, index);
        
	}
	public void refresh() {
		//ArrayList<StateObject> states = State.getState();
		//BLog.e("NTE", "1");
		BriefManager.clearController(activity);
		if(State.hasStateObjects(State.SECTION_NOTES_ITEM)) {
			//BLog.e("NTE", "2");
			if(State.hasStateObject(State.SECTION_NOTES_ITEM,StateObject.STRING_BJSON_OBJECT)) {
				
				JSONObject job = new JSONObject(State.getStateObjectString(State.SECTION_NOTES_ITEM,StateObject.STRING_BJSON_OBJECT));
				if(job!=null) {
					
					note = new Note(job);
					//BLog.e("NOTE", "STRING_BJSON_OBJECT: " + note.getInt(Note.INT_ID) + " -- ");
					if(note.getInt(Note.INT_ID)!=0)
						isEditMode=true;
					else
						isEditMode=false;
					poulatedFieldsFromNote();
				}
				
			}
			//for(StateObject state: State.getState(State.SECTION_NOTES_ITEM)) {
				//BLog.e("NTE", "--"+state.getName()+" = "+state.getObjectAsString());
            if(State.hasStateObject(State.SECTION_NOTES_ITEM,StateObject.INT_FORCE_NEW)) {
                //isEditMode=true;
                //int selectedindex = state.getObjectAsInt();
                //BLog.e("NOTE", "force new");
                note=new Note();
                isEditMode=false;
                //noteText.setText(note.getString(Note.STRING_TEXT));

            }
            if(State.hasStateObject(State.SECTION_NOTES_ITEM,StateObject.STRING_USE_DATABASE_ID)) {
                isEditMode=true;
                //int selectedindex = state.getObjectAsInt();
                //BLog.e("NOTE", "with dbid");
                note=NotesDb.getById(Sf.toInt(State.getStateObjectString(State.SECTION_NOTES_ITEM,StateObject.STRING_USE_DATABASE_ID)));
                noteText.setText(note.getString(Note.STRING_TEXT));
                //isEditMode=false;
                //noteText.setText(note.getString(Note.STRING_TEXT));

            } else  if(State.hasStateObject(State.SECTION_NOTES_ITEM,StateObject.INT_USE_SELECTED_INDEX)) {
                //BLog.e("NOTE", "with selindex");
                isEditMode=true;
                int selectedindex = State.getStateObjectInt(State.SECTION_NOTES_ITEM,StateObject.INT_USE_SELECTED_INDEX);
                note=NotesDb.getByIndex(selectedindex);

                noteText.setText(note.getString(Note.STRING_TEXT));

		    }
			//}
            if(State.hasStateObject(State.SECTION_FILE_EXPLORE,StateObject.STRING_FILE_PATH)) {
                //BLog.e("NOTE", "has files");
				try {
					JSONArray jarr = new JSONArray(State.getStateObjectString(State.SECTION_FILE_EXPLORE, StateObject.STRING_BJSON_ARRAY));
					for (int i = 0; i < jarr.length(); i++) {
						//BLog.e("NTE", jarr.getString(i));
						note.addFile(jarr.getString(i));
					}
					//BLog.e("NTE","has files: "+jarr.length());
					State.clearStateObject(State.SECTION_FILE_EXPLORE,StateObject.STRING_BJSON_ARRAY);
				} catch(Exception e) {}
                //showAttachedFiles();
            }
			if(State.hasStateObject(State.SECTION_NOTES_ITEM,StateObject.INT_TEST_SELECT_START)) {
				//BLog.e("CMDD", "selection start "+State.getStateObjectInt(StateObject.INT_TEST_SELECT_START));
				noteText.setSelection(State.getStateObjectInt(State.SECTION_NOTES_ITEM,StateObject.INT_TEST_SELECT_START));
				
			}
			//State.clearState();
		} else {
			poulatedFieldsFromNote();
		}
		State.clearStateObjects(State.SECTION_NOTES_ITEM);
		/*
		amb = new ActionModeBack(activity, activity.getString(R.string.title_notes)
				,R.menu.notes_new
				, new ActionModeCallback() {
			@Override
			public void onActionMenuItem(ActionMode mode, MenuItem item) {
				onOptionsItemSelected(item);
			}
		});
		*/
		ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getString(R.string.title_notes),R.menu.notes_new,R.color.actionbar_notes);

    	
		if(isEditMode) {
			btnSave.setVisibility(View.GONE);
			btnUpdate.setVisibility(View.VISIBLE);
		} else {
            Device.setKeyboard(activity,noteText,true);
            btnSave.setVisibility(View.VISIBLE);
			btnUpdate.setVisibility(View.GONE);
		}
		showAttachedFiles();
		
	}
	@Override
    public void onResume() {
    	super.onResume();

    	State.setCurrentSection(State.SECTION_NOTES_ITEM);

		//btnSave.setVisibility(View.GONE);
		//btnFile.setVisibility(View.GONE);

    	noteText = (bEditText) view.findViewById(R.id.note_item_text);
    	noteText.requestFocus();
    	noteText.setOnClickListener(messageBoxListner);


        B.addStyle(noteText,B.FONT_LARGE);

    	btnSave = (bButton) view.findViewById(R.id.notes_item_save);
    	btnSave.setOnClickListener(saveListner);
    	//B.addStyle(btnSave);
    	btnUpdate = (bButton) view.findViewById(R.id.notes_item_update);
    	btnUpdate.setOnClickListener(updateListner);
    	
    	btnFile = (bButton) view.findViewById(R.id.notes_item_file);
    	btnFile.setOnClickListener(addFileListner);
    	
    	
    	btnRecord = (bButton) view.findViewById(R.id.notes_item_voice);
    	btnRecord.setOnClickListener(addRecordListner);
    	//btnCamera = (bButton) activity.findViewById(R.id.notes_item_photo);
    	//btnCamera.setOnClickListener(useCameraListner);
    	
    	btnAlert = (bButton) view.findViewById(R.id.notes_set_alert);
    	btnAlert.setOnClickListener(setAlertListner);
    	
    	alertText = (TextView) view.findViewById(R.id.notes_alert_text);

		smsSmile = (bImageButton) view.findViewById(R.id.sms_i_smile);

		
		list=(GridView) view.findViewById(R.id.notes_files);
		
		//noteText.removeTextChangedListener(watcher);
		manager=null;
		refresh();
		
		manager = new ViewManagerText();
		manager.manageEditText(activity, noteText,this);
        //ViewManagerText.enableStatics();
        noteText.setOnEditTextImeBackListener(new bEditText.EditTextImeBackListener() {
            public void onImeBack() {
                manager.dismissPopup();
                smsSmile.setImageDrawable(activity.getResources().getDrawable(R.drawable.i_smile));
            }
        });
        manager.addClickListneerOpenEmoji(view,noteText,smsSmile);
        //smsSmile.setOnClickListener(manager.getSmilPopupListener(activity,view,noteText,smsSmile));
    }
	@Override
    public void onStop() {
		super.onStop();
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		poulatedNoteFromFields();
        if(manager!=null)
            manager.dismissPopup();
		//BLog.e("NOTE", "onPause");
		State.addToState(State.SECTION_NOTES_ITEM,new StateObject(StateObject.STRING_BJSON_OBJECT,note.toString()));
    	int lenStart=noteText.getSelectionStart();
    	//int lenEnd=noteText.length(); //editField.getSelectionEnd();
		State.addToState(State.SECTION_NOTES_ITEM,new StateObject(StateObject.INT_TEST_SELECT_START,lenStart));
		//State.addToState(new StateObject(StateObject.INT_TEST_SELECT_END,lenEnd));
	}
	protected OnClickListener messageBoxListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			if(manager.getPopupWindow()!=null)
                manager.getPopupWindow().dismiss();
			smsSmile.setImageDrawable(activity.getResources().getDrawable(R.drawable.i_smile));
		}
	};	
	private void poulatedNoteFromFields() {
		//EditText txt = (EditText) activity.findViewById(R.id.note_item_text);
		note.setString(Note.STRING_TEXT,noteText.getText().toString()); 
	}
	private void poulatedFieldsFromNote() {
		if(note!=null) {
			//EditText txt = (EditText) activity.findViewById(R.id.note_item_text);
			
			noteText.setText(note.getString(Note.STRING_TEXT));
			//BLog.e("NOTE", "set text: "+note.getString(Note.STRING_TEXT).replaceAll("\n", "-"));
			//note.setString(Note.STRING_TEXT,txt.getText().toString()); 
		}
	}
	protected OnClickListener saveListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			poulatedNoteFromFields();

			
			if(!note.isEmpty()) {
				note.setLong(Note.LONG_DATE_CREATED, Cal.getUnixTime());
				
				long id=NotesDb.add(note);
				//BLog.e("NOTEADD", "with id: "+id);
				BriefManager.setDirty(BriefManager.IS_DIRTY_NOTES);
				clear();
				Bgo.goPreviousFragment(activity);
				//Bgo.openFragment(activity,new NotesHomeFragment());
			} else {
				Toast.makeText(activity, R.string.note_nothing_save, Toast.LENGTH_SHORT).show();
			}
			
		}
	};	
	protected OnClickListener updateListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			EditText txt = (EditText) activity.findViewById(R.id.note_item_text);
			note.setString(Note.STRING_TEXT,txt.getText().toString()); 
			
			if(!note.isEmpty()) {
				note.setLong(Note.LONG_DATE_CREATED, Cal.getUnixTime());
				
				NotesDb.update(note);
				BriefManager.setDirty(BriefManager.IS_DIRTY_NOTES);
				clear();
				Bgo.goPreviousFragment(activity);
				//Bgo.openFragment(activity,new NotesHomeFragment());
			} else {
				Toast.makeText(activity, R.string.note_nothing_update, Toast.LENGTH_SHORT).show();
			}
			
		}
	};	
	protected OnClickListener addFileListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			State.setFileExploreState(State.FILE_EXPLORE_STATE_SELECTFILE);
			Bgo.openFragmentBackStack(activity,FileExploreFragment.class);
			
		}
	};	
	protected OnClickListener addRecordListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//State.setFileExploreState(State.FILE_EXPLORE_STATE_SELECTFILE);
			//Bgo.openFragmentBackStack(activity,new SoundRecorder());
			
		}
	};	
	protected OnClickListener setAlertListner = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//State.setFileExploreState(State.FILE_EXPLORE_STATE_SELECTFILE);
			//Bgo.openFragmentBackStack(activity,new FileExploreFragment());
			
		}
	};	
	
	public void showAttachedFiles() {
		
		//list.setClickable(true);
		//list.setOnItemClickListener(openListener);
		//BLog.e("FILES", "showing: "+note.getFiles().size());
        listen=new PopListner();
		adapter=new FilesAdapter(getActivity(),note.getFiles(),FilesAdapter.TYPE_EDIT,listen);
        list.setAdapter(adapter);
		//adapter.notifyDataSetChanged();
	}

    public class PopListner extends FilePopListener {
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if(menuItem.getTitle().equals(activity.getResources().getString(R.string.label_open))) {
                File f = new File(adapter.selectedFile);
                Device.openFile(activity, f);

            } else {
                    JSONArray files=note.getJSONArray(Note.JSONARRAY_FILES);
                    for(int i=0; i<files.length(); i++) {
                        if(files.get(i).equals(adapter.selectedFile)) {
                            files.remove(i);
                            break;
                        }
                    }
                    note.setJSONArray(Note.JSONARRAY_FILES, files);
                    showAttachedFiles();

            }
            return false;
        }
    }
}
