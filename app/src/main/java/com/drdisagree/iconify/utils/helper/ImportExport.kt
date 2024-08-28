package com.drdisagree.iconify.utils.helper

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_PRIMARY_LIGHT
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.common.Preferences.COLOR_ACCENT_SECONDARY_LIGHT
import com.drdisagree.iconify.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.common.Preferences.ON_HOME_PAGE
import com.drdisagree.iconify.common.Preferences.QSPANEL_BLUR_SWITCH
import com.drdisagree.iconify.common.Preferences.SELECTED_ICON_SHAPE
import com.drdisagree.iconify.common.Preferences.SELECTED_PROGRESSBAR
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_BG
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_COLOR
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SET
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SHAPE
import com.drdisagree.iconify.common.Preferences.SELECTED_SETTINGS_ICONS_SIZE
import com.drdisagree.iconify.common.Preferences.SELECTED_SWITCH
import com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME
import com.drdisagree.iconify.common.Preferences.UI_CORNER_RADIUS
import com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_PRIMARY
import com.drdisagree.iconify.common.References.ICONIFY_COLOR_ACCENT_SECONDARY
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.common.Resources.MODULE_DIR
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.color.ColorUtils.colorNames
import com.drdisagree.iconify.utils.overlay.FabricatedUtils
import com.drdisagree.iconify.utils.overlay.compiler.DynamicCompiler
import com.drdisagree.iconify.utils.overlay.compiler.OnDemandCompiler
import com.drdisagree.iconify.utils.overlay.compiler.SwitchCompiler
import com.drdisagree.iconify.utils.overlay.manager.MonetEngineManager
import com.drdisagree.iconify.utils.overlay.manager.RoundnessManager
import com.drdisagree.iconify.utils.overlay.manager.SettingsIconResourceManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.topjohnwu.superuser.Shell
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.util.Objects
import java.util.concurrent.Executors

object ImportExport {

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
        context: Context,
        contentResolver: ContentResolver
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return

            Executors.newSingleThreadExecutor().execute {
                try {
                    exportSettings(
                        RPrefs.getPrefs,
                        contentResolver.openOutputStream(data.data!!)!!
                    )

                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_export_settings_successfull),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (exception: Exception) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            context.getString(R.string.toast_error),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("Settings", "Error exporting settings", exception)
                    }
                }
            }
        }
    }

    fun handleImportResult(
        result: ActivityResult,
        fragment: Fragment,
        loadingDialog: LoadingDialog
    ) {
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data ?: return
            MaterialAlertDialogBuilder(fragment.requireContext())
                .setTitle(fragment.getString(R.string.import_settings_confirmation_title))
                .setMessage(fragment.getString(R.string.import_settings_confirmation_desc))
                .setPositiveButton(fragment.getString(R.string.btn_positive)) { dialog, _ ->
                    dialog.dismiss()
                    loadingDialog.show(fragment.getString(R.string.loading_dialog_wait))
                    Executors.newSingleThreadExecutor().execute {
                        try {
                            val success = importSettings(
                                RPrefs.getPrefs,
                                fragment.requireContext().contentResolver.openInputStream(data.data!!)!!,
                                true
                            )
                            Handler(Looper.getMainLooper()).post {
                                loadingDialog.hide()
                                Toast.makeText(
                                    fragment.requireContext(),
                                    if (success) fragment.getString(R.string.toast_import_settings_successfull)
                                    else fragment.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (exception: Exception) {
                            Handler(Looper.getMainLooper()).post {
                                loadingDialog.hide()
                                Toast.makeText(
                                    fragment.requireContext(),
                                    fragment.getString(R.string.toast_error),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.e("Settings", "Error importing settings", exception)
                            }
                        }
                    }
                }
                .setNegativeButton(fragment.getString(R.string.btn_negative)) { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
    }

    fun exportSettings(preferences: SharedPreferences, outputStream: OutputStream) {
        try {
            outputStream.use {
                ObjectOutputStream(outputStream).use { objectOutputStream ->
                    objectOutputStream.writeObject(
                        preferences.all
                    )
                }
            }
        } catch (ioException: IOException) {
            Log.e("ExportSettings", "Error serializing preferences", ioException)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Throws(IOException::class)
    fun importSettings(
        sharedPreferences: SharedPreferences,
        inputStream: InputStream,
        restoreOverlays: Boolean
    ): Boolean {
        var objectInputStream: ObjectInputStream? = null
        val map: Map<String, Any>

        try {
            objectInputStream = ObjectInputStream(inputStream)
            map = objectInputStream.readObject() as Map<String, Any>
        } catch (exception: Exception) {
            Log.e("ImportSettings", "Error deserializing preferences", exception)
            return false
        } finally {
            objectInputStream?.close()
            inputStream.close()
        }

        val editor = sharedPreferences.edit()
        editor.clear()

        for ((key, value) in map) {
            when (value) {
                is Boolean -> {
                    editor.putBoolean(key, value)
                }

                is String -> {
                    editor.putString(key, value)
                }

                is Int -> {
                    editor.putInt(key, value)
                }

                is Float -> {
                    editor.putFloat(key, value)
                }

                is Long -> {
                    editor.putLong(key, value)
                }

                is Set<*> -> {
                    editor.putStringSet(key, value as Set<String?>)
                }

                else -> {
                    throw IllegalArgumentException("Type " + value.javaClass.getName() + " is unknown.")
                }
            }
        }

        val status = editor.commit()

        if (restoreOverlays) {
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
            var monetEngine = false
            var dynamicOverlay = false

            for ((key, value) in map) {
                if (value is Boolean) {
                    if (value) {
                        if (key.startsWith("IconifyComponent") && key.endsWith(".overlay")) { // Handling overlays
                            commands.add(addOverlay(key))
                            when {
                                key.contains("IconifyComponentSIP") && !systemIconPack -> { // Settings Icon Pack
                                    systemIconPack = true
                                    try {
                                        val selectedIcon = Objects.requireNonNull<Any?>(
                                            map[SELECTED_SETTINGS_ICONS_SET]
                                        ) as Int
                                        val selectedBackground = Objects.requireNonNull<Any?>(
                                            map[SELECTED_SETTINGS_ICONS_BG]
                                        ) as Int
                                        val selectedShape = Objects.requireNonNull<Any?>(
                                            map[SELECTED_SETTINGS_ICONS_SHAPE]
                                        ) as Int
                                        val selectedSize = Objects.requireNonNull<Any?>(
                                            map[SELECTED_SETTINGS_ICONS_SIZE]
                                        ) as Int
                                        val selectedIconColor = Objects.requireNonNull<Any?>(
                                            map[SELECTED_SETTINGS_ICONS_COLOR]
                                        ) as Int

                                        SettingsIconResourceManager.buildOverlay(
                                            selectedIcon,
                                            selectedBackground,
                                            selectedShape,
                                            selectedSize,
                                            selectedIconColor,
                                            false
                                        )
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building settings icon pack",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentPGB") && !progressBar -> { // Progressbar Style
                                    progressBar = true
                                    try {
                                        val selectedStyle = Objects.requireNonNull<Any?>(
                                            map[SELECTED_PROGRESSBAR]
                                        ) as Int

                                        OnDemandCompiler.buildOverlay(
                                            "PGB",
                                            selectedStyle + 1,
                                            FRAMEWORK_PACKAGE,
                                            false
                                        )
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building progressbar style",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentSWITCH") && !switch -> { // Switch Style
                                    switch = true
                                    try {
                                        val selectedStyle = Objects.requireNonNull<Any?>(
                                            map[SELECTED_SWITCH]
                                        ) as Int

                                        SwitchCompiler.buildOverlay(selectedStyle + 1, false)
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building switch style",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentTSTFRM") && !toastFrame -> { // Toast Frame Style
                                    toastFrame = true
                                    try {
                                        val selectedStyle = Objects.requireNonNull<Any?>(
                                            map[SELECTED_TOAST_FRAME]
                                        ) as Int

                                        OnDemandCompiler.buildOverlay(
                                            "TSTFRM",
                                            selectedStyle + 1,
                                            FRAMEWORK_PACKAGE,
                                            false
                                        )
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building toast frame style",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentSIS") && !systemIconShape -> { // Icon Shape Style
                                    systemIconShape = true
                                    try {
                                        val selectedStyle = Objects.requireNonNull<Any?>(
                                            map[SELECTED_ICON_SHAPE]
                                        ) as Int

                                        OnDemandCompiler.buildOverlay(
                                            "SIS",
                                            selectedStyle,
                                            FRAMEWORK_PACKAGE,
                                            false
                                        )
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

                                key.contains("IconifyComponentME") && !monetEngine -> { // Monet Engine
                                    monetEngine = true
                                    try {
                                        val colors = colorNames
                                        val palette: MutableList<List<List<Any>>> = ArrayList()
                                        val statNames = arrayOf("_day", "_night")

                                        for (stat in statNames) {
                                            val temp: MutableList<List<Any>> = ArrayList()
                                            for (types in colors) {
                                                val tmp: MutableList<Any> = ArrayList()
                                                for (color in types) {
                                                    tmp.add(
                                                        Objects.requireNonNull(
                                                            map[color + stat]
                                                        ).toString().toInt()
                                                    )
                                                }
                                                temp.add(tmp)
                                            }
                                            palette.add(temp)
                                        }

                                        MonetEngineManager.buildOverlay(palette, false)
                                    } catch (exception: Exception) {
                                        Log.e(
                                            "ImportSettings",
                                            "Error building Monet Engine",
                                            exception
                                        )
                                    }
                                }

                                key.contains("IconifyComponentDynamic") && !dynamicOverlay -> { // Dynamic overlays
                                    dynamicOverlay = true
                                    try {
                                        DynamicCompiler.buildOverlay(false)
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
            commands.add("find " + Resources.BACKUP_DIR + " -name \"IconifyComponent*.apk\" -exec cp {} " + Resources.DATA_DIR + " \\; ")

            // Change permissions for copied overlay APKs
            commands.add("find " + Resources.DATA_DIR + " -name \"IconifyComponent*.apk\" -exec chmod 644 {} \\; ")

            // Install overlay APKs
            commands.add("for file in " + Resources.DATA_DIR + "/IconifyComponent*.apk; do pm install -r \"\$file\"; done")

            // Remove copied overlay APKs
            commands.add("for file in " + Resources.DATA_DIR + "/IconifyComponent*.apk; do rm -f \"\$file\"; done")

            // Remount the filesystem as read-write
            commands.add("mount -o remount,rw /")

            // Copy overlay APKs to system overlay
            commands.add("find " + Resources.DATA_DIR + " -name \"IconifyComponent*.apk\" -exec cp {} " + Resources.SYSTEM_OVERLAY_DIR + " \\; ")

            // Change permissions for copied overlay APKs in system overlay
            commands.add("find " + Resources.SYSTEM_OVERLAY_DIR + " -name \"IconifyComponent*.apk\" -exec chmod 644 {} \\; ")

            // Remount the filesystem as read-only
            commands.add("mount -o remount,ro /")

            // Clear temp backup directory
            commands.add("rm -rf " + Resources.BACKUP_DIR)

            // Wait and restart SystemUI
            commands.add("sleep 3")
            commands.add("killall com.android.systemui")

            Shell.cmd(java.lang.String.join("; ", commands)).submit()
        }
        return status
    }

    private fun addOverlay(pkgName: String): String {
        return "cmd overlay enable --user current $pkgName; cmd overlay set-priority $pkgName highest"
    }
}
