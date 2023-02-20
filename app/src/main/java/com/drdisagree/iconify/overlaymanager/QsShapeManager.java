package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_QSSHAPES;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class QsShapeManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);

        if (!Prefs.getBoolean("IconifyComponentCR.overlay"))
            OverlayUtil.enableOverlay("IconifyComponentCR.overlay");
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSN" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSN" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSN" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSN" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_QSSHAPES; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentQSSN" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentQSSN" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}