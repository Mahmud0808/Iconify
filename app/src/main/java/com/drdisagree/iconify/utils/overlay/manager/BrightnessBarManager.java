package com.drdisagree.iconify.utils.overlay.manager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_BRIGHTNESSBARS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class BrightnessBarManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentBBN" + n + ".overlay");

        if (!OverlayUtil.isOverlayEnabled("IconifyComponentCR1.overlay") || !OverlayUtil.isOverlayEnabled("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlay("IconifyComponentBBN" + n + ".overlay");
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_BRIGHTNESSBARS; i++) {
            Prefs.putBoolean("IconifyComponentBBN" + i + ".overlay", i == n);
            Prefs.putBoolean("IconifyComponentBBP" + i + ".overlay", false);
        }
    }
}