package com.drdisagree.iconify.utils

import android.content.Context
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
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteDir(dir: File?): Boolean {
        return if (dir != null && dir.isDirectory()) {
            val children = dir.list() ?: return false

            for (child in children) {
                val success = deleteDir(File(dir, child))

                if (!success) {
                    return false
                }
            }

            dir.delete()
        } else if (dir != null && dir.isFile()) {
            dir.delete()
        } else {
            false
        }
    }
}
