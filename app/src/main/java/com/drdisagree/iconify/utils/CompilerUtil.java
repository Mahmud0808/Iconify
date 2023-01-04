package com.drdisagree.iconify.utils;

import android.os.Build;
import android.os.Environment;
import android.util.Log;
import com.android.apksig.ApkSigner;
import com.topjohnwu.superuser.Shell;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CompilerUtil {
    private static final String TEMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify";
    private static final String UNSIGNED_UNALIGNED_DIR = TEMP_DIR + "/overlays/unsigned_unaligned";
    private static final String UNSIGNED_DIR = TEMP_DIR + "/overlays/unsigned";
    private static final String SIGNED_DIR = TEMP_DIR + "/overlays/signed";

    public static void buildOverlays() throws IOException {
        // Extract keystore and overlays from assets
        FileUtil.copyAssets("Keystore");
        FileUtil.copyAssets("Overlays");

        Shell.cmd("rm -rf " + TEMP_DIR + "; mkdir -p " + TEMP_DIR).exec();
        Shell.cmd("rm -rf " + UNSIGNED_UNALIGNED_DIR + "; mkdir -p " + UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("rm -rf " + UNSIGNED_DIR + "; mkdir -p " + UNSIGNED_DIR).exec();
        Shell.cmd("rm -rf " + SIGNED_DIR + "; mkdir -p " + SIGNED_DIR).exec();

        // Create AndroidManifest.xml and build APK using AAPT
        File dir = new File(ModuleUtil.DATA_DIR + "/Overlays");
        for (File pkg : Objects.requireNonNull(dir.listFiles())) {
            if (pkg.isDirectory()) {
                for (File overlay : Objects.requireNonNull(pkg.listFiles())) {
                    if (overlay.isDirectory()) {
                        String overlay_name = overlay.toString().replace(pkg.toString() + '/', "");
                        createManifest(overlay_name, pkg.toString().replace(ModuleUtil.DATA_DIR + "/Overlays/", ""), overlay.getAbsolutePath());

                        runAapt(overlay.getAbsolutePath(), UNSIGNED_UNALIGNED_DIR, overlay_name);
                    }
                }
            }
        }

        // ZipAlign the APK
        dir = new File(UNSIGNED_UNALIGNED_DIR);
        Log.d("ZipAlign", Arrays.toString(dir.listFiles()));
        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                zipAlign(overlay.getAbsolutePath(), UNSIGNED_DIR, overlay.toString().replace(UNSIGNED_UNALIGNED_DIR + '/', "").replace("-unaligned", ""));
            }
        }

        // Sign the APK
        dir = new File(UNSIGNED_DIR);
        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                apkSigner(overlay.getAbsolutePath(), overlay.toString().replace(UNSIGNED_DIR + '/', "").replace("-unsigned", ""));
            }
        }

        // Move all generated overlays to module
        Shell.cmd("cp -a " + SIGNED_DIR + "/. " + ModuleUtil.OVERLAY_DIR).exec();

        // Clean temp directory
        Shell.cmd("rm -rf " + TEMP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleUtil.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + ModuleUtil.DATA_DIR + "/Overlays").exec();

        // Set permissions
        RootUtil.setPermissionsRecursively(644, ModuleUtil.OVERLAY_DIR + '/');
    }

    private static void createManifest(String pkgName, String target, String destination) {
        Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"" + pkgName + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + target + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + destination + "/AndroidManifest.xml;").exec();
    }

    private static void runAapt(String source, String destination, String name) {
        Shell.cmd(ModuleUtil.MODULE_DIR + "/tools/aapt p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + destination + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec();
    }

    private static void zipAlign(String source, String destination, String name) {
        Shell.cmd(ModuleUtil.MODULE_DIR + "/tools/zipalign 4 " + source + ' ' + destination + '/' + name).exec();
    }

    private static void apkSigner(String source, String name) {
        try {
            File key = new File(ModuleUtil.DATA_DIR + "/Keystore/key");
            char[] keyPass = "overlay".toCharArray();

            if (!key.exists()) {
                Log.d("KeyStore", "Loading keystore...");
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
                    .setV1SigningEnabled(false)
                    .setV2SigningEnabled(true)
                    .setV3SigningEnabled(true)
                    .setV4SigningEnabled(true)
                    .setInputApk(new File(source))
                    .setOutputApk(new File(SIGNED_DIR + '/' + name))
                    .setMinSdkVersion(Build.VERSION.SDK_INT)
                    .build()
                    .sign();

            Log.e("ApkSigner", "APK successfully signed!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
