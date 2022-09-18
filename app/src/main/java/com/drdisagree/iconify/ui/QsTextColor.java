package com.drdisagree.iconify.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.List;
import java.util.Objects;

public class QsTextColor extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qs_text_color);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Icon and Label");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        List<String> enabledOverlays = OverlayUtils.getEnabledOverlayList();

        // Hide Label

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_label = findViewById(R.id.hide_label);
        LinearLayout text_size_container = findViewById(R.id.text_size_container);
        View text_size_divider = findViewById(R.id.text_size_divider);

        hide_label.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQSHL.overlay"));

        if (PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQSHL.overlay")) {
            text_size_container.setVisibility(View.GONE);
            text_size_divider.setVisibility(View.GONE);
        }

        hide_label.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    text_size_container.setVisibility(View.GONE);
                    text_size_divider.setVisibility(View.GONE);
                    OverlayUtils.enableOverlay("IconifyComponentQSHL.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQSHL.overlay", true);
                } else {
                    text_size_container.setVisibility(View.VISIBLE);
                    text_size_divider.setVisibility(View.VISIBLE);
                    OverlayUtils.disableOverlay("IconifyComponentQSHL.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQSHL.overlay", false);
                }
            }
        });

        // Text Size

        SeekBar text_size = findViewById(R.id.text_size);
        TextView text_size_output = findViewById(R.id.text_size_output);

        text_size.setPadding(0, 0, 0, 0);
        final int[] finalTextSize = {4};

        if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsTextSize").equals("null")) {
            if ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsTextSize")) + 10) == 14)
                text_size_output.setText("Selected: " + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsTextSize")) + 10) + "sp (Default)");
            else
                text_size_output.setText("Selected: " + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsTextSize")) + 10) + "sp");
            finalTextSize[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsTextSize"));
            text_size.setProgress(finalTextSize[0]);
        }

        text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalTextSize[0] = progress;
                if (progress + 10 == 14)
                    text_size_output.setText("Selected: " + (progress + 10) + "sp (Default)");
                else
                    text_size_output.setText("Selected: " + (progress + 10) + "sp");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qsTextSize", String.valueOf(finalTextSize[0]));
                PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedqsTextSize", true);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        FabricatedOverlay.buildOverlay("systemui", "qsTileTextSize", "dimen", "qs_tile_text_size", "0x" + ((finalTextSize[0] + 10 + 14) * 100));

                        FabricatedOverlay.enableOverlay("qsTileTextSize");

                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();

                Toast.makeText(getApplicationContext(), (finalTextSize[0] + 10 + "sp Applied"), Toast.LENGTH_SHORT).show();
            }
        });

        // Icon Size

        SeekBar icon_size = findViewById(R.id.icon_size);
        TextView icon_size_output = findViewById(R.id.icon_size_output);

        icon_size.setPadding(0, 0, 0, 0);
        final int[] finalIconSize = {10};

        if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsIconSize").equals("null")) {
            if ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsIconSize")) + 10) == 20)
                icon_size_output.setText("Selected: " + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsIconSize")) + 10) + "dp (Default)");
            else
                icon_size_output.setText("Selected: " + (Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsIconSize")) + 10) + "dp");
            finalIconSize[0] = Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsIconSize"));
            icon_size.setProgress(finalIconSize[0]);
        }

        icon_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalIconSize[0] = progress;
                if (progress + 10 == 20)
                    icon_size_output.setText("Selected: " + (progress + 10) + "dp (Default)");
                else
                    icon_size_output.setText("Selected: " + (progress + 10) + "dp");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "qsIconSize", String.valueOf(finalIconSize[0]));
                PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedqsIconSize", true);

                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {

                        FabricatedOverlay.buildOverlay("systemui", "qsTileIconSize", "dimen", "qs_icon_size", "0x" + ((finalIconSize[0] + 10 + 16) * 100));

                        FabricatedOverlay.enableOverlay("qsTileIconSize");

                    }
                };
                Thread thread = new Thread(runnable);
                thread.start();

                Toast.makeText(getApplicationContext(), (finalIconSize[0] + 10 + "dp Applied"), Toast.LENGTH_SHORT).show();
            }
        });

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_white = findViewById(R.id.label_white);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_whiteV2 = findViewById(R.id.label_whiteV2);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverse = findViewById(R.id.label_systemInverse);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverseV2 = findViewById(R.id.label_systemInverseV2);

        label_white.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQST1.overlay"));

        label_white.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    label_whiteV2.setChecked(false);
                    label_systemInverse.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST4.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST4.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", false);
                }
            }
        });

        label_whiteV2.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQST2.overlay"));

        label_whiteV2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_systemInverse.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST4.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST4.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", false);
                }
            }
        });

        label_systemInverse.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQST3.overlay"));

        label_systemInverse.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_whiteV2.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST4.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST4.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", false);
                }
            }
        });

        label_systemInverseV2.setChecked(PrefConfig.loadPrefBool(getApplicationContext(), "IconifyComponentQST4.overlay"));

        label_systemInverseV2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_whiteV2.setChecked(false);
                    label_systemInverse.setChecked(false);
                    OverlayUtils.disableOverlay("IconifyComponentQST1.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST1.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST2.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST2.overlay", false);
                    OverlayUtils.disableOverlay("IconifyComponentQST3.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST3.overlay", false);
                    OverlayUtils.enableOverlay("IconifyComponentQST4.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST4.overlay", true);
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentQST4.overlay");
                    PrefConfig.savePrefBool(getApplicationContext(), "IconifyComponentQST4.overlay", false);
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