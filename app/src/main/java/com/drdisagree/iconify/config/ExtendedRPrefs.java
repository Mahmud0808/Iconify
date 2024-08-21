package com.drdisagree.iconify.config;

import android.content.Context;

import com.crossbowffs.remotepreferences.RemotePreferences;
import com.drdisagree.iconify.ui.preferences.SliderPreference;

import java.util.List;

public class ExtendedRPrefs extends RemotePreferences {

    public ExtendedRPrefs(Context context, String authority, String prefFileName) {
        super(context, authority, prefFileName);
    }

    public ExtendedRPrefs(Context context, String authority, String prefFileName, boolean strictMode) {
        super(context, authority, prefFileName, strictMode);
    }

    public int getSliderInt(String key, int defaultVal) {
        return SliderPreference.getSingleIntValue(this, key, defaultVal);
    }

    public float getSliderFloat(String key, float defaultVal) {
        return SliderPreference.getSingleFloatValue(this, key, defaultVal);
    }

    public List<Float> getSliderValues(String key, float defaultValue) {
        return SliderPreference.getValues(this, key, defaultValue);
    }
}
