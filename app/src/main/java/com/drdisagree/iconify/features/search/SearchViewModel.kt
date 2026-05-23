package com.drdisagree.iconify.features.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.search.SearchIndex
import com.drdisagree.iconify.core.search.SearchablePreference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    application: Application,
    private val prefController: PreferenceController,
) : AndroidViewModel(application) {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _results = MutableStateFlow<List<SearchablePreference>>(emptyList())
    val results: StateFlow<List<SearchablePreference>> = _results.asStateFlow()

    private val _history = MutableStateFlow<List<SearchablePreference>>(emptyList())
    val history: StateFlow<List<SearchablePreference>> = _history.asStateFlow()

    private val context get() = getApplication<Application>()

    init {
        loadHistory()
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
        _results.value = if (newQuery.isBlank()) {
            emptyList()
        } else {
            SearchIndex.allItems.filter { pref ->
                pref.isVisible(prefController) && matchesQuery(pref, newQuery)
            }
        }
    }

    fun clearQuery() {
        _query.value = ""
        _results.value = emptyList()
        loadHistory()
    }

    fun addToHistory(pref: SearchablePreference) {
        val currentKeys = prefController.getString("search_history_keys", "")
            .split(",")
            .filter { it.isNotBlank() }
            .toMutableList()

        currentKeys.remove(pref.key)
        currentKeys.add(0, pref.key)

        if (currentKeys.size > 20) {
            currentKeys.removeAt(currentKeys.size - 1)
        }

        prefController.setString("search_history_keys", currentKeys.joinToString(","))
        loadHistory()
    }

    fun clearHistory() {
        prefController.setString("search_history_keys", "")
        loadHistory()
    }

    private fun loadHistory() {
        val keys = prefController.getString("search_history_keys", "")
            .split(",")
            .filter { it.isNotBlank() }

        _history.value = keys.mapNotNull { key ->
            SearchIndex.allItems.find { it.key == key && it.isVisible(prefController) }
        }
    }

    /**
     * Resolves a [PrefStringRes] to its string value outside of Compose.
     * Handles Hardcoded and Resource variants; Composable variants return null.
     */
    fun resolveText(stringRes: PrefStringRes): String? = when (stringRes) {
        is PrefStringRes.Hardcoded -> stringRes.value
        is PrefStringRes.Resource -> context.getString(stringRes.resId)
        is PrefStringRes.Composable -> null
    }

    private fun matchesQuery(pref: SearchablePreference, query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return false

        // Match title
        val titleText = resolveText(pref.title)
        if (titleText != null && titleText.lowercase().contains(q)) return true

        // Match breadcrumbs
        pref.breadcrumbs.forEach { bc ->
            val bcText = resolveText(bc)
            if (bcText != null && bcText.lowercase().contains(q)) return true
        }

        // Match screen title
        val screenTitle = context.getString(pref.screenTitleResId)
        return screenTitle.lowercase().contains(q)
    }
}
