package com.drdisagree.iconify.features.home.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.texts.AutoResizeableText

@Composable
fun HomeCategoryCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    icon: Int,
    backgroundColor: Color,
    foregroundColor: Color,
    smallVariant: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (smallVariant) Modifier.wrapContentHeight()
                else Modifier.height(160.dp)
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable(onClick = withHaptic { onClick() }),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier.then(
                if (smallVariant) Modifier.wrapContentHeight()
                else Modifier.fillMaxSize()
            )
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = foregroundColor.copy(alpha = 0.45f),
                modifier = Modifier
                    .padding(8.dp)
                    .then(
                        if (smallVariant) {
                            Modifier
                                .size(36.dp)
                                .align(Alignment.CenterEnd)
                        } else {
                            Modifier
                                .size(68.dp)
                                .align(Alignment.BottomEnd)
                        }
                    )
            )

            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Top
            ) {
                AutoResizeableText(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = foregroundColor
                )
                if (!smallVariant && subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = foregroundColor.secondaryText()
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun HomeCategoryGridPreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeCategoryCard(
            title = "Icon Pack",
            subtitle = "Change system icon pack",
            icon = R.drawable.ic_styles_iconpack,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            foregroundColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.weight(1f),
            onClick = {}
        )
        HomeCategoryCard(
            title = "More",
            smallVariant = true,
            icon = R.drawable.ic_arrow_end_long,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            foregroundColor = MaterialTheme.colorScheme.onTertiaryContainer,
            modifier = Modifier.weight(1f),
            onClick = {}
        )
    }
}