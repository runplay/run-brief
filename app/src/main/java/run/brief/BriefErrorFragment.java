package run.brief;


import run.brief.secure.BOAUTHFragment;
import run.brief.R;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class BriefErrorFragment extends BOAUTHFragment {
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		//ActionBarManager.setActionBarMenu(getActivity(), R.menu.brief_home);
		return inflater.inflate(R.layout.b_error,container, false);
	}
	
}
