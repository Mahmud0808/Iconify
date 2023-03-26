package com.drdisagree.iconify.ui.activities;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.Preferences.USE_LIGHT_ACCENT;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.views.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.List;
import java.util.Objects;

public class BasicColors extends AppCompatActivity implements ColorPickerDialogListener {

    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    private static boolean isSelectedPrimary = false, isSelectedSecondary = false;
    private static String accentPrimary, accentSecondary;
    Button enable_custom_color;
    LoadingDialog loadingDialog;
    ColorPickerDialog.Builder colorPickerDialogPrimary, colorPickerDialogSecondary;

    public static void applyPrimaryColors() {
        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_PRIMARY))));
        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(Prefs.getString(COLOR_ACCENT_PRIMARY)), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
    }

    public static void applySecondaryColors() {
        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(Prefs.getString(COLOR_ACCENT_SECONDARY))));
    }

    public static void disableMonetColors() {
        Prefs.clearPref("customMonetColor");
        Prefs.clearPref(CUSTOM_PRIMARY_COLOR_SWITCH);
        Prefs.clearPref(CUSTOM_SECONDARY_COLOR_SWITCH);
        Prefs.clearPref(COLOR_ACCENT_PRIMARY);
        Prefs.clearPref(COLOR_ACCENT_SECONDARY);

        FabricatedUtil.disableOverlay(COLOR_ACCENT_PRIMARY);
        FabricatedUtil.disableOverlay(COLOR_PIXEL_DARK_BG);
        FabricatedUtil.disableOverlay(COLOR_ACCENT_SECONDARY);

        if (shouldUseDefaultColors()) {
            FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY);
            FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ICONIFY_COLOR_PIXEL_DARK_BG);
            FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY);
        }
    }

    private static boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMACL.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentAMGCL.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_colors);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_basic_colors));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Loading dialog
        loadingDialog = new LoadingDialog(this);

        // Apply monet accent and gradient
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_accent = findViewById(R.id.apply_monet_accent);
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch apply_monet_gradient = findViewById(R.id.apply_monet_gradient);

        apply_monet_accent.setChecked(Prefs.getBoolean("IconifyComponentAMAC.overlay") || Prefs.getBoolean("IconifyComponentAMACL.overlay"));
        apply_monet_gradient.setChecked(Prefs.getBoolean("IconifyComponentAMGC.overlay") || Prefs.getBoolean("IconifyComponentAMGCL.overlay"));

        apply_monet_accent.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    if (Prefs.getBoolean(USE_LIGHT_ACCENT, false)) {
                        OverlayUtil.disableOverlay("IconifyComponentAMAC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMACL.overlay");
                    } else {
                        OverlayUtil.disableOverlay("IconifyComponentAMACL.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMAC.overlay");
                    }

                    if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL)) {
                        BasicColors.applyPrimaryColors();
                    } else {
                        FabricatedUtil.disableOverlay(COLOR_ACCENT_PRIMARY);
                    }

                    if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL)) {
                        BasicColors.applySecondaryColors();
                    } else {
                        FabricatedUtil.disableOverlay(COLOR_ACCENT_SECONDARY);
                    }

                    apply_monet_accent.postDelayed(() -> {
                        findViewById(R.id.activity_basic_colors).invalidate();
                    }, 1000);
                } else {
                    Runnable runnable = () -> {
                        if (!apply_monet_gradient.isChecked() && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay")) {
                            if (Prefs.getString(COLOR_ACCENT_PRIMARY).equals(STR_NULL)) {
                                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY);
                                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ICONIFY_COLOR_PIXEL_DARK_BG);
                            }

                            if (Prefs.getString(COLOR_ACCENT_SECONDARY).equals(STR_NULL)) {
                                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY);
                            }
                        }

                        OverlayUtil.disableOverlay("IconifyComponentAMAC.overlay");
                        OverlayUtil.disableOverlay("IconifyComponentAMACL.overlay");
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();

                    apply_monet_accent.postDelayed(() -> {
                        findViewById(R.id.activity_basic_colors).invalidate();
                    }, 1000);
                }
            }, 200);
        });

        apply_monet_gradient.setOnCheckedChangeListener((buttonView, isChecked) -> {
            new Handler().postDelayed(() -> {
                if (isChecked) {
                    if (Prefs.getBoolean(USE_LIGHT_ACCENT, false)) {
                        OverlayUtil.disableOverlay("IconifyComponentAMGC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMGCL.overlay");
                    } else {
                        OverlayUtil.disableOverlay("IconifyComponentAMGCL.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMGC.overlay");
                    }

                    if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL)) {
                        BasicColors.applyPrimaryColors();
                    } else {
                        FabricatedUtil.disableOverlay(COLOR_ACCENT_PRIMARY);
                    }

                    if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL)) {
                        BasicColors.applySecondaryColors();
                    } else {
                        FabricatedUtil.disableOverlay(COLOR_ACCENT_SECONDARY);
                    }

                    apply_monet_gradient.postDelayed(() -> {
                        findViewById(R.id.activity_basic_colors).invalidate();
                    }, 1000);
                } else {
                    Runnable runnable = () -> {
                        if (!apply_monet_accent.isChecked() && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentME.overlay")) {
                            if (Prefs.getString(COLOR_ACCENT_PRIMARY).equals(STR_NULL)) {
                                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY);
                                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ICONIFY_COLOR_PIXEL_DARK_BG);
                            }

                            if (Prefs.getString(COLOR_ACCENT_SECONDARY).equals(STR_NULL)) {
                                FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY);
                            }
                        }

                        OverlayUtil.disableOverlay("IconifyComponentAMGC.overlay");
                        OverlayUtil.disableOverlay("IconifyComponentAMGCL.overlay");
                    };
                    Thread thread = new Thread(runnable);
                    thread.start();

                    apply_monet_gradient.postDelayed(() -> {
                        findViewById(R.id.activity_basic_colors).invalidate();
                    }, 1000);
                }
            }, 200);
        });

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_PRIMARY), STR_NULL))
            accentPrimary = Prefs.getString(COLOR_ACCENT_PRIMARY);
        else
            accentPrimary = String.valueOf(getResources().getColor(android.R.color.holo_blue_light));

        if (!Objects.equals(Prefs.getString(COLOR_ACCENT_SECONDARY), STR_NULL))
            accentSecondary = Prefs.getString(COLOR_ACCENT_SECONDARY);
        else
            accentSecondary = String.valueOf(getResources().getColor(android.R.color.holo_green_light));

        updatePrimaryColor();
        updateSecondaryColor();

        // Primary and Secondary color
        colorPickerDialogPrimary = ColorPickerDialog.newBuilder();
        colorPickerDialogSecondary = ColorPickerDialog.newBuilder();

        colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogSecondary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentSecondary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout preview_coloraccentprimary = findViewById(R.id.preview_coloraccentprimary);
        preview_coloraccentprimary.setOnClickListener(v -> colorPickerDialogPrimary.show(BasicColors.this));

        LinearLayout preview_coloraccentsecondary = findViewById(R.id.preview_coloraccentsecondary);
        preview_coloraccentsecondary.setOnClickListener(v -> colorPickerDialogSecondary.show(BasicColors.this));

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
                Prefs.putBoolean(CUSTOM_PRIMARY_COLOR_SWITCH, true);
                colorPickerDialogPrimary.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(accentPrimary)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
                break;
            case 2:
                isSelectedSecondary = true;
                accentSecondary = String.valueOf(color);
                updateSecondaryColor();
                enable_custom_color.setVisibility(View.VISIBLE);
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
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

        if (isSelectedPrimary) Prefs.putString(COLOR_ACCENT_PRIMARY, accentPrimary);

        if (isSelectedSecondary) Prefs.putString(COLOR_ACCENT_SECONDARY, accentSecondary);

        if (isSelectedPrimary) applyPrimaryColors();

        if (isSelectedSecondary) applySecondaryColors();

    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }
}