package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.SIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.SYSTEM_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_UNALIGNED_DIR
import com.drdisagree.iconify.utils.FileUtils.copyAssets
import com.drdisagree.iconify.utils.RootUtils.setPermissions
import com.drdisagree.iconify.utils.SystemUtils.mountRO
import com.drdisagree.iconify.utils.SystemUtils.mountRW
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.helper.Logger.writeLog
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.topjohnwu.superuser.Shell
import java.io.IOException

object RoundnessCompiler {

    private val TAG = RoundnessCompiler::class.java.simpleName
    private val mPackages = arrayOf(FRAMEWORK_PACKAGE, SYSTEMUI_PACKAGE)
    private val mOverlayName = arrayOf("CR1", "CR2")
    private var mForce = false

    @Throws(IOException::class)
    fun buildOverlay(resources: Array<String>, force: Boolean): Boolean {
        mForce = force
        preExecute()

        for (i in 0..1) {
            // Create AndroidManifest.xml
            if (OverlayCompiler.createManifest(
                    mOverlayName[i],
                    mPackages[i],
                    DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i]
                )
            ) {
                Log.e(TAG, "Failed to create Manifest for " + mOverlayName[i] + "! Exiting...")
                postExecute(true)
                return true
            }

            // Write resources
            if (writeResources(
                    DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i],
                    resources[i]
                )
            ) {
                Log.e(TAG, "Failed to write resource for " + mOverlayName[i] + "! Exiting...")
                postExecute(true)
                return true
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(
                    DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i],
                    mPackages[i]
                )
            ) {
                Log.e(TAG, "Failed to build " + mOverlayName[i] + "! Exiting...")
                postExecute(true)
                return true
            }

            // ZipAlign the APK
            if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + mOverlayName[i] + "-unsigned-unaligned.apk")) {
                Log.e(
                    TAG,
                    "Failed to align " + mOverlayName[i] + "-unsigned-unaligned.apk! Exiting..."
                )
                postExecute(true)
                return true
            }

            // Sign the APK
            if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/" + mOverlayName[i] + "-unsigned.apk")) {
                Log.e(TAG, "Failed to sign " + mOverlayName[i] + "-unsigned.apk! Exiting...")
                postExecute(true)
                return true
            }
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
        Shell.cmd("rm -rf $DATA_DIR/Overlays").exec()

        // Extract overlay from assets
        for (i in 0..1) {
            copyAssets("Overlays/" + mPackages[i] + "/" + mOverlayName[i])
        }

        // Create temp directory
        Shell.cmd("rm -rf $TEMP_DIR; mkdir -p $TEMP_DIR").exec()
        Shell.cmd("mkdir -p $TEMP_OVERLAY_DIR").exec()
        Shell.cmd("mkdir -p $UNSIGNED_UNALIGNED_DIR").exec()
        Shell.cmd("mkdir -p $UNSIGNED_DIR").exec()
        Shell.cmd("mkdir -p $SIGNED_DIR").exec()

        if (!mForce) {
            Shell.cmd("mkdir -p $BACKUP_DIR").exec()
        } else {
            // Disable the overlay in case it is already enabled
            val overlayNames = arrayOfNulls<String>(mOverlayName.size)

            for (i in 1..mOverlayName.size) {
                overlayNames[i - 1] = "IconifyComponentCR$i.overlay"
            }

            disableOverlays(*overlayNames)
        }
    }

    private fun postExecute(hasErroredOut: Boolean) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (overlayName in mOverlayName) {
                Shell.cmd(
                    "cp -rf $SIGNED_DIR/IconifyComponent$overlayName.apk $OVERLAY_DIR/IconifyComponent$overlayName.apk"
                ).exec()
                setPermissions(
                    644,
                    "$OVERLAY_DIR/IconifyComponent$overlayName.apk"
                )

                if (mForce) {
                    // Move to files dir and install
                    Shell.cmd(
                        "cp -rf $SIGNED_DIR/IconifyComponent$overlayName.apk $DATA_DIR/IconifyComponent$overlayName.apk"
                    ).exec()
                    setPermissions(
                        644,
                        "$DATA_DIR/IconifyComponent$overlayName.apk"
                    )
                    Shell.cmd(
                        "pm install -r $DATA_DIR/IconifyComponent$overlayName.apk"
                    ).exec()
                    Shell.cmd(
                        "rm -rf $DATA_DIR/IconifyComponent$overlayName.apk"
                    ).exec()
                }
            }
            if (mForce) {
                // Move to system overlay dir
                mountRW()
                for (overlayName in mOverlayName) {
                    Shell.cmd(
                        "cp -rf $SIGNED_DIR/IconifyComponent$overlayName.apk $SYSTEM_OVERLAY_DIR/IconifyComponent$overlayName.apk"
                    ).exec()
                    setPermissions(644, "/system/product/overlay/IconifyComponent$overlayName.apk")
                }
                mountRO()

                // Enable the overlays
                val overlayNames = arrayOfNulls<String>(mOverlayName.size)

                for (i in 1..mOverlayName.size) {
                    overlayNames[i - 1] = "IconifyComponentCR$i.overlay"
                }

                enableOverlays(*overlayNames)
            } else {
                for (overlayName in mOverlayName) {
                    Shell.cmd(
                        "cp -rf $SIGNED_DIR/IconifyComponent$overlayName.apk $BACKUP_DIR/IconifyComponent$overlayName.apk"
                    ).exec()
                }
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf $TEMP_DIR").exec()
        Shell.cmd("rm -rf $DATA_DIR/Overlays").exec()
    }

    private fun writeResources(source: String, resources: String): Boolean {
        val result = Shell.cmd(
            "rm -rf $source/res/values/dimens.xml",
            "printf '$resources' > $source/res/values/dimens.xml;"
        ).exec()

        if (result.isSuccess) Log.i(
            "$TAG - WriteResources",
            "Successfully written resources for UiRoundness"
        ) else {
            Log.e(
                "$TAG - WriteResources",
                "Failed to write resources for UiRoundness\n${
                    java.lang.String.join(
                        "\n",
                        result.out
                    )
                }"
            )
            writeLog(
                "$TAG - WriteResources",
                "Failed to write resources for UiRoundness",
                result.out
            )
        }

        return !result.isSuccess
    }
}
