package com.drdisagree.iconify.installer;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.config.PrefConfig;
import com.drdisagree.iconify.services.ApplyOnBoot;
import com.topjohnwu.superuser.Shell;

import java.io.File;

public class BrightnessInstaller {

    private static final int TOTAL_BRIGHTNESSBARS = 9;

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
        ApplyOnBoot.applyDefaultColors();
        PrefConfig.savePrefBool(Iconify.getAppContext(), "fabricatedcornerRadius", true);
        ApplyOnBoot.applyCornerRadius();
    }

    protected static void enable_pack(int n) {

        String path = "/system/product/overlay/IconifyComponentBB" + n + ".apk";

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

        String path = "/system/product/overlay/IconifyComponentBB" + n + ".apk";

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

        for (int i = 1; i <= TOTAL_BRIGHTNESSBARS; i++) {
            if (i != n) {
                String path = "/system/product/overlay/IconifyComponentBB" + i + ".apk";

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