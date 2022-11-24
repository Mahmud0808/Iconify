package com.drdisagree.iconify.installer;

import static com.drdisagree.iconify.common.References.TOTAL_TEXTSIZE;

import com.topjohnwu.superuser.Shell;

import java.io.File;

public class TextSizeInstaller {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
    }

    protected static void enable_pack(int n) {

        String path = "/system/product/overlay/IconifyComponentTextSize" + n + ".apk";

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

        String path = "/system/product/overlay/IconifyComponentTextSize" + n + ".apk";

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

        for (int i = 0; i <= TOTAL_TEXTSIZE; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentTextSize" + i + ".apk";

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