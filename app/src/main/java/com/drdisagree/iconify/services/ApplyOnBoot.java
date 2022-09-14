package com.drdisagree.iconify.services;

import android.annotation.SuppressLint;
import android.content.Context;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.SplashActivity;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.utils.OverlayUtils;
import com.topjohnwu.superuser.Shell;

import java.util.Objects;

public class ApplyOnBoot {

    private static final String INVALID = "null";

    public static void applyColor() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                OverlayUtils.disableOverlay("IconifyComponentAMC.overlay");
                OverlayUtils.enableOverlay("IconifyComponentAMC.overlay");
                String colorAccentPrimary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentPrimary");
                if (!Objects.equals(colorAccentPrimary, INVALID)) {
                    Shell.cmd("cmd overlay fabricate --target android --name colorAccentPrimary android:color/holo_blue_light 0x1c " + ColorToSpecialHex(Integer.parseInt(colorAccentPrimary))).exec();
                    OverlayUtils.enableOverlay("com.android.shell:colorAccentPrimary");
                }

                String colorAccentSecondary = PrefConfig.loadPrefSettings(Iconify.getAppContext(), "colorAccentSecondary");
                if (!Objects.equals(colorAccentSecondary, INVALID)) {
                    Shell.cmd("cmd overlay fabricate --target android --name colorAccentSecondary android:color/holo_green_light 0x1c " + ColorToSpecialHex(Integer.parseInt(colorAccentSecondary))).exec();
                    OverlayUtils.enableOverlay("com.android.shell:colorAccentSecondary");
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
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
        return hex.substring(hex.length() - 2, hex.length());
    }
}
