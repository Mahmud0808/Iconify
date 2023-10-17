package com.drdisagree.iconify.utils.overlay.manager;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class MediaPlayerIconManager {

    public static void enableOverlay(int m, int n) {
        disable_others(m, n);
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentMPIP" + m + n + ".overlay");
    }

    public static void disableOverlay(int m, int n) {
        OverlayUtil.disableOverlay("IconifyComponentMPIP" + m + n + ".overlay");
    }

    private static void disable_others(int m, int n) {
        for (int i = 1; i <= 3; i++) {
            Prefs.putBoolean("IconifyComponentMPIP" + m + i + ".overlay", i == n);
        }
    }
}