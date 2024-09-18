package com.drdisagree.iconify.services

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.common.Preferences.AUTO_UPDATE
import com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getLong
import java.util.concurrent.TimeUnit

object UpdateScheduler {

    private val TAG = UpdateScheduler::class.java.getSimpleName()
    private val UPDATE_WORK_NAME = BuildConfig.APPLICATION_ID
        .replace(".debug", "") + ".services.UpdateScheduler"

    fun scheduleUpdates(context: Context?) {
        Log.i(TAG, "Updating update schedule...")

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context!!, Configuration.Builder().build())
        }

        val workManager = WorkManager.getInstance(context!!)

        if (getBoolean(AUTO_UPDATE, true)) {
            val builder: PeriodicWorkRequest.Builder = PeriodicWorkRequest.Builder(
                UpdateWorker::class.java,
                getLong(UPDATE_CHECK_TIME, 12),
                TimeUnit.HOURS
            ).setBackoffCriteria(
                BackoffPolicy.LINEAR,
                1,
                TimeUnit.HOURS
            )

            workManager.enqueueUniquePeriodicWork(
                UPDATE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                builder.build()
            )
        } else {
            workManager.cancelUniqueWork(UPDATE_WORK_NAME)
        }
    }
}