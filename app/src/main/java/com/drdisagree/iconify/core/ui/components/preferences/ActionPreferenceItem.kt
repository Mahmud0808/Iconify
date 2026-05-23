package com.drdisagree.iconify.core.ui.components.preferences

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.preferences.PrefParam
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.preferences.resolveOrNull
import com.drdisagree.iconify.core.ui.components.others.withHaptic

@Composable
fun ActionPreferenceItem(
    prefDefinition: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    type: PreferenceType.Action,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val navController = LocalNavController.current

    val param = PrefParam<String?>(
        prefDefinition.key,
        null,
        null,
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
        onClick = withHaptic { if (isEnabled) type.onClick(param) }
    ) {
        LeadingIcon(prefDefinition.icon, isEnabled)
        TitleSummaryBlock(prefDefinition.title, summary, isEnabled)
    }
}