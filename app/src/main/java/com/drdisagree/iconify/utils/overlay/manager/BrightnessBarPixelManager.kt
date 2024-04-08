package com.drdisagree.iconify.utils.overlay.manager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_BRIGHTNESSBARSPIXEL;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class BrightnessBarPixelManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentBBP" + n + ".overlay");

        if (!OverlayUtil.isOverlayEnabled("IconifyComponentCR1.overlay") || !OverlayUtil.isOverlayEnabled("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlay("IconifyComponentBBP" + n + ".overlay");
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_BRIGHTNESSBARSPIXEL; i++) {
            Prefs.putBoolean("IconifyComponentBBP" + i + ".overlay", i == n);
            Prefs.putBoolean("IconifyComponentBBN" + i + ".overlay", false);
        }
    }
}