package com.drdisagree.iconify.common

import android.os.Environment
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext

object Resources {

    // Preference files
    const val SHARED_PREFERENCES = BuildConfig.APPLICATION_ID
    const val SHARED_XPREFERENCES = BuildConfig.APPLICATION_ID + "_xpreference"

    // Storage location
    val DOCUMENTS_DIR: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath

    val DOWNLOADS_DIR: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    val LOG_DIR = "$DOCUMENTS_DIR/Iconify"
    const val MODULE_DIR = "/data/adb/modules/Iconify"
    const val SYSTEM_OVERLAY_DIR = "/system/product/overlay"

    val DATA_DIR: String = appContext.filesDir.absolutePath
    const val OVERLAY_DIR = "$MODULE_DIR/system/product/overlay"

    val BIN_DIR = appContext.dataDir.toString() + "/bin"

    val BACKUP_DIR = Environment.getExternalStorageDirectory().absolutePath + "/.iconify_backup"

    val TEMP_DIR = Environment.getExternalStorageDirectory().absolutePath + "/.iconify"

    val TEMP_MODULE_DIR = "$TEMP_DIR/Iconify"

    val TEMP_MODULE_OVERLAY_DIR = "$TEMP_MODULE_DIR/system/product/overlay"

    val TEMP_OVERLAY_DIR = "$TEMP_DIR/overlays"

    val TEMP_CACHE_DIR = "$TEMP_OVERLAY_DIR/cache"

    val UNSIGNED_UNALIGNED_DIR = "$TEMP_OVERLAY_DIR/unsigned_unaligned"

    val UNSIGNED_DIR = "$TEMP_OVERLAY_DIR/unsigned"

    val SIGNED_DIR = "$TEMP_OVERLAY_DIR/signed"

    val COMPANION_TEMP_DIR = "$TEMP_DIR/companion"

    val COMPANION_COMPILED_DIR = "$COMPANION_TEMP_DIR/compiled"

    val COMPANION_MODULE_DIR = "$TEMP_DIR/module/IconifyCompanion"
    private val COMPANION_RES_DIR = "$COMPANION_MODULE_DIR/substratumXML/SystemUI/res"

    val COMPANION_DRAWABLE_DIR = "$COMPANION_RES_DIR/drawable"

    val COMPANION_LAYOUT_DIR = "$COMPANION_RES_DIR/layout"

    // File resources
    const val FRAMEWORK_DIR = "/system/framework/framework-res.apk"

    // Xposed resource dir
    val XPOSED_RESOURCE_TEMP_DIR =
        "${Environment.getExternalStorageDirectory()}/.iconify_files"

    val LSCLOCK_FONT_DIR = "$XPOSED_RESOURCE_TEMP_DIR/lsclock_font.ttf"

    val HEADER_CLOCK_FONT_DIR = "$XPOSED_RESOURCE_TEMP_DIR/headerclock_font.ttf"

    val HEADER_IMAGE_DIR = "$XPOSED_RESOURCE_TEMP_DIR/header_image.png"

    val DEPTH_WALL_FG_DIR = "$XPOSED_RESOURCE_TEMP_DIR/depth_wallpaper_fg.png"

    val DEPTH_WALL_BG_DIR = "$XPOSED_RESOURCE_TEMP_DIR/depth_wallpaper_bg.png"

    // Resource names
    const val HEADER_CLOCK_LAYOUT = "preview_header_clock_"
    const val LOCKSCREEN_CLOCK_LAYOUT = "preview_lockscreen_clock_"
    const val LOCKSCREEN_CLOCK_LOTTIE = "lottie_lockscreen_clock_"
}
