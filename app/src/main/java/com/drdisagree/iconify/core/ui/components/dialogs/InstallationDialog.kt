package com.drdisagree.iconify.core.ui.components.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.others.BlurBehindDialog
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.coloredLogText
import kotlinx.coroutines.flow.collectLatest

@Composable
fun InstallationDialog(
    title: String,
    desc: String,
    logs: List<String>,
    onDismiss: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        BlurBehindDialog()

        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_bg2),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_fg),
                            contentDescription = stringResource(id = R.string.iconify_logo),
                            modifier = Modifier
                                .padding(4.dp)
                                .fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .wrapContentHeight()
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                val logBgColor = MaterialTheme.colorScheme.surface
                val listState = rememberLazyListState()
                val containerHeight = 200.dp
                var contentHeightPx by remember { mutableIntStateOf(0) }
                val density = LocalDensity.current

                LaunchedEffect(Unit) {
                    snapshotFlow { logs.size }
                        .collectLatest {
                            if (it > 0) {
                                try {
                                    listState.scrollToItem(it - 1)
                                } catch (_: Exception) {
                                    // List mutated during scroll animation; safe to ignore
                                }
                            }
                        }
                }

                AnimatedVisibility(logs.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                            .heightIn(max = containerHeight)
                            .clip(RoundedCornerShape(8.dp))
                            .background(logBgColor)
                    ) {
                        LazyColumn(
                            state = listState,
                            userScrollEnabled = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .onGloballyPositioned { coordinates ->
                                    contentHeightPx = coordinates.size.height
                                },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            itemsIndexed(
                                items = logs,
                                key = { index, _ -> index }
                            ) { _, log ->
                                Text(
                                    text = coloredLogText(log),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        if (with(density) { contentHeightPx.toDp() >= containerHeight }) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                logBgColor.copy(alpha = 0.8f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .fillMaxWidth()
                                    .height(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InstallationDialogPreview() {
    PreviewComposable {
        InstallationDialog(
            title = "Installing",
            desc = "Please wait",
            logs = listOf(
                "I: Creating module template",
                "W: Some overlays skipped",
                "E: Failed to flash module",
            )
        )
    }
}