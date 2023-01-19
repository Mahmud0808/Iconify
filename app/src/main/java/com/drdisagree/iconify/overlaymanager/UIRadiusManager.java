package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_RADIUS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class UIRadiusManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentCR" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentCR" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);

            FabricatedOverlayUtil.buildAndEnableOverlay("com.android.systemui", "qsScrimCornerRadius", "dimen", "notification_scrim_corner_radius", (n + 8) + "dp");
        }
    }

    public static void disable_pack(int n) {

        String path = "/system/product/overlay/IconifyComponentCR" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentCR" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 0; i <= TOTAL_RADIUS; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentCR" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentCR" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}