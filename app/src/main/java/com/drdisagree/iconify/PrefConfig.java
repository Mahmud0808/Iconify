package com.drdisagree.iconify;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefConfig {

    private static final String pref_aurora = "com.drdisagree.iconify";
    private static final String pref_key = "pref_key";

    public static void savePrefAurora(Context context, boolean val) {
        SharedPreferences pref = context.getSharedPreferences(pref_aurora, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(pref_key, val);
        editor.apply();
    }

    public static boolean loadPrefAurora(Context context) {
        SharedPreferences pref = context.getSharedPreferences(pref_aurora, Context.MODE_PRIVATE);
        return pref.getBoolean(pref_key, false);
    }
}
