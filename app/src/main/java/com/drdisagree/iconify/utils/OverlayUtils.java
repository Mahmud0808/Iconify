package com.drdisagree.iconify.utils;

import android.content.Context;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Objects;

public class OverlayUtils {

    public static final int BYTE_ACCESS_RATE = 8192;
    public static final String MAGISK_DIR = RootUtil.getMagiskDirectory();
    public static final String OVERLAY_DIR = "/data/adb/modules/Iconify/system/product/overlay";

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponent' | sed -E 's/^....//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '^.x..IconifyComponent' | sed -E 's/^.x..//'").exec().getOut();
    }

    public static boolean isOverlayEnabled(List<String> overlays, String pkgName) {
        for (String overlay : overlays) {
            if (overlay.contains(pkgName))
                return true;
        }
        return false;
    }

    public static boolean isOverlayDisabled(List<String> overlays, String pkgName) {
        for (String overlay : overlays) {
            if (overlay.contains(pkgName))
                return false;
        }
        return true;
    }

    static boolean isOverlayInstalled(List<String> enabledOverlays, String pkgName) {
        for (String line : enabledOverlays) {
            if (line.contains(pkgName))
                return true;
        }
        return false;
    }

    public static void enableOverlay(String pkgName) {
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName + " highest").exec();
    }

    public static void disableOverlay(String pkgName) {
        Shell.cmd("cmd overlay disable --user current " + pkgName).exec();
    }

    public static boolean overlayExists() {
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

    public static void handleModule(final Context context) throws IOException {
        if (moduleExists()) {
            com.topjohnwu.superuser.Shell.cmd("rm -rf " + MAGISK_DIR).exec();
        }
        installModule(context);
    }

    public static boolean moduleExists() {
        if (Objects.equals(MAGISK_DIR, "/data/adb/modules/Iconify")) {
            List<String> lines = Shell.cmd("test -d " + MAGISK_DIR + " && echo '1'").exec().getOut();
            for (String line : lines) {
                if (line.contains("1"))
                    return true;
            }
        }
        return false;
    }

    static void copyAssets(Context context) {

        String ext = "apk";
        String[] overlays = new String[0];
        try {
            overlays = context.getAssets().list("Component");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String data_dir = context.getFilesDir().toString();
        // Clean temporary directory
        Shell.cmd("rm -rf " + data_dir).exec();
        File device_file = new File(data_dir + "/Component/");
        device_file.mkdirs();

        for (String overlay : overlays) {
            File file = new File(data_dir + "/Component/" + overlay);
            if (!file.exists()) {
                try {
                    copyFileTo(context, "Component/" + overlay, data_dir + "/Component/" + overlay);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Shell.cmd("cp -f " + data_dir + "/Component/" + overlay + " " + OVERLAY_DIR + '/' + changeExtension(overlay, ext)).exec();
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

    public static String changeExtension(String filename, String extension) {
        if (filename.contains(".")) {
            filename = filename.substring(0, filename.lastIndexOf('.'));
        }
        return filename + "." + extension;
    }
}
