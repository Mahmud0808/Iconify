package com.drdisagree.iconify.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Const.LATEST_VERSION_URL
import com.drdisagree.iconify.common.Preferences.UPDATE_OVER_WIFI
import com.drdisagree.iconify.common.Preferences.VER_CODE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.fragments.settings.AppUpdates
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class UpdateWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val tag =this::class.java.simpleName

    override suspend fun doWork(): Result {
        val isGoodNetwork = isGoodNetworkAvailable()

        if (isGoodNetwork) {
            val jsonStr = fetchLatestVersion()

            if (jsonStr != null) {
                try {
                    val latestVersion = JSONObject(jsonStr)
                    val latestVersionCode = latestVersion.getInt(VER_CODE)

                    if (latestVersionCode > BuildConfig.VERSION_CODE) {
                        showUpdateNotification()
                    }
                } catch (e: Exception) {
                    Log.e(tag, "Error: ${e.message}")
                }
            }
        }

        return if (isGoodNetwork) Result.success() else Result.retry()
    }

    private fun isGoodNetworkAvailable(): Boolean {
        val updateOverWifiOnly = RPrefs.getBoolean(UPDATE_OVER_WIFI, true)
        val connectivityManager =
            applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities =
            connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return capabilities != null && (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && !updateOverWifiOnly
                || capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED))
    }

    private suspend fun fetchLatestVersion(): String? {
        return withContext(Dispatchers.IO) {
            var urlConnection: HttpURLConnection? = null
            var bufferedReader: BufferedReader? = null

            try {
                val url = URL(LATEST_VERSION_URL)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connect()

                val inputStream = urlConnection.inputStream
                bufferedReader = BufferedReader(InputStreamReader(inputStream))

                val stringBuffer = StringBuilder()
                var line: String?

                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuffer.append(line).append("\n")
                }

                if (stringBuffer.isEmpty()) {
                    null
                } else {
                    stringBuffer.toString()
                }
            } catch (e: Exception) {
                null
            } finally {
                urlConnection?.disconnect()
                bufferedReader?.close()
            }
        }
    }

    private fun showUpdateNotification() {
        // Grant permission for notifications
        Shell.cmd("pm grant ${BuildConfig.APPLICATION_ID} android.permission.POST_NOTIFICATIONS")
            .exec()

        val notificationIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(AppUpdates.KEY_NEW_UPDATE, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(
            applicationContext,
            applicationContext.getString(R.string.update_notification_channel_name)
        )
            .setSmallIcon(R.drawable.ic_launcher_fg)
            .setContentTitle(applicationContext.getString(R.string.new_update_title))
            .setContentText(applicationContext.getString(R.string.new_update_desc))
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        createChannel(notificationManager)

        notificationManager.notify(0, notificationBuilder.build())
    }

    private fun createChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            applicationContext.getString(R.string.update_notification_channel_name),
            applicationContext.getString(R.string.update_notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description =
            applicationContext.getString(R.string.update_notification_channel_desc)

        notificationManager.createNotificationChannel(channel)
    }
}