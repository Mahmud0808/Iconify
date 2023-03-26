package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BOOT_ID;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.Preferences.COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_PRIMARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.CUSTOM_SECONDARY_COLOR_SWITCH;
import static com.drdisagree.iconify.common.Preferences.QS_ROW_COLUMN_SWITCH;
import static com.drdisagree.iconify.common.Preferences.STR_NULL;
import static com.drdisagree.iconify.common.References.DEVICE_BOOT_ID_CMD;
import static com.drdisagree.iconify.common.References.FABRICATED_QS_ROW;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY;
import static com.drdisagree.iconify.common.References.ICONIFY_COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activities.QsRowColumn;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class ApplyOnBoot {

    private static final String INVALID = STR_NULL;
    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    public static List<String> FabricatedEnabledOverlays = FabricatedUtil.getEnabledOverlayList();

    public static void applyColors() {
        Runnable runnable = () -> {
            if (Prefs.getBoolean("customColor")) {
                Prefs.putBoolean(CUSTOM_PRIMARY_COLOR_SWITCH, true);
                Prefs.putBoolean(CUSTOM_SECONDARY_COLOR_SWITCH, true);
            }

            String colorAccentPrimary = Prefs.getString(COLOR_ACCENT_PRIMARY);
            String colorAccentSecondary = Prefs.getString(COLOR_ACCENT_SECONDARY);
            if ((Prefs.getBoolean(CUSTOM_PRIMARY_COLOR_SWITCH) || Prefs.getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH)) && (FabricatedUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_PRIMARY) || FabricatedUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_SECONDARY))) {
                boolean amc_reApplied = false;
                if (Prefs.getBoolean(CUSTOM_PRIMARY_COLOR_SWITCH) && FabricatedUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_PRIMARY)) {
                    if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                        amc_reApplied = true;
                    }

                    if (!Objects.equals(colorAccentPrimary, INVALID)) {
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(colorAccentPrimary), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
                    } else {
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_PRIMARY, "color", "holo_blue_light", ICONIFY_COLOR_ACCENT_PRIMARY);
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ICONIFY_COLOR_PIXEL_DARK_BG);
                    }
                }
                if (Prefs.getBoolean(CUSTOM_SECONDARY_COLOR_SWITCH) && FabricatedUtil.isOverlayDisabled(FabricatedEnabledOverlays, COLOR_ACCENT_SECONDARY)) {
                    if (!amc_reApplied && OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                    }

                    if (!Objects.equals(colorAccentSecondary, INVALID))
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                    else
                        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_ACCENT_SECONDARY, "color", "holo_green_light", ICONIFY_COLOR_ACCENT_SECONDARY);
                }
                Prefs.putBoolean("customColor", true);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void applyQsCustomization() {
        Runnable runnable = () -> {
            if (!Objects.equals(Prefs.getString(BOOT_ID), Shell.cmd(DEVICE_BOOT_ID_CMD).exec().getOut().toString()) && OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentQSPB.overlay")) {
                OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
                OverlayUtil.enableOverlay("IconifyComponentQSPB.overlay");
            }
            SystemUtil.getBootId();

            if (Prefs.getBoolean(QS_ROW_COLUMN_SWITCH) && FabricatedUtil.isOverlayDisabled(FabricatedEnabledOverlays, FABRICATED_QS_ROW))
                QsRowColumn.applyRowColumn();

        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
