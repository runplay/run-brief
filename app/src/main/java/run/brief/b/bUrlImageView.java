package run.brief.b;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class bUrlImageView extends ImageView
{

    public bUrlImageView(Context context)    {
        super(context);
        setBackgroundColor(0xFFFFFF);
    }

    public bUrlImageView(Context context, AttributeSet attrs)    {
        super(context, attrs);
    }

    public bUrlImageView(Context context, AttributeSet attrs, int defStyle)    {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(Canvas canvas)    {
        super.onDraw(canvas);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event)    {
        return super.onTouchEvent(event);
    }
}