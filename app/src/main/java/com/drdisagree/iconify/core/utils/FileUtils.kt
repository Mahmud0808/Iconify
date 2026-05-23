package com.drdisagree.iconify.core.utils

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_FOLDER_NAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FileUtils {

    private const val TAG = "FileUtils"

    /**
     * Copies a file from the given [Uri] to a shared path accessible by the Xposed module
     * using the [MediaStore] API.
     *
     * This method resolves the file name (prioritizing [customFileName]), removes any existing
     * file with the same name in the target directory, and saves the content to the
     * "Download/[XPOSED_RESOURCE_FOLDER_NAME]" directory.
     *
     * @param customFileName Optional custom name for the file. If null, the name is resolved
     * from the [Uri]'s display name or path segment.
     * @return The absolute file path of the copied file if successful, or `null` if the
     * operation failed.
     */
    @SuppressLint("SetWorldReadable")
    suspend fun Uri.copyToXposedSharedPath(customFileName: String? = null):
            String? = withContext(Dispatchers.IO) {
        val context = appContext
        val resolver = context.contentResolver

        try {
            val fileName = customFileName
                ?: resolver.query(this@copyToXposedSharedPath, null, null, null, null)
                    ?.use { cursor ->
                        val col = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (col != -1 && cursor.moveToFirst()) cursor.getString(col) else null
                    } ?: lastPathSegment ?: "unknown_file"

            val relativePath = "Download/$XPOSED_RESOURCE_FOLDER_NAME"

            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val projection = arrayOf(MediaStore.Downloads._ID)
            val selection =
                "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf(fileName, "Download/$XPOSED_RESOURCE_FOLDER_NAME%")

            resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                    val existingUri = ContentUris.withAppendedId(collection, id)
                    try {
                        resolver.delete(existingUri, null, null)
                    } catch (e: Exception) {
                        Log.e(TAG, "Could not delete existing file: $existingUri", e)
                    }
                }
            }

            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val itemUri = resolver.insert(collection, values) ?: return@withContext null

            resolver.openOutputStream(itemUri, "wt")?.use { output ->
                resolver.openInputStream(this@copyToXposedSharedPath)?.use { input ->
                    input.copyTo(output)
                }
            }

            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(itemUri, values, null, null)

            resolver.query(itemUri, arrayOf(MediaStore.Downloads.DATA), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) cursor.getString(0) else null
                }
        } catch (e: Exception) {
            Log.e("FilePicker", "toXposedSharedPath failed", e)
            null
        }
    }

    /**
     * Call this method only from SystemUI process.
     * 
     * Copies a file to the "Download/Iconify" directory using the MediaStore API.
     * This method ensures that any existing file with the same name in the destination
     * directory is replaced.
     *
     * @param sourceFile The local [File] to be copied.
     * @param customFileName An optional custom name for the file in the destination directory.
     * If null, the name of the [sourceFile] will be used.
     * @return `true` if the file was successfully copied and registered in the MediaStore, `false` otherwise.
     */
    fun copyToIconifyDir(
        sourceFile: File,
        customFileName: String? = null
    ): Boolean {
        return try {
            val resolver = appContext.contentResolver

            val fileName = customFileName ?: sourceFile.name
            val relativePath = "Download/Iconify" // MediaStore RELATIVE_PATH

            // Delete existing file with same name
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            val projection = arrayOf(MediaStore.Downloads._ID)
            val selection =
                "${MediaStore.Downloads.DISPLAY_NAME} = ? AND ${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf(fileName, "$relativePath%")

            resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                    val existingUri = ContentUris.withAppendedId(collection, id)
                    try {
                        resolver.delete(existingUri, null, null)
                    } catch (_: Exception) {
                        /* ignore */
                    }
                }
            }

            // Insert new file
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.RELATIVE_PATH, relativePath)
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val itemUri = resolver.insert(collection, values) ?: return false

            // Copy bytes
            resolver.openOutputStream(itemUri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }

            // Mark as ready
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(itemUri, values, null, null)

            true
        } catch (e: Exception) {
            Log.e("FileCopy", "Failed to copy to Iconify folder", e)
            false
        }
    }

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

    fun File.ensureRw(executable: Boolean = false) {
        if (!exists()) return

        setReadable(true, true)
        setWritable(true, true)
        setExecutable(executable, true)

        try {
            val mode = when {
                executable -> 0b111_000_000
                else -> 0b110_000_000
            }
            Os.chmod(absolutePath, mode)
        } catch (_: ErrnoException) {
        }
    }
}
