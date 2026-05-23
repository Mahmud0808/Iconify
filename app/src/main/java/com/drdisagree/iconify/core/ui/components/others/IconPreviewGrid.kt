package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.core.ui.utils.rememberXmlPainter

@Composable
fun IconPreviewGrid(
    modifier: Modifier = Modifier,
    isApplied: Boolean,
    icons: List<Int>,
    shouldTint: Boolean = true
) {
    val iconColor = if (shouldTint) {
        if (isApplied) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface
    } else {
        Color.Unspecified
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = rememberXmlPainter(icons[0]),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
            Icon(
                painter = rememberXmlPainter(icons[1]),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                painter = rememberXmlPainter(icons[2]),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
            Icon(
                painter = rememberXmlPainter(icons[3]),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconColor
            )
        }
    }
}