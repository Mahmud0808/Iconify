package com.drdisagree.iconify.utils.compilerutil;

import android.os.Environment;
import android.util.Log;

import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.utils.AppUtil;
import com.drdisagree.iconify.utils.FileUtil;
import com.topjohnwu.superuser.Shell;

import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

public class VolumeCompilerUtil {

    private static final String TAG = "VolumeCompilerUtil";
    private static final String aapt = References.TOOLS_DIR + "/libaapt.so";

    public static boolean buildModule(String overlayName, String packageName) throws IOException {
        preExecute(overlayName, packageName);

        // Create AndroidManifest.xml and build APK using AAPT
        String location = References.DATA_DIR + "/SpecialOverlays/" + packageName + '/' + overlayName;
        File dir = new File(location);

        if (dir.isDirectory()) {
            if (createManifest(overlayName, packageName, location)) {
                Log.e(TAG, "Failed to create Manifest for " + overlayName + "! Exiting...");
                postExecute(true);
                return true;
            }

            String[] splitLocations = AppUtil.getSplitLocations(packageName);
            if (runAapt(location, overlayName, splitLocations)) {
                Log.e(TAG, "Failed to build " + overlayName + "! Exiting...");
                postExecute(true);
                return true;
            }
        } else {
            Log.e(TAG, location + "is not a directory! Exiting...");
            return true;
        }

        // Extract the necessary folders from zip
        String[] dirs = {
                "res/drawable-v30/",
                "res/drawable-v31/",
                "res/layout-v30/",
                "res/layout-v31/"
        };
        for (String res : dirs) {
            ZipUtil.unpack(new File(References.COMPANION_COMPILED_DIR + '/' + overlayName + ".zip"), new File(References.COMPANION_COMPILED_DIR), name ->
                    name.startsWith(res) ? name : null);
        }

        postExecute(false);
        return false;
    }

    private static void preExecute(String overlayName, String packageName) throws IOException {
        // Clean data directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/SpecialOverlays").exec();

        // Extract the overlay from assets
        FileUtil.copyAssets("SpecialOverlays/" + packageName + '/' + overlayName);

        // Create temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR + "; mkdir -p " + References.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + References.TEMP_DIR + "/module").exec();
        Shell.cmd("mkdir -p " + References.COMPANION_TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + References.COMPANION_COMPILED_DIR).exec();

        // Extract module from assets
        try {
            FileUtil.copyAssets("Module");
            Shell.cmd("cp -a " + References.DATA_DIR + "/Module/. " + References.TEMP_DIR + "/module").exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated files to module
        if (!hasErroredOut) {
            Shell.cmd("cp -a " + References.COMPANION_COMPILED_DIR + "/res/drawable-v30/. " + References.COMPANION_DRAWABLE_DIR).exec();
            Shell.cmd("cp -a " + References.COMPANION_COMPILED_DIR + "/res/drawable-v31/. " + References.COMPANION_DRAWABLE_DIR).exec();
            Shell.cmd("cp -a " + References.COMPANION_COMPILED_DIR + "/res/layout-v30/. " + References.COMPANION_LAYOUT_DIR).exec();
            Shell.cmd("cp -a " + References.COMPANION_COMPILED_DIR + "/res/layout-v31/. " + References.COMPANION_LAYOUT_DIR).exec();

            // Create flashable module
            Shell.cmd("cd " + References.COMPANION_MODULE_DIR + "; /data/adb/modules/Iconify/tools/zip -r IconifyCompanion *").exec();

            // Move the module to Iconify folder
            Shell.cmd("mkdir -p " + Environment.getExternalStorageDirectory() + "/Download").exec();
            Shell.cmd("rm " + Environment.getExternalStorageDirectory() + "/Download/IconifyCompanion.zip").exec();
            Shell.cmd("mv " + References.COMPANION_MODULE_DIR + "/IconifyCompanion.zip " + Environment.getExternalStorageDirectory() + "/Download/IconifyCompanion.zip").exec();
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/ModuleCompanion").exec();
    }

    private static boolean createManifest(String pkgName, String target, String source) {
        return !Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + target + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec().isSuccess();
    }

    private static boolean runAapt(String source, String name, String[] splitLocations) {
        StringBuilder aaptCommand = new StringBuilder(aapt + " p -M " + source + "/AndroidManifest.xml -S " + source + "/res -F " + References.COMPANION_COMPILED_DIR + '/' + name + ".zip --include-meta-data --auto-add-overlay -f -I /system/framework/framework-res.apk");

        if (splitLocations != null) {
            for (String split : splitLocations) {
                aaptCommand.append(" -I ").append(split);
            }
        }

        return !Shell.cmd(String.valueOf(aaptCommand)).exec().isSuccess();
    }
}