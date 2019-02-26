package run.brief.news;


import android.text.Editable;
import android.text.TextWatcher;

import java.util.Locale;
import java.util.regex.Pattern;

import run.brief.b.BRefreshable;
import run.brief.util.Sf;
import run.brief.util.json.JSONArray;


/**
 * Created by coops on 19/12/14.
 */
public class FiltersWatcher implements TextWatcher {

    private BRefreshable refreshableFragment;
    private boolean isInclude;
    //private
    public FiltersWatcher(BRefreshable refreshableFragment, boolean isInclude) {
        super();
        this.refreshableFragment=refreshableFragment;
        this.isInclude=isInclude;
    }

    public void afterTextChanged(Editable editText) {
//BLog.e("TXT", "change to Text for to: ");
        String in = editText.toString().toLowerCase(Locale.getDefault());
        synchronized(this) {

            if(in.endsWith(" ")) {
                Pattern pattern = Pattern.compile(in, Pattern.CASE_INSENSITIVE + Pattern.LITERAL);
                int countSpeech= Sf.countOccourences(in, "\"");
                if(countSpeech==0)
                    countSpeech= Sf.countOccourences(in, "'");
                if(countSpeech>0 && countSpeech<2) {

                } else {
                    in.replaceAll("\"","");
                    in.replaceAll("'","");
                    JSONArray use = null;
                    if (isInclude)
                        use = NewsFiltersDb.getIncludeArray();
                    else
                        use = NewsFiltersDb.getDiscludeArray();

                    use.put(use.length(), in.substring(0, in.length() - 1));
                    if (isInclude)
                        NewsFiltersDb.setIncludeArray(use);
                    else
                        NewsFiltersDb.setDiscludeArray(use);
                    editText.clear();
                    refreshableFragment.refreshData();
                }
            }
        }

    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
}