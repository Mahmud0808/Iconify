package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.utils.helpers.Logger.writeLog;

import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helpers.BinaryInstaller;
import com.topjohnwu.superuser.Shell;

import java.io.IOException;

public class SwitchCompiler {

    private static final String TAG = "SwitchCompiler";
    private static final String[] mPackage = new String[]{"com.android.settings", "com.android.systemui"};
    private static final String[] mOverlayName = new String[]{"SWITCH1", "SWITCH2"};
    private static int mStyle = 0;
    private static boolean mEnable = false;

    public static boolean buildOverlay(int style, boolean enable) throws IOException {
        mStyle = style;
        mEnable = enable;

        preExecute();
        moveOverlaysToCache();

        for (int i = 0; i < mOverlayName.length; i++) {
            // Create AndroidManifest.xml
            if (createManifest(mOverlayName[i], mPackage[i], Resources.TEMP_CACHE_DIR + "/" + mPackage[i] + "/" + mOverlayName[i])) {
                Log.e(TAG, "Failed to create Manifest for " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(Resources.TEMP_CACHE_DIR + "/" + mPackage[i] + "/" + mOverlayName[i])) {
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
        for (String packageName : mPackage)
            FileUtil.copyAssets("CompileOnDemand/" + packageName + "/SWITCH" + mStyle);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
        for (String packageName : mPackage)
            Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + packageName + "/").exec();

        // Disable the overlay in case it is already enabled
        if (mEnable) {
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
                Shell.cmd("pm install -r " + Resources.OVERLAY_DIR + "/IconifyComponent" + overlayName + ".apk").exec();
            }

            SystemUtil.mountRW();
            for (String overlayName : mOverlayName) {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + "/system/product/overlay/IconifyComponent" + overlayName + ".apk").exec();
                RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponent" + overlayName + ".apk");
            }
            SystemUtil.mountRO();

            // Enable the overlays
            if (mEnable) {
                String[] overlayNames = new String[mOverlayName.length];
                for (int i = 1; i <= mOverlayName.length; i++) {
                    overlayNames[i - 1] = "IconifyComponentSWITCH" + i + ".overlay";
                }
                OverlayUtil.enableOverlays(overlayNames);
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        for (int i = 0; i < mOverlayName.length; i++) {
            Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + mPackage[i] + "/" + "SWITCH" + mStyle + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackage[i] + "/" + mOverlayName[i] + "\"").exec().isSuccess();
        }
    }

    private static boolean createManifest(String overlayName, String mPackage, String source) {
        Shell.Result result = Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + overlayName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + mPackage + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + overlayName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + overlayName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + overlayName, result.getOut());
        }

        return !result.isSuccess();
    }
}
