package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_COLOR_SOURCE;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_COLOR_TINT;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_LEFT_PADDING;
import static com.drdisagree.iconify.common.References.FABRICATED_SB_RIGHT_PADDING;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Objects;

public class Statusbar extends BaseActivity implements ColorPickerDialogListener {

    private static String colorSBTint;
    private static String selectedStyle;
    ColorPickerDialog.Builder colorPickerSBTint;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statusbar);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_statusbar);

        // Statusbar left padding
        SeekBar sb_left_padding_seekbar = findViewById(R.id.sb_left_padding_seekbar);
        TextView sb_left_padding_output = findViewById(R.id.sb_left_padding_output);
        final int[] finalSBLeftPadding = {Prefs.getInt(FABRICATED_SB_LEFT_PADDING, 8)};
        sb_left_padding_output.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBLeftPadding[0] + "dp");
        sb_left_padding_seekbar.setProgress(finalSBLeftPadding[0]);

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
                Prefs.putInt(FABRICATED_SB_LEFT_PADDING, finalSBLeftPadding[0]);
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_SB_LEFT_PADDING, "dimen", "status_bar_padding_start", finalSBLeftPadding[0] + "dp");

                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        // Statusbar right padding
        SeekBar sb_right_padding_seekbar = findViewById(R.id.sb_right_padding_seekbar);
        TextView sb_right_padding_output = findViewById(R.id.sb_right_padding_output);
        final int[] finalSBRightPadding = {Prefs.getInt(FABRICATED_SB_RIGHT_PADDING, 8)};
        sb_right_padding_output.setText(getResources().getString(R.string.opt_selected) + ' ' + finalSBRightPadding[0] + "dp");
        sb_right_padding_seekbar.setProgress(finalSBRightPadding[0]);

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
                Prefs.putInt(FABRICATED_SB_RIGHT_PADDING, finalSBRightPadding[0]);
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_SB_RIGHT_PADDING, "dimen", "status_bar_padding_end", finalSBRightPadding[0] + "dp");

                Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
            }
        });

        colorSBTint = String.valueOf(getResources().getColor(R.color.colorAccent));

        //set current choosen style
        selectedStyle = Prefs.getString(FABRICATED_SB_COLOR_SOURCE);

        if (Objects.equals(selectedStyle, "Monet") || Prefs.getBoolean("IconifyComponentSBTint.overlay")) {
            ((RadioButton) findViewById(R.id.sb_tint_monet)).setChecked(true);
            Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "Monet");
        } else if (Objects.equals(selectedStyle, "System"))
            ((RadioButton) findViewById(R.id.sb_tint_system)).setChecked(true);
        else if (Objects.equals(selectedStyle, "Custom"))
            ((RadioButton) findViewById(R.id.sb_tint_custom)).setChecked(true);

        // Statusbar color source select
        RadioGroup tint_selector = findViewById(R.id.sb_tint_source_selector);

        tint_selector.setOnCheckedChangeListener((group, checkedId) -> {
            if (Objects.equals(checkedId, R.id.sb_tint_system)) {
                if (!Objects.equals(selectedStyle, "System")) {
                    Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "System");
                    resetSBColor();
                }
            } else if (Objects.equals(checkedId, R.id.sb_tint_monet)) {
                if (!Objects.equals(selectedStyle, "Monet")) {
                    OverlayUtil.enableOverlay("IconifyComponentSBTint.overlay");
                    Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "Monet");
                    new Handler().postDelayed(SystemUtil::restartSystemUI, SWITCH_ANIMATION_DELAY);
                }
            } else if (Objects.equals(checkedId, R.id.sb_tint_custom)) {
                colorPickerSBTint = ColorPickerDialog.newBuilder();
                colorPickerSBTint.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorSBTint)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
                colorPickerSBTint.show(Statusbar.this);
            }
        });
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if (dialogId == 1) {
            colorSBTint = String.valueOf(color);
            Prefs.putString(FABRICATED_SB_COLOR_TINT, colorSBTint);
            applySBColor();
            OverlayUtil.disableOverlay("IconifyComponentSBTint.overlay");
            colorPickerSBTint.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorSBTint)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
            Prefs.putString(FABRICATED_SB_COLOR_SOURCE, "Custom");
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        selectedStyle = Prefs.getString(FABRICATED_SB_COLOR_SOURCE);
        if (Objects.equals(selectedStyle, "System"))
            ((RadioButton) findViewById(R.id.sb_tint_system)).setChecked(true);
        else if (Objects.equals(selectedStyle, "Monet"))
            ((RadioButton) findViewById(R.id.sb_tint_monet)).setChecked(true);
        else if (Objects.equals(selectedStyle, "Custom"))
            ((RadioButton) findViewById(R.id.sb_tint_custom)).setChecked(true);
    }

    private void applySBColor() {
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "colorSBTint1", "color", "dark_mode_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "colorSBTint2", "color", "dark_mode_icon_color_single_tone", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "colorSBTint3", "color", "dark_mode_qs_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "colorSBTint4", "color", "dark_mode_qs_icon_color_single_tone", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "colorSBTint5", "color", "light_mode_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "colorSBTint6", "color", "light_mode_icon_color_single_tone", ColorToSpecialHex(Integer.parseInt(colorSBTint)));
        FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, "colorSBTint7", "color", "status_bar_clock_color", ColorToSpecialHex(Integer.parseInt(colorSBTint)));

        new Handler().postDelayed(SystemUtil::restartSystemUI, 1000);
    }

    private void resetSBColor() {
        FabricatedUtil.disableOverlay("colorSBTint1");
        FabricatedUtil.disableOverlay("colorSBTint2");
        FabricatedUtil.disableOverlay("colorSBTint3");
        FabricatedUtil.disableOverlay("colorSBTint4");
        FabricatedUtil.disableOverlay("colorSBTint5");
        FabricatedUtil.disableOverlay("colorSBTint6");
        FabricatedUtil.disableOverlay("colorSBTint7");
        OverlayUtil.disableOverlay("IconifyComponentSBTint.overlay");

        new Handler().postDelayed(SystemUtil::restartSystemUI, 1000);
    }
}