package com.drdisagree.iconify.utils.overlay.manager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_SETTINGSICONPACKS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

import java.io.File;

public class SettingsIconManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        enable_pack(n);

        if (!Prefs.getBoolean("IconifyComponentCR1.overlay") || !Prefs.getBoolean("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentSIP" + n + '1' + ".apk";

        if (new File(path).exists()) {
            String[] overlays = new String[]{
                    "IconifyComponentSIP" + n + '1' + ".overlay",
                    "IconifyComponentSIP" + n + '2' + ".overlay",
                    "IconifyComponentSIP" + n + '3' + ".overlay"
            };

            for (String overlay : overlays) {
                if (!Prefs.getBoolean(overlay))
                    OverlayUtil.enableOverlay(overlay);
            }
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentSIP" + n + '1' + ".apk";

        if (new File(path).exists()) {
            String[] overlays = new String[]{
                    "IconifyComponentSIP" + n + '1' + ".overlay",
                    "IconifyComponentSIP" + n + '2' + ".overlay",
                    "IconifyComponentSIP" + n + '3' + ".overlay"
            };

            for (String overlay : overlays) {
                if (Prefs.getBoolean(overlay))
                    OverlayUtil.disableOverlay(overlay);
            }
        }
    }

    protected static void disable_others(int n) {
        for (int i = 0; i <= TOTAL_SETTINGSICONPACKS; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentSIP" + i + '1' + ".apk";

                if (new File(path).exists()) {
                    String[] overlays = new String[]{
                            "IconifyComponentSIP" + i + '1' + ".overlay",
                            "IconifyComponentSIP" + i + '2' + ".overlay",
                            "IconifyComponentSIP" + i + '3' + ".overlay"
                    };

                    for (String overlay : overlays) {
                        if (Prefs.getBoolean(overlay))
                            OverlayUtil.disableOverlay(overlay);
                    }
                }
            }
        }
    }
}