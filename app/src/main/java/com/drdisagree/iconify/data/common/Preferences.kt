package com.drdisagree.iconify.data.common

import com.drdisagree.iconify.data.config.Config
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.keys.SettingsKey

object Preferences {

    // Xposed mods
    const val XPOSED_HOOK_CHECK = "xposedHookCheck"

    // Xposed view tags
    const val ICONIFY_HEADER_CLOCK_TAG = "iconify_header_clock"
    const val ICONIFY_LOCKSCREEN_CONTAINER_TAG = "iconify_lockscreen_container"
    const val ICONIFY_LOCKSCREEN_CLOCK_TAG = "iconify_lockscreen_clock"
    const val ICONIFY_LOCKSCREEN_WEATHER_TAG = "iconify_lockscreen_weather"
    const val ICONIFY_LOCKSCREEN_WIDGET_TAG = "iconify_lockscreen_widget"
    const val ICONIFY_DEPTH_WALLPAPER_TAG = "iconify_depth_wallpaper"
    const val ICONIFY_DEPTH_WALLPAPER_FOREGROUND_TAG = "iconify_depth_wallpaper_foreground"
    const val ICONIFY_DEPTH_WALLPAPER_BACKGROUND_TAG = "iconify_depth_wallpaper_background"
    const val ICONIFY_CHARGING_ICON_TAG = "iconify_charging_icon"
    const val ICONIFY_QS_HEADER_CONTAINER_TAG = "iconify_qs_header_container"
    const val ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG = "iconify_qs_header_container_shade"
    const val ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG = "iconify_sb_center_clock_container"

    // Preference keys
    const val UPDATE_SCHEDULE = "iconify_update_schedule"
    const val UPDATE_CHECK_TIME = "iconify_update_check_time"
    const val LAST_UPDATE_CHECK_TIME = "iconify_last_update_check_time"
    const val NEW_UPDATE_VERSION_CODE = "iconify_new_update_version_code"
    const val FIRST_INSTALL = "firstInstall"
    const val UPDATE_DETECTED = "updateDetected"
    const val ON_HOME_PAGE = "onHomePage"
    const val COLORED_BATTERY_SWITCH = "isColoredBatteryEnabled"
    const val COLOR_ACCENT_PRIMARY = "colorAccentPrimary"
    const val COLOR_ACCENT_PRIMARY_LIGHT = "colorAccentPrimaryLight"
    const val COLOR_ACCENT_SECONDARY = "colorAccentSecondary"
    const val COLOR_ACCENT_SECONDARY_LIGHT = "colorAccentSecondaryLight"
    const val CUSTOM_PRIMARY_COLOR_SWITCH = "customPrimaryColor"
    const val CUSTOM_SECONDARY_COLOR_SWITCH = "customSecondaryColor"
    const val QS_ROW_COLUMN_SWITCH = "fabricatedqsRowColumn"
    const val QSPANEL_BLUR_SWITCH = "qsBlurSwitch"
    const val AGGRESSIVE_QSPANEL_BLUR_SWITCH = "aggressiveQsBlurSwitch"
    const val UI_CORNER_RADIUS = "uiCornerRadius"
    const val PORT_QSTILE_EXPANDED_HEIGHT = "portraitQsTileExpandedHeight"
    const val PORT_QSTILE_NONEXPANDED_HEIGHT = "portraitQsTileNonExpandedHeight"
    const val LAND_QSTILE_EXPANDED_HEIGHT = "landscapeQsTileExpandedHeight"
    const val LAND_QSTILE_NONEXPANDED_HEIGHT = "landscapeQsTileNonExpandedHeight"
    const val SELECTED_SETTINGS_ICONS_COLOR = "selectedSettignsIconsColor"
    const val SELECTED_SETTINGS_ICONS_BG = "selectedSettignsIconsBg"
    const val SELECTED_SETTINGS_ICONS_SHAPE = "selectedSettignsIconsShape"
    const val SELECTED_SETTINGS_ICONS_SIZE = "selectedSettignsIconsSize"
    const val SELECTED_SETTINGS_ICONS_SET = "selectedSettignsIconsSet"
    const val SELECTED_TOAST_FRAME = "selectedToastFrame"
    const val SELECTED_ICON_SHAPE = "selectedIconShape"
    const val RESTART_SYSUI_AFTER_BOOT = "restartSysuiAfterBoot"
    const val VOLUME_PANEL_BACKGROUND_WIDTH = "volumePanelBackgroundWidth"
    const val SELECTED_PROGRESSBAR = "selectedProgressbar"
    const val SELECTED_SWITCH = "selectedSwitch"
    const val COLORED_BATTERY_CHECK = "isColoredBatteryEnabledByUser"
    const val CUSTOM_ACCENT = "customAccentColor"
    const val QS_TEXT_COLOR_VARIANT = "qsTextColorVariant"
    const val QS_TEXT_COLOR_VARIANT_NORMAL = "qsTextColorVariantNormal"
    const val QS_TEXT_COLOR_VARIANT_PIXEL = "qsTextColorVariantPixel"
    const val DYNAMIC_OVERLAY_RESOURCES = "dynamicOverlayResources"
    const val DYNAMIC_OVERLAY_RESOURCES_NIGHT = "dynamicOverlayResourcesNight"
    const val DYNAMIC_OVERLAY_RESOURCES_LAND = "dynamicOverlayResourcesLand"
    const val QS_HIDE_LABEL_SWITCH = "qsHideLabelSwitch"
    const val PROGRESS_WAVE_ANIMATION_SWITCH = "progressWaveAnimationSwitch"
    const val NOTCH_BAR_KILLER_SWITCH = "notchBarKillerSwitch"
    const val TABLET_LANDSCAPE_SWITCH = "tabletLandscapeSwitch"
    const val PILL_SHAPE_SWITCH = "navigationPillShapeSwitch"
    const val NAVBAR_FULL_SCREEN = "navbarfullscreen"
    const val NAVBAR_IMMERSIVE_V1 = "navbarimmersivev1"
    const val NAVBAR_IMMERSIVE_V2 = "navbarimmersivev2"
    const val NAVBAR_IMMERSIVE_V3 = "navbarimmersivev3"
    const val NAVBAR_GCAM_LAG_FIX = "navbargcamlagfix"
    const val NAVBAR_LOW_SENS = "navbarlowsens"
    const val NAVBAR_HIDE_PILL = "navbarhidepill"

    // Settings
    const val AUTO_UPDATE = "IconifyAutoUpdate"
    const val UPDATE_OVER_WIFI = "IconifyUpdateOverWifi"
    const val NEW_UPDATE_FOUND = "newUpdateFound"

    var isXposedOnlyMode = RPrefs.getBoolean(SettingsKey.XPOSED_ONLY_MODE, true) &&
            !Config.SKIP_TO_HOMEPAGE_FOR_TESTING

    // Others
    const val BOOT_ID = "boot_id"
}
