package com.drdisagree.iconify.utils.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Preferences.COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.data.common.Preferences.COLOR_ACCENT_PRIMARY_LIGHT
import com.drdisagree.iconify.data.common.Preferences.COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.data.common.Preferences.COLOR_ACCENT_SECONDARY_LIGHT
import com.drdisagree.iconify.data.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.data.common.Preferences.ON_HOME_PAGE
import com.drdisagree.iconify.data.common.Preferences.QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.SELECTED_ICON_SHAPE
import com.drdisagree.iconify.data.common.Preferences.SELECTED_PROGRESSBAR
import com.drdisagree.iconify.data.common.Preferences.SELECTED_SETTINGS_ICONS_BG
import com.drdisagree.iconify.data.common.Preferences.SELECTED_SETTINGS_ICONS_COLOR
import com.drdisagree.iconify.data.common.Preferences.SELECTED_SETTINGS_ICONS_SET
import com.drdisagree.iconify.data.common.Preferences.SELECTED_SETTINGS_ICONS_SHAPE
import com.drdisagree.iconify.data.common.Preferences.SELECTED_SETTINGS_ICONS_SIZE
import com.drdisagree.iconify.data.common.Preferences.SELECTED_SWITCH
import com.drdisagree.iconify.data.common.Preferences.SELECTED_TOAST_FRAME
import com.drdisagree.iconify.data.common.Preferences.UI_CORNER_RADIUS
import com.drdisagree.iconify.data.common.References.ICONIFY_COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.data.common.References.ICONIFY_COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.DYNAMIC_RESOURCE_DATABASE_NAME
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.SYSTEM_OVERLAY_DIR
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_TEMP_DIR
import com.drdisagree.iconify.data.config.RPrefs.getPrefs
import com.drdisagree.iconify.data.database.DynamicResourceDatabase
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.utils.overlay.compiler.DynamicCompiler
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler
import com.drdisagree.iconify.utils.overlay.compiler.SwitchCompiler
import com.drdisagree.iconify.utils.overlay.manager.RoundnessManager
import com.drdisagree.iconify.utils.overlay.manager.SettingsIconResourceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.CompressionLevel
import net.lingala.zip4j.model.enums.CompressionMethod
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.util.Objects

object ImportExport {

    private const val PREFERENCE_BACKUP_FILE_NAME = "preference_backup"
    private const val DATABASE_BACKUP_FILE_NAME = "database_backup"

    fun importSettings(
        fragment: Fragment,
        activityIntent: ActivityResultLauncher<Intent>
    ) {
        importExportSettings(
            fragment = fragment,
            export = false,
            startExportActivityIntent = null,
            startImportActivityIntent = activityIntent
        )
    }

    fun exportSettings(
        fragment: Fragment,
        activityIntent: ActivityResultLauncher<Intent>
    ) {
        importExportSettings(
            fragment = fragment,
            export = true,
            startExportActivityIntent = activityIntent,
            startImportActivityIntent = null
        )
    }

    private fun importExportSettings(
        fragment: Fragment,
        export: Boolean,
        startExportActivityIntent: ActivityResultLauncher<Intent>? = null,
        startImportActivityIntent: ActivityResultLauncher<Intent>? = null
    ) {
        if (!hasStoragePermission()) {
            requestStoragePermission(fragment.requireContext())
        } else {
            val fileIntent = Intent().apply {
                action = if (export) Intent.ACTION_CREATE_DOCUMENT else Intent.ACTION_GET_CONTENT
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "configs.iconify")
            }

            if (export) {
                startExportActivityIntent!!.launch(fileIntent)
            } else {
                startImportActivityIntent!!.launch(fileIntent)
            }
        }
    }

    fun handleExportResult(
        result: ActivityResult,
        context: Context
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri: Uri = data?.data ?: return

            CoroutineScope(Dispatchers.IO).launch {
                val success = uri.exportSettings()

                withContext(Dispatchers.Main) {
                    if (success) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_export_settings_successfull),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    fun handleImportResult(
        result: ActivityResult,
        fragment: Fragment,
        loadingDialog: LoadingDialog?
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val uri: Uri = data?.data ?: return
            val context = appContextLocale

            MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(context.getString(R.string.import_settings_confirmation_title))
                .setMessage(context.getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(context.getString(R.string.btn_positive)) { dialog, _ ->
                    dialog.dismiss()

                    loadingDialog?.show(context.getString(R.string.loading_dialog_wait))

                    CoroutineScope(Dispatchers.IO).launch {
                        val success = uri.importSettings()

                        withContext(Dispatchers.Main) {
                            loadingDialog?.hide()

                            if (success) {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.toast_import_settings_successfull),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                .setNegativeButton(context.getString(R.string.btn_negative)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    suspend fun Uri.exportSettings(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val outputStream = appContext.contentResolver
                    .openOutputStream(this@exportSettings)
                    ?: throw IOException("Failed to open output stream")

                val tempZipFile = File(appContext.cacheDir, "backup.zip")

                if (tempZipFile.exists()) tempZipFile.delete()

                val zipFile = ZipFile(tempZipFile)
                val zipParameters = ZipParameters().apply {
                    compressionMethod = CompressionMethod.DEFLATE
                    compressionLevel = CompressionLevel.NORMAL
                }

                val prefsBackupStream = ByteArrayOutputStream().also { exportPreferences(it) }
                zipParameters.fileNameInZip = PREFERENCE_BACKUP_FILE_NAME
                zipFile.addStream(
                    ByteArrayInputStream(prefsBackupStream.toByteArray()),
                    zipParameters
                )

                val dbPath = appContext.getDatabasePath(DYNAMIC_RESOURCE_DATABASE_NAME).absolutePath
                val databaseFiles = listOf(
                    File(dbPath) to DYNAMIC_RESOURCE_DATABASE_NAME,
                    File("$dbPath-sh") to "$DYNAMIC_RESOURCE_DATABASE_NAME-sh",
                    File("$dbPath-shm") to "$DYNAMIC_RESOURCE_DATABASE_NAME-shm",
                    File("$dbPath-wal") to "$DYNAMIC_RESOURCE_DATABASE_NAME-wal"
                )

                databaseFiles.forEach { (dbFile, dbFileName) ->
                    if (dbFile.exists()) {
                        zipParameters.fileNameInZip = dbFileName
                        zipFile.addFile(dbFile, zipParameters)
                    }
                }

                if (XPOSED_RESOURCE_TEMP_DIR.exists() && XPOSED_RESOURCE_TEMP_DIR.isDirectory) {
                    zipFile.addFolder(XPOSED_RESOURCE_TEMP_DIR)
                }

                tempZipFile.inputStream().use { it.copyTo(outputStream) }

                tempZipFile.delete()

                true
            } catch (e: IOException) {
                Log.e("ExportSettings", "Error backing up settings", e)
                false
            }
        }
    }

    private suspend fun exportPreferences(outputStream: OutputStream) {
        withContext(Dispatchers.IO) {
            try {
                ObjectOutputStream(outputStream).use { objectOutputStream ->
                    val allPrefs = getPrefs.all.toMutableMap()
                    objectOutputStream.writeObject(allPrefs)
                    objectOutputStream.flush()
                }
            } catch (e: IOException) {
                Log.e("ExportSettings", "Error serializing preferences", e)
            }
        }
    }

    suspend fun Uri.importSettings(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val inputStreamLegacy = appContext.contentResolver
                    .openInputStream(this@importSettings)

                /*
                 * Start of backwards compatibility
                 */
                val success = restorePrefs(inputStreamLegacy, true)
                if (success) return@withContext true
                /*
                 * End of backwards compatibility
                 */

                val tempDir = File(appContext.cacheDir, "temp_restore").also {
                    if (it.exists()) {
                        it.deleteRecursively()
                    }
                    it.mkdirs()
                }

                val cacheFile = File(tempDir, "backup.zip")
                appContext.contentResolver.openInputStream(this@importSettings)
                    ?.use { inputStream ->
                        cacheFile.outputStream().use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }

                val zipFile = ZipFile(cacheFile)

                if (!zipFile.isValidZipFile) {
                    throw IOException("Invalid backup file")
                }

                val prefsFile = File(tempDir, PREFERENCE_BACKUP_FILE_NAME)
                zipFile.extractFile(PREFERENCE_BACKUP_FILE_NAME, tempDir.absolutePath)
                restorePrefs(prefsFile.inputStream(), false)

                val dbPath = appContext.getDatabasePath(DYNAMIC_RESOURCE_DATABASE_NAME).absolutePath
                val databaseFiles = listOf(
                    DATABASE_BACKUP_FILE_NAME to File(dbPath),
                    "$DATABASE_BACKUP_FILE_NAME-sh" to File("$dbPath-sh"),
                    "$DATABASE_BACKUP_FILE_NAME-shm" to File("$dbPath-shm"),
                    "$DATABASE_BACKUP_FILE_NAME-wal" to File("$dbPath-wal")
                )

                databaseFiles.forEach { (dbFileName, targetFile) ->
                    try {
                        val extractedFile = File(tempDir, dbFileName)
                        zipFile.extractFile(dbFileName, tempDir.absolutePath)
                        extractedFile.copyTo(targetFile, overwrite = true)
                    } catch (_: IOException) {
                    }
                }

                try {
                    if (XPOSED_RESOURCE_TEMP_DIR.exists()) {
                        XPOSED_RESOURCE_TEMP_DIR.deleteRecursively()
                    }
                    val extractedIconifyDir = File(tempDir, XPOSED_RESOURCE_TEMP_DIR.name)
                    zipFile.extractFile(XPOSED_RESOURCE_TEMP_DIR.name + "/", tempDir.absolutePath)
                    extractedIconifyDir.copyRecursively(XPOSED_RESOURCE_TEMP_DIR, overwrite = true)
                } catch (_: Exception) {
                }

                tempDir.deleteRecursively()

                // Reload database
                DynamicResourceDatabase.reloadInstance()

                true
            } catch (e: IOException) {
                Log.e("ImportSettings", "Error during restore", e)
                false
            }
        }
    }

    private suspend fun restorePrefs(
        inputStream: InputStream?,
        suppressException: Boolean
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                @Suppress("UNCHECKED_CAST")
                val map = ObjectInputStream(inputStream).use {
                    it.readObject() as Map<String, Any>
                }
                restorePrefsMap(map)
                true
            } catch (e: Exception) {
                if (!suppressException) {
                    Log.e("ImportSettings", "Error deserializing preferences", e)
                } else {
                    // Backwards compatibility
                }
                false
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    suspend fun restorePrefsMap(map: Map<String, Any>): Boolean {
        return withContext(Dispatchers.IO) {
            val editor = getPrefs.edit()
            editor.clear()

            map.forEach { (key, value) ->
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Long -> editor.putLong(key, value)
                    is Set<*> -> editor.putStringSet(key, value as Set<String?>)
                    else -> throw IllegalArgumentException("Type ${value.javaClass.simpleName} is unknown.")
                }
            }

            val status = editor.commit()

            val commands: MutableList<String> = ArrayList()
            commands.add("> $MODULE_DIR/system.prop; > $MODULE_DIR/post-exec.sh; for ol in $(cmd overlay list | grep -E '.x.*IconifyComponent' | sed -E 's/^.x..//'); do cmd overlay disable \$ol; done")

            SystemUtils.saveBootId
            SystemUtils.disableBlur(false)
            SystemUtils.saveVersionCode()

            editor.putBoolean(ON_HOME_PAGE, true)
            editor.putBoolean(FIRST_INSTALL, false)
            editor.putBoolean(QSPANEL_BLUR_SWITCH, false)

            var systemIconPack = false
            var progressBar = false
            var switch = false
            var toastFrame = false
            var systemIconShape = false
            var cornerRadius = false
            var dynamicOverlay = false

            map.forEach { (key, value) ->
                if (value is Boolean) {
                    if (value) {
                        if (key.startsWith("IconifyComponent") && key.endsWith(".overlay")) { // Handling overlays
                            commands.add(addOverlay(key))
                            when {
                                key.contains("IconifyComponentSIP") && !systemIconPack -> { // Settings Icon Pack
                                    try {
                                        val selectedIcon =
                                            map[SELECTED_SETTINGS_ICONS_SET] as? Int
                                        val selectedBackground =
                                            map[SELECTED_SETTINGS_ICONS_BG] as? Int
                                        val selectedShape =
                                            map[SELECTED_SETTINGS_ICONS_SHAPE] as? Int
                                        val selectedSize =
                                            map[SELECTED_SETTINGS_ICONS_SIZE] as? Int
                                        val selectedIconColor =
                                            map[SELECTED_SETTINGS_ICONS_COLOR] as? Int

                                        if (selectedIcon != null &&
                                            selectedBackground != null &&
                                            selectedShape != null &&
                                            selectedSize != null &&
                                            selectedIconColor != null
                                        ) {
                                            systemIconPack = true
                                            SettingsIconResourceManager.buildOverlay(
                                                selectedIcon,
                                                selectedBackground,
                                                selectedShape,
                                                selectedSize,
                                                selectedIconColor,
                                                false
                                            )
                                        }
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building settings icon pack",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentPGB") && !progressBar -> { // Progressbar Style
                                    try {
                                        val selectedStyle = map[SELECTED_PROGRESSBAR] as? Int

                                        if (selectedStyle != null) {
                                            progressBar = true
                                            OnDemandCompiler.buildOverlay(
                                                "PGB",
                                                selectedStyle + 1,
                                                FRAMEWORK_PACKAGE,
                                                false
                                            )
                                        }
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building progressbar style",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentSWITCH") && !switch -> { // Switch Style
                                    try {
                                        val selectedStyle = map[SELECTED_SWITCH] as? Int

                                        if (selectedStyle != null) {
                                            switch = true
                                            SwitchCompiler.buildOverlay(selectedStyle + 1, false)
                                        }
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building switch style",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentTSTFRM") && !toastFrame -> { // Toast Frame Style
                                    try {
                                        val selectedStyle = map[SELECTED_TOAST_FRAME] as? Int

                                        if (selectedStyle != null) {
                                            toastFrame = true
                                            OnDemandCompiler.buildOverlay(
                                                "TSTFRM",
                                                selectedStyle,
                                                FRAMEWORK_PACKAGE,
                                                false
                                            )
                                        }
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building toast frame style",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentSIS") && !systemIconShape -> { // Icon Shape Style
                                    try {
                                        val selectedStyle = map[SELECTED_ICON_SHAPE] as? Int

                                        if (selectedStyle != null) {
                                            systemIconShape = true
                                            OnDemandCompiler.buildOverlay(
                                                "SIS",
                                                selectedStyle,
                                                FRAMEWORK_PACKAGE,
                                                false
                                            )
                                        }
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building icon shape style",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentCR") && !cornerRadius -> { // UI Roundness
                                    cornerRadius = true
                                    try {
                                        val radius = map[UI_CORNER_RADIUS] as Int?

                                        radius?.let { RoundnessManager.buildOverlay(it, false) }
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building UI roundness",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentDynamic") && !dynamicOverlay -> { // Dynamic overlays
                                    dynamicOverlay = true
                                    try {
                                        DynamicCompiler.buildDynamicOverlay(false)
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building dynamic overlays",
                                            exception
                                        )
                                    }
                                }
                            }
                        } else if (key.startsWith("fabricated")) { // Handling fabricated overlays
                            val overlayName = key.replace("fabricated", "")
                            try {
                                if (map["FOCMDtarget$overlayName"] == null) {
                                    if (overlayName.contains(COLOR_ACCENT_PRIMARY)) {
                                        val build =
                                            "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimary android:color/holo_blue_light 0x1c $ICONIFY_COLOR_ACCENT_PRIMARY"
                                        val enable =
                                            "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimary"

                                        commands.add("echo -e \"$build\n$enable\" >> $MODULE_DIR/post-exec.sh")
                                        commands.add(build)
                                        commands.add(enable)
                                    }
                                    if (overlayName.contains(COLOR_ACCENT_PRIMARY_LIGHT)) {
                                        val build =
                                            "cmd overlay fabricate --target android --name IconifyComponentcolorAccentPrimaryLight android:color/holo_green_light 0x1c $ICONIFY_COLOR_ACCENT_PRIMARY"
                                        val enable =
                                            "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentPrimaryLight"

                                        commands.add("echo -e \"$build\n$enable\" >> $MODULE_DIR/post-exec.sh")
                                        commands.add(build)
                                        commands.add(enable)
                                    }
                                    if (overlayName.contains(COLOR_ACCENT_SECONDARY)) {
                                        val build =
                                            "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondary android:color/holo_blue_dark 0x1c $ICONIFY_COLOR_ACCENT_SECONDARY"
                                        val enable =
                                            "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondary"

                                        commands.add("echo -e \"$build\n$enable\" >> $MODULE_DIR/post-exec.sh")
                                        commands.add(build)
                                        commands.add(enable)
                                    }
                                    if (overlayName.contains(COLOR_ACCENT_SECONDARY_LIGHT)) {
                                        val build =
                                            "cmd overlay fabricate --target android --name IconifyComponentcolorAccentSecondaryLight android:color/holo_green_dark 0x1c $ICONIFY_COLOR_ACCENT_SECONDARY"
                                        val enable =
                                            "cmd overlay enable --user current com.android.shell:IconifyComponentcolorAccentSecondaryLight"

                                        commands.add("echo -e \"$build\n$enable\" >> $MODULE_DIR/post-exec.sh")
                                        commands.add(build)
                                        commands.add(enable)
                                    }
                                } else {
                                    val tempCommands = FabricatedUtils.buildCommands(
                                        Objects.requireNonNull(map["FOCMDtarget$overlayName"]) as String,
                                        Objects.requireNonNull(map["FOCMDname$overlayName"]) as String,
                                        Objects.requireNonNull(map["FOCMDtype$overlayName"]) as String,
                                        Objects.requireNonNull(map["FOCMDresourceName$overlayName"]) as String,
                                        Objects.requireNonNull(map["FOCMDval$overlayName"]) as String
                                    )

                                    commands.add("echo -e \"${tempCommands[0]}\n${tempCommands[1]}\" >> $MODULE_DIR/post-exec.sh")
                                    commands.add(tempCommands[0])
                                    commands.add(tempCommands[1])
                                }
                            } catch (exception: Exception) {
                                Log.e(
                                    "ImportSettings",
                                    "Error building fabricated commands",
                                    exception
                                )
                            }
                        }
                    }
                }
            }

            // Copy overlay APK files
            commands.add("find $BACKUP_DIR -name \"IconifyComponent*.apk\" -exec cp {} $DATA_DIR \\; ")

            // Change permissions for copied overlay APKs
            commands.add("find $DATA_DIR -name \"IconifyComponent*.apk\" -exec chmod 644 {} \\; ")

            // Install overlay APKs
            commands.add("for file in $DATA_DIR/IconifyComponent*.apk; do pm install -r \"\$file\"; done")

            // Remove copied overlay APKs
            commands.add("for file in $DATA_DIR/IconifyComponent*.apk; do rm -f \"\$file\"; done")

            // Remount the filesystem as read-write
            commands.add("mount -o remount,rw /")

            // Copy overlay APKs to system overlay
            commands.add("find $DATA_DIR -name \"IconifyComponent*.apk\" -exec cp {} $SYSTEM_OVERLAY_DIR \\; ")

            // Change permissions for copied overlay APKs in system overlay
            commands.add("find $SYSTEM_OVERLAY_DIR -name \"IconifyComponent*.apk\" -exec chmod 644 {} \\; ")

            // Remount the filesystem as read-only
            commands.add("mount -o remount,ro /")

            // Clear temp backup directory
            commands.add("rm -rf $BACKUP_DIR")

            // Wait and restart SystemUI
            commands.add("sleep 3")
            commands.add("killall com.android.systemui")

            Shell.cmd(commands.joinToString("; ")).submit()

            status
        }
    }

    private fun addOverlay(pkgName: String): String {
        return "cmd overlay enable --user current $pkgName; cmd overlay set-priority $pkgName highest"
    }
}
