package run.brief.contacts;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import run.brief.BriefManager;
import run.brief.R;
import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.b.bToPersonsView;
import run.brief.beans.Person;
import run.brief.contacts.ContactsAdapter.Item;
import run.brief.contacts.ContactsAdapter.Row;
import run.brief.contacts.ContactsAdapter.Section;
import run.brief.service.BriefService;

public class ContactsHomeFragment extends BFragment implements BRefreshable {
	
	//private Handler contactsHandler = new Handler();
	//private static ArrayList<Person> contacts=null;
	
	private View view;
	private LayoutInflater inflater;
	private ViewGroup container;
	private ContactsAdapter adapter;
	private bToPersonsView personsView;
	private Activity activity;
	//private static HashMap<String,Person> to=new HashMap<String,Person>();
	//private static EditText addContactText;
	//private bImageButton addContactBtn;
	private LinearLayout contactSelectPod;
	private AsyncTask<Boolean, Void, Boolean> gdi;
	private ListView list;


    //private AlphabetListAdapter abetadapter = new AlphabetListAdapter();
    private GestureDetector mGestureDetector;
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private int sideIndexHeight;
    private static float sideIndexX;
    private static float sideIndexY;
    private int indexListSize;



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.container=container;
		this.inflater=inflater;

		activity=getActivity();
        ContactsDb.init(activity);
		view = inflater.inflate(R.layout.contacts,container, false);
		return view;
	}
	@Override
	public void onResume() {
		super.onResume();
        BriefManager.clearController(activity);
        State.setCurrentSection(State.SECTION_CONTACTS);

        mGestureDetector = new GestureDetector(activity, new SideIndexGestureListener());

        list=(ListView) view.findViewById(R.id.contacts_name_list);
        list.setEmptyView(inflater.inflate(R.layout.wait, container, false));


        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else {
                    return false;
                }
            }
        });


        contactSelectPod = (LinearLayout) view.findViewById(R.id.contacts_selected_pod);
        //bToPersonsView.setMode(bToPersonsView.MODE_VIEW_EDIT);
        if(personsView==null)
            personsView=(bToPersonsView) view.findViewById(R.id.contacts_selected);

		refresh();
	}
	public void refreshData() {
		
	}

	public void refresh() {


        //BLog.e("CCO","----"+(State.getContactsMode()==State.CONTACT_MODE_VIEW));


		contactSelectPod.setVisibility(View.VISIBLE);

		if(State.getContactsMode()==State.CONTACT_MODE_VIEW) {
/*
            amb = new ActionModeBack(activity, activity.getResources().getString(R.string.contacts)
                    ,R.menu.contacts
                    , new ActionModeCallback() {
                @Override
                public void onActionMenuItem(ActionMode mode, MenuItem item) {
                    onOptionsItemSelected(item);
                }
            });
            */
            ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.contacts),R.menu.contacts,R.color.brand);


			//contactSelectPod.setVisibility(View.GONE);
			//personsView.setVisibility(View.GONE);
		} else {
			
			/*
			switch(State.getContactsMode()) {
				case State.CONTACT_MODE_SELECT_EMAIL:
					addContactText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
					break;
				default:
					addContactText.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_CLASS_PHONE);
					break;
			}
			*/
            /*
            amb = new ActionModeBack(activity, activity.getResources().getString(R.string.contacts_select)
                    ,R.menu.basic
                    , new ActionModeCallback() {
                @Override
                public void onActionMenuItem(ActionMode mode, MenuItem item) {
                    onOptionsItemSelected(item);
                }
            });
            */
            ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(),activity.getResources().getString(R.string.contacts_select),R.menu.basic,R.color.actionbar_general);


		}
        int lastPos=list.getFirstVisiblePosition();
		//BLog.e("CHOME","contact home refresh");
        displayContacts();
        list.setSelection(lastPos);

        if(State.hasStateObject(State.SECTION_CONTACTS,StateObject.INT_LISTVIEW_FIRST_VIEWABLE)) {
            //BLog.e("CC","scrol: "+State.getStateObjectInt(State.SECTION_CONTACTS,StateObject.INT_LISTVIEW_FIRST_VIEWABLE));
            list.setSelection(State.getStateObjectInt(State.SECTION_CONTACTS,StateObject.INT_LISTVIEW_FIRST_VIEWABLE));

        }
	}
    private void displayContacts() {
        if(State.getContactsMode()==State.CONTACT_MODE_SELECT_EMAIL) {
            adapter=new ContactsAdapter(activity, ContactsDb.getContactsHasEmail());
        } else {
            adapter=new ContactsAdapter(activity, ContactsDb.getContactsAll());
        }
        //if(State.getContactsMode()==State.CONTACT_MODE_VIEW) {
        //    personsView.setMode(bToPersonsView.);
        //} else {
        personsView.setMode(bToPersonsView.MODE_VIEW_EDIT);
        //}


        personsView.setContext(activity);
        personsView.setMaxHeight(250);
        personsView.refreshData();

        list.setClickable(true);
        list.setOnItemClickListener(openListener);
        //adapter=new ContactsAdapter(activity, ContactsDb.getContactsAll());
        adapter.setRefeshableParent(this);
        list.invalidate();
        //list.setAdapter(adapter);


        List<Row> rows = new ArrayList<Row>();
        int start = 0;
        int end = 0;
        String previousLetter = null;
        Object[] tmpIndexItem = null;
        Pattern numberPattern = Pattern.compile("[0-9]");
        alphabet.clear();
        for (Person person : adapter.getData()) {

            String name = person.getString(Person.STRING_NAME).toUpperCase(Locale.getDefault());

            String firstLetter = name.substring(0, 1);

            // Group numbers together in the scroller
            if (numberPattern.matcher(firstLetter).matches()) {
                firstLetter = "#";
            }

            // If we've changed to a new letter, add the previous letter to the alphabet scroller
            if (previousLetter != null && !firstLetter.equals(previousLetter)) {
                end = rows.size() - 1;
                tmpIndexItem = new Object[3];
                tmpIndexItem[0] = previousLetter.toUpperCase(Locale.getDefault());
                tmpIndexItem[1] = start;
                tmpIndexItem[2] = end;
                alphabet.add(tmpIndexItem);

                start = end + 1;
            }

            // Check if we need to add a header row
            if (!firstLetter.equals(previousLetter)) {
                rows.add(new Section(firstLetter,null));
                sections.put(firstLetter, start);
            }

            // Add the country to the list
            rows.add(new Item(name,person));
            previousLetter = firstLetter;
        }

        if (previousLetter != null) {
            // Save the last letter
            tmpIndexItem = new Object[3];
            tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
            tmpIndexItem[1] = start;
            tmpIndexItem[2] = rows.size() - 1;
            alphabet.add(tmpIndexItem);
        }

        adapter.setRows(rows);
        list.setAdapter(adapter);
        //setListAdapter(abetadapter);

        updateList();

    }
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_contacts_refresh:
                ActionBarManager.setActionBarBackOnly(activity, activity.getResources().getString(R.string.contacts),R.menu.contacts);
                adapter.clearData();
                adapter.notifyDataSetInvalidated();
                list.removeAllViews();
                list.invalidate();
                gdi=new goDrawit().execute(true);
                break;
            case R.id.action_resync_contacts:
                if(!ContactsDb.isRefreshing()) {
                    resyncTask = new ResyncContactsTask();
                    resyncTask.execute(true);
                }
                break;
        }
        return false;
    }


    // alphabet scroller stuff
    private ResyncContactsTask resyncTask;
    private class ResyncContactsTask extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {
            ContactsDb.loadContactsFull(activity);
            ContactsDb.mapContactsFullToContacts(activity);

            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            refresh();
        }

    }



    public void updateList() {
        LinearLayout sideIndex = (LinearLayout) view.findViewById(R.id.sideIndex);
        sideIndex.removeAllViews();
        indexListSize = alphabet.size();
        if (indexListSize < 1) {
            return;
        }

        int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
        int tmpIndexListSize = indexListSize;
        while (tmpIndexListSize > indexMaxSize) {
            tmpIndexListSize = tmpIndexListSize / 2;
        }
        double delta;
        if (tmpIndexListSize > 0) {
            delta = indexListSize / tmpIndexListSize;
        } else {
            delta = 1;
        }

        TextView tmpTV=null;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);

        for (double i = 1; i <= indexListSize; i = i + delta) {
            Object[] tmpIndexItem = alphabet.get((int) i - 1);
            String tmpLetter = tmpIndexItem[0].toString();

            tmpTV =new TextView(activity);

            tmpTV.setText(tmpLetter);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextSize(15);

            tmpTV.setLayoutParams(params);
            B.addStyle(tmpTV);

            sideIndex.addView(tmpTV);
        }

        sideIndexHeight = sideIndex.getHeight();

        sideIndex.setOnTouchListener(new LinearLayout.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // now you know coordinates of touch
                sideIndexX = event.getX();
                sideIndexY = event.getY();

                // and can display a proper item it country list
                displayListItem();

                return false;
            }
        });
    }

    public void displayListItem() {
        LinearLayout sideIndex = (LinearLayout) view.findViewById(R.id.sideIndex);
        sideIndexHeight = sideIndex.getHeight();
        // compute number of pixels for every side index item
        double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

        // compute the item index for given event position belongs to
        int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

        // get the item (we can do it since we know item index)
        if (itemPosition < alphabet.size()) {
            Object[] indexItem = alphabet.get(itemPosition);
            int subitemPosition = sections.get(indexItem[0]);

            //ListView listView = (ListView) findViewById(android.R.id.list);
            list.setSelection(subitemPosition);
        }
    }

    class SideIndexGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            sideIndexX = sideIndexX - distanceX;
            sideIndexY = sideIndexY - distanceY;

            if (sideIndexX >= 0 && sideIndexY >= 0) {
                displayListItem();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    // end alphabet scroller stuff





	private class goDrawit extends AsyncTask<Boolean, Void, Boolean> {
		
		@Override
		protected Boolean doInBackground(Boolean... params) {
			synchronized(this) {
				if(BriefService.shouldReloadContacts) {
					//BLog.e("CHOME","di in background refresh contacts db");
					ContactsDb.refresh(activity);
					BriefService.shouldReloadContacts=false;
				}
			}
		    return Boolean.TRUE;
		}      
		@Override
		protected void onPostExecute(Boolean result) {
			//BLog.e("CHOME","drawit post execute");
			displayContacts();
		}
	 
	}


	
	@Override
	public void onPause() {
		super.onPause();
        State.addToState(State.SECTION_CONTACTS,new StateObject(StateObject.INT_LISTVIEW_FIRST_VIEWABLE,list.getFirstVisiblePosition()));
        //State.addToState(State.SECTION_CONTACTS,new StateObject(StateObject.INT_VALUE,State.getContactsMode()));
		//BLog.e("PAUSE","Contacts select");
	}


	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			State.clearStateObjects(State.SECTION_CONTACTS_ITEM);
            Person prow = adapter.getItem(position);
            if(prow!=null) {
                State.addToState(State.SECTION_CONTACTS_ITEM, new StateObject(StateObject.STRING_ID, prow.getString(Person.STRING_PERSON_ID)));
                Bgo.openFragmentBackStackAnimate(activity, ContactViewFragment.class);
            }
		}
	};

    
}
