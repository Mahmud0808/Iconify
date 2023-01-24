package com.drdisagree.iconify.common;

import android.os.Environment;

import com.drdisagree.iconify.Iconify;
import com.topjohnwu.superuser.Shell;

public class References {

    // Grab number of overlays dynamically for each variant
    public static final int TOTAL_BRIGHTNESSBARS = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBN' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_BRIGHTNESSBARSPIXEL = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBP' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_ICONPACKS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentIPAS' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentNF' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPES = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSN' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPESPIXEL = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_RADIUS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentCR' | sed -E 's/^....//'").exec().getOut().size();

    // Storage location
    public static final String MODULE_DIR = "/data/adb/modules/Iconify";
    public static final String DATA_DIR = Iconify.getAppContext().getFilesDir().toString();
    public static final String OVERLAY_DIR = MODULE_DIR + "/system/product/overlay";
    public static final String TOOLS_DIR = MODULE_DIR + "/tools";
    public static final String TEMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify";
    public static final String TEMP_OVERLAY_DIR = TEMP_DIR + "/overlays";
    public static final String UNSIGNED_UNALIGNED_DIR = TEMP_DIR + "/overlays/unsigned_unaligned";
    public static final String UNSIGNED_DIR = TEMP_DIR + "/overlays/unsigned";
    public static final String SIGNED_DIR = TEMP_DIR + "/overlays/signed";
    public static final String COMPANION_TEMP_DIR = TEMP_DIR + "/companion";
    public static final String COMPANION_COMPILED_DIR = COMPANION_TEMP_DIR + "/compiled";
    public static final String COMPANION_MODULE_DIR = TEMP_DIR + "/module/IconifyCompanion";
    public static final String COMPANION_RES_DIR = COMPANION_MODULE_DIR + "/substratumXML/SystemUI/res";
    public static final String COMPANION_DRAWABLE_DIR = COMPANION_RES_DIR + "/drawable";
    public static final String COMPANION_LAYOUT_DIR = COMPANION_RES_DIR + "/layout";

    // System packages
    public static final String SYSTEM_UI_PACKAGE = "com.android.systemui";
    public static final String FRAMEWORK_PACKAGE = "android";

    // Preference files
    public static final String SharedPref = Iconify.getAppContext().getPackageName();
    public static final String SharedXPref = Iconify.getAppContext().getPackageName() + "_xpreference";

    // Xposed mods
    public static final String QSTRANSPARENCY_SWITCH = "xposed_qstransparency";
    public static final String QSALPHA_LEVEL = "xposed_qsalpha";
    public static final String STATUSBAR_CLOCKBG = "xposed_qsclockbg";
    public static final String VERTICAL_QSTILE_SWITCH = "xposed_verticalqstile";
    public static final String HIDE_QSLABEL_SWITCH = "xposed_showqslabel";
    public static final String HEADER_IMAGE_SWITCH = "xposed_headerimage";
    public static final String HEADER_IMAGE_HEIGHT = "xposed_headerimageheight";
    public static final String HEADER_IMAGE_ALPHA = "xposed_headerimagealpha";
    public static final String HIDE_STATUS_ICONS_SWITCH = "xposed_hidestatusicons";
    public static final String HEADER_CLOCK_SWITCH = "xposed_headerclock";
    public static final String HEADER_CLOCK_SIDEMARGIN = "xposed_headerclocksidemargin";
    public static final String HEADER_CLOCK_TOPMARGIN = "xposed_headerclocktopmargin";
    public static final String HEADER_CLOCK_QSTOPMARGIN = "xposed_headerclockqstopmargin";
    public static final String HEADER_CLOCK_STYLE = "xposed_headerclockstyle";

    // Parse new update
    public static final String LATEST_VERSION = "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/latestVersion.json";

    // Parse changelogs
    public static final String OLDER_CHANGELOGS = "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/fastlane/metadata/android/en-US/changelogs/{VersionCode}.txt";

    // Notification service checker
    public static boolean isNotificationServiceRunning = false;
}
