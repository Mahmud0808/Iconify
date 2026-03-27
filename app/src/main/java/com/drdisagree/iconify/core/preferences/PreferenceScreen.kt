package com.drdisagree.iconify.core.preferences

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.preferences.preferenceCategoryItems
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold

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

    // Seed defaults once when the category list is first seen.
    LaunchedEffect(items) {
        items.filterIsInstance<PreferenceScreenItem.Category>()
            .forEach { cat ->
                cat.definition.preferences.forEach { pref ->
                    prefController.init(pref.key, pref.defaultValue)
                }
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
            innerPadding = innerPadding, horizontal = 16.dp, vertical = 16.dp
        )

        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = padding,
        ) {
            var firstVisibleCategoryAdded = false

            items.forEach { screenItem ->
                when (screenItem) {
                    is PreferenceScreenItem.Category -> {
                        val isFirstVisible = !firstVisibleCategoryAdded
                        preferenceCategoryItems(
                            category = screenItem.definition,
                            controller = prefController,
                            addTopSpacer = !isFirstVisible
                        )

                        if (screenItem.definition.preferences.any { it.isVisible(prefController) }) {
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