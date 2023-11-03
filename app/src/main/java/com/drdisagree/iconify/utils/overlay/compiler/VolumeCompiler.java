package com.drdisagree.iconify.utils.overlay.compiler;

import android.util.Log;

import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.AppUtil;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.helper.BinaryInstaller;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.topjohnwu.superuser.Shell;

import net.lingala.zip4j.ZipFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class VolumeCompiler {

    private static final String TAG = VolumeCompiler.class.getSimpleName();
    private static String mOverlayName = null;

    public static boolean buildModule(String overlayName, String packageName) throws Exception {
        mOverlayName = overlayName;

        preExecute(overlayName, packageName);

        // Create AndroidManifest.xml and build APK using AAPT
        String source = Resources.DATA_DIR + "/SpecialOverlays/" + packageName + '/' + overlayName;
        File dir = new File(source);

        if (dir.isDirectory()) {
            if (OverlayCompiler.createManifest(overlayName, packageName, source)) {
                Log.e(TAG, "Failed to create Manifest for " + overlayName + "! Exiting...");
                postExecute(true);
                return true;
            }

            String[] splitLocations = AppUtil.getSplitLocations(packageName);
            if (OverlayCompiler.runAapt(source, splitLocations)) {
                Log.e(TAG, "Failed to build " + overlayName + "! Exiting...");
                postExecute(true);
                return true;
            }
        } else {
            Log.e(TAG, source + "is not a directory! Exiting...");
            return true;
        }

        // Extract the necessary folders from zip
        String[] dirs = {"res/drawable-v30/", "res/drawable-v31/", "res/layout-v30/", "res/layout-v31/"};
        try (ZipFile zipFile = new ZipFile(new File(Resources.COMPANION_COMPILED_DIR + '/' + overlayName + ".zip"))) {
            for (String res : dirs) {
                zipFile.extractFile(res, Resources.COMPANION_COMPILED_DIR);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract " + overlayName + ".zip! Exiting...");
            e.printStackTrace();
            return true;
        }

        try {
            postExecute(false);
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    private static void preExecute(String overlayName, String packageName) throws IOException {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/SpecialOverlays").exec();

        // Extract the overlay from assets
        FileUtil.copyAssets("SpecialOverlays/" + packageName + '/' + overlayName);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_DIR + "/module").exec();
        Shell.cmd("mkdir -p " + Resources.COMPANION_TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.COMPANION_COMPILED_DIR).exec();

        // Extract module from assets
        try {
            FileUtil.copyAssets("Module");
            Shell.cmd("cp -a " + Resources.DATA_DIR + "/Module/. " + Resources.TEMP_DIR + "/module").exec();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void postExecute(boolean hasErroredOut) throws Exception {
        // Move all generated files to module
        if (!hasErroredOut) {
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/drawable-v30/. " + Resources.COMPANION_DRAWABLE_DIR).exec();
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/drawable-v31/. " + Resources.COMPANION_DRAWABLE_DIR).exec();
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/layout-v30/. " + Resources.COMPANION_LAYOUT_DIR).exec();
            Shell.cmd("cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/layout-v31/. " + Resources.COMPANION_LAYOUT_DIR).exec();

            // Create flashable module
            Shell.cmd("rm " + Resources.DOWNLOADS_DIR + "/IconifyCompanion.zip").exec();
            ModuleUtil.createModule(Resources.COMPANION_MODULE_DIR, Resources.DOWNLOADS_DIR + "/IconifyCompanion.zip");
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Module").exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/SpecialOverlays").exec();

        // Enable required overlay
        if (Objects.equals(mOverlayName, "VolumeNeumorphOutline"))
            OverlayUtil.enableOverlay("IconifyComponentIXCC.overlay");
    }
}
