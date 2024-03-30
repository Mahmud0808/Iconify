package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.common.Dynamic.AAPT;
import static com.drdisagree.iconify.common.Dynamic.AAPT2;
import static com.drdisagree.iconify.common.Dynamic.ZIPALIGN;
import static com.drdisagree.iconify.common.Dynamic.isAtleastA14;
import static com.drdisagree.iconify.common.Resources.FRAMEWORK_DIR;
import static com.drdisagree.iconify.common.Resources.UNSIGNED_UNALIGNED_DIR;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readCertificate;
import static com.drdisagree.iconify.utils.apksigner.CryptoUtils.readPrivateKey;
import static com.drdisagree.iconify.utils.helper.Logger.writeLog;

import android.util.Log;

import androidx.annotation.NonNull;

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
    private static final String aapt2 = AAPT2.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();

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
        String command;

        if (!isAtleastA14) {
            command = aapt + " p -f -M " + source + "/AndroidManifest.xml -I " + FRAMEWORK_DIR + " -S " + source + "/res -F " + UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk --include-meta-data --auto-add-overlay";
        } else {
            command = getAAPT2Command(source, name);

            if (isQsTileOrTextOverlay(name)) {
                QsResourceManager.removeQuickSettingsStyles(source, name);
            }
        }

        while (attempt-- != 0) {
            result = Shell.cmd(command).exec();

            if (!result.isSuccess() && OverlayCompiler.listContains(result.getOut(), "colorSurfaceHeader")) {
                Shell.cmd("find " + source + "/res -type f -name \"*.xml\" -exec sed -i '/colorSurfaceHeader/d' {} +").exec();
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

    @NonNull
    private static String getAAPT2Command(String source, String name) {
        String folderCommand = "rm -rf " + source + "/compiled; mkdir " + source + "/compiled; [ -d " + source + "/compiled ] && ";
        String compileCommand = aapt2 + " compile --dir " + source + "/res -o " + source + "/compiled && ";
        String linkCommand = aapt2 + " link -o " + UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk -I " + FRAMEWORK_DIR + " --manifest " + source + "/AndroidManifest.xml " + source + "/compiled/* --auto-add-overlay";

        return folderCommand + compileCommand + linkCommand;
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
