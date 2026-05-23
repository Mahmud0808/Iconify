package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun innerPaddingValues(
    innerPadding: PaddingValues,
    top: Dp = 0.dp,
    bottom: Dp = 0.dp,
    start: Dp = 0.dp,
    end: Dp = 0.dp
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        top = innerPadding.calculateTopPadding() + top,
        bottom = innerPadding.calculateBottomPadding() +
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                bottom,
        start = innerPadding.calculateStartPadding(layoutDirection) + start,
        end = innerPadding.calculateEndPadding(layoutDirection) + end
    )
}

@Composable
fun innerPaddingValues(
    innerPadding: PaddingValues,
    horizontal: Dp = 0.dp,
    vertical: Dp = 0.dp,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        top = innerPadding.calculateTopPadding() + vertical,
        bottom = innerPadding.calculateBottomPadding() +
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() +
                vertical,
        start = innerPadding.calculateStartPadding(layoutDirection) + horizontal,
        end = innerPadding.calculateEndPadding(layoutDirection) + horizontal
    )
}