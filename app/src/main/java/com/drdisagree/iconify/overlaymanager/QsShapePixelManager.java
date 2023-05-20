package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPESPIXEL;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class QsShapePixelManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        enable_pack(n);

        if (!Prefs.getBoolean("IconifyComponentCR1.overlay") || !Prefs.getBoolean("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSP" + n + ".overlay";

            if (!Prefs.getBoolean(overlay)) OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSP" + n + ".overlay";

            if (Prefs.getBoolean(overlay)) OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_QSSHAPESPIXEL; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentQSSP" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentQSSP" + i + ".overlay";

                    if (Prefs.getBoolean(overlay)) OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}