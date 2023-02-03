package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.References.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activity.HomePage;
import com.drdisagree.iconify.ui.activity.QsRowColumn;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class ApplyOnBoot {

    private static final String INVALID = "null";
    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    public static List<String> FabricatedEnabledOverlays = FabricatedOverlayUtil.getEnabledOverlayList();

    public static void applyColors() {
        Runnable runnable = () -> {
            if (Prefs.getBoolean("customColor")) {
                Prefs.putBoolean("customPrimaryColor", true);
                Prefs.putBoolean("customSecondaryColor", true);
            }

            String colorAccentPrimary = Prefs.getString("colorAccentPrimary");
            String colorAccentSecondary = Prefs.getString("colorAccentSecondary");
            if ((Prefs.getBoolean("customPrimaryColor") || Prefs.getBoolean("customSecondaryColor")) && (FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "colorAccentPrimary") || FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "colorAccentSecondary"))) {
                boolean amc_reApplied = false;
                if (Prefs.getBoolean("customPrimaryColor") && FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "colorAccentPrimary")) {
                    if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                        amc_reApplied = true;
                    }

                    if (!Objects.equals(colorAccentPrimary, INVALID)) {
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary", "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary1_100", "color", "system_accent1_100", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary1_200", "color", "system_accent1_200", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary1_300", "color", "system_accent1_300", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary2_100", "color", "system_accent2_100", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary2_200", "color", "system_accent2_200", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary3_200", "color", "system_accent2_300", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorPixelBackgroundDark", "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(colorAccentPrimary), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
                    } else {
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary1_100", "color", "system_accent1_100", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary1_200", "color", "system_accent1_200", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary1_300", "color", "system_accent1_300", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary2_100", "color", "system_accent2_100", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary2_200", "color", "system_accent2_200", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentPrimary3_200", "color", "system_accent2_300", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorPixelBackgroundDark", "color", "holo_blue_dark", "0xFF122530");
                    }
                }
                if (Prefs.getBoolean("customSecondaryColor") && FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "colorAccentSecondary")) {
                    if (!amc_reApplied && OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                    }

                    if (!Objects.equals(colorAccentSecondary, INVALID))
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                    else
                        FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
                }
                Prefs.putBoolean("customColor", true);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void applyQsCustomization() {
        Runnable runnable = () -> {
            if (!Objects.equals(Prefs.getString("boot_id"), Shell.cmd("cat /proc/sys/kernel/random/boot_id").exec().getOut().toString()) && OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentQSPB.overlay")) {
                OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
                OverlayUtil.enableOverlay("IconifyComponentQSPB.overlay");
            }
            HomePage.getBootId();

            if (Prefs.getBoolean("fabricatedqsRowColumn") && FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "qsRow"))
                QsRowColumn.applyRowColumn();

        };
        Thread thread = new Thread(runnable);
        thread.start();
    }
}
