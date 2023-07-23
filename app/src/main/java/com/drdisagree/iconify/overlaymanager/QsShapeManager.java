package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPES;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class QsShapeManager {

    public static final String QSC_overlay = "IconifyComponentQSC.overlay";
    public static final String QST1_overlay = "IconifyComponentQST1.overlay";
    public static final String QST2_overlay = "IconifyComponentQST2.overlay";
    public static final String QST3_overlay = "IconifyComponentQST3.overlay";
    public static final String QST4_overlay = "IconifyComponentQST4.overlay";
    public static final String QST5_overlay = "IconifyComponentQST5.overlay";

    public static void enableOverlay(int n) {
        disable_others(n);
        enable_pack(n);

        if (!Prefs.getBoolean("IconifyComponentCR1.overlay") || !Prefs.getBoolean("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlays("IconifyComponentCR1.overlay", "IconifyComponentCR2.overlay");
        }
    }

    protected static void enable_pack(int n) {
        boolean QST1_state = Prefs.getBoolean(QST1_overlay);
        boolean QST2_state = Prefs.getBoolean(QST2_overlay);
        boolean QST3_state = Prefs.getBoolean(QST3_overlay);
        boolean QST4_state = Prefs.getBoolean(QST4_overlay);
        boolean QST5_state = Prefs.getBoolean(QST5_overlay);

        String path = "/system/product/overlay/IconifyComponentQSSN" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSN" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.changeOverlayState(overlay, true, QSC_overlay, true, QST1_overlay, QST1_state, QST2_overlay, QST2_state, QST3_overlay, QST3_state, QST4_overlay, QST4_state, QST5_overlay, QST5_state);
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