package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.common.Dynamic.AAPT
import com.drdisagree.iconify.common.Dynamic.AAPT2
import com.drdisagree.iconify.common.Dynamic.ZIPALIGN
import com.drdisagree.iconify.common.Dynamic.isAtleastA14
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.common.Resources.FRAMEWORK_DIR
import com.drdisagree.iconify.utils.AppUtils.getSplitLocations
import com.drdisagree.iconify.utils.apksigner.CryptoUtils
import com.drdisagree.iconify.utils.apksigner.SignAPK
import com.drdisagree.iconify.utils.helper.Logger.writeLog
import com.topjohnwu.superuser.Shell
import java.security.PrivateKey
import java.security.cert.X509Certificate

object OverlayCompiler {

    private val TAG = OverlayCompiler::class.java.simpleName
    private val aapt: String = AAPT.absolutePath
    private val aapt2: String = AAPT2.absolutePath
    private val zipalign: String = ZIPALIGN.absolutePath
    private var key: PrivateKey? = null
    private var cert: X509Certificate? = null

    fun createManifest(overlayName: String?, targetPackage: String?, sourceDir: String): Boolean {
        val module: MutableList<String> = ArrayList()
        module.add(
            "printf '${
                CompilerUtils.createManifestContent(
                    overlayName,
                    targetPackage
                )
            }' > " + sourceDir + "/AndroidManifest.xml;"
        )

        val result = Shell.cmd(java.lang.String.join("\\n", module)).exec()

        if (result.isSuccess) Log.i(
            "$TAG - Manifest",
            "Successfully created manifest for $overlayName"
        ) else {
            Log.e(
                "$TAG - Manifest",
                "Failed to create manifest for $overlayName\n${
                    java.lang.String.join(
                        "\n",
                        result.out
                    )
                }"
            )
            writeLog("$TAG - Manifest", "Failed to create manifest for $overlayName", result.out)
        }

        return !result.isSuccess
    }

    fun runAapt(source: String, targetPackage: String?): Boolean {
        val name = CompilerUtils.getOverlayName(source) +
                if (source.contains("SpecialOverlays")) {
                    ".zip"
                } else {
                    "-unsigned-unaligned.apk"
                }
        val aaptCommand = buildAAPT2Command(source, name)
        val splitLocations = getSplitLocations(targetPackage)

        for (targetApk in splitLocations) {
            aaptCommand.append(" -I ").append(targetApk)
        }

        val command = aaptCommand.toString()
        var result = Shell.cmd(command).exec()

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
        } else {
            Log.e(
                "$TAG - AAPT",
                "Failed to build APK for $name\n${java.lang.String.join("\n", result.out)}"
            )
            writeLog("$TAG - AAPT", "Failed to build APK for $name", result.out)
        }

        return !result.isSuccess
    }

    private fun buildAAPT2Command(source: String, name: String): StringBuilder {
        val outputDir =
            if (source.contains("SpecialOverlays")) {
                Resources.COMPANION_COMPILED_DIR
            } else {
                Resources.UNSIGNED_UNALIGNED_DIR
            }

        return if (!isAtleastA14) {
            StringBuilder("$aapt p -f -M $source/AndroidManifest.xml -S $source/res -F $outputDir/$name -I $FRAMEWORK_DIR --include-meta-data --auto-add-overlay")
        } else {
            StringBuilder(getAAPT2Command(source, name, outputDir))
        }
    }

    private fun getAAPT2Command(source: String, name: String, outputDir: String): String {
        val folderCommand =
            "rm -rf $source/compiled; mkdir $source/compiled; [ -d $source/compiled ] && "
        val compileCommand = "$aapt2 compile --dir $source/res -o $source/compiled && "
        val linkCommand =
            "$aapt2 link -o $outputDir/$name -I $FRAMEWORK_DIR --manifest $source/AndroidManifest.xml $source/compiled/* --auto-add-overlay"

        return folderCommand + compileCommand + linkCommand
    }

    fun zipAlign(source: String): Boolean {
        val fileName = CompilerUtils.getOverlayName(source)
        val result =
            Shell.cmd(
                zipalign + " 4 " + source + ' ' + Resources.UNSIGNED_DIR + "/" + fileName + "-unsigned.apk"
            ).exec()

        if (result.isSuccess) Log.i(
            "$TAG - ZipAlign",
            "Successfully zip aligned $fileName"
        ) else {
            Log.e(
                "$TAG - ZipAlign",
                "Failed to zip align $fileName\n${java.lang.String.join("\n", result.out)}"
            )
            writeLog("$TAG - ZipAlign", "Failed to zip align $fileName", result.out)
        }

        return !result.isSuccess
    }

    fun apkSigner(source: String): Boolean {
        var fileName: String? = "null"

        try {
            if (key == null) {
                key =
                    CryptoUtils.readPrivateKey(appContext.assets.open("Keystore/testkey.pk8"))
            }
            if (cert == null) {
                cert =
                    CryptoUtils.readCertificate(appContext.assets.open("Keystore/testkey.x509.pem"))
            }

            fileName = CompilerUtils.getOverlayName(source)

            SignAPK.sign(
                cert, key, source, Resources.SIGNED_DIR + "/IconifyComponent" + fileName + ".apk"
            )

            Log.i("$TAG - APKSigner", "Successfully signed $fileName")
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            writeLog("$TAG - APKSigner", "Failed to sign $fileName", e)
            return true
        }

        return false
    }
}
