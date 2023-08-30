package com.drdisagree.iconify.utils.overlaymanager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPES;
import static com.drdisagree.iconify.common.Resources.QSC_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT1_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT2_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT3_overlay;
import static com.drdisagree.iconify.common.Resources.QSNT4_overlay;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class QsShapeManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        enable_pack(n);

        if (!Prefs.getBoolean("IconifyComponentCR1.overlay") || !Prefs.getBoolean("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    protected static void enable_pack(int n) {
        boolean QST1_state = Prefs.getBoolean(QSNT1_overlay);
        boolean QST2_state = Prefs.getBoolean(QSNT2_overlay);
        boolean QST3_state = Prefs.getBoolean(QSNT3_overlay);
        boolean QST4_state = Prefs.getBoolean(QSNT4_overlay);

        String path = "/system/product/overlay/IconifyComponentQSSN" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSN" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.changeOverlayState(overlay, true, QSC_overlay, true, QSNT1_overlay, QST1_state, QSNT2_overlay, QST2_state, QSNT3_overlay, QST3_state, QSNT4_overlay, QST4_state);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSN" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSN" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlays(overlay, QSC_overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_QSSHAPES; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentQSSN" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentQSSN" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}