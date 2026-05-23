package com.drdisagree.iconify.core.ui.components.preferences

import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.preferences.PrefParam
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.preferences.resolveOrNull
import com.drdisagree.iconify.core.preferences.toValueOrNull
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.others.withHapticResult

@Composable
fun SwitchPreferenceItem(
    prefDefinition: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    type: PreferenceType.Switch,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val navController = LocalNavController.current
    val checked by prefController.observe(
        prefDefinition.key,
        (prefDefinition.defaultValue as PrefValue.BoolValue).v
    )
    val containerColor = if (type.isMasterSwitch) MaterialTheme.colorScheme.primaryContainer
    else MaterialTheme.colorScheme.surfaceContainerHigh
    val contentColor = if (type.isMasterSwitch) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurface
    val shape = if (type.isMasterSwitch) CircleShape else shape
    val contentPadding = if (type.isMasterSwitch) PaddingValues(20.dp)
    else PaddingValues(horizontal = 16.dp, vertical = 14.dp)

    val param = PrefParam(
        prefDefinition.key,
        prefDefinition.defaultValue.toValueOrNull(),
        checked,
        context,
        activity,
        prefController,
        navController
    )

    val summary =
        if (type.isMasterSwitch) null else prefDefinition.summary?.invoke(param).resolveOrNull()

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        containerColor = containerColor,
        contentPadding = contentPadding,
        onClick = withHaptic {
            if (isEnabled) prefController.setBoolean(
                prefDefinition.key,
                !checked
            )
        }
    ) {
        LeadingIcon(prefDefinition.icon, isEnabled, contentColor)
        TitleSummaryBlock(prefDefinition.title, summary, isEnabled, contentColor)
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 1.dp) {
            Switch(
                checked = checked,
                onCheckedChange = withHapticResult {
                    if (isEnabled) prefController.setBoolean(prefDefinition.key, it as Boolean)
                },
                enabled = isEnabled,
            )
        }
    }
}