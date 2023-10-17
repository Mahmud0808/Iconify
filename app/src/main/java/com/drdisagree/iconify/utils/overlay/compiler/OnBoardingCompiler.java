package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;
import static com.drdisagree.iconify.utils.helper.Logger.writeLog;

import android.os.Build;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.apksigner.SignAPK;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OnBoardingCompiler {

    private static final String TAG = OnBoardingCompiler.class.getSimpleName();
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();

    public static boolean createManifest(String name, String target, String source) {
        Shell.Result result = null;
        int attempt = 3;
        String category = OverlayUtil.getCategory(name);

        while (attempt-- != 0) {
            List<String> module = new ArrayList<>();
            module.add("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
            module.add("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + name + ".overlay\">");
            module.add("\\t<uses-sdk android:minSdkVersion=\"" + BuildConfig.MIN_SDK_VERSION + "\" android:targetSdkVersion=\"" + Build.VERSION.SDK_INT + "\" />");
            module.add("\\t<overlay android:category=\"" + category + "\" android:priority=\"1\" android:targetPackage=\"" + target + "\" />");
            module.add("\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />");
            module.add("</manifest>' > " + source + "/AndroidManifest.xml;");

            result = Shell.cmd(String.join("\\n", module)).exec();

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
            PrivateKey key = readPrivateKey(Objects.requireNonNull(Iconify.getAppContext()).getAssets().open("Keystore/testkey.pk8"));
            X509Certificate cert = readCertificate(Iconify.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));

            SignAPK.sign(cert, key, source, Resources.SIGNED_DIR + "/IconifyComponent" + name);

            Log.i(TAG + " - APKSigner", "Successfully signed " + name.replace(".apk", ""));
        } catch (Exception e) {
            Log.e(TAG + " - APKSigner", "Failed to sign " + name.replace(".apk", "") + '\n' + e);
            writeLog(TAG + " - APKSigner", "Failed to sign " + name, e);
            return true;
        }
        return false;
    }
}
