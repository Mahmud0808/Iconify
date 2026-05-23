package com.drdisagree.iconify.core.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import com.drdisagree.iconify.R
import com.drdisagree.iconify.app.Iconify.Companion.appContext
import com.drdisagree.iconify.app.MainActivity
import com.topjohnwu.superuser.Shell
import kotlin.system.exitProcess

object AppUtils {

    fun isAppInstalled(packageName: String): Boolean {
        val pm = appContext.packageManager

        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return pm.getApplicationInfo(packageName, 0).enabled
        } catch (_: PackageManager.NameNotFoundException) {
        }

        return false
    }

    fun isAppInstalledRoot(packageName: String): Boolean {
        return Shell.cmd($$"res=$(pm path $$packageName); if [ ! -z \"$res\" ]; then echo \"installed\"; else echo \"not found\"; fi")
            .exec()
            .out[0]
            .contains("installed")
    }

    fun getAppUid(packageName: String): Int {
        val pm = appContext.packageManager

        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return pm.getApplicationInfo(packageName, 0).uid
        } catch (_: PackageManager.NameNotFoundException) {
        }

        return 0
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun getAppIcon(packageName: String): Drawable? {
        var appIcon = ContextCompat.getDrawable(appContext, R.drawable.ic_statusbar_logo_android).also {
            it?.setTint("#30dc80".toColorInt())
        }

        try {
            appIcon = appContext.packageManager.getApplicationIcon(packageName)
        } catch (_: PackageManager.NameNotFoundException) {
        }

        return appIcon
    }

    fun getAppName(packageName: String): String {
        val pm = appContext.applicationContext.packageManager
        var ai: ApplicationInfo? = null

        try {
            ai = pm.getApplicationInfo(packageName, 0)
        } catch (_: PackageManager.NameNotFoundException) {
        }

        return (if (ai == null) "Unavailable" else pm.getApplicationLabel(ai)) as String
    }

    fun launchApp(activity: Activity, packageName: String) {
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)

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

    fun launchAppThrowError(activity: Activity, packageName: String) {
        val launchIntent = appContext.packageManager.getLaunchIntentForPackage(packageName)

        if (launchIntent != null) {
            activity.startActivity(launchIntent)
        } else {
            throw Exception("App not found $packageName")
        }
    }

    fun openUrl(context: Context, url: String) {
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = url.toUri()
                }
            )
        } catch (_: Exception) {
            Toast.makeText(
                context,
                appContext.resources.getString(R.string.toast_error),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun getSplitLocations(packageName: String?): Array<String?> {
        return try {
            appContext
                .packageManager
                .getApplicationInfo(packageName!!, 0)
                .splitSourceDirs
                ?: arrayOf(
                    appContext
                        .packageManager
                        .getApplicationInfo(packageName, 0)
                        .sourceDir
                )
        } catch (_: PackageManager.NameNotFoundException) {
            arrayOfNulls(0)
        }
    }

    val isLSPosedInstalled: Boolean
        get() = RootUtils.fileExists("/data/adb/lspd/manager.apk")
                || RootUtils.fileExists("/data/adb/modules/*lsposed*/manager.apk")

    fun restartApplication(activity: Activity) {
        val intent = Intent(activity, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        activity.startActivity(intent)
        activity.finish()
        exitProcess(0)
    }
}
