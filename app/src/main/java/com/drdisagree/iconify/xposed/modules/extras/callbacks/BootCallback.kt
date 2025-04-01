package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.os.Environment
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

object BootCallback {

    interface BootListener {
        fun onDeviceBooted()
    }

    fun registerBootListener(listener: BootListener) {
        try {
            Executors.newSingleThreadScheduledExecutor().apply {
                scheduleWithFixedDelay({
                    if (File(Environment.getExternalStorageDirectory(), "Android").isDirectory()) {
                        listener.onDeviceBooted()
                        shutdown()
                        shutdownNow()
                    }
                }, 0, 5, TimeUnit.SECONDS)
            }
        } catch (_: Throwable) {
        }
    }
}