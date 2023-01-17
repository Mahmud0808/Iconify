package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_ICONSIZE;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class IconSizeManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentIconSize" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentIconSize" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentIconSize" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentIconSize" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 0; i <= TOTAL_ICONSIZE; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentIconSize" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentIconSize" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}