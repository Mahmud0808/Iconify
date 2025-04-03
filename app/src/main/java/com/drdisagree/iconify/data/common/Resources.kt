package com.drdisagree.iconify.data.common

import android.os.Build
import android.os.Environment
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.models.SearchPreferenceItem
import com.drdisagree.iconify.ui.fragments.home.Home
import com.drdisagree.iconify.ui.fragments.settings.Settings
import com.drdisagree.iconify.ui.fragments.tweaks.Tweaks
import com.drdisagree.iconify.ui.fragments.xposed.AlbumArt
import com.drdisagree.iconify.ui.fragments.xposed.BackgroundChip
import com.drdisagree.iconify.ui.fragments.xposed.BatteryStyle
import com.drdisagree.iconify.ui.fragments.xposed.DepthWallpaper
import com.drdisagree.iconify.ui.fragments.xposed.DualStatusbar
import com.drdisagree.iconify.ui.fragments.xposed.HeaderClock
import com.drdisagree.iconify.ui.fragments.xposed.Lockscreen
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenClockParent
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWeather
import com.drdisagree.iconify.ui.fragments.xposed.LockscreenWidget
import com.drdisagree.iconify.ui.fragments.xposed.OpQsHeader
import com.drdisagree.iconify.ui.fragments.xposed.Others
import com.drdisagree.iconify.ui.fragments.xposed.QuickSettings
import com.drdisagree.iconify.ui.fragments.xposed.Statusbar
import com.drdisagree.iconify.ui.fragments.xposed.StatusbarLogo
import com.drdisagree.iconify.ui.fragments.xposed.Themes
import com.drdisagree.iconify.ui.fragments.xposed.TransparencyBlur
import com.drdisagree.iconify.ui.fragments.xposed.VolumePanelParent
import com.drdisagree.iconify.ui.fragments.xposed.Xposed
import com.drdisagree.iconify.ui.preferences.preferencesearch.SearchConfiguration

object Resources {

    // Preference files
    const val SHARED_XPREFERENCES = BuildConfig.APPLICATION_ID + "_xpreference"

    // Storage location
    val DOCUMENTS_DIR: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
    val DOWNLOADS_DIR: String =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath

    val LOG_DIR = "$DOCUMENTS_DIR/Iconify"
    const val MODULE_DIR = "/data/adb/modules/Iconify"
    const val SYSTEM_OVERLAY_DIR = "/system/product/overlay"
    const val OVERLAY_DIR = "$MODULE_DIR/system/product/overlay"
    val BACKUP_DIR = Environment.getExternalStorageDirectory().absolutePath + "/.iconify_backup"
    val TEMP_DIR = Environment.getExternalStorageDirectory().absolutePath + "/.iconify"
    val TEMP_MODULE_DIR = "$TEMP_DIR/Iconify"
    val TEMP_MODULE_OVERLAY_DIR = "$TEMP_MODULE_DIR/system/product/overlay"
    val TEMP_OVERLAY_DIR = "$TEMP_DIR/overlays"
    val TEMP_CACHE_DIR = "$TEMP_OVERLAY_DIR/cache"
    val UNSIGNED_UNALIGNED_DIR = "$TEMP_OVERLAY_DIR/unsigned_unaligned"
    val UNSIGNED_DIR = "$TEMP_OVERLAY_DIR/unsigned"
    val SIGNED_DIR = "$TEMP_OVERLAY_DIR/signed"

    // File resources
    const val FRAMEWORK_DIR = "/system/framework/framework-res.apk"

    // Resource names
    const val HEADER_CLOCK_LAYOUT = "preview_header_clock_"
    const val LOCKSCREEN_CLOCK_LAYOUT = "preview_lockscreen_clock_"

    // Database
    const val DYNAMIC_RESOURCE_DATABASE_NAME = "dynamic_resource_database"
    const val DYNAMIC_RESOURCE_TABLE = "dynamic_resource_table"

    val searchConfiguration = SearchConfiguration()

    val searchableFragments = arrayOf(
        SearchPreferenceItem(
            R.xml.home,
            R.string.navbar_home,
            Home(),
            !Preferences.isXposedOnlyMode
        ),
        SearchPreferenceItem(
            R.xml.tweaks,
            R.string.navbar_tweaks,
            Tweaks(),
            !Preferences.isXposedOnlyMode
        ),
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
            R.xml.xposed_lockscreen,
            R.string.activity_title_lockscreen,
            Lockscreen()
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
            R.xml.xposed_dual_statusbar,
            R.string.activity_title_dual_statusbar,
            DualStatusbar()
        ),
        SearchPreferenceItem(
            R.xml.xposed_statusbar_logo,
            R.string.status_bar_logo_title,
            StatusbarLogo()
        ),
        SearchPreferenceItem(
            R.xml.xposed_volume_panel,
            R.string.activity_title_volume_panel,
            VolumePanelParent()
        ),
        SearchPreferenceItem(
            R.xml.xposed_op_qs_header,
            R.string.activity_title_op_qs_header,
            OpQsHeader(),
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
        ),
        SearchPreferenceItem(
            R.xml.xposed_header_clock,
            R.string.activity_title_header_clock,
            HeaderClock()
        ),
        SearchPreferenceItem(
            R.xml.xposed_lockscreen_clock,
            R.string.activity_title_lockscreen_clock,
            LockscreenClockParent()
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
            R.xml.xposed_lockscreen_album_art,
            R.string.activity_title_lockscreen_album_art,
            AlbumArt()
        ),
        SearchPreferenceItem(
            R.xml.xposed_others,
            R.string.activity_title_xposed_others,
            Others(),
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
        )
    ).filter { it.shouldAdd }
        .distinctBy { it.fragment::class.java }
        .toTypedArray()
}
