package com.drdisagree.iconify.features.home.main.components

import android.graphics.BlurMaskFilter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativePaint
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.secondaryText
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.texts.AutoResizeableText

private fun Modifier.categoryShadow(
    color: Color,
    borderRadius: Dp = 16.dp,
    blurRadius: Dp = 8.dp,
    offsetY: Dp = 6.dp
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val frameworkPaint = paint.nativePaint

        frameworkPaint.maskFilter = BlurMaskFilter(
            blurRadius.toPx(),
            BlurMaskFilter.Blur.NORMAL
        )

        paint.color = color.copy(alpha = 0.75f)

        canvas.drawRoundRect(
            left = blurRadius.toPx(),
            top = size.height - (blurRadius.toPx() / 2) + offsetY.toPx(),
            right = size.width - blurRadius.toPx(),
            bottom = size.height + blurRadius.toPx() + offsetY.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}

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
                else Modifier.aspectRatio(1.1f)
            )
            .clip(MaterialTheme.shapes.large)
            .clickable(onClick = withHaptic { onClick() }),
        shape = MaterialTheme.shapes.large,
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
                tint = foregroundColor.copy(alpha = 0.35f),
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