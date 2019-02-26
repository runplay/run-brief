package run.brief.b;

import android.app.Fragment;
import android.view.Menu;
import android.view.View;

import run.brief.R;
import run.brief.menu.BriefMenu;


public class BFragment extends Fragment {

	//protected ActionModeBack amb;
	protected Menu menu;

	@Override
	public void onPause() {
		super.onPause();
		//if(amb!=null)
		//	amb.done();
		//ViewManagerText.dismissPopup();
		
	}
	public BFragment() {
		this.setHasOptionsMenu(true);
		if(BriefMenu.isMenuShowing())
			BriefMenu.hideMenu();
		//BLog.e("CR", "create new BFragment");
	}

	@Override
	public void onResume() {
        View gotop=getActivity().findViewById(R.id.main_gotop);
        if(gotop!=null)
            gotop.setVisibility(View.GONE);
		super.onResume();
        //BLog.e("BFrag", "resume: "+this.getClass().getName());
		//this.getView().setFocusableInTouchMode(true);
/*
		this.getView().setOnKeyListener( new OnKeyListener()
		{
		    @Override
		    public boolean onKey( View v, int keyCode, KeyEvent event )
		    {
		    	BLog.e("BACKCALL", "fragment back key pressed");
		        if( keyCode == KeyEvent.KEYCODE_BACK )
		        {
		        	
		            return true;
		        }
		        return false;
		    }
		} );
		*/
	}
	/*
	public void refresh() {
		
		LinearLayout emoticonsCover = (LinearLayout) getActivity().findViewById(R.id.footer_for_emoticons);
		if(emoticonsCover!=null) {
			BLog.e("BLASS", "on refesh called");
			emoticonsCover.removeAllViews();
			emoticonsCover.setVisibility(LinearLayout.GONE);
			
		}
	}
	*/
}
