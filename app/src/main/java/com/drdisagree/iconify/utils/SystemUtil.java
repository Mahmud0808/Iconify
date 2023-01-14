package com.drdisagree.iconify.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;

import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

public class SystemUtil {

    @SuppressLint("StaticFieldLeak")
    static SystemUtil instance;
    Context mContext;
    static boolean darkSwitching = false;

    public SystemUtil(Context context) {
        mContext = context;
        instance = this;
    }

    public static boolean isDarkMode() {
        if (instance == null) return false;
        return instance.getIsDark();
    }

    private boolean getIsDark() {
        return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void restartSystemUI() {
        Shell.cmd("killall com.android.systemui").exec();
    }

    public static void restartDevice() {
        Shell.cmd("su -c 'svc power reboot'").exec();
    }

    public static void disableBlur() {
        Shell.cmd("mount -o remount,rw /").exec();
        Shell.cmd("grep -v \"ro.sf.blurs_are_expensive\" /system/build.prop > " + References.MODULE_DIR + "/iconify_temp.prop && mv " + References.MODULE_DIR + "/iconify_temp.prop /system/build.prop").exec();
        Shell.cmd("grep -v \"ro.surface_flinger.supports_background_blur\" /system/build.prop > " + References.MODULE_DIR + "/iconify_temp.prop && mv " + References.MODULE_DIR + "/iconify_temp.prop /system/build.prop").exec();
        RootUtil.setPermissionsRecursively(600, "/system/build.prop");
        Shell.cmd("mount -o remount,ro /").exec();
    }

    public static void enableBlur() {
        disableBlur();
        Shell.cmd("mount -o remount,rw /").exec();
        String blur_cmd = "ro.sf.blurs_are_expensive=1\nro.surface_flinger.supports_background_blur=1";
        Shell.cmd("echo \"" + blur_cmd + "\" >> /system/build.prop").exec();
        RootUtil.setPermissionsRecursively(600, "/system/build.prop");
        Shell.cmd("mount -o remount,ro /").exec();
    }

    public static void doubleToggleDarkMode() {
        boolean isDark = isDarkMode();
        new Thread(() -> {
            try {
                while (darkSwitching) {
                    Thread.currentThread().wait(100);
                }
                darkSwitching = true;

                Shell.cmd("cmd uimode night " + (isDark ? "no" : "yes")).exec();
                Thread.sleep(1000);
                Shell.cmd("cmd uimode night " + (isDark ? "yes" : "no")).exec();
                Thread.sleep(500);

                darkSwitching = false;
            } catch (Exception ignored) {
            }
        }).start();
    }
}
