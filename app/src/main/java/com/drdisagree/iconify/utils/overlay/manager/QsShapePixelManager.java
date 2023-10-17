package com.drdisagree.iconify.utils.overlay.manager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPESPIXEL;
import static com.drdisagree.iconify.common.Resources.QSC_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT1_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT2_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT3_overlay;
import static com.drdisagree.iconify.common.Resources.QSPT4_overlay;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;

import java.io.File;

public class QsShapePixelManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        enable_pack(n);

        if (!Prefs.getBoolean("IconifyComponentCR1.overlay") || !Prefs.getBoolean("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    protected static void enable_pack(int n) {
        boolean QST1_state = Prefs.getBoolean(QSPT1_overlay);
        boolean QST2_state = Prefs.getBoolean(QSPT2_overlay);
        boolean QST3_state = Prefs.getBoolean(QSPT3_overlay);
        boolean QST4_state = Prefs.getBoolean(QSPT4_overlay);

        String path = "/system/product/overlay/IconifyComponentQSSP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSP" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.changeOverlayState(overlay, true, QSC_overlay, true, QSPT1_overlay, QST1_state, QSPT2_overlay, QST2_state, QSPT3_overlay, QST3_state, QSPT4_overlay, QST4_state);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSP" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlays(overlay, QSC_overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_QSSHAPESPIXEL; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentQSSP" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentQSSP" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}