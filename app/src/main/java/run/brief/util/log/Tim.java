package run.brief.util.log;

import run.brief.util.Cal;

/**
 * Created by coops on 13/12/15.
 */
public class Tim {
    private static long startedat;

    public static void start() {
        startedat= Cal.getUnixTime();

        BLog.e("Tim started..........");
    }
    public static void printTime(String extra) {
        long dt=(Cal.getUnixTime()-startedat);
        BLog.e("----------------------------------------------    Tim: "+((Cal.getUnixTime()-startedat))+ "mil -- "+extra+" -- "+(dt/1000D)+" secs");
    }

}
