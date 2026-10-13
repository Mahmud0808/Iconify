package com.drdisagree.iconify.features.onboarding.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.ui.components.extensions.bounceClick
import com.drdisagree.iconify.core.ui.components.others.DecorativeShape
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.texts.AutoResizeableText
import com.drdisagree.iconify.features.onboarding.states.SetupMode
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PageFour(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    selectedMode: SetupMode,
    onModeSelected: (SetupMode) -> Unit,
) {
    val scale = remember { Animatable(0f) }
    val scaleMainShape = remember { Animatable(0.75f) }
    val semiCircleShape = MaterialShapes.SemiCircle.toShape()

    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage == 3) {
            launch {
                scale.animateTo(
                    1f,
                    tween(1500, easing = FastOutSlowInEasing)
                )
            }
            launch {
                scaleMainShape.animateTo(
                    1f,
                    tween(1500, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val scrollState = rememberScrollState()
    val fadeAlpha by animateFloatAsState(
        targetValue = if (scrollState.canScrollForward) 1f else 0f,
        label = "bottomFadeAlpha"
    )

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        DecorativeShape(
            size = 250,
            shape = MaterialShapes.Cookie9Sided.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f),
            scale = scale.value,
            modifier = Modifier.align(Alignment.Center)
        )

        DecorativeShape(
            size = 70,
            shape = MaterialShapes.Sunny.toShape(),
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(y = (-110).dp, x = (-20).dp)
        )

        DecorativeShape(
            size = 90,
            shape = MaterialShapes.Pill.toShape(),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = (-170).dp, x = 10.dp)
        )

        DecorativeShape(
            size = 110,
            shape = MaterialShapes.Arch.toShape(),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
            scale = scale.value,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-10).dp, x = 30.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 80.dp)
                .background(Color.Transparent)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 65.dp, start = 20.dp, end = 20.dp)
                    .align(Alignment.CenterHorizontally)
                    .graphicsLayer {
                        scaleX = scaleMainShape.value
                        scaleY = scaleMainShape.value
                        shape = semiCircleShape
                        clip = true
                    }
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                AutoResizeableText(
                    text = stringResource(R.string.setup_mode_title),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(
                        top = 55.dp,
                        bottom = 30.dp,
                        start = 20.dp,
                        end = 20.dp
                    )
                )
            }

            Text(
                text = stringResource(R.string.setup_mode_desc),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SetupModeCard(
                    icon = Iconsax.Outline.Magicpen,
                    title = stringResource(R.string.setup_mode_full_title),
                    description = stringResource(R.string.setup_mode_full_desc),
                    badge = stringResource(R.string.setup_mode_recommended),
                    isSelected = selectedMode == SetupMode.FULL,
                    onClick = { onModeSelected(SetupMode.FULL) }
                )

                SetupModeCard(
                    icon = Iconsax.Outline.Cpu,
                    title = stringResource(R.string.setup_mode_xposed_title),
                    description = stringResource(R.string.setup_mode_xposed_desc),
                    badge = stringResource(R.string.setup_mode_lightweight),
                    isSelected = selectedMode == SetupMode.XPOSED_ONLY,
                    onClick = { onModeSelected(SetupMode.XPOSED_ONLY) }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 80.dp)
                .height(40.dp)
                .align(Alignment.BottomCenter)
                .graphicsLayer { alpha = fadeAlpha }
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SetupModeCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    description: String,
    badge: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceContainer,
        label = "setupModeContainer"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
        else MaterialTheme.colorScheme.onSurface,
        label = "setupModeContent"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outlineVariant,
        label = "setupModeBorder"
    )

    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .bounceClick(pressedScale = 0.97f)
            .clip(MaterialTheme.shapes.large)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = withHaptic { onClick() }
            ),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.outlinedCardColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (badge != null) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized
                )

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.alpha(0.9f)
                )
            }

            Icon(
                imageVector = if (isSelected) Iconsax.Bold.TickCircle else Iconsax.Outline.TickCircle,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PageFourPreview() {
    val pagerState = rememberPagerState(pageCount = { 4 })
    var selectedMode by rememberSaveable { mutableStateOf(SetupMode.FULL) }

    PageFour(
        pagerState = pagerState,
        selectedMode = selectedMode,
        onModeSelected = { selectedMode = it }
    )
}
