package com.drdisagree.iconify.core.search

import androidx.annotation.StringRes
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.preferences.PreferenceController

/**
 * Represents a single preference item that can appear in search results.
 *
 * @property key The unique preference key (used for highlight on navigation).
 * @property title The title of the preference item.
 * @property screenTitleResId The R.string ID of the parent screen's title.
 * @property breadcrumbs Ordered list of breadcrumb labels (e.g. ["Xposed", "Status Bar", "Clock Chip"]).
 * @property route The NavRoutes destination that hosts this preference.
 * @property isVisible Visibility check — hidden items should not appear in search.
 */
data class SearchablePreference(
    val key: String,
    val title: PrefStringRes,
    @param:StringRes val screenTitleResId: Int,
    val breadcrumbs: List<PrefStringRes>,
    val route: NavRoutes,
    val isVisible: ((PreferenceController) -> Boolean) = { true },
)
