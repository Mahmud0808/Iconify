package com.drdisagree.iconify.core.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.search.SearchHighlightState
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.preferences.preferenceCategoryItems
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.core.ui.components.topappbar.TopAppBarAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first

fun preferenceScreen(
    block: PreferenceScreenScope.() -> Unit
): List<PreferenceScreenItem> = PreferenceScreenScope().apply(block).items

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferenceScreen(
    modifier: Modifier = Modifier,
    items: List<PreferenceScreenItem>,
    title: String,
    subtitle: String = "",
    @DrawableRes backIcon: Int? = null,
    showBackIcon: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    showActionIcon: Boolean = true,
    actions: List<TopAppBarAction> = emptyList(),
) {
    val prefController = LocalPreferenceController.current
    val listState = rememberLazyListState()

    // ── Highlight support ──────────────────────────────────────────
    val highlightKey by SearchHighlightState.highlightKey.collectAsStateWithLifecycle()
    var activeHighlightKey by remember { mutableStateOf<String?>(null) }

    val categories = remember(items) {
        items.filterIsInstance<PreferenceScreenItem.Category>().map { it.definition }
    }

    val firstLoadMapPerCategory = remember(items) {
        categories.associateWith { cat ->
            mutableStateMapOf<String, Boolean>().also { map ->
                cat.preferences.forEach { map[it.key] = true }
            }
        }
    }

    val visibleIndicesPerCategory = categories.associateWith { cat ->
        rememberCategoryVisibleIndices(cat, prefController)
    }

    LaunchedEffect(items) {
        categories.forEach { cat ->
            cat.preferences.forEach { pref ->
                prefController.init(pref.key, pref.defaultValue)
            }
        }
        delay(100)
        firstLoadMapPerCategory.forEach { (_, map) ->
            map.keys.forEach { map[it] = false }
        }
    }

    AppScaffold(
        modifier = modifier,
        title = title,
        subtitle = subtitle,
        backIcon = backIcon,
        showBackIcon = showBackIcon,
        onBackClick = onBackClick,
        showActionIcon = showActionIcon,
        actions = actions
    ) { innerPadding, scrollBehavior ->
        // ── Scroll to + highlight the target preference ────────────────
        LaunchedEffect(highlightKey, items) {
            val key = highlightKey ?: return@LaunchedEffect

            // Compute the flat index by iterating through categories
            var flatIndex = 0
            var found = false
            for (screenItem in items) {
                if (found) break
                when (screenItem) {
                    is PreferenceScreenItem.Category -> {
                        flatIndex++ // header
                        val cat = screenItem.definition
                        for (pref in cat.preferences) {
                            if (pref.key == key) {
                                found = true
                                break
                            }
                            flatIndex++
                        }
                    }

                    is PreferenceScreenItem.Custom -> {
                        flatIndex++
                    }
                }
            }

            if (found) {
                // Wait for the list to be populated with enough items
                snapshotFlow { listState.layoutInfo.totalItemsCount }
                    .filter { it > flatIndex }
                    .first()

                // Try to find it among visible items first
                val layoutInfo = listState.layoutInfo
                val allKeys = layoutInfo.visibleItemsInfo.map { it.key }
                val visibleIndex = allKeys.indexOfFirst { it == key }

                if (visibleIndex >= 0) {
                    val itemInfo = layoutInfo.visibleItemsInfo[visibleIndex]
                    listState.animateScrollToItem(itemInfo.index)
                } else {
                    listState.animateScrollToItem(flatIndex)
                }

                // Adjust top app bar based on scroll target
                if (flatIndex > 0) {
                    scrollBehavior.state.heightOffset = scrollBehavior.state.heightOffsetLimit
                } else {
                    scrollBehavior.state.heightOffset = 0f
                }
            }

            // Trigger highlight animation
            activeHighlightKey = key

            // Auto-clear highlight after animation
            delay(1500)
            activeHighlightKey = null
            SearchHighlightState.clearHighlight()
        }

        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            horizontal = 16.dp,
            vertical = 16.dp
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = padding,
        ) {
            var firstVisibleCategoryAdded = false

            items.forEach { screenItem ->
                when (screenItem) {
                    is PreferenceScreenItem.Category -> {
                        val cat = screenItem.definition
                        val isFirstVisible = !firstVisibleCategoryAdded
                        val firstLoadMap = firstLoadMapPerCategory[cat] ?: emptyMap()
                        val visibleIndices = visibleIndicesPerCategory[cat]?.value ?: emptyList()

                        preferenceCategoryItems(
                            category = cat,
                            prefController = prefController,
                            addTopSpacer = !isFirstVisible,
                            firstLoadMap = firstLoadMap,
                            visibleIndices = visibleIndices,
                            highlightKey = activeHighlightKey,
                        )

                        if (visibleIndices.isNotEmpty()) {
                            firstVisibleCategoryAdded = true
                        }
                    }

                    is PreferenceScreenItem.Custom -> {
                        item(key = screenItem.key, contentType = "custom") {
                            screenItem.content()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberCategoryVisibleIndices(
    category: PreferenceCategoryDefinition,
    prefController: PreferenceController,
): State<List<Int>> = remember(category) {
    derivedStateOf {
        category.preferences.indices.filter { i ->
            category.preferences[i].isVisible(prefController)
        }
    }
}