package com.drdisagree.iconify.features.xposed.quicksettings.clock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.materialkolor.ktx.harmonize

private val containerDarkColor = Color(0xFF282C34)
private val toggleTargetSize = 56.dp
private val tileHeight = 72.dp
private val tileStartPadding = 8.dp
private val activeIconCornerRadius = 16.dp
private val activeTileCornerRadius = 24.dp
private val notificationHeight = 152.dp

@Composable
fun FakeQuickSettings(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        FakeQsPanel()
        FakeNotificationPanel()
    }
}

@Composable
private fun FakeQsPanel() {
    val isDarkTheme = LocalDarkMode.current
    val tileBgColor = containerDarkColor.harmonize(
        if (isDarkTheme) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.inverseSurface
    )
    val accentColor = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(tileHeight)
                        .clip(RoundedCornerShape(activeTileCornerRadius))
                        .background(tileBgColor),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Box(
                        modifier = Modifier
                            .padding(start = tileStartPadding)
                            .size(toggleTargetSize)
                            .clip(RoundedCornerShape(activeIconCornerRadius))
                            .background(accentColor)
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(4) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(tileHeight)
                        .clip(RoundedCornerShape(activeTileCornerRadius))
                        .background(tileBgColor)
                )
            }
        }
    }
}

@Composable
private fun FakeNotificationPanel() {
    val isDarkTheme = LocalDarkMode.current
    val notificationBgColor = containerDarkColor.harmonize(
        if (isDarkTheme) MaterialTheme.colorScheme.surface
        else MaterialTheme.colorScheme.inverseSurface
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(notificationHeight)
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        topStart = activeTileCornerRadius,
                        topEnd = activeTileCornerRadius,
                        bottomStart = 4.dp,
                        bottomEnd = 4.dp
                    )
                )
                .background(notificationBgColor)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 4.dp,
                        bottomStart = activeTileCornerRadius,
                        bottomEnd = activeTileCornerRadius
                    )
                )
                .background(notificationBgColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun FakeQuickSettingsPreview() {
    PreviewComposable {
        FakeQuickSettings()
    }
}