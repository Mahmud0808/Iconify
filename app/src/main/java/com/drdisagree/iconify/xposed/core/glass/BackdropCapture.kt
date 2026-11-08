package com.drdisagree.iconify.xposed.core.glass

import android.graphics.Rect
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.SystemClock
import android.view.SurfaceControl
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.atomic.AtomicBoolean

object BackdropCapture {

    private const val FRAME_INTERVAL_MS = 33L
    private const val CAPTURE_SCALE = 0.5f
    private const val CAPTURE_MARGIN = 96

    private val clients: MutableSet<BackdropClient> = Collections.newSetFromMap(WeakHashMap())
    private val mainHandler = Handler(Looper.getMainLooper())
    private val inFlight = AtomicBoolean(false)
    private val clientRect = Rect()
    private var thread: HandlerThread? = null
    private var handler: Handler? = null
    private var running = false

    val isAvailable: Boolean
        get() = ScreenCaptureBridge.isAvailable

    fun register(client: BackdropClient) {
        synchronized(clients) { clients.add(client) }
        start()
    }

    fun unregister(client: BackdropClient) {
        synchronized(clients) { clients.remove(client) }
    }

    private fun start() {
        if (running) return
        running = true
        val worker = thread ?: HandlerThread("IconifyBackdrop").also {
            it.start()
            thread = it
            handler = Handler(it.looper)
        }
        Handler(worker.looper).post(::tick)
    }

    private fun tick() {
        val worker = handler ?: return
        val snapshot = synchronized(clients) { clients.toList() }

        if (snapshot.isEmpty()) {
            running = false
            return
        }

        val startedAt = SystemClock.uptimeMillis()
        if (!inFlight.get()) captureFor(snapshot)
        val delay = (FRAME_INTERVAL_MS - (SystemClock.uptimeMillis() - startedAt)).coerceAtLeast(1L)
        worker.postDelayed(::tick, delay)
    }

    private fun captureFor(snapshot: List<BackdropClient>) {
        val crop = Rect()
        val exclude = LinkedHashSet<SurfaceControl>()
        var displayId = 0

        snapshot.forEach { client ->
            if (!client.captureRect(clientRect)) return@forEach
            if (crop.isEmpty) crop.set(clientRect) else crop.union(clientRect)
            client.excludeLayer()?.let(exclude::add)
            displayId = client.displayId
        }

        if (crop.isEmpty) return
        crop.inset(-CAPTURE_MARGIN, -CAPTURE_MARGIN)
        if (crop.left < 0) crop.left = 0
        if (crop.top < 0) crop.top = 0

        inFlight.set(true)
        val captureRect = Rect(crop)
        val started = ScreenCaptureBridge.capture(
            displayId,
            captureRect,
            CAPTURE_SCALE,
            exclude.toTypedArray()
        ) { bitmap ->
            inFlight.set(false)
            bitmap ?: return@capture
            val frame = BackdropFrame(bitmap, captureRect, CAPTURE_SCALE)
            mainHandler.post {
                val targets = synchronized(clients) { clients.toList() }
                targets.forEach { it.onBackdrop(frame) }
            }
        }
        if (!started) inFlight.set(false)
    }
}
