package com.drdisagree.iconify.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RawRes
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.core.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.core.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.topjohnwu.superuser.Shell
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.min

object FileUtils {

    private const val TAG = "FileUtils"

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
            Shell.cmd("mkdir -p $DATA_DIR/$dirName").exec()
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
    fun copyAndClose(input: InputStream, output: OutputStream) {
        copy(input, output)
        closeQuietly(input)
        closeQuietly(output)
    }

    @Throws(IOException::class)
    fun copy(input: InputStream, output: OutputStream) {
        val buffer = ByteArray(1024)
        var n: Int

        while (-1 != input.read(buffer).also { n = it }) {
            output.write(buffer, 0, n)
        }
    }

    fun getRealPath(obj: Any?): String? {
        return when (obj) {
            is Intent -> {
                getRealPathFromURI(obj.data)
            }

            is Uri -> {
                getRealPathFromURI(obj as Uri?)
            }

            else -> {
                throw IllegalArgumentException("Object must be an Intent or Uri")
            }
        }
    }

    @SuppressLint("Recycle", "UnsanitizedFilenameFromContentProvider")
    private fun getRealPathFromURI(uri: Uri?): String? {
        val file: File
        try {
            val returnCursor = appContext.contentResolver.query(
                uri!!, null, null, null, null
            ) ?: return null
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()

            val name = returnCursor.getString(nameIndex)
            file = File(appContext.filesDir, name)

            val inputStream = appContext.contentResolver.openInputStream(
                uri
            )
            val outputStream = FileOutputStream(file)
            var read: Int
            val maxBufferSize = 1024 * 1024

            if (inputStream == null) return null

            val bytesAvailable = inputStream.available()
            val bufferSize = min(bytesAvailable.toDouble(), maxBufferSize.toDouble()).toInt()
            val buffers = ByteArray(bufferSize)

            while (inputStream.read(buffers).also { read = it } != -1) {
                outputStream.write(buffers, 0, read)
            }

            inputStream.close()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }

        return file.path
    }

    fun moveToIconifyHiddenDir(sourcePath: String, destinationPath: String): Boolean {
        try {
            val source = File(sourcePath)
            val destination = File(destinationPath)

            destination.parentFile?.let { parent ->
                ensureDirs(parent.absolutePath)
            }

            var moved = source.renameTo(destination)
            if (!moved) {
                val copiedFile = source.copyTo(destination, overwrite = true)
                moved = copiedFile.exists() && copiedFile.length() == source.length()
                source.delete()

                if (!moved) {
                    Log.e(TAG, "Failed to move file from $sourcePath to $destinationPath")
                    return false
                }
            }

            Log.d(TAG, "Successfully moved $sourcePath to $destinationPath")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error moving file", e)
            return false
        }
    }

    fun launchFilePicker(
        context: Context,
        type: String?,
        launcher: ActivityResultLauncher<Intent?>
    ) {
        if (!hasStoragePermission()) {
            requestStoragePermission(context)
        } else {
            val fileType = when (type) {
                "image" -> "image/*"
                "font" -> "font/*"
                "video" -> "video/*"
                "audio" -> "audio/*"
                "pdf" -> "application/pdf"
                "text" -> "text/*"
                "zip" -> "application/zip"
                "apk" -> "application/vnd.android.package-archive"
                else -> "*/*"
            }

            launchFilePicker(launcher, fileType)
        }
    }

    fun launchFilePicker(launcher: ActivityResultLauncher<Intent?>, type: String?) {
        launcher.launch(
            Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                setType(type)
            }
        )
    }

    fun readJsonFileFromAssets(fileName: String): String {
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

    fun ensureDirs(vararg paths: String) {
        paths.forEach { path ->
            val dir = File(path)

            if (dir.exists() && !dir.isDirectory) {
                dir.delete()
            }

            if (!dir.exists()) {
                val created = dir.mkdirs()
                Log.d(TAG, "Created ${dir.absolutePath}: $created")
            }

            Log.d(TAG, "Writable ${dir.absolutePath}: ${dir.canWrite()}")
        }
    }
}
