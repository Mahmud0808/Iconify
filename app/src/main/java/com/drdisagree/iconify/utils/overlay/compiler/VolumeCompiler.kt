package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.FileUtils.copyAssets
import com.drdisagree.iconify.utils.ModuleUtils.createModule
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlay
import com.topjohnwu.superuser.Shell
import net.lingala.zip4j.ZipFile
import java.io.File
import java.io.IOException

object VolumeCompiler {

    private val TAG = VolumeCompiler::class.java.simpleName
    private var mOverlayName: String? = null

    @Throws(Exception::class)
    fun buildModule(overlayName: String, targetPackage: String): Boolean {
        mOverlayName = overlayName
        preExecute(overlayName, targetPackage)

        // Create AndroidManifest.xml and build APK using AAPT
        val source = Resources.DATA_DIR + "/SpecialOverlays/" + targetPackage + '/' + overlayName
        val dir = File(source)

        if (dir.isDirectory()) {
            if (OverlayCompiler.createManifest(overlayName, targetPackage, source)) {
                Log.e(TAG, "Failed to create Manifest for $overlayName! Exiting...")
                postExecute(true)
                return true
            }
            if (OverlayCompiler.runAapt(source, targetPackage)) {
                Log.e(TAG, "Failed to build $overlayName! Exiting...")
                postExecute(true)
                return true
            }
        } else {
            Log.e(TAG, source + "is not a directory! Exiting...")
            return true
        }

        // Extract the necessary folders from zip
        val dirs =
            arrayOf("res/drawable-v30/", "res/drawable-v31/", "res/layout-v30/", "res/layout-v31/")

        try {
            ZipFile(File(Resources.COMPANION_COMPILED_DIR + '/' + overlayName + ".zip")).use { zipFile ->
                for (res in dirs) {
                    zipFile.extractFile(res, Resources.COMPANION_COMPILED_DIR)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract $overlayName.zip! Exiting...")
            e.printStackTrace()
            return true
        }

        try {
            postExecute(false)
        } catch (e: Exception) {
            e.printStackTrace()
            return true
        }

        return false
    }

    @Throws(IOException::class)
    private fun preExecute(overlayName: String, packageName: String) {
        // Create symbolic link
        symLinkBinaries()

        // Clean data directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/SpecialOverlays").exec()

        // Extract the overlay from assets
        copyAssets("SpecialOverlays/$packageName/$overlayName")

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_DIR + "/module").exec()
        Shell.cmd("mkdir -p " + Resources.COMPANION_TEMP_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.COMPANION_COMPILED_DIR).exec()

        // Extract module from assets
        try {
            copyAssets("Module")
            Shell.cmd(
                "cp -a " + Resources.DATA_DIR + "/Module/. " + Resources.TEMP_DIR + "/module"
            ).exec()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun postExecute(hasErroredOut: Boolean) {
        // Move all generated files to module
        if (!hasErroredOut) {
            Shell.cmd(
                "cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/drawable-v30/. " + Resources.COMPANION_DRAWABLE_DIR
            ).exec()
            Shell.cmd(
                "cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/drawable-v31/. " + Resources.COMPANION_DRAWABLE_DIR
            ).exec()
            Shell.cmd(
                "cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/layout-v30/. " + Resources.COMPANION_LAYOUT_DIR
            ).exec()
            Shell.cmd(
                "cp -a " + Resources.COMPANION_COMPILED_DIR + "/res/layout-v31/. " + Resources.COMPANION_LAYOUT_DIR
            ).exec()

            // Create flashable module
            Shell.cmd("rm " + Resources.DOWNLOADS_DIR + "/IconifyCompanion.zip").exec()
            createModule(
                Resources.COMPANION_MODULE_DIR,
                Resources.DOWNLOADS_DIR + "/IconifyCompanion.zip"
            )
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Module").exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/SpecialOverlays").exec()

        // Enable required overlay
        if (mOverlayName == "VolumeNeumorphOutline") enableOverlay("IconifyComponentIXCC.overlay")
    }
}
