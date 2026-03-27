package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.iconRes
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText

@Composable
fun InfoPreferenceItem(
    def: PreferenceDefinition,
    isEnabled: Boolean,
    summary: String?,
    modifier: Modifier,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface

    PreferenceContainer(
        shape = RoundedCornerShape(0.dp),
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = 1,
        containerColor = Color.Transparent,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LeadingIcon(def.icon ?: iconRes(R.drawable.ic_info), isEnabled)
            Spacer(Modifier.height(12.dp))
            Text(
                text = def.title.resolve(),
                style = MaterialTheme.typography.bodyMedium,
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
}