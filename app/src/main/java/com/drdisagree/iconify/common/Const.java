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

    // Parse new update
    public static final String LATEST_VERSION = "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/latestVersion.json";

    // Parse changelogs
    public static final String CHANGELOG_URL = "https://api.github.com/repos/Mahmud0808/Iconify/releases/tags/v";

    // Fragment variables
    public static final int FRAGMENT_TRANSITION_DELAY = 120;
    public static final int FRAGMENT_BACK_BUTTON_DELAY = 50;
    public static final int SWITCH_ANIMATION_DELAY = 300;

    // Xposed variables
    public static final List<String> PREF_UPDATE_EXCLUSIONS = Arrays.asList(BootLoopProtector.LOAD_TIME_KEY_KEY, BootLoopProtector.PACKAGE_STRIKE_KEY_KEY);
    public static final String ACTION_HOOK_CHECK_REQUEST = BuildConfig.APPLICATION_ID + ".ACTION_HOOK_CHECK_REQUEST";
    public static final String ACTION_HOOK_CHECK_RESULT = BuildConfig.APPLICATION_ID + ".ACTION_HOOK_CHECK_RESULT";
}
