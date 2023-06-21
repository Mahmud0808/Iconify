package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.SHOW_LSCLOCK_CUSTOMIZATION;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Switch;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;

public class Experimental extends BaseActivity {

    @SuppressLint({"SetTextI18n", "CommitPrefEdits"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimental);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_experimental);

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch lsclock_customization = findViewById(R.id.enable_lsclock_customization);
        lsclock_customization.setChecked(Prefs.getBoolean(SHOW_LSCLOCK_CUSTOMIZATION, false));
        lsclock_customization.setOnCheckedChangeListener((buttonView, isChecked) -> Prefs.putBoolean(SHOW_LSCLOCK_CUSTOMIZATION, isChecked));
    }
}