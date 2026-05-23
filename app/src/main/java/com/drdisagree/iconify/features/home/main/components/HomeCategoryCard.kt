package com.drdisagree.iconify.features.home.main.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    val outerShapeCorner by animateDpAsState(
        targetValue = if (isPressed) 40.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "shape"
    )
    val innerShapeCornerLarge by animateDpAsState(
        targetValue = if (isPressed) 23.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "shape"
    )
    val innerShapeCornerSmall by animateDpAsState(
        targetValue = if (isPressed) 17.dp else 12.dp,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "shape"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(if (smallVariant) 84.dp else 190.dp)
            .scale(scale)
            .clip(RoundedCornerShape(outerShapeCorner))
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = withHaptic { onClick() }
            ),
        shape = RoundedCornerShape(outerShapeCorner),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (smallVariant) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AutoResizeableText(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = foregroundColor
                    )
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = foregroundColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(innerShapeCornerSmall)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = foregroundColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            } else {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = null,
                    tint = foregroundColor.copy(alpha = 0.05f),
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 24.dp, y = 24.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = foregroundColor.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(innerShapeCornerLarge)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            tint = foregroundColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        AutoResizeableText(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            ),
                            color = foregroundColor
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = foregroundColor.secondaryText(),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun HomeCategoryGridPreview() {
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