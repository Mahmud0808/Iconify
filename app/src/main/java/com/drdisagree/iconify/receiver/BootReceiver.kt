package com.drdisagree.iconify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.drdisagree.iconify.services.UpdateScheduler

class BootReceiver : BroadcastReceiver() {

    private val tag = javaClass.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.i(tag, "Broadcast received: " + intent.action)
            UpdateScheduler.scheduleUpdates(context)
        }
    }
}