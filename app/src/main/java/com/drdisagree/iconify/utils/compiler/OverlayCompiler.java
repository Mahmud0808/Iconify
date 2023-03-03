package com.drdisagree.iconify.utils.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;
import static com.drdisagree.iconify.utils.helpers.Logger.writeLog;

import android.util.Log;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.apksigner.JarMap;
import com.drdisagree.iconify.utils.apksigner.SignAPK;
import com.topjohnwu.superuser.Shell;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class OverlayCompiler {

    private static final String TAG = "OverlayCompiler";
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();

    public static boolean createManifest(String name, String target, String source) {
        Shell.Result result = null;
        int attempt = 3;

        while (attempt-- != 0) {
            result = Shell.cmd("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>\\n<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + name + ".overlay\">\\n\\t<overlay android:priority=\"1\" android:targetPackage=\"" + target + "\" />\\n\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />\\n</manifest>' > " + source + "/AndroidManifest.xml;").exec();

            if (result.isSuccess()) {
                Log.i(TAG + " - Manifest", "Successfully created manifest for " + name);
                break;
            } else {
                Log.e(TAG + " - Manifest", "Failed to create manifest for " + name + '\n' + String.join("\n", result.getOut()));
                try {
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                }
            }
        }

        if (!result.isSuccess())
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + name, result.getOut());

        return !result.isSuccess();
    }

    public static boolean runAapt(String source, String name) {
        Shell.Result result = null;
        int attempt = 3;

        while (attempt-- != 0) {
            result = Shell.cmd(aapt + " p -f -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + Resources.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk >/dev/null;").exec();

            if (result.isSuccess()) {
                Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
                break;
            } else {
                Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
                try {
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                }
            }
        }

        if (!result.isSuccess())
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());

        return !result.isSuccess();
    }

    public static boolean zipAlign(String source, String name) {
        Shell.Result result = null;
        int attempt = 3;

        while (attempt-- != 0) {
            result = Shell.cmd(zipalign + " -p -f 4 " + source + ' ' + Resources.UNSIGNED_DIR + '/' + name).exec();

            if (result.isSuccess()) {
                Log.i(TAG + " - ZipAlign", "Successfully zip aligned " + name);
                break;
            } else {
                Log.e(TAG + " - ZipAlign", "Failed to zip align " + name + '\n' + String.join("\n", result.getOut()));
                try {
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                }
            }
        }

        if (!result.isSuccess())
            writeLog(TAG + " - ZipAlign", "Failed to zip align " + name, result.getOut());

        return !result.isSuccess();
    }

    public static boolean apkSigner(String source, String name) {
        try {
            PrivateKey key = readPrivateKey(Iconify.getAppContext().getAssets().open("Keystore/testkey.pk8"));
            X509Certificate cert = readCertificate(Iconify.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));

            JarMap jar = JarMap.open(new FileInputStream(source), true);
            FileOutputStream out = new FileOutputStream(Resources.SIGNED_DIR + "/IconifyComponent" + name);

            SignAPK.sign(cert, key, jar, out);

            Log.i(TAG + " - APKSigner", "Successfully signed " + name.replace(".apk", ""));
        } catch (Exception e) {
            Log.e(TAG + " - APKSigner", "Failed to sign " + name.replace(".apk", "") + '\n' + e);
            writeLog(TAG + " - APKSigner", "Failed to sign " + name, e.toString());
            return true;
        }
        return false;
    }
}
