package com.drdisagree.iconify.utils;

import static com.drdisagree.iconify.common.References.FORCE_APPLY_XPOSED_CHOICE;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.config.Prefs;
import com.topjohnwu.superuser.Shell;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HelperUtil {

    public static void backupFiles() {
        // Create backup directory
        Shell.cmd("rm -rf " + References.BACKUP_DIR, "mkdir -p " + References.BACKUP_DIR).exec();

        backupFile(References.MODULE_DIR + "/common/system.prop");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentME.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentCR.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentQSTH.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentSIP1.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentSIP2.apk");
        backupFile(References.OVERLAY_DIR + "/IconifyComponentSIP3.apk");
    }

    public static void restoreFiles() {
        restoreFile("system.prop", References.MODULE_DIR + "/common");
        restoreFile("IconifyComponentME.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentCR.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentQSTH.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP1.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP2.apk", References.OVERLAY_DIR);
        restoreFile("IconifyComponentSIP3.apk", References.OVERLAY_DIR);
        restoreBlurSettings();

        // Remove backup directory
        Shell.cmd("rm -rf " + References.BACKUP_DIR).exec();
    }

    private static boolean backupExists(String fileName) {
        return RootUtil.fileExists(References.BACKUP_DIR + "/" + fileName);
    }

    private static void backupFile(String source) {
        if (RootUtil.fileExists(source))
            Shell.cmd("cp -rf " + source + " " + References.BACKUP_DIR + "/").exec();
    }

    private static void restoreFile(String fileName, String dest) {
        if (HelperUtil.backupExists(fileName)) {
            Shell.cmd("rm -rf " + dest + "/" + fileName).exec();
            Shell.cmd("cp -rf " + References.BACKUP_DIR + "/" + fileName + " " + dest + "/").exec();
            RootUtil.setPermissions(644, dest + "/" + fileName);
        }
    }

    private static void restoreBlurSettings() {
        if (SystemUtil.isBlurEnabled()) {
            SystemUtil.enableBlur();
        }
    }

    public static void forceApply() {
        if (Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == 0)
            SystemUtil.doubleToggleDarkMode();
        else if (Prefs.getInt(FORCE_APPLY_XPOSED_CHOICE, 0) == 1)
            SystemUtil.restartSystemUI();
    }

    public static void writeLog(String tag, String header, List<String> details) {
        StringBuilder log = getDeviceInfo();

        log.append("error: ").append(header).append('\n');
        log.append('\n');
        log.append(tag).append(":\n");

        for (String line : details) {
            log.append('\t').append(line).append('\n');
        }

        writeLogToFile(log);
    }

    public static void writeLog(String tag, String header, String details) {
        StringBuilder log = getDeviceInfo();

        log.append("error: ").append(header).append('\n');
        log.append('\n');
        log.append(tag).append(":\n");
        log.append(details).append('\n');

        writeLogToFile(log);
    }

    private static StringBuilder getDeviceInfo() {
        StringBuilder info = new StringBuilder("Iconify bug report ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        info.append(sdf.format(new Date())).append('\n');
        info.append("version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n");
        info.append("build.brand: ").append(Build.BRAND).append('\n');
        info.append("build.device: ").append(Build.DEVICE).append('\n');
        info.append("build.display: ").append(Build.DISPLAY).append('\n');
        info.append("build.fingerprint: ").append(Build.FINGERPRINT).append('\n');
        info.append("build.hardware: ").append(Build.HARDWARE).append('\n');
        info.append("build.id: ").append(Build.ID).append('\n');
        info.append("build.manufacturer: ").append(Build.MANUFACTURER).append('\n');
        info.append("build.model: ").append(Build.MODEL).append('\n');
        info.append("build.product: ").append(Build.PRODUCT).append('\n');
        info.append("build.type: ").append(Build.TYPE).append('\n');
        info.append("version.codename: ").append(Build.VERSION.CODENAME).append('\n');
        info.append("version.release: ").append(Build.VERSION.RELEASE).append('\n');
        info.append("version.sdk_int: ").append(Build.VERSION.SDK_INT).append('\n');
        info.append('\n');

        return info;
    }

    private static void writeLogToFile(StringBuilder log) {
        try {
            Shell.cmd("mkdir -p " + Environment.getExternalStorageDirectory() + "/Download/").exec();
            SimpleDateFormat dF = new SimpleDateFormat("dd-MM-yy_HH_mm_ss", Locale.getDefault());
            String filename = "iconify_logcat_" + dF.format(new Date()) + ".txt";
            Shell.cmd("printf '" + log + "' > " + Environment.getExternalStorageDirectory() + "/Download/" + filename).exec();
        } catch (Exception e) {
            Log.e("HelperUtil", "Failed to write logs.\n" + e);
        }
    }
}
