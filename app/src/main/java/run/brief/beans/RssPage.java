package run.brief.beans;

import java.util.ArrayList;

/**
 * Created by coops on 02/12/15.
 */
public class RssPage {
    private String url;
    private String title;
    private String extratext;
    private ArrayList<RssItem> items=new ArrayList<RssItem>();
    private String errormessage;

    public RssPage(String url) {
        this.url=url;
    }
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExtratext() {
        return extratext;
    }

    public void setExtratext(String extratext) {
        this.extratext = extratext;
    }

    public ArrayList<RssItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<RssItem> items) {
        this.items = items;
    }

    public String getErrormessage() {
        return errormessage;
    }

    public void setErrormessage(String errormessage) {
        this.errormessage = errormessage;
    }
}
