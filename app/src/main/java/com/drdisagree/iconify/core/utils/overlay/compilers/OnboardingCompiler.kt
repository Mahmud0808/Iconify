package com.drdisagree.iconify.core.utils.overlay.compilers

import android.util.Log
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.core.utils.Logger.writeLog
import com.drdisagree.iconify.core.utils.apksigner.CryptoUtils
import com.drdisagree.iconify.core.utils.apksigner.SignAPK
import com.drdisagree.iconify.data.common.Dynamic.AAPT2
import com.drdisagree.iconify.data.common.Dynamic.ZIPALIGN
import com.drdisagree.iconify.data.common.Resources.FRAMEWORK_DIR
import com.drdisagree.iconify.data.common.Resources.SIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_UNALIGNED_DIR
import com.topjohnwu.superuser.Shell
import java.io.File
import java.security.PrivateKey
import java.security.cert.X509Certificate

object OnboardingCompiler {

    private val TAG = OnboardingCompiler::class.java.simpleName
    private val aapt2: String = AAPT2.absolutePath
    private val zipalign: String = ZIPALIGN.absolutePath
    private var key: PrivateKey? = null
    private var cert: X509Certificate? = null

    fun createManifest(name: String?, target: String?, source: String): Boolean {
        var hasErroredOut = false
        var attempt = 3

        while (attempt-- != 0) {
            if (OverlayCompiler.createManifest(name, target, source)) {
                try {
                    Thread.sleep(1000)
                } catch (_: Exception) {
                }
            } else {
                hasErroredOut = true
                break
            }
        }

        return !hasErroredOut
    }

    fun runAapt(source: String, name: String): Boolean {
        var result: Shell.Result? = null
        var attempt = 3

        val command: String = getAAPT2Command(source, name)

        while (attempt-- != 0) {
            result = Shell.cmd(command).exec()

            if (!result.isSuccess) {
                val keywords = listOf(
                    "colorSurfaceHeader"
                )

                val foundKeywords = keywords.filter { keyword ->
                    result.out.any { it.contains(keyword, ignoreCase = true) }
                }

                if (foundKeywords.isNotEmpty()) {
                    foundKeywords.forEach { keyword ->
                        Shell.cmd(
                            "find $source/res -type f -name \"*.xml\" -exec sed -i '/$keyword/d' {} +"
                        ).exec()
                    }
                    result = Shell.cmd(command).exec()
                }
            }

            if (result.isSuccess) {
                Log.i("$TAG - AAPT", "Successfully built APK for $name")
                break
            } else {
                Log.e(
                    "$TAG - AAPT",
                    "Failed to build APK for $name\n${java.lang.String.join("\n", result.out)}"
                )
                try {
                    Thread.sleep(1000)
                } catch (_: Exception) {
                }
            }
        }

        if (!result!!.isSuccess) writeLog(
            "$TAG - AAPT",
            "Failed to build APK for $name",
            result.out
        )

        return !result.isSuccess
    }

    private fun getAAPT2Command(source: String, name: String): String {
        val folderCommand =
            "rm -rf $source/compiled; mkdir $source/compiled; [ -d $source/compiled ] && "
        val compileCommand =
            "$aapt2 compile --dir $source/res -o $source/compiled && "
        val linkCommand =
            "$aapt2 link -o $UNSIGNED_UNALIGNED_DIR/$name-unsigned-unaligned.apk -I $FRAMEWORK_DIR --manifest $source/AndroidManifest.xml $source/compiled/* --auto-add-overlay"

        return folderCommand + compileCommand + linkCommand
    }

    fun zipAlign(source: String, name: String): Boolean {
        var result: Shell.Result? = null
        var attempt = 3

        while (attempt-- != 0) {
            result =
                Shell.cmd(
                    "$zipalign -p -f 4 $source $UNSIGNED_DIR/$name"
                ).exec()

            if (result.isSuccess) {
                Log.i(
                    "$TAG - ZipAlign",
                    "Successfully zip aligned " + name.replace("-unsigned.apk", "")
                )
                break
            } else {
                Log.e(
                    "$TAG - ZipAlign",
                    "Failed to zip align ${
                        name.replace(
                            "-unsigned.apk",
                            ""
                        )
                    }\n${java.lang.String.join("\n", result.out)}"
                )
                try {
                    Thread.sleep(1000)
                } catch (_: Exception) {
                }
            }
        }

        if (!result!!.isSuccess) writeLog(
            "$TAG - ZipAlign",
            "Failed to zip align " + name.replace("-unsigned.apk", ""),
            result.out
        )

        return !result.isSuccess
    }

    fun apkSigner(source: String?, name: String): Boolean {
        try {
            if (key == null) {
                key = CryptoUtils.readPrivateKey(
                    appContext.assets.open("Keystore/testkey.pk8")
                )
            }
            if (cert == null) {
                cert = CryptoUtils.readCertificate(
                    appContext.assets.open("Keystore/testkey.x509.pem")
                )
            }

            File("$SIGNED_DIR/IconifyComponent$name").let { file ->
                if (file.parentFile?.exists() == false) file.parentFile?.mkdirs()
                if (!file.exists()) file.createNewFile()
            }

            SignAPK.sign(cert, key, source, "$SIGNED_DIR/IconifyComponent$name")

            Log.i("$TAG - APKSigner", "Successfully signed " + name.replace(".apk", ""))
        } catch (e: Exception) {
            Log.e(
                "$TAG - APKSigner", "Failed to sign ${name.replace(".apk", "")}\n$e"
            )
            writeLog("$TAG - APKSigner", "Failed to sign $name", e)
            return true
        }

        return false
    }
}
