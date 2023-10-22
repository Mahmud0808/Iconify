package com.drdisagree.iconify.utils.overlay.compiler;

import static com.drdisagree.iconify.utils.helper.Logger.writeLog;

import android.os.Build;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.common.Const;
import com.drdisagree.iconify.common.Resources;
import com.drdisagree.iconify.utils.FileUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.drdisagree.iconify.utils.helper.BinaryInstaller;
import com.drdisagree.iconify.utils.overlay.OverlayUtil;
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager;
import com.topjohnwu.superuser.Shell;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DynamicCompiler {

    private static final String TAG = DynamicCompiler.class.getSimpleName();
    private static String mOverlayName = null;
    private static String mPackage = null;
    private static boolean mForce = false;
    private static final String[] mResource = new String[3];
    private static final JSONObject[] jsonResources = new JSONObject[3];

    public static boolean buildOverlay() throws IOException {
        return buildOverlay(true);
    }

    public static boolean buildOverlay(boolean force) throws IOException {
        mForce = force;

        try {
            JSONObject[] jsonObject = ResourceManager.getResources();

            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec();

            for (int i = 0; i < 3; i++) {
                jsonResources[i] = ResourceManager.generateJsonResource(jsonObject[i]);
            }

            Iterator<String> keys = jsonResources[0].keys();

            // Create overlay for each package
            while (keys.hasNext()) {
                mPackage = keys.next();
                for (int i = 0; i < 3; i++) {
                    mResource[i] = jsonResources[i].getString(mPackage)
                            .replace("'", "\"")
                            .replace("><", ">\n<");
                }
                mOverlayName = mPackage.equals(Const.FRAMEWORK_PACKAGE) ? "Dynamic1" : "Dynamic2";

                preExecute();
                moveOverlaysToCache();

                // Create AndroidManifest.xml
                if (createManifestResource(mOverlayName, Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName)) {
                    Log.e(TAG, "Failed to create Manifest for " + mOverlayName + "! Exiting...");
                    postExecute(true);
                    return true;
                }

                // Build APK using AAPT
                if (OverlayCompiler.runAapt(Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName)) {
                    Log.e(TAG, "Failed to build " + mOverlayName + "! Exiting...");
                    postExecute(true);
                    return true;
                }

                // ZipAlign the APK
                if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + mOverlayName + "-unsigned-unaligned.apk")) {
                    Log.e(TAG, "Failed to align " + mOverlayName + "-unsigned-unaligned.apk! Exiting...");
                    postExecute(true);
                    return true;
                }

                // Sign the APK
                if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/" + mOverlayName + "-unsigned.apk")) {
                    Log.e(TAG, "Failed to sign " + mOverlayName + "-unsigned.apk! Exiting...");
                    postExecute(true);
                    return true;
                }

                postExecute(false);
            }

            if (mForce) {
                Shell.cmd("rm -rf " + Resources.BACKUP_DIR).exec();

                // Disable the overlays incase they are already enabled
                OverlayUtil.disableOverlays("IconifyComponentDynamic1.overlay", "IconifyComponentDynamic2.overlay");

                // Install from files dir
                for (int i = 1; i <= 2; i++) {
                    Shell.cmd("pm install -r " + Resources.DATA_DIR + "/IconifyComponentDynamic" + i + ".apk").exec();
                    Shell.cmd("rm -rf " + Resources.DATA_DIR + "/IconifyComponentDynamic" + i + ".apk").exec();
                }

                // Move to system overlay dir
                SystemUtil.mountRW();
                for (int i = 1; i <= 2; i++) {
                    Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentDynamic" + i + ".apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponentDynamic" + i + ".apk").exec();
                    RootUtil.setPermissions(644, "/system/product/overlay/IconifyComponentDynamic" + i + ".apk");
                }
                SystemUtil.mountRO();

                // Enable the overlays
                OverlayUtil.enableOverlays("IconifyComponentDynamic1.overlay", "IconifyComponentDynamic2.overlay");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to build overlay! Exiting...", e);
            postExecute(true);
            return true;
        }

        return false;
    }

    private static void preExecute() throws IOException {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();

        // Extract overlay from assets
        FileUtil.copyAssets("Overlays/" + mPackage + "/" + mOverlayName);

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + mPackage + "/").exec();
        Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec();
    }

    private static void postExecute(boolean hasErroredOut) {
        if (!hasErroredOut) {
            // Move all generated overlays to module
            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
            RootUtil.setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk");

            Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.BACKUP_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();

            // Move to files dir
            if (mForce) {
                Shell.cmd("cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk").exec();
                RootUtil.setPermissions(644, Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk");
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec();
    }

    private static void moveOverlaysToCache() {
        Shell.cmd("mv -f \"" + Resources.DATA_DIR + "/Overlays/" + mPackage + "/" + mOverlayName + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName + "\"").exec().isSuccess();
    }

    private static boolean createManifestResource(String overlayName, String source) {
        Shell.cmd("mkdir -p " + source + "/res").exec();
        String[] values = {"values", "values-land", "values-night"};

        for (int i = 0; i < 3; i++) {
            Shell.cmd("mkdir -p " + source + "/res/" + values[i]).exec();
            Shell.cmd("printf '" + mResource[i] + "' > " + source + "/res/" + values[i] + "/iconify.xml;").exec();
        }

        String category = OverlayUtil.getCategory(overlayName);
        List<String> module = new ArrayList<>();
        module.add("printf '<?xml version=\"1.0\" encoding=\"utf-8\" ?>");
        module.add("<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\" android:versionName=\"v1.0\" package=\"IconifyComponent" + overlayName + ".overlay\">");
        module.add("\\t<uses-sdk android:minSdkVersion=\"" + BuildConfig.MIN_SDK_VERSION + "\" android:targetSdkVersion=\"" + Build.VERSION.SDK_INT + "\" />");
        module.add("\\t<overlay android:category=\"" + category + "\" android:priority=\"1\" android:targetPackage=\"" + mPackage + "\" />");
        module.add("\\t<application android:allowBackup=\"false\" android:hasCode=\"false\" />");
        module.add("</manifest>' > " + source + "/AndroidManifest.xml;");

        Shell.Result result = Shell.cmd(String.join("\\n", module)).exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + overlayName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + overlayName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + overlayName, result.getOut());
        }

        return !result.isSuccess();
    }
}
