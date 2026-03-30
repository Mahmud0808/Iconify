package com.drdisagree.iconify.core.ui.components.others

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun RowScope.ColumnScrollIndicator(
    modifier: Modifier = Modifier,
    listState: LazyListState
) {
    val isScrollable by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf false

            val firstItem = visibleItemsInfo.first()
            val lastItem = visibleItemsInfo.last()

            val isAtStart = listState.firstVisibleItemIndex == 0 &&
                    firstItem.offset == layoutInfo.viewportStartOffset
            val isAtEnd = lastItem.index == layoutInfo.totalItemsCount - 1 &&
                    (lastItem.offset + lastItem.size) <= layoutInfo.viewportEndOffset

            !(isAtStart && isAtEnd)
        }
    }

    if (!isScrollable) return

    var trackHeightPx by remember { mutableIntStateOf(0) }

    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems <= 1) return@derivedStateOf 0f

            val firstVisibleItem = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            val itemHeight = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0

            val scrolled = firstVisibleItem * itemHeight + firstVisibleOffset
            val total = totalItems * itemHeight - layoutInfo.viewportEndOffset

            if (total <= 0) 0f else (scrolled.toFloat() / total).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .width(6.dp)
            .fillMaxHeight()
            .align(Alignment.Top)
            .padding(vertical = 12.dp)
            .onGloballyPositioned { trackHeightPx = it.size.height }
    ) {
        val thumbHeightPx = with(LocalDensity.current) { 40.dp.toPx() }
        val offsetY = ((trackHeightPx - thumbHeightPx) * scrollProgress).toInt()

        Box(
            modifier = Modifier
                .width(6.dp)
                .height(40.dp)
                .offset { IntOffset(x = 0, y = offsetY) }
                .background(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
        )
    }
}

@Composable
fun ColumnScope.RowScrollIndicator(
    modifier: Modifier = Modifier,
    listState: LazyListState
) {
    val isScrollable by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf false

            val firstItem = visibleItemsInfo.first()
            val lastItem = visibleItemsInfo.last()

            val isAtStart = listState.firstVisibleItemIndex == 0 &&
                    firstItem.offset == layoutInfo.viewportStartOffset
            val isAtEnd = lastItem.index == layoutInfo.totalItemsCount - 1 &&
                    (lastItem.offset + lastItem.size) <= layoutInfo.viewportEndOffset

            !(isAtStart && isAtEnd)
        }
    }

    if (!isScrollable) return

    var trackWidthPx by remember { mutableIntStateOf(0) }

    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems <= 1) return@derivedStateOf 0f

            val firstVisibleItem = listState.firstVisibleItemIndex
            val firstVisibleOffset = listState.firstVisibleItemScrollOffset
            val itemHeight = layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0

            val scrolled = firstVisibleItem * itemHeight + firstVisibleOffset
            val total = totalItems * itemHeight - layoutInfo.viewportEndOffset

            if (total <= 0) 0f else (scrolled.toFloat() / total).coerceIn(0f, 1f)
        }
    }

    Box(
        modifier = modifier
            .height(6.dp)
            .fillMaxWidth()
            .align(Alignment.Start)
            .padding(horizontal = 12.dp)
            .onGloballyPositioned { trackWidthPx = it.size.width }
    ) {
        val thumbWidthPx = with(LocalDensity.current) { 40.dp.toPx() }
        val offsetX = ((trackWidthPx - thumbWidthPx) * scrollProgress).toInt()

        Box(
            modifier = Modifier
                .height(6.dp)
                .width(40.dp)
                .offset { IntOffset(x = offsetX, y = 0) }
                .background(MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
        )
    }
}