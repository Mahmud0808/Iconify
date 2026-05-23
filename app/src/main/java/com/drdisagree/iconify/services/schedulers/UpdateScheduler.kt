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
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.config.Config
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.services.workers.UpdateWorker
import java.util.concurrent.TimeUnit

object UpdateScheduler {

    private val TAG = UpdateScheduler::class.java.simpleName
    private val UPDATE_WORK_NAME = BuildConfig.APPLICATION_ID
        .replace(".debug", "")
        .replace(".foss", "") + ".services.UpdateScheduler"

    fun scheduleUpdates(context: Context?) {
        if (context == null) return
        Log.i(TAG, "Updating update schedule...")

        if (!WorkManager.isInitialized()) {
            try {
                WorkManager.initialize(context, Configuration.Builder().build())
            } catch (_: Exception) {}
        }

        val workManager = WorkManager.getInstance(context)

        val autoUpdate = RPrefs.getBoolean(SettingsKey.AUTO_UPDATE)
        val scheduleStr = RPrefs.getString(SettingsKey.UPDATE_SCHEDULE)!!
        val scheduleIndex = scheduleStr.toIntOrNull()
            ?: (SettingsKey.UPDATE_SCHEDULE.default as String).toInt()

        if ((BuildConfig.DEBUG && !Config.ENABLE_AUTO_UPDATE_IN_DEBUG) || !autoUpdate) {
            workManager.cancelUniqueWork(UPDATE_WORK_NAME)
            workManager.cancelUniqueWork(UPDATE_WORK_NAME + "_onetime")
            return
        }

        val constraintsBuilder = Constraints.Builder()
        if (RPrefs.getBoolean(SettingsKey.UPDATE_OVER_WIFI)) {
            constraintsBuilder.setRequiredNetworkType(NetworkType.UNMETERED)
        } else {
            constraintsBuilder.setRequiredNetworkType(NetworkType.CONNECTED)
        }

        val oneTimeBuilder = OneTimeWorkRequest.Builder(UpdateWorker::class.java)
            .setConstraints(constraintsBuilder.build())
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                1,
                TimeUnit.HOURS
            )

        workManager.enqueueUniqueWork(
            UPDATE_WORK_NAME + "_onetime",
            ExistingWorkPolicy.REPLACE,
            oneTimeBuilder.build()
        )

        if (scheduleIndex == 4) {
            workManager.cancelUniqueWork(UPDATE_WORK_NAME)
            return
        }

        val hours = when (scheduleIndex) {
            0 -> 6L
            1 -> 12L
            2 -> 24L
            3 -> 168L
            else -> 168L
        }

        val builder = PeriodicWorkRequest.Builder(
            UpdateWorker::class.java,
            hours,
            TimeUnit.HOURS
        ).setConstraints(constraintsBuilder.build())
        .setBackoffCriteria(
            BackoffPolicy.EXPONENTIAL,
            1,
            TimeUnit.HOURS
        )

        workManager.enqueueUniquePeriodicWork(
            UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            builder.build()
        )
    }
}