package com.drdisagree.iconify.core.utils

import android.content.ContentUris
import android.content.ContentValues
import android.provider.MediaStore
import android.util.Log
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import java.io.File

object FileUtils {

    private const val TAG = "FileUtils"

    fun moveToIconifyDir(
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
}
