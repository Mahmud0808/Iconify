package com.drdisagree.iconify.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.drdisagree.iconify.services.UpdateScheduler
import com.drdisagree.iconify.services.WeatherScheduler

class BootReceiver : BroadcastReceiver() {

    private val tag = this::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.i(tag, "Broadcast received: " + intent.action)

            // Schedule Updates
            WeatherScheduler.scheduleUpdates(context)
            WeatherScheduler.scheduleUpdateNow(context)
            UpdateScheduler.scheduleUpdates(context)
        }
    }
}