package com.drdisagree.iconify.utils.helpers;

import static com.drdisagree.iconify.common.Resources.DOC_DIR;
import static com.drdisagree.iconify.common.Resources.LOG_DIR;

import android.os.Build;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.topjohnwu.superuser.Shell;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Logger {

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
            Files.createDirectories(Paths.get(LOG_DIR));
            Shell.cmd("mkdir -p " + DOC_DIR, "mkdir -p " + LOG_DIR).exec();
            SimpleDateFormat dF = new SimpleDateFormat("dd-MM-yy_HH_mm_ss", Locale.getDefault());
            String filename = "iconify_logcat_" + dF.format(new Date()) + ".txt";
            Shell.cmd("printf \"" + log + "\" > " + LOG_DIR + '/' + filename).exec();
        } catch (Exception e) {
            Log.e("Logger", "Failed to write logs.\n" + e);
        }
    }
}
