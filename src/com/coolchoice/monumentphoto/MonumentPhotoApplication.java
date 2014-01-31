package com.coolchoice.monumentphoto;

import com.coolchoice.monumentphoto.dal.DB;

import android.app.Application;



public class MonumentPhotoApplication extends Application {

    @Override
    public void onCreate() {
    	Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    	ConfigureLog4J.configure();
        super.onCreate();
        DB.setContext(getApplicationContext());
    }

    @Override
    public void onTerminate() {
    	DB.release();
        super.onTerminate();
    }
}

