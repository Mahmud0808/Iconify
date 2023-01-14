package com.drdisagree.iconify.utils;

import android.content.res.Configuration;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

public class SystemUtil {
    public static boolean isDarkMode() {
        return (Iconify.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void restartSystemUI() {
        Shell.cmd("killall com.android.systemui").exec();
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
}
