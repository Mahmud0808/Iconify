package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.utils.helper.Logger.writeLog;

import android.os.Build;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.BinaryInstaller;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MonetCompiler {

    private static final String TAG = MonetCompiler.class.getSimpleName();
    private static boolean mForce = false;

    public static boolean buildOverlay(String[] resources, boolean force) throws IOException {
        mForce = force;

        preExecute();

        // Create AndroidManifest.xml
        String overlay_name = "ME";

        if (createManifest(overlay_name, Resources.DATA_DIR + "/Overlays/android/ME")) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Write color resources
        if (writeResources(Resources.DATA_DIR + "/Overlays/android/ME", resources)) {
            Log.e(TAG, "Failed to write resource for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (OverlayCompiler.runAapt(Resources.DATA_DIR + "/Overlays/android/ME")) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/ME-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align ME-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/ME-unsigned.apk")) {
            Log.e(TAG, "Failed to sign ME-unsigned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();

        // Extract overlay from assets
        FileUtil.copyAssets("Overlays/android/ME");

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
        if (!mForce) {
            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec();
        }

        if (mForce) {
            // Disable the overlay in case it is already enabled
            OverlayUtil.disableOverlay("IconifyComponentME.overlay");
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.OVERLAY_DIR + "/IconifyComponentME.apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentME.apk");

            if (mForce) {
                // Move to files dir and install
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.DATA_DIR + "/IconifyComponentME.apk").exec();
                RootUtil.setPermissions(644, Resources.DATA_DIR + "/IconifyComponentME.apk");
                Shell.cmd("pm install -r " + Resources.DATA_DIR + "/IconifyComponentME.apk").exec();
                Shell.cmd("rm -rf " + Resources.DATA_DIR + "/IconifyComponentME.apk").exec();

                // Move to system overlay dir
                SystemUtil.mountRW();
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponentME.apk").exec();
                RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentME.apk");
                SystemUtil.mountRO();

                // Enable the overlays
                OverlayUtil.enableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay");
            } else {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.BACKUP_DIR + "/IconifyComponentME.apk").exec();
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();
    }

    private static boolean createManifest(String pkgName, String source) {
        String category = OverlayUtil.getCategory(pkgName);
        List<String> module = new ArrayList<>();
        module.add("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        module.add("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">");
        module.add("\\t<uses-sdk android:minSdkVersion=\"" + BuildConfig.MIN_SDK_VERSION + "\" android:targetSdkVersion=\"" + Build.VERSION.SDK_INT + "\" />");
        module.add("\\t<overlay android:category=\"" + category + "\" android:priority=\"1\" android:targetPackage=\"" + Const.FRAMEWORK_PACKAGE + "\" />");
        module.add("\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />");
        module.add("</manifest>' > " + source + "/AndroidManifest.xml;");

        Shell.Result result = Shell.cmd(String.join("\\n", module)).exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + pkgName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + pkgName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + pkgName, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean writeResources(String source, String[] resources) {
        Shell.Result result = Shell.cmd("rm -rf " + source + "/res/values/colors.xml", "printf '" + resources[0] + "' > " + source + "/res/values/colors.xml;", "rm -rf " + source + "/res/values-night/colors.xml", "printf '" + resources[1] + "' > " + source + "/res/values-night/colors.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - WriteResources", "Successfully written resources for MonetEngine");
        else {
            Log.e(TAG + " - WriteResources", "Failed to write resources for MonetEngine" + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - WriteResources", "Failed to write resources for MonetEngine", result.getOut());
        }

        return !result.isSuccess();
    }
}
