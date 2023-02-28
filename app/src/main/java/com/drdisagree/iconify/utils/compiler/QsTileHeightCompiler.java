package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;
import static com.drdisagree.iconify.utils.helpers.Logger.writeLog;

import android.util.Log;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.apksigner.JarMap;
import com.drdisagree.iconify.utils.apksigner.SignAPK;
import com.drdisagree.iconify.utils.helpers.BinaryInstaller;
import com.topjohnwu.superuser.Shell;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class QsTileHeightCompiler {

    private static final String TAG = "QsTileHeightCompiler";
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();

    public static boolean buildOverlay(String[] resources) throws IOException {
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
        if (runAapt(Resources.DATA_DIR + "/Overlays/com.android.systemui/QSTH", overlay_name)) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/QSTH-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align QSTH-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (apkSigner(Resources.UNSIGNED_DIR + "/QSTH-unsigned.apk")) {
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

        // Disable the overlay in case it is already enabled
        OverlayUtil.disableOverlay("IconifyComponentQSTH.overlay");
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentQSTH.apk " + Resources.OVERLAY_DIR + "/IconifyComponentQSTH.apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentQSTH.apk");

            SystemUtil.mountRW();
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentQSTH.apk " + "/system/product/overlay/IconifyComponentQSTH.apk").exec();
            RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentQSTH.apk");
            SystemUtil.mountRO();

            OverlayUtil.enableOverlay("IconifyComponentQSTH.overlay");
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();
    }

    private static boolean createManifest(String pkgName, String source) {
        Shell.Result result = Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + Const.SYSTEMUI_PACKAGE + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec();

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

    private static boolean runAapt(String source, String name) {
        Shell.Result result = Shell.cmd(aapt + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + Resources.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec();

        if (result.isSuccess()) Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
        else {
            Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean zipAlign(String source) {
        Shell.Result result = Shell.cmd(zipalign + " 4 " + source + ' ' + Resources.UNSIGNED_DIR + "/QSTH-unsigned.apk").exec();

        if (result.isSuccess()) Log.i(TAG + " - ZipAlign", "Successfully zip aligned QSTH");
        else {
            Log.e(TAG + " - ZipAlign", "Failed to zip align QSTH\n" + String.join("\n", result.getOut()));
            writeLog(TAG + " - ZipAlign", "Failed to zip align QSTH", result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean apkSigner(String source) {
        try {
            PrivateKey key = readPrivateKey(Iconify.getAppContext().getAssets().open("Keystore/testkey.pk8"));
            X509Certificate cert = readCertificate(Iconify.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));

            JarMap jar = JarMap.open(new FileInputStream(source), true);
            FileOutputStream out = new FileOutputStream(Resources.SIGNED_DIR + "/IconifyComponentQSTH.apk");

            SignAPK.sign(cert, key, jar, out);

            Log.i(TAG + " - APKSigner", "Successfully signed QSTH");
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            writeLog(TAG + " - APKSigner", "Failed to sign QSTH", e.toString());
            postExecute(true);
            return true;
        }
        return false;
    }
}
