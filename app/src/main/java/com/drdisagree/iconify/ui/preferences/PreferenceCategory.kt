package com.drdisagree.iconify.ui.preferences;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import com.drdisagree.iconify.R;

public class PreferenceCategory extends androidx.preference.PreferenceCategory {

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initResource();
    }

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initResource();
    }

    public PreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initResource();
    }

    public PreferenceCategory(@NonNull Context context) {
        super(context);
        initResource();
    }

    private void initResource() {
        setLayoutResource(R.layout.custom_preference_category);
    }
}
