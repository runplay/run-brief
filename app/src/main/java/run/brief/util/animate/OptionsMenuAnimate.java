package run.brief.util.animate;

import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

public class OptionsMenuAnimate extends Animation {
    int endX;
    float startX;
    View slideView;
    //ImageView imageView;
    boolean close;
    public OptionsMenuAnimate(View _v, int _endX) {
    	
        slideView = _v;
        startX = slideView.getX();
        //this.imageView = imageView;
        endX = _endX;
    }
/*
    public ContactsOptions(View _v, boolean _close, int _maxWidth, ImageView imageView) {
        this.slideView = _v;
        this.imageView = imageView;
        targetWidth = _maxWidth;
        close = _close;
    }
*/
    protected void applyTransformation(float interpolatedTime, Transformation t) {
        int newX;
        if(startX>endX) {
        	newX = (int) (startX - (endX * interpolatedTime));
        } else {
        	newX = (int) (startX + (endX * interpolatedTime));
        }

        slideView.setVisibility(View.VISIBLE);
        slideView.setX(newX);
        //slideView.getLayoutParams().width = newWidth;
        //slideView.requestLayout();
        //imageView.setImageResource(slideView.getWidth() > 0 ? R.drawable.purple_arrow_right : R.drawable.purple_arrow_left);
    }

    public void initalize(int width, int height, int parentWidth, int parentHeight) {
        super.initialize(width, height, parentWidth, parentHeight);
    }

    public boolean willChangeBounds() {
        return false;
    }

}
