package com.drdisagree.iconify;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

public class Iconify extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public void onCreate() {
        super.onCreate();
        Iconify.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return Iconify.context;
    }
}