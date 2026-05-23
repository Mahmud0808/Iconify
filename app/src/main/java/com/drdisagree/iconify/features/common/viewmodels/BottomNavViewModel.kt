package com.drdisagree.iconify.features.common.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.drdisagree.iconify.app.navigation.BOTTOM_BAR_TABS
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.floor

@HiltViewModel
class BottomNavViewModel @Inject constructor() : ViewModel() {

    val defaultTabIndex: Int
        get() = floor((BOTTOM_BAR_TABS.size / 2).toDouble()).toInt()

    var selectedTabIndex by mutableIntStateOf(defaultTabIndex)
        private set

    var isBottomBarVisible by mutableStateOf(false)
        private set

    fun selectTab(index: Int) {
        selectedTabIndex = index
    }

    fun showBottomBar(show: Boolean) {
        isBottomBarVisible = show
    }
}