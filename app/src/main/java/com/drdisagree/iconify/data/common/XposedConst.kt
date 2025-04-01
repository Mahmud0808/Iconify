package com.drdisagree.iconify.data.common

import android.os.Environment
import java.io.File

object XposedConst {

    // Xposed resource dir
    val XPOSED_RESOURCE_TEMP_DIR: File
        get() = File(Environment.getExternalStorageDirectory(), ".iconify_files")
    val LSCLOCK_FONT_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "lsclock_font.ttf")
    val LSCLOCK_IMAGE1_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "lsclock_image1.png")
    val LSCLOCK_IMAGE2_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "lsclock_image2.png")
    val HEADER_CLOCK_FONT_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "headerclock_font.ttf")
    val HEADER_IMAGE_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "header_image.png")
    val DEPTH_WALL_FG_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "depth_wallpaper_fg.png")
    val DEPTH_WALL_BG_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "depth_wallpaper_bg.png")
    val LOCKSCREEN_WEATHER_FONT_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "lockscreen_weather_font.ttf")
    val STATUSBAR_LOGO_FILE: File
        get() = File(XPOSED_RESOURCE_TEMP_DIR, "statusbar_logo.png")
}
