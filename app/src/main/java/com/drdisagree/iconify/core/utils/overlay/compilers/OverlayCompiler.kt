package com.drdisagree.iconify.core.utils.overlay.compilers

import android.util.Log
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.core.utils.AppUtils.getSplitLocations
import com.drdisagree.iconify.core.utils.Logger.writeLog
import com.drdisagree.iconify.core.utils.apksigner.CryptoUtils
import com.drdisagree.iconify.core.utils.apksigner.SignAPK
import com.drdisagree.iconify.data.common.Dynamic.AAPT2
import com.drdisagree.iconify.data.common.Dynamic.ZIPALIGN
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.common.Resources.FRAMEWORK_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.topjohnwu.superuser.Shell
import java.io.File
import java.security.PrivateKey
import java.security.cert.X509Certificate

object OverlayCompiler {

    private val TAG = OverlayCompiler::class.java.simpleName
    private val aapt2: String = AAPT2.absolutePath
    private val zipalign: String = ZIPALIGN.absolutePath
    private var key: PrivateKey? = null
    private var cert: X509Certificate? = null

    fun createManifest(overlayName: String?, targetPackage: String?, sourceDir: String): Boolean {
        return try {
            val content = CompilerUtils.createManifestContent(overlayName, targetPackage)
            File("$sourceDir/AndroidManifest.xml").writeText(content)
            Log.i("$TAG - Manifest", "Successfully created manifest for $overlayName")
            false
        } catch (e: Exception) {
            val output = listOf(e.message ?: e.cause?.toString() ?: e.toString())
            Log.e("$TAG - Manifest", "Failed to create manifest for $overlayName\n${e.message}")
            writeLog(
                "$TAG - Manifest",
                "Failed to create manifest for $overlayName",
                output
            )
            CompilerErrorStore.record(
                CompilerFailure(
                    stage = "Manifest",
                    target = overlayName ?: "overlay",
                    message = "Failed to create manifest for $overlayName",
                    output = output
                )
            )
            true
        }
    }

    fun runAapt(source: String, targetPackage: String?): Boolean {
        val name = CompilerUtils.getOverlayName(source) + "-unsigned-unaligned.apk"
        val aaptCommand = buildAAPT2Command(source, name)
        val splitLocations = getSplitLocations(targetPackage)

        for (targetApk in splitLocations) {
            aaptCommand.append(" -I ").append(targetApk)
        }

        return runAaptCommand(aaptCommand.toString(), source, name) != null
    }

    /**
     * Runs a single aapt2 compile+link [command] (with the colorSurfaceHeader
     * self-heal retry), capturing stderr and recording a [CompilerFailure] on
     * failure. Shared between [runAapt] and OnboardingCompiler so the
     * stderr-capture / retry / logging logic lives in exactly one place.
     *
     * @return null on success, or the [CompilerFailure] describing the error.
     */
    internal fun runAaptCommand(command: String, source: String, name: String): CompilerFailure? {
        // Wrap the whole chain so 2>&1 captures stderr from compile AND link;
        // libsu does not collect stderr by default and a bare trailing "2>&1"
        // would only bind to the last command in the chain.
        var result = Shell.cmd("{ $command ; } 2>&1").exec()

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
                result = Shell.cmd("{ $command ; } 2>&1").exec()
            }
        }

        if (result.isSuccess) {
            Log.i("$TAG - AAPT", "Successfully built APK for $name")
            return null
        }

        val errorOutput = errorOutputOf(result)

        Log.e(
            "$TAG - AAPT",
            "Failed to build APK for $name\n${errorOutput.joinToString("\n")}"
        )

        val fileContents = Shell.cmd(
            $$"find $$source/res -type f -name '*.xml' -exec sh -c 'echo \"===== $1 =====\"; cat \"$1\"; echo' sh {} \\;"
        ).exec().out

        writeLog(
            tag = "$TAG - AAPT",
            header = "Failed to build APK for $name",
            command = command,
            fileContents = fileContents,
            errorLog = errorOutput
        )

        return CompilerFailure(
            stage = "AAPT",
            target = name,
            message = "Failed to build APK for $name",
            command = command,
            output = errorOutput.filterNotNull()
        ).also { CompilerErrorStore.record(it) }
    }

    private fun buildAAPT2Command(source: String, name: String): StringBuilder {
        val outputDir = Resources.UNSIGNED_UNALIGNED_DIR

        return StringBuilder(getAAPT2Command(source, name, outputDir))
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
                "rm -rf $UNSIGNED_DIR/$fileName-unsigned.apk",
                "{ $zipalign 4 $source $UNSIGNED_DIR/$fileName-unsigned.apk ; } 2>&1"
            ).exec()

        if (result.isSuccess) Log.i(
            "$TAG - ZipAlign",
            "Successfully zip aligned $fileName"
        ) else {
            val errorOutput = errorOutputOf(result)

            Log.e(
                "$TAG - ZipAlign",
                "Failed to zip align $fileName\n${errorOutput.joinToString("\n")}"
            )
            writeLog("$TAG - ZipAlign", "Failed to zip align $fileName", errorOutput)
            CompilerErrorStore.record(
                CompilerFailure(
                    stage = "ZipAlign",
                    target = fileName,
                    message = "Failed to zip align $fileName",
                    output = errorOutput.filterNotNull()
                )
            )
        }

        return !result.isSuccess
    }

    fun apkSigner(source: String): Boolean {
        var fileName: String? = "null"

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

            fileName = CompilerUtils.getOverlayName(source)

            SignAPK.sign(
                cert,
                key,
                source,
                Resources.SIGNED_DIR + "/IconifyComponent" + fileName + ".apk"
            )

            Log.i("$TAG - APKSigner", "Successfully signed $fileName")
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
            writeLog("$TAG - APKSigner", "Failed to sign $fileName", e)
            CompilerErrorStore.record(
                CompilerFailure(
                    stage = "Sign",
                    target = fileName ?: "overlay",
                    message = "Failed to sign $fileName",
                    output = listOf(e.message ?: e.toString())
                )
            )
            return true
        }

        return false
    }

    /**
     * Runs the full compile ladder for a single overlay:
     * manifest -> aapt -> zipalign -> sign.
     *
     * This is the sequence every compiler repeated verbatim. Each step already
     * logs and records its own [CompilerFailure]; this just chains them and
     * stops at the first failure.
     *
     * @param overlayName bare overlay name (e.g. "SIP1", "CR1", "Dynamic2").
     * @param targetPackage package the overlay targets.
     * @param source overlay working directory (containing res/ + manifest).
     * @return true if any step failed (matching the legacy "true == error").
     */
    fun buildOverlayApk(overlayName: String, targetPackage: String?, source: String): Boolean {
        if (createManifest(overlayName, targetPackage, source)) {
            Log.e(TAG, "Failed to create Manifest for $overlayName! Exiting...")
            return true
        }

        if (runAapt(source, targetPackage)) {
            Log.e(TAG, "Failed to build $overlayName! Exiting...")
            return true
        }

        if (zipAlign("${Resources.UNSIGNED_UNALIGNED_DIR}/$overlayName-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align $overlayName-unsigned-unaligned.apk! Exiting...")
            return true
        }

        if (apkSigner("$UNSIGNED_DIR/$overlayName-unsigned.apk")) {
            Log.e(TAG, "Failed to sign $overlayName-unsigned.apk! Exiting...")
            return true
        }

        return false
    }

    /**
     * Picks the meaningful output of a shell result: prefer stdout (which holds
     * everything once a command is wrapped with `2>&1`), falling back to stderr
     * only when stdout is entirely blank.
     */
    private fun errorOutputOf(result: Shell.Result): List<String?> =
        result.out.takeIf { lines -> lines.any { !it.isNullOrBlank() } }
            ?: result.err.takeIf { lines -> lines.any { !it.isNullOrBlank() } }
            ?: listOf("Process exited with code ${result.code} without any output")
}
