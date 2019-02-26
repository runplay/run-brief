package run.brief.service;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import run.brief.util.camera.CameraFragment;

/**
 * Created by coops on 09/01/15.
 */
public class BriefNotifyAction extends Activity {
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //BLog.e("OOOOOOOK","ACTIVTY RESULT");
        switch (requestCode) {
            case CameraFragment.ACTION_TAKE_PHOTO_B:

                //BLog.e("YO","TAKE PHOTO: "+State.getCameraLastPhoto());
                break;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //BLog.e("OOOOOOOK","CRATED");
        //this.finish();
    }
    @Override
    public void onResume() {
        super.onResume();
        //BLog.e("OOOOOOOK","RESUMED");
    }
}
