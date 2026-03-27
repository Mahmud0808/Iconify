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

    val controller = PreferenceController(preferenceStorage)

    init {
        viewModelScope.launch {
            loadSettings()
        }
    }

    private fun loadSettings() {
        controller.initAll(
            SettingsKey.entries.associate { key ->
                key.name to key.default.toPrefValue()
            }
        )
    }

    fun getValue(key: SettingsKey): Any? {
        return controller.get(key.name, key.default?.toPrefValue())
            ?: key.default
    }

    fun setValue(key: SettingsKey, value: Any) {
        controller.set(key.name, value.toPrefValue())
    }

    fun getBooleanFlow(key: SettingsKey): Flow<Boolean> = controller.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { controller.getBoolean(key.name, key.default as Boolean) }
        .onStart { emit(controller.getBoolean(key.name, key.default as Boolean)) }

    fun getIntFlow(key: SettingsKey): Flow<Int> = controller.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { controller.getInt(key.name, key.default as Int) }
        .onStart { emit(controller.getInt(key.name, key.default as Int)) }

    fun getFloatFlow(key: SettingsKey): Flow<Float> = controller.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { controller.getFloat(key.name, key.default as Float) }
        .onStart { emit(controller.getFloat(key.name, key.default as Float)) }

    fun getStringFlow(key: SettingsKey): Flow<String> = controller.changesFlow
        .filterNotNull()
        .filter { it.key == key.name }
        .map { controller.getString(key.name, key.default as String) }
        .onStart { emit(controller.getString(key.name, key.default as String)) }

    override fun onCleared() {
        controller.dispose()
        super.onCleared()
    }
}