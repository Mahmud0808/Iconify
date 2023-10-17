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

public class QsTileHeightCompiler {

    private static final String TAG = QsTileHeightCompiler.class.getSimpleName();
    private static boolean mForce = false;

    public static boolean buildOverlay(String[] resources, boolean force) throws IOException {
        mForce = force;

        preExecute();

        // Create AndroidManifest.xml
        String overlay_name = "QSTH";

        if (createManifest(overlay_name, Resources.DATA_DIR + "/Overlays/com.android.systemui/QSTH")) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Write resources
        if (writeResources(Resources.DATA_DIR + "/Overlays/com.android.systemui/QSTH", resources)) {
            Log.e(TAG, "Failed to write resource for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (OverlayCompiler.runAapt(Resources.DATA_DIR + "/Overlays/com.android.systemui/QSTH")) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/QSTH-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align QSTH-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/QSTH-unsigned.apk")) {
            Log.e(TAG, "Failed to sign QSTH-unsigned.apk! Exiting...");
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
        FileUtil.copyAssets("Overlays/com.android.systemui/QSTH");

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
            OverlayUtil.disableOverlay("IconifyComponentQSTH.overlay");
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentQSTH.apk " + Resources.OVERLAY_DIR + "/IconifyComponentQSTH.apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentQSTH.apk");

            if (mForce) {
                // Move to files dir and install
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentQSTH.apk " + Resources.DATA_DIR + "/IconifyComponentQSTH.apk").exec();
                RootUtil.setPermissions(644, Resources.DATA_DIR + "/IconifyComponentQSTH.apk");
                Shell.cmd("pm install -r " + Resources.DATA_DIR + "/IconifyComponentQSTH.apk").exec();
                Shell.cmd("rm -rf " + Resources.DATA_DIR + "/IconifyComponentQSTH.apk").exec();

                // Move to system overlay dir
                SystemUtil.mountRW();
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentQSTH.apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponentQSTH.apk").exec();
                RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentQSTH.apk");
                SystemUtil.mountRO();

                // Enable the overlay
                OverlayUtil.enableOverlay("IconifyComponentQSTH.overlay");
            } else {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentQSTH.apk " + Resources.BACKUP_DIR + "/IconifyComponentQSTH.apk").exec();
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
        module.add("\\t<overlay android:category=\"" + category + "\" android:priority=\"1\" android:targetPackage=\"" + Const.SYSTEMUI_PACKAGE + "\" />");
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
        Shell.Result result = Shell.cmd("rm -rf " + source + "/res/values/dimens.xml", "printf '" + resources[0] + "' > " + source + "/res/values/dimens.xml;", "rm -rf " + source + "/res/values-land/dimens.xml", "printf '" + resources[1] + "' > " + source + "/res/values-land/dimens.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - WriteResources", "Successfully written resources for QSTH");
        else {
            Log.e(TAG + " - WriteResources", "Failed to write resources for QSTH" + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - WriteResources", "Failed to write resources for QSTH", result.getOut());
        }

        return !result.isSuccess();
    }
}
