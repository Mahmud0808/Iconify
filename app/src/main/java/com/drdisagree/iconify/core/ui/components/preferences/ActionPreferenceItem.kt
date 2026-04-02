package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.ui.components.others.withHaptic

@Composable
fun ActionPreferenceItem(
    def: PreferenceDefinition,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.Action,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val prefController = LocalPreferenceController.current
    val navController = LocalNavController.current

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        onClick = withHaptic {
            if (isEnabled) type.onClick(
                context,
                prefController,
                navController
            )
        }
    ) {
        LeadingIcon(def.icon, isEnabled)
        TitleSummaryBlock(def.title, summary, isEnabled)
    }
}