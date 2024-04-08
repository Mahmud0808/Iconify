package com.drdisagree.iconify.utils.overlay.manager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPESPIXEL;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class QsShapePixelManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        OverlayUtil.enableOverlayExclusiveInCategory("IconifyComponentQSSP" + n + ".overlay");

        if (!OverlayUtil.isOverlayEnabled("IconifyComponentCR1.overlay") || !OverlayUtil.isOverlayEnabled("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }

        OverlayUtil.changeOverlayState(
                "IconifyComponentQSPT1.overlay",
                !OverlayUtil.isOverlayEnabled("IconifyComponentQSPT1.overlay"),
                "IconifyComponentQSPT1.overlay",
                OverlayUtil.isOverlayEnabled("IconifyComponentQSPT1.overlay"),
                "IconifyComponentQSPT2.overlay",
                !OverlayUtil.isOverlayEnabled("IconifyComponentQSPT2.overlay"),
                "IconifyComponentQSPT2.overlay",
                OverlayUtil.isOverlayEnabled("IconifyComponentQSPT2.overlay"),
                "IconifyComponentQSPT3.overlay",
                !OverlayUtil.isOverlayEnabled("IconifyComponentQSPT3.overlay"),
                "IconifyComponentQSPT3.overlay",
                OverlayUtil.isOverlayEnabled("IconifyComponentQSPT3.overlay"),
                "IconifyComponentQSPT4.overlay",
                !OverlayUtil.isOverlayEnabled("IconifyComponentQSPT4.overlay"),
                "IconifyComponentQSPT4.overlay",
                OverlayUtil.isOverlayEnabled("IconifyComponentQSPT4.overlay")
        );
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlay("IconifyComponentQSSP" + n + ".overlay");
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_QSSHAPESPIXEL; i++) {
            Prefs.putBoolean("IconifyComponentQSSP" + i + ".overlay", i == n);
            Prefs.putBoolean("IconifyComponentQSSN" + i + ".overlay", false);
        }
    }
}