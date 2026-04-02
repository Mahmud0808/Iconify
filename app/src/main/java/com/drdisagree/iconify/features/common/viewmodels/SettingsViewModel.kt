package com.drdisagree.iconify.features.common.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.core.di.SharedPrefs
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.toPrefValue
import com.drdisagree.iconify.data.keys.SettingsKey
import com.drdisagree.iconify.data.storage.PreferenceStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:SharedPrefs private val preferenceStorage: PreferenceStorage
) : ViewModel() {

    private val prefController = PreferenceController(preferenceStorage)

    init {
        viewModelScope.launch {
            loadSettings()
        }
    }

    private fun loadSettings() {
        prefController.initAll(
            SettingsKey.entries.associate { key ->
                key.name to key.default.toPrefValue()
            }
        )
    }

    fun getValue(key: SettingsKey): Any? {
        return prefController.get(key.name, key.default?.toPrefValue())
            ?: key.default
    }

    fun setValue(key: SettingsKey, value: Any) {
        prefController.set(key.name, value.toPrefValue())
    }

    fun getBooleanFlow(key: SettingsKey): Flow<Boolean> = prefController.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { prefController.getBoolean(key.name, key.default as Boolean) }
        .onStart { emit(prefController.getBoolean(key.name, key.default as Boolean)) }

    fun getIntFlow(key: SettingsKey): Flow<Int> = prefController.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { prefController.getInt(key.name, key.default as Int) }
        .onStart { emit(prefController.getInt(key.name, key.default as Int)) }

    fun getFloatFlow(key: SettingsKey): Flow<Float> = prefController.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { prefController.getFloat(key.name, key.default as Float) }
        .onStart { emit(prefController.getFloat(key.name, key.default as Float)) }

    fun getStringFlow(key: SettingsKey): Flow<String> = prefController.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { prefController.getString(key.name, key.default as String) }
        .onStart { emit(prefController.getString(key.name, key.default as String)) }

    override fun onCleared() {
        prefController.dispose()
        super.onCleared()
    }
}