package com.drdisagree.iconify;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

public class Iconify extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static Context getAppContext() {
        return Iconify.context;
    }

    public void onCreate() {
        super.onCreate();
        Iconify.context = getApplicationContext();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}