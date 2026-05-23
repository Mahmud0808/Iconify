package com.drdisagree.iconify.xposed.modules.extras

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
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.ref.WeakReference

class ActivityLauncherUtils(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        contextRef = WeakReference(mContext)
        mPackageManager = mContext.packageManager

        val keyguardQuickAffordanceInteractorClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.domain.interactor.KeyguardQuickAffordanceInteractor")

        keyguardQuickAffordanceInteractorClass
            .hookConstructor()
            .runAfter { param ->
                mActivityStarter = param.thisObject.getFieldSilently("activityStarter")
            }
    }

    companion object {
        private lateinit var contextRef: WeakReference<Context>
        private var mActivityStarter: Any? = null
        private lateinit var mPackageManager: PackageManager

        fun launchApp(launchIntent: Intent?, fromQs: Boolean = false) {
            if (launchIntent == null) return
            if (mActivityStarter == null) {
                log(ActivityLauncherUtils, "ActivityStarter is null")
                return
            }
            mActivityStarter.callMethod(
                "postStartActivityDismissingKeyguard",
                launchIntent,
                0 /* delay */
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
            val launchIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            launchAppIfAvailable(launchIntent, R.string.calculator)
        }

        fun launchWallet() {
            val launchIntent =
                mPackageManager.getLaunchIntentForPackage("com.google.android.apps.walletnfcrel")
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
            val launchIntent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            launchAppIfAvailable(launchIntent, 0)
        }

        fun launchAudioSettings() {
            val launchIntent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            launchAppIfAvailable(launchIntent, 0)
        }

        fun launchInternetSettings() {
            val launchIntent = Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            launchAppIfAvailable(launchIntent, 0)
        }

        fun launchWeatherActivity(fromQs: Boolean) {
            val launchIntent = Intent().apply {
                component = ComponentName(
                    BuildConfig.APPLICATION_ID,
                    BuildConfig.APPLICATION_ID
                        .replace(".debug", "")
                        .replace(".foss", "") + ".ui.activities.WeatherActivity"
                )
            }
            if (mActivityStarter == null) {
                log(ActivityLauncherUtils, "ActivityStarter is null")
                return
            }
            mActivityStarter.callMethod(
                "postStartActivityDismissingKeyguard",
                launchIntent,
                0 /* delay */
            )
        }

        private fun launchAppIfAvailable(launchIntent: Intent?, @StringRes appTypeResId: Int) {
            val apps = mPackageManager.queryIntentActivities(
                launchIntent!!,
                PackageManager.MATCH_DEFAULT_ONLY
            )
            if (apps.isNotEmpty()) {
                if (mActivityStarter == null) {
                    log(ActivityLauncherUtils, "ActivityStarter is null")
                    return
                }
                mActivityStarter.callMethod("startActivity", launchIntent, true)
            } else {
                if (appTypeResId != 0) showNoDefaultAppFoundToast(appTypeResId)
            }
        }

        private fun showNoDefaultAppFoundToast(@StringRes appTypeResId: Int) {
            Toast.makeText(
                contextRef.get()!!,
                modRes.getString(appTypeResId) + " not found",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}