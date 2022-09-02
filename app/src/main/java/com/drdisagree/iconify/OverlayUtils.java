package com.drdisagree.iconify;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class OverlayUtils {

    public static final int BYTE_ACCESS_RATE = 8192;
    public static final String MAGISK_DIR = RootUtil.getMagiskDirectory();
    public static final String OVERLAY_DIR = "/data/adb/modules/Iconify/system/product/overlay";

    static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list").exec().getOut();
    }

    static boolean isOverlayEnabled(List<String> overlays, String pkgName) {
        for (String line : overlays) {
            if (line.startsWith("[x]") && line.contains(pkgName))
                return true;
        }
        return false;
    }

    static boolean isOverlayDisabled(List<String> overlays, String pkgName) {
        for (String line : overlays) {
            if (line.startsWith("[ ]") && line.contains(pkgName))
                return true;
        }
        return false;
    }

    static boolean isOverlayInstalled(List<String> overlays, String pkgName) {
        for (String line : overlays) {
            if (line.contains(pkgName))
                return true;
        }
        return false;
    }

    static void enableOverlay(List<String> overlays, String pkgName) {
        if (isOverlayEnabled(overlays, pkgName))
            disableOverlay(pkgName);
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName +" highest").exec();
    }

    static void disableOverlay(String pkgName) {
        Shell.cmd("cmd overlay disable --user current " + pkgName).exec();
    }

    static boolean overlayExists() {
        File f = new File("/system/product/overlay/IconifyComponentIPAS1.apk");
        return (f.exists() && !f.isDirectory());
    }

    static void installModule(Context context) {
        if (MAGISK_DIR.equals("/"))
            return;

        Log.e("ModuleCheck", "Magisk module does not exist, creating!");
        // Clean temporary directory
        Shell.cmd("mkdir -p " + MAGISK_DIR).exec();
        Shell.cmd("printf 'id=Iconify\nname=Iconify\nversion=" + BuildConfig.VERSION_NAME + "\nversionCode=" + BuildConfig.VERSION_CODE + "\nauthor=@DrDisagree\ndescription=Systemless module for Iconify.\n' > " + MAGISK_DIR + "/module.prop").exec();
        Shell.cmd("touch " + MAGISK_DIR + "/auto_mount").exec();
        Shell.cmd("mkdir -p " + MAGISK_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + MAGISK_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + MAGISK_DIR + "/system/product/overlay").exec();
        Log.e("ModuleCheck", "Magisk module successfully created!");

        copyAssets(context);
    }

    static void handleModule(final Context context) throws IOException {
        if (moduleExists()) {
            com.topjohnwu.superuser.Shell.cmd("rm -rf " + MAGISK_DIR).exec();
        }
        installModule(context);
    }

    static boolean moduleExists() {
        List<String> lines = Shell.cmd("test -d " + MAGISK_DIR + " && echo '1'").exec().getOut();
        for (String line: lines) {
            if (line.contains("1"))
                return true;
        }
        return false;
    }

    static void copyAssets(Context context) {

        String[] overlays = new String[0];
        try {
            overlays = context.getAssets().list("Component");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data_dir = context.getFilesDir().toString();
        // Clean temporary directory
        Shell.cmd("rm -rf " + data_dir).exec();
        File devicefile = new File(data_dir + "/Component/");
        devicefile.mkdirs();

        for (String overlay : overlays) {
            File file = new File(data_dir + "/Component/" + overlay);
            if (!file.exists()) {
                try {
                    copyFileTo(context, "Component/" + overlay, data_dir + "/Component/" + overlay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Shell.cmd("cp -f " + data_dir + "/Component/" + overlay + " " + OVERLAY_DIR + '/' + overlay).exec();
        }
        RootUtil.setPermissionsRecursively(644, OVERLAY_DIR + '/');
        // Clean temporary directory
        Shell.cmd("rm -rf " + data_dir + "/Component").exec();
    }

    static void copyFileTo(Context c, String source, String destination) throws IOException {

        InputStream myInput;
        OutputStream myOutput = new FileOutputStream(destination);
        myInput = c.getAssets().open(source);

        byte[] buffer = new byte[BYTE_ACCESS_RATE];
        int length = myInput.read(buffer);
        while (length > 0) {
            myOutput.write(buffer, 0, length);
            length = myInput.read(buffer);
        }

        myOutput.flush();
        myInput.close();
        myOutput.close();
    }
}
