package com.drdisagree.iconify.utils;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BOOT_ID;
import static com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_OVERLAY_STATE;
import static com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_PACKAGE_NAME;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_BEHAVIOR_EXT;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;
import static com.drdisagree.iconify.common.References.DEVICE_BOOT_ID_CMD;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Insets;
import android.net.Uri;
import android.os.Environment;
import android.view.WindowInsets;
import android.view.WindowMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RPrefs;
import com.topjohnwu.superuser.Shell;

public class SystemUtil {

    private static final int CLICK_DELAY_TIME = 5000;
    static boolean darkSwitching = false;
    private static long lastClickTime = 0;
    private static final String blur_cmd0 = "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger";
    private static final String blur_cmd1 = "ro.sf.blurs_are_expensive=1";
    private static final String blur_cmd2 = "ro.surface_flinger.supports_background_blur=1";
    private static final String blur_cmd3 = "persist.sys.sf.disable_blurs=0";
    private static final String blur_cmd4 = "persist.sysui.disableBlur=false";
    private static final String blur_cmd5 = "ro.config.avoid_gfx_accel=false";

    public static boolean isDarkMode() {
        return (Iconify.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void restartSystemUI() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            Shell.cmd("killall " + SYSTEMUI_PACKAGE).submit();
        } else {
            Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.toast_try_again_later), Toast.LENGTH_SHORT).show();
        }
    }

    public static void forceReloadUI() {
        boolean state = RPrefs.getBoolean(FORCE_RELOAD_OVERLAY_STATE, false);
        String pkgName = FORCE_RELOAD_PACKAGE_NAME;
        Shell.cmd("cmd overlay " + (state ? "disable" : "enable") + " --user current " + pkgName + "; cmd overlay " + (state ? "enable" : "disable") + " --user current " + pkgName).submit();
    }

    public static void handleSystemUIRestart() {
        int selectedBehavior = Prefs.getInt(RESTART_SYSUI_BEHAVIOR_EXT, 0);

        if (selectedBehavior == 0) {
            restartSystemUI();
        } else if (selectedBehavior == 1) {
            forceReloadUI();
        } else {
            Toast.makeText(Iconify.getAppContext(), Iconify.getAppContext().getResources().getString(R.string.settings_systemui_restart_required), Toast.LENGTH_SHORT).show();
        }
    }

    public static void restartDevice() {
        Shell.cmd("am start -a android.intent.action.REBOOT").exec();
    }

    public static void disableBlur(boolean force) {
        Shell.cmd(
                !force ?
                        "mv " + Resources.MODULE_DIR +
                                "/system.prop " +
                                Resources.MODULE_DIR +
                                "/system.txt; " +
                                "grep -vE \"" +
                                blur_cmd1 + "|" +
                                blur_cmd2 + "|" +
                                blur_cmd3 + "|" +
                                blur_cmd4 + "|" +
                                blur_cmd5 + "\" " +
                                Resources.MODULE_DIR +
                                "/system.txt > " +
                                Resources.MODULE_DIR +
                                "/system.txt.tmp; " +
                                "rm -rf " +
                                Resources.MODULE_DIR +
                                "/system.prop; " +
                                "mv " + Resources.MODULE_DIR +
                                "/system.txt.tmp " +
                                Resources.MODULE_DIR +
                                "/system.prop; " +
                                "rm -rf " + Resources.MODULE_DIR +
                                "/system.txt; " +
                                "rm -rf " + Resources.MODULE_DIR +
                                "/system.txt.tmp" :
                        ":", // do nothing
                "grep -v \"ro.surface_flinger.supports_background_blur\" " +
                        Resources.MODULE_DIR + "/service.sh > " +
                        Resources.MODULE_DIR + "/service.sh.tmp && mv " +
                        Resources.MODULE_DIR + "/service.sh.tmp " +
                        Resources.MODULE_DIR + "/service.sh"
        ).submit();
    }

    public static void enableBlur(boolean force) {
        disableBlur(false);

        Shell.cmd(
                "echo \"" +
                        blur_cmd1 + "\n" +
                        blur_cmd2 + "\n" +
                        blur_cmd3 + "\n" +
                        blur_cmd4 + "\n" +
                        blur_cmd5 + "\" >> " +
                        Resources.MODULE_DIR +
                        "/system.prop",
                force ?
                        "sed '/*}/a " +
                                blur_cmd0 + "' " +
                                Resources.MODULE_DIR +
                                "/service.sh > " +
                                Resources.MODULE_DIR +
                                "/service.sh.tmp && mv " +
                                Resources.MODULE_DIR +
                                "/service.sh.tmp " +
                                Resources.MODULE_DIR +
                                "/service.sh" :
                        ":" // do nothing
        ).submit();
    }

    public static void mountRW() {
        Shell.cmd("mount -o remount,rw /").exec();
        if (RootUtil.isKSUInstalled()) {
            Shell.cmd("-mm -c magic_remount_rw").exec();
        }
    }

    public static void mountRO() {
        Shell.cmd("mount -o remount,ro /").exec();
        if (RootUtil.isKSUInstalled()) {
            Shell.cmd("-mm -c magic_remount_ro").exec();
        }
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

    public static boolean isBlurEnabled(boolean force) {
        return Shell.cmd(
                "if grep -q \"ro.surface_flinger.supports_background_blur\" " +
                        Resources.MODULE_DIR +
                        (force ?
                                "/service.sh;" :
                                "/system.prop;"
                        ) +
                        " then echo yes; else echo no; fi"
        ).exec().getOut().get(0).equals("yes");
    }

    // Save unique id of each boot
    public static void getBootId() {
        Prefs.putString(BOOT_ID, Shell.cmd(DEVICE_BOOT_ID_CMD).exec().getOut().toString());
    }

    public static void saveVersionCode() {
        Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE);
    }

    public static int getSavedVersionCode() {
        return Prefs.getInt(VER_CODE, -1);
    }

    public static boolean hasStoragePermission() {
        return Environment.isExternalStorageManager() || Environment.isExternalStorageLegacy();
    }

    public static void requestStoragePermission(Context context) {
        Intent intent = new Intent();
        intent.setAction(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
        ((Activity) context).startActivityForResult(intent, 0);

        ActivityCompat.requestPermissions((Activity) context, new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
        }, 0);
    }

    public static void enableRestartSystemuiAfterBoot() {
        disableRestartSystemuiAfterBoot();
        Shell.cmd("sed '/^sleep.6/i killall " + SYSTEMUI_PACKAGE + "' " + Resources.MODULE_DIR + "/service.sh > " + Resources.MODULE_DIR + "/service.sh.tmp && mv " + Resources.MODULE_DIR + "/service.sh.tmp " + Resources.MODULE_DIR + "/service.sh").submit();
    }

    public static void disableRestartSystemuiAfterBoot() {
        Shell.cmd("grep -v \"killall " + SYSTEMUI_PACKAGE + "\" " + Resources.MODULE_DIR + "/service.sh > " + Resources.MODULE_DIR + "/service.sh.tmp && mv " + Resources.MODULE_DIR + "/service.sh.tmp " + Resources.MODULE_DIR + "/service.sh").submit();
    }

    public static int getScreenWidth(@NonNull Activity activity) {
        WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
        Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        return windowMetrics.getBounds().width() - (insets.left + insets.right);
    }

    public static int getScreenHeight(@NonNull Activity activity) {
        WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
        Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        return windowMetrics.getBounds().height() - (insets.top + insets.bottom);
    }
}
