package run.brief.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.TextView;

import run.brief.b.B;
import run.brief.BriefManager;
import run.brief.b.Device;
import run.brief.R;
import run.brief.b.State;
import run.brief.beans.BriefSettings;
import run.brief.util.BriefActivityManager;

public class SettingsBriefFragment extends Fragment {
	View view;
	private RadioButton stylePods;
	private RadioButton styleColors;
	private RadioButton stylePlain;
	private Activity activity;
    private ThemeDialog dialog;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view=inflater.inflate(R.layout.settings_brief,container, false);
        activity=getActivity();
		return view;

	}

	@Override
	public void onResume() {
		super.onResume();

        //if(!Validator.isNativeStart()) {
//BLog.e("SET","brief frag");
            //ActionBarManager.setActionBarBackOnlyWithLogo(getActivity(), R.drawable.icon_settings, getActivity().getResources().getString(R.string.action_settings), R.menu.settings, R.color.actionbar_general);

        CheckBox briefphonecheck = (CheckBox) view.findViewById(R.id.settings_brief_phone_check);
            if (Device.hasPhone()) {

                briefphonecheck.setOnClickListener(onBriefPhoneCheckboxClicked);
                briefphonecheck.setVisibility(View.VISIBLE);
                if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_PHONE))
                    briefphonecheck.setChecked(true);
                else
                    briefphonecheck.setChecked(false);

            }
            CheckBox briefsmscheck = (CheckBox) view.findViewById(R.id.settings_brief_sms_check);
            //View smsview = (View) view.findViewById(R.id.settings_brief_sms_check);
            if (Device.hasPhone()) {


                briefsmscheck.setOnClickListener(onBriefSmsCheckboxClicked);
                briefsmscheck.setVisibility(View.VISIBLE);
                if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_SMS))
                    briefsmscheck.setChecked(true);
                else
                    briefsmscheck.setChecked(false);

            } else {

                briefsmscheck.setVisibility(View.GONE);
            }

            CheckBox notescheck = (CheckBox) view.findViewById(R.id.settings_brief_notes_check);
            notescheck.setOnClickListener(onBriefNotesCheckboxClicked);

            if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_NOTES))
                notescheck.setChecked(true);
            else
                notescheck.setChecked(false);

            CheckBox newscheck = (CheckBox) view.findViewById(R.id.settings_brief_news_check);
            newscheck.setOnClickListener(onBriefNewsCheckboxClicked);
            if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_NEWS))
                newscheck.setChecked(true);
            else
                newscheck.setChecked(false);

            CheckBox newscheckhead = (CheckBox) view.findViewById(R.id.settings_brief_news_check_style);
            newscheckhead.setOnClickListener(onBriefNewsStyleCheckboxClicked);
            if (State.getSettings().getInt(BriefSettings.INT_BRIEF_SHOW_NEWS_STYLE) > 0)
                newscheckhead.setChecked(true);
            else
                newscheckhead.setChecked(false);

/*
            CheckBox chatcheck = (CheckBox) view.findViewById(R.id.settings_brief_chat_check);
            chatcheck.setOnClickListener(onBriefChatCheckboxClicked);
            if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_CHAT))
                chatcheck.setChecked(true);
            else
                chatcheck.setChecked(false);
*/
            CheckBox emailcheck = (CheckBox) view.findViewById(R.id.settings_brief_email_check);
            emailcheck.setOnClickListener(onBriefEmailCheckboxClicked);
            if (State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_EMAIL))
                emailcheck.setChecked(true);
            else
                emailcheck.setChecked(false);

        /*
		CheckBox twittercheck=(CheckBox) view.findViewById(R.id.settings_brief_twitter_check);

        twittercheck.setVisibility(View.GONE);
		twittercheck.setOnClickListener(onBriefTwitterCheckboxClicked);
		if(State.getSettings().getBoolean(BriefSettings.BOOL_BRIEF_SHOW_TWITTER))
			twittercheck.setChecked(true);
		else
			twittercheck.setChecked(false);
		*/
            stylePods = (RadioButton) view.findViewById(R.id.settings_style_pods);
            styleColors = (RadioButton) view.findViewById(R.id.settings_style_colors);
            stylePlain = (RadioButton) view.findViewById(R.id.settings_style_plain);
            int style = State.getSettings().getInt(BriefSettings.INT_STYLE_LIST);
            switch (style) {
                case BriefSettings.STYLE_LIST_COLOR:
                    styleColors.setChecked(true);
                    break;
                case BriefSettings.STYLE_LIST_PLAIN:
                    stylePlain.setChecked(true);
                    break;
                default:
                    stylePods.setChecked(true);
                    break;
            }
            stylePods.setOnClickListener(onRadioClicked);
            styleColors.setOnClickListener(onRadioClicked);
            stylePlain.setOnClickListener(onRadioClicked);

            TextView fontFace = (TextView) view.findViewById(R.id.settings_style_font);
            fontFace.setOnClickListener(onFontFaceClicked);
            TextView fontSize = (TextView) view.findViewById(R.id.settings_style_font_size);
            fontSize.setOnClickListener(onFontSizeClicked);

            //String ff=;

            String fface = State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE);
            if (fface.isEmpty()) {
                fface = BriefSettings.FONT_FACE_DEFAULT;
            }
            fontFace.setText(getString(R.string.settings_font_typeface) + ": " + fface);

            String fsize = State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_SIZE);
            if (fsize.isEmpty())
                fsize = BriefSettings.FONT_SIZE_MEDIUM;
            fontSize.setText(getString(R.string.settings_font_size) + ": " + fsize);


            ImageView themeBg = (ImageView) view.findViewById(R.id.settings_theme_bg);
            themeBg.setImageBitmap(B.getThemeBackgroundThumbnail(activity, State.getSettings().getString(BriefSettings.STRING_THEME)));
            themeBg.setOnClickListener(onThemeClicked);

            TextView themeText = (TextView) view.findViewById(R.id.settings_theme_text);
            themeText.setText(State.getSettings().getString(BriefSettings.STRING_THEME));
            themeText.setOnClickListener(onThemeClicked);


            TextView textHeadShow = (TextView) view.findViewById(R.id.settings_brief_head_show);
            TextView textHeadTheme = (TextView) view.findViewById(R.id.settings_brief_head_theme);

            B.addStyle(new TextView[]{newscheckhead, briefphonecheck,fontFace, fontSize, notescheck, newscheck, briefsmscheck, emailcheck, styleColors, stylePods, styleColors, stylePlain});
            B.addStyleBold(textHeadShow, B.FONT_LARGE);
            B.addStyleBold(textHeadTheme, B.FONT_LARGE);
            B.addStyleBold(themeText, B.FONT_XLARGE);

        //}

	}
    public OnClickListener onThemeClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            dialog = new ThemeDialog(activity);

            dialog.show();

        }
    };
    public OnClickListener onFontFaceClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            PopupMenu popupMenu = new PopupMenu(activity, view);
            //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());
            popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity, BriefSettings.FONT_FACE_DEFAULT,BriefSettings.FONT_FACE_DEFAULT,1F));
            popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_FACE_COMIC,BriefSettings.FONT_FACE_COMIC,1F));
            popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_FACE_CAVIAR,BriefSettings.FONT_FACE_CAVIAR,1F));

            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    BriefSettings settings = State.getSettings();
                    settings.setString(BriefSettings.STRING_STYLE_FONT_FACE,item.getTitle().toString());
                    settings.save();
                    B.initTypeface(activity, item.getTitle().toString());
                    //onResume();
                    onResume();
                    return true;
                }
            });

            popupMenu.show();

        }
    };
    public OnClickListener onFontSizeClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            PopupMenu popupMenu = new PopupMenu(activity, view);
            //popupMenu.getMenuInflater().inflate(R.menu.contacts_clipboard, popupMenu.getMenu());
            String csize= State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_SIZE);
            String fface = State.getSettings().getString(BriefSettings.STRING_STYLE_FONT_FACE);
            if(csize.equals(BriefSettings.FONT_SIZE_XLARGE)) {
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity, BriefSettings.FONT_SIZE_SMALL,fface,0.4F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_MEDIUM,fface,0.6F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_LARGE,fface,0.8F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_XLARGE,fface,1.0F));
            } else if(csize.equals(BriefSettings.FONT_SIZE_LARGE)) {
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity, BriefSettings.FONT_SIZE_SMALL,fface,0.6F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_MEDIUM,fface,0.8F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_LARGE,fface,1F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_XLARGE,fface,1.2F));
            } else if(csize.equals(BriefSettings.FONT_SIZE_MEDIUM)) {
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity, BriefSettings.FONT_SIZE_SMALL,fface,0.8F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_MEDIUM,fface,1F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_LARGE,fface,1.2F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_XLARGE,fface,1.4F));
            } else {
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity, BriefSettings.FONT_SIZE_SMALL,fface,1F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_MEDIUM,fface,1.2F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_LARGE,fface,1.4F));
                popupMenu.getMenu().add(B.getStyledWithTypeFaceName(activity,BriefSettings.FONT_SIZE_XLARGE,fface,1.6F));
            }



            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    BriefSettings settings = State.getSettings();
                    //BLog.e("PREF", item.getTitle().toString());
                    settings.setString(BriefSettings.STRING_STYLE_FONT_SIZE,item.getTitle().toString());
                    settings.save();
                    B.initTypeface(activity, item.getTitle().toString());
                    B.resetDefaultTextSize();
                    BriefActivityManager.closeAndRestartBrief(activity);

                    return true;
                }
            });

            popupMenu.show();

        }
    };

    public OnClickListener onBriefSmsCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_SMS,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_SMS,Boolean.FALSE);
                //BriefManager.clearSmsBriefs();
		    }
            BriefManager.setDirty(BriefManager.IS_DIRTY_SMS);
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onRadioClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((RadioButton) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    // Check which radio button was clicked
		    switch(view.getId()) {
		        case R.id.settings_style_plain:
		            if (checked) {
		                settings.setInt(BriefSettings.INT_STYLE_LIST, BriefSettings.STYLE_LIST_PLAIN);
		                styleColors.setChecked(false);
		                stylePods.setChecked(false);
		            }
		            break;
		        case R.id.settings_style_colors:
		            if (checked) {
		            	settings.setInt(BriefSettings.INT_STYLE_LIST, BriefSettings.STYLE_LIST_COLOR);
		            	stylePods.setChecked(false);
		            	stylePlain.setChecked(false);
		            }
		            break;
		        default:
		        	if(checked) {
		        		settings.setInt(BriefSettings.INT_STYLE_LIST, BriefSettings.STYLE_LIST_PODS);
		        		stylePlain.setChecked(false);
		        		styleColors.setChecked(false);
		        	}
		        	break;
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
	public OnClickListener onBriefPhoneCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_PHONE,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_PHONE,Boolean.FALSE);
                //BriefManager.clearPhoneBriefs();
		    }
		    settings.save();
		    State.setSettings(settings);
            BriefManager.setDirty(BriefManager.IS_DIRTY_PHONE);
		}
	};	
	public OnClickListener onBriefNotesCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NOTES,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NOTES,Boolean.FALSE);
                //BriefManager.clearNoteBriefs();
		    }
		    settings.save();
		    State.setSettings(settings);
            BriefManager.setDirty(BriefManager.IS_DIRTY_NOTES);
		}
	};	
	public OnClickListener onBriefNewsCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NEWS,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_NEWS,Boolean.FALSE);
                //BriefManager.clearNewsBriefs();
		    }
		    settings.save();
		    State.setSettings(settings);
            BriefManager.setDirty(BriefManager.IS_DIRTY_NEWS);
			
		}
	};
    public OnClickListener onBriefNewsStyleCheckboxClicked = new OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean checked = ((CheckBox) view).isChecked();
            BriefSettings settings = State.getSettings();
            if(checked) {
                settings.setInt(BriefSettings.INT_BRIEF_SHOW_NEWS_STYLE,1);
            } else {
                settings.setInt(BriefSettings.INT_BRIEF_SHOW_NEWS_STYLE,0);
                //BriefManager.clearNewsBriefs();
            }
            settings.save();
            State.setSettings(settings);
            BriefManager.setDirty(BriefManager.IS_DIRTY_NEWS);

        }
    };
    /*
	public OnClickListener onBriefTwitterCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_TWITTER,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_TWITTER,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};
    */
	public OnClickListener onBriefEmailCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_EMAIL,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_EMAIL,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
            BriefManager.setDirty(BriefManager.IS_DIRTY_EMAIL);
		}
	};
    /*
	public OnClickListener onBriefChatCheckboxClicked = new OnClickListener() {
		@Override
		public void onClick(View view) {
		    boolean checked = ((CheckBox) view).isChecked();
		    BriefSettings settings = State.getSettings();
		    if(checked) {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_CHAT,Boolean.TRUE);
		    } else {
		    	settings.setBoolean(BriefSettings.BOOL_BRIEF_SHOW_CHAT,Boolean.FALSE);
		    }
		    settings.save();
		    State.setSettings(settings);
			
		}
	};	
*/
}
