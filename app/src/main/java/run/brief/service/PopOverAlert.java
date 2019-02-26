package run.brief.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import run.brief.R;

public class PopOverAlert extends Service {
    //HUDView mView;
    private LayoutInflater inflater=null;
    private View view;
    OnScreenLockReceiver receiver;
    OnScreenUnlockReceiver ureceiver;
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        show();
        //mView = new HUDView(this);
    }

	public int onStartCommand(Intent intent, int flags, int startId) {
	    return 1;
	}
	
    @Override
    public void onDestroy() {
        super.onDestroy();
        hide();
    }
    public void show() {
    	
    	IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		receiver = new OnScreenLockReceiver();
		this.registerReceiver(receiver, filter);
    	IntentFilter ufilter = new IntentFilter(Intent.ACTION_USER_PRESENT);
		ureceiver = new OnScreenUnlockReceiver();
		this.registerReceiver(ureceiver, ufilter);
		
        inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view=inflater.inflate(R.layout.brief_pop, null);
        //view.setClickable(true);
        //view.setOnTouchListener(touchBrief);
        //AlphaAnimation alpha = new AlphaAnimation(1F, 0.5F);
        //alpha.setDuration(0); // Make animation instant
        //alpha.setFillAfter(true); // Tell it to persist after the animation ends
       // view.startAnimation(alpha);
        // And then on your layout
        
        //LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        
        
        
        /*
WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
             PixelFormat.TRANSLUCENT);
WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
View myView = inflater.inflate(R.layout.my_view, null);
myView.setOnTouchListener(new OnTouchListener() {
   @Override
   public boolean onTouch(View v, MotionEvent event) {
       Log.d(TAG, "touch me");
       return false;
   }
 });
        */
        
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                0,
//              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//                      | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                PixelFormat.TRANSPARENT);
        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Load Average");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        //wm.addView(mView, params);
        
        view.setOnTouchListener(new OnTouchListener() {
        	   @Override
        	   public boolean onTouch(View v, MotionEvent event) {
        	       Log.d("WIN", "WINDOW SERVICE TOUCH LISTNER");
        	       return false;
        	   }
        	 });
        
        wm.addView(view, params);
        
        AlphaAnimation alpha = new AlphaAnimation(0.5F, 0.0F);
        alpha.setDuration(500); // Make animation instant
        alpha.setFillAfter(true); 
        RelativeLayout layout=(RelativeLayout) view.findViewById(R.id.info_layout);
        layout.setAnimation(alpha);
        layout.animate();
        
    }
    private void captureScreen() {
    	/*
    	 * 
    	 LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		FrameLayout root = (FrameLayout) inflater.inflate(R.layout.activity_main, null); // activity_main is UI(xml) file we used in our Activity class. FrameLayout is root view of my UI(xml) file.
		root.setDrawingCacheEnabled(true);]
		Bitmap bitmap = getBitmapFromView(this.getWindow().findViewById(R.id.frameLayout)); // here give id of our root layout (here its my FrameLayout's id)
    	*/
    	View u = view;
        u.setDrawingCacheEnabled(true);                                                
        ScrollView z = (ScrollView) view.findViewById(R.id.info_scroll);
        int totalHeight = z.getChildAt(0).getHeight();
        int totalWidth = z.getChildAt(0).getWidth();
        u.layout(0, 0, totalWidth, totalHeight);    
        u.buildDrawingCache(true);
        Bitmap b = Bitmap.createBitmap(u.getDrawingCache());             
        u.setDrawingCacheEnabled(false);

        //Save bitmap
        String extr = Environment.getExternalStorageDirectory().toString() +   File.separator + "Folder";
        String fileName = new SimpleDateFormat("yyyyMMddhhmm'_report.jpg'").format(new Date());
        File myPath = new File(extr, fileName);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(myPath);
            b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            MediaStore.Images.Media.insertImage(getContentResolver(), b, "Screen", "screen");
        }catch (FileNotFoundException e) {

            //BLog.add("PopOver", e);
        } catch (Exception e) {

        	//BLog.add("PopOver", e);
        }
    }
    public void hide() {
    	this.unregisterReceiver(receiver);
    	this.unregisterReceiver(ureceiver);
        if(view != null)
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(view);
            view = null;
        }
    }
    public class OnScreenLockReceiver extends BroadcastReceiver {
    	
    	public OnScreenLockReceiver() {}
    	
    	public OnScreenLockReceiver(Context context, Bundle extras, int timeoutInSeconds){

    	}
    	
    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		hide();
    	}
    }
    public class OnScreenUnlockReceiver extends BroadcastReceiver {
    	
    	public OnScreenUnlockReceiver() {}
    	
    	public OnScreenUnlockReceiver(Context context, Bundle extras, int timeoutInSeconds){
    		
    	}
    	
    	
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		show();
    	}
    }

	
}
