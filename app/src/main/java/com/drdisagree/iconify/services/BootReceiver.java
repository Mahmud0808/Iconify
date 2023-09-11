package com.drdisagree.iconify.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.drdisagree.iconify.BuildConfig;

public class BootReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            if (BuildConfig.DEBUG) {
                Log.i(TAG, "Broadcast received: " + intent.getAction());
            }
            UpdateScheduler.scheduleUpdates(context);
        }
    }
}