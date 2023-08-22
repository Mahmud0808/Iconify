package com.drdisagree.iconify;

import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

import java.lang.ref.WeakReference;

public class Iconify extends Application {

    private static WeakReference<Context> contextReference;

    public static Context getAppContext() {
        if (contextReference != null) {
            return contextReference.get();
        }
        return null;
    }

    public void onCreate() {
        super.onCreate();
        contextReference = new WeakReference<>(getApplicationContext());
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}