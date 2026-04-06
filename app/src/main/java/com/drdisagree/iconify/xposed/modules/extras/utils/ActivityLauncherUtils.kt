package com.drdisagree.iconify.xposed.modules.extras.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookEntry.Companion.moduleResources
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log

class ActivityLauncherUtils(private val mContext: Context, private val mActivityStarter: Any?) {
    private val mPackageManager: PackageManager = mContext.packageManager

    private fun launchAppIfAvailable(launchIntent: Intent?, @StringRes appTypeResId: Int) {
        val apps =
            mPackageManager.queryIntentActivities(launchIntent!!, PackageManager.MATCH_DEFAULT_ONLY)
        if (apps.isNotEmpty()) {
            if (mActivityStarter == null) {
                log(this@ActivityLauncherUtils, "ActivityStarter is null")
                return
            }
            mActivityStarter.callMethod("startActivity", launchIntent, true)
        } else {
            if (appTypeResId != 0) showNoDefaultAppFoundToast(appTypeResId)
        }
    }

    fun launchApp(launchIntent: Intent?, fromQs: Boolean = false) {
        if (launchIntent == null) return
        if (mActivityStarter == null) {
            log(this@ActivityLauncherUtils, "ActivityStarter is null")
            return
        }
        mActivityStarter.callMethod(
            "postStartActivityDismissingKeyguard",
            launchIntent, /* dismissShade */
            0
        )
    }

    fun launchCamera() {
        val launchIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE)
        launchAppIfAvailable(launchIntent, R.string.camera)
    }

    fun launchTimer() {
        val intent = Intent().apply {
            action = "android.intent.action.SHOW_ALARMS"
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        launchAppIfAvailable(intent, R.string.clock_timer)
    }

    fun launchCalculator() {
        val launchIntent = Intent(Intent.ACTION_MAIN)
        launchIntent.addCategory(Intent.CATEGORY_APP_CALCULATOR)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        launchAppIfAvailable(launchIntent, R.string.calculator)
    }

    fun launchWallet() {
        val launchIntent =
            mContext.packageManager.getLaunchIntentForPackage("com.google.android.apps.walletnfcrel")
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        launchAppIfAvailable(launchIntent, R.string.wallet)
    }

    fun launchSettingsComponent(className: String) {
        if (mActivityStarter == null) return
        val intent = Intent().apply {
            component = ComponentName("com.android.settings", className)
        }
        mActivityStarter.callMethod("startActivity", intent, true)
    }

    fun launchBluetoothSettings() {
        val launchIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        launchAppIfAvailable(launchIntent, 0)
    }

    fun launchAudioSettings() {
        val launchIntent = Intent(Settings.ACTION_SOUND_SETTINGS)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        launchAppIfAvailable(launchIntent, 0)
    }

    fun launchInternetSettings() {
        val launchIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        launchAppIfAvailable(launchIntent, 0)
    }

    private fun showNoDefaultAppFoundToast(@StringRes appTypeResId: Int) {
        Toast.makeText(
            mContext,
            moduleResources.getString(appTypeResId) + " not found",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun launchWeatherActivity(fromQs: Boolean) {
        val launchIntent = Intent()
        launchIntent.component = ComponentName(
            BuildConfig.APPLICATION_ID,
            BuildConfig.APPLICATION_ID
                .replace(".debug", "")
                .replace(".foss", "") + ".ui.activities.WeatherActivity"
        )
        if (mActivityStarter == null) {
            log(this@ActivityLauncherUtils, "ActivityStarter is null")
            return
        }
        mActivityStarter.callMethod(
            "postStartActivityDismissingKeyguard",
            launchIntent, /* dismissShade */
            0
        )
    }
}
