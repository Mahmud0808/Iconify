package com.drdisagree.iconify.utils;

import android.os.Build;
import android.util.Log;

import com.android.apksig.ApkSigner;
import com.drdisagree.iconify.common.References;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CompilerUtil {

    private static final String TAG = "CompilerUtil";

    public static boolean buildOverlays() throws IOException {
        preExecute();

        // Detect and use compatible AAPT
        List<String> aaptOuts = Shell.cmd("[ ! -z \"$(" + References.MODULE_DIR + "/tools/aapt v)\" ] && echo \"is executable by iconify\"; [ ! -z \"$(" + References.MODULE_DIR + "/tools/aapt64 v)\" ] && echo \"is executable by iconify\"").exec().getOut();
        String aaptToUse = aaptOuts.get(0).contains("is executable by iconify") ? "aapt" : (aaptOuts.get(1).contains("is executable by iconify") ? "aapt64" : "aaptx86");

        // Detect and use compatible ZipAlign
        List<String> zipAlignOuts1 = Shell.cmd(References.MODULE_DIR + "/tools/zipalign").exec().getOut();
        List<String> zipAlignOuts2 = Shell.cmd(References.MODULE_DIR + "/tools/zipalign64").exec().getOut();
        String zipAlignToUse = zipAlignOuts1.get(0).contains("Zip alignment utility") ? "zipalign" : (zipAlignOuts2.get(0).contains("Zip alignment utility") ? "zipalign64" : "zipalign86");

        // Create AndroidManifest.xml and build APK using AAPT
        File dir = new File(References.DATA_DIR + "/Overlays");
        for (File pkg : Objects.requireNonNull(dir.listFiles())) {
            if (pkg.isDirectory()) {
                for (File overlay : Objects.requireNonNull(pkg.listFiles())) {
                    if (overlay.isDirectory()) {
                        String overlay_name = overlay.toString().replace(pkg.toString() + '/', "");
                        if (createManifest(overlay_name, pkg.toString().replace(References.DATA_DIR + "/Overlays/", ""), overlay.getAbsolutePath())) {
                            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
                            postExecute();
                            return true;
                        }

                        if (runAapt(aaptToUse, overlay.getAbsolutePath(), overlay_name)) {
                            Log.e(TAG, "Failed to build APK using " + aaptToUse + " for " + overlay_name + "! Exiting...");
                            postExecute();
                            return true;
                        }
                    }
                }
            }
        }

        // ZipAlign the APK
        dir = new File(References.UNSIGNED_UNALIGNED_DIR);
        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                if (zipAlign(zipAlignToUse, overlay.getAbsolutePath(), overlay.toString().replace(References.UNSIGNED_UNALIGNED_DIR + '/', "").replace("-unaligned", ""))) {
                    Log.e(TAG, "Failed to align APK using " + zipAlignToUse + " for " + overlay + "! Exiting...");
                    postExecute();
                    return true;
                }
            }
        }

        // Sign the APK
        dir = new File(References.UNSIGNED_DIR);
        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                if (apkSigner(overlay.getAbsolutePath(), overlay.toString().replace(References.UNSIGNED_DIR + '/', "").replace("-unsigned", ""))) {
                    Log.e(TAG, "Failed to sign " + overlay + "! Exiting...");
                    postExecute();
                    return true;
                }
            }
        }
        postExecute();
        return false;
    }

    private static void preExecute() throws IOException {
        // Clean temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();

        // Extract keystore and overlays from assets
        FileUtil.copyAssets("Keystore");
        FileUtil.copyAssets("Overlays");

        // Create temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR + "; mkdir -p " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.UNSIGNED_UNALIGNED_DIR + "; mkdir -p " + References.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("rm -rf " + References.UNSIGNED_DIR + "; mkdir -p " + References.UNSIGNED_DIR).exec();
        Shell.cmd("rm -rf " + References.SIGNED_DIR + "; mkdir -p " + References.SIGNED_DIR).exec();
    }

    private static void postExecute() {
        // Move all generated overlays to module
        Shell.cmd("cp -a " + References.SIGNED_DIR + "/. " + References.OVERLAY_DIR).exec();

        // Clean temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();

        // Set permissions
        RootUtil.setPermissionsRecursively(644, References.OVERLAY_DIR + '/');
    }

    private static boolean createManifest(String pkgName, String target, String destination) {
        return !Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + target + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + destination + "/AndroidManifest.xml;").exec().isSuccess();
    }

    private static boolean runAapt(String aaptToUse, String source, String name) {
        return !Shell.cmd(References.MODULE_DIR + "/tools/" + aaptToUse + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + References.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec().isSuccess();
    }

    private static boolean zipAlign(String zipAlignToUse, String source, String name) {
        return !Shell.cmd(References.MODULE_DIR + "/tools/" + zipAlignToUse + " 4 " + source + ' ' + References.UNSIGNED_DIR + '/' + name).exec().isSuccess();
    }

    private static boolean apkSigner(String source, String name) {
        try {
            File key = new File(References.DATA_DIR + "/Keystore/key");
            char[] keyPass = "overlay".toCharArray();

            if (!key.exists()) {
                Log.d("KeyStore", "Loading keystore from assets...");
                FileUtil.copyAssets("Keystore");
            }

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(new FileInputStream(key), keyPass);
            PrivateKey privateKey = (PrivateKey) keyStore.getKey("key", keyPass);
            List<X509Certificate> certs = new ArrayList<>();
            certs.add((X509Certificate) keyStore.getCertificateChain("key")[0]);

            ApkSigner.SignerConfig signerConfig = new ApkSigner.SignerConfig.Builder("overlay", privateKey, certs).build();
            List<ApkSigner.SignerConfig> signerConfigs = new ArrayList<>();
            signerConfigs.add(signerConfig);
            new ApkSigner.Builder(signerConfigs)
                    .setV1SigningEnabled(true)
                    .setV2SigningEnabled(true)
                    .setV3SigningEnabled(true)
                    .setInputApk(new File(source))
                    .setOutputApk(new File(References.SIGNED_DIR + "/IconifyComponent" + name))
                    .setMinSdkVersion(Build.VERSION.SDK_INT)
                    .build()
                    .sign();
        } catch (Exception e) {
            Log.e(TAG, "Failed to sign " + name + "! Exiting...\n" + e);
            return true;
        }
        return false;
    }
}
