package run.brief;


import run.brief.b.BFragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BriefInfoFragment extends BFragment {
	
	View view;
	Class<? extends Fragment> nextFragment;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view==null) {
			view=inflater.inflate(R.layout.b_info,container, false);
		}
		//ActionBarManager.setActionBarMenu(getActivity(), R.menu.brief_home);
		return view;
	}
	
	
	/*
	View view;
	Class<? extends Fragment> nextFragment;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if(view==null) {
			view=inflater.inflate(R.layout.b_info,container, false);
		}
		//ActionBarManager.setActionBarMenu(getActivity(), R.menu.brief_home);
		return view;
	}
	
	public void populateInfo(String message, Class<? extends Fragment> nextFragment, int Rdrawable) {
		TextView msg = (TextView) view.findViewById(R.id.info_message);
		if(msg!=null) {
			msg.setText(message);
			if(nextFragment!=null) {
				this.nextFragment=nextFragment;
				msg.setClickable(true);
				msg.setOnClickListener(nextFragListener);
			}
			if(Rdrawable!=0) {
				//LayoutParams params=new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
				Drawable img = view.getResources().getDrawable(Rdrawable );
				msg.setCompoundDrawablesWithIntrinsicBounds( img, null, null, null );
				//msg.setLayoutParams(params);
			}
		}
 	}
	public OnClickListener nextFragListener = new OnClickListener() {
		@Override
		public void onClick(View arg1) {
			// TODO Auto-generated method stub
			try {
				Bgo.openFragment(getActivity(), nextFragment);
			} catch(Exception e) {
				Errors.add("BriefInfoFragment",e);
			}
		}
	};	
	*/
}
