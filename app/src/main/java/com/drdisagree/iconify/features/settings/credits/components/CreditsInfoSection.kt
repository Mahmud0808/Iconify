package com.drdisagree.iconify.features.settings.credits.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.ui.components.others.animatedPreferenceShape
import com.drdisagree.iconify.core.ui.components.preferences.CategoryTitleRow
import com.drdisagree.iconify.core.ui.utils.resolvePosition
import com.drdisagree.iconify.features.settings.credits.models.CreditInfoModel

fun LazyListScope.creditsInfoSection(
    title: String,
    items: List<CreditInfoModel>,
) {
    item(key = "header_$title") {
        CategoryTitleRow(
            modifier = Modifier.padding(top = 24.dp),
            title = PrefStringRes.Hardcoded(title)
        )
    }

    itemsIndexed(
        items = items,
        key = { _, item -> item.url }
    ) { index, item ->
        val uriHandler = LocalUriHandler.current
        val position = resolvePosition(items.indices.toList(), index)
        val shape = animatedPreferenceShape(position)

        CreditsInfoItemRow(
            item = item,
            shape = shape,
            onClick = { uriHandler.openUri(item.url) },
            isFirstItem = index == 0
        )
    }
}