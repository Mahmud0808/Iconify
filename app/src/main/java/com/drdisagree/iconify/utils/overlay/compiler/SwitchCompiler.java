package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.common.Const.SETTINGS_PACKAGE;
import static com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE;

import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.BinaryInstaller;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class SwitchCompiler {

    private static final String TAG = SwitchCompiler.class.getSimpleName();
    private static final String[] mPackages = new String[]{SETTINGS_PACKAGE, SYSTEMUI_PACKAGE};
    private static final String[] mOverlayName = new String[]{"SWITCH1", "SWITCH2"};
    private static int mStyle = 0;
    private static boolean mForce = false;

    public static boolean buildOverlay(int style, boolean force) throws IOException {
        mStyle = style;
        mForce = force;

        preExecute();
        moveOverlaysToCache();

        for (int i = 0; i < mOverlayName.length; i++) {
            // Create AndroidManifest.xml
            if (OverlayCompiler.createManifest(mOverlayName[i], mPackages[i], Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + mOverlayName[i])) {
                Log.e(TAG, "Failed to create Manifest for " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + mOverlayName[i], mPackages[i])) {
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
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();

        // Extract overlay from assets
        for (String packageName : mPackages)
            FileUtil.copyAssets("CompileOnDemand/" + packageName + "/SWITCH" + mStyle);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
        for (String packageName : mPackages)
            Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + packageName + "/").exec();
        if (!mForce) {
            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec();
        }

        if (mForce) {
            // Disable the overlay in case it is already enabled
            String[] overlayNames = new String[mOverlayName.length];
            for (int i = 1; i <= mOverlayName.length; i++) {
                overlayNames[i - 1] = "IconifyComponentSWITCH" + i + ".overlay";
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
                    overlayNames[i - 1] = "IconifyComponentSWITCH" + i + ".overlay";
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
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        for (int i = 0; i < mOverlayName.length; i++) {
            Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + mPackages[i] + "/" + "SWITCH" + mStyle + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + mOverlayName[i] + "\"").exec().isSuccess();
        }
    }
}
