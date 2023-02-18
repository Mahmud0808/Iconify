package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.References.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.References.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.References.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.References.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.References.STR_NULL;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.util.List;
import java.util.Objects;

public class ApplyOnBoot {

    private static final String INVALID = STR_NULL;
    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    public static List<String> FabricatedEnabledOverlays = FabricatedOverlayUtil.getEnabledOverlayList();

    public static void applyColors() {
        Runnable runnable = () -> {
            if (Prefs.getBoolean("customColor")) {
                Prefs.putBoolean(CUSTOM_PRIMARY_COLOR_SWITCH, true);
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
            }

            String colorAccentPrimary = Prefs.getString(COLOR_ACCENT_PRIMARY);
            String colorAccentSecondary = Prefs.getString(COLOR_ACCENT_SECONDARY);
            if ((Prefs.getBoolean(CUSTOM_PRIMARY_COLOR_SWITCH) || Prefs.getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH)) && (FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_PRIMARY) || FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_SECONDARY))) {
                boolean amc_reApplied = false;
                if (Prefs.getBoolean(CUSTOM_PRIMARY_COLOR_SWITCH) && FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_PRIMARY)) {
                    if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                        amc_reApplied = true;
                    }

                    if (!Objects.equals(colorAccentPrimary, INVALID)) {
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(colorAccentPrimary), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
                    } else {
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY);
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ICONIFY_COLOR_PIXEL_DARK_BG);
                    }
                }
                if (Prefs.getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH) && FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_SECONDARY)) {
                    if (!amc_reApplied && OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                    }

                    if (!Objects.equals(colorAccentSecondary, INVALID))
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                    else
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY);
                }
                Prefs.putBoolean("customColor", true);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
