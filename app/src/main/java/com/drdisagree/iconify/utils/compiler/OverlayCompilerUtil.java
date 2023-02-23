package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.utils.HelperUtil.writeLog;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;

import android.util.Log;

import com.drdisagree.iconify.common.References;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.HelperUtil;
import com.drdisagree.iconify.utils.RootUtil;
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

public class OverlayCompilerUtil {

    private static final String TAG = "OverlayCompilerUtil";
    private static final String aapt = References.TOOLS_DIR + "/libaapt.so";
    private static final String zipalign = References.TOOLS_DIR + "/libzipalign.so";

    public static boolean buildAPK() {
        // Create AndroidManifest.xml and build APK using AAPT
        File dir = new File(References.DATA_DIR + "/Overlays");
        if (dir.listFiles() == null) return true;

        for (File pkg : Objects.requireNonNull(dir.listFiles())) {
            if (pkg.isDirectory()) {
                for (File overlay : Objects.requireNonNull(pkg.listFiles())) {
                    if (overlay.isDirectory()) {
                        String overlay_name = overlay.toString().replace(pkg.toString() + '/', "");
                        if (createManifest(overlay_name, pkg.toString().replace(References.DATA_DIR + "/Overlays/", ""), overlay.getAbsolutePath())) {
                            postExecute(true);
                            return true;
                        }
                        if (runAapt(overlay.getAbsolutePath(), overlay_name)) {
                            postExecute(true);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static boolean alignAPK() {
        // ZipAlign the APK
        File dir = new File(References.UNSIGNED_UNALIGNED_DIR);
        if (dir.listFiles() == null) return true;

        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                if (zipAlign(overlay.getAbsolutePath(), overlay.toString().replace(References.UNSIGNED_UNALIGNED_DIR + '/', "").replace("-unaligned", ""))) {
                    postExecute(true);
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean signAPK() {
        // Sign the APK
        File dir = new File(References.UNSIGNED_DIR);
        if (dir.listFiles() == null) return true;

        for (File overlay : Objects.requireNonNull(dir.listFiles())) {
            if (!overlay.isDirectory()) {
                if (apkSigner(overlay.getAbsolutePath(), overlay.toString().replace(References.UNSIGNED_DIR + '/', "").replace("-unsigned", ""))) {
                    postExecute(true);
                    return true;
                }
            }
        }
        return false;
    }

    public static void preExecute() throws IOException {
        // Clean data directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();

        // Extract keystore and overlays from assets
        FileUtil.copyAssets("Keystore");
        FileUtil.copyAssets("Overlays");

        // Create temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR + "; mkdir -p " + References.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + References.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + References.SIGNED_DIR).exec();
    }

    public static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -a " + References.SIGNED_DIR + "/. " + References.OVERLAY_DIR).exec();
            RootUtil.setPermissionsRecursively(644, References.OVERLAY_DIR + '/');
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + References.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Keystore").exec();
        Shell.cmd("rm -rf " + References.DATA_DIR + "/Overlays").exec();

        // Restore backups
        HelperUtil.restoreFiles();
    }

    private static boolean createManifest(String name, String target, String source) {
        Shell.Result result = Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + name + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + target + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + name);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean runAapt(String source, String name) {
        Shell.Result result = Shell.cmd(aapt + " p -f -v -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + References.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec();

        if (result.isSuccess()) Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
        else {
            Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean zipAlign(String source, String name) {
        Shell.Result result = Shell.cmd(zipalign + " 4 " + source + ' ' + References.UNSIGNED_DIR + '/' + name).exec();

        if (result.isSuccess()) Log.i(TAG + " - ZipAlign", "Successfully zip aligned " + name);
        else {
            Log.e(TAG + " - ZipAlign", "Failed to zip align " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - ZipAlign", "Failed to zip align " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    private static boolean apkSigner(String source, String name) {
        try {
            File testKey = new File(References.DATA_DIR + "/Keystore/testkey.pk8");
            File certificate = new File(References.DATA_DIR + "/Keystore/testkey.x509.pem");

            if (!testKey.exists() || !certificate.exists()) {
                Log.d("KeyStore", "Loading keystore from assets...");
                FileUtil.copyAssets("Keystore");
            }

            InputStream keyFile = new FileInputStream(testKey);
            PrivateKey key = readPrivateKey(keyFile);

            InputStream certFile = new FileInputStream(certificate);
            X509Certificate cert = readCertificate(certFile);

            JarMap jar = JarMap.open(new FileInputStream(source), true);
            FileOutputStream out = new FileOutputStream(References.SIGNED_DIR + "/IconifyComponent" + name);

            SignAPK.sign(cert, key, jar, out);

            Log.i(TAG + " - APKSigner", "Successfully signed " + name.replace(".apk", ""));
        } catch (Exception e) {
            Log.e(TAG + " - APKSigner", "Failed to sign " + name.replace(".apk", "") + '\n' + e);
            writeLog(TAG + " - APKSigner", "Failed to sign " + name, e.toString());
            postExecute(true);
            return true;
        }
        return false;
    }
}
