package run.brief.util.camera;

/**
 * Created by coops on 01/08/15.
 */

import android.graphics.Bitmap;

/**
 * This class describes and contains the entry point to an application that
 * demonstrates the blending transition.
 */

public class Blender
{
    /**
     * Construct Blender1 GUI.
     */

    public Blender()
    {

    }

    /**
     * Blend the contents of two BufferedImages according to a specified
     * weight.
     *
     * @param bi1 first BufferedImage
     * @param bi2 second BufferedImage
     * @param weight the fractional percentage of the first image to keep
     *
     * @return new BufferedImage containing blended contents of BufferedImage
     * arguments
     */

    public Bitmap blend (Bitmap bi1, Bitmap bi2,
                                double weight)
    {
        if (bi1 == null)
            throw new NullPointerException ("bi1 is null");

        if (bi2 == null)
            throw new NullPointerException ("bi2 is null");

        int width = bi1.getWidth ();
        if (width != bi2.getWidth ())
            throw new IllegalArgumentException ("widths not equal");

        int height = bi1.getHeight ();
        if (height != bi2.getHeight ())
            throw new IllegalArgumentException ("heights not equal");


        Bitmap bi3 = Bitmap.createBitmap(width, height,	Bitmap.Config.ARGB_8888);//new Bitmap (width, height,  BufferedImage.TYPE_INT_RGB);
        int [] rgbim1 = new int [width];
        int [] rgbim2 = new int [width];
        int [] rgbim3 = new int [width];

        for (int row = 0; row < height; row++)
        {

            //bi1.getP.getRGB (0, row, width, 1, rgbim1, 0, width);
            //bi2.getRGB (0, row, width, 1, rgbim2, 0, width);

            for (int col = 0; col < width; col++)
            {
                int rgb1 = bi1.getPixel(row,col);   //rgbim1 [col];
                int r1 = (rgb1 >> 16) & 255;
                int g1 = (rgb1 >> 8) & 255;
                int b1 = rgb1 & 255;

                int rgb2 = bi2.getPixel(row,col);//rgbim2 [col];
                int r2 = (rgb2 >> 16) & 255;
                int g2 = (rgb2 >> 8) & 255;
                int b2 = rgb2 & 255;

                int r3 = (int) (r1*weight+r2*(1.0-weight));
                int g3 = (int) (g1*weight+g2*(1.0-weight));
                int b3 = (int) (b1*weight+b2*(1.0-weight));
                rgbim3 [col] = (r3 << 16) | (g3 << 8) | b3;

                bi3.setPixel(row,col,rgbim3 [col]);
                //bi3.setRGB (0, row, width, 1, rgbim3, 0, width);
            }


        }

        return bi3;
    }

}

