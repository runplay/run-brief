package run.brief.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by coops on 03/01/15.
 */
public class UrlImageLoadStore {

    private static final UrlImageLoadStore STORE=new UrlImageLoadStore();
    private Map<String,Boolean> loading = new HashMap<String,Boolean>();

    public UrlImageLoadStore() {

    }

    public static boolean isLoading(String imgUrl) {
        if(STORE.loading.get(imgUrl)!=null)
            return true;
        return false;
    }

    public static synchronized void addToStore(String imgUrl) {
        STORE.loading.put(imgUrl,Boolean.TRUE);
    }
    public static synchronized void removeFromStore(String imgUrl) {
        STORE.loading.remove(imgUrl);
    }

}
