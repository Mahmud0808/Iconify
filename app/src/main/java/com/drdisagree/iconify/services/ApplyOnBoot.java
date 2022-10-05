package com.drdisagree.iconify.services;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.ui.QsRowColumn;
import com.drdisagree.iconify.utils.FabricatedOverlay;
import com.drdisagree.iconify.utils.OverlayUtils;

import java.util.List;
import java.util.Objects;

public class ApplyOnBoot {

    private static final String INVALID = "null";
    private static List<String> overlays = OverlayUtils.getEnabledOverlayList();
    private static List<String> fabricatedOverlays = FabricatedOverlay.getEnabledOverlayList();

    public static void applyColors() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                String colorAccentPrimary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary");
                String colorAccentSecondary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary");
                if ((PrefConfig.loadPrefBool(Iconify.getAppContext(), "customPrimaryColor") && PrefConfig.loadPrefBool(Iconify.getAppContext(), "customSecondaryColor")) && (FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "colorAccentPrimary") || FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "colorAccentSecondary"))) {
                    if (OverlayUtils.isOverlayEnabled(overlays, "IconifyComponentAMC.overlay")) {
                        OverlayUtils.disableOverlay("IconifyComponentAMC.overlay");
                        OverlayUtils.enableOverlay("IconifyComponentAMC.overlay");
                    }
                    if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "customPrimaryColor") && FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "colorAccentPrimary")) {
                        if (!Objects.equals(colorAccentPrimary, INVALID))
                            FabricatedOverlay.buildOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", ColorToSpecialHex(Integer.parseInt(colorAccentPrimary)));
                        else
                            FabricatedOverlay.buildOverlay("android", "colorAccentPrimary", "color", "holo_blue_light", "0xFF50A6D7");

                        FabricatedOverlay.enableOverlay("colorAccentPrimary");
                    }
                    if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "customSecondaryColor") && FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "colorAccentSecondary")) {
                        if (!Objects.equals(colorAccentSecondary, INVALID))
                            FabricatedOverlay.buildOverlay("android", "colorAccentSecondary", "color", "holo_green_light", ColorToSpecialHex(Integer.parseInt(colorAccentSecondary)));
                        else
                            FabricatedOverlay.buildOverlay("android", "colorAccentSecondary", "color", "holo_green_light", "0xFF387BFF");

                        FabricatedOverlay.enableOverlay("colorAccentSecondary");
                    }
                    PrefConfig.savePrefBool(Iconify.getAppContext(), "customColor", true);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void applyCornerRadius() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "dialogCornerRadius") && PrefConfig.loadPrefBool(Iconify.getAppContext(), "fabricatedcornerRadius")) {
                    if (OverlayUtils.isOverlayDisabled(overlays, "IconifyComponentCR.overlay") && PrefConfig.loadPrefSettings(Iconify.getAppContext(), "dialogCornerRadius").equals("null"))
                        OverlayUtils.enableOverlay("IconifyComponentCR.overlay");
                    if (!PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius").equals("null")) {
                        FabricatedOverlay.buildOverlay("android", "dialogCornerRadius", "dimen", "dialog_corner_radius", "0x" + ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 8 + 16) * 100));
                        FabricatedOverlay.buildOverlay("android", "insetCornerRadius2", "dimen", "harmful_app_name_padding_right", "0x" + ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 6 + 16) * 100));
                        FabricatedOverlay.buildOverlay("android", "insetCornerRadius4", "dimen", "harmful_app_name_padding_left", "0x" + ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "cornerRadius")) + 4 + 16) * 100));
                        FabricatedOverlay.enableOverlay("dialogCornerRadius");
                        FabricatedOverlay.enableOverlay("insetCornerRadius2");
                        FabricatedOverlay.enableOverlay("insetCornerRadius4");
                    }
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public static void applyQsCustomization() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "qsRowColumn") && FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "qsRow"))
                    QsRowColumn.applyRowColumn();

                if (OverlayUtils.isOverlayDisabled(overlays, "IconifyComponentQSHL.overlay") && PrefConfig.loadPrefBool(Iconify.getAppContext(), "fabricatedqsTextSize") && FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "qsTileTextSize")) {
                    FabricatedOverlay.buildOverlay("systemui", "qsTileTextSize", "dimen", "qs_tile_text_size", "0x" + ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsTextSize")) + 10 + 14) * 100));
                    FabricatedOverlay.enableOverlay("qsTileTextSize");
                }

                if (PrefConfig.loadPrefBool(Iconify.getAppContext(), "fabricatedqsIconSize") && FabricatedOverlay.isOverlayDisabled(fabricatedOverlays, "qsTileIconSize")) {
                    FabricatedOverlay.buildOverlay("systemui", "qsTileIconSize", "dimen", "qs_icon_size", "0x" + ((Integer.parseInt(PrefConfig.loadPrefSettings(Iconify.getAppContext(), "qsIconSize")) + 10 + 16) * 100));
                    FabricatedOverlay.enableOverlay("qsTileIconSize");
                }
            }
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

        StringBuilder str = new StringBuilder("0xFF");
//      str.append(alphaHex);
        str.append(redHex);
        str.append(greenHex);
        str.append(blueHex);
        return str.toString();
    }

    private static String To00Hex(int value) {
        String hex = "00".concat(Integer.toHexString(value));
        return hex.substring(hex.length() - 2);
    }
}
