package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_NOTIFICATIONS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class NotificationManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);

        if (Prefs.getString("cornerRadius").equals("null"))
            OverlayUtil.enableOverlay("IconifyComponentCR16.overlay");
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentNF" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentNF" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentNF" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentNF" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_NOTIFICATIONS; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentNF" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentNF" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}