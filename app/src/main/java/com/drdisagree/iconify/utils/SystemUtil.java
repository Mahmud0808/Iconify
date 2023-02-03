package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;

import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class SystemUtil {

    @SuppressLint("StaticFieldLeak")
    static SystemUtil instance;
    static boolean darkSwitching = false;
    Context mContext;

    public SystemUtil(Context context) {
        mContext = context;
        instance = this;
    }

    public static boolean isDarkMode() {
        if (instance == null) return false;
        return instance.getIsDark();
    }

    public static void restartSystemUI() {
        Shell.cmd("killall " + SYSTEM_UI_PACKAGE).exec();
    }

    public static void restartDevice() {
        Shell.cmd("su -c 'svc power reboot'").exec();
    }

    public static void disableBlur() {
        mountRW();
        Shell.cmd("grep -v \"ro.sf.blurs_are_expensive\" /system/build.prop > " + References.MODULE_DIR + "/iconify_temp.prop && mv " + References.MODULE_DIR + "/iconify_temp.prop /system/build.prop").exec();
        Shell.cmd("grep -v \"ro.surface_flinger.supports_background_blur\" /system/build.prop > " + References.MODULE_DIR + "/iconify_temp.prop && mv " + References.MODULE_DIR + "/iconify_temp.prop /system/build.prop").exec();
        RootUtil.setPermissionsRecursively(600, "/system/build.prop");
        mountRO();
    }

    public static void enableBlur() {
        disableBlur();

        mountRW();
        String blur_cmd = "ro.sf.blurs_are_expensive=1\nro.surface_flinger.supports_background_blur=1";
        Shell.cmd("echo \"" + blur_cmd + "\" >> /system/build.prop").exec();
        RootUtil.setPermissionsRecursively(600, "/system/build.prop");
        mountRO();
    }

    public static void mountRW() {
        Shell.cmd("mount -o remount,rw /").exec();
    }

    public static void mountRO() {
        Shell.cmd("mount -o remount,ro /").exec();
    }

    public static void disableForcedBlur() {
        disableBlur();

        mountRW();
        Shell.cmd("grep -v \"ro.surface_flinger.supports_background_blur\" " + References.MODULE_DIR + "/service.sh > " + References.MODULE_DIR + "/temp_service.sh && mv " + References.MODULE_DIR + "/temp_service.sh " + References.MODULE_DIR + "/service.sh").exec();
        RootUtil.setPermissionsRecursively(600, "/system/build.prop");
        mountRO();
    }

    public static void forceEnableBlur() {
        disableForcedBlur();

        mountRW();
        String blur_cmd = "ro.sf.blurs_are_expensive=1\nro.surface_flinger.supports_background_blur=1";
        Shell.cmd("echo \"" + blur_cmd + "\" >> /system/build.prop").exec();
        Shell.cmd("sed -i '1 i\\ resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger\n' " + References.MODULE_DIR + "/service.sh").exec();
        RootUtil.setPermissionsRecursively(600, "/system/build.prop");
        mountRO();
    }

    /*
     * From AOSPMods
     * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/utils/SystemUtils.java
     */
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

    public static boolean supportsBlur() {
        List<String> outs = Shell.cmd("getprop ro.sf.blurs_are_expensive", "getprop ro.surface_flinger.supports_background_blur").exec().getOut();
        return (Objects.equals(outs.get(0), "1") && Objects.equals(outs.get(1), "1"));
    }

    public static boolean supportsForcedBlur() {
        List<String> outs = Shell.cmd("getprop ro.surface_flinger.supports_background_blur").exec().getOut();
        return (Objects.equals(outs.get(0), "1"));
    }

    private boolean getIsDark() {
        return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }
}
