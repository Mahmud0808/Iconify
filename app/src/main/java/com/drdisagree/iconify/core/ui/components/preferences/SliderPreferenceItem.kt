package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.ui.components.others.withHaptic

@Composable
fun SliderPreferenceItem(
    def: PreferenceDefinition,
    controller: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.Slider,
    modifier: Modifier,
) {
    val value by controller.observe(def.key, type.min)
    var previousValue by remember { mutableFloatStateOf(value) }

    val onValueChangeWithHaptic = withHaptic { /* no-op */ }

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 2 else 3,
    ) {
        Column(modifier = modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LeadingIcon(def.icon, isEnabled)
                TitleSummaryBlock(def.title, summary, isEnabled)
                Text(
                    text = type.valueLabel?.invoke(value) ?: value.toInt().toString(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = value,
                onValueChange = { newValue ->
                    if (isEnabled) {
                        if (previousValue != newValue) {
                            onValueChangeWithHaptic()
                            previousValue = newValue
                            controller.setFloat(def.key, newValue)
                        }
                    }
                },
                valueRange = type.min..type.max,
                steps = type.steps,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }
    }
}