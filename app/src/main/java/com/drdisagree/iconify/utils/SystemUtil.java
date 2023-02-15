package com.drdisagree.iconify.utils;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.References.BOOT_ID;
import static com.drdisagree.iconify.common.References.DEVICE_BOOT_ID_CMD;
import static com.drdisagree.iconify.common.References.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.References.VER_CODE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.TypedValue;

import androidx.annotation.AttrRes;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.Prefs;
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
        Shell.cmd("killall " + SYSTEMUI_PACKAGE).exec();
    }

    public static void restartDevice() {
        Shell.cmd("su -c 'svc power reboot'").exec();
    }

    public static void disableBlur() {
        Shell.cmd("mv " + References.MODULE_DIR + "/common/system.prop " + References.MODULE_DIR + "/common/system.txt; grep -v \"ro.surface_flinger.supports_background_blur\" " + References.MODULE_DIR + "/common/system.txt > " + References.MODULE_DIR + "/common/system.txt.tmp && mv " + References.MODULE_DIR + "/common/system.txt.tmp " + References.MODULE_DIR + "/common/system.prop; rm -rf " + References.MODULE_DIR + "/common/system.txt; rm -rf " + References.MODULE_DIR + "/common/system.txt.tmp").submit();
        Shell.cmd("grep -v \"ro.surface_flinger.supports_background_blur\" " + References.MODULE_DIR + "/service.sh > " + References.MODULE_DIR + "/service.sh.tmp && mv " + References.MODULE_DIR + "/service.sh.tmp " + References.MODULE_DIR + "/service.sh").submit();
    }

    public static void enableBlur() {
        disableBlur();

        String blur_cmd1 = "ro.surface_flinger.supports_background_blur=1";
        String blur_cmd2 = "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger";

        Shell.cmd("echo \"" + blur_cmd1 + "\" >> " + References.MODULE_DIR + "/common/system.prop").submit();
        Shell.cmd("sed '/*}/a " + blur_cmd2 + "' " + References.MODULE_DIR + "/service.sh > " + References.MODULE_DIR + "/service.sh.tmp && mv " + References.MODULE_DIR + "/service.sh.tmp " + References.MODULE_DIR + "/service.sh").submit();
    }

    public static void mountRW() {
        Shell.cmd("mount -o remount,rw /").exec();
    }

    public static void mountRO() {
        Shell.cmd("mount -o remount,ro /").exec();
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

    public static boolean isBlurEnabled() {
        List<String> outs = Shell.cmd("if grep -q \"ro.surface_flinger.supports_background_blur=1\" " + References.MODULE_DIR + "/common/system.prop; then echo yes; else echo no; fi").exec().getOut();
        return Objects.equals(outs.get(0), "yes");
    }

    // Save unique id of each boot
    public static void getBootId() {
        Prefs.putString(BOOT_ID, Shell.cmd(DEVICE_BOOT_ID_CMD).exec().getOut().toString());
    }

    public static void getVersionCode() {
        Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE);
    }

    public static void getStoragePermission(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        Uri uri = Uri.fromParts("package", Iconify.getAppContext().getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    public static boolean isNightMode() {
        return (Iconify.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static int getColorResCompat(Context context, @AttrRes int id) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, false);
        @SuppressLint("Recycle") TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{id});
        return arr.getColor(0, -1);
    }

    private boolean getIsDark() {
        return (mContext.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void doubleToggleDarkTheme() {
        boolean isDark = (Iconify.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
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
