package com.drdisagree.iconify.features.xposed.main.components

import android.content.ComponentName
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE
import com.drdisagree.iconify.data.common.Preferences
import com.drdisagree.iconify.features.xposed.main.viewmodels.HookCheckViewModel
import kotlinx.coroutines.delay

@Composable
fun HookCheckCard(
    modifier: Modifier = Modifier,
    viewModel: HookCheckViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val previewMode = LocalInspectionMode.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showDialog by rememberSaveable { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(text = stringResource(R.string.attention))
            },
            text = {
                val message = if (!uiState.hasBootlooped) {
                    buildString {
                        if (Preferences.isXposedOnlyMode) {
                            append(stringResource(R.string.xposed_only_desc))
                            append("\n\n")
                        }
                        append(stringResource(R.string.lsposed_warn))
                    }
                } else {
                    stringResource(R.string.lsposed_bootloop_warn)
                }
                Text(text = message)
            },
            confirmButton = {
                TextButton(onClick = withHaptic { showDialog = false }) {
                    Text(stringResource(R.string.understood))
                }
            }
        )
    }

    var showCard by rememberSaveable { mutableStateOf(false) }
    var hasChanged by rememberSaveable { mutableStateOf(false) }

    val targetVisible by remember(uiState.isHooked, uiState.hasBootlooped) {
        derivedStateOf { !uiState.isHooked || uiState.hasBootlooped }
    }

    LaunchedEffect(targetVisible) {
        delay(800)
        showCard = targetVisible
    }

    LaunchedEffect(showCard) {
        if (showCard != targetVisible) {
            hasChanged = true
        }
    }

    if (!previewMode) {
        val lifecycleOwner = LocalLifecycleOwner.current

        DisposableEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    viewModel.initializeHookCheck()
                }
            }

            lifecycleOwner.lifecycle.addObserver(observer)

            onDispose {
                lifecycleOwner.lifecycle.removeObserver(observer)
            }
        }
    }

    AnimatedVisibility(
        visible = showCard,
        enter = if (hasChanged) fadeIn() + expandVertically() else EnterTransition.None,
        exit = if (hasChanged) fadeOut() + shrinkVertically() else ExitTransition.None,
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val containerColor = MaterialTheme.colorScheme.error
        val shape = RoundedCornerShape(CARD_CORNER_LARGE)

        Row(
            modifier = modifier
                .fillMaxWidth()
                .clip(shape)
                .background(containerColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    onClick = withHaptic {
                        // Launch LSPosed Manager
                        try {
                            context.startActivity(
                                Intent(Intent.ACTION_MAIN).apply {
                                    component = ComponentName(
                                        "org.lsposed.manager",
                                        "org.lsposed.manager.ui.activity.MainActivity"
                                    )
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                        } catch (_: Exception) {
                        }
                    }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_xposed_disabled),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(24.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(
                        if (uiState.hasBootlooped) R.string.xposed_module_bootlooped_title
                        else R.string.xposed_module_disabled_title
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onError,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(
                        if (uiState.hasBootlooped) R.string.xposed_module_bootlooped_desc
                        else R.string.xposed_module_disabled_desc
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onError.secondaryText(),
                )
            }
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.defaultMinSize(minWidth = 1.dp, minHeight = 1.dp),
                onClick = withHaptic { showDialog = true }
            ) {
                Text(text = stringResource(R.string.more))
            }
        }
    }
}