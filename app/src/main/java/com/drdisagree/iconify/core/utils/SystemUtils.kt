package com.drdisagree.iconify.core.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import android.view.WindowInsets
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.data.common.Const
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.xposed.utils.BootLoopProtector
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

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
        val loadTimeKey = String.format(
            "%s%s",
            BootLoopProtector.LOAD_TIME_KEY_KEY,
            Const.SYSTEMUI_PACKAGE
        )
        val strikeKey = String.format(
            "%s%s",
            BootLoopProtector.PACKAGE_STRIKE_KEY_KEY,
            Const.SYSTEMUI_PACKAGE
        )
        val currentTime = Calendar.getInstance().time.time

        RPrefs.putLong(loadTimeKey, currentTime)
        RPrefs.putInt(strikeKey, 0)
        Shell.cmd("killall ${Const.SYSTEMUI_PACKAGE}").submit()
    }

    fun restartDevice() {
        Shell.cmd("am start -a android.intent.action.REBOOT").exec()
    }

    fun setMultiAudioFocusEnabled(enabled: Boolean) {
        val value = if (enabled) "1" else "0"

        Shell.cmd(
            "settings put system multi_audio_focus_enabled $value",
            "settings put --user 0 system multi_audio_focus_enabled $value"
        ).exec()
    }

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
            } catch (_: Exception) {
            }
        }
    }

    fun isBlurEnabled(force: Boolean): Boolean {
        return Shell.cmd(
            "if grep -q \"ro.surface_flinger.supports_background_blur\" " +
                    Resources.MODULE_DIR +
                    (if (force) "/service.sh;" else "/system.prop;") +
                    " then echo yes; else echo no; fi"
        ).exec().out[0] == "yes"
    }

    fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestStoragePermission(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
        }

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
            "sed '/^sleep.6/i killall ${Const.SYSTEMUI_PACKAGE}' ${Resources.MODULE_DIR}/service.sh > ${Resources.MODULE_DIR}/service.sh.tmp && mv ${Resources.MODULE_DIR}/service.sh.tmp ${Resources.MODULE_DIR}/service.sh"
        ).submit()
    }

    fun disableRestartSystemuiAfterBoot() {
        Shell.cmd(
            "grep -v \"killall ${Const.SYSTEMUI_PACKAGE}\" ${Resources.MODULE_DIR}/service.sh > ${Resources.MODULE_DIR}/service.sh.tmp && mv ${Resources.MODULE_DIR}/service.sh.tmp ${Resources.MODULE_DIR}/service.sh"
        ).submit()
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