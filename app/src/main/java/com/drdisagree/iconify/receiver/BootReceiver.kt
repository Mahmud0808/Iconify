package com.drdisagree.iconify.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.drdisagree.iconify.common.Const.ACTION_BOOT_COMPLETED
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.services.UpdateScheduler

class BootReceiver : BroadcastReceiver() {

    private val tag = this::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.i(tag, "Broadcast received: " + intent.action)

            // Schedule updates
            UpdateScheduler.scheduleUpdates(context)

            // Update QS clock on boot
            val broadcast: Intent = Intent(ACTION_BOOT_COMPLETED)
            broadcast.putExtra("packageName", SYSTEMUI_PACKAGE)
            context.sendBroadcast(broadcast)
        }
    }
}