package com.chaoyang805.cloudnote.app;

import android.app.Application;

import com.chaoyang805.cloudnote.db.DBImpl;

/**
 * Created by chaoyang805 on 2015/10/19.
 */
public class App extends Application {

    private static final String TAG = "app";
    private DBImpl mDB;
    @Override
    public void onCreate() {
        super.onCreate();
        mDB = new DBImpl(this);
    }

    public DBImpl getDB() {
        return mDB;
    }
}
