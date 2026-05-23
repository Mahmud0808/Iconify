package com.drdisagree.iconify.core.ui.components.preferences

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.preferences.PrefParam
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.preferences.resolveOrNull
import com.drdisagree.iconify.core.preferences.toValueOrNull
import com.drdisagree.iconify.core.ui.components.others.withHaptic

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun EditTextPreferenceItem(
    prefDefinition: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val navController = LocalNavController.current

    var showDialog by rememberSaveable { mutableStateOf(false) }
    val storedValue by prefController.observe(prefDefinition.key, "")

    val param = PrefParam(
        prefDefinition.key,
        prefDefinition.defaultValue.toValueOrNull(),
        storedValue,
        context,
        activity,
        prefController,
        navController
    )

    val summary = prefDefinition.summary?.invoke(param).resolveOrNull()

    val displaySummary = summary ?: storedValue.ifBlank { null }

    PreferenceContainer(
        shape = shape,
        isEnabled = isEnabled,
        modifier = modifier,
        minLine = if (summary.isNullOrEmpty()) 1 else 2,
        onClick = withHaptic { if (isEnabled) showDialog = true }
    ) {
        LeadingIcon(prefDefinition.icon, isEnabled)
        TitleSummaryBlock(prefDefinition.title, displaySummary, isEnabled)
    }

    if (showDialog) {
        var draft by remember { mutableStateOf(storedValue) }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(prefDefinition.title.resolve()) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                        prefController.setString(prefDefinition.key, draft)
                        showDialog = false
                    }
                ) { Text(stringResource(R.string.btn_select)) }
            },
            dismissButton = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic { showDialog = false }
                ) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }
}