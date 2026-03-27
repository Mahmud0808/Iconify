package com.drdisagree.iconify.data.common

import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.xposed.utils.BootLoopProtector

object Const {

    // System packages
    const val SYSTEMUI_PACKAGE = "com.android.systemui"
    const val FRAMEWORK_PACKAGE = "android"
    const val PIXEL_LAUNCHER_PACKAGE = "com.google.android.apps.nexuslauncher"
    const val LAUNCHER3_PACKAGE = "com.android.launcher3"
    const val SETTINGS_PACKAGE = "com.android.settings"
    const val WELLBEING_PACKAGE = "com.google.android.apps.wellbeing"
    const val GMS_PACKAGE = "com.google.android.gms"

    val DYNAMIC_OVERLAYABLE_PACKAGES = listOf(
        FRAMEWORK_PACKAGE,
        SYSTEMUI_PACKAGE,
        SETTINGS_PACKAGE,
        PIXEL_LAUNCHER_PACKAGE,
        LAUNCHER3_PACKAGE
    )

    // 3rd party packages
    const val COLORBLENDR_PACKAGE = "com.drdisagree.colorblendr"
    const val PL_ENHANCED_PACKAGE = "com.drdisagree.pixellauncherenhanced"

    // Github repo
    const val GITHUB_REPO = "https://github.com/Mahmud0808/Iconify"

    // Telegram group
    const val TELEGRAM_GROUP = "https://t.me/DrDsProjectsChat"

    // Crowdin
    const val ICONIFY_CROWDIN = "https://crowdin.com/project/iconify"

    // Parse new update
    const val LATEST_VERSION_URL =
        "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/latestVersion.json"

    // Parse changelogs
    const val CHANGELOG_URL = "https://api.github.com/repos/Mahmud0808/Iconify/releases/tags/v"

    // ColorBlender URL
    const val COLORBLENDR_URL = "https://github.com/Mahmud0808/ColorBlendr"
    const val PL_ENHANCED_URL = "https://github.com/Mahmud0808/PixelLauncherEnhanced"

    // Fragment variables
    const val TRANSITION_DELAY = 120
    const val FRAGMENT_BACK_BUTTON_DELAY = 50
    const val SWITCH_ANIMATION_DELAY: Long = 300

    // Xposed variables
    val PREF_UPDATE_EXCLUSIONS = listOf(
        BootLoopProtector.LOAD_TIME_KEY_KEY,
        BootLoopProtector.PACKAGE_STRIKE_KEY_KEY,
    )

    // Shell commands
    const val RESET_LOCKSCREEN_CLOCK_COMMAND =
        "settings put secure lock_screen_custom_clock_face default"
    const val ENABLE_DYNAMIC_CLOCK_COMMAND =
        "settings put secure lockscreen_use_double_line_clock 1"
    const val DISABLE_DYNAMIC_CLOCK_COMMAND =
        "settings put secure lockscreen_use_double_line_clock 0"

    // AI Plugin
    const val AI_PLUGIN_PACKAGE = "it.dhd.oxygencustomizer.aiplugin"
    const val AI_PLUGIN_URL =
        "https://github.com/DHD2280/Oxygen-Customizer-AI-Plugin/releases/latest/"

    const val ACTION_HOOK_CHECK_REQUEST = "${BuildConfig.APPLICATION_ID}.ACTION_HOOK_CHECK_REQUEST"
    const val ACTION_HOOK_CHECK_RESULT = "${BuildConfig.APPLICATION_ID}.ACTION_HOOK_CHECK_RESULT"
    const val ACTION_BOOT_COMPLETED = "${BuildConfig.APPLICATION_ID}.ACTION_BOOT_COMPLETED"
    const val ACTION_LS_CLOCK_INFLATED = "${BuildConfig.APPLICATION_ID}.ACTION_LS_CLOCK_INFLATED"
    const val ACTION_WEATHER_INFLATED = "${BuildConfig.APPLICATION_ID}.ACTION_WEATHER_INFLATED"
    const val ACTION_EXTRACT_SUBJECT = "$AI_PLUGIN_PACKAGE.ACTION_EXTRACT_SUBJECT"
    const val ACTION_EXTRACT_SUCCESS = "$AI_PLUGIN_PACKAGE.ACTION_EXTRACT_SUCCESS"
    const val ACTION_EXTRACT_FAILURE = "$AI_PLUGIN_PACKAGE.ACTION_EXTRACT_FAILURE"
    const val ACTION_UPDATE_DEPTH_WALLPAPER_FOREGROUND_VISIBILITY =
        "${BuildConfig.APPLICATION_ID}.ACTION_UPDATE_DEPTH_WALLPAPER_FOREGROUND_VISIBILITY"

    val BROADCAST_ACTIONS = listOf(
        ACTION_HOOK_CHECK_REQUEST,
        ACTION_HOOK_CHECK_RESULT,
        ACTION_BOOT_COMPLETED,
        ACTION_LS_CLOCK_INFLATED,
        ACTION_WEATHER_INFLATED,
        ACTION_EXTRACT_SUBJECT,
        ACTION_EXTRACT_SUCCESS,
        ACTION_EXTRACT_FAILURE,
        ACTION_UPDATE_DEPTH_WALLPAPER_FOREGROUND_VISIBILITY
    )
}
