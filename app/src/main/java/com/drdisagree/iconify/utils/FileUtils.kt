package com.drdisagree.iconify.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.topjohnwu.superuser.Shell
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.min

object FileUtils {

    val DATA_DIR = appContext.filesDir.toString()

    @Throws(IOException::class)
    fun copyAssets(assetFolder: String) {
        cleanDir(assetFolder)
        createDir(assetFolder)
        copyFileOrDirectory(appContext, assetFolder, "$DATA_DIR/$assetFolder")
    }

    fun cleanDir(dirName: String) {
        Shell.cmd("rm -rf $DATA_DIR/$dirName").exec()
    }

    private fun createDir(dirName: String) {
        val newFolder = File("$DATA_DIR/$dirName/")
        newFolder.mkdirs()
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
            } catch (e: IOException) {
                File(outFileName).mkdir()
                copyFileOrDirectory(context, inFileName, outFileName)
            }
        }
    }

    private fun closeQuietly(autoCloseable: AutoCloseable?) {
        try {
            autoCloseable?.close()
        } catch (ignored: Exception) {
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

    @SuppressLint("Recycle")
    private fun getRealPathFromURI(uri: Uri?): String? {
        val file: File
        try {
            val returnCursor =
                appContext.contentResolver.query(
                    uri!!, null, null, null, null
                ) ?: return null
            val nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            returnCursor.moveToFirst()

            val name = returnCursor.getString(nameIndex)
            file = File(appContext.filesDir, name)

            val inputStream =
                appContext.contentResolver.openInputStream(
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

    fun moveToIconifyHiddenDir(source: String, destination: String): Boolean {
        return Shell.cmd(
            "mkdir -p " + Resources.XPOSED_RESOURCE_TEMP_DIR,
            "rm -f \"$destination\"",
            "mv -f \"$source\" \"$destination\""
        ).exec().isSuccess
    }

    fun launchFilePicker(
        context: Context,
        type: String?,
        launcher: ActivityResultLauncher<Intent?>
    ) {
        if (!hasStoragePermission()) {
            requestStoragePermission(context)
        } else {
            var fileType = "*/*"

            if (type.isNullOrEmpty() || type == "all") {
                fileType = "*/*"
            } else if (type == "image") {
                fileType = "image/*"
            } else if (type == "font") {
                fileType = "font/*"
            } else if (type == "video") {
                fileType = "video/*"
            } else if (type == "audio") {
                fileType = "audio/*"
            } else if (type == "pdf") {
                fileType = "application/pdf"
            } else if (type == "text") {
                fileType = "text/*"
            } else if (type == "zip") {
                fileType = "application/zip"
            } else if (type == "apk") {
                fileType = "application/vnd.android.package-archive"
            }

            launchFilePicker(launcher, fileType)
        }
    }

    fun launchFilePicker(launcher: ActivityResultLauncher<Intent?>, type: String?) {
        val chooseFile = Intent(Intent.ACTION_GET_CONTENT)
        chooseFile.addCategory(Intent.CATEGORY_OPENABLE)
        chooseFile.setType(type)
        launcher.launch(chooseFile)
    }
}
