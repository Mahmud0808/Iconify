package com.drdisagree.iconify.core.ui.components.bottomnavbar

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.drdisagree.iconify.app.navigation.BOTTOM_BAR_TABS
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.common.LocalHazeState
import com.drdisagree.iconify.core.common.LocalLayerBackdrop
import com.drdisagree.iconify.core.common.LocalNavController
import com.drdisagree.iconify.core.common.LocalSettings
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.utils.InteractiveHighlight
import com.drdisagree.iconify.core.ui.utils.sharedHiltViewModel
import com.drdisagree.iconify.features.common.viewmodels.BottomNavViewModel
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import dev.chrisbanes.haze.hazeChild
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

@Composable
fun BottomNavigation(bottomNavViewModel: BottomNavViewModel = sharedHiltViewModel()) {
    val settings = LocalSettings.current
    val hazeState = LocalHazeState.current
    val backdrop = LocalLayerBackdrop.current
    val navController = LocalNavController.current
    val selectedTabIndex by bottomNavViewModel::selectedTabIndex
    val blurEffect = settings.blurEffect
    val floatingBottomBar = settings.floatingBottomBar
    val defaultTabIndex = if (settings.isXposedOnlyMode) 0 else bottomNavViewModel.defaultTabIndex

    if (!floatingBottomBar) {
        NavigationBar(
            modifier = Modifier.then(
                if (blurEffect) {
                    Modifier.hazeChild(
                        state = hazeState,
                        shape = RectangleShape
                    )
                } else {
                    Modifier
                }
            ),
            containerColor = if (blurEffect) Color.Transparent
            else NavigationBarDefaults.containerColor,
        ) {
            BOTTOM_BAR_TABS.forEachIndexed { index, tab ->
                val isEnabled = !settings.isXposedOnlyMode || tab.route != NavRoutes.MainGraph.Home.Tab.route

                NavigationBarItem(
                    selected = selectedTabIndex == index,
                    enabled = isEnabled,
                    onClick = {
                        if (index != selectedTabIndex) {
                            val isDefaultTab = index == defaultTabIndex

                            navController.navigate(tab.route) {
                                popUpTo(BOTTOM_BAR_TABS[defaultTabIndex]) {
                                    inclusive = isDefaultTab
                                }
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = {
                        val iconRes = if (selectedTabIndex == index) {
                            tab.iconChecked
                        } else {
                            tab.iconUnchecked
                        }
                        Icon(
                            painter = painterResource(iconRes),
                            contentDescription = stringResource(tab.title)
                        )
                    },
                    label = {
                        Text(text = stringResource(tab.title))
                    }
                )
            }
        }
        return
    }

    val backdropColor = MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = if (blurEffect) .2f else .92f
    )

    val animationScope = rememberCoroutineScope()
    val highlightColor = MaterialTheme.colorScheme.primary.copy(
        alpha = if (blurEffect) 0.75f else 0.9f
    )
    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(
            animationScope = animationScope,
            color = highlightColor
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { CircleShape },
                    effects = {
                        vibrancy()
                        if (blurEffect) {
                            blur(6.dp.toPx())
                            lens(24.dp.toPx(), 32.dp.toPx())
                        }
                    },
                    layerBlock = {
                        val width = size.width
                        val height = size.height

                        val progress = interactiveHighlight.pressProgress
                        val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

                        val maxOffset = size.minDimension
                        val initialDerivative = 0.05f
                        val offset = interactiveHighlight.offset
                        translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                        translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                        val maxDragScale = 4f.dp.toPx() / size.height
                        val offsetAngle = atan2(offset.y, offset.x)
                        scaleX = scale +
                                maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) *
                                (width / height).fastCoerceAtMost(1f)
                        scaleY = scale +
                                maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) *
                                (height / width).fastCoerceAtMost(1f)
                    },
                    onDrawSurface = { drawRect(backdropColor) }
                )
                .clickable(interactionSource = null, indication = null) {}
                .then(interactiveHighlight.modifier)
                .then(interactiveHighlight.gestureModifier)
                .wrapContentWidth()
                .height(64.dp)
        ) {
            BottomBarTabs(
                tabs = BOTTOM_BAR_TABS,
                selectedTab = selectedTabIndex,
                isTabEnabled = { tab ->
                    !settings.isXposedOnlyMode || tab.route != NavRoutes.MainGraph.Home.Tab.route
                },
                onTabSelected = { tab ->
                    val newIndex = BOTTOM_BAR_TABS.indexOf(tab)

                    if (newIndex != selectedTabIndex) {
                        val isDefaultTab = newIndex == defaultTabIndex

                        navController.navigate(tab.route) {
                            popUpTo(BOTTOM_BAR_TABS[defaultTabIndex]) {
                                inclusive = isDefaultTab
                            }
                            launchSingleTop = true
                        }
                    }
                }
            )

            val animatedSelectedTabIndex by animateFloatAsState(
                targetValue = selectedTabIndex.toFloat(), label = "animatedSelectedTabIndex",
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                    dampingRatio = Spring.DampingRatioLowBouncy,
                )
            )

            val animatedColor by animateColorAsState(
                targetValue = MaterialTheme.colorScheme.primary,
                label = "animatedColor",
                animationSpec = spring(
                    stiffness = Spring.StiffnessLow,
                )
            )

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .blur(50.dp, edgeTreatment = BlurredEdgeTreatment.Unbounded)
            ) {
                val tabWidth = size.width / BOTTOM_BAR_TABS.size
                drawCircle(
                    color = animatedColor.copy(alpha = .6f),
                    radius = size.height / 2,
                    center = Offset(
                        (tabWidth * animatedSelectedTabIndex) + tabWidth / 2,
                        size.height / 2
                    )
                )
            }

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            ) {
                val path = Path().apply {
                    addRoundRect(RoundRect(size.toRect(), CornerRadius(size.height)))
                }
                val length = PathMeasure().apply { setPath(path, false) }.length
                val tabWidth = size.width / BOTTOM_BAR_TABS.size

                drawPath(
                    path,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            animatedColor.copy(alpha = 0f),
                            animatedColor.copy(alpha = 1f),
                            animatedColor.copy(alpha = 1f),
                            animatedColor.copy(alpha = 0f),
                        ),
                        startX = tabWidth * animatedSelectedTabIndex,
                        endX = tabWidth * (animatedSelectedTabIndex + 1),
                    ),
                    style = Stroke(
                        width = 6f,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(length / 2, length)
                        )
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavigationPreview() {
    PreviewComposable {
        BottomNavigation(viewModel())
    }
}