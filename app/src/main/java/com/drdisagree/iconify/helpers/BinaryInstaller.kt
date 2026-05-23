package com.drdisagree.iconify.helpers

import android.annotation.SuppressLint
import android.util.Log
import com.drdisagree.iconify.data.common.Dynamic.AAPT2
import com.drdisagree.iconify.data.common.Dynamic.BIN_DIR
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Dynamic.ZIPALIGN
import com.drdisagree.iconify.core.utils.AbiUtils
import com.drdisagree.iconify.core.utils.AssetsUtils
import com.topjohnwu.superuser.Shell
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

object BinaryInstaller {

    private val TAG = BinaryInstaller::class.java.simpleName

    fun symLinkBinaries(): Boolean {
        if (AAPT2.exists() && ZIPALIGN.exists()) {
            val aapt2Valid = Shell.cmd("${AAPT2.absolutePath} version")
                .exec()
                .isSuccess
            val zipalignValid = Shell.cmd("${ZIPALIGN.absolutePath} --version 2>&1; echo $?")
                .exec()
                .isSuccess

            if (aapt2Valid && zipalignValid) {
                Log.d(TAG, "Binaries already installed and functional, skipping.")
                return false
            }

            Log.d(TAG, "Binaries exist but are not functional, reinstalling...")
        }

        return extractTools()
    }

    @SuppressLint("SetWorldReadable")
    private fun extractTools(): Boolean {
        Log.d(TAG, "Extracting tools...")

        val abi = AbiUtils.getSupportedAbi() ?: run {
            Log.e(TAG, "No supported ABI found, skipping extraction.")
            return true
        }

        var hasErroredOut = false

        try {
            // Copy Tools folder (containing jniLibs.zip) from assets to DATA_DIR/Tools/
            AssetsUtils.copyAssets("Tools")

            val zipFile = File("$DATA_DIR/Tools", "jniLibs.zip")

            if (!zipFile.exists()) {
                Log.e(TAG, "jniLibs.zip not found at ${zipFile.absolutePath}")
                return true
            }

            // Extract only the matching ABI libs directly into DATA_DIR/bin
            val binDir = File(BIN_DIR)
            if (binDir.exists()) {
                if (!binDir.canWrite()) {
                    Shell.cmd("rm -rf ${binDir.absolutePath}").exec()
                } else {
                    binDir.deleteRecursively()
                }
            }
            binDir.mkdirs()
            Shell.cmd("ls -ld ${binDir.absolutePath}").exec()

            ZipInputStream(zipFile.inputStream().buffered()).use { zis ->
                var entry = zis.nextEntry

                while (entry != null) {
                    if (!entry.isDirectory && entry.name.startsWith("$abi/")) {
                        val libName = entry.name.substringAfterLast("/")

                        if (libName.isNotBlank()) {
                            File(BIN_DIR, libName).apply {
                                parentFile?.mkdirs()
                                Log.d(TAG, "Extracting $libName to $absolutePath")

                                BufferedOutputStream(FileOutputStream(this)).use { bos ->
                                    zis.copyTo(bos)
                                }
                                setExecutable(true, false)
                                setReadable(true, false)

                                if (!exists()) {
                                    Log.e(TAG, "Failed to extract $libName")
                                    hasErroredOut = true
                                } else {
                                    Shell.cmd("chmod 755 $absolutePath").exec()
                                }
                            }
                        }
                    }

                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            Log.d(TAG, "Extraction complete for ABI: $abi")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract tools.\n$e")
            hasErroredOut = true
        } finally {
            AssetsUtils.cleanAssetsDir("Tools")
        }

        return hasErroredOut
    }
}