package com.drdisagree.iconify.services;

import android.graphics.Color;

import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.ui.activity.HomePage;
import com.drdisagree.iconify.ui.activity.QsRowColumn;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.util.Objects;

public class ApplyOnBoot {

    private static final String INVALID = "null";

    public static void applyColors() {
        Runnable runnable = () -> {
            if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "customColor")) {
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customPrimaryColor", true);
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customSecondaryColor", true);
            }

            String colorAccentPrimary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary");
            String colorAccentSecondary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary");
            if ((PrefConfig.loadPrefBool(Iconify.getAppContext(), "customPrimaryColor") || PrefConfig.loadPrefBool(Iconify.getAppContext(), "customSecondaryColor")) && (FabricatedOverlayUtil.isOverlayDisabled(References.FabricatedEnabledOverlays, "colorAccentPrimary") || FabricatedOverlayUtil.isOverlayDisabled(References.FabricatedEnabledOverlays, "colorAccentSecondary"))) {
                boolean amc_reApplied = false;
                if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "customPrimaryColor") && FabricatedOverlayUtil.isOverlayDisabled(References.FabricatedEnabledOverlays, "colorAccentPrimary")) {
                    if (OverlayUtil.isOverlayEnabled(References.EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                        amc_reApplied = true;
                    }

                    if (!Objects.equals(colorAccentPrimary, INVALID)) {
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary1_100", "color", "system_accent1_100", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary1_200", "color", "system_accent1_200", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary1_300", "color", "system_accent1_300", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary2_100", "color", "system_accent2_100", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary2_200", "color", "system_accent2_200", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary3_200", "color", "system_accent2_300", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(Integer.parseInt(colorAccentPrimary), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));
                    } else {
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary1_100", "color", "system_accent1_100", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary1_200", "color", "system_accent1_200", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary1_300", "color", "system_accent1_300", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary2_100", "color", "system_accent2_100", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary2_200", "color", "system_accent2_200", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimary3_200", "color", "system_accent2_300", "0xFF50A6D7");
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", "0xFF122530");
                    }
                }
                if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "customSecondaryColor") && FabricatedOverlayUtil.isOverlayDisabled(References.FabricatedEnabledOverlays, "colorAccentSecondary")) {
                    if (!amc_reApplied && OverlayUtil.isOverlayEnabled(References.EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                    }

                    if (!Objects.equals(colorAccentSecondary, INVALID))
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                    else
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
                }
                PrefConfig.savePrefBool(Iconify.getAppContext(), "customColor", true);
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void applyQsCustomization() {
        Runnable runnable = () -> {
            if (!Objects.equals(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "boot_id"), Shell.cmd("cat /proc/sys/kernel/random/boot_id").exec().getOut().toString()) && OverlayUtil.isOverlayEnabled(References.EnabledOverlays, "IconifyComponentQSPB.overlay")) {
                OverlayUtil.disableOverlay("IconifyComponentQSPB.overlay");
                OverlayUtil.enableOverlay("IconifyComponentQSPB.overlay");
            }
            HomePage.getBootId();

            if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "fabricatedqsRowColumn") && FabricatedOverlayUtil.isOverlayDisabled(References.FabricatedEnabledOverlays, "qsRow"))
                QsRowColumn.applyRowColumn();

        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

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

        //      str.append(alphaHex);
        return "0xFF" + redHex + greenHex + blueHex;
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length() - 2);
    }
}
