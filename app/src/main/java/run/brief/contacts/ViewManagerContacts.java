package run.brief.contacts;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spannable.Factory;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import run.brief.R;
import run.brief.b.State;
import run.brief.beans.BriefSettings;

public final class ViewManagerContacts {
	
	Context context;
	EditText editField;
	//TextView textField
	public void manageEditText(Context context, EditText editTextView) {
		
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_EMOTICONS).booleanValue()) {
			this.context=context;
			this.editField=editTextView;
			applyEmoticonsToEditText();
		}
	}
	public void manageTextView(Context context, TextView textField) {
		
		if(State.getSettings().getBoolean(BriefSettings.BOOL_USE_EMOTICONS).booleanValue()) {
			//textField.set
			Spannable str = new SpannableString(textField.getText());
			addSmiles(context,str);
			textField.setText(str);
			textField.refreshDrawableState();
		}
	}
	
	
	
	
	
	//  Emoticons edittext functions
	private static final Factory spannableFactory = Spannable.Factory.getInstance();
	private static final Map<Pattern, Integer> emoticons = new HashMap<Pattern, Integer>();

	static {
		
	    addPattern(emoticons, ":ps", R.drawable.social_person);


	}
	
	private static void addPattern(Map<Pattern, Integer> map, String smile,
	        int resource) {
	    map.put(Pattern.compile(Pattern.quote(smile)), resource);
	}

	private static boolean addSmiles(Context context, Spannable spannable) {
	    boolean hasChanges = false;
	    for (Entry<Pattern, Integer> entry : emoticons.entrySet()) {
	        Matcher matcher = entry.getKey().matcher(spannable);
	        while (matcher.find()) {
	            boolean set = true;
	            for (ImageSpan span : spannable.getSpans(matcher.start(),
	                    matcher.end(), ImageSpan.class))
	                if (spannable.getSpanStart(span) >= matcher.start()
	                        && spannable.getSpanEnd(span) <= matcher.end())
	                    spannable.removeSpan(span);
	                else {
	                    set = false;
	                    break;
	                }
	            if (set) {
	                hasChanges = true;
	                spannable.setSpan(new ImageSpan(context, entry.getValue()),
	                        matcher.start(), matcher.end(),
	                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
	            }
	        }
	    }
	    return hasChanges;
	}

	private static Spannable getSmiledText(Context context, CharSequence text) {
	    Spannable spannable = spannableFactory.newSpannable(text);
	    addSmiles(context, spannable);
	    return spannable;
	}
	
	
	
	private void applyEmoticonsToEditText() {
		editField.addTextChangedListener(new TextWatcher() {
			
			private int lastLength=0;
			
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            	int len=s.length();
            	if(len!=lastLength) {
	            	if(len>lastLength+1) {
	            		lastLength=len;
	            		editField.setText(getSmiledText(context, s));
	            		editField.setSelection(lastLength);
	            	}
            	
            		
            	}
            }
        });
	}
}
