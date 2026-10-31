package com.drdisagree.iconify.core.ui.components.pagers

import android.content.Context
import android.view.View
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PreviewViewCache @Inject constructor() : ViewModel() {

    private val views = mutableStateMapOf<Int, View>()

    fun viewsFor(context: Context): SnapshotStateMap<Int, View> {
        if (views.values.any { it.context !== context }) views.clear()
        return views
    }

    override fun onCleared() {
        views.clear()
    }
}
