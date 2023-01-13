package com.drdisagree.iconify.services;

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
                if (Prefs.getBoolean("customSecondaryColor") && FabricatedOverlayUtil.isOverlayDisabled(FabricatedEnabledOverlays, "colorAccentSecondary")) {
                    if (!amc_reApplied && OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtil.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtil.enableOverlay("IconifyComponentAMC.overlay");
                    }

                    if (!Objects.equals(colorAccentSecondary, INVALID))
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                    else
                        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");
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
