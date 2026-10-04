package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.os.Environment
import android.os.Handler
import android.os.Looper
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

object BootCallback {

    fun interface BootListener {
        fun onDeviceBooted()
    }

    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private val pendingListeners = mutableListOf<BootListener>()
    private var poller: ScheduledExecutorService? = null

    @Volatile
    private var isStorageReady = false

    fun registerBootListener(listener: BootListener) {
        synchronized(this) {
            if (!isStorageReady) {
                pendingListeners += listener
                startPolling()
                return
            }
        }
        dispatch(listener)
    }

    private fun startPolling() {
        if (poller != null) return
        poller = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleWithFixedDelay(::checkStorage, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS)
        }
    }

    private fun checkStorage() {
        if (!isStorageAvailable()) return

        val listeners = synchronized(this) {
            isStorageReady = true
            poller?.shutdown()
            poller = null
            pendingListeners.toList().also { pendingListeners.clear() }
        }
        listeners.forEach(::dispatch)
    }

    private fun isStorageAvailable(): Boolean = try {
        File(Environment.getExternalStorageDirectory(), "Android").isDirectory
    } catch (_: Throwable) {
        false
    }

    private fun dispatch(listener: BootListener) {
        val task = Runnable {
            try {
                listener.onDeviceBooted()
            } catch (throwable: Throwable) {
                log(this, throwable)
            }
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            task.run()
        } else {
            mainHandler.post(task)
        }
    }

    private const val POLL_INTERVAL_SECONDS = 2L
}