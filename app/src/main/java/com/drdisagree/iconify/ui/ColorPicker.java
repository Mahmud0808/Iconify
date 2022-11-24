package com.drdisagree.iconify.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.services.ApplyOnBoot;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.List;
import java.util.Objects;

public class ColorPicker extends AppCompatActivity implements ColorPickerDialogListener {

    private static List<String> overlays = OverlayUtils.getEnabledOverlayList();

    public static String ColorToHex(int color, boolean opacity, boolean hash) {
        int alpha = android.graphics.Color.alpha(color);
        int blue = android.graphics.Color.blue(color);
        int green = android.graphics.Color.green(color);
        int red = android.graphics.Color.red(color);

        String alphaHex = To00Hex(alpha);
        String blueHex = To00Hex(blue);
        String greenHex = To00Hex(green);
        String redHex = To00Hex(red);

        StringBuilder str;

        if (hash)
            str = new StringBuilder("#");
        else
            str = new StringBuilder("");

        if (opacity)
            str.append(alphaHex);
        str.append(redHex);
        str.append(greenHex);
        str.append(blueHex);
        return str.toString();
    }

    public static String ColorToSpecialHex(int color) {
        int alpha = android.graphics.Color.alpha(color);
        int blue = android.graphics.Color.blue(color);
        int green = android.graphics.Color.green(color);
        int red = android.graphics.Color.red(color);

        String alphaHex = To00Hex(alpha);
        String blueHex = To00Hex(blue);
        String greenHex = To00Hex(green);
        String redHex = To00Hex(red);

        StringBuilder str = new StringBuilder("0xff");
//      str.append(alphaHex);
        str.append(redHex);
        str.append(greenHex);
        str.append(blueHex);
        return str.toString();
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length() - 2, hex.length());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.color_picker);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle("Color Picker");
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Enable default colors
        LinearLayout enable_default_colors = findViewById(R.id.enable_default_colors);
        enable_default_colors.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customPrimaryColor", true);
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customSecondaryColor", true);
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", "null");
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", "null");

                FabricatedOverlay.buildOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");
                FabricatedOverlay.buildOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", "0xFF122530");
                FabricatedOverlay.buildOverlay("android", "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
                FabricatedOverlay.enableOverlay("colorAccentPrimary");
                FabricatedOverlay.enableOverlay("colorAccentPrimaryDark");
                FabricatedOverlay.enableOverlay("colorAccentSecondary");

                ApplyOnBoot.applyColors();
                Toast.makeText(Iconify.getAppContext(), "Default Colors Applied", Toast.LENGTH_SHORT).show();
                findViewById(R.id.page_color_picker).invalidate();
                return true;
            }
        });

        // Primary and Secondary color
        ColorPickerDialog.Builder colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        ColorPickerDialog.Builder colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.holo_blue_light)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.holo_green_light)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false);

        LinearLayout preview_coloraccentprimary = findViewById(R.id.preview_coloraccentprimary);
        preview_coloraccentprimary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerDialogPrimary.show(ColorPicker.this);
            }
        });

        LinearLayout preview_coloraccentsecondary = findViewById(R.id.preview_coloraccentsecondary);
        preview_coloraccentsecondary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                colorPickerDialogSecondary.show(ColorPicker.this);
            }
        });

        // Disable custom colors button
        Button disable_custom_color = findViewById(R.id.disable_custom_color);
        if (OverlayUtils.isOverlayEnabled(overlays, "IconifyComponentAMC.overlay") && (PrefConfig.loadPrefBool(Iconify.getAppContext(), "customPrimaryColor") || PrefConfig.loadPrefBool(Iconify.getAppContext(), "customSecondaryColor")))
            disable_custom_color.setVisibility(View.VISIBLE);
        else
            disable_custom_color.setVisibility(View.GONE);

        disable_custom_color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customPrimaryColor", false);
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customSecondaryColor", false);
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", "null");
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", "null");
                FabricatedOverlay.disableOverlay("colorAccentPrimary");
                FabricatedOverlay.disableOverlay("colorAccentSecondary");
                disable_custom_color.setVisibility(View.GONE);
                findViewById(R.id.page_color_picker).invalidate();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        String accent = String.valueOf(color);
        switch (dialogId) {
            case 1:
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customPrimaryColor", true);
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", accent);
                Runnable runnable1 = new Runnable() {
                    @Override
                    public void run() {
                        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentAMC.overlay")) {
                            OverlayUtils.disableOverlay("IconifyComponentAMC.overlay");
                            OverlayUtils.enableOverlay("IconifyComponentAMC.overlay");
                        }
                        FabricatedOverlay.buildOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(accent)));
                        FabricatedOverlay.buildOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(accent), Color.BLACK, 0.8f)));
                        FabricatedOverlay.enableOverlay("colorAccentPrimary");
                        FabricatedOverlay.enableOverlay("colorAccentPrimaryDark");
                        String colorAccentSecondary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary");
                        if (!Objects.equals(colorAccentSecondary, "null")) {
                            FabricatedOverlay.buildOverlay("android", "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                            FabricatedOverlay.enableOverlay("colorAccentSecondary");
                        }
                        findViewById(R.id.page_color_picker).invalidate();
                    }
                };
                Thread thread1 = new Thread(runnable1);
                thread1.start();
                break;
            case 2:
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customSecondaryColor", true);
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", accent);
                Runnable runnable2 = new Runnable() {
                    @Override
                    public void run() {
                        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentAMC.overlay")) {
                            OverlayUtils.disableOverlay("IconifyComponentAMC.overlay");
                            OverlayUtils.enableOverlay("IconifyComponentAMC.overlay");
                        }
                        FabricatedOverlay.buildOverlay("android", "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(accent)));
                        FabricatedOverlay.enableOverlay("colorAccentSecondary");
                        String colorAccentPrimary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary");
                        if (!Objects.equals(colorAccentPrimary, "null")) {
                            FabricatedOverlay.buildOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                            FabricatedOverlay.buildOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(colorAccentPrimary), Color.BLACK, 0.8f)));
                            FabricatedOverlay.enableOverlay("colorAccentPrimary");
                            FabricatedOverlay.enableOverlay("colorAccentPrimaryDark");
                        }
                        findViewById(R.id.page_color_picker).invalidate();
                    }
                };
                Thread thread2 = new Thread(runnable2);
                thread2.start();
                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        ;
    }
}