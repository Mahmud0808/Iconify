package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.STR_NULL;
import static com.drdisagree.iconify.common.References.TOTAL_NOTIFICATIONSPIXEL;
import static com.drdisagree.iconify.common.References.UI_CORNER_RADIUS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class NotificationPixelManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);

        if (Prefs.getString(UI_CORNER_RADIUS).equals(STR_NULL))
            OverlayUtil.enableOverlay("IconifyComponentCR16.overlay");
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentNFP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentNFP" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentNFP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentNFP" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_NOTIFICATIONSPIXEL; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentNFP" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentNFP" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}