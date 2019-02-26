package run.brief.menu;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public class ResizeMoveAnimation extends Animation {
    View view; 
    int fromLeft; 
    int fromTop; 
    int fromRight;
    int fromBottom;
    int toLeft; 
    int toTop; 
    int toRight;
    int toBottom;

    public ResizeMoveAnimation(View v, int toLeft, int toTop, int toRight, int toBottom) {
        this.view = v;
        this.toLeft = toLeft;
        this.toTop = toTop;
        this.toRight = toRight;
        this.toBottom = toBottom;

        fromLeft = v.getLeft();
        fromTop = v.getTop();
        fromRight = v.getRight();
        fromBottom = v.getBottom();

        setDuration(500);
    }

    @Override
    protected void applyTransformation(float interpolatedTime, Transformation t) {

        float left = fromLeft + (toLeft - fromLeft) * interpolatedTime;
        float top = fromTop + (toTop - fromTop) * interpolatedTime;
        float right = fromRight + (toRight - fromRight) * interpolatedTime;
        float bottom = fromBottom + (toBottom - fromBottom) * interpolatedTime;

        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) view.getLayoutParams();
        p.leftMargin = (int) left;
        p.topMargin = (int) top;
        p.width = (int) ((right - left) + 1);
        p.height = (int) ((bottom - top) + 1);

        view.requestLayout();
    }
}