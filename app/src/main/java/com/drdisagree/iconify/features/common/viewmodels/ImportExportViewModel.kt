package com.drdisagree.iconify.features.common.viewmodels

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.utils.SystemUtils
import com.drdisagree.iconify.core.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.core.utils.overlay.compilers.DynamicCompiler
import com.drdisagree.iconify.core.utils.overlay.compilers.OnDemandCompiler
import com.drdisagree.iconify.core.utils.overlay.managers.RoundnessManager
import com.drdisagree.iconify.core.utils.overlay.managers.SettingsIconResourceManager
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.LAUNCHER3_PACKAGE
import com.drdisagree.iconify.data.common.Const.PIXEL_LAUNCHER_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.RESOURCE_DATABASE_NAME
import com.drdisagree.iconify.data.common.Resources.SYSTEM_OVERLAY_DIR
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_FOLDER_NAME
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.database.ResourceDatabase
import com.drdisagree.iconify.data.keys.CustomizationKey
import com.drdisagree.iconify.data.keys.Key
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.data.keys.TweaksKey
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.data.states.ImportExportState
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
import javax.inject.Inject
import kotlin.math.roundToInt

@HiltViewModel
class ImportExportViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context
) : ViewModel() {

    private val _state = MutableStateFlow<ImportExportState>(ImportExportState.Idle)
    val state: StateFlow<ImportExportState> = _state.asStateFlow()

    fun createExportIntent(): Intent = Intent().apply {
        action = Intent.ACTION_CREATE_DOCUMENT
        type = "*/*"
        putExtra(Intent.EXTRA_TITLE, "configs.iconify")
    }

    fun createImportIntent(): Intent = Intent().apply {
        action = Intent.ACTION_GET_CONTENT
        type = "*/*"
    }

    /** Called after the user selects a destination URI for the export. */
    fun onExportUriReceived(uri: Uri) {
        viewModelScope.launch {
            _state.value = ImportExportState.Loading

            val success = uri.exportSettings()

            _state.value = if (success) {
                ImportExportState.Success(R.string.toast_export_settings_successfull)
            } else {
                ImportExportState.Failure(R.string.toast_error)
            }
        }
    }

    /**
     * Called after the user selects a backup file to import.
     * Transitions to [ImportExportState.AwaitingConfirmation] so the composable can show
     * a confirmation dialog before destructively overwriting settings.
     */
    fun onImportUriReceived(uri: Uri) {
        _state.value = ImportExportState.AwaitingConfirmation(uri)
    }

    /** Call this when the user confirms the import dialog. */
    fun confirmImport(uri: Uri) {
        viewModelScope.launch {
            _state.value = ImportExportState.Loading

            val success = uri.importSettings()

            _state.value = if (success) {
                ImportExportState.Success(R.string.toast_import_settings_successfull)
            } else {
                ImportExportState.Failure(R.string.toast_error)
            }
        }
    }

    /** Call this when the user dismisses the confirmation dialog. */
    fun cancelImport() {
        _state.value = ImportExportState.Idle
    }

    /** Reset back to Idle after the composable has consumed a Success/Failure event. */
    fun resetState() {
        _state.value = ImportExportState.Idle
    }

    private suspend fun Uri.exportSettings(): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputStream = context.contentResolver.openOutputStream(this@exportSettings)
                ?: throw IOException("Failed to open output stream")

            val tempZipFile = File(context.cacheDir, TEMP_BACKUP_ZIP_FILE_NAME).also {
                if (it.exists()) it.delete()
            }

            val zipFile = ZipFile(tempZipFile)
            val zipParameters = ZipParameters().apply {
                compressionMethod = CompressionMethod.DEFLATE
                compressionLevel = CompressionLevel.NORMAL
            }

            // 1. Preferences
            val prefsStream = ByteArrayOutputStream().also { exportPreferences(it) }
            zipParameters.fileNameInZip = PREFERENCE_BACKUP_FILE_NAME
            zipFile.addStream(ByteArrayInputStream(prefsStream.toByteArray()), zipParameters)

            // 2. Database files
            val dbPath = context.getDatabasePath(RESOURCE_DATABASE_NAME).absolutePath
            listOf(
                File(dbPath) to RESOURCE_DATABASE_NAME,
                File("$dbPath-sh") to "$RESOURCE_DATABASE_NAME-sh",
                File("$dbPath-shm") to "$RESOURCE_DATABASE_NAME-shm",
                File("$dbPath-wal") to "$RESOURCE_DATABASE_NAME-wal"
            ).forEach { (dbFile, dbFileName) ->
                if (dbFile.exists()) {
                    zipParameters.fileNameInZip = dbFileName
                    zipFile.addFile(dbFile, zipParameters)
                }
            }

            // 3. Xposed resource files
            queryXposedSharedFolderFiles().forEach { uri ->
                val name = context.contentResolver.query(
                    uri,
                    arrayOf(OpenableColumns.DISPLAY_NAME),
                    null, null, null
                )?.use { c -> if (c.moveToFirst()) c.getString(0) else null } ?: return@forEach

                val input = context.contentResolver.openInputStream(uri) ?: return@forEach
                zipParameters.fileNameInZip = "$ICONIFY_FILES_FOLDER_NAME/$name"
                zipFile.addStream(input, zipParameters)
            }

            // 4. Enabled overlays snapshot
            val overlayOutput = Shell.cmd("cmd overlay list").exec().out
            val enabledOverlays = overlayOutput
                .filter { line -> line.startsWith("[x] IconifyComponent") }
                .map { line -> line.substringAfter("[x] ").trim() }

            if (enabledOverlays.isNotEmpty()) {
                val content = enabledOverlays.joinToString("\n")
                zipParameters.fileNameInZip = ENABLED_OVERLAYS_FILE_NAME
                zipFile.addStream(
                    ByteArrayInputStream(content.toByteArray(Charsets.UTF_8)),
                    zipParameters
                )
            }

            // Flush zip to the chosen URI
            tempZipFile.inputStream().use { it.copyTo(outputStream) }
            tempZipFile.delete()

            true
        } catch (e: IOException) {
            Log.e(TAG, "Error backing up settings", e)
            false
        }
    }

    private suspend fun exportPreferences(outputStream: OutputStream) =
        withContext(Dispatchers.IO) {
            try {
                ObjectOutputStream(outputStream).use { oos ->
                    oos.writeObject(RPrefs.getPrefs.all.toMutableMap())
                    oos.flush()
                }
            } catch (e: IOException) {
                Log.e(TAG, "Error serializing preferences", e)
            }
        }

    private suspend fun Uri.importSettings(): Boolean = withContext(Dispatchers.IO) {
        try {
            // Backwards compatibility: old backups were a raw serialised prefs stream
            val legacyStream = context.contentResolver.openInputStream(this@importSettings)
            if (restorePrefs(
                    inputStream = legacyStream,
                    zipFile = null,
                    suppressException = true
                )
            ) return@withContext true

            // Modern zip-based backup
            val tempDir = File(context.cacheDir, "temp_restore").also {
                if (it.exists()) it.deleteRecursively()
                it.mkdirs()
            }

            val cacheFile = File(tempDir, TEMP_BACKUP_ZIP_FILE_NAME)
            context.contentResolver.openInputStream(this@importSettings)?.use { input ->
                cacheFile.outputStream().use { input.copyTo(it) }
            }

            val zipFile = ZipFile(cacheFile)
            if (!zipFile.isValidZipFile) throw IOException("Invalid backup file")

            // 1. Restore database files
            val dbPath = context.getDatabasePath(RESOURCE_DATABASE_NAME).absolutePath
            listOf(
                DATABASE_BACKUP_FILE_NAME to File(dbPath),
                "$DATABASE_BACKUP_FILE_NAME-sh" to File("$dbPath-sh"),
                "$DATABASE_BACKUP_FILE_NAME-shm" to File("$dbPath-shm"),
                "$DATABASE_BACKUP_FILE_NAME-wal" to File("$dbPath-wal")
            ).forEach { (zipName, target) ->
                try {
                    zipFile.extractFile(zipName, tempDir.absolutePath)
                    File(tempDir, zipName).copyTo(target, overwrite = true)
                } catch (_: IOException) {
                    /* file may not exist in older backups */
                }
            }

            // Reload the database instance so the app picks up restored data immediately
            ResourceDatabase.reloadInstance()

            // 2. Restore Xposed resource files
            deleteXposedFolderFiles()
            zipFile.extractAll(tempDir.absolutePath)
            File(tempDir, ICONIFY_FILES_FOLDER_NAME).listFiles()?.forEach { file ->
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, file.name)
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "Download/$XPOSED_RESOURCE_FOLDER_NAME"
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }

                val fileUri = context.contentResolver.insert(
                    MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY),
                    values
                ) ?: return@forEach

                context.contentResolver.openOutputStream(fileUri)?.use { out ->
                    file.inputStream().use { it.copyTo(out) }
                }

                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                context.contentResolver.update(fileUri, values, null, null)
            }

            // 3. Write post-exec.sh for fabricated overlays
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
            Shell.cmd(
                "cat << 'EOF' > $MODULE_DIR/post-exec.sh\n${postExec.trimEnd()}\nEOF"
            ).exec()

            // 4. Restore preferences and re-enable overlays
            val prefsFile = File(tempDir, PREFERENCE_BACKUP_FILE_NAME)
            zipFile.extractFile(PREFERENCE_BACKUP_FILE_NAME, tempDir.absolutePath)
            restorePrefs(
                inputStream = prefsFile.inputStream(),
                zipFile = zipFile,
                suppressException = false
            )

            tempDir.deleteRecursively()

            true
        } catch (e: IOException) {
            Log.e(TAG, "Error during restore", e)
            false
        }
    }

    private suspend fun restorePrefs(
        inputStream: InputStream?,
        zipFile: ZipFile?,
        suppressException: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            @Suppress("UNCHECKED_CAST")
            val map = ObjectInputStream(inputStream).use {
                it.readObject() as Map<String, Any>
            }

            val editor = RPrefs.getPrefs.edit()
            editor.clear()
            map.forEach { (key, value) ->
                when (value) {
                    is Boolean -> editor.putBoolean(key, value)
                    is String -> editor.putString(key, value)
                    is Int -> editor.putInt(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Long -> editor.putLong(key, value)
                    is Set<*> -> editor.putStringSet(
                        key,
                        @Suppress("UNCHECKED_CAST") (value as Set<String?>)
                    )

                    else -> throw IllegalArgumentException("Unknown type: ${value.javaClass.simpleName}")
                }
            }
            val committed = editor.commit()

            // Build the full shell command list
            val commands: MutableList<String> = mutableListOf(
                // Wipe module props and disable all currently active Iconify overlays
                """> $MODULE_DIR/system.prop; > $MODULE_DIR/post-exec.sh; """ +
                        """for ol in $(cmd overlay list | grep -E '.x.*IconifyComponent' | """ +
                        $$"""sed -E 's/^.x..//'); do cmd overlay disable $ol; done"""
            )

            SystemUtils.disableBlur(false)

            editor.apply {
                putBoolean(SettingsKey.ON_HOME_PAGE.name, true)
                putBoolean(SettingsKey.FIRST_INSTALL.name, false)
                putBoolean(XposedKey.QUICK_SETTINGS_BLUR.name, false)
            }

            val onDemandOverlaysPrefNames = onDemandOverlays.map { it.prefKey.name }
            map.forEach { (key, value) ->
                if (key in onDemandOverlaysPrefNames) {
                    val overlaySpec = onDemandOverlays.find { it.prefKey.name == key }!!

                    if (value != overlaySpec.prefKey.default) {
                        commands.add(
                            enableOverlayCmd(
                                "IconifyComponent" + overlaySpec.overlayName + ".overlay"
                            )
                        )
                    }

                    when (key) {
                        CustomizationKey.SETTINGS_ICONS_STYLE.name -> buildSettingsIconsOverlay(map)
                        TweaksKey.UI_CORNER_RADIUS.name -> buildCornerRadiusOverlay(map)
                        else -> buildOnDemandOverlay(key, map)
                    }
                }
            }

            if (zipFile != null) {
                try {
                    val header = zipFile.getFileHeader(ENABLED_OVERLAYS_FILE_NAME)

                    if (header != null) {
                        val overlayNames = zipFile.getInputStream(header)
                            .bufferedReader(Charsets.UTF_8)
                            .readLines()
                            .map { it.trim() }
                            .filter { it.isNotBlank() }

                        if (overlayNames.isNotEmpty()) {
                            overlayNames.forEach { name -> commands.add(enableOverlayCmd(name)) }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error restoring enabled overlays", e)
                }

                DynamicCompiler.buildDynamicOverlay(force = false)
            }

            // Install APKs, deploy to system overlay, restart SystemUI — all in one shot
            commands += listOf(
                """find $BACKUP_DIR -name "IconifyComponent*.apk" -exec cp {} $DATA_DIR \; """,
                """find $DATA_DIR -name "IconifyComponent*.apk" -exec chmod 644 {} \; """,
                $$"""for file in $$DATA_DIR/IconifyComponent*.apk; do pm install -r "$file"; done""",
                $$"""for file in $$DATA_DIR/IconifyComponent*.apk; do rm -f "$file"; done""",
                "mount -o remount,rw /",
                """find $DATA_DIR -name "IconifyComponent*.apk" -exec cp {} $SYSTEM_OVERLAY_DIR \; """,
                """find $SYSTEM_OVERLAY_DIR -name "IconifyComponent*.apk" -exec chmod 644 {} \; """,
                "mount -o remount,ro /",
                "rm -rf $BACKUP_DIR",
                "sleep 3",
                "killall $PIXEL_LAUNCHER_PACKAGE",
                "killall $LAUNCHER3_PACKAGE",
                "killall $SYSTEMUI_PACKAGE",
            )

            Shell.cmd(commands.joinToString("; ")).submit()

            committed
        } catch (e: Exception) {
            if (!suppressException) Log.e(TAG, "Error deserializing preferences", e)
            false
        }
    }

    /**
     * Rebuilds the Settings Icons overlay pack (SIP1 / SIP2 / SIP3).
     *
     * The pref stores "backgroundStyle,backgroundShape,iconSize,iconColor,iconSet"
     * with 0-indexed values; [SettingsIconResourceManager.buildOverlay] expects 1-indexed.
     */
    private fun buildSettingsIconsOverlay(map: Map<String, Any>) {
        try {
            val raw = map[CustomizationKey.SETTINGS_ICONS_STYLE.name] as? String ?: return
            val parts = raw.split(",").mapNotNull { it.toIntOrNull() }
            if (parts.size != 5 || parts.any { it < 0 }) return

            SettingsIconResourceManager.buildOverlay(
                backgroundStyle = parts[0] + 1,
                backgroundShape = parts[1] + 1,
                iconSize = parts[2] + 1,
                iconColor = parts[3] + 1,
                iconSet = parts[4] + 1,
                force = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error building settings icons overlay", e)
        }
    }

    /** Rebuilds the UI corner-radius overlay (CR*). */
    private fun buildCornerRadiusOverlay(map: Map<String, Any>) {
        try {
            val radius = map[TweaksKey.UI_CORNER_RADIUS_SAVED.name] as? Float ?: return
            if (radius == TweaksKey.UI_CORNER_RADIUS_SAVED.default) return

            RoundnessManager.buildOverlay(radius.roundToInt(), force = false)
        } catch (e: Exception) {
            Log.e(TAG, "Error building corner radius overlay", e)
        }
    }

    /**
     * Rebuilds any [OnDemandCompiler]-compiled overlay by looking up its strategy
     * in [onDemandOverlays].
     *
     * Unregistered categories are skipped with a warning — they'll still get the
     * enable command in the shell batch, but their APK won't be rebuilt here.
     */
    private fun buildOnDemandOverlay(key: String, map: Map<String, Any>) {
        val overlaySpec = onDemandOverlays.firstOrNull { it.prefKey.name == key } ?: run {
            Log.w(TAG, "No rebuild strategy for overlay category '$key' — APK not rebuilt")
            return
        }

        try {
            val style = map[overlaySpec.prefKey.name] as? String
            if (style.isNullOrBlank() || style == overlaySpec.prefKey.default) return

            OnDemandCompiler.buildOverlay(
                overlayName = overlaySpec.overlayName,
                style = style,
                targetPackage = overlaySpec.targetPackage,
                force = false
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error rebuilding OnDemand overlay '$key'", e)
        }
    }

    private fun queryXposedSharedFolderFiles(): List<Uri> {
        val resolver = context.contentResolver
        val collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        val uris = mutableListOf<Uri>()

        resolver.query(
            collection,
            arrayOf(MediaStore.Downloads._ID),
            "${MediaStore.Downloads.RELATIVE_PATH} LIKE ?",
            arrayOf("Download/$XPOSED_RESOURCE_FOLDER_NAME/%"),
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID)
            while (cursor.moveToNext()) {
                uris += ContentUris.withAppendedId(collection, cursor.getLong(idCol))
            }
        }
        return uris
    }

    private fun deleteXposedFolderFiles() {
        val resolver = context.contentResolver
        queryXposedSharedFolderFiles().forEach {
            try {
                resolver.delete(it, null, null)
            } catch (_: Exception) {
            }
        }
    }

    private fun enableOverlayCmd(pkgName: String) =
        "cmd overlay enable --user current $pkgName; cmd overlay set-priority $pkgName highest"

    private data class OnDemandOverlays(
        val overlayName: String,
        val prefKey: Key,
        val targetPackage: String
    )

    private val onDemandOverlays: List<OnDemandOverlays> = listOf(
        OnDemandOverlays("SGIC", CustomizationKey.CELLULAR_ICON_STYLE, SYSTEMUI_PACKAGE),
        OnDemandOverlays("WIFI", CustomizationKey.WIFI_ICON_STYLE, SYSTEMUI_PACKAGE),
        OnDemandOverlays("NFN", CustomizationKey.NOTIFICATION_STYLE, SYSTEMUI_PACKAGE),
        OnDemandOverlays("TSTFRM", CustomizationKey.TOAST_FRAME_STYLE, FRAMEWORK_PACKAGE),
        OnDemandOverlays("SIS", CustomizationKey.ICON_SHAPE_STYLE, FRAMEWORK_PACKAGE),
    )

    companion object {
        private const val TAG = "ImportExport"

        private const val PREFERENCE_BACKUP_FILE_NAME = "preference_backup"
        private const val DATABASE_BACKUP_FILE_NAME = "database_backup"
        private const val ICONIFY_FILES_FOLDER_NAME = ".iconify_files"
        private const val ENABLED_OVERLAYS_FILE_NAME = "enabled_overlays.txt"
        private const val TEMP_BACKUP_ZIP_FILE_NAME = "backup.zip"
    }
}