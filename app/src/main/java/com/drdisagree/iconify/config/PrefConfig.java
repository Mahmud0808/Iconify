package com.drdisagree.iconify.config;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefConfig {

    private static final String SharedPref = "com.drdisagree.iconify";

    // Save sharedPref config
    public static void savePrefBool(Context context, String key, boolean val) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, val);
        editor.apply();
    }

    public static void savePrefInt(Context context, String key, int val) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(key, val);
        editor.apply();
    }

    public static void savePrefSettings(Context context, String key, String val) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, val);
        editor.apply();
    }

    // Load sharedPref config
    public static boolean loadPrefBool(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        return pref.getBoolean(key, false);
    }

    public static int loadPrefInt(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        return pref.getInt(key, 0);
    }

    public static String loadPrefSettings(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        return pref.getString(key, "null");
    }

    // Clear sharedPref config
    public static void clearPref(Context context, String key) {
        SharedPreferences pref = context.getSharedPreferences(SharedPref, Context.MODE_PRIVATE);
        pref.edit().remove(key).commit();
    }
}
