package com.drdisagree.iconify.core.utils.overlay.compilers

import android.util.Log
import com.drdisagree.iconify.core.utils.AssetsUtils
import com.drdisagree.iconify.core.utils.FileUtils
import com.drdisagree.iconify.core.utils.Logger
import com.drdisagree.iconify.core.utils.RootUtils
import com.drdisagree.iconify.core.utils.SystemUtils
import com.drdisagree.iconify.core.utils.overlay.OverlayUtils
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceManager
import com.drdisagree.iconify.core.utils.overlay.resource.ResourceType
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.LAUNCHER3_PACKAGE
import com.drdisagree.iconify.data.common.Const.PIXEL_LAUNCHER_PACKAGE
import com.drdisagree.iconify.data.common.Const.SETTINGS_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.SIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.SYSTEM_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_CACHE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_UNALIGNED_DIR
import com.drdisagree.iconify.helpers.BinaryInstaller
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

object DynamicCompiler {

    private val TAG = DynamicCompiler::class.java.simpleName
    private var mForce = false
    private var mPackage: String? = null
    private var mOverlayName: String? = null
    private val mResource: MutableMap<ResourceType, ArrayList<String>> = mutableMapOf()

    @Throws(IOException::class)
    suspend fun buildDynamicOverlay(
        packagesToUpdate: List<String> = emptyList(),
        force: Boolean = true,
    ): Boolean {
        mForce = force

        try {
            val resourcesMap = ResourceManager.generateXmlStructureForAllResources(
                packagesToUpdate.ifEmpty {
                    listOf(
                        FRAMEWORK_PACKAGE,
                        SYSTEMUI_PACKAGE,
                        PIXEL_LAUNCHER_PACKAGE,
                        LAUNCHER3_PACKAGE,
                        SETTINGS_PACKAGE
                    )
                }
            )

            // Create overlay for each package
            for (packageName in resourcesMap.keys) {
                Log.i(TAG, "Generating dynamic overlay for $packageName")

                mPackage = packageName

                mResource.clear()
                mResource[ResourceType.PORTRAIT] = ArrayList(
                    resourcesMap[packageName]!![ResourceType.PORTRAIT]!!
                )
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
                OverlayUtils.disableOverlays(*overlayPackageNames)

                // Install from files dir
                for (packageName in resourcesMap.keys) {
                    val apkName = "IconifyComponent${getOverlayName(packageName)}.apk"

                    Shell.cmd(
                        "pm install -r $DATA_DIR/$apkName",
                        "rm -rf $DATA_DIR/$apkName"
                    ).exec()
                }

                // Move to system overlay dir
                SystemUtils.mountRW()
                for (packageName in resourcesMap.keys) {
                    val apkName = "IconifyComponent${getOverlayName(packageName)}.apk"

                    Shell.cmd("cp -rf $SIGNED_DIR/$apkName $SYSTEM_OVERLAY_DIR/$apkName").exec()
                    RootUtils.setPermissions(644, "/system/product/overlay/$apkName")
                }
                SystemUtils.mountRO()

                // Enable the overlays
                OverlayUtils.enableOverlays(*overlayPackageNames)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to build overlay! Exiting...", e)
            Logger.writeLog(
                tag = TAG,
                header = "Dynamic overlay build failed",
                exception = e
            )
            postExecute(true)
            return true
        }

        return false
    }

    @Throws(IOException::class)
    private suspend fun preExecute() = withContext(Dispatchers.IO) {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries()

        // Clean data directory
        Shell.cmd(
            "rm -rf $TEMP_DIR",
            "rm -rf $DATA_DIR/Overlays"
        ).exec()

        // Extract overlay from assets
        AssetsUtils.copyAssets("Overlays/$mPackage/$mOverlayName")

        // Create temp directory
        FileUtils.ensureDirs(
            TEMP_DIR,
            TEMP_OVERLAY_DIR,
            TEMP_CACHE_DIR,
            UNSIGNED_UNALIGNED_DIR,
            UNSIGNED_DIR,
            SIGNED_DIR,
            "$TEMP_CACHE_DIR/$mPackage",
            BACKUP_DIR
        )
    }

    private fun postExecute(hasErroredOut: Boolean) {
        if (!hasErroredOut) {
            // Move all generated overlays to module
            Shell.cmd(
                "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $OVERLAY_DIR/IconifyComponent$mOverlayName.apk"
            ).exec()
            RootUtils.setPermissions(644, "$OVERLAY_DIR/IconifyComponent$mOverlayName.apk")

            if (mForce) {
                // Move to files dir
                Shell.cmd(
                    "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $DATA_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()
                RootUtils.setPermissions(644, "$DATA_DIR/IconifyComponent$mOverlayName.apk")
            } else {
                Shell.cmd(
                    "cp -rf $SIGNED_DIR/IconifyComponent$mOverlayName.apk $BACKUP_DIR/IconifyComponent$mOverlayName.apk"
                ).exec()
            }
        }

        // Clean temp directory
        Shell.cmd(
            "rm -rf $TEMP_DIR",
            "rm -rf $DATA_DIR/Overlays"
        ).exec()
    }

    private fun moveOverlaysToCache(): Boolean {
        return try {
            val source = Paths.get("$DATA_DIR/Overlays/$mPackage/$mOverlayName")
            val target = Paths.get("$TEMP_CACHE_DIR/$mPackage/$mOverlayName")

            Files.createDirectories(target.parent)

            Files.move(
                source,
                target,
                StandardCopyOption.REPLACE_EXISTING
            )

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to move overlay", e)
            Logger.writeLog(
                tag = TAG,
                header = "Dynamic overlay moving failed",
                exception = e
            )
            false
        }
    }

    private fun createManifestResource(
        overlayName: String?,
        targetPackage: String?,
        source: String
    ): Boolean {
        FileUtils.ensureDirs("$source/res")

        val resourceTypes = arrayOf(
            ResourceType.PORTRAIT to "values",
            ResourceType.LANDSCAPE to "values-land",
            ResourceType.NIGHT to "values-night"
        )

        resourceTypes.forEach { (resourceType, folderName) ->
            val dir = File("$source/res/$folderName")
            val file = File(dir, "iconify.xml")

            val resourceList = mResource[resourceType]?.let { ArrayList(it) }

            if (!resourceList.isNullOrEmpty()) {
                FileUtils.ensureDirs(dir.absolutePath)
                file.writeText(resourceList.joinToString("\n"))
            }
        }

        return OverlayCompiler.createManifest(
            overlayName,
            targetPackage,
            source
        )
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