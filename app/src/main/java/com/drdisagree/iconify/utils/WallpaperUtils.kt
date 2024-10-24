package com.drdisagree.iconify.utils

import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

object WallpaperUtils {

    fun loadWallpaper(context: Context, isLockscreen: Boolean): Deferred<Bitmap?> {
        return CoroutineScope(Dispatchers.Main).async {
            withContext(Dispatchers.IO) {
                getCompressedWallpaper(
                    context,
                    80,
                    if (isLockscreen) {
                        WallpaperManager.FLAG_LOCK
                    } else {
                        WallpaperManager.FLAG_SYSTEM
                    }
                )
            }
        }
    }

    fun getCompressedWallpaper(context: Context?, quality: Int, which: Int): Bitmap? {
        return try {
            val wallpaperManager = WallpaperManager.getInstance(context)
            val wallpaperFile = wallpaperManager.getWallpaperFile(which)

            if (wallpaperFile == null) {
                val wallpaperDrawable = wallpaperManager.drawable

                if (wallpaperDrawable is BitmapDrawable) {
                    val bitmap = wallpaperDrawable.bitmap
                    compressBitmap(bitmap, quality)
                } else {
                    Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                }
            } else {
                val bitmap = BitmapFactory.decodeFileDescriptor(wallpaperFile.fileDescriptor)
                wallpaperFile.close()
                compressBitmap(bitmap, quality)
            }
        } catch (e: Exception) {
            //            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
            null
        }
    }

    private fun compressBitmap(bitmap: Bitmap, quality: Int): Bitmap {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        val byteArray = stream.toByteArray()

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
}