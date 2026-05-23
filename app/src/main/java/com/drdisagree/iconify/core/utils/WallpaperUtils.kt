package com.drdisagree.iconify.core.utils

import android.util.Log
import com.drdisagree.iconify.data.common.Resources.WALLPAPER_DIR
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object WallpaperUtils {

    private const val TAG = "WallpaperUtils"

    suspend fun prepareLockWallpaper(): File? {
        val targetFile = File(
            WALLPAPER_DIR.also { FileUtils.ensureDirs(it) },
            "wallpaper_lock"
        )

        val systemFile1 = File("/data/system/users/0/wallpaper_lock")
        val systemFile2 = File("/data/system/users/0/wallpaper")

        fun hashViaShell(path: String): String? {
            fun run(cmd: String): String? {
                return try {
                    val result = Shell.cmd(cmd).exec()

                    if (result.code != 0 || result.out.isEmpty()) {
                        null
                    } else {
                        result.out.first().trim().split(Regex("\\s+")).firstOrNull()
                    }
                } catch (_: Exception) {
                    null
                }
            }

            run("sha256sum \"$path\"")?.let { return it }
            run("md5sum \"$path\"")?.let { return it }

            return null
        }

        return withContext(Dispatchers.IO) {
            try {
                when {
                    RootUtils.fileExists(targetFile.absolutePath) -> {
                        val systemFile = when {
                            RootUtils.fileExists(systemFile1.absolutePath) -> systemFile1
                            RootUtils.fileExists(systemFile2.absolutePath) -> systemFile2
                            else -> return@withContext null
                        }

                        val targetHash = hashViaShell(targetFile.absolutePath)
                        val systemHash = hashViaShell(systemFile.absolutePath)

                        if (targetHash == null || systemHash == null || targetHash != systemHash) {
                            Shell.cmd(
                                "cp \"${systemFile.absolutePath}\" \"${targetFile.absolutePath}\" && chmod 644 \"${targetFile.absolutePath}\""
                            ).exec()
                        }
                    }

                    RootUtils.fileExists(systemFile1.absolutePath) -> {
                        Shell.cmd(
                            "cp \"${systemFile1.absolutePath}\" \"${targetFile.absolutePath}\" && chmod 644 \"${targetFile.absolutePath}\""
                        ).exec()
                    }

                    RootUtils.fileExists(systemFile2.absolutePath) -> {
                        Shell.cmd(
                            "cp \"${systemFile2.absolutePath}\" \"${targetFile.absolutePath}\" && chmod 644 \"${targetFile.absolutePath}\""
                        ).exec()
                    }

                    else -> return@withContext null
                }

                if (targetFile.exists()) targetFile else null
            } catch (e: Exception) {
                Log.e(TAG, "Exception while preparing wallpaper", e)
                null
            }
        }
    }
}