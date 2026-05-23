package com.drdisagree.iconify.data.common

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
    const val ICONIFY_QS_HEADER_IMAGE_CONTAINER_TAG = "iconify_qs_header_image_container"
    const val ICONIFY_SB_CENTER_CLOCK_CONTAINER_TAG = "iconify_sb_center_clock_container"
    const val ICONIFY_SB_BATTERY_ICON_TAG = "iconify_sb_battery_icon"
    const val ICONIFY_LS_BATTERY_ICON_TAG = "iconify_ls_battery_icon"
    const val ICONIFY_QS_BATTERY_ICON_TAG = "iconify_qs_battery_icon"

    // Battery styles
    const val BATTERY_STYLE_DEFAULT = 0
    const val BATTERY_STYLE_CUSTOM_R_LANDSCAPE = 1
    const val BATTERY_STYLE_CUSTOM_L_LANDSCAPE = 2
    const val BATTERY_STYLE_PORTRAIT_CAPSULE = 3
    const val BATTERY_STYLE_PORTRAIT_LORN = 4
    const val BATTERY_STYLE_PORTRAIT_MX = 5
    const val BATTERY_STYLE_PORTRAIT_AIROO = 6
    const val BATTERY_STYLE_R_LANDSCAPE_STYLE_A = 7
    const val BATTERY_STYLE_L_LANDSCAPE_STYLE_A = 8
    const val BATTERY_STYLE_R_LANDSCAPE_STYLE_B = 9
    const val BATTERY_STYLE_L_LANDSCAPE_STYLE_B = 10
    const val BATTERY_STYLE_LANDSCAPE_IOS_15 = 11
    const val BATTERY_STYLE_LANDSCAPE_IOS_16 = 12
    const val BATTERY_STYLE_PORTRAIT_ORIGAMI = 13
    const val BATTERY_STYLE_LANDSCAPE_SMILEY = 14
    const val BATTERY_STYLE_LANDSCAPE_MIUI_PILL = 15
    const val BATTERY_STYLE_L_LANDSCAPE_COLOROS = 16
    const val BATTERY_STYLE_R_LANDSCAPE_COLOROS = 17
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_A = 18
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_B = 19
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_C = 20
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_D = 21
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_E = 22
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_F = 23
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_G = 24
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_H = 25
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_I = 26
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_J = 27
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_K = 28
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_L = 29
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_M = 30
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_N = 31
    const val BATTERY_STYLE_LANDSCAPE_BATTERY_O = 32
    const val BATTERY_STYLE_CIRCLE = 33
    const val BATTERY_STYLE_DOTTED_CIRCLE = 34
    const val BATTERY_STYLE_FILLED_CIRCLE = 35
    const val BATTERY_STYLE_LANDSCAPE_KIM = 36
    const val BATTERY_STYLE_LANDSCAPE_ONE_UI_7 = 37

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

    // Others
    const val BOOT_ID = "boot_id"
}
