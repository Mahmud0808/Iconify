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

    public static void handleModule() throws IOException {
        if (moduleExists()) {
            Shell.cmd("rm -rf " + MODULE_DIR).exec();
        }
        installModule();
    }

    static void installModule() throws IOException {
        Log.e("ModuleCheck", "Magisk module does not exist, creating!");
        // Clean temporary directory
        Shell.cmd("mkdir -p " + MODULE_DIR).exec();
        Shell.cmd("printf 'id=Iconify\n" +
                "name=Iconify\nversion=" + BuildConfig.VERSION_NAME + "\n" +
                "versionCode=" + BuildConfig.VERSION_CODE + "\n" + "" +
                "author=@DrDisagree\n" +
                "description=Systemless module for Iconify.\n' > " + MODULE_DIR + "/module.prop").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/common").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n' > " + MODULE_DIR + "/post-fs-data.sh").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n' > " + MODULE_DIR + "/service.sh").exec();
        Shell.cmd("touch " + MODULE_DIR + "/common/system.prop").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/tools").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + MODULE_DIR + "/system/product/overlay").exec();
        Log.d("ModuleCheck", "Magisk module successfully created!");

        extractTools();
        CompilerUtil.buildOverlays();
    }

    public static boolean moduleExists() {
        List<String> lines = Shell.cmd("test -d " + MODULE_DIR + " && echo '1'").exec().getOut();
        for (String line : lines) {
            if (line.contains("1"))
                return true;
        }
        return false;
    }

    static void extractTools() {
        try {
            FileUtil.copyAssets("Tools");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Shell.cmd("cp -a " + DATA_DIR + "/Tools/. " + MODULE_DIR + "/tools").exec();
            FileUtil.cleanDir("Tools");
            RootUtil.setPermissionsRecursively(755, MODULE_DIR + "/tools");
        }
    }
}
