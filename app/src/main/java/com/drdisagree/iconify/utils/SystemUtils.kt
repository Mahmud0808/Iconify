package com.drdisagree.iconify.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.BOOT_ID
import com.drdisagree.iconify.data.common.Preferences.FORCE_RELOAD_OVERLAY_STATE
import com.drdisagree.iconify.data.common.Preferences.FORCE_RELOAD_PACKAGE_NAME
import com.drdisagree.iconify.data.common.Preferences.RESTART_SYSUI_BEHAVIOR_EXT
import com.drdisagree.iconify.data.common.Preferences.VER_CODE
import com.drdisagree.iconify.data.common.References.DEVICE_BOOT_ID_CMD
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.config.RPrefs.getString
import com.drdisagree.iconify.data.config.RPrefs.putString
import com.drdisagree.iconify.xposed.utils.BootLoopProtector.LOAD_TIME_KEY_KEY
import com.drdisagree.iconify.xposed.utils.BootLoopProtector.PACKAGE_STRIKE_KEY_KEY
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object SystemUtils {

    private var darkSwitching = false
    private const val BLUR_CMD_0 =
        "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger"
    private const val BLUR_CMD_1 = "ro.sf.blurs_are_expensive=1"
    private const val BLUR_CMD_2 = "ro.surface_flinger.supports_background_blur=1"
    private const val BLUR_CMD_3 = "persist.sys.sf.disable_blurs=0"
    private const val BLUR_CMD_4 = "persist.sysui.disableBlur=false"
    private const val BLUR_CMD_5 = "ro.config.avoid_gfx_accel=false"

    val isDarkMode: Boolean
        get() = appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES == Configuration.UI_MODE_NIGHT_YES

    fun restartSystemUI() {
        val loadTimeKey = String.format("%s%s", LOAD_TIME_KEY_KEY, SYSTEMUI_PACKAGE)
        val strikeKey = String.format("%s%s", PACKAGE_STRIKE_KEY_KEY, SYSTEMUI_PACKAGE)
        val currentTime = Calendar.getInstance().time.time

        RPrefs.putLong(loadTimeKey, currentTime)
        RPrefs.putInt(strikeKey, 0)
        Shell.cmd("killall $SYSTEMUI_PACKAGE").submit()
    }

    private fun forceReloadUI() {
        val state = RPrefs.getBoolean(FORCE_RELOAD_OVERLAY_STATE, false)
        val pkgName: String = FORCE_RELOAD_PACKAGE_NAME

        Shell.cmd(
            "cmd overlay " + (if (state) "disable" else "enable") + " --user current " + pkgName + "; cmd overlay " + (if (state) "enable" else "disable") + " --user current " + pkgName
        ).submit()
    }

    fun handleSystemUIRestart() {
        val selectedBehavior = getString(RESTART_SYSUI_BEHAVIOR_EXT, "0")!!.toInt()

        when (selectedBehavior) {
            0 -> {
                restartSystemUI()
            }

            1 -> {
                forceReloadUI()
            }

            else -> {
                Toast.makeText(
                    appContext,
                    appContext.resources.getString(R.string.settings_systemui_restart_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun restartDevice() {
        Shell.cmd("am start -a android.intent.action.REBOOT").exec()
    }

    fun disableBlur(force: Boolean) {
        Shell.cmd(
            if (!force) "mv " + MODULE_DIR +
                    "/system.prop " +
                    MODULE_DIR +
                    "/system.txt; " +
                    "grep -vE \"" +
                    BLUR_CMD_1 + "|" +
                    BLUR_CMD_2 + "|" +
                    BLUR_CMD_3 + "|" +
                    BLUR_CMD_4 + "|" +
                    BLUR_CMD_5 + "\" " +
                    MODULE_DIR +
                    "/system.txt > " +
                    MODULE_DIR +
                    "/system.txt.tmp; " +
                    "rm -rf " +
                    MODULE_DIR +
                    "/system.prop; " +
                    "mv " + MODULE_DIR +
                    "/system.txt.tmp " +
                    MODULE_DIR +
                    "/system.prop; " +
                    "rm -rf " + MODULE_DIR +
                    "/system.txt; " +
                    "rm -rf " + MODULE_DIR +
                    "/system.txt.tmp" else ":",  // do nothing
            "grep -v \"ro.surface_flinger.supports_background_blur\" " +
                    MODULE_DIR + "/service.sh > " +
                    MODULE_DIR + "/service.sh.tmp && mv " +
                    MODULE_DIR + "/service.sh.tmp " +
                    MODULE_DIR + "/service.sh"
        ).submit()
    }

    fun enableBlur(force: Boolean) {
        disableBlur(false)
        Shell.cmd(
            "echo \"$BLUR_CMD_1\n$BLUR_CMD_2\n$BLUR_CMD_3\n$BLUR_CMD_4\n$BLUR_CMD_5\"" +
                    " >> $MODULE_DIR/system.prop",
            if (force) "sed '/*}/a " +
                    BLUR_CMD_0 + "' " +
                    MODULE_DIR +
                    "/service.sh > " +
                    MODULE_DIR +
                    "/service.sh.tmp && mv " +
                    MODULE_DIR +
                    "/service.sh.tmp " +
                    MODULE_DIR +
                    "/service.sh" else ":" // do nothing
        ).submit()
    }

    private fun clearExpressiveThemeFromServiceSh() {
        Shell.cmd(
            "grep -v -e \"setprop is_expressive_design_enabled true\" " +
                    "-e \"setprop is_expressive_design_enabled false\" " +
                    MODULE_DIR + "/service.sh > " +
                    MODULE_DIR + "/service.sh.tmp && mv " +
                    MODULE_DIR + "/service.sh.tmp " +
                    MODULE_DIR + "/service.sh"
        ).exec()
    }

    fun switchExpressiveTheme(enable: Boolean) {
        clearExpressiveThemeFromServiceSh()

        Shell.cmd("setprop is_expressive_design_enabled ${if (enable) "true" else "false"}").exec()
        Shell.cmd(
            "sed '/sleep 6/a setprop is_expressive_design_enabled ${if (enable) "true" else "false"}' " +
                    MODULE_DIR + "/service.sh > " +
                    MODULE_DIR + "/service.sh.tmp && mv " +
                    MODULE_DIR + "/service.sh.tmp " +
                    MODULE_DIR + "/service.sh"
        ).submit()
    }

    fun mountRW() {
        Shell.cmd("mount -o remount,rw /").exec()

        if (RootUtils.moduleExists("magisk_overlayfs")) {
            Shell.cmd("-mm -c magic_remount_rw").exec()
        } else if (RootUtils.moduleExists("overlayfs")) {
            Shell.cmd("/data/overlayfs/tmp/overlayrw -rw /system/product/overlay").exec()
        }
    }

    fun mountRO() {
        Shell.cmd("mount -o remount,ro /").exec()

        if (RootUtils.moduleExists("magisk_overlayfs")) {
            Shell.cmd("-mm -c magic_remount_ro").exec()
        } else if (RootUtils.moduleExists("overlayfs")) {
            Shell.cmd("/data/overlayfs/tmp/overlayrw -ro /system/product/overlay").exec()
        }
    }

    /*
     * From AOSPMods
     * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/utils/SystemUtils.java
     */
    fun doubleToggleDarkMode() {
        val isDark = isDarkMode

        CoroutineScope(Dispatchers.Default).launch {
            try {
                while (darkSwitching) {
                    delay(100)
                }

                darkSwitching = true

                Shell.cmd("cmd uimode night ${if (isDark) "no" else "yes"}").exec()
                delay(1000)
                Shell.cmd("cmd uimode night ${if (isDark) "yes" else "no"}").exec()
                delay(500)

                darkSwitching = false
            } catch (ignored: Exception) {
            }
        }
    }

    fun isBlurEnabled(force: Boolean): Boolean {
        return Shell.cmd(
            "if grep -q \"ro.surface_flinger.supports_background_blur\" " +
                    MODULE_DIR +
                    (if (force) "/service.sh;" else "/system.prop;") +
                    " then echo yes; else echo no; fi"
        ).exec().out[0] == "yes"
    }

    fun isExpressiveThemeEnabled(): Boolean {
        return Shell.cmd(
            "if grep -q \"setprop is_expressive_design_enabled true\" " +
                    "$MODULE_DIR/service.sh;" +
                    " then echo yes; else echo no; fi"
        ).exec().out[0] == "yes"
    }

    val saveBootId: Unit // Save unique id of each boot
        get() {
            val bootId = Shell.cmd(DEVICE_BOOT_ID_CMD).exec().out.toString()
            if (getString(BOOT_ID) != bootId) {
                putString(BOOT_ID, bootId)
            }
        }

    fun saveVersionCode() {
        RPrefs.putInt(VER_CODE, BuildConfig.VERSION_CODE)
    }

    val savedVersionCode: Int
        get() = RPrefs.getInt(VER_CODE, -1)

    fun hasStoragePermission(): Boolean {
        return Environment.isExternalStorageManager() || Environment.isExternalStorageLegacy()
    }

    fun requestStoragePermission(context: Context) {
        val intent = Intent()
        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.setData(Uri.fromParts("package", BuildConfig.APPLICATION_ID, null))

        (context as Activity).startActivityForResult(intent, 0)
        ActivityCompat.requestPermissions(
            context, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.MANAGE_EXTERNAL_STORAGE
            ), 0
        )
    }

    fun enableRestartSystemuiAfterBoot() {
        disableRestartSystemuiAfterBoot()

        Shell.cmd(
            "sed '/^sleep.6/i killall $SYSTEMUI_PACKAGE' $MODULE_DIR/service.sh > $MODULE_DIR/service.sh.tmp && mv $MODULE_DIR/service.sh.tmp $MODULE_DIR/service.sh"
        ).submit()
    }

    fun disableRestartSystemuiAfterBoot() {
        Shell.cmd(
            "grep -v \"killall $SYSTEMUI_PACKAGE\" $MODULE_DIR/service.sh > $MODULE_DIR/service.sh.tmp && mv $MODULE_DIR/service.sh.tmp $MODULE_DIR/service.sh"
        ).submit()
    }

    fun isSecurityPatchBeforeJune2024(): Boolean {
        val securityPatch = Build.VERSION.SECURITY_PATCH
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        return try {
            val securityPatchDate = dateFormat.parse(securityPatch)

            val june2024 = Calendar.getInstance()
            june2024.set(2024, Calendar.JUNE, 1)

            (securityPatchDate != null && (securityPatchDate < june2024.time))
        } catch (e: Exception) {
            Log.e("SECURITY_PATCH_CHECK", "Error parsing security patch date", e)
            false
        }
    }

    fun getScreenWidth(activity: Activity): Int {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        return windowMetrics.bounds.width() - (insets.left + insets.right)
    }

    fun getScreenHeight(activity: Activity): Int {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets = windowMetrics.windowInsets
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        return windowMetrics.bounds.height() - (insets.top + insets.bottom)
    }
}
