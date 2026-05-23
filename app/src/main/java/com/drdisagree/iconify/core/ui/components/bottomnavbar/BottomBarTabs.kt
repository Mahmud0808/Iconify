package com.drdisagree.iconify.core.ui.components.bottomnavbar

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drdisagree.iconify.app.navigation.NavRoutes
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import kotlin.math.roundToInt

@Composable
fun BottomBarTabs(
    tabs: List<NavRoutes.BottomBarTab>,
    selectedTab: Int,
    isTabEnabled: (NavRoutes.BottomBarTab) -> Boolean,
    onTabSelected: (NavRoutes.BottomBarTab) -> Unit,
    maxScreenFraction: Float = 0.7f
) {
    val containerSize = LocalWindowInfo.current.containerSize
    val screenWidth = containerSize.width
    val maxTotalWidth = screenWidth * maxScreenFraction

    CompositionLocalProvider(
        LocalTextStyle provides LocalTextStyle.current.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        ),
        LocalContentColor provides MaterialTheme.colorScheme.onSurface
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
            SubcomposeLayout { constraints ->
                val naturalPlaceables = tabs.mapIndexed { index, tab ->
                    subcompose("natural_$index") {
                        TabContent(
                            tab = tab,
                            selected = selectedTab == index,
                            enabled = isTabEnabled(tab),
                            onTabSelected = onTabSelected
                        )
                    }.first().measure(constraints)
                }

                val naturalMaxWidth = naturalPlaceables.maxOf { it.width }
                val tabCount = tabs.size
                val maxTotalWidthPx = maxTotalWidth.roundToInt()

                val finalTabWidth = (naturalMaxWidth * tabCount)
                    .coerceAtMost(maxTotalWidthPx) / tabCount

                val fixedConstraints = Constraints.fixedWidth(finalTabWidth)

                val finalPlaceables = tabs.mapIndexed { index, tab ->
                    subcompose("final_$index") {
                        TabContent(
                            tab = tab,
                            selected = selectedTab == index,
                            enabled = isTabEnabled(tab),
                            onTabSelected = onTabSelected
                        )
                    }.first().measure(fixedConstraints)
                }

                val height = naturalPlaceables.maxOf { it.height }

                layout(finalTabWidth * tabCount, height) {
                    var x = 0
                    finalPlaceables.forEach { placeable ->
                        val y = (height - placeable.height) / 2
                        placeable.placeRelative(x, y)
                        x += finalTabWidth
                    }
                }
            }
        }
    }
}

@Composable
private fun TabContent(
    tab: NavRoutes.BottomBarTab,
    selected: Boolean,
    enabled: Boolean,
    onTabSelected: (NavRoutes.BottomBarTab) -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = when {
            !enabled -> 0.25f
            selected -> 1f
            else -> 0.35f
        },
        label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected && enabled) 1f else 0.98f,
        visibilityThreshold = 0.000001f,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioMediumBouncy
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .scale(scale)
            .alpha(alpha)
            .fillMaxHeight()
            .clickable(
                enabled = enabled,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = withHaptic { onTabSelected(tab) }
            )
            .padding(horizontal = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val iconRes = if (selected) tab.iconChecked else tab.iconUnchecked
        Icon(
            painter = painterResource(iconRes),
            contentDescription = stringResource(tab.title),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.4f)
        )
        Text(
            text = stringResource(tab.title),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.4f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.basicMarquee(Int.MAX_VALUE)
        )
    }
}