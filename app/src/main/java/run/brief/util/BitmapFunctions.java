package run.brief.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

import java.io.File;

/**
 * Created by coops on 25/08/15.
 */
public class BitmapFunctions {
    public static Bitmap getBitmap(File image) {
        System.gc();
        //image.length()
        Bitmap bm = BitmapFactory.decodeFile(image.getPath(), null);


        return bm;

    }
    public static Bitmap getBitmap(Context context, File image) {

        Bitmap bm = getThumbnail(context, image.getPath());
        if(bm==null)
            bm = BitmapFactory.decodeFile(image.getPath(), null);


        return bm;

    }
    public static Bitmap resizeTo(Bitmap b, int w, int h) {

        if (b.getWidth() > w) {

            int nh = Double.valueOf(b.getHeight() / (b.getWidth() / h)).intValue();

            if (nh < h-20)
                nh = h-20;
            else if (nh > h+20)
                nh = h+20;
            return Bitmap.createScaledBitmap(b, w, nh, false);
        }
        return Bitmap.createScaledBitmap(b, w, h, false);
    }
    public static Bitmap resizeToFitMax(Bitmap b, int maxw, int maxh) {

        if (b.getWidth() > maxw || b.getHeight() > maxh) {
            double reduceby=1D;
            if(maxh>maxw)
                reduceby=b.getHeight()/ maxh;
            else
                reduceby=b.getWidth()/ maxw;

            if(reduceby>0.01D) {


                int nh = Double.valueOf(b.getHeight() / reduceby).intValue();
                int nw = Double.valueOf(b.getWidth() / reduceby).intValue();
                return Bitmap.createScaledBitmap(b, nw, nh, false);
            }
//BLog.e(reduceby+" == "+nh+" -- "+nw);

        }
        return Bitmap.createScaledBitmap(b, maxw, maxh, false);
        //return Bitmap.createScaledBitmap(b, maxw, maxh, false);
    }
    public static Bitmap getPreview(File image, int toSize) {
        //File image = new File(uri);

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;

        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        if(originalSize>300) {
            opts.inSampleSize = originalSize / toSize;
        }
        Bitmap bm = BitmapFactory.decodeFile(image.getPath(), opts);


        return bm;

    }
    public static Bitmap getPreview(File image) {
        //File image = new File(uri);
        return getPreview(image,300);

    }
    public static Bitmap getThumbnail(Context context, String path) {
        ContentResolver cr= context.getContentResolver();
        Cursor ca = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.MediaColumns._ID }, MediaStore.MediaColumns.DATA + "=?", new String[] {path}, null);
        if (ca != null && ca.moveToFirst()) {
            int id = ca.getInt(ca.getColumnIndex(MediaStore.MediaColumns._ID));
            ca.close();
            return MediaStore.Images.Thumbnails.getThumbnail(cr, id, MediaStore.Images.Thumbnails.MINI_KIND, null );
        }
        if(ca!=null)
            ca.close();
        return null;

    }

}
