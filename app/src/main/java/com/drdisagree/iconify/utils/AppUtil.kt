package com.drdisagree.iconify.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.Iconify
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.topjohnwu.superuser.Shell

object AppUtil {

    fun isAppInstalled(packageName: String?): Boolean {
        val pm = appContext.packageManager

        try {
            pm.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            return pm.getApplicationInfo(packageName, 0).enabled
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return false
    }

    @JvmStatic
    fun isAppInstalledRoot(packageName: String): Boolean {
        return Shell.cmd("res=$(pm path $packageName); if [ ! -z \"\$res\" ]; then echo \"installed\"; else echo \"not found\"; fi")
            .exec().out[0].contains("installed")
    }

    fun getAppUid(packageName: String?): Int {
        val pm = appContext.packageManager

        try {
            pm.getPackageInfo(packageName!!, PackageManager.GET_ACTIVITIES)
            return pm.getApplicationInfo(packageName, 0).uid
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return 0
    }

    @JvmStatic
    @SuppressLint("UseCompatLoadingForDrawables")
    fun getAppIcon(packageName: String?): Drawable? {
        var appIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_android)

        try {
            appIcon = appContext.packageManager.getApplicationIcon(packageName!!)
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return appIcon
    }

    @JvmStatic
    fun getAppName(packageName: String?): String {
        val pm = appContext.applicationContext.packageManager
        var ai: ApplicationInfo? = null

        try {
            ai = pm.getApplicationInfo(packageName!!, 0)
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return (if (ai == null) "Unavailable" else pm.getApplicationLabel(ai)) as String
    }

    @JvmStatic
    fun launchApp(activity: Activity, packageName: String?) {
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(
            packageName!!
        )

        if (launchIntent != null) {
            activity.startActivity(launchIntent)
        } else {
            Toast.makeText(
                appContext,
                appContext.resources.getString(R.string.app_not_found),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    @JvmStatic
    fun getSplitLocations(packageName: String?): Array<String?> {
        try {
            var splitLocations = appContext.packageManager.getApplicationInfo(
                packageName!!, 0
            ).splitSourceDirs

            if (splitLocations == null) {
                splitLocations = arrayOf(
                    appContext.packageManager.getApplicationInfo(
                        packageName, 0
                    ).sourceDir
                )
            }
            return splitLocations
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        return arrayOfNulls(0)
    }

    @JvmStatic
    val isLsposedInstalled: Boolean
        get() = RootUtil.fileExists("/data/adb/lspd/manager.apk") || RootUtil.fileExists("/data/adb/modules/*lsposed*/manager.apk")

    @JvmStatic
    fun restartApplication(activity: Activity) {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = activity.intent
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            activity.finish()
            activity.startActivity(intent)
        }, 600)
    }
}
