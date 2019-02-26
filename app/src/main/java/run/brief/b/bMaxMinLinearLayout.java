package run.brief.b;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class bMaxMinLinearLayout extends LinearLayout {
    private int maxHeight;
    private int minHeight;
    private int maxWidth;
    private int minWidth;
    

    public bMaxMinLinearLayout(Context context) {
        super(context);
        maxHeight = 0;
    }

    public bMaxMinLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        //TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MaxWidthLinearLayout);
        //mMaxWidth = a.getDimensionPixelSize(R.styleable.MaxWidthLinearLayout_maxWidth, Integer.MAX_VALUE);
    }
    public void setMinMaxHeight(int minHeightInDp, int maxHeightInDp) {
    	maxHeight=maxHeightInDp;
    	minHeight=minHeightInDp;
    }
    

    public void setMinMaxWidth(int minWidthInDp,int maxWidthInDp) {
    	maxWidth=maxWidthInDp;
    	minWidth=minWidthInDp;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        //BLog.e("MS", "** measuredspec w/h: "+widthMeasureSpec+","+heightMeasureSpec+" ----- measuredHeight: "+measuredHeight +" - measuredWidth: "+measuredWidth);
        
        if (maxHeight > 0 && maxHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, measureMode);
        } else if (minHeight > 0 && minHeight > measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(minHeight, measureMode);
        }
        
        if (maxWidth > 0 && maxWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, measureMode);
        } else if (minWidth > 0 && minWidth > measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(minWidth, measureMode);
        }
        //BLog.e("MS", "measured w/h: "+widthMeasureSpec+","+heightMeasureSpec+" ----- max w/h: "+maxWidth + ","+maxHeight+" - min w/h: "+minWidth+","+minHeight);
        
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
