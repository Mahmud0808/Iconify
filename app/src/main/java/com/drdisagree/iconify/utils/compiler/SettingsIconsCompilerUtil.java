package com.drdisagree.iconify.utils.compiler;

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
import java.util.Objects;

public class SettingsIconsCompilerUtil {

    private static final String TAG = "SettingsIconsCompilerUtil";
    private static final String aapt = References.TOOLS_DIR + "/libaapt.so";
    private static final String zipalign = References.TOOLS_DIR + "/libzipalign.so";
    private static final String[] packages = new String[]{"com.android.settings", "com.google.android.apps.wellbeing", "com.google.android.gms"};
    private static int mIconSet = 1, mIconBg = 1;

    public static boolean buildOverlay(int iconSet, int iconBg, String resources) throws IOException {
        mIconSet = iconSet;
        mIconBg = iconBg;

        preExecute();
        moveOverlaysToCache();

        for (int i = 0; i < packages.length; i++) {
            String overlay_name = "SIP" + (i + 1);

            // Create AndroidManifest.xml
            if (createManifest(overlay_name, packages[i], References.TEMP_CACHE_DIR + "/" + packages[i] + "/" + overlay_name)) {
                Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Write resources
            if (!Objects.equals(resources, "")) {
                if (writeResources(References.TEMP_CACHE_DIR + "/" + packages[i] + "/" + overlay_name, resources)) {
                    Log.e(TAG, "Failed to write resource for " + overlay_name + "! Exiting...");
                    postExecute(true);
                    return true;
                }
            }

            // Build APK using AAPT
            if (runAapt(References.TEMP_CACHE_DIR + "/" + packages[i] + "/" + overlay_name, overlay_name)) {
                Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
                postExecute(true);
                return true;
            }

            // ZipAlign the APK
            if (zipAlign(References.UNSIGNED_UNALIGNED_DIR + "/" + overlay_name + "-unsigned-unaligned.apk", overlay_name)) {
                Log.e(TAG, "Failed to align " + overlay_name + "-unsigned-unaligned.apk! Exiting...");
                postExecute(true);
                return true;
            }

            // Sign the APK
            if (apkSigner(References.UNSIGNED_DIR + "/" + overlay_name + "-unsigned.apk", overlay_name)) {
                Log.e(TAG, "Failed to sign " + overlay_name + "-unsigned.apk! Exiting...");
                postExecute(true);
                return true;
            }
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Clean data directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/CompileOnDemand").exec();

        // Extract keystore and overlay from assets
        FileUtil.copyAssets("Keystore");
        for (String aPackage : packages)
            FileUtil.copyAssets("CompileOnDemand/" + aPackage + "/SIP" + mIconSet);

        // Create temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR + "; mkdir -p " + References.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + References.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + References.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.SIGNED_DIR).exec();
        for (String aPackages : packages)
            Shell.cmd("mkdir -p " + References.TEMP_CACHE_DIR + "/" + aPackages + "/").exec();

        // Disable the overlay in case it is already enabled
        for (int i = 1; i <= packages.length; i++)
            OverlayUtil.disableOverlay("IconifyComponentSIP" + i + ".overlay");
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (int i = 1; i <= packages.length; i++) {
                Shell.cmd("cp -rf " + References.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + References.OVERLAY_DIR + "/IconifyComponentSIP" + i + ".apk").exec();
                RootUtil.setPermissions(644, References.OVERLAY_DIR + "/IconifyComponentSIP" + i + ".apk");
            }

            SystemUtil.mountRW();
            for (int i = 1; i <= 3; i++) {
                Shell.cmd("cp -rf " + References.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + "/system/product/overlay/IconifyComponentSIP" + i + ".apk").exec();
                RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentSIP" + i + ".apk");
            }
            SystemUtil.mountRO();

            for (int i = 1; i <= 3; i++) {
                OverlayUtil.enableOverlay("IconifyComponentSIP" + i + ".overlay");
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        Shell.cmd("mv -f \"" + References.DATA_DIR + "/CompileOnDemand/" + packages[0] + "/" + "SIP" + mIconSet + "\" \"" + References.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1\"").exec().isSuccess();
        Shell.cmd("mv -f \"" + References.DATA_DIR + "/CompileOnDemand/" + packages[1] + "/" + "SIP" + mIconSet + "\" \"" + References.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2\"").exec().isSuccess();
        Shell.cmd("mv -f \"" + References.DATA_DIR + "/CompileOnDemand/" + packages[2] + "/" + "SIP" + mIconSet + "\" \"" + References.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3\"").exec().isSuccess();

        if (mIconBg == 1) {
            Shell.cmd("rm -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable\"", "cp -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-night\" \"" + References.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable\"").exec();
            Shell.cmd("rm -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-anydpi\"", "cp -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-night-anydpi\" \"" + References.TEMP_CACHE_DIR + "/" + packages[0] + "/" + "SIP1/res/drawable-anydpi\"").exec();

            Shell.cmd("rm -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable\"", "cp -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-night\" \"" + References.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable\"").exec();
            Shell.cmd("rm -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-anydpi\"", "cp -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-night-anydpi\" \"" + References.TEMP_CACHE_DIR + "/" + packages[1] + "/" + "SIP2/res/drawable-anydpi\"").exec();

            Shell.cmd("rm -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable\"", "cp -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-night\" \"" + References.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable\"").exec();
            Shell.cmd("rm -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-anydpi\"", "cp -rf \"" + References.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-night-anydpi\" \"" + References.TEMP_CACHE_DIR + "/" + packages[2] + "/" + "SIP3/res/drawable-anydpi\"").exec();
        }
    }

    private static boolean createManifest(String overlayName, String pkgName, String source) {
        return !Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + overlayName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + pkgName + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec().isSuccess();
    }

    private static boolean writeResources(String source, String resources) {
        return !Shell.cmd("rm -rf " + source + "/res/values/Iconify.xml", "printf '" + resources + "' > " + source + "/res/values/Iconify.xml;").exec().isSuccess();
    }

    private static boolean runAapt(String source, String name) {
        return !Shell.cmd(aapt + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + References.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec().isSuccess();
    }

    private static boolean zipAlign(String source, String name) {
        return !Shell.cmd(zipalign + " 4 " + source + ' ' + References.UNSIGNED_DIR + "/" + name + "-unsigned.apk").exec().isSuccess();
    }

    private static boolean apkSigner(String source, String name) {
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
            FileOutputStream out = new FileOutputStream(References.SIGNED_DIR + "/IconifyComponent" + name + ".apk");

            SignAPK.sign(cert, key, jar, out);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            postExecute(true);
            return true;
        }
        return false;
    }
}
