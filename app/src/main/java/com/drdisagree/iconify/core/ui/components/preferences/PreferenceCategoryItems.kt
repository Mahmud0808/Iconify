package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PreferenceCategoryDefinition
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.ui.components.others.animatedPreferenceShape
import com.drdisagree.iconify.core.ui.utils.CARD_ITEM_SPACING
import com.drdisagree.iconify.core.ui.utils.resolvePosition

fun LazyListScope.preferenceCategoryItems(
    category: PreferenceCategoryDefinition,
    prefController: PreferenceController,
    addTopSpacer: Boolean = true,
    firstLoadMap: Map<String, Boolean>,
    visibleIndices: List<Int>,
    highlightKey: String? = null,
) {
    item(
        key = "header_${System.identityHashCode(category)}",
        contentType = "category_header"
    ) {
        if (visibleIndices.isEmpty()) return@item
        if (addTopSpacer) Spacer(Modifier.height(24.dp))
        CategoryTitleRow(title = category.title, icon = category.icon)
    }

    itemsIndexed(
        items = category.preferences,
        key = { _, pref -> pref.key },
        contentType = { _, pref -> pref.type::class.simpleName },
    ) { index, prefDefinition ->
        val isVisible = index in visibleIndices
        val position = resolvePosition(visibleIndices, index)
        val shape = animatedPreferenceShape(position)
        val firstLoad = firstLoadMap[prefDefinition.key] ?: false
        val isHighlighted = highlightKey == prefDefinition.key

        AnimatedVisibility(
            visible = isVisible,
            enter = if (firstLoad) EnterTransition.None else fadeIn() + expandVertically(),
            exit = if (firstLoad) ExitTransition.None else fadeOut() + shrinkVertically(),
        ) {
            val isEnabled = prefDefinition.isEnabled(prefController)
            val topPad = if (index == visibleIndices.firstOrNull()) 0.dp else CARD_ITEM_SPACING

            PreferenceItem(
                prefDefinition = prefDefinition,
                prefController = prefController,
                shape = shape,
                isEnabled = isEnabled,
                isHighlighted = isHighlighted,
                modifier = Modifier.padding(top = topPad),
            )
        }
    }
}