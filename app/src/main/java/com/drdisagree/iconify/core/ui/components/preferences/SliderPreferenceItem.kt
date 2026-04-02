package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.PrefValue
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.helpers.replaceAll

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SliderPreferenceItem(
    def: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.Slider,
    modifier: Modifier,
) {
    val defaultValue = (def.defaultValue as PrefValue.FloatValue).v
    val persistedValue by prefController.observe(def.key, defaultValue)
    var sliderValue by remember { mutableFloatStateOf(persistedValue) }
    var previousLabel by remember { mutableStateOf<String?>(null) }
    val originalValueLabel = type.valueLabel?.invoke(sliderValue) ?: sliderValue.toInt().toString()
    val valueLabel = if (type.showDefaultIndicator && sliderValue == defaultValue) {
        if (type.hideDefaultValue) {
            stringResource(R.string.opt_default).replaceAll("(" to "", ")" to "")
        } else {
            "%s %s".format(originalValueLabel, stringResource(R.string.opt_default))
        }
    } else {
        originalValueLabel
    }

    val onValueChangeWithHaptic = withHaptic { /* no-op */ }

    fun updateUiValue(newValue: Float) {
        if (!isEnabled || sliderValue == newValue) return

        val newLabel = type.valueLabel?.invoke(newValue)
            ?: newValue.toInt().toString()

        if (newLabel != previousLabel) {
            onValueChangeWithHaptic()
            previousLabel = newLabel
        }

        sliderValue = newValue
    }

    fun persistValue(value: Float) {
        prefController.setFloat(def.key, value)
    }

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 2 else 3,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LeadingIcon(def.icon, isEnabled)
                TitleSummaryBlock(def.title, summary, isEnabled)
                Text(
                    text = valueLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Slider(
                    value = sliderValue,
                    onValueChange = { newValue ->
                        updateUiValue(newValue)

                        if (!type.applyOnValueChangeFinished) {
                            persistValue(newValue)
                        }
                    },
                    onValueChangeFinished = {
                        if (type.applyOnValueChangeFinished) {
                            persistValue(sliderValue)
                        }
                    },
                    valueRange = type.min..type.max,
                    steps = type.steps,
                    enabled = isEnabled,
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1f)
                        .padding(top = 4.dp)
                )
                if (type.showResetButton) {
                    IconButton(
                        enabled = isEnabled && sliderValue != defaultValue,
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledIconButtonColors(),
                        onClick = {
                            updateUiValue(defaultValue)
                            persistValue(defaultValue)
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_reset),
                            contentDescription = stringResource(R.string.btn_reset),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}