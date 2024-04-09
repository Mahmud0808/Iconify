package com.drdisagree.iconify.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.view.WindowInsets
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.BOOT_ID
import com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_OVERLAY_STATE
import com.drdisagree.iconify.common.Preferences.FORCE_RELOAD_PACKAGE_NAME
import com.drdisagree.iconify.common.Preferences.RESTART_SYSUI_BEHAVIOR_EXT
import com.drdisagree.iconify.common.Preferences.VER_CODE
import com.drdisagree.iconify.common.References.DEVICE_BOOT_ID_CMD
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.config.Prefs.putString
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.xposed.utils.BootLoopProtector.LOAD_TIME_KEY_KEY
import com.drdisagree.iconify.xposed.utils.BootLoopProtector.PACKAGE_STRIKE_KEY_KEY
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

object SystemUtil {

    private var darkSwitching = false
    private const val BLUR_CMD_0 =
        "resetprop ro.surface_flinger.supports_background_blur 1 && killall surfaceflinger"
    private const val BLUR_CMD_1 = "ro.sf.blurs_are_expensive=1"
    private const val BLUR_CMD_2 = "ro.surface_flinger.supports_background_blur=1"
    private const val BLUR_CMD_3 = "persist.sys.sf.disable_blurs=0"
    private const val BLUR_CMD_4 = "persist.sysui.disableBlur=false"
    private const val BLUR_CMD_5 = "ro.config.avoid_gfx_accel=false"

    @JvmStatic
    val isDarkMode: Boolean
        get() = appContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES == Configuration.UI_MODE_NIGHT_YES

    @JvmStatic
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

    @JvmStatic
    fun handleSystemUIRestart() {
        val selectedBehavior = RPrefs.getInt(RESTART_SYSUI_BEHAVIOR_EXT, 0)

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

    @JvmStatic
    fun restartDevice() {
        Shell.cmd("am start -a android.intent.action.REBOOT").exec()
    }

    @JvmStatic
    fun disableBlur(force: Boolean) {
        Shell.cmd(
            if (!force) "mv " + Resources.MODULE_DIR +
                    "/system.prop " +
                    Resources.MODULE_DIR +
                    "/system.txt; " +
                    "grep -vE \"" +
                    BLUR_CMD_1 + "|" +
                    BLUR_CMD_2 + "|" +
                    BLUR_CMD_3 + "|" +
                    BLUR_CMD_4 + "|" +
                    BLUR_CMD_5 + "\" " +
                    Resources.MODULE_DIR +
                    "/system.txt > " +
                    Resources.MODULE_DIR +
                    "/system.txt.tmp; " +
                    "rm -rf " +
                    Resources.MODULE_DIR +
                    "/system.prop; " +
                    "mv " + Resources.MODULE_DIR +
                    "/system.txt.tmp " +
                    Resources.MODULE_DIR +
                    "/system.prop; " +
                    "rm -rf " + Resources.MODULE_DIR +
                    "/system.txt; " +
                    "rm -rf " + Resources.MODULE_DIR +
                    "/system.txt.tmp" else ":",  // do nothing
            "grep -v \"ro.surface_flinger.supports_background_blur\" " +
                    Resources.MODULE_DIR + "/service.sh > " +
                    Resources.MODULE_DIR + "/service.sh.tmp && mv " +
                    Resources.MODULE_DIR + "/service.sh.tmp " +
                    Resources.MODULE_DIR + "/service.sh"
        ).submit()
    }

    @JvmStatic
    fun enableBlur(force: Boolean) {
        disableBlur(false)
        Shell.cmd(
            "echo \"$BLUR_CMD_1\n$BLUR_CMD_2\n$BLUR_CMD_3\n$BLUR_CMD_4\n$BLUR_CMD_5\"" +
                    " >> ${Resources.MODULE_DIR}/system.prop",
            if (force) "sed '/*}/a " +
                    BLUR_CMD_0 + "' " +
                    Resources.MODULE_DIR +
                    "/service.sh > " +
                    Resources.MODULE_DIR +
                    "/service.sh.tmp && mv " +
                    Resources.MODULE_DIR +
                    "/service.sh.tmp " +
                    Resources.MODULE_DIR +
                    "/service.sh" else ":" // do nothing
        ).submit()
    }

    @JvmStatic
    fun mountRW() {
        Shell.cmd("mount -o remount,rw /").exec()

        if (RootUtil.moduleExists("magisk_overlayfs")) {
            Shell.cmd("-mm -c magic_remount_rw").exec()
        } else if (RootUtil.moduleExists("overlayfs")) {
            Shell.cmd("/data/overlayfs/tmp/overlayrw -rw /system/product/overlay").exec()
        }
    }

    @JvmStatic
    fun mountRO() {
        Shell.cmd("mount -o remount,ro /").exec()

        if (RootUtil.moduleExists("magisk_overlayfs")) {
            Shell.cmd("-mm -c magic_remount_ro").exec()
        } else if (RootUtil.moduleExists("overlayfs")) {
            Shell.cmd("/data/overlayfs/tmp/overlayrw -ro /system/product/overlay").exec()
        }
    }

    /*
     * From AOSPMods
     * https://github.com/siavash79/AOSPMods/blob/canary/app/src/main/java/sh/siava/AOSPMods/utils/SystemUtils.java
     */
    @JvmStatic
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

    @JvmStatic
    fun isBlurEnabled(force: Boolean): Boolean {
        return Shell.cmd(
            "if grep -q \"ro.surface_flinger.supports_background_blur\" " +
                    Resources.MODULE_DIR +
                    (if (force) "/service.sh;" else "/system.prop;") +
                    " then echo yes; else echo no; fi"
        ).exec().out[0] == "yes"
    }

    @JvmStatic
    val saveBootId: Unit // Save unique id of each boot
        get() {
            putString(BOOT_ID, Shell.cmd(DEVICE_BOOT_ID_CMD).exec().out.toString())
        }

    @JvmStatic
    fun saveVersionCode() {
        Prefs.putInt(VER_CODE, BuildConfig.VERSION_CODE)
    }

    @JvmStatic
    val savedVersionCode: Int
        get() = Prefs.getInt(VER_CODE, -1)

    @JvmStatic
    fun hasStoragePermission(): Boolean {
        return Environment.isExternalStorageManager() || Environment.isExternalStorageLegacy()
    }

    @JvmStatic
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

    @JvmStatic
    fun enableRestartSystemuiAfterBoot() {
        disableRestartSystemuiAfterBoot()

        Shell.cmd(
            "sed '/^sleep.6/i killall " + SYSTEMUI_PACKAGE + "' " + Resources.MODULE_DIR + "/service.sh > " + Resources.MODULE_DIR + "/service.sh.tmp && mv " + Resources.MODULE_DIR + "/service.sh.tmp " + Resources.MODULE_DIR + "/service.sh"
        ).submit()
    }

    @JvmStatic
    fun disableRestartSystemuiAfterBoot() {
        Shell.cmd(
            "grep -v \"killall " + SYSTEMUI_PACKAGE + "\" " + Resources.MODULE_DIR + "/service.sh > " + Resources.MODULE_DIR + "/service.sh.tmp && mv " + Resources.MODULE_DIR + "/service.sh.tmp " + Resources.MODULE_DIR + "/service.sh"
        ).submit()
    }

    fun getScreenWidth(activity: Activity): Int {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets = windowMetrics.getWindowInsets()
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        return windowMetrics.bounds.width() - (insets.left + insets.right)
    }

    fun getScreenHeight(activity: Activity): Int {
        val windowMetrics = activity.windowManager.currentWindowMetrics
        val insets = windowMetrics.getWindowInsets()
            .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
        return windowMetrics.bounds.height() - (insets.top + insets.bottom)
    }
}
