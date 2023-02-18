package com.drdisagree.iconify.common;

import android.os.Environment;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.topjohnwu.superuser.Shell;

public class References {

    // Grab number of overlays dynamically for each variant
    public static final int TOTAL_BRIGHTNESSBARS = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBN' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_BRIGHTNESSBARSPIXEL = (Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentBBP' | sed -E 's/^....//'").exec().getOut()).size();
    public static final int TOTAL_ICONPACKS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentIPAS' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentNFN' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_NOTIFICATIONSPIXEL = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentNFP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPES = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSN' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_QSSHAPESPIXEL = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentQSSP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_SETTINGSICONPACKS = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentSIP' | sed -E 's/^....//'").exec().getOut().size();
    public static final int TOTAL_ICONSHAPES = Shell.cmd("cmd overlay list |  grep -E '^....IconifyComponentSIS' | sed -E 's/^....//'").exec().getOut().size();

    // Storage location
    public static final String MODULE_DIR = "/data/adb/modules/Iconify";
    public static final String DATA_DIR = Iconify.getAppContext().getFilesDir().toString();
    public static final String OVERLAY_DIR = MODULE_DIR + "/system/product/overlay";
    public static final String TOOLS_DIR = MODULE_DIR + "/tools";
    public static final String BACKUP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify_backup";
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
    public static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    public static final String FRAMEWORK_PACKAGE = "android";

    // Preference files
    public static final String SharedPref = Iconify.getAppContext().getPackageName();
    public static final String SharedXPref = BuildConfig.APPLICATION_ID + "_xpreference";

    // Xposed mods
    public static final String RESOURCE_TEMP_DIR = Environment.getExternalStorageDirectory() + "/.iconify_files";
    public static final String QSTRANSPARENCY_SWITCH = "xposed_qstransparency";
    public static final String QSALPHA_LEVEL = "xposed_qsalpha";
    public static final String STATUSBAR_CLOCKBG_SWITCH = "xposed_sbclockbg";
    public static final String CHIP_STATUSBAR_CLOCKBG_STYLE = "xposed_chipstatusbarclockbgstyle";
    public static final String QSPANEL_STATUSICONSBG_SWITCH = "xposed_qsstatusiconsbg";
    public static final String CHIP_QSSTATUSICONS_STYLE = "xposed_chipqsstatusiconsstyle";
    public static final String VERTICAL_QSTILE_SWITCH = "xposed_verticalqstile";
    public static final String HIDE_QSLABEL_SWITCH = "xposed_hideqslabel";
    public static final String HEADER_IMAGE_SWITCH = "xposed_headerimage";
    public static final String HEADER_IMAGE_HEIGHT = "xposed_headerimageheight";
    public static final String HEADER_IMAGE_ALPHA = "xposed_headerimagealpha";
    public static final String HEADER_IMAGE_ZOOMTOFIT = "xposed_headerimagezoomtofit";
    public static final String HIDE_STATUS_ICONS_SWITCH = "xposed_hidestatusicons";
    public static final String HEADER_CLOCK_SWITCH = "xposed_headerclock";
    public static final String HEADER_CLOCK_SIDEMARGIN = "xposed_headerclocksidemargin";
    public static final String HEADER_CLOCK_TOPMARGIN = "xposed_headerclocktopmargin";
    public static final String HEADER_CLOCK_TEXT_WHITE = "xposed_headerclocktextwhite";
    public static final String PANEL_TOPMARGIN_SWITCH = "xposed_paneltopmargin";
    public static final String QS_TOPMARGIN = "xposed_qstopmargin";
    public static final String QSPANEL_HIDE_CARRIER = "xposed_qspanelhidecarrier";
    public static final String HEADER_CLOCK_STYLE = "xposed_headerclockstyle";
    public static final String HEADER_CLOCK_FONT_TEXT_SCALING = "xposed_headerclocktextscaling";
    public static final String LSCLOCK_SWITCH = "xposed_lockscreenclock";
    public static final String LSCLOCK_STYLE = "xposed_lockscreenclockstyle";
    public static final String LSCLOCK_TOPMARGIN = "xposed_lockscreenclocktopmargin";
    public static final String LSCLOCK_BOTTOMMARGIN = "xposed_lockscreenclockbottommargin";
    public static final String LSCLOCK_FONT_SWITCH = "xposed_lockscreenclockfont";
    public static final String LSCLOCK_FONT_LINEHEIGHT = "xposed_lockscreenclockfontlineheight";
    public static final String LSCLOCK_FONT_TEXT_SCALING = "xposed_lockscreenclocktextscaling";
    public static final String LSCLOCK_TEXT_WHITE = "xposed_lockscreenclocktextwhite";
    public static final String FIXED_STATUS_ICONS_SWITCH = "xposed_fixedstatusicons";
    public static final String FIXED_STATUS_ICONS_SIDEMARGIN = "xposed_fixedstatusiconssidemargin";
    public static final String FIXED_STATUS_ICONS_TOPMARGIN = "xposed_fixedstatusiconstopmargin";

    // Parse new update
    public static final String LATEST_VERSION = "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/latestVersion.json";

    // Parse changelogs
    public static final String OLDER_CHANGELOGS = "https://raw.githubusercontent.com/Mahmud0808/Iconify/stable/fastlane/metadata/android/en-US/changelogs/{VersionCode}.txt";

    // Preference keys
    public static final String STR_NULL = "null";
    public static final String UPDATE_SCHEDULE = "iconify_update_schedule";
    public static final String UPDATE_CHECK_TIME = "iconify_update_check_time";
    public static final String LAST_UPDATE_CHECK_TIME = "iconify_last_update_check_time";
    public static final String FIRST_INSTALL = "firstInstall";
    public static final String UPDATE_DETECTED = "updateDetected";
    public static final String COLORED_BATTERY_SWITCH = "isColoredBatteryEnabled";
    public static final String COLOR_ACCENT_PRIMARY = "colorAccentPrimary";
    public static final String COLOR_ACCENT_SECONDARY = "colorAccentSecondary";
    public static final String CUSTOM_PRIMARY_COLOR_SWITCH = "customPrimaryColor";
    public static final String CUSTOM_SECONDARY_COLOR_SWITCH = "customSecondaryColor";
    public static final String COLOR_PIXEL_DARK_BG = "colorPixelBackgroundDark";
    public static final String QS_ROW_COLUMN_SWITCH = "fabricatedqsRowColumn";
    public static final String MONET_ENGINE_SWITCH = "customMonet";
    public static final String QSPANEL_BLUR_SWITCH = "qsBlurSwitch";
    public static final String UI_CORNER_RADIUS = "cornerRadius";
    public static final String MONET_STYLE = "customMonetStyle";
    public static final String MONET_ACCENT_SATURATION = "monetAccentSaturation";
    public static final String MONET_BACKGROUND_SATURATION = "monetBackgroundSaturation";
    public static final String MONET_BACKGROUND_LIGHTNESS = "monetBackgroundLightness";
    public static final String MONET_ACCURATE_SHADES = "monetAccurateShades";
    public static final String SHOW_XPOSED_WARN = "showXposedMenuWarn";
    public static final String PORT_QSTILE_EXPANDED_HEIGHT = "portraitQsTileExpandedHeight";
    public static final String PORT_QSTILE_NONEXPANDED_HEIGHT = "portraitQsTileNonExpandedHeight";
    public static final String LAND_QSTILE_EXPANDED_HEIGHT = "landscapeQsTileExpandedHeight";
    public static final String LAND_QSTILE_NONEXPANDED_HEIGHT = "landscapeQsTileNonExpandedHeight";

    // Fabricated overlays
    public static final String ICONIFY_COLOR_ACCENT_PRIMARY = "0xFF50A6D7";
    public static final String ICONIFY_COLOR_ACCENT_SECONDARY = "0xFF387BFF";
    public static final String ICONIFY_COLOR_PIXEL_DARK_BG = "0xFF122530";
    public static final String FABRICATED_COLORED_BATTERY = "coloredBattery";
    public static final String FABRICATED_BATTERY_COLOR_BG = "batteryColorBackground";
    public static final String FABRICATED_BATTERY_COLOR_FG = "batteryColorFilled";
    public static final String FABRICATED_QS_ROW = "qsRow";
    public static final String FABRICATED_QQS_ROW = "qqsRow";
    public static final String FABRICATED_QS_COLUMN = "qsColumn";
    public static final String FABRICATED_QQS_TILE = "qqsTile";
    public static final String FABRICATED_QS_TILE = "qsTile";
    public static final String FABRICATED_QS_TEXT_SIZE = "qsTextSize";
    public static final String FABRICATED_QS_ICON_SIZE = "qsIconSize";
    public static final String FABRICATED_QS_MOVE_ICON = "qsMoveIcon";
    public static final String FABRICATED_SB_LEFT_PADDING = "sbLeftPadding";
    public static final String FABRICATED_SB_RIGHT_PADDING = "sbRightPadding";
    public static final String FABRICATED_SB_COLOR_SOURCE = "colorSBSource";
    public static final String FABRICATED_SB_COLOR_TINT = "colorSBTint";
    public static final String FABRICATED_QSPANEL_BLUR_RADIUS = "qsBlurRadius";

    // Commands
    public static final String DEVICE_BOOT_ID_CMD = "cat /proc/sys/kernel/random/boot_id";

    // Others
    public static final String BOOT_ID = "boot_id";
    public static final String VER_CODE = "versionCode";
    public static final String EASTER_EGG = "iconify_easter_egg";

    // Notification service checker
    public static boolean isNotificationServiceRunning = false;
}
