package com.drdisagree.iconify.core.search

import com.drdisagree.iconify.core.search.SearchHighlightState.clearHighlight
import com.drdisagree.iconify.core.search.SearchHighlightState.highlightKey
import com.drdisagree.iconify.core.search.SearchHighlightState.requestHighlight
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Shared singleton that carries a "highlight this preference key" signal
 * from the search screen to the target [com.drdisagree.iconify.core.preferences.PreferenceScreen].
 *
 * Flow:
 * 1. User taps a search result → [requestHighlight] is called with the pref key.
 * 2. NavController navigates to the target screen.
 * 3. The target [com.drdisagree.iconify.core.preferences.PreferenceScreen] observes [highlightKey], scrolls to the item,
 *    plays a highlight animation, then calls [clearHighlight].
 */
object SearchHighlightState {

    private val _highlightKey = MutableStateFlow<String?>(null)
    val highlightKey: StateFlow<String?> = _highlightKey.asStateFlow()

    fun requestHighlight(key: String) {
        _highlightKey.value = key
    }

    fun clearHighlight() {
        _highlightKey.value = null
    }
}
