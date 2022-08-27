package com.drdisagree.iconify;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IconInstaller {

    public static void install_icon(int n) {
        disable_others(n);
        enable_pack(n);
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= 5; i++) {
            if (i == n)
                continue;
            else
                disable_pack(n);
        }
    }

    private static void enable_pack(int n) {
        Process process = null;
        String[] paths = {"/system/product/overlay/AnotherThemeBattery" + n + ".apk", "/system/product/overlay/AnotherThemeBatteryColor" + n + ".apk", "/system/product/overlay/AnotherThemeBatteryPadding" + n + ".apk", "/system/product/overlay/AnotherThemeBatterySize" + n + ".apk", "/system/product/overlay/AnotherThemeIcons1" + n + ".apk", "/system/product/overlay/AnotherThemeIcons2" + n + ".apk", "/system/product/overlay/AnotherThemeNavbar" + n + ".apk", "/system/product/overlay/AnotherThemeSignal1" + n + ".apk", "/system/product/overlay/AnotherThemeSignal2" + n + ".apk", "/system/product/overlay/AnotherThemeSignalType" + n + ".apk", "/system/product/overlay/AnotherThemeStatusbar" + n + ".apk",};

        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream stdin = process.getOutputStream();
        InputStream stderr = process.getErrorStream();
        InputStream stdout = process.getInputStream();

        for (String path : paths) {
            if (new File(path).exists()) {

                String overlay = (path.replaceAll("/system/product/overlay/", "")).replaceAll("apk", "overlay");

                try {
                    stdin.write(("cmd overlay enable --user current " + overlay + "\n").getBytes());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        try {
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void disable_pack(int n) {
        Process process = null;
        String[] paths = {"/system/product/overlay/AnotherThemeBattery" + n + ".apk", "/system/product/overlay/AnotherThemeBatteryColor" + n + ".apk", "/system/product/overlay/AnotherThemeBatteryPadding" + n + ".apk", "/system/product/overlay/AnotherThemeBatterySize" + n + ".apk", "/system/product/overlay/AnotherThemeIcons1" + n + ".apk", "/system/product/overlay/AnotherThemeIcons2" + n + ".apk", "/system/product/overlay/AnotherThemeNavbar" + n + ".apk", "/system/product/overlay/AnotherThemeSignal1" + n + ".apk", "/system/product/overlay/AnotherThemeSignal2" + n + ".apk", "/system/product/overlay/AnotherThemeSignalType" + n + ".apk", "/system/product/overlay/AnotherThemeStatusbar" + n + ".apk",};

        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        OutputStream stdin = process.getOutputStream();
        InputStream stderr = process.getErrorStream();
        InputStream stdout = process.getInputStream();

        for (String path : paths) {
            if (new File(path).exists()) {

                String overlay = (path.replaceAll("/system/product/overlay/", "")).replaceAll("apk", "overlay");

                try {
                    stdin.write(("cmd overlay disable --user current " + overlay + "\n").getBytes());
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
        try {
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}