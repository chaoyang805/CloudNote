package com.chaoyang805.cloudnote;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.URLSpan;
import android.view.View;

import java.io.File;

/**
 * Created by chaoyang805 on 2015/11/4.
 */
public class VideoUrlSpan extends URLSpan {

    private Context mContext;

    public VideoUrlSpan(String url) {
        super(url);
    }

    public VideoUrlSpan(Context context,String url){
        this(url);
        mContext = context;
    }

    @Override
    public void onClick(View widget) {
        String url = getURL();
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(url)), "video/mp4");
        mContext.startActivity(intent);
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setColor(0xfff90202);
    }
}
