package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_TEXTSIZE;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class QSTextSizeManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentTextSize" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentTextSize" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentTextSize" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentTextSize" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 0; i <= TOTAL_TEXTSIZE; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentTextSize" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentTextSize" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}