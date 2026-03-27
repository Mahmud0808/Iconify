package com.drdisagree.iconify.features.common.viewmodels

import androidx.lifecycle.ViewModel
import com.drdisagree.iconify.data.repository.SystemActionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SystemActionViewModel @Inject constructor(
    private val systemActionRepository: SystemActionRepository
) : ViewModel() {

    val shouldRestartSystemUI = systemActionRepository.shouldRestartSystemUI
    val shouldRebootDevice = systemActionRepository.shouldRebootDevice

    fun shouldRestartSystemUI() = systemActionRepository.shouldRestartSystemUI()
    fun shouldRebootDevice() = systemActionRepository.shouldRebootDevice()
    fun triggerRestartSystemUI() = systemActionRepository.triggerRestartSystemUI()
    fun triggerRestartDevice() = systemActionRepository.triggerRestartDevice()
    fun clearFlags() = systemActionRepository.clearFlags()
}