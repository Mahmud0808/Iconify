package com.drdisagree.iconify.utils;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.common.Preferences.BOOT_ID;
import static com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_BEHAVIOR;
import static com.drdisagree.iconify.common.Preferences.VER_CODE;
import static com.drdisagree.iconify.common.References.DEVICE_BOOT_ID_CMD;

import android.Manifest;
import android.annotation.SuppressLint;
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
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

public class SystemUtil {

    private static final int CLICK_DELAY_TIME = 8000;
    @SuppressLint("StaticFieldLeak")
    static boolean darkSwitching = false;
    private static long lastClickTime = 0;

    public static boolean isDarkMode() {
        return (Objects.requireNonNull(Iconify.getAppContext()).getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_YES) == Configuration.UI_MODE_NIGHT_YES;
    }

    public static void restartSystemUI() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime >= CLICK_DELAY_TIME) {
            lastClickTime = currentTime;
            try {
                Iconify.mRootServiceProvider.restartSystemUI();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(Iconify.getAppContext(), Objects.requireNonNull(Iconify.getAppContext()).getResources().getString(R.string.toast_try_again_later), Toast.LENGTH_SHORT).show();
        }
    }

    public static void handleSystemUIRestart() {
        if (Prefs.getBoolean(RESTART_SYSUI_BEHAVIOR, true)) {
            restartSystemUI();
        } else {
            Toast.makeText(Iconify.getAppContext(), Objects.requireNonNull(Iconify.getAppContext()).getResources().getString(R.string.settings_systemui_restart_required), Toast.LENGTH_SHORT).show();
        }
    }

    public static void restartDevice() {
        Shell.cmd("am start -a android.intent.action.REBOOT").exec();
    }

    public static void disableBlur() {
        Shell.cmd("mv " + Resources.MODULE_DIR + "/common/system.prop " + Resources.MODULE_DIR + "/common/system.txt; grep -v \"ro.surface_flinger.supports_background_blur\" " + Resources.MODULE_DIR + "/common/system.txt > " + Resources.MODULE_DIR + "/common/system.txt.tmp; rm -rf " + Resources.MODULE_DIR + "/common/system.prop; mv " + Resources.MODULE_DIR + "/common/system.txt.tmp " + Resources.MODULE_DIR + "/common/system.prop; rm -rf " + Resources.MODULE_DIR + "/common/system.txt; rm -rf " + Resources.MODULE_DIR + "/common/system.txt.tmp").submit();
        Shell.cmd("grep -v \"ro.surface_flinger.supports_background_blur\" " + Resources.MODULE_DIR + "/service.sh > " + Resources.MODULE_DIR + "/service.sh.tmp && mv " + Resources.MODULE_DIR + "/service.sh.tmp " + Resources.MODULE_DIR + "/service.sh").submit();
    }

    public static void enableBlur() {
        disableBlur();

        String blur_cmd1 = "ro.surface_flinger.supports_background_blur=1";
        String blur_cmd2 = "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger";

        Shell.cmd("echo \"" + blur_cmd1 + "\" >> " + Resources.MODULE_DIR + "/common/system.prop").submit();
        Shell.cmd("sed '/*}/a " + blur_cmd2 + "' " + Resources.MODULE_DIR + "/service.sh > " + Resources.MODULE_DIR + "/service.sh.tmp && mv " + Resources.MODULE_DIR + "/service.sh.tmp " + Resources.MODULE_DIR + "/service.sh").submit();
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
        List<String> outs = Shell.cmd("if grep -q \"ro.surface_flinger.supports_background_blur=1\" " + Resources.MODULE_DIR + "/common/system.prop; then echo yes; else echo no; fi").exec().getOut();
        return Objects.equals(outs.get(0), "yes");
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
