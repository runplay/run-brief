package run.brief.settings;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import run.brief.b.B;
import run.brief.b.Bgo;
import run.brief.R;
import run.brief.b.State;
import run.brief.beans.BriefSettings;
import run.brief.beans.Theme;


public class ThemeDialog extends Dialog {


	//private BRefreshable refreshFragment;
	//public static boolean shouldRefresh=false;

    private LinearLayout view;
    private List<Theme> themes=new ArrayList<Theme>();
    private Activity activity;
    private ListView list;
    private LoadListView loadList;
    private Handler handler = new Handler();
    private ThemeAdapter themeAdapter;

    public void buildThemes() {
        themes.add(new Theme(BriefSettings.THEME_DEFAULT,R.drawable.bg_port_brief_bubbles,R.drawable.bg_land_brief_bubbles));
        themes.add(new Theme(BriefSettings.THEME_BLUE_CLOG,R.drawable.bg_port_blue_clog,R.drawable.bg_land_blue_clog));
        themes.add(new Theme(BriefSettings.THEME_GREEN_CLOUD,R.drawable.bg_port_cloud_green,R.drawable.bg_land_cloud_green));
        themes.add(new Theme(BriefSettings.THEME_WORK_DAY,R.drawable.bg_port_grey_day,R.drawable.bg_land_grey_day));
    }
	public ThemeDialog(Activity activity) {
		super(activity);
		this.activity=activity;
		//this.refreshFragment=refreshFragment;
        buildThemes();

        view = new LinearLayout(activity);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setLayoutParams(lp);
        this.setContentView(view);
        this.setTitle(activity.getResources().getString(R.string.sms_menu_options));

        list = new ListView(activity);
        ListView.LayoutParams lip = new ListView.LayoutParams(ListView.LayoutParams.MATCH_PARENT,ListView.LayoutParams.MATCH_PARENT);
        list.setLayoutParams(lip);

        list.setEmptyView(activity.getLayoutInflater().inflate(R.layout.wait,null));
        view.addView(list);
        list.setAdapter(new ThemeAdapter(true));
        list.setOnItemClickListener(openListener);
        //if(!showEmpty)

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadList=new LoadListView();
                loadList.execute(1);
            }
        },100);



		//context.getMenuInflater().inflate(R.menu.notes_home_popup, popupMenu.getMenu());
	}
    public void loadFinished() {
        list.setAdapter(themeAdapter);
        list.invalidate();
    }
    private class LoadListView extends AsyncTask<Integer, Void, Boolean> {
        Bitmap bitmap;
        int position;
        @Override
        protected Boolean doInBackground(Integer... params) {
            themeAdapter=new ThemeAdapter(false);

            return Boolean.TRUE;

        }
        @Override
        protected void onPostExecute(Boolean result) {
            loadFinished();
        }

    }
    public class ThemeAdapter extends BaseAdapter {
       //public static int currentSelectedPosition;
        private boolean showEmpty;
        public ThemeAdapter(boolean showEmpty) {
            this.showEmpty=showEmpty;
        }

        public int getCount() {
            if(showEmpty)
                return 1;
            return themes.size();
        }

        public Object getItem(int position) {
            return themes.get(position);
        }

        public long getItemId(int position) {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent) {
            Theme theme = themes.get(position);

            if(showEmpty) {
                AbsListView.LayoutParams tlayp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
                RelativeLayout view = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.wait,null);
                view.setLayoutParams(tlayp);
                return view;
            } else {
                LinearLayout view;
                if (convertView == null) {
                    AbsListView.LayoutParams tlayp = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
                    view = new LinearLayout(activity);
                    view.setOrientation(LinearLayout.HORIZONTAL);
                    view.setLayoutParams(tlayp);
                    view.setPadding(10,5,10,5);

                    ImageView timg = new ImageView(activity);
                    AbsListView.LayoutParams tlayi = new AbsListView.LayoutParams(100, 70);
                    timg.setLayoutParams(tlayi);
                    timg.setId(R.id.theme_list_img);

                    TextView ttext = new TextView(activity);
                    AbsListView.LayoutParams tlayt = new AbsListView.LayoutParams(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
                    ttext.setGravity(Gravity.CENTER_VERTICAL);
                    //ttext.setTextSize(Float.valueOf(ttext.getTextSize() * 1.1f).intValue());
                    ttext.setPadding(10,0,0,0);
                    ttext.setLayoutParams(tlayt);
                    ttext.setId(R.id.theme_list_text);

                    view.addView(timg);
                    view.addView(ttext);

                    B.addStyleBold(ttext,B.FONT_LARGE);
                } else {
                    view = (LinearLayout) convertView;
                }
                TextView ttext = (TextView) view.findViewById(R.id.theme_list_text);
                ImageView timg = (ImageView) view.findViewById(R.id.theme_list_img);
                timg.setImageBitmap(B.getThemeBackgroundThumbnail(activity, theme));
                ttext.setText(theme.name);
                return view;
            }

        }
    }


    public OnItemClickListener openListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Theme theme = themes.get(position);
            if(theme.name.equals(State.getSettings().getString(BriefSettings.STRING_THEME))) {
                dismiss();
            } else {
                BriefSettings settings = State.getSettings();
                settings.setString(BriefSettings.STRING_THEME,theme.name);
                State.setSettings(settings);
                State.getSettings().save();
                B.addBackgroundImage(activity,true);
                dismiss();
                Bgo.refreshCurrentFragment(activity);
            }
        }
    };
	public OnDismissListener onDismiss = new OnDismissListener() {
		@Override
		public void onDismiss(DialogInterface intf) {

		}
	};
	public Button.OnClickListener onSaveNoteClick = new Button.OnClickListener() {
		@Override 
		public void onClick(View view) {
			
			dismiss();
		}
	};

}
