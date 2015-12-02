package com.chaoyang805.cloudnote;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;

/**
 * Created by chaoyang805 on 2015/10/24.
 */
public class UrlDrawable extends BitmapDrawable {

    protected Bitmap mBitmap;

    @Override
    public void draw(Canvas canvas) {
        if (mBitmap != null) {
            canvas.drawBitmap(mBitmap,0,0,getPaint());
        }
    }

}
