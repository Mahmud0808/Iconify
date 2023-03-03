package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;
import static com.drdisagree.iconify.utils.helpers.Logger.writeLog;

import android.util.Log;

import com.drdisagree.iconify.Iconify;
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

public class OnDemandCompiler {

    private static final String TAG = "OnDemandCompiler";
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();
    private static String mOverlayName = null;
    private static String mPackage = null;
    private static int mStyle = 0;

    public static boolean buildOverlay(String overlay_name, int style, String package_name) throws IOException {
        mOverlayName = overlay_name;
        mPackage = package_name;
        mStyle = style;

        preExecute();
        moveOverlaysToCache();

        // Create AndroidManifest.xml
        if (createManifest(overlay_name, Resources.TEMP_CACHE_DIR + "/" + package_name + "/" + overlay_name)) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (runAapt(Resources.TEMP_CACHE_DIR + "/" + package_name + "/" + overlay_name, overlay_name)) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + overlay_name + "-unsigned-unaligned.apk", overlay_name)) {
            Log.e(TAG, "Failed to align " + overlay_name + "-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (apkSigner(Resources.UNSIGNED_DIR + "/" + overlay_name + "-unsigned.apk", overlay_name)) {
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

        // Disable the overlay in case it is already enabled
        OverlayUtil.disableOverlay("IconifyComponent" + mOverlayName + ".overlay");
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk");

            SystemUtil.mountRW();
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + "/system/product/overlay/IconifyComponent" + mOverlayName + ".apk").exec();
            RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponent" + mOverlayName + ".apk");
            SystemUtil.mountRO();

            OverlayUtil.enableOverlay("IconifyComponent" + mOverlayName + ".overlay");
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + mPackage + "/" + mOverlayName + mStyle + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName + "\"").exec().isSuccess();
    }

    private static boolean createManifest(String overlayName, String source) {
        Shell.Result result = Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + overlayName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + mPackage + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + overlayName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + overlayName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + overlayName, result.getOut());
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

    private static boolean zipAlign(String source, String name) {
        Shell.Result result = Shell.cmd(zipalign + " 4 " + source + ' ' + Resources.UNSIGNED_DIR + "/" + name + "-unsigned.apk").exec();

        if (result.isSuccess())
            Log.i(TAG + " - ZipAlign", "Successfully zip aligned " + mOverlayName);
        else {
            Log.e(TAG + " - ZipAlign", "Failed to zip align " + mOverlayName + "\n" + String.join("\n", result.getOut()));
            writeLog(TAG + " - ZipAlign", "Failed to zip align " + mOverlayName, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean apkSigner(String source, String name) {
        try {
            PrivateKey key = readPrivateKey(Iconify.getAppContext().getAssets().open("Keystore/testkey.pk8"));
            X509Certificate cert = readCertificate(Iconify.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));

            JarMap jar = JarMap.open(new FileInputStream(source), true);
            FileOutputStream out = new FileOutputStream(Resources.SIGNED_DIR + "/IconifyComponent" + name + ".apk");

            SignAPK.sign(cert, key, jar, out);

            Log.i(TAG + " - APKSigner", "Successfully signed " + mOverlayName);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            writeLog(TAG + " - APKSigner", "Failed to sign " + mOverlayName, e.toString());
            postExecute(true);
            return true;
        }
        return false;
    }
}
