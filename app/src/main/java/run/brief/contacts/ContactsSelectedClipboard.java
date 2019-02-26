package run.brief.contacts;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import run.brief.beans.Person;
import run.brief.util.json.JSONArray;

public class ContactsSelectedClipboard {
	
	public static final int CONTACTS_TYPE_ALL=0;
	public static final int CONTACTS_TYPE_NUMBER=1;
	public static final int CONTACTS_TYPE_EMAIL=2;
	
	private static final ContactsSelectedClipboard C=new ContactsSelectedClipboard();
	
	private HashMap<String,Person> contacts=new HashMap<String,Person>();
	private ArrayList<ContactsDb.SearchResult> searchResults=new ArrayList<ContactsDb.SearchResult>();
	private String lastSearch;
	
	public static void set(HashMap<String,Person> allPersonSlims) {
		C.contacts=allPersonSlims;
	}
	public static HashMap<String,Person>  get() {
		return C.contacts;
	}
	public static Person get(String PersonSlimId) {
		return C.contacts.get(PersonSlimId);
	}
	public static void addPerson(Person p) {
		C.contacts.put(p.getString(Person.STRING_PERSON_ID), p);
		
	}
	public static void removePerson(Person p) {
		C.contacts.remove(p.getString(Person.STRING_PERSON_ID));
		
	}
	public static void clear() {
		C.contacts.clear();
	}
	public static int size() {
		return C.contacts.size();
	}
	public static boolean isEmpty() {
		return C.contacts.isEmpty();
	}
	public static String getLastSearch() {
		return C.lastSearch;
	}

	public synchronized static ArrayList<ContactsDb.SearchResult>  search(String nameNumberEmail, int CONTACTS_TYPE_) {
		C.lastSearch=nameNumberEmail;
		List<ContactsDb.SearchResult> res=ContactsDb.find(nameNumberEmail,CONTACTS_TYPE_);
		ArrayList<ContactsDb.SearchResult> resfil=new ArrayList<ContactsDb.SearchResult>();
		for(ContactsDb.SearchResult sr: res) {
			boolean add=true;
			if(C.contacts.get(sr.person.getString(Person.STRING_PERSON_ID))!=null)
				add=false;
			if(add)
				resfil.add(sr);
		}
		
		C.searchResults=resfil;
		return C.searchResults;
	}
	public static void clearSearch() {
		C.searchResults.clear(); C.lastSearch="";
	}
	public static ArrayList<ContactsDb.SearchResult> getSearchResults() {
		return C.searchResults;
	}



	public static String getNamesSummary() {
		Iterator<String> it = C.contacts.keySet().iterator();
		StringBuilder txt = new StringBuilder();
		while(it.hasNext()) {
			Person p = ContactsSelectedClipboard.get(it.next());
			txt.append(":ps");
			txt.append(p.getString(Person.STRING_NAME));

		}
		return txt.toString();
	}
    public static List<String> getEmailSummaryAsList(String emailSummary) {
        List<String> list = new ArrayList<String>();
        String[] sp=emailSummary.split(",");
        if(sp!=null && sp.length>0) {
            for(int i=0; i<sp.length; i++) {
                list.add(sp[i]);
            }
        }
        return list;
    }
    public static List<String> getEmailSummaryAsListInet(String emailSummary) {
        List<String> list = new ArrayList<String>();
        String[] sp=emailSummary.split(",");
        if(sp!=null && sp.length>0) {
            for(int i=0; i<sp.length; i++) {
                list.add(sp[i]);
            }
        }
        return list;
    }
	public static String getEmailSummary() {
        StringBuilder txt = new StringBuilder();
        if(C.contacts!=null) {
            Iterator<String> it = C.contacts.keySet().iterator();

            boolean first = true;
            while (it.hasNext()) {
                Person p = ContactsSelectedClipboard.get(it.next());
                JSONArray emails = p.getJSONArray(Person.JSONARRAY_EMAIL);
                if (emails != null) {
                    String me = (String) emails.get(p.getInt(Person.INT_INDEX_USE_EMAIL));
                    if (me.length() > 0) {
                        if (first)
                            first = false;
                        else
                            txt.append(",");
                        txt.append(me);
                        //BLog.e("FOUND", ""+p.getMainEmail());
                    }
                }
            }
        }
		return txt.toString();
	}
	public static void addFromEmailSummary(Activity activity,String emailSummary) {
		if(emailSummary!=null && emailSummary.length()>0) {
			String[] emails = emailSummary.split(",");
			if(emails!=null && emails.length>0) {
				for(int i=0; i<emails.length; i++) {
					//BLog.e("EM", "em: "+emails[i]);
					Person p = ContactsDb.getWithEmail(emails[i]);
					if(p==null) {
                        p=Person.getNewUnknownPerson(activity,null,emails[i]);

						//BLog.e("EM", "em: null PersonSlim");
					}
                    addPerson(p);
				}
			}
		}
	}
}
