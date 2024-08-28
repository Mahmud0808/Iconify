package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.common.Const.GMS_PACKAGE
import com.drdisagree.iconify.common.Const.SETTINGS_PACKAGE
import com.drdisagree.iconify.common.Const.WELLBEING_PACKAGE
import com.drdisagree.iconify.common.Resources
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

object SettingsIconsCompiler {
    private val TAG = SettingsIconsCompiler::class.java.simpleName
    private val mPackages = arrayOf<String>(SETTINGS_PACKAGE, WELLBEING_PACKAGE, GMS_PACKAGE)
    private var mIconSet = 1
    private var mIconBg = 1
    private var mForce = false

    @Throws(IOException::class)
    fun buildOverlay(iconSet: Int, iconBg: Int, resources: String, force: Boolean): Boolean {
        mIconSet = iconSet
        mIconBg = iconBg
        mForce = force

        preExecute()
        moveOverlaysToCache()

        for (i in mPackages.indices) {
            val overlayName = "SIP" + (i + 1)

            // Create AndroidManifest.xml
            if (OverlayCompiler.createManifest(
                    overlayName,
                    mPackages[i],
                    Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + overlayName
                )
            ) {
                Log.e(TAG, "Failed to create Manifest for $overlayName! Exiting...")
                postExecute(true)
                return true
            }

            // Write resources
            if (resources != "" && writeResources(
                    Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + overlayName,
                    resources
                )
            ) {
                Log.e(TAG, "Failed to write resource for $overlayName! Exiting...")
                postExecute(true)
                return true
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(
                    Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + overlayName,
                    mPackages[i]
                )
            ) {
                Log.e(TAG, "Failed to build $overlayName! Exiting...")
                postExecute(true)
                return true
            }

            // ZipAlign the APK
            if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + overlayName + "-unsigned-unaligned.apk")) {
                Log.e(TAG, "Failed to align $overlayName-unsigned-unaligned.apk! Exiting...")
                postExecute(true)
                return true
            }

            // Sign the APK
            if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/" + overlayName + "-unsigned.apk")) {
                Log.e(TAG, "Failed to sign $overlayName-unsigned.apk! Exiting...")
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
        for (aPackage in mPackages) {
            copyAssets("CompileOnDemand/$aPackage/SIP$mIconSet")
        }

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec()

        for (aPackages in mPackages) {
            Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + aPackages + "/").exec()
        }

        if (!mForce) {
            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec()
        } else {
            // Disable the overlay in case it is already enabled
            val overlayNames = arrayOfNulls<String>(mPackages.size)

            for (i in 1..mPackages.size) {
                overlayNames[i - 1] = "IconifyComponentSIP$i.overlay"
            }

            disableOverlays(*overlayNames)
        }
    }

    private fun postExecute(hasErroredOut: Boolean) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (i in 1..mPackages.size) {
                Shell.cmd(
                    "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponentSIP" + i + ".apk"
                ).exec()
                setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponentSIP" + i + ".apk")
                if (mForce) {
                    // Move to files dir and install
                    Shell.cmd(
                        "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + Resources.DATA_DIR + "/IconifyComponentSIP" + i + ".apk"
                    ).exec()
                    setPermissions(644, Resources.DATA_DIR + "/IconifyComponentSIP" + i + ".apk")
                    Shell.cmd(
                        "pm install -r " + Resources.DATA_DIR + "/IconifyComponentSIP" + i + ".apk"
                    ).exec()
                    Shell.cmd(
                        "rm -rf " + Resources.DATA_DIR + "/IconifyComponentSIP" + i + ".apk"
                    ).exec()
                }
            }

            if (mForce) {
                // Move to system overlay dir
                mountRW()
                for (i in 1..mPackages.size) {
                    Shell.cmd(
                        "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponentSIP" + i + ".apk"
                    ).exec()
                    setPermissions(644, "/system/product/overlay/IconifyComponentSIP$i.apk")
                }
                mountRO()

                // Enable the overlays
                val overlayNames = arrayOfNulls<String>(mPackages.size)

                for (i in 1..mPackages.size) {
                    overlayNames[i - 1] = "IconifyComponentSIP$i.overlay"
                }

                enableOverlays(*overlayNames)
            } else {
                for (i in 1..mPackages.size) {
                    Shell.cmd(
                        "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentSIP" + i + ".apk " + Resources.BACKUP_DIR + "/IconifyComponentSIP" + i + ".apk"
                    ).exec()
                }
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/CompileOnDemand").exec()
    }

    private fun moveOverlaysToCache() {
        for (i in mPackages.indices) {
            Shell.cmd(
                "mv -f \"" + Resources.DATA_DIR + "/CompileOnDemand/" + mPackages[i] + "/" + "SIP" + mIconSet + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + "SIP" + (i + 1) + "\""
            ).exec()
        }

        if (mIconBg == 1) {
            for (i in mPackages.indices) {
                Shell.cmd(
                    "rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + "SIP" + (i + 1) + "/res/drawable\"",
                    "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + "SIP" + (i + 1) + "/res/drawable-night\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + "SIP" + (i + 1) + "/res/drawable\""
                ).exec()
                Shell.cmd(
                    "rm -rf \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + "SIP" + (i + 1) + "/res/drawable-anydpi\"",
                    "cp -rf \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + "SIP" + (i + 1) + "/res/drawable-night-anydpi\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackages[i] + "/" + "SIP" + (i + 1) + "/res/drawable-anydpi\""
                ).exec()
            }
        }
    }

    private fun writeResources(source: String, resources: String): Boolean {
        val result = Shell.cmd(
            "rm -rf $source/res/values/Iconify.xml",
            "printf '$resources' > $source/res/values/Iconify.xml;"
        ).exec()

        if (result.isSuccess) Log.i(
            "$TAG - WriteResources",
            "Successfully written resources for SettingsIcons"
        ) else {
            Log.e(
                "$TAG - WriteResources",
                "Failed to write resources for SettingsIcons\n${
                    java.lang.String.join(
                        "\n",
                        result.out
                    )
                }"
            )
            writeLog(
                "$TAG - WriteResources",
                "Failed to write resources for SettingsIcons",
                result.out
            )
        }

        return !result.isSuccess
    }
}
