package com.drdisagree.iconify.services.schedulers

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.core.utils.weather.WeatherConfig
import com.drdisagree.iconify.core.utils.weather.WeatherWork
import java.util.concurrent.TimeUnit

object WeatherScheduler {

    private const val UPDATE_WORK_NAME: String = BuildConfig.APPLICATION_ID + ".WeatherSchedule"
    private const val UPDATE_NOW_WORK_NAME: String = BuildConfig.APPLICATION_ID + ".WeatherUpdateNow"
    private val TAG = "Iconify - ${WeatherScheduler::class.java.simpleName}: "
    private val networkConstraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    fun scheduleUpdates(context: Context) {
        Log.d(TAG, "Updating update schedule...")

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, Configuration.Builder().build())
        }

        val workManager = WorkManager.getInstance(context)

        val weatherEnabled: Boolean = WeatherConfig.isEnabled()

        Log.d(TAG, "Weather enabled: $weatherEnabled")

        if (weatherEnabled) {
            Log.d(TAG, "Scheduling updates")
            val builder: PeriodicWorkRequest.Builder = PeriodicWorkRequest.Builder(
                WeatherWork::class.java,
                WeatherConfig.getUpdateInterval().toLong(), TimeUnit.HOURS
            )
                .setConstraints(networkConstraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS)

            workManager.enqueueUniquePeriodicWork(
                UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                builder.build()
            )
        } else {
            workManager.cancelUniqueWork(UPDATE_WORK_NAME)
        }
    }

    fun cancelUpdates(context: Context) {
        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, Configuration.Builder().build())
        }

        val workManager = WorkManager.getInstance(context)

        workManager.cancelUniqueWork(UPDATE_WORK_NAME)
    }

    fun scheduleUpdateNow(context: Context) {
        Log.d(TAG, "Check update now")

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, Configuration.Builder().build())
        }

        val workManager = WorkManager.getInstance(context)

        val request = OneTimeWorkRequest.Builder(WeatherWork::class.java)
            .setConstraints(networkConstraints)
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(UPDATE_NOW_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }

}
