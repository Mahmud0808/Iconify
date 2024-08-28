package com.drdisagree.iconify.common

import android.os.Environment
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.common.Preferences.UPDATE_DETECTED
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.ui.fragments.home.Home
import com.drdisagree.iconify.ui.fragments.settings.Settings
import com.drdisagree.iconify.ui.fragments.tweaks.Tweaks
import com.drdisagree.iconify.ui.fragments.xposed.BackgroundChip
import com.drdisagree.iconify.ui.fragments.xposed.BatteryStyle
import com.drdisagree.iconify.ui.fragments.xposed.DepthWallpaper
import com.drdisagree.iconify.ui.fragments.xposed.HeaderClock
import com.drdisagree.iconify.ui.fragments.xposed.HeaderImage
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenClock
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWeather
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWidget
import com.drdisagree.iconify.ui.fragments.xposed.Others
import com.drdisagree.iconify.ui.fragments.xposed.QuickSettings
import com.drdisagree.iconify.ui.fragments.xposed.Statusbar
import com.drdisagree.iconify.ui.fragments.xposed.Themes
import com.drdisagree.iconify.ui.fragments.xposed.TransparencyBlur
import com.drdisagree.iconify.ui.fragments.xposed.VolumePanel
import com.drdisagree.iconify.ui.fragments.xposed.Xposed
import com.drdisagree.iconify.ui.models.SearchPreferenceItem
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchConfiguration
import com.drdisagree.iconify.utils.RootUtils.folderExists

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

    fun shouldShowRebootDialog() = (!getBoolean(FIRST_INSTALL) && getBoolean(UPDATE_DETECTED)) ||
            folderExists("/data/adb/modules_update/Iconify")

    val searchConfiguration = SearchConfiguration()

    private val commonFragments = arrayOf(
        SearchPreferenceItem(
            R.xml.xposed,
            R.string.navbar_xposed,
            Xposed()
        ),
        SearchPreferenceItem(
            R.xml.settings,
            R.string.navbar_settings,
            Settings()
        ),
        SearchPreferenceItem(
            R.xml.xposed_transparency_blur,
            R.string.activity_title_transparency_blur,
            TransparencyBlur()
        ),
        SearchPreferenceItem(
            R.xml.xposed_background_chip,
            R.string.activity_title_background_chip,
            BackgroundChip()
        ),
        SearchPreferenceItem(
            R.xml.xposed_quick_settings,
            R.string.activity_title_quick_settings,
            QuickSettings()
        ),
        SearchPreferenceItem(
            R.xml.xposed_themes,
            R.string.activity_title_themes,
            Themes()
        ),
        SearchPreferenceItem(
            R.xml.xposed_battery_style,
            R.string.activity_title_battery_style,
            BatteryStyle()
        ),
        SearchPreferenceItem(
            R.xml.xposed_statusbar,
            R.string.activity_title_statusbar,
            Statusbar()
        ),
        SearchPreferenceItem(
            R.xml.xposed_volume_panel,
            R.string.activity_title_volume_panel,
            VolumePanel()
        ),
        SearchPreferenceItem(
            R.xml.xposed_header_image,
            R.string.activity_title_header_image,
            HeaderImage()
        ),
        SearchPreferenceItem(
            R.xml.xposed_header_clock,
            R.string.activity_title_header_clock,
            HeaderClock()
        ),
        SearchPreferenceItem(
            R.xml.xposed_lockscreen_clock,
            R.string.activity_title_lockscreen_clock,
            LockscreenClock()
        ),
        SearchPreferenceItem(
            R.xml.xposed_lockscreen_weather,
            R.string.activity_title_lockscreen_weather,
            LockscreenWeather()
        ),
        SearchPreferenceItem(
            R.xml.xposed_lockscreen_widget,
            R.string.activity_title_lockscreen_widget,
            LockscreenWidget()
        ),
        SearchPreferenceItem(
            R.xml.xposed_depth_wallpaper,
            R.string.activity_title_depth_wallpaper,
            DepthWallpaper()
        ),
        SearchPreferenceItem(
            R.xml.xposed_others,
            R.string.activity_title_xposed_others,
            Others()
        )
    )

    val searchableFragments = if (Preferences.isXposedOnlyMode) {
        commonFragments
    } else {
        arrayOf(
            SearchPreferenceItem(
                R.xml.home,
                R.string.navbar_home,
                Home()
            ),
            SearchPreferenceItem(
                R.xml.tweaks,
                R.string.navbar_tweaks,
                Tweaks()
            )
        ) + commonFragments
    }.distinctBy { it.fragment::class.java }
}
