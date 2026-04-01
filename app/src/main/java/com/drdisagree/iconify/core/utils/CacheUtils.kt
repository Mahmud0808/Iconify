package com.drdisagree.iconify.core.utils

import android.content.ContentUris
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_FOLDER_NAME
import java.io.File

object CacheUtils {

    fun clearCache(context: Context) {
        try {
            var dir = context.cacheDir
            deleteDir(dir)

            dir = context.externalCacheDir
            deleteDir(dir)

            dir = context.filesDir
            deleteDir(dir)

            clearIconifyDownloadDir(context)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory) {
            val children = dir.list() ?: return false

            for (child in children) {
                val success = deleteDir(File(dir, child))

                if (!success) {
                    return false
                }
            }

            dir.delete()
        } else if (dir != null && dir.isFile) {
            dir.delete()
        } else {
            false
        }
    }

    private fun clearIconifyDownloadDir(context: Context) {
        try {
            val resolver = context.contentResolver
            val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

            val projection = arrayOf(MediaStore.Downloads._ID)
            val selection = "${MediaStore.Downloads.RELATIVE_PATH} LIKE ?"
            val selectionArgs = arrayOf("Download/$XPOSED_RESOURCE_FOLDER_NAME/%")

            resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idCol)
                    val uri = ContentUris.withAppendedId(collection, id)
                    try {
                        resolver.delete(uri, null, null)
                    } catch (e: Exception) {
                        Log.e("Cache", "Failed to delete uri: $uri", e)
                    }
                }
            }

            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                XPOSED_RESOURCE_FOLDER_NAME
            )
            dir.deleteRecursively() // handles non-empty too, in case MediaStore missed anything
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}