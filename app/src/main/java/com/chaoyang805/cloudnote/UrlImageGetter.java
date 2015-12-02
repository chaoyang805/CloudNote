package com.chaoyang805.cloudnote;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.chaoyang805.cloudnote.utils.LogHelper;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * Created by chaoyang805 on 2015/10/24.
 */
public class UrlImageGetter implements Html.ImageGetter {

    private static final String TAG = LogHelper.makeLogTag(UrlImageGetter.class);

    private TextView mTextView;

    public UrlImageGetter(Context context, TextView textView) {
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context).build();
        ImageLoader.getInstance().init(config);
        mTextView = textView;
    }

    private int position = 0;
    private int count = 1;

    @Override
    public Drawable getDrawable(String source) {
        final UrlDrawable urlDrawable = new UrlDrawable();
        if (source.startsWith("/storage")) {
            urlDrawable.mBitmap = BitmapFactory.decodeFile(source);
            urlDrawable.setBounds(0, 0, urlDrawable.mBitmap.getWidth(), urlDrawable.mBitmap.getHeight());
            mTextView.invalidate();
            mTextView.setText(mTextView.getText());
        } else if (source.startsWith("http")){
            ImageLoader.getInstance().loadImage(source, new SimpleImageLoadingListener() {

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    urlDrawable.mBitmap = loadedImage;
                    urlDrawable.setBounds(0, 0, loadedImage.getWidth(), loadedImage.getHeight());
                    mTextView.invalidate();
                    mTextView.setText(mTextView.getText());
                }
            });
        }
        return urlDrawable;
    }
}
