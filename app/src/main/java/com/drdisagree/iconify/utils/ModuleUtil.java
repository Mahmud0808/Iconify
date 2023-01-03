package com.drdisagree.iconify.utils;

import android.content.Context;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.topjohnwu.superuser.Shell;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class ModuleUtil {

    public static final String MODULE_DIR = "/data/adb/modules/Iconify";
    public static final String DATA_DIR = Iconify.getAppContext().getFilesDir().toString();
    public static final String OVERLAY_DIR = "/data/adb/modules/Iconify/system/product/overlay";

    public static void handleModule(final Context context) throws IOException {
        if (moduleExists()) {
            Shell.cmd("rm -rf " + MODULE_DIR).exec();
        }
        installModule(context);
    }

    static void installModule(Context context) {
        Log.e("ModuleCheck", "Magisk module does not exist, creating!");
        // Clean temporary directory
        Shell.cmd("mkdir -p " + MODULE_DIR).exec();
        Shell.cmd("printf 'id=Iconify\n" +
                "name=Iconify\nversion=" + BuildConfig.VERSION_NAME + "\n" +
                "versionCode=" + BuildConfig.VERSION_CODE + "\n" + "" +
                "author=@DrDisagree\n" +
                "description=Systemless module for Iconify.\n' > " + MODULE_DIR + "/module.prop").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/common").exec();
        Shell.cmd("printf '#!/system/bin/sh\n" +
                "# Do NOT assume where your module will be located.\n" +
                "# ALWAYS use $MODDIR if you need to know where this script\n" +
                "# and module is placed.\n" +
                "# This will make sure your module will still work\n" +
                "# if Magisk change its mount point in the future\n" +
                "MODDIR=${0%%/*}\n" +
                "# This script will be executed in post-fs-data mode' > " + MODULE_DIR + "/common/post-fs-data.sh").exec();
        Shell.cmd("printf '#!/system/bin/sh\n" +
                "# Do NOT assume where your module will be located.\n" +
                "# ALWAYS use $MODDIR if you need to know where this script\n" +
                "# and module is placed.\n" +
                "# This will make sure your module will still work\n" +
                "# if Magisk change its mount point in the future\n" +
                "MODDIR=${0%%/*}\n" +
                "# This script will be executed in late_start service mode\n' > " + MODULE_DIR + "/common/service.sh").exec();
        Shell.cmd("printf '#!/system/bin/sh\n" +
                "# Do NOT assume where your module will be located.\n" +
                "# ALWAYS use $MODDIR if you need to know where this script\n" +
                "# and module is placed.\n" +
                "# This will make sure your module will still work\n" +
                "# if Magisk change its mount point in the future\n" +
                "MODDIR=${0%%/*}\n" +
                "# This script will be executed in late_start service mode\n' > " + MODULE_DIR + "/service.sh").exec();
        Shell.cmd("touch " + MODULE_DIR + "/common/system.prop").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/system/product/overlay").exec();
        Log.e("ModuleCheck", "Magisk module successfully created!");

        copyOverlays(context);
    }

    public static boolean moduleExists() {
        List<String> lines = Shell.cmd("test -d " + MODULE_DIR + " && echo '1'").exec().getOut();
        for (String line : lines) {
            if (line.contains("1"))
                return true;
        }
        return false;
    }

    static void copyOverlays(Context context) {
        try {
            FileUtil.copyAssets("Component");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Shell.cmd("cp -a " + DATA_DIR + "/Component/. " + OVERLAY_DIR + '/').exec();
            FileUtil.cleanDir("Component");
            RootUtil.setPermissionsRecursively(644, OVERLAY_DIR + '/');
        }
    }
}
