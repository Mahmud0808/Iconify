package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;

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
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class Miscellaneous extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_miscellaneous);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_miscellaneous));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Qs Panel Blur
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_blur = findViewById(R.id.enable_blur);
        Prefs.putBoolean("qsBlurSwitch", SystemUtil.supportsBlur());
        enable_blur.setChecked(Prefs.getBoolean("qsBlurSwitch", false));
        enable_blur.setOnCheckedChangeListener((buttonView, isChecked) -> {
            Prefs.putBoolean("qsBlurSwitch", isChecked);
            if (isChecked)
                SystemUtil.enableBlur();
            else {
                SystemUtil.disableBlur();
                FabricatedOverlayUtil.disableOverlay("qsBlurRadius");
            }
        });

        SeekBar blur_seekbar = findViewById(R.id.blur_seekbar);
        blur_seekbar.setPadding(0, 0, 0, 0);
        TextView blur_output = findViewById(R.id.blur_output);
        final int[] blur_radius = {Prefs.getInt("qsBlurRadius", 23)};
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
                Prefs.putInt("qsBlurRadius", blur_radius[0]);
                FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "qsBlurRadius", "dimen", "max_window_blur_radius", blur_radius[0] + "px");
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