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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PreferenceCategoryDefinition
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.resolveOrNull
import com.drdisagree.iconify.core.ui.components.others.animatedPreferenceShape
import com.drdisagree.iconify.core.ui.utils.CARD_ITEM_SPACING
import com.drdisagree.iconify.core.ui.utils.resolvePosition
import kotlinx.coroutines.delay

/**
 * Emits lazy list items for a single [PreferenceCategoryDefinition].
 *
 * This is a [LazyListScope] extension so it composes naturally inside any
 * [LazyColumn] — you can mix preference categories with other list content.
 *
 * Shape resolution (SOLO / FIRST / MIDDLE / LAST) is deferred to item
 * composition time via [derivedStateOf], so it reacts to visibility changes.
 */
fun LazyListScope.preferenceCategoryItems(
    category: PreferenceCategoryDefinition,
    controller: PreferenceController,
    addTopSpacer: Boolean = true
) {
    item(key = "header_${System.identityHashCode(category)}", contentType = "category_header") {
        // Re-evaluate whether this category has any visible children.
        // If none, skip rendering the header entirely.
        val hasVisible by remember {
            derivedStateOf {
                category.preferences.any { it.isVisible(controller) }
            }
        }
        if (!hasVisible) return@item

        if (addTopSpacer) Spacer(Modifier.height(24.dp))
        CategoryTitleRow(title = category.title, icon = category.icon)
    }

    items(
        items = category.preferences,
        key = { pref -> pref.key },
        contentType = { pref -> pref.type::class.simpleName },
    ) { pref ->
        // visibleIndices must be derived inside each item's composition
        // scope so that every item re-reads it when any sibling's
        // visibility changes — not just its own.
        val visibleIndices by remember {
            derivedStateOf {
                category.preferences.indices.filter { i ->
                    category.preferences[i].isVisible(controller)
                }
            }
        }

        val isVisible = pref.isVisible(controller)
        var firstLoad by remember { mutableStateOf(true) }

        LaunchedEffect(Unit) {
            delay(100)
            firstLoad = false
        }

        AnimatedVisibility(
            visible = isVisible,
            enter = if (firstLoad) EnterTransition.None else fadeIn() + expandVertically(),
            exit = if (firstLoad) ExitTransition.None else fadeOut() + shrinkVertically(),
        ) {
            val index = category.preferences.indexOf(pref)
            val position = resolvePosition(visibleIndices, index)
            val shape = animatedPreferenceShape(position)
            val isEnabled = pref.isEnabled(controller)
            val summary = pref.summary?.invoke(controller, pref.key).resolveOrNull()

            val topPad = if (index == visibleIndices.firstOrNull()) 0.dp
            else CARD_ITEM_SPACING

            PreferenceItem(
                definition = pref,
                controller = controller,
                shape = shape,
                isEnabled = isEnabled,
                summary = summary,
                modifier = Modifier.padding(top = topPad),
            )
        }
    }
}