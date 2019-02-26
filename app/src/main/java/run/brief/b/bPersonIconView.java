package run.brief.b;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.HashMap;

import run.brief.beans.Person;
import run.brief.beans.PersonFull;

public class bPersonIconView extends ImageView {

	private int size=80;
	Person person=null;
	private final static String defaultBandColor="#000000";
	String bandColor=defaultBandColor;
	//private boolean isChecked;
	private static HashMap<String,Bitmap> storeImages=new HashMap<String,Bitmap>();
	
public bPersonIconView(Context context) {
    super(context);
    // TODO Auto-generated constructor stub
}

public bPersonIconView(Context context, AttributeSet attrs) {
    super(context, attrs);
}

public bPersonIconView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
}
public void setPerson(Person p) {
	person=p;
}
public void clearPerson() {
	person=null;
}
public void clearBandColor() {
	bandColor=defaultBandColor;
}
public void setBandColor(String hexColor) {
	bandColor=hexColor;
}

public void setSize(int size) {
	this.size=size;
}
public void setLayoutParamsInPx(int width, int height) {
	RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width,height);
	
	this.setLayoutParams(layoutParams);
}

@Override
protected void onDraw(Canvas canvas) {

    Drawable drawable = getDrawable();

    if (drawable == null) {
        return;
    }

    if (getWidth() == 0 || getHeight() == 0) {
        return; 
    }
    Bitmap b =  null;
    if(person!=null) {
    	
    	b=person.getThumbnail(getContext());

    }
    //canvas.
    final Paint paintbot = new Paint();
    paintbot.setAlpha(getImageAlpha());
    if(b!=null){
    	//canvas.drawBitmap(b, 0,0, null);
    	//Bitmap bt=;
    	setLayoutParamsInPx(size,size);
    	canvas.drawBitmap(getScaledImage(b,size), 0, 0, paintbot);

    	//this.setSi
    	/*
	    Bitmap baseBitmap =  getBaseCircle(size+20,bandColor);
	    canvas.drawBitmap(baseBitmap, 0,0, null);

	    Bitmap bitmap = b.copy(Bitmap.Config.ARGB_8888, true);
	    Bitmap roundBitmap =  getCroppedBitmap(person,bitmap, size);
	    
	    canvas.drawBitmap(roundBitmap, 10,10, null);
	    
	    canvas.drawARGB(0, 255, 255, 255);
	    */
    } else {
	    Bitmap baseBitmap =  getBaseCircle(size,bandColor);
	    canvas.drawBitmap(baseBitmap, 0,0, paintbot);
    }
}

public static Bitmap getBaseCircle(int radius) {
	
	
	return getBaseCircle(radius,"#000000");
	//canvas.drawBitmap(bmp, rect, rect, paintbot);
}
public static Bitmap getScaledImage(Bitmap image, int size) {
	return Bitmap.createScaledBitmap(image, size, size, false);
}
public static Bitmap getBaseCircle(int radius,String color) {
	String key=radius+color;
	if(storeImages.containsKey(key))
		return storeImages.get(key);
	
	Bitmap output = Bitmap.createBitmap(radius, radius,	Config.ARGB_8888);
	
	Canvas canvas = new Canvas(output);
	final Paint paintbot = new Paint();
	paintbot.setStyle(Paint.Style.FILL);
	paintbot.setAntiAlias(true);
	paintbot.setFilterBitmap(true);
	paintbot.setDither(true);
	//canvas.drawARGB(0, 0, 0, 0);
	paintbot.setColor(Color.parseColor(color));
	//
	
	//canvas.drawColor(Color.parseColor(color));
	int rad=Double.valueOf(radius / 2).intValue();
	canvas.drawCircle(rad,rad,rad, paintbot);
	
	
	storeImages.put(key, output);
	return output;
	//canvas.drawBitmap(bmp, rect, rect, paintbot);
}
public static Bitmap getCroppedBitmap(PersonFull p, Bitmap bmp, int radius) {

	String key="p."+radius+p.getId();
	if(storeImages.containsKey(key))
		return storeImages.get(key);
	
	Bitmap output = Bitmap.createBitmap(radius, radius,	Config.ARGB_8888);
	
	Canvas canvas = new Canvas(output);

	//final int color = 0xffa19774;
	
	
	final Rect rect = new Rect(0, 0, radius, radius);
	
	final Paint paint = new Paint();
	paint.setAntiAlias(true);
	paint.setFilterBitmap(true);
	paint.setDither(true);
	canvas.drawARGB(0, 0, 0, 0);
	paint.setColor(Color.parseColor("#BAB399"));
	canvas.drawCircle(radius / 2,
			radius / 2, radius / 2, paint);
	paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
	
	//canvas.drawBitmap(bmp, 0, 0, paint);
	canvas.drawBitmap(Bitmap.createScaledBitmap(bmp, radius, radius, false), rect, rect, paint);

	storeImages.put(key, output);
	
	return output;
}

}