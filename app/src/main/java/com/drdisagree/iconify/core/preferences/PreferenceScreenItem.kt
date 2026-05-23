package com.drdisagree.iconify.core.preferences

import androidx.compose.runtime.Composable

sealed class PreferenceScreenItem {
    data class Category(val definition: PreferenceCategoryDefinition) : PreferenceScreenItem()
    data class Custom(val key: String, val content: @Composable () -> Unit) : PreferenceScreenItem()
}