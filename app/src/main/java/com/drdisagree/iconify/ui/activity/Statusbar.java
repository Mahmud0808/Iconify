package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.FABRICATED_SB_COLOR_TINT;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_LEFT_PADDING;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_RIGHT_PADDING;
import static com.drdisagree.iconify.common.References.STR_NULL;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Objects;

public class Statusbar extends AppCompatActivity implements ColorPickerDialogListener {

    private static String colorSBTint;
    ColorPickerDialog.Builder colorPickerSBTint;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statusbar);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_statusbar));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Statusbar left padding
        SeekBar sb_left_padding_seekbar = findViewById(R.id.sb_left_padding_seekbar);
        TextView sb_left_padding_output = findViewById(R.id.sb_left_padding_output);

        sb_left_padding_seekbar.setPadding(0, 0, 0, 0);
        final int[] finalSBLeftPadding = {8};

        if (Prefs.getInt(FABRICATED_SB_LEFT_PADDING, -1) != -1) {
            finalSBLeftPadding[0] = Prefs.getInt(FABRICATED_SB_LEFT_PADDING);
            sb_left_padding_output.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBLeftPadding[0] + "dp");
            sb_left_padding_seekbar.setProgress(finalSBLeftPadding[0]);
        } else
            sb_left_padding_output.setText(getResources().getString(R.string.opt_selected) + " 8dp");

        sb_left_padding_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalSBLeftPadding[0] = progress;
                sb_left_padding_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, FABRICATED_SB_LEFT_PADDING, "dimen", "status_bar_padding_start", finalSBLeftPadding[0] + "dp");

                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Statusbar right padding
        SeekBar sb_right_padding_seekbar = findViewById(R.id.sb_right_padding_seekbar);
        TextView sb_right_padding_output = findViewById(R.id.sb_right_padding_output);

        sb_right_padding_seekbar.setPadding(0, 0, 0, 0);
        final int[] finalSBRightPadding = {8};

        if (Prefs.getInt(FABRICATED_SB_RIGHT_PADDING, -1) != -1) {
            finalSBRightPadding[0] = Prefs.getInt(FABRICATED_SB_RIGHT_PADDING);
            sb_right_padding_output.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBRightPadding[0] + "dp");
            sb_right_padding_seekbar.setProgress(finalSBRightPadding[0]);
        } else
            sb_right_padding_output.setText(getResources().getString(R.string.opt_selected) + " 8dp");

        sb_right_padding_seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                finalSBRightPadding[0] = progress;
                sb_right_padding_output.setText(getResources().getString(R.string.opt_selected) + ' ' + progress + "dp");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, FABRICATED_SB_RIGHT_PADDING, "dimen", "status_bar_padding_end", finalSBRightPadding[0] + "dp");

                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Statusbar color tint
        if (!Objects.equals(Prefs.getString(FABRICATED_SB_COLOR_TINT), STR_NULL))
            colorSBTint = Prefs.getString(FABRICATED_SB_COLOR_TINT);
        else colorSBTint = String.valueOf(getResources().getColor(R.color.colorAccent));

        // Color preview
        colorPickerSBTint = ColorPickerDialog.newBuilder();

        colorPickerSBTint.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorSBTint)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout sb_color_tint = findViewById(R.id.sb_color_tint);
        sb_color_tint.setOnClickListener(v -> colorPickerSBTint.show(Statusbar.this));

        updateSBColorPreview();

        // Reset statusbar color tint
        Button sb_reset_tint = findViewById(R.id.sb_reset_tint);
        sb_reset_tint.setVisibility(!Objects.equals(Prefs.getString(FABRICATED_SB_COLOR_TINT), STR_NULL) ? View.VISIBLE : View.GONE);

        sb_reset_tint.setOnClickListener(v -> {
            Prefs.putString(FABRICATED_SB_COLOR_TINT, STR_NULL);
            resetSBColor();
        });
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 1) {
            colorSBTint = String.valueOf(color);
            Prefs.putString(FABRICATED_SB_COLOR_TINT, colorSBTint);
            updateSBColorPreview();
            applySBColor();
            colorPickerSBTint.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorSBTint)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void applySBColor() {
        FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "colorSBTint1", "color", "dark_mode_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "colorSBTint2", "color", "dark_mode_icon_color_single_tone", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "colorSBTint3", "color", "dark_mode_qs_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "colorSBTint4", "color", "dark_mode_qs_icon_color_single_tone", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "colorSBTint5", "color", "light_mode_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "colorSBTint6", "color", "light_mode_icon_color_single_tone", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "colorSBTint7", "color", "status_bar_clock_color", ColorToSpecialHex(Integer.parseInt(colorSBTint)));

        new Handler().postDelayed(SystemUtil::restartSystemUI, 2000);
    }

    private void resetSBColor() {
        FabricatedOverlayUtil.disableOverlay("colorSBTint1");
        FabricatedOverlayUtil.disableOverlay("colorSBTint2");
        FabricatedOverlayUtil.disableOverlay("colorSBTint3");
        FabricatedOverlayUtil.disableOverlay("colorSBTint4");
        FabricatedOverlayUtil.disableOverlay("colorSBTint5");
        FabricatedOverlayUtil.disableOverlay("colorSBTint6");
        FabricatedOverlayUtil.disableOverlay("colorSBTint7");

        new Handler().postDelayed(SystemUtil::restartSystemUI, 2000);
    }

    private void updateSBColorPreview() {
        View sb_color_preview = findViewById(R.id.sb_color_preview);

        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(colorSBTint), Integer.parseInt(colorSBTint)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        sb_color_preview.setBackgroundDrawable(gd);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}