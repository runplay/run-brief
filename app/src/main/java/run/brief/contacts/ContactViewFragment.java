package run.brief.contacts;


import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import run.brief.b.ActionBarManager;
import run.brief.b.B;
import run.brief.b.BFragment;
import run.brief.b.BRefreshable;
import run.brief.b.Bgo;
import run.brief.BriefManager;
import run.brief.BriefPodAdapterList;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.b.StateObject;
import run.brief.beans.Brief;
import run.brief.beans.Email;
import run.brief.beans.Person;
import run.brief.beans.PersonFull;
import run.brief.email.EmailSendFragment;
import run.brief.settings.AccountsDb;
import run.brief.sms.SmsSendFragment;
import run.brief.util.BriefActivityManager;
import run.brief.util.Functions;
import run.brief.util.ImageCache;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;

public class ContactViewFragment extends BFragment implements BRefreshable {
	
	//private Handler contactsHandler = new Handler();
	//private static ArrayList<Person> contacts=null;
	
	private View view;
	private BriefPodAdapterList adapter;
	private ImageView personImage;
	private Activity activity;
	private Person person;
    private PersonFull personFull;
	
	//private View skypeView;
	private boolean hasSkype=false;
	private boolean hasLine=false;
	private boolean hasGmail=false;
	private boolean hasViber=false;
	
	//private ImageButton btnedit;
	//private ImageButton btndelete;


	private ImageButton btnskype;



	private ImageButton btnline;
	
	private TextView name;



    private TextView messages;
    private TextView notes;
    private TextView contactUnknown;
	
	private LinearLayout hasPhoneView;
	private LinearLayout hasEmailView;
    private LinearLayout hasOtherView;

	private ImageView bgHeaderImage;
    private RelativeLayout bgHeader;

    private View showNotes;
    private View showAddress;

    private ScrollView scrollView;
    private int mActionBarHeight;
    //private int mPodHeight;
    private int mMinHeight;
    private int mOriginalHeight;
    //private static Bitmap bgImage=null;


    private LoadOtherData loadOtherData;

    private static final List<String> bgImagesList = new ArrayList<String>();
    static {
        bgImagesList.add("bgc_bee.jpg");
        bgImagesList.add("bgc_kohtaoat.jpg");
        bgImagesList.add("bgc_luak_coffee.jpg");
        bgImagesList.add("bgc_ramtbutan.jpg");
    }
    private static final int[] bgImagesRes = new int[4];
    static {
        bgImagesRes[0]=R.drawable.bgc_bee;
        bgImagesRes[1]=R.drawable.bgc_luak_coffee;
        bgImagesRes[2]=R.drawable.bgc_kohtaoat;
        bgImagesRes[3]=R.drawable.bgc_rambutan;
    }
	//private static HashMap<String,Person> to=new HashMap<String,Person>();

	//private View options;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//activity=getActivity();
	    ContactsDb.init(getActivity());
		view = inflater.inflate(R.layout.contacts_view,container, false);
		return view;
	}


    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_contacts_edit:
                editContact();
                break;
        }
        return false;
    }

    private void editContact() {
        // The Cursor that contains the Contact row
        if(person.isUnknownPerson())
            BriefActivityManager.openAndroidContactsCreateNew(activity,person);
        else
            BriefActivityManager.openAndroidContactsWithPerson(activity,person);
    }

	public void refreshData() {
		
	}
	@Override
	public void onResume() {
		super.onResume();
        activity=getActivity();
		hasSkype=BriefActivityManager.isSkypeClientInstalled(activity);
		hasGmail=BriefActivityManager.isGmailClientInstalled(activity);
		hasViber = BriefActivityManager.isViberClientInstalled(activity);
		hasLine = BriefActivityManager.isNaverLineClientInstalled(activity);
		
		State.setCurrentSection(State.SECTION_CONTACTS_ITEM);

        hasPhoneView =(LinearLayout) view.findViewById(R.id.contact_view_has_number);
        hasEmailView =(LinearLayout) view.findViewById(R.id.contact_view_has_email);
        hasOtherView =(LinearLayout) view.findViewById(R.id.contact_view_has_other);

        personImage = (ImageView) view.findViewById(R.id.contact_person_image);

        name = (TextView) view.findViewById(R.id.contact_view_name);
        contactUnknown = (TextView) view.findViewById(R.id.contact_unknown);
        contactUnknown.setVisibility(View.GONE);
        scrollView=(ScrollView) view.findViewById(R.id.contacts_scroll);

        messages=(TextView) view.findViewById(R.id.contact_view_message);
        notes=(TextView) view.findViewById(R.id.contact_view_notes);

        showNotes=view.findViewById(R.id.contact_view_show_notes);
        showAddress=view.findViewById(R.id.contact_view_show_postal);

        TextView othertitle = (TextView) view.findViewById(R.id.contact_view_other);
        TextView postaltitle = (TextView) view.findViewById(R.id.contact_view_postal_title);

        B.addStyle(new TextView[]{messages,notes});
        B.addStyleBold(new TextView[]{contactUnknown,othertitle,postaltitle,name});
         //scrollView
		
		//btnedit = (ImageButton) view.findViewById(R.id.contact_view_mode_edit);
		//btnedit.setOnClickListener(editContactListener);
		
		//btndelete = (ImageButton) view.findViewById(R.id.contact_view_mode_delete);
		//btndelete.setOnClickListener(deleteListener);
		

		
		btnskype = (ImageButton) view.findViewById(R.id.contact_view_skype);
		btnskype.setOnClickListener(skypeListener);
		

		
		btnline = (ImageButton) view.findViewById(R.id.contact_view_line);
		btnline.setOnClickListener(lineListener);
		
		bgHeaderImage = (ImageView) view.findViewById(R.id.contact_thumbnail);
        bgHeader = (RelativeLayout) view.findViewById(R.id.contact_header);


		if(hasSkype) {
			btnskype.setVisibility(View.VISIBLE);
		} else {
			btnskype.setVisibility(View.GONE);
		}

		if(hasLine) {
			btnline.setVisibility(View.VISIBLE);
		} else {
			btnline.setVisibility(View.GONE);
		}


		

		
		//btnedit.setVisibility(View.VISIBLE);

        //mPodHeight= Functions.dpToPx(220,activity);

		refresh();
		

		
	}
    public View getEmailView(final String email) {
        View emailpod = activity.getLayoutInflater().inflate(R.layout.contacts_view_email,null);

        TextView showemail = (TextView) emailpod.findViewById(R.id.contact_view_email);
        showemail.setText(email);
        B.addStyleBold(showemail);
        ImageButton btnemail= (ImageButton) emailpod.findViewById(R.id.contact_view_email_now);;
        ImageButton btngmail =  (ImageButton) emailpod.findViewById(R.id.contact_view_email_gmail);
        btnemail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                State.clearStateObjects(State.SECTION_EMAIL_NEW);
                //State.addToState(new StateObject(StateObject.LONG_USE_ACCOUNT_ID,currentAccount.getLong(Account.LONG_ID)));

                Email e = new Email();
                e.setString(Email.STRING_FROM,email);
                State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_BJSON_OBJECT,e.toString()));
                State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.INT_FORCE_NEW,1));
                Bgo.openFragmentBackStack(activity,EmailSendFragment.class);

            }
        });

        btngmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //State.setFileExploreState(State.FILE_EXPLORE_STATE_SELECTFILE);
                BriefActivityManager.openGmailClient(activity, email);

            }
        });
        if(hasGmail) {
            btngmail.setVisibility(View.VISIBLE);
        } else {
            btngmail.setVisibility(View.GONE);
        }
        if(!AccountsDb.getAllEmailAccounts().isEmpty()) {
            btnemail.setVisibility(View.VISIBLE);
        } else {
            btnemail.setVisibility(View.GONE);
        }

        return emailpod;

    }
    public View getPhoneView(final String number) {
        View phonepod = activity.getLayoutInflater().inflate(R.layout.contacts_view_phone,null);
        ImageButton btnphone;
        ImageButton btnsms;
        ImageButton btnviber;

        TextView shownumber = (TextView) phonepod.findViewById(R.id.contact_view_number);
        shownumber.setText(number);
        B.addStyleBold(shownumber);
        btnphone = (ImageButton) phonepod.findViewById(R.id.contact_view_phone);
        btnphone.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                BriefActivityManager.openPhone(activity, number);

            }
        });
        btnsms = (ImageButton) phonepod.findViewById(R.id.contact_view_sms);
        btnsms.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                State.clearStateObjects(State.SECTION_SMS_SEND);
                StateObject sob = new StateObject(StateObject.INT_FORCE_NEW,1);
                State.addToState(State.SECTION_SMS_SEND,sob);

                JSONArray arr= new JSONArray();
                arr.put(0,person);

                State.addToState(State.SECTION_SMS_SEND,new StateObject(StateObject.STRING_CONTACTS,arr.toString()));

                Bgo.openFragmentBackStackAnimate(activity, SmsSendFragment.class);

            }
        });

        btnviber = (ImageButton) phonepod.findViewById(R.id.contact_view_viber);
        btnviber.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                BriefActivityManager.openViber(activity,number);

            }
        });
        if(hasViber) {
            btnviber.setVisibility(View.VISIBLE);
        } else {
            btnviber.setVisibility(View.GONE);
        }

        if(Device.hasPhone()) {
            btnsms.setVisibility(View.VISIBLE);
            btnsms.setVisibility(View.VISIBLE);
        } else {
            btnsms.setVisibility(View.GONE);
            btnsms.setVisibility(View.GONE);
        }
        return phonepod;

    }
    public void refresh() {
        BriefManager.clearController(activity);
        State.setContactsMode(State.CONTACT_MODE_VIEW);
        ContactsSelectedClipboard.clear();
        ContactsSelectedClipboard.clearSearch();
        ActionBarManager.setActionBarBackOnlyTransparentNoUnderlay(activity, "", R.menu.contacts_view);

        showNotes.setVisibility(View.GONE);
        showAddress.setVisibility(View.GONE);

        if(State.hasStateObject(State.SECTION_CONTACTS_ITEM,StateObject.STRING_BJSON_OBJECT)) {

            person=new Person(new JSONObject(State.getStateObjectString(State.SECTION_CONTACTS_ITEM, StateObject.STRING_BJSON_OBJECT)));

        } else if(State.hasStateObject(State.SECTION_CONTACTS_ITEM,StateObject.STRING_ID)) {


            person=ContactsDb.getWithPersonId(activity, State.getStateObjectString(State.SECTION_CONTACTS_ITEM, StateObject.STRING_ID));
        } else if(State.hasStateObject(State.SECTION_CONTACTS_ITEM,StateObject.INT_USE_SELECTED_BRIEF_INDEX)) {
            Brief b = BriefManager.get(State.getStateObjectInt(State.SECTION_CONTACTS_ITEM,StateObject.INT_USE_SELECTED_BRIEF_INDEX));
            if(b!=null) {
                person=ContactsDb.getWithPersonId(activity, b.getPersonId());
                //BLog.e("B", "BOOOOO!!!!!");

                if(person==null) {
                    if(b.getWITH_()==Brief.WITH_PHONE) {
                        person = Person.getNewUnknownPerson(activity, b.getSubject(), null);
                        //person.addNumber(Person.TYPE_CNUM_MAIN, b.getSubject());
                        //person.setName(activity.getString(R.string.unknown));
                    } else if(b.getWITH_()==Brief.WITH_SMS) {

                    } else if(b.getWITH_()==Brief.WITH_EMAIL) {

                    }
                }

            }
        } else if(State.hasStateObject(State.SECTION_CONTACTS_ITEM,StateObject.INT_USE_SELECTED_INDEX)) {
            person=ContactsDb.get(State.getStateObjectInt(State.SECTION_CONTACTS_ITEM,StateObject.INT_USE_SELECTED_INDEX));
        } else if(State.hasStateObject(State.SECTION_CONTACTS_ITEM,StateObject.STRING_ID)) {
            person=ContactsDb.getWithPersonId(activity, State.getStateObjectString(State.SECTION_CONTACTS_ITEM, StateObject.STRING_ID));
        }


        State.clearStateObjects(State.SECTION_CONTACTS_ITEM);




        if(person!=null) {

            if(person.isUnknownPerson()) {
                //BLog.e("UNK", "Unkown person.................................");
                contactUnknown.setVisibility(View.VISIBLE);
            }
            personFull =ContactsDb.getPeronFullWithId(activity,person.getString(Person.STRING_PERSON_ID));
            //ActionBarManager.hideActionBarUnderlayer(activity);
            personImage.setImageBitmap(person.getThumbnail(activity));

            int modval = 0;
            try {
                modval = Integer.valueOf(person.getString(Person.STRING_PERSON_ID)).intValue() % (bgImagesList.size() - 1);
            } catch (Exception e) {
            }


            String pathname = "ccche-bmp-" + modval;

            ImageCache.CacheBitmap cb = ImageCache.get(pathname);
            if (cb == null) {
                cb = ImageCache.getNewCacheBitmap();

                cb.bitmap = BitmapFactory.decodeResource(getResources(), bgImagesRes[modval]);
                cb.status = ImageCache.CACHE_B_LOADED;
                ImageCache.putFinal(pathname, cb);
            }
            //bgImage=;
            bgHeaderImage.setImageBitmap(cb.bitmap);
            bgHeaderImage.setScaleType(ImageView.ScaleType.FIT_XY);
            //}

            mActionBarHeight=((AppCompatActivity) activity).getSupportActionBar().getHeight();
            mMinHeight=Functions.dpToPx(40,activity);


            mOriginalHeight=0;
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    mActionBarHeight=((AppCompatActivity) activity).getSupportActionBar().getHeight();
                    //int scrollX = scrollView.getScrollX(); //for horizontalScrollView
                    if (mOriginalHeight == 0)
                        mOriginalHeight = bgHeader.getHeight();
                    int scrollY = scrollView.getScrollY(); //for verticalScrollView
                    int topscroolval = mOriginalHeight - (mMinHeight + mActionBarHeight);

                    int pct = Double.valueOf(((100D / topscroolval) * scrollY)).intValue();
                    if (pct > 100)
                        pct = 100;
                    int revPct = 100 - pct;
                    //if(mOriginalHeight-scrollY<mMinHeight)
                    //int tmph = (mOriginalHeight - scrollY);
                    //BLog.e("CC", mMinHeight + "--" + mActionBarHeight + "--" + mOriginalHeight);
                    //    tmph=(mMinHeight + mActionBarHeight);
                    double ratio = 1D;
                    if (scrollY > 90)
                        ratio = 0.25;
                    else if (scrollY > 60)
                        ratio = 0.5;
                    else if (scrollY > 30)
                        ratio = 0.75;

                    int alp = Double.valueOf((255D / 100) * (revPct * 0.75)).intValue();
                    if (scrollY == 0 || alp > 255)
                        alp = 255;
                    if (scrollY < (topscroolval)) {
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bgHeader.getLayoutParams();//new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,tmph);
                        lp.setMargins(0, -scrollY, 0, 0);
                        bgHeader.setLayoutParams(lp);
                    } else {
                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) bgHeader.getLayoutParams();//new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,tmph);
                        lp.setMargins(0, -topscroolval, 0, 0);
                        bgHeader.setLayoutParams(lp);
                        alp = 0;
                    }

                    personImage.setImageAlpha(alp);

                }
            });

            //if (person.getCompany() != null)
                name.setText(person.getString(Person.STRING_NAME));
            //else
               // name.setText(personFull.getNickname());


            if (!person.has(Person.JSONARRAY_EMAIL) || person.getJSONArray(Person.JSONARRAY_EMAIL).length()==0) {
                hasEmailView.setVisibility(View.GONE);
            } else {
                hasEmailView.setVisibility(View.VISIBLE);
                hasEmailView.removeAllViews();
                JSONArray emails = person.getJSONArray(Person.JSONARRAY_EMAIL);
                for(int i=0; i<emails.length(); i++) {
                    hasEmailView.addView(getEmailView((String) emails.get(i)));
                }
            }
            if (!Device.hasPhone() || person.getJSONArray(Person.JSONARRAY_PHONE).length()==0) {
                hasPhoneView.setVisibility(View.GONE);
            } else {
                hasPhoneView.setVisibility(View.VISIBLE);
                hasPhoneView.removeAllViews();
                JSONArray phones = person.getJSONArray(Person.JSONARRAY_PHONE);
                if(phones!=null) {
                    for (int i = 0; i < phones.length(); i++) {
                        hasPhoneView.addView(getPhoneView((String) phones.get(i)));
                    }
                }
            }
            if (hasSkype || hasLine) {
                hasOtherView.setVisibility(View.VISIBLE);
            } else {
                hasOtherView.setVisibility(View.GONE);
            }



            if (personFull != null) {





                loadOtherData = new LoadOtherData();
                loadOtherData.execute(true);
            }
        }
    }

    protected OnClickListener copyPostalListner = new OnClickListener() {
        @Override
        public void onClick(View view) {
            StringBuilder sb = getPersonAddress();
            Functions.copyToClipFlashView(activity, showAddress);


            Device.copyToClipboard(activity, sb.toString());
            Toast.makeText(activity, R.string.copied_to_clip, Toast.LENGTH_SHORT).show();
        }
    };
    protected OnClickListener copyNotesListner = new OnClickListener() {
        @Override
        public void onClick(View view) {

            Functions.copyToClipFlashView(activity, showNotes);


            Device.copyToClipboard(activity, notes.getText().toString());
            Toast.makeText(activity, R.string.copied_to_clip, Toast.LENGTH_SHORT).show();
        }
    };
    private void finishedLoadOtherData() {
        StringBuilder sb = new StringBuilder();
        if(personFull.getNotes()!=null && !personFull.getNotes().isEmpty()) {
            //BLog.e("PF","has notes: "+personFull.getNotes().size());
            showNotes.setVisibility(View.VISIBLE);
            for (String note : personFull.getNotes()) {
                sb.append(note);
                sb.append("\n\n");
            }
        }
        notes.setText(sb.toString());
        notes.invalidate();
        notes.setOnClickListener(copyNotesListner);

        messages.setText(getPersonAddress().toString());
        messages.setOnClickListener(copyPostalListner);
        //messages.invalidate();

    }
    private StringBuilder getPersonAddress() {
        StringBuilder add = new StringBuilder();

        if(personFull!=null && personFull.getAddress()!=null && !personFull.getAddress().isEmpty()) {

            showAddress.setVisibility(View.VISIBLE);
            String type=personFull.getAddress().keySet().iterator().next();

            HashMap<PersonFull.addressField,String>address=personFull.getAddress().get(type);


            if(address.get(PersonFull.addressField.pobox)!=null) {
                add.append(address.get(PersonFull.addressField.pobox));
                add.append("\n");
            }
            if(address.get(PersonFull.addressField.street)!=null) {
                add.append(address.get(PersonFull.addressField.street));
                add.append("\n");
            }
            if(address.get(PersonFull.addressField.state)!=null) {
                add.append(address.get(PersonFull.addressField.state));
                add.append("\n");
            }

            if(address.get(PersonFull.addressField.city)!=null) {
                add.append(address.get(PersonFull.addressField.city));
                add.append("\n");
            }
            if(address.get(PersonFull.addressField.postcode)!=null) {
                add.append(address.get(PersonFull.addressField.postcode));
                add.append("\n");
            }
            if(address.get(PersonFull.addressField.country)!=null) {
                add.append(address.get(PersonFull.addressField.country));
                add.append("\n");
            }



        }
        return add;
    }
    private class LoadOtherData extends AsyncTask<Boolean, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Boolean... params) {

            personFull.loadOtherData(activity);
            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            finishedLoadOtherData();
        }

    }
	@Override
	public void onPause() {
		super.onPause();
        if(person!=null)
            State.addToState(State.SECTION_CONTACTS_ITEM,new StateObject(StateObject.STRING_ID,person.getString(Person.STRING_PERSON_ID)));

		//BLog.e("PAUSE","Contacts select");
	}
	

	public OnItemClickListener openListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			
		}
	};
	protected OnClickListener generalListener = new OnClickListener() {
		@Override
		public void onClick(View view) {

			
		}
	};	


	protected OnClickListener lineListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			BriefActivityManager.openNaverLineClientUser(activity);
			
		}
	};	
	protected OnClickListener phoneListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			BriefActivityManager.openPhone(activity, (String) person.getJSONArray(Person.JSONARRAY_PHONE).get(0));
			
		}
	};	
	protected OnClickListener skypeListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			BriefActivityManager.openSkype(activity,(String) person.getJSONArray(Person.JSONARRAY_PHONE).get(0));
			
		}
	};
    /*
	protected OnClickListener viberListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			BriefActivityManager.openViber(activity,(String) person.getJSONArray(Person.JSONARRAY_PHONE).get(0));
			
		}
	};
	protected OnClickListener smsListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
            State.clearStateObjects(State.SECTION_SMS_SEND);
            StateObject sob = new StateObject(StateObject.INT_FORCE_NEW,1);
            State.addToState(State.SECTION_SMS_SEND,sob);

            JSONArray arr= new JSONArray();
            arr.put(0,person);

            State.addToState(State.SECTION_SMS_SEND,new StateObject(StateObject.STRING_CONTACTS,arr.toString()));

            Bgo.openFragmentBackStackAnimate(activity, new SmsSendFragment());
			
		}
	};	
	protected OnClickListener emailListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			State.clearStateObjects(State.SECTION_EMAIL_NEW);
			//State.addToState(new StateObject(StateObject.LONG_USE_ACCOUNT_ID,currentAccount.getLong(Account.LONG_ID)));
			
			Email e = new Email();
			e.setString(Email.STRING_FROM, (String) person.getJSONArray(Person.JSONARRAY_EMAIL).get(0));
			State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.STRING_BJSON_OBJECT,e.toString()));
			State.addToState(State.SECTION_EMAIL_NEW,new StateObject(StateObject.INT_FORCE_NEW,1));
			Bgo.openFragmentBackStack(activity,new EmailNewFragment());
			
		}
	};	
	protected OnClickListener gmailListener = new OnClickListener() {
		@Override
		public void onClick(View view) {
			//State.setFileExploreState(State.FILE_EXPLORE_STATE_SELECTFILE);
			BriefActivityManager.openGmailClient(activity, (String) person.getJSONArray(Person.JSONARRAY_EMAIL).get(0));
			
		}
	};	
	
	



	private void addNewDefaultContact() {
		if(person!=null) {
		// Creates a new Intent to insert a contact
		Intent intent = new Intent(Intents.Insert.ACTION);
		// Sets the MIME type to match the Contacts Provider
		intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);
		
		intent.putExtra(Intents.Insert.EMAIL, person.getString(Person.STRING_EMAIL))

		      .putExtra(Intents.Insert.EMAIL_TYPE, CommonDataKinds.Email.TYPE_WORK)
		// Inserts a phone number
		      .putExtra(Intents.Insert.PHONE, person.getString(Person.STRING_EMAIL))

		      .putExtra(Intents.Insert.PHONE_TYPE, Phone.TYPE_WORK);
			
			startActivity(intent);
		}

	}
    */


}
