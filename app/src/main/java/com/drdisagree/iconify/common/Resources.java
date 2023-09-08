package com.drdisagree.iconify.common;

import android.os.Environment;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;

import java.util.Objects;

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
    public static final String DATA_DIR = Objects.requireNonNull(Iconify.getAppContext()).getFilesDir().toString();
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

    // Xposed resource dir
    public static final String XPOSED_RESOURCE_TEMP_DIR = Environment.getExternalStorageDirectory() + "/.iconify_files";
    public static final String LSCLOCK_FONT_DIR = XPOSED_RESOURCE_TEMP_DIR + "/lsclock_font.ttf";
    public static final String HEADER_CLOCK_FONT_DIR = XPOSED_RESOURCE_TEMP_DIR + "/headerclock_font.ttf";
    public static final String HEADER_IMAGE_DIR = XPOSED_RESOURCE_TEMP_DIR + "/header_image.png";

    // Overlays
    public static final String QSC_overlay = "IconifyComponentQSC.overlay";
    public static final String QSNPT_overlay = "IconifyComponentQSNPT.overlay";
    public static final String QSNT1_overlay = "IconifyComponentQSNT1.overlay";
    public static final String QSNT2_overlay = "IconifyComponentQSNT2.overlay";
    public static final String QSNT3_overlay = "IconifyComponentQSNT3.overlay";
    public static final String QSNT4_overlay = "IconifyComponentQSNT4.overlay";
    public static final String QSPT1_overlay = "IconifyComponentQSPT1.overlay";
    public static final String QSPT2_overlay = "IconifyComponentQSPT2.overlay";
    public static final String QSPT3_overlay = "IconifyComponentQSPT3.overlay";
    public static final String QSPT4_overlay = "IconifyComponentQSPT4.overlay";
}
