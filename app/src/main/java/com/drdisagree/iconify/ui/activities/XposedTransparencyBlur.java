package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.NOTIF_TRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QSALPHA_LEVEL;
import static com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QS_TRANSPARENCY_SWITCH;
import static com.drdisagree.iconify.common.References.FABRICATED_QSPANEL_BLUR_RADIUS;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.SystemUtil;

public class XposedTransparencyBlur extends BaseActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_transparency_blur);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_transparency_blur);

        // Qs Panel & Notification Shade Transparency
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_qs_transparency = findViewById(R.id.enable_qs_transparency);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_notif_transparency = findViewById(R.id.enable_notif_transparency);

        enable_qs_transparency.setChecked(RPrefs.getBoolean(QS_TRANSPARENCY_SWITCH, false));
        enable_qs_transparency.setOnCheckedChangeListener(qsTransparencyListener);

        enable_notif_transparency.setChecked(RPrefs.getBoolean(NOTIF_TRANSPARENCY_SWITCH, false));
        enable_notif_transparency.setOnCheckedChangeListener(notifTransparencyListener);

        // Tansparency Alpha
        SeekBar transparency_seekbar = findViewById(R.id.transparency_seekbar);
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
            if (isChecked) SystemUtil.enableBlur();
            else {
                SystemUtil.disableBlur();
                FabricatedUtil.disableOverlay(FABRICATED_QSPANEL_BLUR_RADIUS);
            }
        });

        SeekBar blur_seekbar = findViewById(R.id.blur_seekbar);
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
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QSPANEL_BLUR_RADIUS, "dimen", "max_window_blur_radius", blur_radius[0] + "px");
                // Restart SystemUI
                new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
            }
        });
    }

    CompoundButton.OnCheckedChangeListener qsTransparencyListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            RPrefs.putBoolean(QS_TRANSPARENCY_SWITCH, isChecked);

            if (isChecked) {
                RPrefs.putBoolean(NOTIF_TRANSPARENCY_SWITCH, false);
                ((Switch) findViewById(R.id.enable_notif_transparency)).setOnCheckedChangeListener(null);
                ((Switch) findViewById(R.id.enable_notif_transparency)).setChecked(false);
                ((Switch) findViewById(R.id.enable_notif_transparency)).setOnCheckedChangeListener(notifTransparencyListener);
            }

            // Restart SystemUI
            new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
        }
    };

    CompoundButton.OnCheckedChangeListener notifTransparencyListener = (buttonView, isChecked) -> {
        RPrefs.putBoolean(NOTIF_TRANSPARENCY_SWITCH, isChecked);

        if (isChecked) {
            RPrefs.putBoolean(QS_TRANSPARENCY_SWITCH, false);
            ((Switch) findViewById(R.id.enable_qs_transparency)).setOnCheckedChangeListener(null);
            ((Switch) findViewById(R.id.enable_qs_transparency)).setChecked(false);
            ((Switch) findViewById(R.id.enable_qs_transparency)).setOnCheckedChangeListener(qsTransparencyListener);
        }

        // Restart SystemUI
        new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
    };
}