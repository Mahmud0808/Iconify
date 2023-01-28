package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.HIDE_STATUS_ICONS_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class XposedOthers extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_others);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_xposed_others));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Force enable blur
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch force_enable_blur = findViewById(R.id.force_enable_blur);
        Prefs.putBoolean("qsForceBlurSwitch", SystemUtil.supportsForcedBlur());
        force_enable_blur.setChecked(Prefs.getBoolean("qsForceBlurSwitch", false));
        force_enable_blur.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean("qsForceBlurSwitch", isChecked);
            if (isChecked)
                SystemUtil.forceEnableBlur();
            else {
                SystemUtil.disableForcedBlur();
                FabricatedOverlayUtil.disableOverlay("qsBlurRadius");
            }
        });

        // Hide status icons
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_status_icons = findViewById(R.id.hide_status_icons);
        hide_status_icons.setChecked(RemotePrefs.getBoolean(HIDE_STATUS_ICONS_SWITCH, false));
        hide_status_icons.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RemotePrefs.putBoolean(HIDE_STATUS_ICONS_SWITCH, isChecked);
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });
    }
}