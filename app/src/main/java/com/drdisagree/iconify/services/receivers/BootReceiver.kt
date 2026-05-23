package com.drdisagree.iconify.services.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.drdisagree.iconify.services.schedulers.UpdateScheduler

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            UpdateScheduler.scheduleUpdates(context)
        }
    }
}
