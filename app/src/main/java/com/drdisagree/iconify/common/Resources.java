package com.drdisagree.iconify.common;

import android.os.Environment;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;

public class Resources {

    // Preference files
    public static final String SharedPref = Iconify.getAppContext().getPackageName() + "_preference";
    public static final String SharedXPref = BuildConfig.APPLICATION_ID + "_xpreference";

    // Storage location
    public static final String DOC_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/documents";
    public static final String LOG_DIR = DOC_DIR + "/Iconify";
    public static final String MODULE_DIR = "/data/adb/modules/Iconify";
    public static final String DATA_DIR = Iconify.getAppContext().getFilesDir().toString();
    public static final String OVERLAY_DIR = MODULE_DIR + "/system/product/overlay";
    public static final String BIN_DIR = Iconify.getAppContext().getDataDir() + "/bin";
    public static final String BACKUP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify_backup";
    public static final String TEMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.iconify";
    public static final String TEMP_OVERLAY_DIR = TEMP_DIR + "/overlays";
    public static final String TEMP_CACHE_DIR = TEMP_OVERLAY_DIR + "/cache";
    public static final String UNSIGNED_UNALIGNED_DIR = TEMP_DIR + "/overlays/unsigned_unaligned";
    public static final String UNSIGNED_DIR = TEMP_DIR + "/overlays/unsigned";
    public static final String SIGNED_DIR = TEMP_DIR + "/overlays/signed";
    public static final String COMPANION_TEMP_DIR = TEMP_DIR + "/companion";
    public static final String COMPANION_COMPILED_DIR = COMPANION_TEMP_DIR + "/compiled";
    public static final String COMPANION_MODULE_DIR = TEMP_DIR + "/module/IconifyCompanion";
    public static final String COMPANION_RES_DIR = COMPANION_MODULE_DIR + "/substratumXML/SystemUI/res";
    public static final String COMPANION_DRAWABLE_DIR = COMPANION_RES_DIR + "/drawable";
    public static final String COMPANION_LAYOUT_DIR = COMPANION_RES_DIR + "/layout";

    // Xposed resource dir
    public static final String XPOSED_RESOURCE_TEMP_DIR = Environment.getExternalStorageDirectory() + "/.iconify_files";
}
