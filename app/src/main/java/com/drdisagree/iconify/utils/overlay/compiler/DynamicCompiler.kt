package com.drdisagree.iconify.utils.overlay.compiler

import android.util.Log
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.LAUNCHER3_PACKAGE
import com.drdisagree.iconify.data.common.Const.PIXEL_LAUNCHER_PACKAGE
import com.drdisagree.iconify.data.common.Const.SETTINGS_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.DATA_DIR
import com.drdisagree.iconify.data.common.Resources.OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.SIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.SYSTEM_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_CACHE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_UNALIGNED_DIR
import com.drdisagree.iconify.utils.FileUtils.copyAssets
import com.drdisagree.iconify.utils.RootUtils.setPermissions
import com.drdisagree.iconify.utils.SystemUtils.mountRO
import com.drdisagree.iconify.utils.SystemUtils.mountRW
import com.drdisagree.iconify.utils.helper.BinaryInstaller.symLinkBinaries
import com.drdisagree.iconify.utils.overlay.OverlayUtils.disableOverlays
import com.drdisagree.iconify.utils.overlay.OverlayUtils.enableOverlays
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.ResourceType
import com.drdisagree.iconify.utils.overlay.manager.resource.ResourceManager.generateXmlStructureForAllResources
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object DynamicCompiler {

    private val TAG = DynamicCompiler::class.java.simpleName
    private var mForce = false
    private var mPackage: String? = null
    private var mOverlayName: String? = null
    private val mResource: MutableMap<ResourceType, ArrayList<String>> = mutableMapOf()

    @JvmOverloads
    @Throws(IOException::class)
    suspend fun buildDynamicOverlay(
        force: Boolean = true,
        overlaysToUpdate: List<String>? = null
    ): Boolean {
        mForce = force

        try {
            Shell.cmd("mkdir -p $BACKUP_DIR").exec()

            val resourcesMap = generateXmlStructureForAllResources(overlaysToUpdate)

            // Create overlay for each package
            for (packageName in resourcesMap.keys) {
                Log.i(TAG, packageName)
                mPackage = packageName

                mResource.clear()
                mResource[ResourceType.PORTRAIT] =
                    ArrayList(resourcesMap[packageName]!![ResourceType.PORTRAIT]!!)
                resourcesMap[packageName]!![ResourceType.LANDSCAPE]?.let {
                    mResource[ResourceType.LANDSCAPE] = ArrayList(it)
                }
                resourcesMap[packageName]!![ResourceType.NIGHT]?.let {
                    mResource[ResourceType.NIGHT] = ArrayList(it)
                }

                mOverlayName = getOverlayName(packageName)

                preExecute()
                moveOverlaysToCache()

                // Create AndroidManifest.xml
                if (createManifestResource(
                        mOverlayName,
                        mPackage,
                        "$TEMP_CACHE_DIR/$mPackage/$mOverlayName"
                    )
                ) {
                    Log.e(TAG, "Failed to create Manifest for $mOverlayName! Exiting...")
                    postExecute(true)
                    return true
                }

                // Build APK using AAPT
                if (OverlayCompiler.runAapt(
                        "$TEMP_CACHE_DIR/$mPackage/$mOverlayName",
                        mPackage
                    )
                ) {
                    Log.e(TAG, "Failed to build $mOverlayName! Exiting...")
                    postExecute(true)
                    return true
                }

                // ZipAlign the APK
                if (OverlayCompiler.zipAlign("$UNSIGNED_UNALIGNED_DIR/$mOverlayName-unsigned-unaligned.apk")) {
                    Log.e(
                        TAG,
                        "Failed to align $mOverlayName-unsigned-unaligned.apk! Exiting..."
                    )
                    postExecute(true)
                    return true
                }

                // Sign the APK
                if (OverlayCompiler.apkSigner("$UNSIGNED_DIR/$mOverlayName-unsigned.apk")) {
                    Log.e(TAG, "Failed to sign $mOverlayName-unsigned.apk! Exiting...")
                    postExecute(true)
                    return true
                }

                postExecute(false)
            }

            if (mForce) {
                Shell.cmd("rm -rf $BACKUP_DIR").exec()
                val overlayPackageNames = resourcesMap.keys
                    .map { "IconifyComponent${getOverlayName(it)}.overlay" }
                    .toTypedArray()

                // Disable the overlays in case they are already enabled
                disableOverlays(*overlayPackageNames)

                // Install from files dir
                for (packageName in resourcesMap.keys) {
                    val apkName = "IconifyComponent${getOverlayName(packageName)}.apk"

                    Shell.cmd(
                        "pm install -r $DATA_DIR/$apkName",
                        "rm -rf $DATA_DIR/$apkName"
                    ).exec()
                }

                // Move to system overlay dir
                mountRW()
                for (packageName in resourcesMap.keys) {
                    val apkName = "IconifyComponent${getOverlayName(packageName)}.apk"

                    Shell.cmd("cp -rf $SIGNED_DIR/$apkName $SYSTEM_OVERLAY_DIR/$apkName").exec()
                    setPermissions(644, "/system/product/overlay/$apkName")
                }
                mountRO()

                // Enable the overlays
                enableOverlays(*overlayPackageNames)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build overlay! Exiting...", e)
            postExecute(true)
            return true
        }

        return false
    }

    @Throws(IOException::class)
    private suspend fun preExecute() = withContext(Dispatchers.IO) {
        // Create symbolic link
        symLinkBinaries()

        // Clean data directory
        Shell.cmd(
            "rm -rf $TEMP_DIR",
            "rm -rf $DATA_DIR/Overlays"
        ).exec()

        // Extract overlay from assets
        copyAssets("Overlays/$mPackage/$mOverlayName")

        // Create temp directory
        Shell.cmd(
            "rm -rf $TEMP_DIR; mkdir -p $TEMP_DIR",
            "mkdir -p $TEMP_OVERLAY_DIR",
            "mkdir -p $TEMP_CACHE_DIR",
            "mkdir -p $UNSIGNED_UNALIGNED_DIR",
            "mkdir -p $UNSIGNED_DIR",
            "mkdir -p $SIGNED_DIR",
            "mkdir -p $TEMP_CACHE_DIR/$mPackage/",
            "mkdir -p $BACKUP_DIR"
        ).exec()
    }

    private fun postExecute(hasErroredOut: Boolean) {
        if (!hasErroredOut) {
            // Move all generated overlays to module
            Shell.cmd(
                "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $OVERLAY_DIR/IconifyComponent$mOverlayName.apk"
            ).exec()
            setPermissions(644, "$OVERLAY_DIR/IconifyComponent$mOverlayName.apk")
            Shell.cmd(
                "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $BACKUP_DIR/IconifyComponent$mOverlayName.apk"
            ).exec()

            // Move to files dir
            if (mForce) {
                Shell.cmd(
                    "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $DATA_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()
                setPermissions(644, "$DATA_DIR/IconifyComponent$mOverlayName.apk")
            }
        }

        // Clean temp directory
        Shell.cmd(
            "rm -rf $TEMP_DIR",
            "rm -rf $DATA_DIR/Overlays"
        ).exec()
    }

    private fun moveOverlaysToCache() {
        Shell.cmd(
            "mv -f \"$DATA_DIR/Overlays/$mPackage/$mOverlayName\" \"$TEMP_CACHE_DIR/$mPackage/$mOverlayName\""
        ).exec().isSuccess
    }

    private fun createManifestResource(
        overlayName: String?,
        targetPackage: String?,
        source: String
    ): Boolean {
        Shell.cmd("mkdir -p $source/res").exec()

        val resourceTypes = arrayOf(
            ResourceType.PORTRAIT to "values",
            ResourceType.LANDSCAPE to "values-land",
            ResourceType.NIGHT to "values-night"
        )

        resourceTypes.forEach { (resourceType, values) ->
            val dirPath = "$source/res/${values}"
            val filePath = "$source/res/${values}/iconify.xml"
            val resourceList = mResource[resourceType]?.let { ArrayList(it) }

            if (!resourceList.isNullOrEmpty()) {
                val commands = mutableListOf(
                    "mkdir -p $dirPath",            // Create the directory
                    "rm -f $filePath",              // Remove the file if it exists
                    "touch $filePath"               // Create an empty file
                ).apply {
                    add("echo '${resourceList.joinToString("\n")}' >> $filePath")
                }

                Shell.cmd(*commands.toTypedArray()).exec()
            }
        }

        return OverlayCompiler.createManifest(overlayName, targetPackage, source)
    }

    private fun getOverlayName(packageName: String): String {
        return when (packageName) {
            FRAMEWORK_PACKAGE -> "Dynamic1"
            SYSTEMUI_PACKAGE -> "Dynamic2"
            PIXEL_LAUNCHER_PACKAGE -> "Dynamic3"
            LAUNCHER3_PACKAGE -> "Dynamic4"
            SETTINGS_PACKAGE -> "Dynamic5"
            else -> throw Exception("Unknown package: $packageName")
        }
    }
}
