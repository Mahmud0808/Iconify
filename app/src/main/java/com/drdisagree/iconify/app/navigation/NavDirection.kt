package com.drdisagree.iconify.app.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavDestination

enum class NavDirection {
    Start,
    End
}

fun resolveDirection(
    from: NavDestination?,
    to: NavDestination?
): NavDirection? {
    val fromIndex = from?.bottomTabIndex()
    val toIndex = to?.bottomTabIndex()

    if (fromIndex == null || toIndex == null || fromIndex == toIndex) return null

    return if (toIndex > fromIndex) {
        NavDirection.End
    } else {
        NavDirection.Start
    }
}

fun slideInFrom(
    layoutDirection: LayoutDirection,
    direction: NavDirection
): EnterTransition {
    val sign = when (direction) {
        NavDirection.Start -> if (layoutDirection == LayoutDirection.Ltr) -1 else 1
        NavDirection.End -> if (layoutDirection == LayoutDirection.Ltr) 1 else -1
    }

    return slideInHorizontally(
        initialOffsetX = { it * sign },
        animationSpec = tween(300)
    ) + fadeIn(tween(300))
}

fun slideOutTo(
    layoutDirection: LayoutDirection,
    direction: NavDirection
): ExitTransition {
    val sign = when (direction) {
        NavDirection.Start -> if (layoutDirection == LayoutDirection.Ltr) -1 else 1
        NavDirection.End -> if (layoutDirection == LayoutDirection.Ltr) 1 else -1
    }

    return slideOutHorizontally(
        targetOffsetX = { -it * sign },
        animationSpec = tween(300)
    ) + fadeOut(tween(300))
}