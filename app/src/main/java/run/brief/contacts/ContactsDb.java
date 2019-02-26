package run.brief.contacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.SpannableString;
import android.text.style.StyleSpan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import run.brief.b.Device;
import run.brief.beans.Person;
import run.brief.beans.PersonFull;
import run.brief.secure.Validator;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;


public final class ContactsDb  {
	private static final ContactsDb DB = new ContactsDb();

	private List<Person> contacts=new ArrayList<Person>();
    private List<Person> unknown=new ArrayList<Person>();
    private List<PersonFull> contactsFull=new ArrayList<PersonFull>();
    private PersonSlimDbTable personDb;
	
	public static final String CNAME="contactname";
	public static final String CIMG="contactimage";
	public static final String CNUM="contactnumber";
    private static String numbersOnlyRegex = "[\\d\\s]+";
    public static ContactsDb getDb() {
        if(Validator.isValidCaller())
            return DB;
        return
                null;
    }
    public synchronized static void init(Context context) {
        if(DB.contacts.isEmpty()) {

            DB.personDb=new PersonSlimDbTable(context);

            DB.contacts=DB.personDb.getItems();    //NOTES.database.getItems(0, 50);
            //SortNotes();
        }
    }

    public static synchronized void addUnknownPerson(Person unknown) {
        //BLog.e("PSON"," ADD:  "+unknown.getString(Person.STRING_PERSON_ID));
        DB.unknown.add(unknown);
        if(DB.unknown.size()>200)
            DB.unknown.remove(0);
    }

    public synchronized static void refresh(Context context) {
        init(context);
    }
    public synchronized static void refreshFull(Context context) {
        loadContactsFull(context);
    }


    public static int size() {
        return DB.contacts.size();
    }

    public static Person getWithPersonId(Context context, String id) {
        //BLog.e("PSON",id+" eq test");
        if(!id.startsWith(Person.NO_ID_START)) {
            for (Person tmp : DB.contacts) {
                if (tmp.getString(Person.STRING_PERSON_ID).equals(id))
                    return tmp;

            }
        } else {
            for (int i = DB.unknown.size() - 1; i >= 0; i--) {
                Person tmp = DB.unknown.get(i);
                if (tmp.getString(Person.STRING_PERSON_ID).equals(id))
                    return tmp;
            }
        }
        return null;
    }
    public static Person get(int index) {
        return DB.contacts.get(index);
    }

    public static SearchResult makeSearchResult(SpannableString matches,Person person) {
        return privateDb.new SearchResult(matches,person);
    }
    public class SearchResult {
        public SpannableString matches;
        public Person person;
        public SearchResult(SpannableString matchString,Person person) {
            this.matches=matchString;
            this.person=person;
        }
    }
    private static ContactsDb privateDb=new ContactsDb();
    public static List<SearchResult> find(String nameEmailNumberEtc, int ContactsClipCONTACT_TYPE) {

        List<SearchResult> res = new ArrayList<SearchResult>();
        nameEmailNumberEtc=nameEmailNumberEtc.toLowerCase();
        final String useTermTel=nameEmailNumberEtc.replaceAll(" ","");
        for(Person c: DB.contacts) {
            JSONArray phones = c.getJSONArray(Person.JSONARRAY_PHONE);
            JSONArray emails = c.getJSONArray(Person.JSONARRAY_EMAIL);
            boolean added=false;
            if(c.getString(Person.STRING_NAME).toLowerCase().contains(nameEmailNumberEtc)) {
                Person useperson = c.clone();
                SpannableString spannablecontent=new SpannableString(nameEmailNumberEtc);
                spannablecontent.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC),
                        0,nameEmailNumberEtc.length(), 0);


                if(ContactsClipCONTACT_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_ALL) {
                    SpannableString match = new SpannableString(c.getString(Person.STRING_NAME).replace(nameEmailNumberEtc,spannablecontent));
                    for(int i=0; i<phones.length(); i++) {

                        useperson.setInt(Person.INT_INDEX_USE_PHONE,i);
                        Person tmp=new Person(new JSONObject(useperson.toString()));
                        res.add(privateDb.new SearchResult(new SpannableString(phones.getString(i)),tmp));
                    }
                    for(int i=0; i<emails.length(); i++) {
                        useperson.setInt(Person.INT_INDEX_USE_EMAIL,i);
                        res.add(privateDb.new SearchResult(new SpannableString(emails.getString(i)),useperson));
                    }
                    added = true;
                } else if(ContactsClipCONTACT_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_NUMBER && phones.length()>0) {
                    for(int i=0; i<phones.length(); i++) {
                        useperson.setInt(Person.INT_INDEX_USE_PHONE,i);
                        Person tmp=new Person(new JSONObject(useperson.toString()));
                        //BLog.e("MATCH",tmp.toString());
                        res.add(privateDb.new SearchResult(new SpannableString(phones.getString(i)),tmp));
                    }
                    added = true;
                } else if(ContactsClipCONTACT_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_EMAIL && emails.length()>0) {
                    for(int i=0; i<emails.length(); i++) {
                        useperson.setInt(Person.INT_INDEX_USE_EMAIL,i);
                        Person tmp=new Person(new JSONObject(useperson.toString()));
                        res.add(privateDb.new SearchResult(new SpannableString(emails.getString(i)),tmp));
                    }
                    added = true;
                }

            }
            if(!added) {
                if (ContactsClipCONTACT_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_ALL || ContactsClipCONTACT_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_NUMBER) {

                    if(useTermTel.matches(numbersOnlyRegex)) {
                        Person useperson = c.clone();
                        SpannableString spannablecontent = new SpannableString(nameEmailNumberEtc);
                        spannablecontent.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC),
                                0, nameEmailNumberEtc.length(), 0);

                        //BLog.e("UT","-"+useTerm+"-");
                        for (int i = 0; i < phones.length(); i++) {
                            String ph = (String) phones.get(i);
                            if (!useTermTel.startsWith("0") && ph.startsWith("0"))
                                ph.replaceFirst("0", "");
                            if (ph.replaceAll(" ", "").contains(useTermTel)) {
                                //c.setInt(Person.INT_INDEX_USE_PHONE,i);
                                SpannableString match = new SpannableString(((String) phones.get(i)).replace(nameEmailNumberEtc, spannablecontent));
                                useperson.setInt(Person.INT_INDEX_USE_PHONE, i);
                                Person tmp=new Person(new JSONObject(useperson.toString()));
                                res.add(privateDb.new SearchResult(match, tmp));
                            }
                        }
                    }
                } else if (ContactsClipCONTACT_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_ALL || ContactsClipCONTACT_TYPE == ContactsSelectedClipboard.CONTACTS_TYPE_EMAIL) {
                    Person useperson = c.clone();
                    SpannableString spannablecontent=new SpannableString(nameEmailNumberEtc);
                    spannablecontent.setSpan(new StyleSpan(android.graphics.Typeface.BOLD_ITALIC),
                            0,nameEmailNumberEtc.length(), 0);
                    for (int i = 0; i < emails.length(); i++) {
                        if (((String) emails.get(i)).contains(nameEmailNumberEtc)) {
                            //c.setInt(Person.INT_INDEX_USE_EMAIL,i);
                            SpannableString match = new SpannableString(((String) emails.get(i)).replace(nameEmailNumberEtc,spannablecontent));
                            useperson.setInt(Person.INT_INDEX_USE_EMAIL,i);
                            Person tmp=new Person(new JSONObject(useperson.toString()));
                            res.add(privateDb.new SearchResult(match,tmp));
                        }
                    }
                }
            }
            /*
                    || c.getString(Person.STRING_PHONE).startsWith(nameEmailNumberEtc)
                    || c.getString(Person.STRING_EMAIL).startsWith(nameEmailNumberEtc)
                    ) {
                    ) {search
                if(ContactsClipCONTACT_TYPE==ContactsSelectedClipboard.CONTACTS_TYPE_NUMBER && !c.getString(Person.STRING_PHONE).isEmpty())
                    res.add(c);
                else if(ContactsClipCONTACT_TYPE==ContactsSelectedClipboard.CONTACTS_TYPE_EMAIL && !c.getString(Person.STRING_EMAIL).isEmpty() )
                    res.add(c);
                else
                    res.add(c);
                */

        }
        return res;
    }
    /*
    public static ArrayList<Person> find(String nameEmailNumberEtc, boolean useNumber, boolean useEmail) {

        ArrayList<Person> res = new ArrayList<Person>();
        nameEmailNumberEtc=nameEmailNumberEtc.toLowerCase();
        for(Person c: DB.contacts) {
            if(c.getString(Person.STRING_NAME).startsWith(nameEmailNumberEtc)
                    || (c.getString(Person.STRING_EMAIL).startsWith(nameEmailNumberEtc))
                    || (c.getString(Person.STRING_PHONE).startsWith(nameEmailNumberEtc))
                    ) {

                res.add(c);
            }
        }

        return res;
    }
*/
    public static Person getWithTelephone(Context context, String telephoneNumber) {
        if(telephoneNumber!=null) {
            telephoneNumber=Device.FixPhoneNumber(context, telephoneNumber);
            //Log.e("NUM", telephoneNumber);
            for(Person tmp: DB.contacts) {
                JSONArray phones = tmp.getJSONArray(Person.JSONARRAY_PHONE);
                for(int i=0; i< phones.length(); i++) {
                    if(((String)phones.get(i)).equals(telephoneNumber)) {
                        tmp.setInt(Person.INT_INDEX_USE_PHONE,i);
                        return tmp;
                    }
                }
                /*
                HashMap<String,String> nums = tmp.getNumbers();
                if(nums!=null && !nums.isEmpty()) {
                    Set<String> keys = nums.keySet();
                    for(String key: keys) {
                        String num=nums.get(key);
                        if(num!=null && num.equals(telephoneNumber))
                            return tmp;
                    }

					String num = nums.get(Person.TYPE_CNUM_MAIN);
					if(num!=null && num.equals(telephoneNumber))
						return tmp;

                }
                */
            }
        }
        return null;
    }
    public static Person getWithTelephoneConcatEnd(Context context, String telephoneNumber) {
        if(telephoneNumber!=null) {
            String fixtelephoneNumber=Device.FixPhoneNumber(context, telephoneNumber);
            //Log.e("NUM", telephoneNumber);
            fixtelephoneNumber=fixtelephoneNumber.replaceAll(" ", "");
            for(Person tmp: DB.contacts) {

                JSONArray phones = tmp.getJSONArray(Person.JSONARRAY_PHONE);
                for(int i=0; i< phones.length(); i++) {
                    String number=((String)phones.get(i));
                    number = number.replaceAll(" ", "");
                    if(number.endsWith(fixtelephoneNumber)) {
                        tmp.setInt(Person.INT_INDEX_USE_PHONE,i);
                        return tmp;
                    } else if(number.equals(telephoneNumber)) {
                        tmp.setInt(Person.INT_INDEX_USE_PHONE,i);
                        return tmp;
                    }
                }

                /*
                String number = tmp.getString(Person.STRING_PHONE);
                if(number!=null) {
                    number = number.replaceAll(" ", "");
                    if(number.endsWith(telephoneNumber)) {
                        return tmp;
                    }
                }
                */
            }
        }
        return null;
    }
    public static void update(Person person) {
        DB.personDb.update(person);
        for(int i=0; i<DB.contacts.size(); i++) {
            Person p= DB.contacts.get(i);
            if(p.getLong(Person.LONG_ID)==person.getLong(Person.LONG_ID)) {
                DB.contacts.set(i, person);
                break;
            }
        }
    }
    public static Person getWithEmail(String email) {
        if(email!=null && email.length()>0) {
            for(Person tmp: DB.contacts) {
                JSONArray emails = tmp.getJSONArray(Person.JSONARRAY_EMAIL);
                for(int i=0; i< emails.length(); i++) {
                    if(((String)emails.get(i)).equals(email)) {
                        tmp.setInt(Person.INT_INDEX_USE_PHONE,i);
                        return tmp;
                    }
                }
            }
        }
        return null;
    }
    public static List<Person> getContactsAll() {
        return DB.contacts;
    }
    public static List<Person> getContactsHasEmail() {
        List<Person> withemail = new ArrayList<Person>();
        for(Person p: DB.contacts) {
            JSONArray emails = p.getJSONArray(Person.JSONARRAY_EMAIL);
            if(emails.length()>0)
                withemail.add(p);

            /*
            String str=p.getString(Person.STRING_EMAIL);
            if(str!=null && !str.isEmpty()) {
                withemail.add(p);
            }
            */
        }
        return withemail;
    }




    // full contact functions


	public static int sizeFull() {
		return DB.contacts.size();
	}
	public static PersonFull getFullWithId(Context context, String id) {
		if(id!=null) {
			//telephoneNumber=Device.FixPhoneNumber(context, telephoneNumber);
			//Log.e("NUM", telephoneNumber);
			for(PersonFull tmp: DB.contactsFull) {
				if(tmp.getId().equals(id))
					return tmp;
				
			}
		}
		return null;
	}
	public static PersonFull getFull(int index) {
		return DB.contactsFull.get(index);
	}
	
	
	public static ArrayList<PersonFull> findFull(String nameEmailNumberEtc, int ContactsClipCONTACT_TYPE) {
		
		ArrayList<PersonFull> res = new ArrayList<PersonFull>();
		nameEmailNumberEtc=nameEmailNumberEtc.toLowerCase();
		for(PersonFull c: DB.contactsFull) {
			if(c.getNickname().toLowerCase().startsWith(nameEmailNumberEtc)
					|| c.getMainEmail().startsWith(nameEmailNumberEtc)
					|| c.getMainNumber().startsWith(nameEmailNumberEtc)
					) {
				if(ContactsClipCONTACT_TYPE==ContactsSelectedClipboard.CONTACTS_TYPE_NUMBER && !c.getMainNumber().isEmpty())
					res.add(c);
				else if(ContactsClipCONTACT_TYPE==ContactsSelectedClipboard.CONTACTS_TYPE_EMAIL && !c.getMainEmail().isEmpty() )
					res.add(c);
				else
					res.add(c);
			}
		}
		return res;
	}
	public static ArrayList<PersonFull> findFill(String nameEmailNumberEtc, boolean useNumber, boolean useEmail) {
		
		ArrayList<PersonFull> res = new ArrayList<PersonFull>();
		nameEmailNumberEtc=nameEmailNumberEtc.toLowerCase();
		for(PersonFull c: DB.contactsFull) {
			if(c.getNickname().toLowerCase().startsWith(nameEmailNumberEtc)
					|| (useEmail && c.getMainEmail().startsWith(nameEmailNumberEtc))
					|| (useNumber && c.getMainNumber().startsWith(nameEmailNumberEtc))
					) {
				
				res.add(c);
			}
		}
		
		return res;
	}
	
	public static PersonFull getWithTelephoneFull(Context context, String telephoneNumber) {
		if(telephoneNumber!=null) {
			//telephoneNumber=Device.FixPhoneNumber(context, telephoneNumber);
			//Log.e("NUM", telephoneNumber);
			for(PersonFull tmp: DB.contactsFull) {
				HashMap<String,String> nums = tmp.getNumbers();
				if(nums!=null && !nums.isEmpty()) {
					Set<String> keys = nums.keySet();
					for(String key: keys) {
						String num=nums.get(key);
						if(num!=null && num.equals(telephoneNumber))
							return tmp;
					}
					/*
					String num = nums.get(Person.TYPE_CNUM_MAIN);
					if(num!=null && num.equals(telephoneNumber))
						return tmp;
						*/
				}
			}
		}
		return null;
	}
	public static PersonFull getFullWithTelephoneConcatEnd(Context context, String telephoneNumber) {
		if(telephoneNumber!=null) {
			//telephoneNumber=Device.FixPhoneNumber(context, telephoneNumber);
			//Log.e("NUM", telephoneNumber);
			telephoneNumber=telephoneNumber.replaceAll(" ", "");
			for(PersonFull tmp: DB.contactsFull) {
				String number = tmp.getMainNumber();
				if(number!=null) {
					number = number.replaceAll(" ", "");
					if(number.endsWith(telephoneNumber)) {
						return tmp;
					}
				}
			}
		}
		return null;
	}
	public static PersonFull getFullWithEmail(String email) {
		if(email!=null && email.length()>0) {
		for(PersonFull tmp: DB.contactsFull) {
			if(email.equals(tmp.getMainEmail()))
				return tmp;
		}
		}
		return null;
	}
	public static List<PersonFull> getFullContactsAll() {
		return DB.contactsFull;
	}
	public static List<PersonFull> getFullContactsHasEmail() {
		List<PersonFull> withemail = new ArrayList<PersonFull>();
		for(PersonFull p: DB.contactsFull) {
			if(p.getEmails()!=null && !p.getEmails().isEmpty()) {
				withemail.add(p);
			}
		}
		return withemail;
	}


    public static String getContactLookupKey(Context context,String personId) {
        String key=null;
        ContentResolver cr = context.getContentResolver();
        Uri contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Identity.LOOKUP_KEY
        };
        //String SELECTION = ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "='1'";
        String SELECTION = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?";
        String[] vals = new String[]{personId};
        //String sortOrder =""+  ContactsContract.Contacts.DISPLAY_NAME +" ASC";
        Cursor cur = cr.query(contactUri, PROJECTION, SELECTION, vals, null );
        if (cur !=null && cur.getCount() > 0) {
            while (cur.moveToNext()) {
                key= cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Identity.LOOKUP_KEY));
            }
        }
        cur.close();
        cr=null;
        return key;
    }
    public static PersonFull getPeronFullWithId(Context context, String personId){

        PersonFull contact=null;//new ArrayList<Person>();
        //BLog.e("CC","getting with pid: "+personId);
        ContentResolver cr = context.getContentResolver();
        //Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        //       null, null, null, null);
        //Uri contactUri = PhoneLookup.CONTENT_FILTER_URI;
        Uri contactUri = ContactsContract.Contacts.CONTENT_URI;//ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        //Uri contactUri = RawContacts.CONTENT_URI;
        String SELECTION = ContactsContract.Contacts._ID +" = ?";
        String[] vals = new String[]{personId};

        Cursor cur = cr.query(contactUri, null, SELECTION, vals, null );

        if (cur !=null && cur.getCount() ==1) {
            while (cur.moveToNext()) {
                contact=new PersonFull();
//BLog.e("CC",""+cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
                contact.setId(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
                contact.setName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

                //BLog.e("PERSONFULL","PF: "+cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));

                contact.setThumbnailId(cur.getLong(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_ID)));
                //BLog.e("C_DB", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                //contact.addNumber(PersonFull.TYPE_CNUM_MAIN, Device.FixPhoneNumber(context, cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
                //contact.addEmail(Person.TYPE_CEMAIL, cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                //BLog.e("comtact", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                contact.setLookupKey(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Identity.LOOKUP_KEY)));
                Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " =?", new String[]{contact.getId()}, null);
                while (emails.moveToNext())
                {
                    contact.addEmail(PersonFull.TYPE_CEMAIL,emails.getString(emails.getColumnIndex(Email.DATA)));
                    break;
                }
                emails.close();

                Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                        new String[]{contact.getId()}, null);

                while (pCur.moveToNext()) {
                    if(pCur.isFirst()) {
                        //BLog.e("CONTACT",Device.FixPhoneNumber(context, pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
                        contact.addNumber(PersonFull.TYPE_CNUM_MAIN, Device.FixPhoneNumber(context, pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));

                    } else {
                        contact.addNumber(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),
                                pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    }
                }
                pCur.close();

                //contact.loadThumbnailAsyncTask(context);

            }
        }
        cur.close();
        cr=null;

        return contact;
    }
    public static void deleteContacts() {
        DB.personDb.deleteAll();
    }

    private static boolean isRefreshing=false;

    public static boolean isRefreshing() {
        return DB.isRefreshing;
    }

    public static void mapContactsFullToContacts(Context context) {
        if(Validator.isValidCaller() && DB.contactsFull!=null && !DB.contactsFull.isEmpty()) {
            DB.isRefreshing=true;
            if(DB.contacts.isEmpty()) {
                DB.contacts=new ArrayList<Person>();
                DB.personDb.deleteAll();
            } else {
                for(PersonFull pf: DB.contactsFull) {
                    Person ps=getFromPersonFull(pf);
                    Person hasperson = ContactsDb.getWithPersonId(context,pf.getId());
                    if(hasperson!=null) {
                        boolean hasChange=false;
                        if(!hasperson.getString(Person.STRING_NAME).equals(ps.getString(Person.STRING_NAME))) {
                            hasperson.setString(Person.STRING_NAME, ps.getString(Person.STRING_NAME));
                            hasChange=true;
                        }
                        if(hasperson.getJSONArray(Person.JSONARRAY_EMAIL).length()!=ps.getJSONArray(Person.JSONARRAY_EMAIL).length()) {
                            hasperson.setJSONArray(Person.JSONARRAY_EMAIL, ps.getJSONArray(Person.JSONARRAY_EMAIL));
                            hasChange=true;
                        }
                        if(hasperson.getJSONArray(Person.JSONARRAY_PHONE).length()!=ps.getJSONArray(Person.JSONARRAY_PHONE).length()) {
                            hasperson.setJSONArray(Person.JSONARRAY_PHONE, ps.getJSONArray(Person.JSONARRAY_PHONE));
                            hasChange=true;
                        }
                        if(hasChange) {
                            //BLog.e("UPDATING","has person: "+hasperson.toString());
                            ContactsDb.update(hasperson);
                        }
                    }
                }
            }


            for(PersonFull pf: DB.contactsFull) {
                Person ps=getFromPersonFull(pf);
                Person hasperson = ContactsDb.getWithPersonId(context,pf.getId());
                if(hasperson==null) {
                    long id = DB.personDb.add(ps);
                    //BLog.e("ADDSLIM", "id: " + id);
                    ps.setLong(Person.LONG_ID, id);
                    DB.contacts.add(ps);
                }
            }
            DB.isRefreshing=false;
        }
    }

    private static Person getFromPersonFull(PersonFull pf) {
        Person ps = new Person();
        ps.setString(Person.STRING_NAME, pf.getNickname());

        JSONArray phones = new JSONArray();
        Set<String> nk=pf.getNumbers().keySet();
        for(String numkey: nk) {
            phones.put(pf.getNumbers().get(numkey));
        }

        ps.setJSONArray(Person.JSONARRAY_PHONE, phones);

        JSONArray emails = new JSONArray();
        if(pf.getEmails()!=null) {
            Set<String> ek = pf.getEmails().keySet();
            for (String numkey : ek) {
                emails.put(pf.getEmails().get(numkey));
            }
        }
        ps.setJSONArray(Person.JSONARRAY_EMAIL,emails);
                /*
                ps.setString(Person.STRING_EMAIL,pf.getMainEmail());

                */
        ps.setString(Person.STRING_PERSON_ID,pf.getId());
        ps.setLong(Person.LONG_THUMBNAIL_ID,pf.getThumbnailId());
        return ps;
    }
    public static void loadContactsFull(Context context){
    	
    	ArrayList<PersonFull> tcontacts=new ArrayList<PersonFull>();
    	
        ContentResolver cr = context.getContentResolver();
        //Cursor cur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        //       null, null, null, null);
        //Uri contactUri = PhoneLookup.CONTENT_FILTER_URI;
        Uri contactUri = ContactsContract.Contacts.CONTENT_URI; //ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        //Uri contactUri = RawContacts.CONTENT_URI;
        String[] PROJECTION = new String[] {
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER,
                ContactsContract.CommonDataKinds.Phone.PHOTO_ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Identity.LOOKUP_KEY,
                ContactsContract.CommonDataKinds.Phone.NUMBER

        };
	    //String SELECTION = ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + "='1'";
        //String SELECTION = ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?";
        String sortOrder =""+  ContactsContract.Contacts.DISPLAY_NAME +" ASC";
        try {
            Cursor cur = cr.query(contactUri, null, ContactsContract.Contacts.HAS_PHONE_NUMBER + "> ?", new String[]{"0"}, sortOrder);

            if (cur != null && cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    PersonFull contact = new PersonFull();
//BLog.e("CC",""+cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
                    contact.setId(cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID)));
                    contact.setName(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                    contact.setThumbnailId(cur.getLong(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_ID)));
                    //BLog.e("C_DB", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                    //contact.addNumber(PersonFull.TYPE_CNUM_MAIN, Device.FixPhoneNumber(context, cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
                    //contact.addEmail(Person.TYPE_CEMAIL, cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                    //BLog.e("comtact", cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)));
                    contact.setLookupKey(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Identity.LOOKUP_KEY)));
                    //String emailId=cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));

                    Cursor emails = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null, ContactsContract.CommonDataKinds.Email.CONTACT_ID + " =?", new String[]{contact.getId()}, null);
                    while (emails.moveToNext()) {
                        contact.addEmail(PersonFull.TYPE_CEMAIL, emails.getString(emails.getColumnIndex(Email.DATA)));
                        break;
                    }
                    emails.close();

                    //if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER))) > 0) {
                    // get the phone number
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{contact.getId()}, null);

                    while (pCur.moveToNext()) {
                        if (pCur.isFirst()) {
                            //BLog.e("CONTACT",Device.FixPhoneNumber(context, pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
                            contact.addNumber(PersonFull.TYPE_CNUM_MAIN, Device.FixPhoneNumber(context, pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));

                        } else {
                            contact.addNumber(pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)),
                                    pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
                        }
                    }
                    pCur.close();


                    //}
                    //contact.loadThumbnailAsyncTask(context);
                    tcontacts.add(contact);
                }
            }
            cur.close();
        } catch(Exception e) {}
      cr=null;

      DB.contactsFull=tcontacts;
    }

}
