package com.drdisagree.iconify.data.storage

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.toPrefValue
import com.drdisagree.iconify.data.common.XposedConst.PREF_FILE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

data class ExternalChange(val key: String, val value: PrefValue)

interface PreferenceStorage {
    val externalChanges: Flow<ExternalChange>
    fun loadAll(): Map<String, PrefValue>
    fun read(key: String, defaultValue: PrefValue): Any?
    fun write(key: String, value: PrefValue)
    fun clearAll()
    fun dispose()
}

@Singleton
class SharedPreferencesStorage @Inject constructor(
    @ApplicationContext context: Context,
    fileName: String = PREF_FILE_NAME,
) : PreferenceStorage {

    private val sp: SharedPreferences = context
        .applicationContext
        .createDeviceProtectedStorageContext()
        .getSharedPreferences(fileName, MODE_PRIVATE)

    override fun loadAll(): Map<String, PrefValue> {
        return sp.all.mapNotNull { (key, raw) ->
            val pv = when (raw) {
                is Boolean -> PrefValue.BoolValue(raw)
                is Int -> PrefValue.IntValue(raw)
                is Float -> PrefValue.FloatValue(raw)
                is String -> PrefValue.StringValue(raw)
                is Set<*> -> PrefValue.StringSetValue(raw.filterIsInstance<String>().toSet())
                else -> null
            }
            pv?.let { key to it }
        }.toMap()
    }

    override fun read(key: String, defaultValue: PrefValue): Any? {
        return when (defaultValue) {
            is PrefValue.BoolValue -> sp.getBoolean(key, defaultValue.v)
            is PrefValue.IntValue -> sp.getInt(key, defaultValue.v)
            is PrefValue.FloatValue -> sp.getFloat(key, defaultValue.v)
            is PrefValue.DoubleValue -> sp.getString(key, defaultValue.v.toString())?.toDouble()
            is PrefValue.StringValue -> sp.getString(key, defaultValue.v) ?: defaultValue.v
            is PrefValue.StringSetValue -> sp.getStringSet(key, defaultValue.v) ?: defaultValue.v
            is PrefValue.None -> null
        }
    }

    override fun write(key: String, value: PrefValue) {
        sp.edit {
            when (value) {
                is PrefValue.BoolValue -> putBoolean(key, value.v)
                is PrefValue.IntValue -> putInt(key, value.v)
                is PrefValue.FloatValue -> putFloat(key, value.v)
                is PrefValue.DoubleValue -> putString(key, value.v.toString())
                is PrefValue.StringValue -> putString(key, value.v)
                is PrefValue.StringSetValue -> putStringSet(key, value.v)
                is PrefValue.None -> Unit
            }
        }
    }

    override val externalChanges: Flow<ExternalChange> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, key ->
            if (key == null) return@OnSharedPreferenceChangeListener
            val raw = prefs.all[key] ?: return@OnSharedPreferenceChangeListener
            val pv = when (raw) {
                is Boolean -> PrefValue.BoolValue(raw)
                is Int -> PrefValue.IntValue(raw)
                is Float -> PrefValue.FloatValue(raw)
                is Double -> PrefValue.DoubleValue(raw)
                is String -> PrefValue.StringValue(raw)
                is Set<*> -> PrefValue.StringSetValue(
                    raw.filterIsInstance<String>().toSet()
                )

                else -> return@OnSharedPreferenceChangeListener
            }
            trySend(ExternalChange(key, pv))
        }
        sp.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { sp.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    override fun clearAll() {
        sp.edit { clear() }
    }

    override fun dispose() {}
}

@Singleton
class DataStoreStorage @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val scope: CoroutineScope,
) : PreferenceStorage {

    override fun loadAll(): Map<String, PrefValue> {
        val snapshot = runBlocking { dataStore.data.first() }
        return snapshot.asMap().mapNotNull { (key, raw) ->
            val pv = raw.toPrefValue()
            key.name to pv
        }.toMap()
    }

    override fun read(key: String, defaultValue: PrefValue): Any? {
        val snapshot = runBlocking { dataStore.data.first() }

        return when (defaultValue) {
            is PrefValue.BoolValue -> snapshot[booleanPreferencesKey(key)] ?: defaultValue.v
            is PrefValue.IntValue -> snapshot[intPreferencesKey(key)] ?: defaultValue.v
            is PrefValue.FloatValue -> snapshot[floatPreferencesKey(key)] ?: defaultValue.v
            is PrefValue.DoubleValue -> snapshot[doublePreferencesKey(key)] ?: defaultValue.v
            is PrefValue.StringValue -> snapshot[stringPreferencesKey(key)] ?: defaultValue.v
            is PrefValue.StringSetValue -> snapshot[stringSetPreferencesKey(key)] ?: defaultValue.v
            is PrefValue.None -> null
        }
    }

    override fun write(key: String, value: PrefValue) {
        scope.launch {
            dataStore.edit { prefs ->
                when (value) {
                    is PrefValue.BoolValue -> prefs[booleanPreferencesKey(key)] = value.v
                    is PrefValue.IntValue -> prefs[intPreferencesKey(key)] = value.v
                    is PrefValue.FloatValue -> prefs[floatPreferencesKey(key)] = value.v
                    is PrefValue.DoubleValue -> prefs[doublePreferencesKey(key)] = value.v
                    is PrefValue.StringValue -> prefs[stringPreferencesKey(key)] = value.v
                    is PrefValue.StringSetValue -> prefs[stringSetPreferencesKey(key)] = value.v
                    is PrefValue.None -> Unit
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val externalChanges: Flow<ExternalChange> =
        dataStore.data
            .distinctUntilChanged()
            .zipWithPrevious()                      // (prev, current) pairs
            .drop(1)                                // skip the (null, initial) pair
            .flatMapConcat { (prev, current) ->
                val prevMap = prev?.asMap() ?: emptyMap()
                val currMap = current.asMap()
                // Find keys whose value changed between snapshots
                val changed = currMap.filter { (k, v) -> prevMap[k] != v }
                changed.mapNotNull { (key, raw) ->
                    ExternalChange(key.name, raw.toPrefValue())
                }.asFlow()
            }

    override fun clearAll() {
        scope.launch {
            dataStore.edit { it.clear() }
        }
    }

    override fun dispose() {
        scope.cancel()
    }

    private fun <T> Flow<T>.zipWithPrevious(): Flow<Pair<T?, T>> = flow {
        var previous: T? = null
        collect { current ->
            emit(previous to current)
            previous = current
        }
    }
}