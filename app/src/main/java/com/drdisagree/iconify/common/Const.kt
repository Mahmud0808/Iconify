package com.drdisagree.iconify.common

import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.xposed.utils.BootLoopProtector

object Const {

    // System packages
    const val SYSTEMUI_PACKAGE = "com.android.systemui"
    const val FRAMEWORK_PACKAGE = "android"
    const val PIXEL_LAUNCHER_PACKAGE = "com.google.android.apps.nexuslauncher"
    const val SETTINGS_PACKAGE = "com.android.settings"
    const val WELLBEING_PACKAGE = "com.google.android.apps.wellbeing"
    const val GMS_PACKAGE = "com.google.android.gms"

    val SYSTEM_PACKAGES = listOf(
        SYSTEMUI_PACKAGE,
        FRAMEWORK_PACKAGE,
        SETTINGS_PACKAGE
    )

    // Github repo
    const val GITHUB_REPO = "https://github.com/Mahmud0808/Iconify"

    // Telegram group
    const val TELEGRAM_GROUP = "https://t.me/IconifyDiscussion"

    // Parse new update
    const val LATEST_VERSION_URL =
        "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/latestVersion.json"

    // Parse changelogs
    const val CHANGELOG_URL = "https://api.github.com/repos/Mahmud0808/Iconify/releases/tags/v"

    // Fragment variables
    const val TRANSITION_DELAY = 120
    const val FRAGMENT_BACK_BUTTON_DELAY = 50
    const val SWITCH_ANIMATION_DELAY: Long = 300

    // Xposed variables
    val PREF_UPDATE_EXCLUSIONS = listOf(
        BootLoopProtector.LOAD_TIME_KEY_KEY,
        BootLoopProtector.PACKAGE_STRIKE_KEY_KEY,
    )
    const val ACTION_HOOK_CHECK_REQUEST = "${BuildConfig.APPLICATION_ID}.ACTION_HOOK_CHECK_REQUEST"
    const val ACTION_HOOK_CHECK_RESULT = "${BuildConfig.APPLICATION_ID}.ACTION_HOOK_CHECK_RESULT"
    const val ACTION_BOOT_COMPLETED = "${BuildConfig.APPLICATION_ID}.ACTION_BOOT_COMPLETED"
    const val ACTION_WEATHER_INFLATED = "${BuildConfig.APPLICATION_ID}.ACTION_WEATHER_INFLATED"

    // Module script
    val MAGISK_UPDATE_BINARY = """
            #!/sbin/sh
            
            #################
            # Initialization
            #################
            
            umask 022
            
            # echo before loading util_functions
            ui_print() { echo "${'$'}1"; }
            
            require_new_magisk() {
              ui_print "*******************************"
              ui_print " Please install Magisk v20.4+! "
              ui_print "*******************************"
              exit 1
            }
            
            #########################
            # Load util_functions.sh
            #########################
            
            OUTFD=${'$'}2
            ZIPFILE=${'$'}3
            
            mount /data 2>/dev/null
            
            [ -f /data/adb/magisk/util_functions.sh ] || require_new_magisk
            . /data/adb/magisk/util_functions.sh
            [ ${'$'}MAGISK_VER_CODE -lt 20400 ] && require_new_magisk
            
            install_module
            exit 0
            """.trimIndent()
}
