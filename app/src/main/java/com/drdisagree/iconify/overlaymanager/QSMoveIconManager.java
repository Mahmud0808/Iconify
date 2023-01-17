package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_MOVEICON;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class QSMoveIconManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentMoveIcon" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentMoveIcon" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentMoveIcon" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentMoveIcon" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 0; i <= TOTAL_MOVEICON; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentMoveIcon" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentMoveIcon" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}