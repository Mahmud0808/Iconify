package com.drdisagree.iconify.utils.overlay.compiler;

import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.BinaryInstaller;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class OnDemandCompiler {

    private static final String TAG = OnDemandCompiler.class.getSimpleName();
    private static String mOverlayName = null;
    private static String mPackage = null;
    private static int mStyle = 0;
    private static boolean mForce = false;

    public static boolean buildOverlay(String overlay_name, int style, String package_name, boolean force) throws IOException {
        mOverlayName = overlay_name;
        mPackage = package_name;
        mStyle = style;
        mForce = force;

        preExecute();
        moveOverlaysToCache();

        // Create AndroidManifest.xml
        if (OverlayCompiler.createManifest(overlay_name, package_name, Resources.TEMP_CACHE_DIR + "/" + package_name + "/" + overlay_name)) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (OverlayCompiler.runAapt(Resources.TEMP_CACHE_DIR + "/" + package_name + "/" + overlay_name)) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + overlay_name + "-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align " + overlay_name + "-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/" + overlay_name + "-unsigned.apk")) {
            Log.e(TAG, "Failed to sign " + overlay_name + "-unsigned.apk! Exiting...");
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
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();

        // Extract overlay from assets
        FileUtil.copyAssets("CompileOnDemand/" + mPackage + "/" + mOverlayName + mStyle);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + mPackage + "/").exec();
        if (!mForce) {
            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec();
        }

        if (mForce) {
            // Disable the overlay in case it is already enabled
            OverlayUtil.disableOverlay("IconifyComponent" + mOverlayName + ".overlay");
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk");

            // Move to files dir and install
            if (mForce) {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
                RootUtil.setPermissions(644, Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk");
                Shell.cmd("pm install -r " + Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
                Shell.cmd("rm -rf " + Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();

                // Move to system overlay dir
                SystemUtil.mountRW();
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
                RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponent" + mOverlayName + ".apk");
                SystemUtil.mountRO();

                // Enable the overlay
                OverlayUtil.enableOverlay("IconifyComponent" + mOverlayName + ".overlay");
            } else {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.BACKUP_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + mPackage + "/" + mOverlayName + mStyle + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName + "\"").exec().isSuccess();
    }
}
