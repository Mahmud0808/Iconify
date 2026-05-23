package com.drdisagree.iconify.features.xposed.quicksettings.clock.viewmodels

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.common.Resources.HEADER_CLOCK_LAYOUT
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HeaderClockViewModel @Inject constructor() : ViewModel() {

    private val _clockLayoutIds = MutableStateFlow<List<Int>>(emptyList())
    val clockLayoutIds: StateFlow<List<Int>> = _clockLayoutIds.asStateFlow()

    fun loadClockLayouts(resources: Resources) {
        if (_clockLayoutIds.value.isNotEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val ids = buildList {
                var index = 0
                while (true) {
                    @SuppressLint("DiscouragedApi")
                    val id = resources.getIdentifier(
                        HEADER_CLOCK_LAYOUT + index,
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