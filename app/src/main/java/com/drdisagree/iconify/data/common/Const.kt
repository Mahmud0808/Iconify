package com.drdisagree.iconify.data.common

import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.utils.RootUtils
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
    const val TELEGRAM_GROUP = "https://t.me/IconifyDiscussion"

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

    val POST_FS_DATA = if (RootUtils.isSusfsBinaryAvailable) {
        """
            #!/usr/bin/env sh
            MODDIR="${'$'}{0%%/*}"
            modid="Iconify"
            SUSFS_BIN=/data/adb/ksu/bin/ksu_susfs
            
            if [ ${'$'}KSU_MAGIC_MOUNT = true ]; then
            	exit 0
            fi

            [ ! -f ${'$'}MODDIR/skip_mount ] && touch ${'$'}MODDIR/skip_mount
            
            [ -w /mnt ] && basefolder=/mnt
            [ -w /mnt/vendor ] && basefolder=/mnt/vendor
            
            mkdir ${'$'}basefolder/${'$'}modid
            
            cd ${'$'}MODDIR
            
            for i in ${'$'}(ls -d */*); do
            	mkdir -p ${'$'}basefolder/${'$'}modid/${'$'}i
            	mount --bind ${'$'}MODDIR/${'$'}i ${'$'}basefolder/${'$'}modid/${'$'}i
            	mount -t overlay -o "lowerdir=${'$'}basefolder/${'$'}modid/${'$'}i:/${'$'}i" overlay /${'$'}i
            	${'$'}{SUSFS_BIN} add_sus_mount /${'$'}i
            done
            """
    } else {
        """
            #!/usr/bin/env sh
            MODDIR="${'$'}{0%%/*}"
            """
    }.trimIndent()
}
