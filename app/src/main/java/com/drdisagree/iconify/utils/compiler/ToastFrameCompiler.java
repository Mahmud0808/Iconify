package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;

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

public class ToastFrameCompiler {

    private static final String TAG = "ToastFrameCompiler";
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();
    private static int mStyle = 1;

    public static boolean buildOverlay(int style) throws IOException {
        mStyle = style;

        preExecute();
        moveOverlaysToCache();

        String overlay_name = "TSTFRM";

        // Create AndroidManifest.xml
        if (createManifest(overlay_name, Resources.TEMP_CACHE_DIR + "/" + FRAMEWORK_PACKAGE + "/" + overlay_name)) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (runAapt(Resources.TEMP_CACHE_DIR + "/" + FRAMEWORK_PACKAGE + "/" + overlay_name, overlay_name)) {
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
        FileUtil.copyAssets("CompileOnDemand/android/TSTFRM" + mStyle);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/android/").exec();

        // Disable the overlay in case it is already enabled
        OverlayUtil.disableOverlay("IconifyComponentTSTFRM.overlay");
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentTSTFRM.apk " + Resources.OVERLAY_DIR + "/IconifyComponentTSTFRM.apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentTSTFRM.apk");

            SystemUtil.mountRW();
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentTSTFRM.apk " + "/system/product/overlay/IconifyComponentTSTFRM.apk").exec();
            RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentTSTFRM.apk");
            SystemUtil.mountRO();

            OverlayUtil.enableOverlay("IconifyComponentTSTFRM.overlay");
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/android/TSTFRM" + mStyle + "\" \"" + Resources.TEMP_CACHE_DIR + "/android/TSTFRM\"").exec().isSuccess();
    }

    private static boolean createManifest(String overlayName, String source) {
        return !Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + overlayName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec().isSuccess();
    }

    private static boolean runAapt(String source, String name) {
        return !Shell.cmd(aapt + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + Resources.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec().isSuccess();
    }

    private static boolean zipAlign(String source, String name) {
        return !Shell.cmd(zipalign + " 4 " + source + ' ' + Resources.UNSIGNED_DIR + "/" + name + "-unsigned.apk").exec().isSuccess();
    }

    private static boolean apkSigner(String source, String name) {
        try {
            PrivateKey key = readPrivateKey(Iconify.getAppContext().getAssets().open("Keystore/testkey.pk8"));
            X509Certificate cert = readCertificate(Iconify.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));

            JarMap jar = JarMap.open(new FileInputStream(source), true);
            FileOutputStream out = new FileOutputStream(Resources.SIGNED_DIR + "/IconifyComponent" + name + ".apk");

            SignAPK.sign(cert, key, jar, out);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            postExecute(true);
            return true;
        }
        return false;
    }
}
