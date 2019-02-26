package run.brief.util;

public class UrlStore {

    private static final String DOMAIN_HOST="ht"+"tp://apps.br"+"ief.ink";
    private static final String DOMAIN_BOOK="ht"+"tp://apps.b"+"rief.ink";

    public static final String DOMAIN_WWW="ht"+"tp://ww"+"w.bri"+"ef.ink";
    private static final String DOMAIN_OPEN="ht"+"tp://static.br"+"ief.ink";


    /*
    private static final String DOMAIN_HOST="ht"+"tp://apps.br"+"ief.ink";
    private static final String DOMAIN_BOOK="ht"+"tp://apps.b"+"rief.ink";

    public static final String DOMAIN_WWW="ht"+"tp://ww"+"w.bri"+"ef.ink";
    private static final String DOMAIN_OPEN="ht"+"tp://static.br"+"ief.ink";




        private static final String DOMAIN_HOST="http://devapp.brief.ink:8091";

        private static final String DOMAIN_BOOK="http://devapp.brief.ink:8091";

        public static final String DOMAIN_WWW="http://www.brief.ink";
        private static final String DOMAIN_OPEN="http://static.brief.ink";

     */



    public static final String URL_FARM_SEND = DOMAIN_HOST+"/app/knock.jsp";


	//public static final String USER_AGENT="Brief v1.0";
	public static final String USER_AGENT_PNP="unix/5.1 UPnP/1.0 Brief/1.0";
	
	public static final String MY_IP = DOMAIN_BOOK+"/ip.jsp";


    public static final String URL_HELP = DOMAIN_WWW+"/help/";
    public static final String URL_LEGAL = DOMAIN_WWW+"/legal/";
    public static final String URL_OPEN_SOURCE = DOMAIN_WWW+"/legal/open/";
    public static final String URL_CHECK_INTERNET = DOMAIN_OPEN+"/app/check_internet.json";
	public static final String URL_RSS_CATGORIES_MASTER = DOMAIN_OPEN+"/app/def_news_cats.json";
	public static final String URL_RSS_FEEDS_MASTER = DOMAIN_OPEN+"/app/def_news_feeds.json";
	
	public static final String ERROR_REPORT = DOMAIN_HOST+"/gen/errors.jsp";

    public static final String USER_AGENT="Mozilla/5.0 Brief (X11; U; Linux x86_64; en-GB; rv:1.9.2.24) Gecko/20111107 Ubuntu/10.04 (lucid) Firefox/3.6.24";
    public static final String USER_AGENT_WEBVIEW="Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; BRIEF-L10L Build/IML01K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30";

    public static final String URL_NEWS_PUBLISHER_IMAGES = DOMAIN_OPEN+"/bread/logos/";


    private class ModeLive extends ModeClass {

    }
    private static class ModeClass {

    }
    private static class ModeDev extends ModeClass {

    }

	//public static final String NEW_APP_REGISTER = DOMAIN+"/gen/register.jsp";
}
