package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.common.Resources.FRAMEWORK_DIR;
import static com.drdisagree.iconify.common.Resources.FRAMEWORK_DIR_ALT;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;
import static com.drdisagree.iconify.utils.helper.Logger.writeLog;

import android.os.Build;
import android.util.Log;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.apksigner.SignAPK;
import com.drdisagree.iconify.utils.overlay.manager.QsResourceManager;
import com.topjohnwu.superuser.Shell;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;

public class OnboardingCompiler {

    private static final String TAG = OnboardingCompiler.class.getSimpleName();
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();
    private static boolean isQsTileOrTextOldResource = Build.VERSION.SDK_INT < 34;

    public static boolean createManifest(String name, String target, String source) {
        boolean hasErroredOut = false;
        int attempt = 3;

        while (attempt-- != 0) {
            if (OverlayCompiler.createManifest(name, target, source)) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            } else {
                hasErroredOut = true;
                break;
            }
        }

        return !hasErroredOut;
    }

    public static boolean runAapt(String source, String name) {
        Shell.Result result = null;
        int attempt = 3;
        String command = aapt + " p -f -M " + source + "/AndroidManifest.xml -I " + FRAMEWORK_DIR + " -S " + source + "/res -F " + Resources.UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk --include-meta-data --auto-add-overlay";

        if (isQsTileOrTextOverlay(name) && isQsTileOrTextOldResource) {
            QsResourceManager.replaceResourcesIfRequired(source, name);
        }

        while (attempt-- != 0) {
            result = Shell.cmd(command).exec();

            if (OverlayCompiler.listContains(result.getOut(), "No resource identifier found for attribute")) {
                result = Shell.cmd(command.replace(FRAMEWORK_DIR, FRAMEWORK_DIR_ALT)).exec();
            }

            if (!isQsTileOrTextOldResource &&
                    isQsTileOrTextOverlay(name) &&
                    !result.isSuccess() &&
                    OverlayCompiler.listContains(
                            result.getOut(),
                            "No resource found that matches the given name"
                    )
            ) {
                Log.w(TAG + " - AAPT", "Resources missing, trying to replace resources with old resources...");
                isQsTileOrTextOldResource = true;
                QsResourceManager.replaceResourcesIfRequired(source, name);
                result = Shell.cmd(command).exec();
            }

            if (result.isSuccess()) {
                Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
                break;
            } else {
                Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
                try {
                    Thread.sleep(1000);
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
                Log.i(TAG + " - ZipAlign", "Successfully zip aligned " + name.replace("-unsigned.apk", ""));
                break;
            } else {
                Log.e(TAG + " - ZipAlign", "Failed to zip align " + name.replace("-unsigned.apk", "") + '\n' + String.join("\n", result.getOut()));
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        }

        if (!result.isSuccess())
            writeLog(TAG + " - ZipAlign", "Failed to zip align " + name.replace("-unsigned.apk", ""), result.getOut());

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

    private static boolean isQsTileOrTextOverlay(String name) {
        return name.contains("QSS") || name.contains("QSNT") || name.contains("QSPT");
    }
}
