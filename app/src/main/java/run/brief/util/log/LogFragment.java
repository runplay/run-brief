package run.brief.util.log;

import android.app.Activity;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import run.brief.b.ActionBarManager;
import run.brief.R;
import run.brief.b.State;
import run.brief.util.Cal;

public class LogFragment extends ListFragment {
	
	View view;
	private static Activity activity;
	private static ListView listnews;
	private static LinearLayout options;
	private static AdapterListLogs adapter;
	
	private static float touchX;
	private static float touchY;
	private static int currentPosition;
	private static boolean optionsOpen=false;
	private static boolean optionsBuilt=false;
	
	//private static boolean awaitingAction=false;
	private static long lastscrolltime;
	private static final long WAIT_AFTER_SCROLL_MILLIS=500;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		activity=getActivity();

		view=inflater.inflate(R.layout.logs,container, false);

		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();

		State.setCurrentSection(State.SECTION_LOGS);
		ActionBarManager.setActionBarMenu(getActivity(),  activity.getString(R.string.title_logs),R.menu.basic, R.color.actionbar_basic);

		listnews = (ListView) activity.findViewById(android.R.id.list);
		adapter = new AdapterListLogs(activity);
		listnews.setAdapter(adapter);
		listnews.setTextFilterEnabled(true);

  }


	public static long getLastScrolledTime() {
		return lastscrolltime;
	}
	public static boolean allowDrag() {
		if(lastscrolltime+WAIT_AFTER_SCROLL_MILLIS<Cal.getUnixTime())
			return true;
		return false;
	}
	public static void refreshList() {
		//adapter.notifyDataSetChanged();
		listnews.refreshDrawableState();
	}
	
	public static void setCurrentPosition(int position) {
		currentPosition=position;
	}
	public static int getCurrentPosition() {
		return currentPosition;
	}
  public static void setTouchXY(float x, float y) {
	  touchX=x;
	  touchY=y;
  }
  public static float getTouchX() {
	  return touchX;
  }
  public static float getTouchY() {
	  return touchY;
  }
  
  




}
