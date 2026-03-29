package com.drdisagree.iconify.features.xposed.lockscreen.clock.viewmodels

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.common.Resources.LOCKSCREEN_CLOCK_LAYOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LockscreenClockViewModel @Inject constructor() : ViewModel() {

    private val _clockLayoutIds = MutableStateFlow<List<Int>>(emptyList())
    val clockLayoutIds: StateFlow<List<Int>> = _clockLayoutIds.asStateFlow()

    @SuppressLint("DiscouragedApi")
    fun loadClockLayouts(resources: Resources) {
        viewModelScope.launch(Dispatchers.IO) {
            val ids = buildList {
                var index = 0
                while (true) {
                    val id = resources.getIdentifier(
                        LOCKSCREEN_CLOCK_LAYOUT + index,
                        "layout",
                        BuildConfig.APPLICATION_ID
                    )
                    if (id == 0) break
                    add(id)
                    index++
                }
            }
            _clockLayoutIds.emit(ids)
        }
    }
}