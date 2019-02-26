package run.brief;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import run.brief.b.ActionBarManager;
import run.brief.b.ActionBarManager.bMenuItem;
import run.brief.b.B;

public class ActionBarCustomAdapter extends ArrayAdapter<bMenuItem> implements SpinnerAdapter{

    Context context;
    int textViewResourceId;
    private static ArrayList<bMenuItem> arrayList;
    AbsListView.LayoutParams lp;
    AbsListView.LayoutParams lps;
    boolean isSelected=false;

	public ActionBarCustomAdapter(Context context, int textViewResourceId,  ArrayList<bMenuItem> arrayList) {
        super(context, textViewResourceId, arrayList);
        lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.MATCH_PARENT);
        lps = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT,AbsListView.LayoutParams.WRAP_CONTENT);
        this.context = context;
        this.textViewResourceId = textViewResourceId;
        try {
        	arrayList = (ArrayList<bMenuItem>) arrayList;
        } catch(Exception e) {}

    }
	public static void update(Activity context) {
		//arrayList = ActionBarManager.getMenuSpinnerItems(context);
	}
    @Override
     public View getDropDownView(int position, View theView, ViewGroup parent){
    	
       if (theView == null)
       {
         LayoutInflater vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
         //convertView = vi.inflate(android.R.layout.simple_spinner_dropdown_item, null);
         theView = vi.inflate(R.layout.actionbar_spinner_item_next, null);
         //theView = new TextView(context);
         
       }
       ImageView img = (ImageView) theView.findViewById(R.id.actionbar_spinner_img);
       TextView head = (TextView) theView.findViewById(R.id.actionbar_spinner_text);
       TextView subhead = (TextView) theView.findViewById(R.id.actionbar_spinner_subtext);

       B.addStyle(new TextView[]{head, subhead});

       if(arrayList!=null && arrayList.size()>position) {
    	   bMenuItem bm = arrayList.get(position);

    	   head.setText(bm.title);//after changing from ArrayList<String> to ArrayList<Object>

    	   img.setBackground(bm.img);
    	   //tv.setHeight(80);
    	   if(isSelected) {
    		   img.setVisibility(View.GONE);
    		   head.setPadding(0, 0, 0, 0);
    		   head.setTextSize(16);
    		   subhead.setPadding(0, -4, 0, 0);
    		   subhead.setTextSize(10F);
    	   }  else {
    		   img.setVisibility(View.VISIBLE);
    		   //head.setPadding(0, 3, 3, 3);
    		   head.setPadding(0, 0, 0, 0);
    		   head.setTextSize(20);
    		   
    		   subhead.setPadding(0, 3, 3, 3);
    		   subhead.setTextSize(14F);
    	   }
    	   if(!isSelected && ActionBarManager.getCurrent()==bm.rMenu) {
    		   head.setTextColor(context.getResources().getColor(R.color.white));
    		   theView.setBackgroundColor(context.getResources().getColor(android.R.color.background_dark));
    	   } else {
    		   head.setTextColor(context.getResources().getColor(R.color.black));
    		   theView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));

    	   }
    	   
    	   
    	   subhead.setText(bm.subtext);
    	   //BLog.e("DD", ActionBarManager.getCurrent()+" !!!");
    	   
       }

       return theView;
     }

	@Override
	public View getView(int pos, View cnvtView, ViewGroup prnt) {
		isSelected=true;
		View view = getDropDownView(pos, cnvtView, prnt);
		isSelected=false;
		return view;
	}


}