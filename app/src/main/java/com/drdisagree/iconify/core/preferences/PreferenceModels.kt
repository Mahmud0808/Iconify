package com.drdisagree.iconify.core.preferences

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Represents an event triggered when a preference value is modified.
 *
 * @property key The unique identifier of the preference that was changed.
 * @property oldValue The previous value of the preference, or null if it was not previously set.
 * @property newValue The new value assigned to the preference.
 */
data class PreferenceChangeEvent(
    val key: String,
    val oldValue: PrefValue?,
    val newValue: PrefValue,
)

/**
 * Represents a grouping of related preferences under a specific section.
 *
 * @property title The display name of the category.
 * @property icon An optional [ImageVector] to be displayed alongside the category title.
 * @property preferences The list of [PreferenceDefinition] items belonging to this category.
 */
data class PreferenceCategoryDefinition(
    val title: PrefStringRes,
    val icon: PrefIconRes? = null,
    val preferences: List<PreferenceDefinition>,
)

/**
 * Immutable declaration of a single preference item.
 *
 * Visibility, enabled state, and summary are expressed as lambdas that
 * receive the current [PreferenceController], allowing them to depend on
 * other preference values. These are evaluated reactively during recomposition.
 *
 * @property key Unique identifier used for reading and writing the stored value.
 * @property title The display title of the preference.
 * @property defaultValue The initial value seeded into the prefController on first use.
 * @property type The interaction type (e.g., switch, slider, list) used to render the UI.
 * @property icon An optional leading icon to display next to the preference.
 * @property summary A dynamic lambda to derive summary text from the current state.
 * @property isVisible A predicate to determine if the preference should be shown in the UI.
 * @property isEnabled A predicate to determine if the preference is interactive or disabled.
 */
data class PreferenceDefinition(
    val key: String,
    val title: PrefStringRes,
    val defaultValue: PrefValue,
    val type: PreferenceType,
    val icon: PrefIconRes? = null,
    val summary: ((PreferenceController, String) -> PrefStringRes?)? = null,
    val isVisible: ((PreferenceController) -> Boolean) = { true },
    val isEnabled: ((PreferenceController) -> Boolean) = { true },
)