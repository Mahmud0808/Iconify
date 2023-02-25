package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_ICONPACKS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class IconPackManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        enable_pack(n);
    }

    protected static void enable_pack(int n) {
        String[] paths = {"/system/product/overlay/IconifyComponentIPAS" + n + ".apk", "/system/product/overlay/IconifyComponentIPSUI" + n + ".apk"};

        if (new File(paths[0]).exists()) {
            String overlay = "IconifyComponentIPAS" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }

        if (new File(paths[1]).exists()) {
            String overlay = "IconifyComponentIPSUI" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String[] paths = {"/system/product/overlay/IconifyComponentIPAS" + n + ".apk", "/system/product/overlay/IconifyComponentIPSUI" + n + ".apk"};

        if (new File(paths[0]).exists()) {
            String overlay = "IconifyComponentIPAS" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }

        if (new File(paths[1]).exists()) {
            String overlay = "IconifyComponentIPSUI" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_ICONPACKS; i++) {
            if (i != n) {
                String[] paths = {"/system/product/overlay/IconifyComponentIPAS" + i + ".apk", "/system/product/overlay/IconifyComponentIPSUI" + i + ".apk"};

                if (new File(paths[0]).exists()) {
                    String overlay = "IconifyComponentIPAS" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }

                if (new File(paths[1]).exists()) {
                    String overlay = "IconifyComponentIPSUI" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}