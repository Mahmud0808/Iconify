package com.drdisagree.iconify.core.ui.components.preferences

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
    controller: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.Switch,
    modifier: Modifier,
) {
    val checked by controller.observe(def.key, false)
    val containerColor = if (type.isMasterSwitch) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (type.isMasterSwitch) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        containerColor = containerColor,
        onClick = withHaptic { if (isEnabled) controller.setBoolean(def.key, !checked) }
    ) {
        LeadingIcon(def.icon, isEnabled, contentColor)
        TitleSummaryBlock(def.title, summary, isEnabled, contentColor)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 1.dp) {
            Switch(
                checked = checked,
                onCheckedChange = withHapticResult {
                    if (isEnabled) controller.setBoolean(
                        def.key,
                        it as Boolean
                    )
                },
                enabled = isEnabled,
            )
        }
    }
}