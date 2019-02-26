package run.brief.notes;

import android.content.Context;

import java.util.ArrayList;

import run.brief.BriefRatingsDb;
import run.brief.beans.Brief;
import run.brief.beans.Note;
import run.brief.secure.Validator;


public class NotesDb {
	
	private static final NotesDb NOTES = new NotesDb();
	
	private static final String DB_DEFAULT_ITEMS="notes";
	
	private int countNew;
	private ArrayList<Note>data;
	//private ArrayList<RssUserFeed> userdata;
	private NotesDbTable database;
	private boolean isLoaded=false;
	//public AsyncTask<RssUserFeed, Void, Integer> doload;

    public static NotesDb getDb() {
        if(Validator.isValidCaller())
            return NOTES;
        return
            null;
    }

	public NotesDb() {
		//Load();
	}

    public static boolean isEmpty() {
        if(NOTES.data!=null)
            return NOTES.data.isEmpty();
        return true;
    }
	public static void clearNewCount() {
		NOTES.countNew=0;
	}
	public static int getNewCount() {
		return NOTES.countNew;
	}
	public static void addNewCount(int countNew) {
		NOTES.countNew=NOTES.countNew+countNew;
	}
	
	public static final NotesDbTable getItemsDatabase() {
		return NOTES.database;
	}
	
	public synchronized static void init(Context context) {
		if(NOTES.database==null) {
			NOTES.database=new NotesDbTable(context);
			NOTES.data=NOTES.database.getItems(0, 20);
			//SortNotes();
		}
	}
/*
	public static void SortNotes() {
		Collections.sort(NOTES.data, new Comparator<Note>() {
		    public int compare(Note m1, Note m2) {
		        return (new Date(m2.getLong(Note.LONG_DATE_CREATED)).compareTo(new Date(m1.getLong(Note.LONG_DATE_CREATED))));
		    }
		});
	}
	*/
	public final static ArrayList<Note> getAllItems() {
		return NOTES.data;
	}
	public final static int size() {
		return NOTES.data.size();

	}
	public static Note getByIndex(int index) {
		try {
			return NOTES.data.get(index);
		} catch(Exception e) {}
		
		return null;
	}
	public static Note getById(int id) {
		//Note n=null;
		for(Note t: NOTES.data) {
			if(t.getInt(Note.INT_ID)==id)
				return t;
		}
		return null;

	}
	public static Brief getAsBrief(int index) {
		if(NOTES.data!=null && NOTES.data.size()>index){
			Brief b=new Brief(NOTES.data.get(index),index);
			
			return b;
			
		}
		return null;
	}
	private static void deleteAll() {
		NOTES.database.deleteAll();
		NOTES.data.clear();
	}
	public synchronized static boolean remove(Note item) {
		if(item!=null) {
			Brief b = new Brief(item, 0);
			if(b!=null) {
				BriefRatingsDb.remove(b.getRatingsIdentifier());
			}
			NOTES.data.remove(item);
			NOTES.database.delete(item);
			return true;
		} else 
			return false;
		
	}
	public synchronized static long add(Note item) {
		if(item!=null) {
			
			long id=NOTES.database.add(item);
			item.setLong(Note.INT_ID, id);
			
			NOTES.data=NOTES.database.getItems(0, 50);
			return id;
		}
		return -1;
	}
	public synchronized static void update(Note item) {
		if(item!=null) {
			
			NOTES.database.update(item);
			NOTES.data=NOTES.database.getItems(0, 50);
		}
		
	}
	public static boolean has(int id) {
		if(NOTES.database.hasItem(id))
			return true;
		return false;
		
	}

}
