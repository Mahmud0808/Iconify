package com.drdisagree.iconify.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.drdisagree.iconify.config.Prefs;

public class BootCompletedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED) && Prefs.getBoolean("onHomePage")) {
            Log.w("BootCompletedReceiver", "Starting Background Service...");
            // context.startService(new Intent(context, BackgroundService.class));
        }
    }
}
