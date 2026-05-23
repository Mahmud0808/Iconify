package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PrefIconRes
import com.drdisagree.iconify.core.preferences.PrefStringRes
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText

@Composable
fun LeadingIcon(
    icon: PrefIconRes?,
    isEnabled: Boolean,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    if (icon != null) {
        Icon(
            painter = icon.resolve(),
            contentDescription = null,
            tint = if (isEnabled) contentColor
            else contentColor.copy(alpha = 0.38f),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun RowScope.TitleSummaryBlock(
    title: PrefStringRes,
    summary: String?,
    isEnabled: Boolean,
    contentColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = title.resolve(),
            style = MaterialTheme.typography.bodyLarge,
            color = if (isEnabled) contentColor
            else contentColor.copy(alpha = 0.38f),
        )
        if (!summary.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = summary,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isEnabled) contentColor.secondaryText()
                else contentColor.copy(alpha = 0.38f),
            )
        }
    }
}