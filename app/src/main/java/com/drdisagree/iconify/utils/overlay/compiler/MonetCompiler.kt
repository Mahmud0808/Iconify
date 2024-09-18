package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.common.Const
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.FileUtils.copyAssets
import com.drdisagree.iconify.utils.RootUtils.setPermissions
import com.drdisagree.iconify.utils.SystemUtils.mountRO
import com.drdisagree.iconify.utils.SystemUtils.mountRW
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.topjohnwu.superuser.Shell
import java.io.IOException

object MonetCompiler {
    private val TAG = MonetCompiler::class.java.simpleName
    private var mForce = false

    @Throws(IOException::class)
    fun buildOverlay(resources: Array<String>, force: Boolean): Boolean {
        mForce = force
        preExecute()

        // Create AndroidManifest.xml
        val overlayName = "ME"
        if (createManifestResource(
                overlayName,
                Const.FRAMEWORK_PACKAGE,
                Resources.DATA_DIR + "/Overlays/android/ME",
                resources
            )
        ) {
            Log.e(TAG, "Failed to create Manifest for $overlayName! Exiting...")
            postExecute(true)
            return true
        }

        // Build APK using AAPT
        if (OverlayCompiler.runAapt(
                Resources.DATA_DIR + "/Overlays/android/ME",
                Const.FRAMEWORK_PACKAGE
            )
        ) {
            Log.e(TAG, "Failed to build $overlayName! Exiting...")
            postExecute(true)
            return true
        }

        // ZipAlign the APK
        if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/ME-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align ME-unsigned-unaligned.apk! Exiting...")
            postExecute(true)
            return true
        }

        // Sign the APK
        if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/ME-unsigned.apk")) {
            Log.e(TAG, "Failed to sign ME-unsigned.apk! Exiting...")
            postExecute(true)
            return true
        }

        postExecute(false)
        return false
    }

    @Throws(IOException::class)
    private fun preExecute() {
        // Create symbolic link
        symLinkBinaries()

        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec()

        // Extract overlay from assets
        copyAssets("Overlays/android/ME")

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec()

        if (!mForce) {
            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec()
        } else {
            // Disable the overlay in case it is already enabled
            disableOverlay("IconifyComponentME.overlay")
        }
    }

    private fun postExecute(hasErroredOut: Boolean) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd(
                "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.OVERLAY_DIR + "/IconifyComponentME.apk"
            ).exec()
            setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentME.apk")

            if (mForce) {
                // Move to files dir and install
                Shell.cmd(
                    "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.DATA_DIR + "/IconifyComponentME.apk"
                ).exec()
                setPermissions(644, Resources.DATA_DIR + "/IconifyComponentME.apk")
                Shell.cmd("pm install -r " + Resources.DATA_DIR + "/IconifyComponentME.apk").exec()
                Shell.cmd("rm -rf " + Resources.DATA_DIR + "/IconifyComponentME.apk").exec()

                // Move to system overlay dir
                mountRW()
                Shell.cmd(
                    "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponentME.apk"
                ).exec()
                setPermissions(644, "/system/product/overlay/IconifyComponentME.apk")
                mountRO()

                // Enable the overlays
                enableOverlays("IconifyComponentDM.overlay", "IconifyComponentME.overlay")
            } else {
                Shell.cmd(
                    "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentME.apk " + Resources.BACKUP_DIR + "/IconifyComponentME.apk"
                ).exec()
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec()
    }

    private fun createManifestResource(
        overlayName: String,
        targetPackage: String,
        source: String,
        resources: Array<String>
    ): Boolean {
        Shell.cmd(
            "rm -rf $source/res/values/colors.xml",
            "printf '" + resources[0] + "' > " + source + "/res/values/colors.xml;",
            "rm -rf $source/res/values-night/colors.xml",
            "printf '" + resources[1] + "' > " + source + "/res/values-night/colors.xml;"
        ).exec()

        return OverlayCompiler.createManifest(overlayName, targetPackage, source)
    }
}
