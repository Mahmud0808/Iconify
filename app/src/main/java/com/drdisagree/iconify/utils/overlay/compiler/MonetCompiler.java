package com.drdisagree.iconify.utils.overlay.compiler;

import android.util.Log;

import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.BinaryInstaller;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class MonetCompiler {

    private static final String TAG = MonetCompiler.class.getSimpleName();
    private static boolean mForce = false;

    public static boolean buildOverlay(String[] resources, boolean force) throws IOException {
        mForce = force;

        preExecute();

        // Create AndroidManifest.xml
        String overlay_name = "ME";

        if (createManifestResource(overlay_name, Const.FRAMEWORK_PACKAGE, Resources.DATA_DIR + "/Overlays/android/ME", resources)) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
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

    @SuppressWarnings("SameParameterValue")
    private static boolean createManifestResource(String overlayName, String targetPackage, String source, String[] resources) {
        Shell.cmd("rm -rf " + source + "/res/values/colors.xml", "printf '" + resources[0] + "' > " + source + "/res/values/colors.xml;", "rm -rf " + source + "/res/values-night/colors.xml", "printf '" + resources[1] + "' > " + source + "/res/values-night/colors.xml;").exec();

        return OverlayCompiler.createManifest(overlayName, targetPackage, source);
    }
}
