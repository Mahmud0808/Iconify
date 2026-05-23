package com.drdisagree.iconify.core.ui.components.preferences

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.others.withHapticResult

@Composable
fun TwoTargetSwitchPreferenceItem(
    prefDefinition: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    type: PreferenceType.TwoTargetSwitch,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val navController = LocalNavController.current

    val checked by prefController.observe(
        prefDefinition.key,
        (prefDefinition.defaultValue as PrefValue.BoolValue).v
    )

    val param = PrefParam(
        prefDefinition.key,
        prefDefinition.defaultValue.v,
        checked,
        context,
        activity,
        prefController,
        navController
    )

    val summary = prefDefinition.summary?.invoke(param).resolveOrNull()

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        onClick = withHaptic { type.onClick(param) }
    ) {
        LeadingIcon(prefDefinition.icon, isEnabled)
        TitleSummaryBlock(prefDefinition.title, summary, isEnabled)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(16.dp)
            )
            VerticalDivider(
                modifier = Modifier.height(32.dp),
                color = MaterialTheme.colorScheme.outline,
                thickness = 1.dp,
            )
        }

        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 1.dp) {
            Switch(
                checked = checked,
                onCheckedChange = withHapticResult {
                    if (isEnabled) prefController.setBoolean(
                        prefDefinition.key,
                        it as Boolean
                    )
                },
                enabled = isEnabled,
            )
        }
    }
}