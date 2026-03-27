package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.preferences.resolveOrNull
import com.drdisagree.iconify.core.preferences.resolveToStrings
import com.drdisagree.iconify.core.ui.components.others.withHaptic

@Composable
fun ListPreferenceItem(
    def: PreferenceDefinition,
    controller: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    type: PreferenceType.ListPref,
    modifier: Modifier,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val selectedValue by controller.observe(def.key, "")
    val displaySummary = summary
        ?: type.entries.resolve()
            .getOrNull(type.entryValues.resolveToStrings().indexOf(selectedValue)).resolveOrNull()

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        onClick = withHaptic { if (isEnabled) showDialog = true }
    ) {
        LeadingIcon(def.icon, isEnabled)
        TitleSummaryBlock(def.title, displaySummary, isEnabled)
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(def.title.resolve()) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    type.entries.resolve().forEachIndexed { i, entry ->
                        val value = type.entryValues.resolveToStrings()[i]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(
                                    onClick = withHaptic {
                                        controller.setString(def.key, value)
                                        showDialog = false
                                    }
                                )
                                .padding(horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = selectedValue == value,
                                onClick = withHaptic {
                                    controller.setString(def.key, value)
                                    showDialog = false
                                }
                            )
                            Text(entry.resolve(), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = withHaptic { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}