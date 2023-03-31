package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Dynamic.TOTAL_QSSHAPESPIXEL;
import static com.drdisagree.iconify.common.Preferences.COLOR_PIXEL_DARK_BG;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.FabricatedUtil;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class QsShapePixelManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        enable_pack(n);

        FabricatedUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, COLOR_PIXEL_DARK_BG, "color", "holo_blue_dark", ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(ContextCompat.getColor(Iconify.getAppContext(), R.color.holo_blue_light), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));

        if (!Prefs.getBoolean("IconifyComponentCR1.overlay") || !Prefs.getBoolean("IconifyComponentCR2.overlay")) {
            OverlayUtil.enableOverlay("IconifyComponentCR1.overlay");
            OverlayUtil.enableOverlay("IconifyComponentCR2.overlay");
        }
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSP" + n + ".overlay";

            if (!Prefs.getBoolean(overlay)) OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentQSSP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentQSSP" + n + ".overlay";

            if (Prefs.getBoolean(overlay)) OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_QSSHAPESPIXEL; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentQSSP" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentQSSP" + i + ".overlay";

                    if (Prefs.getBoolean(overlay)) OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}