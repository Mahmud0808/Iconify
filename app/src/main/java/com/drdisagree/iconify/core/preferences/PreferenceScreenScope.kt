package com.drdisagree.iconify.core.preferences

import androidx.compose.runtime.Composable
import com.drdisagree.iconify.core.preferences.PreferenceScreenItem
import com.drdisagree.iconify.core.ui.components.extensions.toStringRes

/** Builds a list of [PreferenceCategoryDefinition]s using a concise DSL. */
class PreferenceScreenScope {
    val items = mutableListOf<PreferenceScreenItem>()

    fun category(
        title: PrefStringRes,
        icon: PrefIconRes? = null,
        block: PreferenceCategoryScope.() -> Unit,
    ) {
        val scope = PreferenceCategoryScope().apply(block)
        items += PreferenceScreenItem.Category(
            PreferenceCategoryDefinition(title, icon, scope.prefs)
        )
    }

    fun category(
        title: String = "",
        icon: PrefIconRes? = null,
        block: PreferenceCategoryScope.() -> Unit,
    ) = category(title.toStringRes(), icon, block)

    fun composable(
        key: String,
        content: @Composable () -> Unit
    ) {
        items += PreferenceScreenItem.Custom(key, content)
    }
}