package com.drdisagree.iconify.services;

import static com.drdisagree.iconify.common.Preferences.AUTO_UPDATE;
import static com.drdisagree.iconify.common.Preferences.UPDATE_CHECK_TIME;

import android.content.Context;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.config.Prefs;

import java.util.concurrent.TimeUnit;

public class UpdateScheduler {

    private static final String TAG = UpdateScheduler.class.getSimpleName();
    private static final String UPDATE_WORK_NAME = BuildConfig.APPLICATION_ID.replace(".debug", "") + ".services.UpdateScheduler";

    public static void scheduleUpdates(Context context) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, "Updating update schedule...");
        }

        WorkManager workManager = WorkManager.getInstance(context);

        if (Prefs.getBoolean(AUTO_UPDATE, true)) {
            PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(UpdateWorker.class, Prefs.getLong(UPDATE_CHECK_TIME, 12), TimeUnit.HOURS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS);

            workManager.enqueueUniquePeriodicWork(
                    UPDATE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    builder.build());
        } else {
            workManager.cancelUniqueWork(UPDATE_WORK_NAME);
        }
    }
}