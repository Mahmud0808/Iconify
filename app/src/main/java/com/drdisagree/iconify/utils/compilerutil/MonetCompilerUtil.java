package com.drdisagree.iconify.utils.compilerutil;

import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;

import android.util.Log;

import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.apksigner.JarMap;
import com.drdisagree.iconify.utils.apksigner.SignAPK;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class MonetCompilerUtil {

    private static final String TAG = "MonetCompilerUtil";
    private static final String aapt = References.TOOLS_DIR + "/libaapt.so";
    private static final String zipalign = References.TOOLS_DIR + "/libzipalign.so";

    public static boolean buildOverlay(String resources) throws IOException {
        preExecute();

        // Create AndroidManifest.xml
        String overlay_name = "ME";

        if (createManifest(overlay_name, References.DATA_DIR + "/Overlays/android/ME")) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Write color resources
        if (writeResources(References.DATA_DIR + "/Overlays/android/ME", resources)) {
            Log.e(TAG, "Failed to write resource for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (runAapt(References.DATA_DIR + "/Overlays/android/ME", overlay_name)) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (zipAlign(References.UNSIGNED_UNALIGNED_DIR + "/ME-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align ME-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (apkSigner(References.UNSIGNED_DIR + "/ME-unsigned.apk")) {
            Log.e(TAG, "Failed to sign ME-unsigned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Clean data directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();

        // Extract keystore and overlay from assets
        FileUtil.copyAssets("Keystore");
        FileUtil.copyAssets("Overlays/android/ME");

        // Create temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR + "; mkdir -p " + References.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + References.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.SIGNED_DIR).exec();

        // Disable the overlay in case it is already enabled
        OverlayUtil.disableOverlay("IconifyComponentME.overlay");
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + References.SIGNED_DIR + "/IconifyComponentME.apk " + References.OVERLAY_DIR + "/IconifyComponentME.apk").exec();
            RootUtil.setPermissions(644, References.OVERLAY_DIR + "/IconifyComponentME.apk");

            SystemUtil.mountRW();
            Shell.cmd("cp -rf " + References.SIGNED_DIR + "/IconifyComponentME.apk " + "/system/product/overlay/IconifyComponentME.apk").exec();
            RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentME.apk");
            SystemUtil.mountRO();

            OverlayUtil.enableOverlay("IconifyComponentDM.overlay");
            OverlayUtil.enableOverlay("IconifyComponentME.overlay");
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();
    }

    private static boolean createManifest(String pkgName, String source) {
        return !Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + References.FRAMEWORK_PACKAGE + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec().isSuccess();
    }

    private static boolean writeResources(String source, String resources) {
        return !Shell.cmd("rm -rf " + source + "/res/values/colors.xml", "printf '" + resources + "' > " + source + "/res/values/colors.xml;").exec().isSuccess();
    }

    private static boolean runAapt(String source, String name) {
        return !Shell.cmd(aapt + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + References.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec().isSuccess();
    }

    private static boolean zipAlign(String source) {
        return !Shell.cmd(zipalign + " 4 " + source + ' ' + References.UNSIGNED_DIR + "/ME-unsigned.apk").exec().isSuccess();
    }

    private static boolean apkSigner(String source) {
        File testKey = new File(References.DATA_DIR + "/Keystore/testkey.pk8");
        File certificate = new File(References.DATA_DIR + "/Keystore/testkey.x509.pem");

        if (!testKey.exists() || !certificate.exists()) {
            Log.d("KeyStore", "Loading keystore from assets...");
            try {
                FileUtil.copyAssets("Keystore");
            } catch (Exception e) {
                postExecute(true);
                return true;
            }
        }

        try {
            InputStream keyFile = new FileInputStream(testKey);
            PrivateKey key = readPrivateKey(keyFile);

            InputStream certFile = new FileInputStream(certificate);
            X509Certificate cert = readCertificate(certFile);

            JarMap jar = JarMap.open(new FileInputStream(source), true);
            FileOutputStream out = new FileOutputStream(References.SIGNED_DIR + "/IconifyComponentME.apk");

            SignAPK.sign(cert, key, jar, out);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            postExecute(true);
            return true;
        }
        return false;
    }
}
