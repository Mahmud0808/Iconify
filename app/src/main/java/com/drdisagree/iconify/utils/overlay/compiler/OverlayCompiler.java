package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;
import static com.drdisagree.iconify.utils.helper.Logger.writeLog;

import android.util.Log;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.apksigner.SignAPK;
import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OverlayCompiler {

    private static final String TAG = OverlayCompiler.class.getSimpleName();
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();
    private static PrivateKey key = null;
    private static X509Certificate cert = null;

    public static boolean createManifest(String overlayName, String targetPackage, String sourceDir) {
        List<String> module = new ArrayList<>();
        module.add("printf '" +
                CompilerUtil.createManifestContent(overlayName, targetPackage) +
                "' > " + sourceDir + "/AndroidManifest.xml;");

        Shell.Result result = Shell.cmd(String.join("\\n", module)).exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + overlayName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + overlayName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + overlayName, result.getOut());
        }

        return !result.isSuccess();
    }

    public static boolean runAapt(String source) {
        String name = getOverlayName(source);
        Shell.Result result = Shell.cmd(aapt + " p -f -M " + source + "/AndroidManifest.xml -I /system/framework/framework-res.apk -S " + source + "/res -F " + Resources.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk").exec();

        if (result.isSuccess()) {
            Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
        } else {
            Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    public static boolean runAapt(String source, String[] splitLocations) {
        String name = getOverlayName(source);
        name += source.contains("SpecialOverlays") ? ".zip" : "-unsigned-unaligned.apk";
        String outputDir = source.contains("SpecialOverlays") ? Resources.COMPANION_COMPILED_DIR : Resources.UNSIGNED_UNALIGNED_DIR;
        StringBuilder aaptCommand = new StringBuilder(aapt + " p -f -M " + source + "/AndroidManifest.xml -S " + source + "/res -F " + outputDir + '/' + name + " --include-meta-data --auto-add-overlay -f -I /system/framework/framework-res.apk");

        if (splitLocations != null) {
            for (String targetApk : splitLocations) {
                aaptCommand.append(" -I ").append(targetApk);
            }
        }

        Shell.Result result = Shell.cmd(String.valueOf(aaptCommand)).exec();

        if (result.isSuccess()) Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
        else {
            Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());
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
                key = readPrivateKey(Objects.requireNonNull(Iconify.getAppContext()).getAssets().open("Keystore/testkey.pk8"));
            }
            if (cert == null) {
                cert = readCertificate(Objects.requireNonNull(Iconify.getAppContext()).getAssets().open("Keystore/testkey.x509.pem"));
            }

            fileName = getOverlayName(source);
            SignAPK.sign(cert, key, source, Resources.SIGNED_DIR + "/IconifyComponent" + fileName + ".apk");

            Log.i(TAG + " - APKSigner", "Successfully signed " + fileName);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            writeLog(TAG + " - APKSigner", "Failed to sign " + fileName, e);
            return true;
        }
        return false;
    }

    private static String getOverlayName(String filePath) {
        File file = new File(filePath);
        String fileName = file.getName();

        return fileName.replaceAll("IconifyComponent|-unsigned|-unaligned|.apk", "");
    }
}
