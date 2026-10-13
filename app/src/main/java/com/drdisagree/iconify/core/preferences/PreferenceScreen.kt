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
import androidx.compose.runtime.key
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

    remember(items) {
        categories.forEach { cat ->
            cat.preferences.forEach { pref ->
                prefController.init(pref.key, pref.defaultValue)
            }
        }
    }

    val lazyKeys = remember(items) { buildLazyKeys(items) }

    val firstLoadMaps = remember(items) {
        items.map { screenItem ->
            (screenItem as? PreferenceScreenItem.Category)?.let { category ->
                mutableStateMapOf<String, Boolean>().also { map ->
                    category.definition.preferences.forEach { map[it.key] = true }
                }
            }
        }
    }

    val visibleIndicesStates = items.mapIndexed { itemIndex, screenItem ->
        key(itemIndex) {
            (screenItem as? PreferenceScreenItem.Category)?.let {
                rememberCategoryVisibleIndices(it.definition, prefController)
            }
        }
    }

    LaunchedEffect(items) {
        delay(100)
        firstLoadMaps.forEach { map ->
            map?.keys?.forEach { map[it] = false }
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
            var targetLazyKey: Any? = null
            for ((itemIndex, screenItem) in items.withIndex()) {
                if (found) break
                when (screenItem) {
                    is PreferenceScreenItem.Category -> {
                        flatIndex++ // header
                        val cat = screenItem.definition
                        for ((prefIndex, pref) in cat.preferences.withIndex()) {
                            if (pref.key == key) {
                                found = true
                                targetLazyKey = (lazyKeys[itemIndex] as? CategoryLazyKeys)
                                    ?.prefs
                                    ?.getOrNull(prefIndex)
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
                val visibleIndex = allKeys.indexOfFirst { it == targetLazyKey }

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
            val previousVisibility = mutableListOf<State<List<Int>>>()

            items.forEachIndexed { itemIndex, screenItem ->
                when (screenItem) {
                    is PreferenceScreenItem.Category -> {
                        val cat = screenItem.definition
                        val earlierCategories = previousVisibility.toList()
                        val visibleIndices = visibleIndicesStates[itemIndex] ?: EmptyVisibleIndices

                        preferenceCategoryItems(
                            category = cat,
                            prefController = prefController,
                            lazyKeys = lazyKeys[itemIndex] as CategoryLazyKeys,
                            addTopSpacer = { earlierCategories.any { it.value.isNotEmpty() } },
                            firstLoadMap = firstLoadMaps[itemIndex] ?: emptyMap(),
                            visibleIndices = visibleIndices,
                            highlightKey = { activeHighlightKey },
                        )

                        previousVisibility += visibleIndices
                    }

                    is PreferenceScreenItem.Custom -> {
                        item(key = lazyKeys[itemIndex], contentType = "custom") {
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

class CategoryLazyKeys(val header: String, val prefs: List<String>)

private val EmptyVisibleIndices: State<List<Int>> = mutableStateOf(emptyList())

private fun buildLazyKeys(items: List<PreferenceScreenItem>): List<Any> {
    val used = HashSet<String>()

    fun unique(base: String): String {
        var candidate = base
        var suffix = 1
        while (!used.add(candidate)) candidate = "$base#${suffix++}"
        return candidate
    }

    return items.mapIndexed { index, item ->
        when (item) {
            is PreferenceScreenItem.Category -> CategoryLazyKeys(
                header = unique("header:$index"),
                prefs = item.definition.preferences.map { unique("pref:${it.key}") }
            )

            is PreferenceScreenItem.Custom -> unique("custom:${item.key}")
        }
    }
}
