package com.drdisagree.iconify.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.Objects;

public class ColorPicker extends AppCompatActivity implements ColorPickerDialogListener {

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

        return "0xff" + redHex + greenHex + blueHex;
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length() - 2);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_picker);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_color_picker));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Primary and Secondary color
        ColorPickerDialog.Builder colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        ColorPickerDialog.Builder colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.holo_blue_light)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(ContextCompat.getColor(Iconify.getAppContext(), R.color.holo_green_light)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false);

        LinearLayout preview_coloraccentprimary = findViewById(R.id.preview_coloraccentprimary);
        preview_coloraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(ColorPicker.this));

        LinearLayout preview_coloraccentsecondary = findViewById(R.id.preview_coloraccentsecondary);
        preview_coloraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(ColorPicker.this));

        // Disable custom colors button
        Button disable_custom_color = findViewById(R.id.disable_custom_color);
        if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentAMC.overlay") && PrefConfig.loadPrefBool(Iconify.getAppContext(), "customColor"))
            disable_custom_color.setVisibility(View.VISIBLE);
        else
            disable_custom_color.setVisibility(View.GONE);

        disable_custom_color.setOnClickListener(v -> {
            PrefConfig.savePrefBool(Iconify.getAppContext(), "customColor", false);
            PrefConfig.savePrefBool(Iconify.getAppContext(), "customPrimaryColor", false);
            PrefConfig.savePrefBool(Iconify.getAppContext(), "customSecondaryColor", false);
            PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", "null");
            PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", "null");
            FabricatedOverlay.disableOverlay("colorAccentPrimary");
            FabricatedOverlay.disableOverlay("colorAccentSecondary");
            disable_custom_color.setVisibility(View.GONE);
            disable_custom_color.postDelayed(() -> {
                findViewById(R.id.page_color_picker).invalidate();
            }, 100);
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
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customColor", true);
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customPrimaryColor", true);
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentPrimary", accent);

                Runnable runnable1 = () -> {
                    if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "IconifyComponentAMC.overlay")) {
                        OverlayUtils.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtils.enableOverlay("IconifyComponentAMC.overlay");
                    }

                    FabricatedOverlay.buildOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(accent)));
                    FabricatedOverlay.buildOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(accent), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
                    FabricatedOverlay.enableOverlay("colorAccentPrimary");
                    FabricatedOverlay.enableOverlay("colorAccentPrimaryDark");

                    String colorAccentSecondary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary");
                    if (!Objects.equals(colorAccentSecondary, "null")) {
                        FabricatedOverlay.buildOverlay("android", "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                        FabricatedOverlay.enableOverlay("colorAccentSecondary");
                    }

                    runOnUiThread(() -> {
                        new Handler().postDelayed(() -> {
                            findViewById(R.id.page_color_picker).invalidate();
                        }, 100);
                    });
                };
                Thread thread1 = new Thread(runnable1);
                thread1.start();
                break;
            case 2:
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customColor", true);
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customSecondaryColor", true);
                PrefConfig.savePrefSettings(Iconify.getAppContext(), "colorAccentSecondary", accent);

                Runnable runnable2 = () -> {
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

                    runOnUiThread(() -> {
                        new Handler().postDelayed(() -> {
                            findViewById(R.id.page_color_picker).invalidate();
                        }, 100);
                    });
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