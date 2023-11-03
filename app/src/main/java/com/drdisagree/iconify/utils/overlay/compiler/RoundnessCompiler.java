package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;
import static com.drdisagree.iconify.utils.helper.Logger.writeLog;

import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.BinaryInstaller;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class RoundnessCompiler {

    private static final String TAG = RoundnessCompiler.class.getSimpleName();
    private static final String[] mPackages = {FRAMEWORK_PACKAGE, SYSTEMUI_PACKAGE};
    private static final String[] mOverlayName = {"CR1", "CR2"};
    private static boolean mForce = false;

    public static boolean buildOverlay(String[] resources, boolean force) throws IOException {
        mForce = force;

        preExecute();

        for (int i = 0; i < 2; i++) {
            // Create AndroidManifest.xml
            if (OverlayCompiler.createManifest(mOverlayName[i], mPackages[i], Resources.DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i])) {
                Log.e(TAG, "Failed to create Manifest for " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Write resources
            if (writeResources(Resources.DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i], resources[i])) {
                Log.e(TAG, "Failed to write resource for " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(Resources.DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i], mPackages[i])) {
                Log.e(TAG, "Failed to build " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // ZipAlign the APK
            if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + mOverlayName[i] + "-unsigned-unaligned.apk")) {
                Log.e(TAG, "Failed to align " + mOverlayName[i] + "-unsigned-unaligned.apk! Exiting...");
                postExecute(true);
                return true;
            }

            // Sign the APK
            if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/" + mOverlayName[i] + "-unsigned.apk")) {
                Log.e(TAG, "Failed to sign " + mOverlayName[i] + "-unsigned.apk! Exiting...");
                postExecute(true);
                return true;
            }
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
        for (int i = 0; i < 2; i++)
            FileUtil.copyAssets("Overlays/" + mPackages[i] + "/" + mOverlayName[i]);

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
            String[] overlayNames = new String[mOverlayName.length];
            for (int i = 1; i <= mOverlayName.length; i++) {
                overlayNames[i - 1] = "IconifyComponentCR" + i + ".overlay";
            }
            OverlayUtil.disableOverlays(overlayNames);
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (String overlayName : mOverlayName) {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponent" + overlayName + ".apk").exec();
                RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponent" + overlayName + ".apk");

                if (mForce) {
                    // Move to files dir and install
                    Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk").exec();
                    RootUtil.setPermissions(644, Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk");
                    Shell.cmd("pm install -r " + Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk").exec();
                    Shell.cmd("rm -rf " + Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk").exec();
                }
            }

            if (mForce) {
                // Move to system overlay dir
                SystemUtil.mountRW();
                for (String overlayName : mOverlayName) {
                    Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponent" + overlayName + ".apk").exec();
                    RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponent" + overlayName + ".apk");
                }
                SystemUtil.mountRO();

                // Enable the overlays
                String[] overlayNames = new String[mOverlayName.length];
                for (int i = 1; i <= mOverlayName.length; i++) {
                    overlayNames[i - 1] = "IconifyComponentCR" + i + ".overlay";
                }
                OverlayUtil.enableOverlays(overlayNames);
            } else {
                for (String overlayName : mOverlayName) {
                    Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.BACKUP_DIR + "/IconifyComponent" + overlayName + ".apk").exec();
                }
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();
    }

    private static boolean writeResources(String source, String resources) {
        Shell.Result result = Shell.cmd("rm -rf " + source + "/res/values/dimens.xml", "printf '" + resources + "' > " + source + "/res/values/dimens.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - WriteResources", "Successfully written resources for UiRoundness");
        else {
            Log.e(TAG + " - WriteResources", "Failed to write resources for UiRoundness" + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - WriteResources", "Failed to write resources for UiRoundness", result.getOut());
        }

        return !result.isSuccess();
    }
}
