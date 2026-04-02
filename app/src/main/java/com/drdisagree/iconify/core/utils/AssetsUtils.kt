package com.drdisagree.iconify.core.utils

import android.content.Context
import android.util.Log
import androidx.annotation.RawRes
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.topjohnwu.superuser.Shell
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths

object AssetsUtils {

    private const val TAG = "AssetsUtils"

    @Throws(IOException::class)
    fun copyAssets(assetFolder: String) {
        cleanAssetsDir(assetFolder)
        createDir(assetFolder)
        copyFileOrDirectory(appContext, assetFolder, "$DATA_DIR/$assetFolder")
    }

    fun cleanAssetsDir(dirName: String) {
        Shell.cmd("rm -rf $DATA_DIR/$dirName").exec()
    }

    private fun createDir(dirName: String) {
        val dir = File("$DATA_DIR/$dirName/")

        if (dir.exists() && !dir.isDirectory) {
            dir.delete()
        }

        if (!dir.exists()) {
            val created = dir.mkdirs()
            Log.d(TAG, "Created ${dir.absolutePath}: $created")
        }

        Log.d(TAG, "Writable ${dir.absolutePath}: ${dir.canWrite()}")

        if (!dir.exists() || !dir.canWrite()) {
            Shell.cmd(
                "rm -rf $DATA_DIR/$dirName && mkdir -p $DATA_DIR/$dirName && chmod 644 $DATA_DIR/$dirName"
            ).exec()
        }
    }

    @Throws(IOException::class)
    private fun copyFileOrDirectory(context: Context, dirName: String, outPath: String) {
        val srcFiles = context.assets.list(dirName) ?: return

        for (srcFileName in srcFiles) {
            val outFileName = outPath + File.separator + srcFileName
            var inFileName = dirName + File.separator + srcFileName

            if (dirName == "") {
                inFileName = srcFileName
            }

            try {
                val inputStream = context.assets.open(inFileName)
                copyAndClose(inputStream, Files.newOutputStream(Paths.get(outFileName)))
            } catch (_: IOException) {
                File(outFileName).mkdir()
                copyFileOrDirectory(context, inFileName, outFileName)
            }
        }
    }

    private fun closeQuietly(autoCloseable: AutoCloseable?) {
        try {
            autoCloseable?.close()
        } catch (_: Exception) {
        }
    }

    @Throws(IOException::class)
    private fun copyAndClose(input: InputStream, output: OutputStream) {
        copy(input, output)
        closeQuietly(input)
        closeQuietly(output)
    }

    @Throws(IOException::class)
    private fun copy(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var n: Int

        while (-1 != input.read(buffer).also { n = it }) {
            output.write(buffer, 0, n)
        }
    }

    fun readJson(fileName: String): String {
        val stringBuilder = StringBuilder()
        val inputStream = appContext.assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()
        return stringBuilder.toString()
    }

    fun readRawResource(@RawRes resId: Int): String = appContext
        .resources
        .openRawResource(resId)
        .bufferedReader()
        .use { it.readText() }
}
