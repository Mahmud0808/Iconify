package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.others.withHapticResult

@Composable
fun TwoTargetSwitchPreferenceItem(
    def: PreferenceDefinition,
    controller: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.TwoTargetSwitch,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val preferenceController = LocalPreferenceController.current
    val navController = LocalNavController.current

    val checked by controller.observe(def.key, false)
    val interactionSource = remember { MutableInteractionSource() }
    val itemMinHeight = if (summary.isNullOrEmpty()) SINGLE_LINE_LIST_ITEM_MIN_HEIGHT
    else TWO_LINE_LIST_ITEM_MIN_HEIGHT

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = itemMinHeight)
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    enabled = isEnabled,
                    role = Role.Button,
                    onClick = withHaptic {
                        type.onClick(
                            context,
                            preferenceController,
                            navController
                        )
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LeadingIcon(def.icon, isEnabled)
            TitleSummaryBlock(def.title, summary, isEnabled)
        }

        VerticalDivider(
            modifier = Modifier
                .height(32.dp)
                .padding(horizontal = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
        )

        Box(
            modifier = Modifier
                .heightIn(min = itemMinHeight)
                .fillMaxHeight()
                .clickable(
                    enabled = isEnabled,
                    role = Role.Switch,
                    onClick = withHaptic { controller.setBoolean(def.key, !checked) }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center,
        ) {
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
}