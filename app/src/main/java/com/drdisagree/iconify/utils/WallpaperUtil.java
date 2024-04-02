package com.drdisagree.iconify.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.ParcelFileDescriptor;

import java.io.ByteArrayOutputStream;

public class WallpaperUtil {

    public static Bitmap getCompressedWallpaper(Context context, int quality) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
            ParcelFileDescriptor wallpaperFile = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);

            if (wallpaperFile == null) {
                Drawable wallpaperDrawable = wallpaperManager.getDrawable();

                if (wallpaperDrawable instanceof BitmapDrawable) {
                    Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
                    return compressBitmap(bitmap, quality);
                } else {
                    return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                }
            } else {
                Bitmap bitmap = BitmapFactory.decodeFileDescriptor(wallpaperFile.getFileDescriptor());
                wallpaperFile.close();
                return compressBitmap(bitmap, quality);
            }
        } catch (Exception e) {
            return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        }
    }

    private static Bitmap compressBitmap(Bitmap bitmap, int quality) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream);
        byte[] byteArray = stream.toByteArray();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
    }
}