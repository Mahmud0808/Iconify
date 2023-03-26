package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_ICON_SIZE;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_MOVE_ICON;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_TEXT_SIZE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.Objects;

public class QsIconLabel extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qs_icon_label);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_qs_icon_label));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Text Size
        SeekBar text_size = findViewById(R.id.text_size);

        TextView text_size_output = findViewById(R.id.text_size_output);

        final int[] finalTextSize = {14};

        if (!Prefs.getString(FABRICATED_QS_TEXT_SIZE).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) == 14)
                text_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) + "sp " + getResources().getString(R.string.opt_default));
            else
                text_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE)) + "sp");
            finalTextSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_TEXT_SIZE));
            text_size.setProgress(finalTextSize[0]);
        } else
            text_size_output.setText(getResources().getString(R.string.opt_selected) + " 14sp " + getResources().getString(R.string.opt_default));

        text_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalTextSize[0] = progress;
                if (progress == 14)
                    text_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "sp " + getResources().getString(R.string.opt_default));
                else
                    text_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "sp");

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putString(FABRICATED_QS_TEXT_SIZE, String.valueOf(finalTextSize[0]));

                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_TEXT_SIZE, "dimen", "qs_tile_text_size", finalTextSize[0] + "sp");

                Toast.makeText(Iconify.getAppContext(), finalTextSize[0] + "sp " + getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Icon Size
        SeekBar icon_size = findViewById(R.id.icon_size);

        TextView icon_size_output = findViewById(R.id.icon_size_output);

        final int[] finalIconSize = {20};

        if (!Prefs.getString(FABRICATED_QS_ICON_SIZE).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) == 20)
                icon_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) + "dp " + getResources().getString(R.string.opt_default));
            else
                icon_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE)) + "dp");
            finalIconSize[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_ICON_SIZE));
            icon_size.setProgress(finalIconSize[0]);
        } else
            icon_size_output.setText(getResources().getString(R.string.opt_selected) + " 20dp " + getResources().getString(R.string.opt_default));

        icon_size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalIconSize[0] = progress;
                if (progress == 20)
                    icon_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp " + getResources().getString(R.string.opt_default));
                else
                    icon_size_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putString(FABRICATED_QS_ICON_SIZE, String.valueOf(finalIconSize[0]));

                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_ICON_SIZE, "dimen", "qs_icon_size", finalIconSize[0] + "dp");

                Toast.makeText(Iconify.getAppContext(), finalIconSize[0] + "dp " + getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Hide text size if hide label is enabled
        LinearLayout text_size_container = findViewById(R.id.text_size_container);
        View text_size_divider = findViewById(R.id.text_size_divider);

        if (Prefs.getBoolean("IconifyComponentQSHL.overlay")) {
            text_size_container.setVisibility(View.GONE);
            text_size_divider.setVisibility(View.GONE);
        }

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_white = findViewById(R.id.label_white);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_whiteV2 = findViewById(R.id.label_whiteV2);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverse = findViewById(R.id.label_systemInverse);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_systemInverseV2 = findViewById(R.id.label_systemInverseV2);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch label_fixtextcolor = findViewById(R.id.label_fixtextcolor);

        label_white.setChecked(Prefs.getBoolean("IconifyComponentQST1.overlay"));

        label_white.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    label_whiteV2.setChecked(false);
                    label_systemInverse.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    label_fixtextcolor.setChecked(false);
                    OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                    OverlayUtil.enableOverlay("IconifyComponentQST1.overlay");
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                }
            }, 200);
        });

        label_whiteV2.setChecked(Prefs.getBoolean("IconifyComponentQST2.overlay"));

        label_whiteV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_systemInverse.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    label_fixtextcolor.setChecked(false);
                    OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                    OverlayUtil.enableOverlay("IconifyComponentQST2.overlay");
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                }
            }, 200);
        });

        label_systemInverse.setChecked(Prefs.getBoolean("IconifyComponentQST3.overlay"));

        label_systemInverse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_whiteV2.setChecked(false);
                    label_systemInverseV2.setChecked(false);
                    label_fixtextcolor.setChecked(false);
                    OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                    OverlayUtil.enableOverlay("IconifyComponentQST3.overlay");
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                }
            }, 200);
        });

        label_systemInverseV2.setChecked(Prefs.getBoolean("IconifyComponentQST4.overlay"));

        label_systemInverseV2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_whiteV2.setChecked(false);
                    label_systemInverse.setChecked(false);
                    label_fixtextcolor.setChecked(false);
                    OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");

                    OverlayUtil.enableOverlay("IconifyComponentQST4.overlay");
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");
                }
            }, 200);
        });

        label_fixtextcolor.setChecked(Prefs.getBoolean("IconifyComponentQST5.overlay"));

        label_fixtextcolor.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    label_white.setChecked(false);
                    label_whiteV2.setChecked(false);
                    label_systemInverse.setChecked(false);
                    label_systemInverseV2.setChecked(false);

                    OverlayUtil.disableOverlay("IconifyComponentQST1.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST2.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST3.overlay");
                    OverlayUtil.disableOverlay("IconifyComponentQST4.overlay");

                    OverlayUtil.enableOverlay("IconifyComponentQST5.overlay");
                } else {
                    OverlayUtil.disableOverlay("IconifyComponentQST5.overlay");
                }
            }, 200);
        });

        // Hide Label

        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch hide_label = findViewById(R.id.hide_label);

        hide_label.setChecked(Prefs.getBoolean("IconifyComponentQSHL.overlay"));

        hide_label.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                OverlayUtil.enableOverlay("IconifyComponentQSHL.overlay");

                text_size_container.setVisibility(View.GONE);
                text_size_divider.setVisibility(View.GONE);
            } else {
                OverlayUtil.disableOverlay("IconifyComponentQSHL.overlay");

                text_size_container.setVisibility(View.VISIBLE);
                text_size_divider.setVisibility(View.VISIBLE);
            }
        });

        // Move Icon
        SeekBar move_icon = findViewById(R.id.move_icon);
        TextView move_icon_output = findViewById(R.id.move_icon_output);
        final int[] finalMoveIcon = {16};

        if (!Prefs.getString(FABRICATED_QS_MOVE_ICON).equals(STR_NULL)) {
            if (Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) == 16)
                move_icon_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) + "dp " + getResources().getString(R.string.opt_default));
            else
                move_icon_output.setText(getResources().getString(R.string.opt_selected) + ' ' + Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON)) + "dp");
            finalMoveIcon[0] = Integer.parseInt(Prefs.getString(FABRICATED_QS_MOVE_ICON));
            move_icon.setProgress(finalMoveIcon[0]);
        } else
            move_icon_output.setText(getResources().getString(R.string.opt_selected) + " 16dp " + getResources().getString(R.string.opt_default));

        move_icon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                finalMoveIcon[0] = progress;
                if (progress == 16)
                    move_icon_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp " + getResources().getString(R.string.opt_default));
                else
                    move_icon_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Prefs.putString(FABRICATED_QS_MOVE_ICON, String.valueOf(finalMoveIcon[0]));

                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_QS_MOVE_ICON, "dimen", "qs_tile_start_padding", finalMoveIcon[0] + "dp");

                Toast.makeText(Iconify.getAppContext(), finalMoveIcon[0] + "dp " + getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}