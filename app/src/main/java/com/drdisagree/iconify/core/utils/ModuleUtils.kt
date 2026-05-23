package com.drdisagree.iconify.core.utils

import android.net.Uri
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.app.MainActivity
import com.drdisagree.iconify.core.utils.AssetsUtils.readRawResource
import com.drdisagree.iconify.core.utils.RootUtils.setPermissionsRecursively
import com.drdisagree.iconify.core.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.RESTART_SYSUI_AFTER_BOOT
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_DIR
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.helpers.BackupRestore
import com.drdisagree.iconify.helpers.replaceAll
import com.topjohnwu.superuser.Shell
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.File

object ModuleUtils {

    private val TAG = ModuleUtils::class.java.simpleName

    suspend fun handleModule(skippedInstallation: Boolean) {
        if (moduleExists()) {
            // Clean temporary directory
            Shell.cmd("rm -rf $TEMP_MODULE_DIR").exec()

            // Backup necessary files
            BackupRestore.backupFiles()
        }

        installModule(skippedInstallation)
    }

    private suspend fun installModule(skippedInstallation: Boolean) {
        Log.d(TAG, "Module does not exist, creating...")

        FileUtils.ensureDirs(TEMP_MODULE_DIR)

        val moduleProp = readRawResource(R.raw.module_module).replaceAll(
            "{{VERSION_NAME}}" to BuildConfig.VERSION_NAME,
            "{{VERSION_CODE}}" to BuildConfig.VERSION_CODE,
            "{{APP_MOTO}}" to appContext.resources.getString(R.string.app_motto)
        )
        val postFsDataSh = readRawResource(
            if (RootUtils.isSusfsBinaryAvailable) R.raw.module_post_fs_data_susfs
            else R.raw.module_post_fs_data
        )
        val actionSh = readRawResource(R.raw.module_action).replaceAll(
            "{{PACKAGE_NAME}}" to BuildConfig.APPLICATION_ID,
            "{{CLASS_PATH}}" to MainActivity::class.qualifiedName!!
        )
        val uninstallSh = readRawResource(R.raw.module_uninstall)
        val serviceSh = if (!skippedInstallation) {
            readRawResource(R.raw.module_service).replaceAll(
                "{{RESTART_SYSUI_AFTER_BOOT}}" to ("killall $SYSTEMUI_PACKAGE\n"
                    .takeIf { RPrefs.getBoolean(RESTART_SYSUI_AFTER_BOOT, false) }
                    ?: "")
            )
        } else {
            readRawResource(R.raw.module_service_skipped)
        }

        File(TEMP_MODULE_DIR).apply {
            resolve("module.prop").writeText(moduleProp)
            resolve("post-fs-data.sh").writeText(postFsDataSh)
            resolve("service.sh").writeText(serviceSh)
            resolve("action.sh").writeText(actionSh)
            resolve("uninstall.sh").writeText(uninstallSh)
            resolve("system.prop").createNewFile()
            resolve("auto_mount").createNewFile()
        }

        FileUtils.ensureDirs("$TEMP_MODULE_DIR/system/product/overlay")

        createMETAINF()
        writePostExec()

        Log.i(TAG, "Module successfully created.")
    }

    private fun createMETAINF() {
        val updateBinary = readRawResource(R.raw.module_update_binary)

        FileUtils.ensureDirs("$TEMP_MODULE_DIR/META-INF/com/google/android")

        File("$TEMP_MODULE_DIR/META-INF/com/google/android").apply {
            resolve("update-binary").writeText(updateBinary)
            resolve("updater-script").writeText("#MAGISK")
        }
    }

    private suspend fun writePostExec() {
        val postExec = StringBuilder()

        FabricatedUtils.getAllLatestResources().forEach { resource ->
            val commands = FabricatedUtils.buildCommands(
                FabricatedUtils.FabricatedOverlay(
                    targetPackageName = resource.targetPackageName,
                    overlayName = resource.overlayName,
                    resourceType = FabricatedUtils.FabricatedResourceType.from(resource.resourceType),
                    resourceName = resource.resourceName,
                    resourceValue = resource.resourceValue
                )
            )

            postExec.append(commands.first).append('\n').append(commands.second).append('\n')
        }

        File("$TEMP_MODULE_DIR/post-exec.sh").writeText(postExec.toString())
    }

    fun moduleExists(): Boolean {
        return RootUtils.folderExists(Resources.OVERLAY_DIR)
    }

    @Throws(Exception::class)
    fun createModule(sourceFolder: String, destinationFilePath: String): String {
        val sourceDir = File(sourceFolder)
        val destinationZip = File(destinationFilePath)

        val zipFile = ZipFile(destinationZip)

        sourceDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val params = ZipParameters().apply {
                    fileNameInZip = sourceDir
                        .toURI()
                        .relativize(file.toURI())
                        .path
                    compressionMethod = CompressionMethod.DEFLATE
                    compressionLevel = CompressionLevel.NORMAL
                }

                appContext.contentResolver
                    .openInputStream(Uri.fromFile(file))!!
                    .use { input ->
                        zipFile.addStream(input, params)
                    }
            }

        return destinationZip.absolutePath
    }

    @Throws(Exception::class)
    fun flashModule(modulePath: String): Boolean {
        var result: Shell.Result? = null

        if (RootUtils.isMagiskInstalled) {
            result = Shell.cmd("magisk --install-module $modulePath").exec()
        } else if (RootUtils.isKSUInstalled) {
            result = Shell.cmd("ksud module install $modulePath").exec()
        } else if (RootUtils.isApatchInstalled) {
            result = Shell.cmd("apd module install $modulePath").exec()
            setPermissionsRecursively(MODULE_DIR)
        }

        if (result == null) {
            throw Exception("No supported root found")
        } else if (result.isSuccess) {
            Log.i(TAG, "Successfully flashed module")
        } else {
            Log.e(TAG, "Failed to flash module")
            throw Exception(java.lang.String.join("\n", result.out))
        }

        return !result.isSuccess
    }
}