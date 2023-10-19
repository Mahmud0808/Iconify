package com.drdisagree.iconify;

import android.app.Application;
import android.content.Context;

import com.drdisagree.iconify.utils.helper.LocaleHelper;
import com.google.android.material.color.DynamicColors;

import java.lang.ref.WeakReference;

public class Iconify extends Application {

    private static Iconify instance;
    private static WeakReference<Context> contextReference;

    public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(LocaleHelper.setLocale(getApplicationContext()));
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    public static Context getAppContext() {
        if (contextReference == null || contextReference.get() == null) {
            contextReference = new WeakReference<>(LocaleHelper.setLocale(Iconify.getInstance().getApplicationContext()));
        }

        return contextReference.get();
    }

    private static Iconify getInstance() {
        return instance;
    }
}