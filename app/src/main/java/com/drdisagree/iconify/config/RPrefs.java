package com.drdisagree.iconify.config;

import static android.content.Context.MODE_PRIVATE;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.Resources.SharedXPref;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.drdisagree.iconify.Iconify;

public class RPrefs {

    @SuppressLint("StaticFieldLeak")
    private static final Context prefContext = Iconify.getAppContext().createDeviceProtectedStorageContext();

    static SharedPreferences prefs = prefContext.getSharedPreferences(SharedXPref, MODE_PRIVATE);
    static SharedPreferences.Editor editor = prefs.edit();

    // Save sharedPref config
    public static void putBoolean(String key, boolean val) {
        editor.putBoolean(key, val).commit();
    }

    public static void putInt(String key, int val) {
        editor.putInt(key, val).commit();
    }

    public static void putFloat(String key, float val) {
        editor.putFloat(key, val).commit();
    }

    public static void putString(String key, String val) {
        editor.putString(key, val).commit();
    }

    // Load sharedPref config
    public static boolean getBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, Boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public static int getInt(String key) {
        return prefs.getInt(key, 0);
    }

    public static int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public static float getFloat(String key) {
        return prefs.getFloat(key, 0);
    }

    public static float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    public static String getString(String key) {
        return prefs.getString(key, STR_NULL);
    }

    public static String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    // Clear specific sharedPref config
    public static void clearPref(String key) {
        editor.remove(key).apply();
    }

    // Clear all sharedPref config
    public static void clearAllPrefs() {
        editor.clear().apply();
    }
}
