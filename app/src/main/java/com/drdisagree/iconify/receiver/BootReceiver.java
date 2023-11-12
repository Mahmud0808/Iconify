package com.drdisagree.iconify.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.drdisagree.iconify.services.UpdateScheduler;

public class BootReceiver extends BroadcastReceiver {

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "Broadcast received: " + intent.getAction());
            UpdateScheduler.scheduleUpdates(context);
        }
    }
}