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

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class OverlayCompiler {

    private static final String TAG = "OverlayCompiler";
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();
    private static PrivateKey key = null;
    private static X509Certificate cert = null;

    public static boolean runAapt(String source) {
        String name = getOverlayName(source);
        Shell.Result result = Shell.cmd(aapt + " p -f -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + Resources.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk").exec();

        if (result.isSuccess()) {
            Log.i(TAG + " - AAPT2", "Successfully built APK for " + name);
        } else {
            Log.e(TAG + " - AAPT2", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT2", "Failed to build APK for " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    public static boolean runAapt(String source, String[] splitLocations) {
        String name = getOverlayName(source);
        StringBuilder aaptCommand = new StringBuilder(aapt + " p -f -M " + source + "/AndroidManifest.xml -S " + source + "/res -F " + Resources.COMPANION_COMPILED_DIR + '/' + name + ".zip --include-meta-data --auto-add-overlay -f -I /system/framework/framework-res.apk");

        if (splitLocations != null) {
            for (String targetApk : splitLocations) {
                aaptCommand.append(" -I ").append(targetApk);
            }
        }

        Shell.Result result = Shell.cmd(String.valueOf(aaptCommand)).exec();

        if (result.isSuccess()) Log.i(TAG + " - AAPT2", "Successfully built APK for " + name);
        else {
            Log.e(TAG + " - AAPT2", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT2", "Failed to build APK for " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    public static boolean zipAlign(String source) {
        String fileName = getOverlayName(source);
        Shell.Result result = Shell.cmd(zipalign + " 4 " + source + ' ' + Resources.UNSIGNED_DIR + "/" + fileName + "-unsigned.apk").exec();

        if (result.isSuccess())
            Log.i(TAG + " - ZipAlign", "Successfully zip aligned " + fileName);
        else {
            Log.e(TAG + " - ZipAlign", "Failed to zip align " + fileName + "\n" + String.join("\n", result.getOut()));
            writeLog(TAG + " - ZipAlign", "Failed to zip align " + fileName, result.getOut());
        }

        return !result.isSuccess();
    }

    public static boolean apkSigner(String source) {
        String fileName = "null";
        try {
            if (key == null) {
                key = readPrivateKey(Iconify.getAppContext().getAssets().open("Keystore/testkey.pk8"));
            }
            if (cert == null) {
                cert = readCertificate(Iconify.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));
            }

            fileName = getOverlayName(source);
            JarMap jar = JarMap.open(Files.newInputStream(Paths.get(source)), true);
            FileOutputStream out = new FileOutputStream(Resources.SIGNED_DIR + "/IconifyComponent" + fileName + ".apk");

            SignAPK.sign(cert, key, jar, out);

            Log.i(TAG + " - APKSigner", "Successfully signed " + fileName);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            writeLog(TAG + " - APKSigner", "Failed to sign " + fileName, e.toString());
            return true;
        }
        return false;
    }

    private static String getOverlayName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();

        return fileName.replace("IconifyComponent", "").replace("-unsigned", "").replace("-unaligned", "").replace(".apk", "");
    }
}
