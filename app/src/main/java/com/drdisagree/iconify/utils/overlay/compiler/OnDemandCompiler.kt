package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.SIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.SYSTEM_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_CACHE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_UNALIGNED_DIR
import com.drdisagree.iconify.utils.FileUtils.copyAssets
import com.drdisagree.iconify.utils.MiscUtils.requiresNewToastStyle
import com.drdisagree.iconify.utils.RootUtils.setPermissions
import com.drdisagree.iconify.utils.SystemUtils.mountRO
import com.drdisagree.iconify.utils.SystemUtils.mountRW
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlay
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlay
import com.topjohnwu.superuser.Shell
import java.io.IOException

object OnDemandCompiler {

    private val TAG = OnDemandCompiler::class.java.simpleName
    private var mOverlayName: String? = null
    private var mPackage: String? = null
    private var mStyle = 0
    private var mForce = false

    @Throws(IOException::class)
    fun buildOverlay(
        overlayName: String,
        style: Int,
        targetPackage: String,
        force: Boolean
    ): Boolean {
        mOverlayName = overlayName
        mPackage = targetPackage
        mStyle = style
        mForce = force

        preExecute()
        moveOverlaysToCache()
        handleNewToastStyle()

        // Create AndroidManifest.xml
        if (OverlayCompiler.createManifest(
                overlayName,
                targetPackage,
                "$TEMP_CACHE_DIR/$targetPackage/$overlayName"
            )
        ) {
            Log.e(TAG, "Failed to create Manifest for $overlayName! Exiting...")
            postExecute(true)
            return true
        }

        // Build APK using AAPT
        if (OverlayCompiler.runAapt(
                "$TEMP_CACHE_DIR/$targetPackage/$overlayName",
                targetPackage
            )
        ) {
            Log.e(TAG, "Failed to build $overlayName! Exiting...")
            postExecute(true)
            return true
        }

        // ZipAlign the APK
        if (OverlayCompiler.zipAlign("$UNSIGNED_UNALIGNED_DIR/$overlayName-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align $overlayName-unsigned-unaligned.apk! Exiting...")
            postExecute(true)
            return true
        }

        // Sign the APK
        if (OverlayCompiler.apkSigner("$UNSIGNED_DIR/$overlayName-unsigned.apk")) {
            Log.e(TAG, "Failed to sign $overlayName-unsigned.apk! Exiting...")
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
        Shell.cmd("rm -rf $TEMP_DIR").exec()
        Shell.cmd("rm -rf $DATA_DIR/CompileOnDemand").exec()

        // Extract overlay from assets
        copyAssets("CompileOnDemand/$mPackage/$mOverlayName$mStyle")

        // Create temp directory
        Shell.cmd("rm -rf $TEMP_DIR; mkdir -p $TEMP_DIR").exec()
        Shell.cmd("mkdir -p $TEMP_OVERLAY_DIR").exec()
        Shell.cmd("mkdir -p $TEMP_CACHE_DIR").exec()
        Shell.cmd("mkdir -p $UNSIGNED_UNALIGNED_DIR").exec()
        Shell.cmd("mkdir -p $UNSIGNED_DIR").exec()
        Shell.cmd("mkdir -p $SIGNED_DIR").exec()
        Shell.cmd("mkdir -p $TEMP_CACHE_DIR/$mPackage/").exec()

        if (!mForce) {
            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec()
        } else {
            // Disable the overlay in case it is already enabled
            disableOverlay("IconifyComponent$mOverlayName.overlay")
        }
    }

    private fun postExecute(hasErroredOut: Boolean) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd(
                "cp -rf " + SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk"
            ).exec()
            setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk")

            // Move to files dir and install
            if (mForce) {
                Shell.cmd(
                    "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $DATA_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()
                setPermissions(
                    644,
                    "$DATA_DIR/IconifyComponent$mOverlayName.apk"
                )
                Shell.cmd(
                    "pm install -r $DATA_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()
                Shell.cmd(
                    "rm -rf $DATA_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()

                // Move to system overlay dir
                mountRW()
                Shell.cmd(
                    "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $SYSTEM_OVERLAY_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()
                setPermissions(
                    644,
                    "/system/product/overlay/IconifyComponent$mOverlayName.apk"
                )
                mountRO()

                // Enable the overlay
                enableOverlay("IconifyComponent$mOverlayName.overlay")
            } else {
                Shell.cmd(
                    "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $BACKUP_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf $TEMP_DIR").exec()
        Shell.cmd("rm -rf $DATA_DIR/CompileOnDemand").exec()
    }

    private fun moveOverlaysToCache() {
        Shell.cmd(
            "mv -f \"$DATA_DIR/CompileOnDemand/$mPackage/$mOverlayName$mStyle\" \"$TEMP_CACHE_DIR/$mPackage/$mOverlayName\""
        ).exec().isSuccess
    }

    private fun handleNewToastStyle() {
        if (mOverlayName != "TSTFRM" || !requiresNewToastStyle()) return

        Shell.cmd(
            "find \"$TEMP_CACHE_DIR/$mPackage/$mOverlayName/\" -type f -name \"*.xml\" -exec sh -c 'for file; do if echo \"\$file\" | grep -q \"/[^/]*-night/\"; then sed -i \"s/?android:colorBackgroundFloating/@*android:color\\/system_neutral2_800/g\" \"\$file\"; else sed -i \"s/?android:colorBackgroundFloating/@*android:color\\/system_neutral2_10/g\" \"\$file\"; fi; done' sh {} +"
        ).exec()
    }
}
