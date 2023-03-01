package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIP;
import static com.drdisagree.iconify.utils.helpers.Logger.writeLog;

import android.os.Environment;
import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.AppUtil;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.helpers.BinaryInstaller;
import com.topjohnwu.superuser.Shell;

import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;

public class VolumeCompiler {

    private static final String TAG = "VolumeCompiler";
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zip = ZIP.getAbsolutePath();

    public static boolean buildModule(String overlayName, String packageName) throws IOException {
        preExecute(overlayName, packageName);

        // Create AndroidManifest.xml and build APK using AAPT
        String location = Resources.DATA_DIR + "/SpecialOverlays/" + packageName + '/' + overlayName;
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
        String[] dirs = {"res/drawable-v30/", "res/drawable-v31/", "res/layout-v30/", "res/layout-v31/"};
        for (String res : dirs) {
            ZipUtil.unpack(new File(Resources.COMPANION_COMPILED_DIR + '/' + overlayName + ".zip"), new File(Resources.COMPANION_COMPILED_DIR), name -> name.startsWith(res) ? name : null);
        }

        postExecute(false);
        return false;
    }

    private static void preExecute(String overlayName, String packageName) throws IOException {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/SpecialOverlays").exec();

        // Extract the overlay from assets
        FileUtil.copyAssets("SpecialOverlays/" + packageName + '/' + overlayName);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_DIR + "/module").exec();
        Shell.cmd("mkdir -p " + Resources.COMPANION_TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.COMPANION_COMPILED_DIR).exec();

        // Extract module from assets
        try {
            FileUtil.copyAssets("Module");
            Shell.cmd("cp -a " + Resources.DATA_DIR + "/Module/. " + Resources.TEMP_DIR + "/module").exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated files to module
        if (!hasErroredOut) {
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/drawable-v30/. " + Resources.COMPANION_DRAWABLE_DIR).exec();
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/drawable-v31/. " + Resources.COMPANION_DRAWABLE_DIR).exec();
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/layout-v30/. " + Resources.COMPANION_LAYOUT_DIR).exec();
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/layout-v31/. " + Resources.COMPANION_LAYOUT_DIR).exec();

            // Create flashable module
            Shell.cmd("cd " + Resources.COMPANION_MODULE_DIR + "; " + zip + " -r IconifyCompanion *").exec();

            // Move the module to Iconify folder
            Shell.cmd("mkdir -p " + Environment.getExternalStorageDirectory() + "/Download").exec();
            Shell.cmd("rm " + Environment.getExternalStorageDirectory() + "/Download/IconifyCompanion.zip").exec();
            Shell.cmd("mv " + Resources.COMPANION_MODULE_DIR + "/IconifyCompanion.zip " + Environment.getExternalStorageDirectory() + "/Download/IconifyCompanion.zip").exec();
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Module").exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/SpecialOverlays").exec();
    }

    private static boolean createManifest(String pkgName, String target, String source) {
        Shell.Result result = Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + target + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + pkgName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + pkgName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + pkgName, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean runAapt(String source, String name, String[] splitLocations) {
        StringBuilder aaptCommand = new StringBuilder(aapt + " p -M " + source + "/AndroidManifest.xml -S " + source + "/res -F " + Resources.COMPANION_COMPILED_DIR + '/' + name + ".zip --include-meta-data --auto-add-overlay -f -I /system/framework/framework-res.apk");

        if (splitLocations != null) {
            for (String split : splitLocations) {
                aaptCommand.append(" -I ").append(split);
            }
        }

        Shell.Result result = Shell.cmd(String.valueOf(aaptCommand)).exec();

        if (result.isSuccess()) Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
        else {
            Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());
        }

        return !result.isSuccess();
    }
}