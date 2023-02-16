package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.STR_NULL;
import static com.drdisagree.iconify.common.References.TOTAL_NOTIFICATIONS;
import static com.drdisagree.iconify.common.References.UI_CORNER_RADIUS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class NotificationManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);

        if (Prefs.getString(UI_CORNER_RADIUS).equals(STR_NULL))
            OverlayUtil.enableOverlay("IconifyComponentCR16.overlay");
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentNFN" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentNFN" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentNFN" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentNFN" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_NOTIFICATIONS; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentNFN" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentNFN" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}