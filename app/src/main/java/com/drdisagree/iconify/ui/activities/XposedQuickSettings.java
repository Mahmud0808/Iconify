package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Preferences.HIDE_QSLABEL_SWITCH;
import static com.drdisagree.iconify.common.Preferences.PANEL_TOPMARGIN_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QS_TOPMARGIN;
import static com.drdisagree.iconify.common.Preferences.VERTICAL_QSTILE_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.RPrefs;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class XposedQuickSettings extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xposed_quick_settings);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_quick_settings));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Vertical QS Tile
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_vertical_tile = findViewById(R.id.enable_vertical_tile);
        enable_vertical_tile.setChecked(RPrefs.getBoolean(VERTICAL_QSTILE_SWITCH, false));
        enable_vertical_tile.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(VERTICAL_QSTILE_SWITCH, isChecked);
            new Handler().postDelayed(HelperUtil::forceApply, 200);
        });

        // Hide label for vertical tiles
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_tile_label = findViewById(R.id.hide_tile_label);
        hide_tile_label.setChecked(RPrefs.getBoolean(HIDE_QSLABEL_SWITCH, false));
        hide_tile_label.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(HIDE_QSLABEL_SWITCH, isChecked);
            new Handler().postDelayed(HelperUtil::forceApply, 200);
        });

        // QS panel top margin switch
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_panel_top_margin = findViewById(R.id.enable_panel_top_margin);
        enable_panel_top_margin.setChecked(RPrefs.getBoolean(PANEL_TOPMARGIN_SWITCH, false));
        enable_panel_top_margin.setOnCheckedChangeListener((buttonView, isChecked) -> {
            RPrefs.putBoolean(PANEL_TOPMARGIN_SWITCH, isChecked);
            if (isChecked) new Handler().postDelayed(HelperUtil::forceApply, 200);
            else new Handler().postDelayed(SystemUtil::restartSystemUI, 200);
        });

        // QS panel top margin slider
        SeekBar qs_top_margin_seekbar = findViewById(R.id.qs_top_margin_seekbar);
        TextView qs_top_margin_output = findViewById(R.id.qs_top_margin_output);
        qs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + RPrefs.getInt(QS_TOPMARGIN, 0) + "dp");
        qs_top_margin_seekbar.setProgress(RPrefs.getInt(QS_TOPMARGIN, 0));
        final int[] qsTopMargin = {RPrefs.getInt(QS_TOPMARGIN, 0)};
        qs_top_margin_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                qsTopMargin[0] = progress;
                qs_top_margin_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                RPrefs.putInt(QS_TOPMARGIN, qsTopMargin[0]);
                if (RPrefs.getBoolean(PANEL_TOPMARGIN_SWITCH, false)) {
                    new Handler().postDelayed(HelperUtil::forceApply, 200);
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}