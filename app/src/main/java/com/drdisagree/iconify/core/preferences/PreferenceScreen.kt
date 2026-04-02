package com.drdisagree.iconify.core.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.preferences.preferenceCategoryItems
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import kotlinx.coroutines.delay

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
    actions: @Composable RowScope.() -> Unit = {},
) {
    val prefController = LocalPreferenceController.current

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
        actions = actions
    ) { innerPadding, _ ->
        val padding = innerPaddingValues(
            innerPadding = innerPadding,
            horizontal = 16.dp,
            vertical = 16.dp
        )

        LazyColumn(
            modifier = modifier.fillMaxSize(),
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