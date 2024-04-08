package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.common.Const.SETTINGS_PACKAGE
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.FileUtil.copyAssets
import com.drdisagree.iconify.utils.RootUtil.setPermissions
import com.drdisagree.iconify.utils.SystemUtil.mountRO
import com.drdisagree.iconify.utils.SystemUtil.mountRW
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.OverlayUtil.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtil.enableOverlays
import com.topjohnwu.superuser.Shell
import java.io.IOException

object SwitchCompiler {

    private val TAG = SwitchCompiler::class.java.simpleName
    private val mPackages = arrayOf<String>(SETTINGS_PACKAGE, SYSTEMUI_PACKAGE)
    private val mOverlayName = arrayOf("SWITCH1", "SWITCH2")
    private var mStyle = 0
    private var mForce = false

    @Throws(IOException::class)
    fun buildOverlay(style: Int, force: Boolean): Boolean {
        mStyle = style
        mForce = force

        preExecute()
        moveOverlaysToCache()

        for (i in mOverlayName.indices) {
            // Create AndroidManifest.xml
            if (OverlayCompiler.createManifest(
                    mOverlayName[i],
                    mPackages[i],
                    Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + mOverlayName[i]
                )
            ) {
                Log.e(TAG, "Failed to create Manifest for " + mOverlayName[i] + "! Exiting...")
                postExecute(true)
                return true
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(
                    Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + mOverlayName[i],
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
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec()

        // Extract overlay from assets
        for (packageName in mPackages) {
            copyAssets("CompileOnDemand/$packageName/SWITCH$mStyle")
        }

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec()

        for (packageName in mPackages) {
            Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + packageName + "/").exec()
        }

        if (!mForce) {
            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec()
        } else {
            // Disable the overlay in case it is already enabled
            val overlayNames = arrayOfNulls<String>(mOverlayName.size)

            for (i in 1..mOverlayName.size) {
                overlayNames[i - 1] = "IconifyComponentSWITCH$i.overlay"
            }

            disableOverlays(*overlayNames)
        }
    }

    private fun postExecute(hasErroredOut: Boolean) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (overlayName in mOverlayName) {
                Shell.cmd(
                    "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponent" + overlayName + ".apk"
                ).exec()
                setPermissions(
                    644,
                    Resources.OVERLAY_DIR + "/IconifyComponent" + overlayName + ".apk"
                )

                if (mForce) {
                    // Move to files dir and install
                    Shell.cmd(
                        "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk"
                    ).exec()
                    setPermissions(
                        644,
                        Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk"
                    )
                    Shell.cmd(
                        "pm install -r " + Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk"
                    ).exec()
                    Shell.cmd(
                        "rm -rf " + Resources.DATA_DIR + "/IconifyComponent" + overlayName + ".apk"
                    ).exec()
                }
            }

            if (mForce) {
                // Move to system overlay dir
                mountRW()
                for (overlayName in mOverlayName) {
                    Shell.cmd(
                        "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponent" + overlayName + ".apk"
                    ).exec()
                    setPermissions(644, "/system/product/overlay/IconifyComponent$overlayName.apk")
                }
                mountRO()

                // Enable the overlays
                val overlayNames = arrayOfNulls<String>(mOverlayName.size)

                for (i in 1..mOverlayName.size) {
                    overlayNames[i - 1] = "IconifyComponentSWITCH$i.overlay"
                }

                enableOverlays(*overlayNames)
            } else {
                for (overlayName in mOverlayName) {
                    Shell.cmd(
                        "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + overlayName + ".apk " + Resources.BACKUP_DIR + "/IconifyComponent" + overlayName + ".apk"
                    ).exec()
                }
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec()
    }

    private fun moveOverlaysToCache() {
        for (i in mOverlayName.indices) {
            Shell.cmd(
                "mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + mPackages[i] + "/" + "SWITCH" + mStyle + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + mOverlayName[i] + "\""
            ).exec().isSuccess
        }
    }
}
