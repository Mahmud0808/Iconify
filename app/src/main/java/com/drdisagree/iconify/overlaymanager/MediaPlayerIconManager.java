package com.drdisagree.iconify.overlaymanager;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class MediaPlayerIconManager {

    public static void enableOverlay(int m, int n) {
        disable_others(m, n);
        enable_pack(m, n);
    }

    protected static void enable_pack(int m, int n) {
        String path = "/system/product/overlay/IconifyComponentMPIP" + m + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentMPIP" + m + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int m, int n) {
        String path = "/system/product/overlay/IconifyComponentMPIP" + m + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentMPIP" + m + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int m, int n) {
        for (int i = 1; i <= 3; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentMPIP" + m + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentMPIP" + m + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}