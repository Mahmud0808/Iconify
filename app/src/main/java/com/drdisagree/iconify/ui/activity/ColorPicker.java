package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.FRAMEWORK_PACKAGE;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.view.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.List;
import java.util.Objects;

public class ColorPicker extends AppCompatActivity implements ColorPickerDialogListener {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false;
    private static String accentPrimary, accentSecondary;
    Button enable_custom_color;
    LoadingDialog loadingDialog;
    ColorPickerDialog.Builder colorPickerDialogPrimary, colorPickerDialogSecondary;

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
            str = new StringBuilder();

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

    public static void applyPrimaryColors() {
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary", "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentPrimary"))));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary1", "color", "system_accent1_100", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentPrimary"))));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary2", "color", "system_accent1_200", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentPrimary"))));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary3", "color", "system_accent1_300", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentPrimary"))));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary4", "color", "system_accent2_100", ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(Prefs.getString("colorAccentPrimary")), Color.WHITE, 0.16f)));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary5", "color", "system_accent2_200", ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(Prefs.getString("colorAccentPrimary")), Color.WHITE, 0.16f)));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary6", "color", "system_accent2_300", ColorToSpecialHex(ColorUtils.blendARGB(Integer.parseInt(Prefs.getString("colorAccentPrimary")), Color.WHITE, 0.16f)));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimaryDark", "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(Prefs.getString("colorAccentPrimary")), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
    }

    public static void applySecondaryColors() {
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentSecondary"))));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary1", "color", "system_accent3_100", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentSecondary"))));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary2", "color", "system_accent3_200", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentSecondary"))));
        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary3", "color", "system_accent3_300", ColorToSpecialHex(Integer.parseInt(Prefs.getString("colorAccentSecondary"))));
    }

    public static void disableMonetColors() {
        Prefs.clearPref("customMonetColor");
        Prefs.clearPref("customPrimaryColor");
        Prefs.clearPref("customSecondaryColor");
        Prefs.clearPref("colorAccentPrimary");
        Prefs.clearPref("colorAccentSecondary");

        FabricatedOverlayUtil.disableOverlay("colorAccentPrimary");
        FabricatedOverlayUtil.disableOverlay("colorAccentPrimary1");
        FabricatedOverlayUtil.disableOverlay("colorAccentPrimary2");
        FabricatedOverlayUtil.disableOverlay("colorAccentPrimary3");
        FabricatedOverlayUtil.disableOverlay("colorAccentPrimary4");
        FabricatedOverlayUtil.disableOverlay("colorAccentPrimary5");
        FabricatedOverlayUtil.disableOverlay("colorAccentPrimary6");
        FabricatedOverlayUtil.disableOverlay("colorAccentPrimaryDark");
        FabricatedOverlayUtil.disableOverlay("colorAccentSecondary");
        FabricatedOverlayUtil.disableOverlay("colorAccentSecondary1");
        FabricatedOverlayUtil.disableOverlay("colorAccentSecondary2");
        FabricatedOverlayUtil.disableOverlay("colorAccentSecondary3");

        if (OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay")) {
            FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");
            FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimaryDark", "color", "holo_blue_dark", "0xFF122530");
            FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
        }
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

        // Loading dialog
        loadingDialog = new LoadingDialog(this);

        if (!Objects.equals(Prefs.getString("colorAccentPrimary"), "null"))
            accentPrimary = Prefs.getString("colorAccentPrimary");
        else if (!Prefs.getBoolean("IconifyComponentAMAC.overlay") && !Prefs.getBoolean("IconifyComponentAMGC.overlay"))
            accentPrimary = String.valueOf(Color.parseColor("#FF50A6D7"));
        else
            accentPrimary = String.valueOf(getResources().getColor(android.R.color.system_accent1_200));

        if (!Objects.equals(Prefs.getString("colorAccentSecondary"), "null"))
            accentSecondary = Prefs.getString("colorAccentSecondary");
        else if (!Prefs.getBoolean("IconifyComponentAMAC.overlay") && !Prefs.getBoolean("IconifyComponentAMGC.overlay"))
            accentSecondary = String.valueOf(Color.parseColor("#FF387BFF"));
        else if (Prefs.getBoolean("IconifyComponentAMAC.overlay"))
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.system_accent1_200));
        else
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.system_accent3_200));

        /* Color table
        LinearLayout color_table = findViewById(R.id.color_table);
        color_table.setOnClickListener(v -> {
            Intent intent = new Intent(ColorPicker.this, ColorTable.class);
            startActivity(intent);
        }); */

        // Primary and Secondary color
        colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout preview_coloraccentprimary = findViewById(R.id.preview_coloraccentprimary);
        preview_coloraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(ColorPicker.this));

        LinearLayout preview_coloraccentsecondary = findViewById(R.id.preview_coloraccentsecondary);
        preview_coloraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(ColorPicker.this));

        // Enable custom colors button
        enable_custom_color = findViewById(R.id.enable_custom_color);

        enable_custom_color.setOnClickListener(v -> {
            enable_custom_color.setVisibility(View.GONE);
            Runnable runnable = () -> {
                applyMonetColors();

                runOnUiThread(() -> {
                    Prefs.putBoolean("customMonetColor", true);

                    new Handler().postDelayed(() -> {
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });

        // Disable custom colors button
        Button disable_custom_color = findViewById(R.id.disable_custom_color);

        if (Prefs.getBoolean("customMonetColor")) disable_custom_color.setVisibility(View.VISIBLE);
        else disable_custom_color.setVisibility(View.GONE);

        disable_custom_color.setOnClickListener(v -> {
            disable_custom_color.setVisibility(View.GONE);
            Runnable runnable = () -> {
                disableMonetColors();

                runOnUiThread(() -> {
                    new Handler().postDelayed(() -> {
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled), Toast.LENGTH_SHORT).show();
                    }, 2000);
                });
            };
            Thread thread = new Thread(runnable);
            thread.start();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:

                isSelectedPrimary = true;
                accentPrimary = String.valueOf(color);
                updatePrimaryColor();
                enable_custom_color.setVisibility(View.VISIBLE);
                Prefs.putBoolean("customPrimaryColor", true);
                colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);

                break;
            case 2:

                isSelectedSecondary = true;
                accentSecondary = String.valueOf(color);
                updateSecondaryColor();
                enable_custom_color.setVisibility(View.VISIBLE);
                Prefs.putBoolean("customSecondaryColor", true);
                colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

                break;
        }
    }

    private void updatePrimaryColor() {
        View preview_color_picker_primary = findViewById(R.id.preview_color_picker_primary);
        GradientDrawable gd;
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentPrimary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        preview_color_picker_primary.setBackgroundDrawable(gd);

        View color_preview_large = findViewById(R.id.color_preview_large);
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        color_preview_large.setBackgroundDrawable(gd);
    }

    private void updateSecondaryColor() {
        View preview_color_picker_secondary = findViewById(R.id.preview_color_picker_secondary);
        GradientDrawable gd;
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentSecondary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        preview_color_picker_secondary.setBackgroundDrawable(gd);

        View color_preview_large = findViewById(R.id.color_preview_large);
        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(accentPrimary), Integer.parseInt(accentSecondary)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        color_preview_large.setBackgroundDrawable(gd);
    }

    private void applyMonetColors() {
        Prefs.putBoolean("customMonetColor", true);

        if (isSelectedPrimary) Prefs.putString("colorAccentPrimary", accentPrimary);

        if (isSelectedSecondary) Prefs.putString("colorAccentSecondary", accentSecondary);

        if (isSelectedPrimary) applyPrimaryColors();

        if (isSelectedSecondary) applySecondaryColors();

    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }
}