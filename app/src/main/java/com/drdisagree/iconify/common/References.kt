package com.drdisagree.iconify.common;

import com.drdisagree.iconify.BuildConfig;

public class References {

    // Fabricated overlays
    public static final String ICONIFY_COLOR_ACCENT_PRIMARY = "0xFF50A6D7";
    public static final String ICONIFY_COLOR_ACCENT_SECONDARY = "0xFF387BFF";
    public static final String FABRICATED_COLORED_BATTERY = "coloredBattery";
    public static final String FABRICATED_BATTERY_COLOR_BG = "batteryColorBackground";
    public static final String FABRICATED_BATTERY_COLOR_FG = "batteryColorFilled";
    public static final String FABRICATED_QS_ROW = "qsRowCount";
    public static final String FABRICATED_QQS_ROW = "qqsRowCount";
    public static final String FABRICATED_QS_COLUMN = "qsColumnCount";
    public static final String FABRICATED_QQS_TILE = "qqsTileCount";
    public static final String FABRICATED_QS_TILE = "qsTileCount";
    public static final String FABRICATED_QS_TEXT_SIZE = "qsTextSize";
    public static final String FABRICATED_QS_ICON_SIZE = "qsIconSize";
    public static final String FABRICATED_QS_MOVE_ICON = "qsMoveIcon";
    public static final String FABRICATED_SB_LEFT_PADDING = "sbLeftPadding";
    public static final String FABRICATED_SB_RIGHT_PADDING = "sbRightPadding";
    public static final String FABRICATED_SB_HEIGHT = "sbTotalHeight";
    public static final String FABRICATED_SB_COLOR_SOURCE = "colorSBSource";
    public static final String FABRICATED_SB_COLOR_TINT = "colorSBTint";
    public static final String FABRICATED_PILL_WIDTH = "navigationPillWidth";
    public static final String FABRICATED_PILL_THICKNESS = "navigationPillThickness";
    public static final String FABRICATED_PILL_BOTTOM_SPACE = "navigationPillBottomSpace";
    public static final String FABRICATED_TABLET_HEADER = "qspanelTabletHeader";

    // Commands
    public static final String DEVICE_BOOT_ID_CMD = "cat /proc/sys/kernel/random/boot_id";

    // Notification service checker
    public static boolean isNotificationServiceRunning = false;

    // Overlay metadata
    public static final String METADATA_OVERLAY_PARENT = "OVERLAY_PARENT";
    public static final String METADATA_OVERLAY_TARGET = "OVERLAY_TARGET";
    public static final String METADATA_THEME_VERSION = "THEME_VERSION";
    public static final String METADATA_THEME_CATEGORY = "THEME_CATEGORY";

    // Overlay categories
    public static final String OVERLAY_CATEGORY_PREFIX = BuildConfig.APPLICATION_ID.replace(".debug", "") + ".category.";
}
