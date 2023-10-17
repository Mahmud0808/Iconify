package com.drdisagree.iconify.utils.overlay.manager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_ICONPACKS;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

public class IconPackManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        OverlayUtil.enableOverlaysExclusiveInCategory(
                "IconifyComponentIPAS" + n + ".overlay",
                "IconifyComponentIPSUI" + n + ".overlay"
        );
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlays("IconifyComponentIPAS" + n + ".overlay", "IconifyComponentIPSUI" + n + ".overlay");
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_ICONPACKS; i++) {
            Prefs.putBoolean("IconifyComponentIPAS" + i + ".overlay", i == n);
            Prefs.putBoolean("IconifyComponentIPSUI" + i + ".overlay", i == n);
        }
    }
}