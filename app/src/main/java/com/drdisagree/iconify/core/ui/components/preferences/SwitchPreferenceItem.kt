package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.others.withHapticResult

@Composable
fun SwitchPreferenceItem(
    def: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.Switch,
    modifier: Modifier,
) {
    val checked by prefController.observe(def.key, false)
    val containerColor = if (type.isMasterSwitch) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (type.isMasterSwitch) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val shape = if (type.isMasterSwitch) CircleShape else shape
    val contentPadding = if (type.isMasterSwitch) PaddingValues(20.dp)
    else PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    val summary = if (type.isMasterSwitch) null else summary

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        containerColor = containerColor,
        contentPadding = contentPadding,
        onClick = withHaptic { if (isEnabled) prefController.setBoolean(def.key, !checked) }
    ) {
        LeadingIcon(def.icon, isEnabled, contentColor)
        TitleSummaryBlock(def.title, summary, isEnabled, contentColor)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 1.dp) {
            Switch(
                checked = checked,
                onCheckedChange = withHapticResult {
                    if (isEnabled) prefController.setBoolean(def.key, it as Boolean)
                },
                enabled = isEnabled,
            )
        }
    }
}