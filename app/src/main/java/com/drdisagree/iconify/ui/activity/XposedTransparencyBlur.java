package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.FABRICATED_QSPANEL_BLUR_RADIUS;
import static com.drdisagree.iconify.common.References.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.References.QSPANEL_BLUR_SWITCH;
import static com.drdisagree.iconify.common.References.QSTRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.References.SYSTEMUI_PACKAGE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class XposedTransparencyBlur extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_transparency_blur);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_transparency_blur));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Qs Panel Transparency
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_qs_transparency = findViewById(R.id.enable_qs_transparency);
        enable_qs_transparency.setChecked(RPrefs.getBoolean(QSTRANSPARENCY_SWITCH, false));
        enable_qs_transparency.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(QSTRANSPARENCY_SWITCH, isChecked);
            // Restart SystemUI
            new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        SeekBar transparency_seekbar = findViewById(R.id.transparency_seekbar);
        transparency_seekbar.setPadding(0, 0, 0, 0);
        TextView transparency_output = findViewById(R.id.transparency_output);
        final int[] transparency = {RPrefs.getInt(QSALPHA_LEVEL, 60)};
        transparency_output.setText(getResources().getString(R.string.opt_selected) + ' ' + transparency[0] + "%");
        transparency_seekbar.setProgress(transparency[0]);
        transparency_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                transparency[0] = progress;
                transparency_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "%");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(QSALPHA_LEVEL, transparency[0]);
            }
        });

        // Qs Panel Blur
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_blur = findViewById(R.id.enable_blur);
        Prefs.putBoolean(QSPANEL_BLUR_SWITCH, SystemUtil.isBlurEnabled());
        enable_blur.setChecked(Prefs.getBoolean(QSPANEL_BLUR_SWITCH, false));
        enable_blur.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean(QSPANEL_BLUR_SWITCH, isChecked);
            if (isChecked)
                SystemUtil.enableBlur();
            else {
                SystemUtil.disableBlur();
                FabricatedOverlayUtil.disableOverlay(FABRICATED_QSPANEL_BLUR_RADIUS);
            }
        });

        SeekBar blur_seekbar = findViewById(R.id.blur_seekbar);
        blur_seekbar.setPadding(0, 0, 0, 0);
        TextView blur_output = findViewById(R.id.blur_output);
        final int[] blur_radius = {Prefs.getInt(FABRICATED_QSPANEL_BLUR_RADIUS, 23)};
        blur_output.setText(getResources().getString(R.string.opt_selected) + ' ' + blur_radius[0] + "px");
        blur_seekbar.setProgress(blur_radius[0]);
        blur_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                blur_radius[0] = progress;
                blur_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "px");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putInt(FABRICATED_QSPANEL_BLUR_RADIUS, blur_radius[0]);
                FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QSPANEL_BLUR_RADIUS, "dimen", "max_window_blur_radius", blur_radius[0] + "px");
                // Restart SystemUI
                new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}