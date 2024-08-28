package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.common.Const
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.FileUtils.copyAssets
import com.drdisagree.iconify.utils.RootUtils.setPermissions
import com.drdisagree.iconify.utils.SystemUtils.mountRO
import com.drdisagree.iconify.utils.SystemUtils.mountRW
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager
import com.topjohnwu.superuser.Shell
import org.json.JSONObject
import java.io.IOException

object DynamicCompiler {

    private val TAG = DynamicCompiler::class.java.simpleName
    private val mResource = arrayOfNulls<String>(3)
    private val jsonResources = arrayOfNulls<JSONObject>(3)
    private var mOverlayName: String? = null
    private var mPackage: String? = null
    private var mForce = false

    @JvmOverloads
    @Throws(IOException::class)
    fun buildOverlay(force: Boolean = true): Boolean {
        mForce = force

        try {
            val jsonObject = ResourceManager.resources

            Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec()

            for (i in 0..2) {
                jsonResources[i] = ResourceManager.generateJsonResource(
                    jsonObject[i]
                )
            }

            val keys = jsonResources[0]!!.keys()

            // Create overlay for each package
            while (keys.hasNext()) {
                mPackage = keys.next()

                for (i in 0..2) {
                    mResource[i] = jsonResources[i]!!.getString(mPackage!!)
                        .replace("'", "\"")
                        .replace("><", ">\n<")
                }

                mOverlayName = if (mPackage == Const.FRAMEWORK_PACKAGE) "Dynamic1" else "Dynamic2"

                preExecute()
                moveOverlaysToCache()

                // Create AndroidManifest.xml
                if (createManifestResource(
                        mOverlayName,
                        mPackage,
                        Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName
                    )
                ) {
                    Log.e(TAG, "Failed to create Manifest for $mOverlayName! Exiting...")
                    postExecute(true)
                    return true
                }

                // Build APK using AAPT
                if (OverlayCompiler.runAapt(
                        Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName,
                        mPackage
                    )
                ) {
                    Log.e(TAG, "Failed to build $mOverlayName! Exiting...")
                    postExecute(true)
                    return true
                }

                // ZipAlign the APK
                if (OverlayCompiler.zipAlign(Resources.UNSIGNED_UNALIGNED_DIR + "/" + mOverlayName + "-unsigned-unaligned.apk")) {
                    Log.e(
                        TAG,
                        "Failed to align $mOverlayName-unsigned-unaligned.apk! Exiting..."
                    )
                    postExecute(true)
                    return true
                }

                // Sign the APK
                if (OverlayCompiler.apkSigner(Resources.UNSIGNED_DIR + "/" + mOverlayName + "-unsigned.apk")) {
                    Log.e(TAG, "Failed to sign $mOverlayName-unsigned.apk! Exiting...")
                    postExecute(true)
                    return true
                }
                postExecute(false)
            }
            if (mForce) {
                Shell.cmd("rm -rf " + Resources.BACKUP_DIR).exec()

                // Disable the overlays in case they are already enabled
                disableOverlays(
                    "IconifyComponentDynamic1.overlay",
                    "IconifyComponentDynamic2.overlay"
                )

                // Install from files dir
                for (i in 1..2) {
                    Shell.cmd(
                        "pm install -r " + Resources.DATA_DIR + "/IconifyComponentDynamic" + i + ".apk"
                    ).exec()
                    Shell.cmd(
                        "rm -rf " + Resources.DATA_DIR + "/IconifyComponentDynamic" + i + ".apk"
                    ).exec()
                }

                // Move to system overlay dir
                mountRW()
                for (i in 1..2) {
                    Shell.cmd(
                        "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponentDynamic" + i + ".apk " + Resources.SYSTEM_OVERLAY_DIR + "/IconifyComponentDynamic" + i + ".apk"
                    ).exec()
                    setPermissions(644, "/system/product/overlay/IconifyComponentDynamic$i.apk")
                }
                mountRO()

                // Enable the overlays
                enableOverlays(
                    "IconifyComponentDynamic1.overlay",
                    "IconifyComponentDynamic2.overlay"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build overlay! Exiting...", e)
            postExecute(true)
            return true
        }
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
        copyAssets("Overlays/$mPackage/$mOverlayName")

        // Create temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR + "; mkdir -p " + Resources.TEMP_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_OVERLAY_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_UNALIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.UNSIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.SIGNED_DIR).exec()
        Shell.cmd("mkdir -p " + Resources.TEMP_CACHE_DIR + "/" + mPackage + "/").exec()
        Shell.cmd("mkdir -p " + Resources.BACKUP_DIR).exec()
    }

    private fun postExecute(hasErroredOut: Boolean) {
        if (!hasErroredOut) {
            // Move all generated overlays to module
            Shell.cmd(
                "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk"
            ).exec()
            setPermissions(644, Resources.OVERLAY_DIR + "/IconifyComponent" + mOverlayName + ".apk")
            Shell.cmd(
                "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.BACKUP_DIR + "/IconifyComponent" + mOverlayName + ".apk"
            ).exec()

            // Move to files dir
            if (mForce) {
                Shell.cmd(
                    "cp -rf " + Resources.SIGNED_DIR + "/IconifyComponent" + mOverlayName + ".apk " + Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk"
                ).exec()
                setPermissions(
                    644,
                    Resources.DATA_DIR + "/IconifyComponent" + mOverlayName + ".apk"
                )
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + Resources.TEMP_DIR).exec()
        Shell.cmd("rm -rf " + Resources.DATA_DIR + "/Overlays").exec()
    }

    private fun moveOverlaysToCache() {
        Shell.cmd(
            "mv -f \"" + Resources.DATA_DIR + "/Overlays/" + mPackage + "/" + mOverlayName + "\" \"" + Resources.TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName + "\""
        ).exec().isSuccess
    }

    private fun createManifestResource(
        overlayName: String?,
        targetPackage: String?,
        source: String
    ): Boolean {
        Shell.cmd("mkdir -p $source/res").exec()

        val values = arrayOf("values", "values-land", "values-night")

        for (i in 0..2) {
            Shell.cmd("mkdir -p " + source + "/res/" + values[i]).exec()
            Shell.cmd(
                "printf '" + mResource[i] + "' > " + source + "/res/" + values[i] + "/iconify.xml;"
            ).exec()
        }

        return OverlayCompiler.createManifest(overlayName, targetPackage, source)
    }
}
