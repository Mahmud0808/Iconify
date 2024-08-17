package com.drdisagree.iconify.services

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.weather.WeatherConfig
import com.drdisagree.iconify.weather.WeatherWork
import java.util.concurrent.TimeUnit

object WeatherScheduler {
    private const val UPDATE_WORK_NAME: String = BuildConfig.APPLICATION_ID + ".WeatherSchedule"
    private val TAG = "Iconify - ${WeatherScheduler::class.java.simpleName}: "

    fun scheduleUpdates(context: Context) {
        Log.d(TAG, "Updating update schedule...")

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, Configuration.Builder().build())
        }

        val workManager = WorkManager.getInstance(context)

        val weatherEnabled: Boolean = WeatherConfig.isEnabled(context)

        Log.d(TAG, "Weather enabled: $weatherEnabled")

        if (weatherEnabled) {
            Log.d(TAG, "Scheduling updates")
            val builder: PeriodicWorkRequest.Builder = PeriodicWorkRequest.Builder(
                WeatherWork::class.java,
                WeatherConfig.getUpdateInterval(context).toLong(), TimeUnit.HOURS
            )
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

        val builder: OneTimeWorkRequest.Builder =
            OneTimeWorkRequest.Builder(WeatherWork::class.java)

        workManager.enqueue(builder.build())
    }

}
