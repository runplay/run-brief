package run.brief.news;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by coops on 27/11/15.
 */
public class NewsWebViewManager {
    private static HashMap<String,NewsWebView> webviews = new HashMap<String,NewsWebView>();
    private static ArrayList<String> order = new ArrayList<String>();

    private static void addWebView(String url, NewsWebView view) {
        webviews.put(url, view);
        order.add(url);
        if(order.size()>5) {
            webviews.remove(order.get(0));
            order.remove(0);
        }
    }
    public static void clear() {
        webviews.clear();
        order.clear();
    }
    public static NewsWebView getWebView(Activity activity, String url) {
        NewsWebView getview=webviews.get(url);
        if(getview==null) {
            getview = new NewsWebView(activity);
            addWebView(url,getview);
        }
        return getview;
    }
}
