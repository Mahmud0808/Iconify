package com.drdisagree.iconify.data.common

import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.features.settings.lookandfeel.screens.lookAndFeelPreferences
import com.drdisagree.iconify.features.settings.main.screens.settingsPreferences
import com.drdisagree.iconify.features.xposed.lockscreen.albumart.screens.lsAlbumArtPreferences
import com.drdisagree.iconify.features.xposed.lockscreen.clock.screens.lsClockPreferences
import com.drdisagree.iconify.features.xposed.lockscreen.depthwallpaper.screens.depthWallpaperPreferences
import com.drdisagree.iconify.features.xposed.lockscreen.main.screens.lockscreenPreferences
import com.drdisagree.iconify.features.xposed.lockscreen.weather.screens.lsWeatherPreferences
import com.drdisagree.iconify.features.xposed.lockscreen.widgets.main.screens.lsWidgetsPreferences
import com.drdisagree.iconify.features.xposed.main.screens.xposedPreferences
import com.drdisagree.iconify.features.xposed.quicksettings.headerimage.screens.headerImagePreferences
import com.drdisagree.iconify.features.xposed.quicksettings.main.screens.quickSettingsPreferences
import com.drdisagree.iconify.features.xposed.quicksettings.margins.screens.qsMarginsPreferences
import com.drdisagree.iconify.features.xposed.quicksettings.themes.screens.qsThemesPreferences
import com.drdisagree.iconify.features.xposed.quicksettings.transparency.screens.qsTransparencyPreferences
import com.drdisagree.iconify.features.xposed.statusbar.clockchip.screens.clockChipPreferences
import com.drdisagree.iconify.features.xposed.statusbar.dualstatusbar.screens.dualStatusbarPreferences
import com.drdisagree.iconify.features.xposed.statusbar.logo.screens.statusbarLogoPreferences
import com.drdisagree.iconify.features.xposed.statusbar.main.screens.statusbarPreferences
import com.drdisagree.iconify.features.xposed.volumepanel.screens.volumePanelPreferences

object References {

    // Xposed preferences
    private val allXposedPreferences = xposedPreferences() +
            lockscreenPreferences +
            lsAlbumArtPreferences +
            lsClockPreferences +
            depthWallpaperPreferences() +
            lsWeatherPreferences() +
            lsWidgetsPreferences() +
            quickSettingsPreferences +
            headerImagePreferences +
            qsMarginsPreferences +
            qsThemesPreferences +
            qsTransparencyPreferences +
            statusbarPreferences +
            clockChipPreferences +
            dualStatusbarPreferences +
            statusbarLogoPreferences() +
            volumePanelPreferences


    // Settings preferences
    private val allSettingsPreferences = settingsPreferences() +
            lookAndFeelPreferences

    // All preference list
    val PREFERENCE_LIST = allXposedPreferences + allSettingsPreferences

    // Fabricated overlays
    const val ICONIFY_COLOR_ACCENT_PRIMARY = "0xFF50A6D7"
    const val ICONIFY_COLOR_ACCENT_SECONDARY = "0xFF387BFF"
    const val FABRICATED_COLORED_BATTERY = "coloredBattery"
    const val FABRICATED_BATTERY_COLOR_BG = "batteryColorBackground"
    const val FABRICATED_BATTERY_COLOR_FG = "batteryColorFilled"
    const val FABRICATED_QS_ROW = "qsRowCount"
    const val FABRICATED_QQS_ROW = "qqsRowCount"
    const val FABRICATED_QS_COLUMN = "qsColumnCount"
    const val FABRICATED_QQS_TILE = "qqsTileCount"
    const val FABRICATED_QS_TILE = "qsTileCount"
    const val FABRICATED_QS_TEXT_SIZE = "qsTextSize"
    const val FABRICATED_QS_ICON_SIZE = "qsIconSize"
    const val FABRICATED_QS_MOVE_ICON = "qsMoveIcon"
    const val FABRICATED_SB_LEFT_PADDING = "sbLeftPadding"
    const val FABRICATED_SB_RIGHT_PADDING = "sbRightPadding"
    const val FABRICATED_SB_HEIGHT = "sbTotalHeight"
    const val FABRICATED_SB_COLOR_SOURCE = "colorSBSource"
    const val FABRICATED_SB_COLOR_TINT = "colorSBTint"
    const val FABRICATED_PILL_WIDTH = "navigationPillWidth"
    const val FABRICATED_PILL_THICKNESS = "navigationPillThickness"
    const val FABRICATED_PILL_BOTTOM_SPACE = "navigationPillBottomSpace"
    const val FABRICATED_TABLET_HEADER = "qspanelTabletHeader"

    // Commands
    const val DEVICE_BOOT_ID_CMD = "cat /proc/sys/kernel/random/boot_id"

    // Notification service checker
    var isNotificationServiceRunning = false

    // Overlay metadata
    const val METADATA_OVERLAY_PARENT = "OVERLAY_PARENT"
    const val METADATA_OVERLAY_TARGET = "OVERLAY_TARGET"
    const val METADATA_THEME_VERSION = "THEME_VERSION"
    const val METADATA_THEME_CATEGORY = "THEME_CATEGORY"

    // Overlay categories
    val OVERLAY_CATEGORY_PREFIX = BuildConfig.APPLICATION_ID
        .replace(".debug", "")
        .replace(".foss", "") + ".category."
}
