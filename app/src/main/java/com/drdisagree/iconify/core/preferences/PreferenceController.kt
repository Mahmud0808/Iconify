package com.drdisagree.iconify.core.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import com.drdisagree.iconify.data.keys.Key
import com.drdisagree.iconify.data.storage.PreferenceStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@Suppress("unused")
class PreferenceController(
    private val storage: PreferenceStorage,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate),
) {
    private val _prefs = mutableStateMapOf<String, PrefValue>()
    val prefs: Map<String, PrefValue> get() = _prefs

    private val _changesFlow = MutableStateFlow<PreferenceChangeEvent?>(null)
    val changesFlow: StateFlow<PreferenceChangeEvent?> = _changesFlow.asStateFlow()

    private val listeners = mutableListOf<PreferenceListenerHandle>()

    private val storedSnapshot: Map<String, PrefValue> by lazy { storage.loadAll() }

    init {
        scope.launch {
            storage.externalChanges
                .filter { change -> _prefs.containsKey(change.key) }
                .collect { change ->
                    val current = _prefs[change.key]
                    if (current != change.value) {
                        setInternal(change.key, change.value, persist = false)
                    }
                }
        }
    }

    fun init(key: String, default: PrefValue) {
        if (_prefs.containsKey(key)) return
        _prefs[key] = storedSnapshot[key]
            ?: storage.read(key, default)?.toPrefValue()
                    ?: default
    }

    fun initAll(defaults: Map<String, PrefValue>) =
        defaults.forEach { (k, v) -> init(k, v) }

    fun set(key: Key) = setInternal(key.name, key.default.toPrefValue(), persist = true)

    fun set(key: Key, value: PrefValue) = setInternal(key.name, value, persist = true)

    fun set(key: String, value: PrefValue) = setInternal(key, value, persist = true)

    private fun setInternal(key: String, value: PrefValue, persist: Boolean) {
        val old = _prefs[key]
        if (old == value) return

        _prefs[key] = value
        if (persist) storage.write(key, value)

        val event = PreferenceChangeEvent(key, old, value)
        _changesFlow.value = event
        dispatchToListeners(event)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> get(key: String, default: T): T {
        return when (val pref = get(key)) {
            is PrefValue.BoolValue -> pref.v as? T ?: default
            is PrefValue.IntValue -> pref.v as? T ?: default
            is PrefValue.FloatValue -> pref.v as? T ?: default
            is PrefValue.StringValue -> pref.v as? T ?: default
            is PrefValue.StringSetValue -> pref.v as? T ?: default
            else -> default
        }
    }

    fun get(key: String): PrefValue? = _prefs[key]

    fun get(key: String, default: PrefValue?): PrefValue? {
        if (default != null) init(key, default)
        return _prefs[key] ?: default
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        init(key, default.toPrefValue())
        return (_prefs[key] as? PrefValue.BoolValue)?.v ?: default
    }

    fun getInt(key: String, default: Int = 0): Int {
        init(key, default.toPrefValue())
        return (_prefs[key] as? PrefValue.IntValue)?.v ?: default
    }

    fun getFloat(key: String, default: Float = 0f): Float {
        init(key, default.toPrefValue())
        return (_prefs[key] as? PrefValue.FloatValue)?.v ?: default
    }

    fun getDouble(key: String, default: Double = 0.0): Double {
        init(key, default.toPrefValue())
        return (_prefs[key] as? PrefValue.DoubleValue)?.v ?: default
    }

    fun getString(key: String, default: String = ""): String {
        init(key, default.toPrefValue())
        return (_prefs[key] as? PrefValue.StringValue)?.v ?: default
    }

    fun getStringSet(key: String, default: Set<String> = emptySet()): Set<String> {
        init(key, default.toPrefValue())
        return (_prefs[key] as? PrefValue.StringSetValue)?.v ?: default
    }

    fun get(key: Key): PrefValue? {
        key.default?.toPrefValue()?.let { init(key.name, it) }
        return _prefs[key.name] ?: key.default?.toPrefValue()
    }

    fun get(key: Key, default: PrefValue?): PrefValue? {
        init(key.name, default ?: key.default?.toPrefValue() ?: return _prefs[key.name])
        return _prefs[key.name] ?: default
    }

    fun getBoolean(key: Key): Boolean {
        val default = key.default as? Boolean ?: false
        init(key.name, default.toPrefValue())
        return (_prefs[key.name] as? PrefValue.BoolValue)?.v ?: default
    }

    fun getInt(key: Key): Int {
        val default = key.default as? Int ?: 0
        init(key.name, default.toPrefValue())
        return (_prefs[key.name] as? PrefValue.IntValue)?.v ?: default
    }

    fun getFloat(key: Key): Float {
        val default = key.default as? Float ?: 0f
        init(key.name, default.toPrefValue())
        return (_prefs[key.name] as? PrefValue.FloatValue)?.v ?: default
    }

    fun getDouble(key: Key): Double {
        val default = key.default as? Double ?: 0.0
        init(key.name, default.toPrefValue())
        return (_prefs[key.name] as? PrefValue.DoubleValue)?.v ?: default
    }

    fun getString(key: Key): String {
        val default = key.default as? String ?: ""
        init(key.name, default.toPrefValue())
        return (_prefs[key.name] as? PrefValue.StringValue)?.v ?: default
    }

    @Suppress("UNCHECKED_CAST")
    fun getStringSet(key: Key): Set<String> {
        val default = key.default as? Set<String> ?: emptySet()
        init(key.name, default.toPrefValue())
        return (_prefs[key.name] as? PrefValue.StringSetValue)?.v ?: default
    }

    fun setBoolean(key: String, value: Boolean) = set(key, value.toPrefValue())

    fun setInt(key: String, value: Int) = set(key, value.toPrefValue())

    fun setFloat(key: String, value: Float) = set(key, value.toPrefValue())

    fun setLong(key: String, value: Long) = set(key, value.toPrefValue())

    fun setString(key: String, value: String) = set(key, value.toPrefValue())

    fun setStringSet(key: String, value: Set<String>) = set(key, value.toPrefValue())

    fun setBoolean(key: Key, value: Boolean) = set(key.name, value.toPrefValue())

    fun setInt(key: Key, value: Int) = set(key.name, value.toPrefValue())

    fun setFloat(key: Key, value: Float) = set(key.name, value.toPrefValue())

    fun setString(key: Key, value: String) = set(key.name, value.toPrefValue())

    fun setStringSet(key: Key, value: Set<String>) = set(key.name, value.toPrefValue())

    fun reset(defaults: Map<String, PrefValue> = emptyMap()) {
        storage.clearAll()
        _prefs.clear()
        defaults.forEach { (k, v) -> _prefs[k] = v }
    }

    @Composable
    inline fun <reified T> observe(key: String, default: T): State<T> =
        remember(key) {
            derivedStateOf {
                @Suppress("UNCHECKED_CAST")
                when (T::class) {
                    Boolean::class -> getBoolean(key, default as Boolean) as T
                    Int::class -> getInt(key, default as Int) as T
                    Float::class -> getFloat(key, default as Float) as T
                    String::class -> getString(key, default as String) as T
                    Set::class -> getStringSet(key, default as Set<String>) as T
                    else -> default
                }
            }
        }

    fun addListener(
        key: String? = null,
        callback: (PreferenceChangeEvent) -> Unit,
    ): PreferenceListenerHandle {
        val handle = PreferenceListenerHandle(key, callback)
        synchronized(listeners) { listeners.add(handle) }
        return handle
    }

    fun addListener(
        key: Key,
        callback: (PreferenceChangeEvent) -> Unit,
    ): PreferenceListenerHandle {
        val handle = PreferenceListenerHandle(key.name, callback)
        synchronized(listeners) { listeners.add(handle) }
        return handle
    }

    fun removeListener(handle: PreferenceListenerHandle?) {
        if (handle != null) synchronized(listeners) { listeners.remove(handle) }
    }

    private fun dispatchToListeners(event: PreferenceChangeEvent) {
        val snapshot = synchronized(listeners) { listeners.toList() }
        snapshot.forEach { if (it.key == null || it.key == event.key) it.callback(event) }
    }

    fun dispose() {
        scope.cancel()
        storage.dispose()
        synchronized(listeners) { listeners.clear() }
    }
}