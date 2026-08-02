package com.drdisagree.iconify.xposed.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.graphics.toColorInt
import com.crossbowffs.remotepreferences.RemotePreferences
import com.drdisagree.iconify.data.keys.Key
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.utils.ExtendedRemotePreferences.Companion.WRITE_TIMEOUT_MS
import java.util.concurrent.Callable
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

@Suppress("unused")
class ExtendedRemotePreferences : RemotePreferences {

    constructor(context: Context, authority: String, prefFileName: String) : super(
        context,
        authority,
        prefFileName
    )

    constructor(
        context: Context,
        authority: String,
        prefFileName: String,
        strictMode: Boolean
    ) : super(context, authority, prefFileName, strictMode)

    private val cache = ConcurrentHashMap<String, Any>()
    private val binderPool = ThreadPoolExecutor(
        0, MAX_BINDER_THREADS, 30L, TimeUnit.SECONDS, SynchronousQueue()
    )
    private val consecutiveFailures = AtomicInteger(0)

    @Volatile
    private var breakerOpen = false
    private val proberRunning = AtomicBoolean(false)
    private val recoveryListeners = CopyOnWriteArrayList<() -> Unit>()

    fun getBoolean(key: String): Boolean {
        return getBoolean(key, false)
    }

    fun getBoolean(key: Key): Boolean {
        return getBoolean(key.name, key.default as? Boolean ?: false)
    }

    fun getString(key: Key): String {
        return getString(key.name, key.default as? String ?: "")!!
    }

    fun getInt(key: Key): Int {
        return getFloat(key.name, key.default as? Float ?: 0f).roundToInt()
    }

    fun getFloat(key: Key): Float {
        return getFloat(key.name, key.default as? Float ?: 0f)
    }

    fun getDouble(key: Key): Double {
        return getFloat(key.name, key.default as? Float ?: 0f).toDouble()
    }

    fun getColor(key: XposedKey) = getString(key).toColorInt()

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        boundedRead(key ?: "", defValue) { super.getBoolean(key, defValue) }

    override fun getString(key: String?, defValue: String?): String? =
        boundedRead(key ?: "", defValue) { super.getString(key, defValue) }

    override fun getInt(key: String?, defValue: Int): Int =
        boundedRead(key ?: "", defValue) { super.getInt(key, defValue) }

    override fun getLong(key: String?, defValue: Long): Long =
        boundedRead(key ?: "", defValue) { super.getLong(key, defValue) }

    override fun getFloat(key: String?, defValue: Float): Float =
        boundedRead(key ?: "", defValue) { super.getFloat(key, defValue) }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? =
        boundedRead(key ?: "", defValues) { super.getStringSet(key, defValues) }

    override fun contains(key: String?): Boolean =
        boundedRead("__contains__$key", false) { super.contains(key) }

    override fun getAll(): MutableMap<String, *> =
        boundedRead<MutableMap<String, *>>("__all__", HashMap<String, Any>()) { super.getAll() }

    /**
     * Probes the provider with a bounded read. Never throws. Used by
     * [com.drdisagree.iconify.xposed.HookEntry] instead of relying on strict mode
     * exceptions from an unbounded read.
     */
    fun isProviderReady(timeoutMs: Long = READ_TIMEOUT_MS): Boolean {
        if (breakerOpen) return false
        return try {
            val future = binderPool.submit(Callable {
                super.getBoolean(PROBE_KEY, false)
            })
            try {
                future.get(timeoutMs, TimeUnit.MILLISECONDS)
                recordSuccess()
                true
            } catch (_: Throwable) {
                recordFailure()
                false
            }
        } catch (_: RejectedExecutionException) {
            recordFailure()
            false
        }
    }

    /**
     * Runs an edit + commit on a worker thread, waiting at most [WRITE_TIMEOUT_MS].
     * Returns whether the commit is known to have succeeded. Never throws, never
     * blocks the caller past the timeout.
     */
    fun boundedCommit(block: (SharedPreferences.Editor) -> Unit): Boolean {
        if (breakerOpen) return false
        return try {
            val future = binderPool.submit(Callable {
                val editor = edit()
                block(editor)
                editor.commit()
            })
            try {
                future.get(WRITE_TIMEOUT_MS, TimeUnit.MILLISECONDS)
            } catch (_: Throwable) {
                recordFailure()
                false
            }
        } catch (_: RejectedExecutionException) {
            recordFailure()
            false
        }
    }

    /** Listener is invoked (on the prober thread) when the provider becomes reachable again. */
    fun addRecoveryListener(listener: () -> Unit) {
        recoveryListeners.add(listener)
    }

    private fun <T> boundedRead(key: String, defValue: T, remote: () -> T): T {
        if (breakerOpen) return cachedOr(key, defValue)
        return try {
            val future = binderPool.submit(Callable { remote() })
            try {
                val value = future.get(READ_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                recordSuccess()
                cache[key] = value ?: NULL
                value
            } catch (_: Throwable) {
                recordFailure()
                cachedOr(key, defValue)
            }
        } catch (_: RejectedExecutionException) {
            recordFailure()
            cachedOr(key, defValue)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> cachedOr(key: String, defValue: T): T {
        val value = cache[key] ?: return defValue
        if (value === NULL) return defValue
        return value as? T ?: defValue
    }

    private fun recordSuccess() {
        consecutiveFailures.set(0)
    }

    private fun recordFailure() {
        if (consecutiveFailures.incrementAndGet() >= FAILURES_TO_OPEN && !breakerOpen) {
            breakerOpen = true
            log("Iconify preference provider unresponsive; serving cached values until it recovers")
            startProber()
        }
    }

    private fun startProber() {
        if (!proberRunning.compareAndSet(false, true)) return

        Thread({
            try {
                while (breakerOpen) {
                    try {
                        super.getBoolean(PROBE_KEY, false)
                        consecutiveFailures.set(0)
                        breakerOpen = false
                        log("Iconify preference provider recovered")
                        recoveryListeners.forEach { listener ->
                            try {
                                listener()
                            } catch (throwable: Throwable) {
                                log("Recovery listener failed: $throwable")
                            }
                        }
                    } catch (_: Throwable) {
                        try {
                            Thread.sleep(PROBE_RETRY_MS)
                        } catch (_: InterruptedException) {
                            return@Thread
                        }
                    }
                }
            } finally {
                proberRunning.set(false)
            }
        }, "Iconify-PrefProber").apply { isDaemon = true }.start()
    }

    companion object {
        private const val PROBE_KEY = "LoadTestBooleanValue"
        private const val READ_TIMEOUT_MS = 1000L
        private const val WRITE_TIMEOUT_MS = 2000L
        private const val FAILURES_TO_OPEN = 3
        private const val PROBE_RETRY_MS = 5000L
        private const val MAX_BINDER_THREADS = 4
        private val NULL = Any()
    }
}