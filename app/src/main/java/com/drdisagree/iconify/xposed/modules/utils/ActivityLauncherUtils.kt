package com.drdisagree.iconify.xposed.modules.utils

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.StringRes
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.ControllersProvider
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.callMethod

class ActivityLauncherUtils(private val mContext: Context, private val mActivityStarter: Any?) {
    private val mPackageManager: PackageManager = mContext.packageManager

    val installedMusicApp: String
        get() {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_APP_MUSIC)
            val musicApps = mPackageManager.queryIntentActivities(intent, 0)
            val musicApp = if (musicApps.isEmpty()) null else musicApps[0]
            return if (musicApp != null) musicApp.activityInfo.packageName else ""
        }

    private fun launchAppIfAvailable(launchIntent: Intent?, @StringRes appTypeResId: Int) {
        val apps =
            mPackageManager.queryIntentActivities(launchIntent!!, PackageManager.MATCH_DEFAULT_ONLY)
        if (apps.isNotEmpty()) {
            if (mActivityStarter == null) {
                log("ActivityStarter is null")
                return
            }
            callMethod(mActivityStarter, "startActivity", launchIntent, true)
        } else {
            if (appTypeResId != 0) showNoDefaultAppFoundToast(appTypeResId)
        }
    }

    fun launchCamera() {
        val launchIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE)
        launchAppIfAvailable(launchIntent, R.string.camera)
    }

    fun launchTimer() {
        val intent = Intent()
        intent.setAction("android.intent.action.SHOW_ALARMS")
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        launchAppIfAvailable(intent, R.string.clock_timer)
    }

    fun launchCalculator() {
        // If the calculator tile is available
        // we can use it to open the calculator
        val calculatorTile: Any? = ControllersProvider.mCalculatorTile
        if (calculatorTile != null) {
            callMethod(calculatorTile, "openCalculator")
            return
        }

        // Otherwise we try to launch the calculator app
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
        val intent =
            if (className == PERSONALIZATIONS_ACTIVITY) Intent(Intent.ACTION_MAIN) else Intent()
        intent.setComponent(ComponentName("com.android.settings", className))
        callMethod(mActivityStarter, "startActivity", intent, true)
    }

    fun launchAudioSettings() {
        val launchIntent = Intent(Settings.ACTION_SOUND_SETTINGS)
        launchAppIfAvailable(launchIntent, 0)
    }

    fun startSettingsActivity() {
        if (mActivityStarter == null) return
        callMethod(
            mActivityStarter,
            "startActivity",
            Intent(Settings.ACTION_SETTINGS),
            true
        )
    }

    fun launchInternetSettings() {
        val launchIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS)
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        launchAppIfAvailable(launchIntent, 0)
    }

    private fun showNoDefaultAppFoundToast(@StringRes appTypeResId: Int) {
        Toast.makeText(mContext, modRes.getString(appTypeResId) + " not found", Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        private const val PERSONALIZATIONS_ACTIVITY =
            "com.android.settings.Settings\$personalizationSettingsLayoutActivity"
    }
}
