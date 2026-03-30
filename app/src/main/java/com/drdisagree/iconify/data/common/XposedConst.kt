package com.drdisagree.iconify.data.common

import android.os.Environment
import com.drdisagree.iconify.BuildConfig
import java.io.File

object XposedConst {

    const val PREF_FILE_NAME = "${BuildConfig.APPLICATION_ID}.preferences"
    const val WEATHER_PREF_FILE_NAME = "${BuildConfig.APPLICATION_ID}.weather.preferences"
    const val XPOSED_RESOURCE_FOLDER_NAME = "Iconify"

    val XPOSED_RESOURCE_DIR: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            XPOSED_RESOURCE_FOLDER_NAME
        )

    val LSCLOCK_FONT_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "lsclock_font.ttf")

    val LSCLOCK_IMAGE1_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "lsclock_image1.png")

    val LSCLOCK_IMAGE2_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "lsclock_image2.png")

    val HEADER_CLOCK_FONT_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "headerclock_font.ttf")

    val HEADER_IMAGE_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "header_image.png")

    val DEPTH_WALL_FG_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "depth_wallpaper_fg.png")

    val DEPTH_WALL_BG_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "depth_wallpaper_bg.png")

    val LOCKSCREEN_WEATHER_FONT_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "lockscreen_weather_font.ttf")

    val STATUSBAR_LOGO_FILE: File
        get() = File(XPOSED_RESOURCE_DIR, "statusbar_logo.png")
}
