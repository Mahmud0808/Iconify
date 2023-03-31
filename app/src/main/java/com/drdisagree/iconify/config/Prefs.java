package com.drdisagree.iconify.config;

import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.Resources.SharedPref;

import android.content.Context;
import android.content.SharedPreferences;

import com.drdisagree.iconify.Iconify;

public class Prefs {

    static SharedPreferences pref = Iconify.getAppContext().getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
    static SharedPreferences.Editor editor = pref.edit();

    // Save sharedPref config
    public static void putBoolean(String key, boolean val) {
        editor.putBoolean(key, val).apply();
    }

    public static void putInt(String key, int val) {
        editor.putInt(key, val).apply();
    }

    public static void putLong(String key, long val) {
        editor.putLong(key, val).apply();
    }

    public static void putString(String key, String val) {
        editor.putString(key, val).apply();
    }

    // Load sharedPref config
    public static boolean getBoolean(String key) {
        return pref.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, Boolean defValue) {
        return pref.getBoolean(key, defValue);
    }

    public static int getInt(String key) {
        return pref.getInt(key, 0);
    }

    public static int getInt(String key, int defValue) {
        return pref.getInt(key, defValue);
    }

    public static long getLong(String key) {
        return pref.getLong(key, 0);
    }

    public static long getLong(String key, long defValue) {
        return pref.getLong(key, defValue);
    }

    public static String getString(String key) {
        return pref.getString(key, STR_NULL);
    }

    public static String getString(String key, String defValue) {
        return pref.getString(key, defValue);
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
