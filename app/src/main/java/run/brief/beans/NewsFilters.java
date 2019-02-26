package run.brief.beans;

import run.brief.news.NewsFiltersDb;
import run.brief.util.json.JSONArray;
import run.brief.util.json.JSONObject;

public class NewsFilters extends BJSONBean {

	//public static final String SET_="";
	public static final String JSONARRAY_INCLUDE="include";
    public static final String JSONARRAY_DISCLUDE="disclude";
    public static final String BOOL_INCLUDE_FIRST="incfirst";



	public NewsFilters() {
		bean=new JSONObject();
        this.setJSONArray(JSONARRAY_INCLUDE,new JSONArray());
        this.setJSONArray(JSONARRAY_DISCLUDE,new JSONArray());

        //setString(STRING_JSONARRAY_INCLUDE);
	}
	public NewsFilters(JSONObject obj) {
		this.bean=obj;

	}

	public void save() {
	    //NewsFiltersDb.Update();
        NewsFiltersDb.Save();
	}


}
