package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_ICONSHAPES;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class IconShapeManager {

    public static void enableOverlay(int n) {
        if (n == 0) {
            disable_others(n);
        } else {
            disable_others(n);
            enable_pack(n);
        }
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentSIS" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentSIS" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentSIS" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentSIS" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 0; i <= TOTAL_ICONSHAPES; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentSIS" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentSIS" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}