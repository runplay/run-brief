package run.brief.service;

import android.content.Context;


/*


CLASS NOT IN USE !!!!!!

OPEN TO DELETE



 */


public class BriefServiceRunner implements Runnable {

    //private Date lastRun = new Date();   // when it was found
    private Thread manager;                       // background search thread
    private final int INTERVAL = 300000*3; // 5 mins *3 = 15 mins
    private boolean stopManager=false;
    private boolean started=false;
    private Context context;
    

    
    public void init(Context context) {                 // always!
    	this.context=context;
        manager = new Thread(this);
        manager.setPriority(Thread.MIN_PRIORITY);  // be a good citizen
        manager.start();
    }
    
    public void destroy() {
        stopManager=true;

        manager.interrupt();
    }

    public void run() {

        while (true) {
            if(stopManager) {
                System.out.println("-MSG: RUNMANAGER STOPPED");
                return;
            }
            if(!started) {
                started=true;
                System.out.println("-MSG: RUNMANAGER STARTED");
            }

            
            //BriefService.refresh(context);


            try {
                manager.sleep(INTERVAL);
            } catch (InterruptedException ignored) {
                System.out.println("-WARN: LiveRunningManager.run() - sleep interupted. possible shutdown call");
            }
        }
    }
}
