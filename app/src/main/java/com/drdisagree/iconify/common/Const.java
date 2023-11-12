package com.drdisagree.iconify.common;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.xposed.utils.BootLoopProtector;

import java.util.Arrays;
import java.util.List;

public class Const {

    // System packages
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    public static final String FRAMEWORK_PACKAGE = "android";
    public static final String PIXEL_LAUNCHER_PACKAGE = "com.google.android.apps.nexuslauncher";
    public static final String SETTINGS_PACKAGE = "com.android.settings";
    public static final String WELLBEING_PACKAGE = "com.google.android.apps.wellbeing";
    public static final String GMS_PACKAGE = "com.google.android.gms";
    public static final List<String> SYSTEM_PACKAGES = Arrays.asList(
            SYSTEMUI_PACKAGE,
            FRAMEWORK_PACKAGE,
            SETTINGS_PACKAGE
    );

    // Github repo
    public static final String GITHUB_REPO = "https://github.com/Mahmud0808/Iconify";

    // Telegram group
    public static final String TELEGRAM_GROUP = "https://t.me/IconifyDiscussion";

    // Parse new update
    public static final String LATEST_VERSION = "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/latestVersion.json";

    // Parse changelogs
    public static final String CHANGELOG_URL = "https://api.github.com/repos/Mahmud0808/Iconify/releases/tags/v";

    // Fragment variables
    public static final int TRANSITION_DELAY = 120;
    public static final int FRAGMENT_BACK_BUTTON_DELAY = 50;
    public static final int SWITCH_ANIMATION_DELAY = 300;

    // Xposed variables
    public static final List<String> PREF_UPDATE_EXCLUSIONS = Arrays.asList(BootLoopProtector.LOAD_TIME_KEY_KEY, BootLoopProtector.PACKAGE_STRIKE_KEY_KEY);
    public static final String ACTION_HOOK_CHECK_REQUEST = BuildConfig.APPLICATION_ID + ".ACTION_HOOK_CHECK_REQUEST";
    public static final String ACTION_HOOK_CHECK_RESULT = BuildConfig.APPLICATION_ID + ".ACTION_HOOK_CHECK_RESULT";

    // Module script
    public static final String MAGISK_UPDATE_BINARY = """
            #!/sbin/sh
                        
            #################
            # Initialization
            #################
                        
            umask 022
                        
            # echo before loading util_functions
            ui_print() { echo "$1"; }
                        
            require_new_magisk() {
              ui_print "*******************************"
              ui_print " Please install Magisk v20.4+! "
              ui_print "*******************************"
              exit 1
            }
                        
            #########################
            # Load util_functions.sh
            #########################
                        
            OUTFD=$2
            ZIPFILE=$3
                        
            mount /data 2>/dev/null
                        
            [ -f /data/adb/magisk/util_functions.sh ] || require_new_magisk
            . /data/adb/magisk/util_functions.sh
            [ $MAGISK_VER_CODE -lt 20400 ] && require_new_magisk
                        
            install_module
            exit 0
            """;
}
