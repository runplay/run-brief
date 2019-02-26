package run.brief.d2d;

import java.util.ArrayList;
import android.content.Context;
import run.brief.beans.Brief;
import run.brief.beans.P2PChat;



public class P2PChatDb {
	
	private static final P2PChatDb P2PChatS = new P2PChatDb();
	
	private static final String DB_DEFAULT_ITEMS="P2PChats";
	
	private int countNew;
	private ArrayList<P2PChat>data;
	//private ArrayList<RssUserFeed> userdata;
	private P2PChatDbTable database;
	private boolean isLoaded=false;
	//public AsyncTask<RssUserFeed, Void, Integer> doload;
	
	public P2PChatDb() {
		//Load();
	}
	public static void clearNewCount() {
		P2PChatS.countNew=0;
	}
	public static int getNewCount() {
		return P2PChatS.countNew;
	}
	public static void addNewCount(int countNew) {
		P2PChatS.countNew=P2PChatS.countNew+countNew;
	}
	
	public static final P2PChatDbTable getItemsDatabase() {
		return P2PChatS.database;
	}
	
	public synchronized static void init(Context context) {
		if(P2PChatS.database==null) {
			P2PChatS.database=new P2PChatDbTable(context);
			P2PChatS.data=P2PChatS.database.getItems(0, 50);
			//SortP2PChats();
		}
	}
/*
	public static void SortP2PChats() {
		Collections.sort(P2PChatS.data, new Comparator<P2PChat>() {
		    public int compare(P2PChat m1, P2PChat m2) {
		        return (new Date(m2.getLong(P2PChat.LONG_DATE_CREATED)).compareTo(new Date(m1.getLong(P2PChat.LONG_DATE_CREATED))));
		    }
		});
	}
	*/
	public final static ArrayList<P2PChat> getAllItems() {
		return P2PChatS.data;
	}
	public final static int size() {
		return P2PChatS.data.size();

	}
	public static P2PChat getByIndex(int index) {
		try {
			return P2PChatS.data.get(index);
		} catch(Exception e) {}
		
		return null;
	}
	public static P2PChat getById(int id) {
		//P2PChat n=null;
		for(P2PChat t: P2PChatS.data) {
			if(t.getInt(P2PChat.INT_ID)==id)
				return t;
		}
		return null;

	}
	public static Brief getAsBrief(int index) {
		if(P2PChatS.data!=null && P2PChatS.data.size()>index){
			Brief b=new Brief(P2PChatS.data.get(index),index);
			
			return b;
			
		}
		return null;
	}
	public static void deleteAll() {
		P2PChatS.database.deleteAll();
		P2PChatS.data.clear();
	}
	public synchronized static boolean remove(P2PChat item) {
		if(item!=null) {
			P2PChatS.data.remove(item);
			P2PChatS.database.delete(item);
			return true;
		} else 
			return false;
		
	}
	public synchronized static long add(P2PChat item) {
		if(item!=null) {
			
			long id=P2PChatS.database.add(item);
			item.setLong(P2PChat.INT_ID, id);
			
			P2PChatS.data=P2PChatS.database.getItems(0, 50);
			return id;
		}
		return -1;
	}
	public synchronized static void update(P2PChat item) {
		if(item!=null) {
			
			P2PChatS.database.update(item);
			P2PChatS.data=P2PChatS.database.getItems(0, 50);
		}
		
	}
	public static boolean has(int id) {
		if(P2PChatS.database.hasItem(id))
			return true;
		return false;
		
	}

}
