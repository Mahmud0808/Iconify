package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_BRIGHTNESSBARSPIXEL;

import android.graphics.Color;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.services.ApplyOnBoot;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;

import java.io.File;

public class BrightnessPixelManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
        FabricatedOverlayUtil.buildAndEnableOverlay("android", "colorAccentPrimaryDark", "color", "holo_blue_dark", ApplyOnBoot.ColorToSpecialHex(ColorUtils.blendARGB(ColorUtils.blendARGB(ContextCompat.getColor(Iconify.getAppContext(), R.color.holo_blue_light), Color.BLACK, 0.8f), Color.WHITE, 0.12f)));

        if (Prefs.getString("cornerRadius").equals("null"))
            OverlayUtil.enableOverlay("IconifyComponentCR16.overlay");
    }

    protected static void enable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentBBP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentBBP" + n + ".overlay";

            if (!Prefs.getBoolean(overlay))
                OverlayUtil.enableOverlay(overlay);
        }
    }

    public static void disable_pack(int n) {
        String path = "/system/product/overlay/IconifyComponentBBP" + n + ".apk";

        if (new File(path).exists()) {
            String overlay = "IconifyComponentBBP" + n + ".overlay";

            if (Prefs.getBoolean(overlay))
                OverlayUtil.disableOverlay(overlay);
        }
    }

    protected static void disable_others(int n) {

        for (int i = 1; i <= TOTAL_BRIGHTNESSBARSPIXEL; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentBBP" + i + ".apk";

                if (new File(path).exists()) {
                    String overlay = "IconifyComponentBBP" + i + ".overlay";

                    if (Prefs.getBoolean(overlay))
                        OverlayUtil.disableOverlay(overlay);
                }
            }
        }
    }
}