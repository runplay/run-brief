package run.brief.util;

import android.graphics.Canvas;
import android.graphics.Point;
import android.view.View;
import android.view.View.DragShadowBuilder;

public class DragShadow extends DragShadowBuilder {
//private Drawable mShadow;
	//private View v;
	float touchx;
	float touchy;
	//View useView;

public DragShadow(View v, float touchx, float touchy) {
    super(v);
	
    this.touchx=touchx;
    this.touchy=touchy;
    //drawable.list_item_main_selected is an image which is currently being 
    //displayed
    //mShadow = v;//.getResources().getDrawable(R.drawable.list_item_shadow);
    //this.v=v;

}
@Override
public void onProvideShadowMetrics(Point size, Point touch){
    int width = getView().getWidth();
    int height = getView().getHeight();
    size.set(width, height);
    touch.set(Float.valueOf(touchx).intValue(),Float.valueOf(touchy).intValue());
}


@Override
public void onDrawShadow(Canvas canvas) {
    //mShadow.draw(canvas);
	getView().draw(canvas);
    //getView().draw(canvas);
}
}