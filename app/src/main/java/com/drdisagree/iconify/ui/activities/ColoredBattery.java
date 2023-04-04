package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SWITCH_ANIMATION_DELAY;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLORED_BATTERY_CHECK;
import static com.drdisagree.iconify.common.Preferences.COLORED_BATTERY_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.FABRICATED_BATTERY_COLOR_BG;
import static com.drdisagree.iconify.common.References.FABRICATED_BATTERY_COLOR_FG;
import static com.drdisagree.iconify.common.References.FABRICATED_COLORED_BATTERY;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.List;
import java.util.Objects;

public class ColoredBattery extends AppCompatActivity implements ColorPickerDialogListener {

    private static String colorBackground, colorFilled;
    private final List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    ColorPickerDialog.Builder colorPickerDialogBackground, colorPickerDialogFilled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colored_battery);

        // Header
        ViewBindingHelpers.setHeader(this, findViewById(R.id.collapsing_toolbar), findViewById(R.id.toolbar), R.string.activity_title_colored_battery);

        // Enable colored battery
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_colored_battery = findViewById(R.id.enable_colored_battery);
        enable_colored_battery.setChecked(Prefs.getString(COLORED_BATTERY_CHECK, STR_NULL).equals(STR_NULL) ? (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI2.overlay") || OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI4.overlay")) : Prefs.getBoolean(COLORED_BATTERY_SWITCH));
        enable_colored_battery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    Prefs.putString(COLORED_BATTERY_CHECK, "On");
                    FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, FABRICATED_COLORED_BATTERY, "bool", "config_batterymeterDualTone", "1");
                } else {
                    Prefs.putString(COLORED_BATTERY_CHECK, "Off");
                    FabricatedUtil.disableOverlay(FABRICATED_COLORED_BATTERY);
                    FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, FABRICATED_COLORED_BATTERY, "bool", "config_batterymeterDualTone", "0");

                    if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_BG), STR_NULL))
                        FabricatedUtil.disableOverlay(FABRICATED_BATTERY_COLOR_BG);

                    if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_FG), STR_NULL))
                        FabricatedUtil.disableOverlay(FABRICATED_BATTERY_COLOR_FG);
                }

                if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI2.overlay"))
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_lorn_colored_battery), Toast.LENGTH_SHORT).show();
                else if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI4.overlay"))
                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_plumpy_colored_battery), Toast.LENGTH_SHORT).show();

                Prefs.putBoolean(COLORED_BATTERY_SWITCH, isChecked);
            }, SWITCH_ANIMATION_DELAY);
        });

        if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_BG), STR_NULL))
            colorBackground = Prefs.getString(FABRICATED_BATTERY_COLOR_BG);
        else colorBackground = String.valueOf(Color.parseColor("#FFF0F0F0"));

        if (!Objects.equals(Prefs.getString(FABRICATED_BATTERY_COLOR_FG), STR_NULL))
            colorFilled = Prefs.getString(FABRICATED_BATTERY_COLOR_FG);
        else colorFilled = String.valueOf(Color.parseColor("#FFF0F0F0"));

        // Battery background and filled color
        colorPickerDialogBackground = ColorPickerDialog.newBuilder();
        colorPickerDialogFilled = ColorPickerDialog.newBuilder();

        colorPickerDialogBackground.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorBackground)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogFilled.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorFilled)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout battery_background_color = findViewById(R.id.battery_background_color);
        battery_background_color.setOnClickListener(v -> colorPickerDialogBackground.show(ColoredBattery.this));

        LinearLayout battery_filled_color = findViewById(R.id.battery_filled_color);
        battery_filled_color.setOnClickListener(v -> colorPickerDialogFilled.show(ColoredBattery.this));

        updateColorPreview();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:

                colorBackground = String.valueOf(color);
                Prefs.putString(FABRICATED_BATTERY_COLOR_BG, colorBackground);
                updateColorPreview();
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_BATTERY_COLOR_BG, "color", "light_mode_icon_color_dual_tone_background", ColorToSpecialHex(Integer.parseInt(colorBackground)));
                colorPickerDialogBackground.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorBackground)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);

                break;
            case 2:

                colorFilled = String.valueOf(color);
                Prefs.putString(FABRICATED_BATTERY_COLOR_FG, colorFilled);
                updateColorPreview();
                FabricatedUtil.buildAndEnableOverlay(SYSTEMUI_PACKAGE, FABRICATED_BATTERY_COLOR_FG, "color", "light_mode_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorFilled)));
                colorPickerDialogFilled.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorFilled)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void updateColorPreview() {
        View preview_color_picker_background = findViewById(R.id.preview_color_picker_background);
        View preview_color_picker_fill = findViewById(R.id.preview_color_picker_fill);
        GradientDrawable gd;

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(colorBackground), Integer.parseInt(colorBackground)});
        gd.setCornerRadius(getResources().getDimension(com.intuit.sdp.R.dimen._24sdp) * getResources().getDisplayMetrics().density);
        preview_color_picker_background.setBackgroundDrawable(gd);

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(colorFilled), Integer.parseInt(colorFilled)});
        gd.setCornerRadius(getResources().getDimension(com.intuit.sdp.R.dimen._24sdp) * getResources().getDisplayMetrics().density);
        preview_color_picker_fill.setBackgroundDrawable(gd);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}