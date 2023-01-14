package com.drdisagree.iconify;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

public class Iconify extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private static Context prefContext;

    public static Context getAppContext() {
        return Iconify.context;
    }

    public static Context getPrefContext() {
        return Iconify.prefContext;
    }

    public void onCreate() {
        super.onCreate();
        Iconify.context = getApplicationContext();
        Iconify.prefContext = getApplicationContext().createDeviceProtectedStorageContext();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}