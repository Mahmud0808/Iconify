package com.drdisagree.iconify.common;

import android.os.Environment;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;

public class Resources {

    // Preference files
    public static final String SharedPref = BuildConfig.APPLICATION_ID;
    public static final String SharedXPref = BuildConfig.APPLICATION_ID + "_xpreference";

    // Storage location
    public static final String DOCUMENTS_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    public static final String DOWNLOADS_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    public static final String LOG_DIR = DOCUMENTS_DIR + "/Iconify";
    public static final String MODULE_DIR = "/data/adb/modules/Iconify";
    public static final String SYSTEM_OVERLAY_DIR = "/system/product/overlay";
    public static final String DATA_DIR = Iconify.getAppContext().getFilesDir().getAbsolutePath();
    public static final String OVERLAY_DIR = MODULE_DIR + "/system/product/overlay";
    public static final String BIN_DIR = Iconify.getAppContext().getDataDir() + "/bin";
    public static final String BACKUP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify_backup";
    public static final String TEMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify";
    public static final String TEMP_MODULE_DIR = TEMP_DIR + "/Iconify";
    public static final String TEMP_MODULE_OVERLAY_DIR = TEMP_MODULE_DIR + "/system/product/overlay";
    public static final String TEMP_OVERLAY_DIR = TEMP_DIR + "/overlays";
    public static final String TEMP_CACHE_DIR = TEMP_OVERLAY_DIR + "/cache";
    public static final String UNSIGNED_UNALIGNED_DIR = TEMP_OVERLAY_DIR + "/unsigned_unaligned";
    public static final String UNSIGNED_DIR = TEMP_OVERLAY_DIR + "/unsigned";
    public static final String SIGNED_DIR = TEMP_OVERLAY_DIR + "/signed";
    public static final String COMPANION_TEMP_DIR = TEMP_DIR + "/companion";
    public static final String COMPANION_COMPILED_DIR = COMPANION_TEMP_DIR + "/compiled";
    public static final String COMPANION_MODULE_DIR = TEMP_DIR + "/module/IconifyCompanion";
    public static final String COMPANION_RES_DIR = COMPANION_MODULE_DIR + "/substratumXML/SystemUI/res";
    public static final String COMPANION_DRAWABLE_DIR = COMPANION_RES_DIR + "/drawable";
    public static final String COMPANION_LAYOUT_DIR = COMPANION_RES_DIR + "/layout";

    // File resources
    public static final String FRAMEWORK_DIR = "/system/framework/framework-res.apk";

    // Xposed resource dir
    public static final String XPOSED_RESOURCE_TEMP_DIR = Environment.getExternalStorageDirectory() + "/.iconify_files";
    public static final String LSCLOCK_FONT_DIR = XPOSED_RESOURCE_TEMP_DIR + "/lsclock_font.ttf";
    public static final String HEADER_CLOCK_FONT_DIR = XPOSED_RESOURCE_TEMP_DIR + "/headerclock_font.ttf";
    public static final String HEADER_IMAGE_DIR = XPOSED_RESOURCE_TEMP_DIR + "/header_image.png";
    public static final String DEPTH_WALL_FG_DIR = XPOSED_RESOURCE_TEMP_DIR + "/depth_wallpaper_fg.png";
    public static final String DEPTH_WALL_BG_DIR = XPOSED_RESOURCE_TEMP_DIR + "/depth_wallpaper_bg.png";

    // Resource names
    public static final String LOCKSCREEN_CLOCK_LAYOUT = "preview_lockscreen_clock_";
}
