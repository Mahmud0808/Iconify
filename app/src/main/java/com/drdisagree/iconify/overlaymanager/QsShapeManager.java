package com.drdisagree.iconify.overlaymanager;

import static com.drdisagree.iconify.common.References.TOTAL_QSSHAPES;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class QsShapeManager {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);

        if (Prefs.getString("cornerRadius").equals("null"))
            OverlayUtil.enableOverlay("IconifyComponentCR16.overlay");
    }

    protected static void enable_pack(int n) {

        String path = "/system/product/overlay/IconifyComponentQSSN" + n + ".apk";

        if (new File(path).exists()) {

            String overlay = (path.replaceAll("/system/product/overlay/", "")).replaceAll("apk", "overlay");

            try {
                Shell.cmd("cmd overlay enable --user current " + overlay).exec();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public static void disable_pack(int n) {

        String path = "/system/product/overlay/IconifyComponentQSSN" + n + ".apk";

        if (new File(path).exists()) {

            String overlay = (path.replaceAll("/system/product/overlay/", "")).replaceAll("apk", "overlay");

            try {
                Shell.cmd("cmd overlay disable --user current " + overlay).exec();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    protected static void disable_others(int n) {

        for (int i = 1; i <= TOTAL_QSSHAPES; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentQSSN" + i + ".apk";

                if (new File(path).exists()) {

                    String overlay = (path.replaceAll("/system/product/overlay/", "")).replaceAll("apk", "overlay");

                    try {
                        Shell.cmd("cmd overlay disable --user current " + overlay).exec();
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    }
}