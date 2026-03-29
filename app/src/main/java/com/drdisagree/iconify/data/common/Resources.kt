package com.drdisagree.iconify.data.common

import android.os.Environment
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.core.utils.FileUtils

object Resources {

    // Storage location
    val DOCUMENTS_DIR: String
        get() = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            .absolutePath
            .also { FileUtils.ensureDirs(it) }

    val DOWNLOADS_DIR: String
        get() = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .absolutePath
            .also { FileUtils.ensureDirs(it) }

    private val BASE_DIR: String
        get() = appContext
            .getExternalFilesDir(null)!!
            .absolutePath
            .also { FileUtils.ensureDirs(it) }

    val LOG_DIR: String
        get() = "$DOCUMENTS_DIR/Iconify"

    val BACKUP_DIR: String
        get() = "$BASE_DIR/.iconify_backup"

    val WALLPAPER_DIR: String
        get() = "$BASE_DIR/.wallpapers"

    val TEMP_DIR: String
        get() = "$BASE_DIR/.iconify"

    val TEMP_MODULE_DIR: String
        get() = "$TEMP_DIR/Iconify"

    val TEMP_MODULE_OVERLAY_DIR: String
        get() = "$TEMP_MODULE_DIR/system/product/overlay"

    val TEMP_OVERLAY_DIR: String
        get() = "$TEMP_DIR/overlays"

    val TEMP_CACHE_DIR: String
        get() = "$TEMP_OVERLAY_DIR/cache"

    val UNSIGNED_UNALIGNED_DIR: String
        get() = "$TEMP_OVERLAY_DIR/unsigned_unaligned"

    val UNSIGNED_DIR: String
        get() = "$TEMP_OVERLAY_DIR/unsigned"

    val SIGNED_DIR: String
        get() = "$TEMP_OVERLAY_DIR/signed"

    const val MODULE_DIR = "/data/adb/modules/Iconify"
    const val SYSTEM_OVERLAY_DIR = "/system/product/overlay"
    const val OVERLAY_DIR = "$MODULE_DIR/system/product/overlay"

    // File resources
    const val FRAMEWORK_DIR = "/system/framework/framework-res.apk"

    // Resource names
    const val HEADER_CLOCK_LAYOUT = "preview_header_clock_"
    const val LOCKSCREEN_CLOCK_LAYOUT = "preview_lockscreen_clock_"

    // Database
    const val DYNAMIC_RESOURCE_DATABASE_NAME = "dynamic_resource_database"
    const val DYNAMIC_RESOURCE_TABLE = "dynamic_resource_table"
}
