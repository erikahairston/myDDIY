// Copyright 2004-present Facebook. All Rights Reserved.

package com.fbu.ddiy;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

public class PictureUtils {
    //checks to see how big screen is, & scales img down to that size
    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay()
                .getSize(size);

        return getScaledBitmap(path, size.x, size.y);
    }

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        //Read in the dimension of the image on disk
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float srcWidth = options.outWidth;
        float srcHeight = options.outHeight;

        //inSampleSize determines how big each "sample" should be for each pixel
        //(a sample size of 1 has 1 final horiz pixl for each horiz pxl on orig. file
        //a sample size of 2 has 1 horiz pixl for every 2 horiz pxls in orig file (aka img. has 1/4 # of pixl of orig)
        //Figure out how much to scale down by
        int inSampleSize = 10;
        if (srcHeight > destHeight || srcWidth > destWidth) {
            if (srcWidth > srcHeight) {
                inSampleSize = Math.round(srcHeight / destHeight);
            } else {
                inSampleSize = Math.round(srcWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        //Read in & create final bitmap
        return BitmapFactory.decodeFile(path, options);
    }
}
