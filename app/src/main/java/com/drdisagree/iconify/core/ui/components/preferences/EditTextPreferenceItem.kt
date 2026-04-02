package com.drdisagree.iconify.core.ui.components.preferences

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.ui.components.others.withHaptic

@Composable
fun EditTextPreferenceItem(
    def: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    summary: String?,
    modifier: Modifier,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    val storedValue by prefController.observe(def.key, "")
    val displaySummary = summary ?: storedValue.ifBlank { null }

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
        var draft by remember { mutableStateOf(storedValue) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(def.title.resolve()) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = withHaptic {
                    prefController.setString(def.key, draft)
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = withHaptic { showDialog = false }) { Text("Cancel") }
            }
        )
    }
}