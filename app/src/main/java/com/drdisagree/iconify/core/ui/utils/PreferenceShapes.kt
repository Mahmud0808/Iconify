package com.drdisagree.iconify.core.ui.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val CARD_CORNER_LARGE: Dp = 24.dp
val CARD_CORNER_SMALL: Dp = 4.dp
val CARD_ITEM_SPACING: Dp = 2.dp

enum class ItemPosition { SOLO, FIRST, MIDDLE, LAST }

data class CardCorners(
    val topStart: Dp,
    val topEnd: Dp,
    val bottomStart: Dp,
    val bottomEnd: Dp,
)

fun cardCorners(position: ItemPosition): CardCorners = when (position) {
    ItemPosition.SOLO -> CardCorners(
        CARD_CORNER_LARGE, CARD_CORNER_LARGE,
        CARD_CORNER_LARGE, CARD_CORNER_LARGE
    )

    ItemPosition.FIRST -> CardCorners(
        CARD_CORNER_LARGE, CARD_CORNER_LARGE,
        CARD_CORNER_SMALL, CARD_CORNER_SMALL
    )

    ItemPosition.MIDDLE -> CardCorners(
        CARD_CORNER_SMALL, CARD_CORNER_SMALL,
        CARD_CORNER_SMALL, CARD_CORNER_SMALL
    )

    ItemPosition.LAST -> CardCorners(
        CARD_CORNER_SMALL, CARD_CORNER_SMALL,
        CARD_CORNER_LARGE, CARD_CORNER_LARGE
    )
}

fun resolvePosition(visibleIndices: List<Int>, index: Int): ItemPosition {
    if (visibleIndices.size <= 1) return ItemPosition.SOLO

    return when (index) {
        visibleIndices.first() -> ItemPosition.FIRST
        visibleIndices.last() -> ItemPosition.LAST
        else -> ItemPosition.MIDDLE
    }
}