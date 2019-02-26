package run.brief.b;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import run.brief.R;
import run.brief.beans.Person;
import run.brief.contacts.ContactsDb;
import run.brief.contacts.ContactsHomeFragment;
import run.brief.contacts.ContactsSelectedClipboard;
import run.brief.util.Functions;
import run.brief.util.Sf;

public class bToPersonsView extends LinearLayout implements BRefreshable {
	
		public static final int MODE_VIEW_ADD=0;
		public static final int MODE_VIEW_EDIT=1;
		

		
		private AutoCompleteTextView manualType;
		private ArrayList<ContactsDb.SearchResult> searchResult;
		//private ListView searchList;
		private ContactsSelectedAdapter csadapter;
		private Person pfocus;
		private GridView contactsList;
		//private static HashMap<String,Person> tos=new HashMap<String,Person>();
		private int THIS_MODE_=MODE_VIEW_ADD;
		
		private static bButton addPeopleButton;
		private static bButton showSettingsButton;
		
		private static int THIS_CONTACTS_TYPE=ContactsSelectedClipboard.CONTACTS_TYPE_ALL;
		
		private static final int IMAGE_SIZE_PX=70;
		private static int IMAGE_SIZE_DP=90;
		
		private Context context;
		//private ViewManagerContacts manager;
		//private bMaxMinLinearLayout mmlayout;
		private RelativeLayout layout;
		private PopupWindow searchPop;
		//private RelativeLayout searchRes;
		private RelativeLayout contactSearchParent;
		private View measureTop;
        //private BRefreshable refreshFragment;
        private SearchListAdapter searchListAdapter;
        private static String numbersOnlyRegex = "[\\d\\s]+";//"[0-9]+";
		
		//private static final int R_SEARCH_LAYOUT=8383;
		//private static final int R_SEARCH_LIST=8384;
	
		public void setViewMeasureTop(View view) {
			measureTop=view;
		}

		public void setSearchParent(RelativeLayout contactSearchParent) {
			this.contactSearchParent=contactSearchParent;
		}
		public bToPersonsView(Context context) {
		    super(context);
		    this.context=context;
		    goall();
		}
		
		public bToPersonsView (Context context, AttributeSet attrs) {
		    super(context, attrs);
		    this.context=context;
		    goall();
		    // TODO Auto-generated constructor stub
		}
		public EditText getSearchEditText() {
			return manualType;
		}
	    public bToPersonsView(Context context, AttributeSet attrs, int defStyle)    {
	        super(context, attrs, defStyle);
		    this.context=context;
		    goall();
	    }
	    private void goall() { 
	    	 IMAGE_SIZE_DP=Functions.dpToPx(IMAGE_SIZE_PX, context);
	    	 layout =  (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.contacts_clipboard, this,false);

            contactsList = (GridView) layout.findViewById(R.id.contacts_clip_list);
            addPeopleButton=(bButton) layout.findViewById(R.id.contacts_clip_add);
            showSettingsButton=(bButton) layout.findViewById(R.id.contacts_clip_opt);

            manualType = (AutoCompleteTextView) layout.findViewById(R.id.contacts_type_find);

	    	 //mmlayout=(bMaxMinLinearLayout) layout.findViewById(R.id.contacts_mmlayout);
	    }

	    public void setMaxHeight(int heightInDp) {
	    	//mmlayout.setMinMaxHeight(100,heightInDp);
	    }
		public void setContactsType(int CONTACTS_TYPE_) {
			THIS_CONTACTS_TYPE=CONTACTS_TYPE_;
		}
		public void setMode(int MODE_) {
			THIS_MODE_=MODE_;
		}

		public void setContext(Context context) {
			this.context=context;
		}
		public void refresh() {
			refreshData();
		}
		public void refreshData() {
			//tos=persons;
            //BLog.e("bpv","1");
			clearChildren();
			
					//new LinearLayout(context);
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			//this.setOrientation(LinearLayout.HORIZONTAL);
			this.setLayoutParams(lp);

            manualType.setOnItemClickListener(searchClick);

			csadapter=new ContactsSelectedAdapter(context);
			contactsList.setAdapter(csadapter);
			contactsList.setOnItemClickListener(contactClick);
			if(csadapter.getCount()>4) {
                RelativeLayout.LayoutParams clp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 210);
                clp.addRule(RelativeLayout.BELOW,R.id.contacts_type_find);
                contactsList.setLayoutParams(clp);
            } else {
                RelativeLayout.LayoutParams clp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                clp.addRule(RelativeLayout.BELOW,R.id.contacts_type_find);
                contactsList.setLayoutParams(clp);
            }


			B.addStyle(manualType);
			//searchRes = (RelativeLayout) layout.findViewById(R.id.contacts_search_pod);
            /*
            if(csadapter.getCount()>6) {
                //RelativeLayout.LayoutParams alp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,200);
                contactsList.getLayoutParams().height=200;//.setLayoutParams(lp);
            } else {
                //RelativeLayout.LayoutParams alp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
                //contactsList.setLayoutParams(lp);
                contactsList.getLayoutParams().height= LayoutParams.WRAP_CONTENT;
            }
			*/
			//RelativeLayout parent = (RelativeLayout) layout.getParent();

			if(THIS_MODE_==MODE_VIEW_ADD) {
				addPeopleButton.setClickable(true);
				addPeopleButton.setOnClickListener(addEditContactsListner);
				addPeopleButton.setVisibility(View.VISIBLE);
				showSettingsButton.setVisibility(View.GONE);
				manualType.setVisibility(View.VISIBLE);
			
				manualType.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
				
				manualType.addTextChangedListener(new TextWatcher() {

					   public void afterTextChanged(Editable st) {
						   //BLog.e("TXT", "change to Text for to: ");
						   synchronized(this) {
							   String s=st.toString();
                               boolean emptyview=true;
                               if(s.length()<3) {
                                   manualType.setAdapter(null);
                                   ContactsSelectedClipboard.clearSearch();
                                   searchResult = ContactsSelectedClipboard.getSearchResults();
                                   if(searchListAdapter!=null) {
                                       SearchFilter sf = searchListAdapter.getFilter();
                                       sf = null;
                                   }
                               } else {
                                   if(ContactsSelectedClipboard.getLastSearch()==null
                                     || !s.equals(ContactsSelectedClipboard.getLastSearch())) {
                                       Boolean added = false;

                                       if (s.length()>4 && (THIS_CONTACTS_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_ALL || THIS_CONTACTS_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_EMAIL)) {
                                           if (Sf.isValidEmail(s.trim())) {
                                               if(s.endsWith(" ") || s.endsWith(",") || s.endsWith(";")) {
                                                   String uses=s.substring(0,s.length()-2);
                                                   added=true;
                                                   ContactsDb.SearchResult p = ContactsDb.makeSearchResult(new SpannableString(uses.trim()), Person.getNewUnknownPerson(getContext(), uses.trim(), null));
                                                   ContactsSelectedClipboard.clearSearch();
                                                   searchResult.clear();
                                                   searchListAdapter.notifyDataSetInvalidated();
                                                   manualType.setAdapter(null);

                                                   ContactsSelectedClipboard.addPerson(p.person);
                                                   ContactsSelectedClipboard.clearSearch();

                                                   manualType.setText("");
                                                   refreshData();


                                                   SearchFilter sf = searchListAdapter.getFilter();
                                                   sf = null;
                                               }

                                           }
                                       }
                                        if(!added) {
                                           //BLog.e("SFOR", "term: " + s);
                                           searchResult = ContactsSelectedClipboard.search(s, THIS_CONTACTS_TYPE);

                                            //if(manualType.getAdapter()==null) {
                                            searchListAdapter = new SearchListAdapter(context);
                                           //}
                                            searchListAdapter.notifyDataSetChanged();
                                            searchListAdapter.notifyDataSetInvalidated();
                                            searchListAdapter.getFilter().filter(s);
                                            manualType.setAdapter(searchListAdapter);
                                            //searchListAdapter.
                                           emptyview=false;
                                        }

                                   }

                                   if(emptyview) {
                                       ContactsSelectedClipboard.clearSearch();
                                       searchResult = ContactsSelectedClipboard.getSearchResults();
                                       searchListAdapter=new SearchListAdapter(context);
                                       manualType.setAdapter(searchListAdapter);

                                       SearchFilter sf = searchListAdapter.getFilter();
                                       sf = null;
                                   }



                               }


						   }
					   }

					   public void beforeTextChanged(CharSequence s, int start,
					     int count, int after) {
					   }

					   public void onTextChanged(CharSequence s, int start,
					     int before, int count) {

					   }
					  });
				
				//this.addView(addPeopleButton);
			} else {
				showSettingsButton.setClickable(true);
				//showSettingsButton.setOnClickListener(settingsContactsListner);
				showSettingsButton.setVisibility(View.VISIBLE);
				addPeopleButton.setVisibility(View.GONE);
				manualType.setVisibility(View.GONE);  
				//this.addView(showSettingsButton);
			}

            showSettingsButton.setVisibility(View.GONE);
			this.addView(layout);

		}

        private void addPersonToSelectedContacts(ContactsDb.SearchResult p) {
            //BLog.e("Aaaaa","addPersonToSelectedContacts(): "+p.person.toString());
            ContactsSelectedClipboard.addPerson(p.person);
            ContactsSelectedClipboard.clearSearch();
            //csadapter=new ContactsSelectedAdapter(context);
            //contactsList.setAdapter(csadapter);
            SearchFilter sf=searchListAdapter.getFilter();
            sf=null;
            manualType.setText("");
            refreshData();
            //BLog.e("bpv", "2");
            Bgo.tryRefreshCurrentFragment();

        }
		protected OnItemClickListener searchClick = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Object ob = parent.getItemAtPosition(position);

                ContactsDb.SearchResult p=(ContactsDb.SearchResult) view.getTag();//ob;

				//ContactsDb.SearchResult p = searchResult.get(position);//.getItem(position);//ContactsSelectedClipboard.getSearchResults().get(position);
				if(p!=null) {
                    addPersonToSelectedContacts(p);
				}
			}
		};
		protected OnItemClickListener contactClick = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
				pfocus= csadapter.data.get(position);
				if(pfocus!=null) {
					   //popupMenu.getMenu().
					PopupMenu popupMenu = new PopupMenu(context, view);
				      //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());
					popupMenu.getMenu().add(context.getResources().getString(R.string.label_remove));
				      popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						   @Override
						   public boolean onMenuItemClick(MenuItem item) {

                               ContactsSelectedClipboard.removePerson(pfocus);
                               ContactsSelectedClipboard.clearSearch();
                               //csadapter=new ContactsSelectedAdapter(context);
                               //contactsList.setAdapter(csadapter);
							   if(searchListAdapter!=null) {
								   SearchFilter sf = searchListAdapter.getFilter();

								   sf = null;
							   }
                               manualType.setText("");
                               refreshData();
                               //BLog.e("bpv", "2");
                               Bgo.tryRefreshCurrentFragment();

						    return true;
						   }
				      });
				    
				      popupMenu.show();
				      //sea
				}
			}
		};

		public void clearPersons() {
			ContactsSelectedClipboard.clear();
		}
		
		private void clearChildren() {
			
			this.removeAllViews();
			this.invalidate();
		}

		protected OnClickListener addEditContactsListner = new OnClickListener() {
			@Override
			public void onClick(View view) {
				if(THIS_CONTACTS_TYPE==ContactsSelectedClipboard.CONTACTS_TYPE_NUMBER)
					State.setContactsMode(State.CONTACT_MODE_SELECT_SMS);
				else
					State.setContactsMode(State.CONTACT_MODE_SELECT_EMAIL);
				Bgo.openFragmentBackStackAnimate((Activity) context, ContactsHomeFragment.class);
			}
		};

        private class SearchFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                // We implement here the filter logic
                //String s=constraint.toString();
                synchronized (this) {

                    //BLog.e("SFOR","constraint term: "+constraint);
                    if (constraint == null || constraint.length() <3) {
                        // No filter implemented we return all the list
                        //BLog.e("SFOR","constraint term: use empty");
                        results.values = new ArrayList<ContactsDb.SearchResult>();
                        results.count = 0;
                    } else {
                        //BLog.e("SFOR","constraint term: "+constraint);
                        results.values = searchListAdapter.getData();
                        results.count = searchListAdapter.getData().size();

                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint,FilterResults results) {
                // Now we have to inform the adapter about the new list filtered
                //BLog.e("SFOR","public term: "+results.count);
                if (results.count>0 ||
                        (constraint!=null && constraint.length()>2 && results.count ==0)) {
                    //BLog.e("SFOR","public term: "+constraint);
                    searchListAdapter.notifyDataSetChanged();
                    manualType.showDropDown();

                } else {
                    //BLog.e("SFOR","public term: is empty");
                    searchListAdapter.notifyDataSetInvalidated();
                    manualType.dismissDropDown();
                    //planetList = (List<Planet>) results.values;


                }
            }

        }
		public class SearchListAdapter extends BaseAdapter implements Filterable {
			 
		    //private Context activity;
		    private ArrayList<ContactsDb.SearchResult> data=new ArrayList<ContactsDb.SearchResult>();
		    //private LayoutInflater inflater=null;
		    
		    private LayoutParams rlp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		    private LayoutParams wlp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
            private SearchFilter searchFilter=new SearchFilter();

            public ArrayList<ContactsDb.SearchResult> getData() {
                return data;
            }

            public SearchFilter getFilter() {
                 return searchFilter;
            }

		    public SearchListAdapter(Context context) {
		        //activity = a;
		        //BLog.e("CALL", "adapter: "+ContactsSelectedClipboard.getSearchResults().size());
		        this.data.clear();
	        	String lastSearch=ContactsSelectedClipboard.getLastSearch();
	        	Person pu = Person.getNewUnknownPerson(context, lastSearch, lastSearch);
                ContactsDb.SearchResult res = ContactsDb.makeSearchResult(new SpannableString(lastSearch), pu);
	        	this.data.add(res);
                //BLog.e("SFOR","adding result: "+res.person.getMainEmail()+", name: "+res.person.getString(Person.STRING_NAME));
		        for(ContactsDb.SearchResult p: ContactsSelectedClipboard.getSearchResults()) {
		        	this.data.add(p);
		        }
		        //inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    }

		    public int getCount() {
		        return data.size();
		    }
		 /*
		    public static void setSelectedPersons(HashMap<String,Person> tos) {
		    	selectedPersons=tos;
		    }
		    */
		    public Object getItem(int position) {
		        return data.get(position);
		    }
		 
		    public long getItemId(int position) {
		        return position;
		    }
		 
		    public View getView(int position, View convertView, ViewGroup parent) {
                //BLog.e("bpv","3");
                if(convertView == null)
                {
                    //holder = new ViewHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.contacts_clipboard_item, parent, false);

                    //convertView.setTag(holder);
                }


                ContactsDb.SearchResult result = data.get(position); //ContactsSelectedClipboard.get(it.next());

                convertView.setTag(result);
				TextView tv = (TextView) convertView.findViewById(R.id.contacts_clip_item_text);
				//tv.setGravity(Gravity.CENTER_VERTICAL);
				tv.setText(result.person.getString(Person.STRING_NAME));

                TextView data = (TextView) convertView.findViewById(R.id.contacts_clip_item_data);
                data.setText(result.matches);
				//tv.setLayoutParams(wlp);
				//tv.setPadding(IMAGE_SIZE_DP+2, 5, 0, 0);
				//tv.setGravity(Gravity.CENTER_VERTICAL);

                B.addStyle(new TextView[]{tv,data});

				ImageView image = (ImageView) convertView.findViewById(R.id.contacts_clip_item_icon);
                //image.setLayoutParamsInPx(IMAGE_SIZE_PX,IMAGE_SIZE_PX);
				image.setVisibility(View.VISIBLE);
				//image.setSize(IMAGE_SIZE_PX);
				//image.setPerson(result.person);
				//image.setBandColor("#000000");
				image.setImageBitmap(result.person.getThumbnail(context));
				//image.setLayoutParamsInPx(IMAGE_SIZE_PX*2,IMAGE_SIZE_PX*2);
				//image.setS
				
				//rel.addView(image);
				//rel.addView(tv);
				
				return convertView;
				
		    }
		}
		public class ContactsSelectedAdapter extends BaseAdapter {
			 
		    private Context activity;
		    private ArrayList<Person> data=new ArrayList<Person>();
		    //private LayoutInflater inflater=null;
		    
		    private LayoutParams rlp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		    private LayoutParams wlp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		    
		    public ContactsSelectedAdapter(Context a) {
		        //activity = a;
		        
		        this.data.clear();
		        Iterator<String> it=ContactsSelectedClipboard.get().keySet().iterator();
		        while(it.hasNext()) {
                    Person p=ContactsSelectedClipboard.get(it.next());
		        	this.data.add(p);
		        }
		        //inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    }

		    public int getCount() {
		        return data.size();
		    }
		 /*
		    public static void setSelectedPersons(HashMap<String,Person> tos) {
		    	selectedPersons=tos;
		    }
		    */
		    public Object getItem(int position) {
		        return data.get(position);
		    }
		 
		    public long getItemId(int position) {
		        return position;
		    }
		 
		    public View getView(int position, View convertView, ViewGroup parent) {

		    	//RelativeLayout citem = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.contacts_clipboard_item, null, false);
                if(convertView == null)
                {
                    //holder = new ViewHolder();
                    convertView = LayoutInflater.from(context).inflate(R.layout.contacts_clipboard_item, parent, false);

                    //convertView.setTag(holder);
                }
                Person p = data.get(position); //ContactsSelectedClipboard.get(it.next());
			
				TextView tv = (TextView) convertView.findViewById(R.id.contacts_clip_item_text);
				//tv.setGravity(Gravity.CENTER_VERTICAL);
                B.addStyle(tv);

                String name = p.getString(Person.STRING_NAME);
				tv.setText(name);

                TextView data = (TextView) convertView.findViewById(R.id.contacts_clip_item_data);

                String showData="none";

                //BLog.e("BTPV",position+" --- contacts type: "+THIS_CONTACTS_TYPE);
                if(THIS_CONTACTS_TYPE==ContactsSelectedClipboard.CONTACTS_TYPE_EMAIL) {
                    try {
                        showData=p.getJSONArray(Person.JSONARRAY_EMAIL).getString(p.getInt(Person.INT_INDEX_USE_EMAIL));
                    } catch(Exception e) {
                        //BLog.e("BTPV","phone: "+e.getMessage());
                    }
                } else if(THIS_CONTACTS_TYPE==ContactsSelectedClipboard.CONTACTS_TYPE_NUMBER) {
                    try {
                        //BLog.e("ADDP","selindex: "+p.getInt(Person.INT_INDEX_USE_PHONE)+", size: "+p.getJSONArray(Person.JSONARRAY_PHONE).length()+" == "+p.toString());
                        showData=p.getJSONArray(Person.JSONARRAY_PHONE).getString(p.getInt(Person.INT_INDEX_USE_PHONE));
                    } catch(Exception e) {
                        //BLog.e("BTPV","email: "+e.getMessage());
                    }
                }

                data.setText(showData);
				//tv.setText(p.getName());
				//tv.setLayoutParams(wlp);
				//tv.setPadding(IMAGE_SIZE_PX+5, 5, 0, 0);
				//tv.setGravity(Gravity.CENTER_VERTICAL);

                ImageView image = (ImageView) convertView.findViewById(R.id.contacts_clip_item_icon);
                //image.setLayoutParamsInPx(IMAGE_SIZE_PX,IMAGE_SIZE_PX);
                image.setVisibility(View.VISIBLE);
                //image.setSize(IMAGE_SIZE_PX);
                //image.setPerson(result.person);
                //image.setBandColor("#000000");
                //image.setImageBitmap(result.person.getThumbnail(context));
				image.setImageBitmap(p.getThumbnail(context));
				//image.setLayoutParamsInPx(IMAGE_SIZE_PX*2,IMAGE_SIZE_PX*2);
				//image.setS
				
				//rel.addView(image);
				//rel.addView(tv);
				
				return convertView;
				
		    }
		}





}
