package com.drdisagree.iconify.core.ui.components.preferences

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.preferences.PrefParam
import com.drdisagree.iconify.core.preferences.PreferenceController
import com.drdisagree.iconify.core.preferences.PreferenceDefinition
import com.drdisagree.iconify.core.preferences.PreferenceType
import com.drdisagree.iconify.core.preferences.resolve
import com.drdisagree.iconify.core.preferences.resolveOrNull
import com.drdisagree.iconify.core.preferences.resolveToStrings
import com.drdisagree.iconify.core.preferences.toValueOrNull
import com.drdisagree.iconify.core.ui.components.others.ColumnScrollIndicator
import com.drdisagree.iconify.core.ui.components.others.withHaptic

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MultiListPreferenceItem(
    prefDefinition: PreferenceDefinition,
    prefController: PreferenceController,
    shape: RoundedCornerShape,
    isEnabled: Boolean,
    type: PreferenceType.MultiList,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val navController = LocalNavController.current

    var showDialog by rememberSaveable { mutableStateOf(false) }
    val selectedValues by prefController.observe(prefDefinition.key, emptySet<String>())

    val param = PrefParam(
        prefDefinition.key,
        prefDefinition.defaultValue.toValueOrNull(),
        selectedValues,
        context,
        activity,
        prefController,
        navController
    )

    val summary = prefDefinition.summary?.invoke(param).resolveOrNull()

    val displaySummary = summary
        ?: selectedValues.mapNotNull { v ->
            type.entries.resolve()
                .getOrNull(type.entryValues.resolveToStrings().indexOf(v))
        }.joinToString(", ").ifBlank { null }

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
        var localSelected by remember { mutableStateOf(selectedValues) }

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(prefDefinition.title.resolve()) },
            text = {
                val listState = rememberLazyListState()
                val entries = type.entries.resolve()

                val showTopDivider by remember {
                    derivedStateOf {
                        listState.firstVisibleItemIndex > 0 ||
                                listState.firstVisibleItemScrollOffset > 0
                    }
                }
                val showBottomDivider by remember {
                    derivedStateOf {
                        val layoutInfo = listState.layoutInfo
                        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                        lastVisibleItem != null &&
                                (lastVisibleItem.index < layoutInfo.totalItemsCount - 1 ||
                                        lastVisibleItem.offset + lastVisibleItem.size >
                                        layoutInfo.viewportEndOffset)
                    }
                }

                Column(modifier = Modifier.fillMaxWidth()) {
                    if (showTopDivider) HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .wrapContentHeight()
                                .weight(1f)
                        ) {
                            itemsIndexed(entries) { i, entry ->
                                val value = type.entryValues.resolveToStrings()[i]

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(
                                            onClick = withHaptic {
                                                localSelected =
                                                    if (value in localSelected)
                                                        localSelected - value
                                                    else localSelected + value
                                            }
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Checkbox(
                                        checked = value in localSelected,
                                        onCheckedChange = {
                                            localSelected =
                                                if (it) localSelected + value
                                                else localSelected - value
                                        }
                                    )
                                    Text(
                                        text = entry.resolve(),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }

                        ColumnScrollIndicator(listState = listState)
                    }

                    if (showBottomDivider) HorizontalDivider()
                }
            },
            confirmButton = {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                        prefController.setStringSet(prefDefinition.key, localSelected)
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