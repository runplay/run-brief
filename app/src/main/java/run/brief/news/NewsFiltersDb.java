package run.brief.news;

import java.util.regex.Pattern;

import run.brief.beans.NewsFilters;
import run.brief.beans.RssItem;
import run.brief.util.FileReadTask;
import run.brief.util.FileWriteTask;
import run.brief.util.Files;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;
import run.brief.util.log.BLog;

public class NewsFiltersDb {

    private static final NewsFiltersDb DB = new NewsFiltersDb();

    private static final String dbArrayName="newsfilter";

    private NewsFilters filters;
    private boolean isLoaded=false;

    //private boolean hasIncludes;
    //private boolean hasDiscludes;

    private static FileWriteTask fwt;
    private static FileReadTask frt;



    public static FileWriteTask getFwt() {
        return fwt;
    }

    public static FileReadTask getFrt() {
        return frt;
    }

    private NewsFiltersDb() {
        //Load();
    }

    public static JSONArray getIncludeArray() {
        JSONArray jinc = DB.filters.getJSONArray(NewsFilters.JSONARRAY_INCLUDE);
        if(jinc!=null)
            return jinc;
        return new JSONArray();
    }
    public static void setIncludeArray(JSONArray includes) {
        DB.filters.setJSONArray(NewsFilters.JSONARRAY_INCLUDE,includes);
    }
    public static void setDiscludeArray(JSONArray discludes) {
        DB.filters.setJSONArray(NewsFilters.JSONARRAY_DISCLUDE,discludes);
    }
    public static JSONArray getDiscludeArray() {
        JSONArray jdis = DB.filters.getJSONArray(NewsFilters.JSONARRAY_DISCLUDE);
        if(jdis!=null)
            return jdis;
        return new JSONArray();
    }
    private static boolean areFiltersEmpty() {
        if(getIncludeArray().length()==0 && getDiscludeArray().length()==0)
            return true;
        return false;
    }
    public static void setFilterStartInclude(boolean includeFirst) {
        DB.filters.setBoolean(NewsFilters.BOOL_INCLUDE_FIRST,includeFirst);
    }
    public static boolean isFiltersStartInclude(){
        return DB.filters.getBoolean(NewsFilters.BOOL_INCLUDE_FIRST);
    }
    public static boolean canShowFeed(RssItem item) {
        //JSONArray jinc = getIncludeArray();
        //JSONArray jdis = getDiscludeArray();
        if(areFiltersEmpty())
            return true;
        boolean isInInclude=isInList(item, getIncludeArray());
        boolean isInDisclude=isInList(item, getDiscludeArray());
        BLog.e("CANS", isInInclude + " - " + isInDisclude + item.getString(RssItem.STRING_HEAD));
        if(DB.filters.getBoolean(NewsFilters.BOOL_INCLUDE_FIRST)) {
            if(isInInclude)
                if(!isInDisclude)
                    return true;

        } else {
            if(!isInDisclude)
                if(isInInclude)
                    return true;
        }
        return false;
    }
    private static boolean isInList(RssItem item, JSONArray list) {
        if(list.length()>0) {
            String sb = new StringBuilder(item.getString(RssItem.STRING_PUBLISHER))
                    .append(" ")
                    .append(item.getString(RssItem.STRING_HEAD))
                    .append(" ")
                    .append(item.getString(RssItem.STRING_TEXT)).toString();

            String inStr = sb.toString();
            for (int i = 0; i < list.length(); i++) {
                String ai = list.getString(i);
                Pattern pattern = Pattern.compile(ai, Pattern.CASE_INSENSITIVE + Pattern.LITERAL);

                if (pattern.matcher(inStr).find())
                    return true;
            }
        }
        return false;

    }

    public static NewsFilters get() {
        return DB.filters;
    }

    public static void Update(NewsFilters filters) {
        if(filters!=null) {
            DB.filters = filters;

        }
    }


    public static boolean Save() {
        //BLog.e("FILT","Save");
        if(DB.isLoaded) {
            try {
                fwt=new FileWriteTask(Files.HOME_PATH_APP, Files.FILENAME_RSS_FILTERS, DB.filters.getBean().toString());

                return fwt.WriteSecureToSd();

            } catch(Exception e) {
                //BLog.e("SAVE", e.getMessage());

            }
        }
        return false;
    }

    public static void init() {

        if(DB.filters==null) {

            frt = new FileReadTask(Files.HOME_PATH_APP, Files.FILENAME_RSS_FILTERS);

            if(frt.ReadSecureFromSd()) {
                //BLog.e("SETTINGS","--"+frt.getFileContent());
                if(frt.getFileContent()!=null && !frt.getFileContent().isEmpty()) {
                    try {
                        JSONObject db = new JSONObject(frt.getFileContent());
                        if(db!=null) {
                            DB.filters = new NewsFilters(db);
                            DB.isLoaded=true;
                        }
                    } catch(Exception e) {
                        //if(e.getMessage()!=null)
                        //BLog.e("SettingsDb.init()",e.getMessage());
                    }
                } else {
                    //BLog.e("SettingsDb.init().empty",frt.getStatusMessage());
                }
            } else {
                //BLog.e("SettingsDb.init().no read",frt.getStatusMessage());
            }
        }
        if(!DB.isLoaded) {
            DB.filters = new NewsFilters();
            DB.isLoaded=true;
            Save();
        }

    }
}
