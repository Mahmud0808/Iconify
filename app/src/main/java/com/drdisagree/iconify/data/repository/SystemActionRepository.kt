package com.drdisagree.iconify.data.repository

import com.drdisagree.iconify.core.utils.SystemUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SystemActionRepository @Inject constructor() {

    private val _shouldRestartSystemUI = MutableStateFlow(false)
    val shouldRestartSystemUI = _shouldRestartSystemUI.asStateFlow()

    private val _shouldRebootDevice = MutableStateFlow(false)
    val shouldRebootDevice = _shouldRebootDevice.asStateFlow()

    fun shouldRestartSystemUI() {
        _shouldRestartSystemUI.value = true
    }

    fun shouldRebootDevice() {
        _shouldRebootDevice.value = true
    }

    fun triggerRestartSystemUI() {
        SystemUtils.restartSystemUI()
        _shouldRestartSystemUI.value = false
    }

    fun triggerRestartDevice() {
        SystemUtils.restartDevice()
        _shouldRebootDevice.value = false
    }

    fun clearFlags() {
        _shouldRestartSystemUI.value = false
        _shouldRebootDevice.value = false
    }
}