package com.drdisagree.iconify;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class IconInstaller {

    public static void install_pack(int n) {
        disable_others(n);
        enable_pack(n);
    }

    protected static void enable_pack(int n) {

        String[] paths = {"/system/product/overlay/IconifyComponentIPAS" + n + ".apk", "/system/product/overlay/IconifyComponentIPSUI" + n + ".apk"};

        Process process = null;

        // Get root permission
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Terminal input
        OutputStream stdin = process.getOutputStream();
        // Terminal error
        InputStream stderr = process.getErrorStream();
        // Terminal output
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

        // Exit root
        try {
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void disable_pack(int n) {

        String[] paths = {"/system/product/overlay/IconifyComponentIPAS" + n + ".apk", "/system/product/overlay/IconifyComponentIPSUI" + n + ".apk"};

        Process process = null;

        // Get root permission
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Terminal input
        OutputStream stdin = process.getOutputStream();
        // Terminal error
        InputStream stderr = process.getErrorStream();
        // Terminal output
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

        // Exit root
        try {
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void disable_others(int n) {

        Process process = null;

        // Get root permission
        try {
            process = Runtime.getRuntime().exec("su");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Terminal input
        OutputStream stdin = process.getOutputStream();
        // Terminal error
        InputStream stderr = process.getErrorStream();
        // Terminal output
        InputStream stdout = process.getInputStream();

        for (int i = 1; i <= 5; i++) {
            if (i == n)
                continue;
            else {
                String[] paths = {"/system/product/overlay/IconifyComponentIPAS" + i + ".apk", "/system/product/overlay/IconifyComponentIPSUI" + i + ".apk"};

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
            }
        }

        // Exit root
        try {
            stdin.write(("exit\n").getBytes());
            stdin.flush();
            stdin.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}