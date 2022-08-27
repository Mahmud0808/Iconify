package com.drdisagree.iconify;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

public class IconInstaller {
    private DataOutputStream os;

    void install_icon(int n) {
        int selected = n;
        disable_others(n);
        enable_pack(n);
    }

    private void disable_others(int n) {
        int exclude = n;
        for (int i = 1; i <= 5; i++) {
            if (i == n)
                continue;
            else
                disable_pack(n);
        }
    }

    private void enable_pack(int n) {
        int i = n;
        Process process = null;
        String[] paths = {"/system/product/overlay/AnotherThemeBattery" + i + ".apk", "/system/product/overlay/AnotherThemeBatteryColor" + i + ".apk", "/system/product/overlay/AnotherThemeBatteryPadding" + i + ".apk", "/system/product/overlay/AnotherThemeBatterySize" + i + ".apk", "/system/product/overlay/AnotherThemeIcons1" + i + ".apk", "/system/product/overlay/AnotherThemeIcons2" + i + ".apk", "/system/product/overlay/AnotherThemeNavbar" + i + ".apk", "/system/product/overlay/AnotherThemeSignal1" + i + ".apk", "/system/product/overlay/AnotherThemeSignal2" + i + ".apk", "/system/product/overlay/AnotherThemeSignalType" + i + ".apk", "/system/product/overlay/AnotherThemeStatusbar" + i + ".apk", };

        for (String path : paths) {
            if (new File(path).exists()) {

                String path2 = path.replaceAll("apk","overlay");

                try {
                    process = Runtime.getRuntime().exec("cmd overlay enable --user current " + path2);

                    os = new DataOutputStream(process.getOutputStream());
                    os.writeBytes("echo \"Enabling overlay.\" >/system/sd/temporary.txt\n");
                    os.flush();
                } catch (Throwable t) {
                    t.printStackTrace();
                } finally {
                    try {
                        os.writeBytes("exit\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void disable_pack(int n) {
    }
}
