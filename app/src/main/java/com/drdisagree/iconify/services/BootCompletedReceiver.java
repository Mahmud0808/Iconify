package com.drdisagree.iconify.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.w("BootCompletedReceiver", "Starting Background Service...");
            context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
