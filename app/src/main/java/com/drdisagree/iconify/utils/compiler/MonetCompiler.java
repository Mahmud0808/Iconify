package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;

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
import com.topjohnwu.superuser.Shell;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class MonetCompiler {

    private static final String TAG = "MonetCompiler";
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();

    public static boolean buildOverlay(String[] resources) throws IOException {
        preExecute();

        // Create AndroidManifest.xml
        String overlay_name = "ME";

        if (createManifest(overlay_name, Resources.DATA_DIR + "/Overlays/android/ME")) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Write color resources
        if (writeResources(Resources.DATA_DIR + "/Overlays/android/ME", resources)) {
            Log.e(TAG, "Failed to write resource for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (runAapt(Resources.DATA_DIR + "/Overlays/android/ME", overlay_name)) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/ME-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align ME-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (apkSigner(Resources.UNSIGNED_DIR + "/ME-unsigned.apk")) {
            Log.e(TAG, "Failed to sign ME-unsigned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();

        // Extract overlay from assets
        FileUtil.copyAssets("Overlays/android/ME");

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();

        // Disable the overlay in case it is already enabled
        OverlayUtil.disableOverlay("IconifyComponentME.overlay");
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.OVERLAY_DIR + "/IconifyComponentME.apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentME.apk");

            SystemUtil.mountRW();
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + "/system/product/overlay/IconifyComponentME.apk").exec();
            RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentME.apk");
            SystemUtil.mountRO();

            OverlayUtil.enableOverlay("IconifyComponentDM.overlay");
            OverlayUtil.enableOverlay("IconifyComponentME.overlay");
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();
    }

    private static boolean createManifest(String pkgName, String source) {
        return !Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + Const.FRAMEWORK_PACKAGE + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec().isSuccess();
    }

    private static boolean writeResources(String source, String[] resources) {
        return !Shell.cmd("rm -rf " + source + "/res/values/colors.xml", "printf '" + resources[0] + "' > " + source + "/res/values/colors.xml;", "rm -rf " + source + "/res/values-night/colors.xml", "printf '" + resources[1] + "' > " + source + "/res/values-night/colors.xml;").exec().isSuccess();
    }

    private static boolean runAapt(String source, String name) {
        return !Shell.cmd(aapt + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + Resources.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec().isSuccess();
    }

    private static boolean zipAlign(String source) {
        return !Shell.cmd(zipalign + " 4 " + source + ' ' + Resources.UNSIGNED_DIR + "/ME-unsigned.apk").exec().isSuccess();
    }

    private static boolean apkSigner(String source) {
        try {
            PrivateKey key = readPrivateKey(Iconify.getAppContext().getAssets().open("Keystore/testkey.pk8"));
            X509Certificate cert = readCertificate(Iconify.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));

            JarMap jar = JarMap.open(new FileInputStream(source), true);
            FileOutputStream out = new FileOutputStream(Resources.SIGNED_DIR + "/IconifyComponentME.apk");

            SignAPK.sign(cert, key, jar, out);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            postExecute(true);
            return true;
        }
        return false;
    }
}
