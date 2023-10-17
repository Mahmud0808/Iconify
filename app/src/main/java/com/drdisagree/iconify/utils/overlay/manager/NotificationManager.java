package com.drdisagree.iconify.utils.overlay.manager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_NOTIFICATIONS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class NotificationManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentNFN" + n + ".overlay");

        if (!OverlayUtil.isOverlayEnabled("IconifyComponentCR1.overlay") || !OverlayUtil.isOverlayEnabled("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlay("IconifyComponentNFN" + n + ".overlay");
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_NOTIFICATIONS; i++) {
            Prefs.putBoolean("IconifyComponentNFN" + i + ".overlay", i == n);
        }
    }
}