package com.drdisagree.iconify.core.ui.components.pagers

import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.asynclayoutinflater.view.AsyncLayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.SubdirectoryArrowRight
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalDarkMode
import com.drdisagree.iconify.core.ui.components.others.AutoScalingDevicePreview
import com.drdisagree.iconify.core.ui.components.others.PreviewComposable
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.materialkolor.ktx.harmonize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DevicePreviewPager(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    startPageIndex: Int = 0,
    @LayoutRes layoutResIds: List<Int>,
    names: List<String> = emptyList(),
    wallpaperReady: Boolean = true,
    wallpaperBytes: ByteArray? = null,
    sidePageScale: Float = 0.82f,
    horizontalPaddingToIgnore: Dp = 0.dp,
    androidViewHeight: Int = ViewGroup.LayoutParams.MATCH_PARENT,
    androidViewGravity: Int = Gravity.CENTER_HORIZONTAL,
    paddingTopPx: Int = 0,
    paddingHorizontalPx: Int = 0,
    showPunchHole: Boolean = true,
    onPageChanged: (index: Int) -> Unit = {},
    onSelect: (index: Int) -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val isDarkTheme = LocalDarkMode.current
    val configuration = LocalConfiguration.current
    val containerSize = LocalWindowInfo.current.containerSize

    val scope = rememberCoroutineScope()
    val deviceAspectRatio = rememberDeviceAspectRatio()

    val viewCache = remember(layoutResIds) { mutableStateMapOf<Int, View>() }
    val isReady by remember(layoutResIds, viewCache, wallpaperReady) {
        derivedStateOf {
            layoutResIds.isNotEmpty() &&
                    layoutResIds.all { viewCache.containsKey(it) } &&
                    wallpaperReady
        }
    }

    val count = layoutResIds.size
    val isLoaded = count > 0 && names.size == count

    val resolvedNames = List(count) { i -> names.getOrNull(i) ?: "${i + 1}" }

    val virtualCount = if (isLoaded) Int.MAX_VALUE else 1
    var currentIndex by rememberSaveable { mutableIntStateOf(startPageIndex) }
    val initialVirtualPage = remember(count, currentIndex) {
        if (count == 0) 0
        else (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % count) + currentIndex
    }

    val pagerState = rememberPagerState(
        initialPage = initialVirtualPage,
        pageCount = { virtualCount },
    )

    var isRestored by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.settledPage) {
        if (!isRestored) return@LaunchedEffect

        currentIndex = pagerState.settledPage.realIndex(count)
        onPageChanged(currentIndex)
    }

    LaunchedEffect(layoutResIds) {
        if (layoutResIds.isEmpty()) return@LaunchedEffect

        val pending = layoutResIds.filter { !viewCache.containsKey(it) }
        if (pending.isEmpty()) return@LaunchedEffect

        val inflated = withContext(Dispatchers.Default) {
            pending.associateWith { resId ->
                suspendCancellableCoroutine { cont ->
                    Handler(Looper.getMainLooper()).post {
                        AsyncLayoutInflater(context).inflate(resId, null) { view, _, _ ->
                            view.layoutParams = FrameLayout.LayoutParams(
                                FrameLayout.LayoutParams.MATCH_PARENT,
                                FrameLayout.LayoutParams.WRAP_CONTENT
                            ).apply { gravity = Gravity.CENTER_HORIZONTAL }
                            cont.resume(view)
                        }
                    }
                }
            }
        }

        viewCache.putAll(inflated)
    }

    LaunchedEffect(isReady, count) {
        if (!isReady || count == 0) return@LaunchedEffect

        val target = (Int.MAX_VALUE / 2) - ((Int.MAX_VALUE / 2) % count) + currentIndex
        if (currentIndex != target) {
            pagerState.scrollToPage(target)
            isRestored = true
        }
    }

    val deviceWidthPx = when (configuration.orientation) {
        Configuration.ORIENTATION_PORTRAIT -> containerSize.width
        else -> containerSize.height
    }
    val deviceWidthDp = with(density) { deviceWidthPx.toDp() }

    val flingBehavior = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            val availableWidth = maxWidth + horizontalPaddingToIgnore * 2

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .requiredWidth(availableWidth)
                    .wrapContentHeight(),
            ) {
                val noRotationAvailableWidth = when (configuration.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> availableWidth
                    else -> deviceWidthDp
                }

                val noRotationFrameWidth = noRotationAvailableWidth * 0.52f
                val noRotationFrameHeight = noRotationFrameWidth / deviceAspectRatio

                val frameWidth = availableWidth * 0.52f
                val frameHeight = frameWidth / deviceAspectRatio

                val bezelH = 8.dp
                val bezelV = 8.dp

                val screenAreaW = noRotationFrameWidth - bezelH * 2
                val screenAreaH = noRotationFrameHeight - bezelV * 2

                val pillW = noRotationFrameWidth * sidePageScale
                val pillH = noRotationFrameHeight * sidePageScale

                val pillOffsetX = pillW * 0.75f  // ~25% of pill visible at screen edge
                val pillVisibleWidth = pillW - pillOffsetX
                val tapThresholdPx = with(density) { pillVisibleWidth.toPx() }

                val pageSpacing =
                    ((availableWidth - noRotationFrameWidth) - (pillW - pillOffsetX) * 2) / 2

                // to center the active page
                val contentPad = (availableWidth - screenAreaW) / 2

                // Shared fraction for animation
                val fraction by remember { derivedStateOf { pagerState.currentPageOffsetFraction } }

                // Direction: are we moving forward or backward?
                var swipeDirection by remember { mutableIntStateOf(0) }

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPageOffsetFraction }
                        .filter { it != 0f }
                        .take(1) // only needed for the very first swipe seed
                        .collect { f ->
                            if (swipeDirection == 0) {
                                swipeDirection = if (f > 0f) 1 else -1
                            }
                        }
                }

                val leftPillAlpha = remember { Animatable(1f) }
                val rightPillAlpha = remember { Animatable(1f) }

                val leftPillSlide by remember {
                    derivedStateOf {
                        if (swipeDirection < 0 && fraction < 0f && fraction > -0.5f)
                            (-fraction * pillOffsetX.value * 0.5f)
                        else 0f
                    }
                }
                val rightPillSlide by remember {
                    derivedStateOf {
                        if (swipeDirection > 0 && fraction > 0f && fraction < 0.5f)
                            (-fraction * pillOffsetX.value * 0.5f)
                        else 0f
                    }
                }

                LaunchedEffect(pagerState) {
                    snapshotFlow {
                        Triple(
                            pagerState.currentPageOffsetFraction,
                            pagerState.isScrollInProgress,
                            swipeDirection
                        )
                    }.collect { (f, isScrolling, dir) ->
                        val absF = f.absoluteValue

                        val targetLeftAlpha = if (dir < 0 && f < 0f && f > -0.5f)
                            (1f - absF * 2f).coerceIn(0f, 1f)
                        else 1f

                        val targetRightAlpha = if (dir > 0 && f > 0f && f < 0.5f)
                            (1f - absF * 2f).coerceIn(0f, 1f)
                        else 1f

                        if (targetLeftAlpha < 1f) {
                            leftPillAlpha.snapTo(targetLeftAlpha)
                        } else if (!isScrolling) {
                            // Only animate back when finger is lifted, not mid-reverse
                            leftPillAlpha.animateTo(1f, animationSpec = tween(150))
                        } else {
                            // Mid-swipe reverse: snap immediately to avoid flicker
                            leftPillAlpha.snapTo(targetLeftAlpha)
                        }

                        if (targetRightAlpha < 1f) {
                            rightPillAlpha.snapTo(targetRightAlpha)
                        } else if (!isScrolling) {
                            rightPillAlpha.animateTo(1f, animationSpec = tween(150))
                        } else {
                            rightPillAlpha.snapTo(targetRightAlpha)
                        }
                    }
                }

                @Composable
                fun ScaledContent() {
                    AutoScalingDevicePreview(
                        modifier = Modifier
                            .width(screenAreaW)
                            .height(screenAreaH)
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(
                                    0.5f,
                                    0.5f
                                )
                            }
                    ) {
                        content()
                    }
                }

                val pillCorner = 20.dp
                val darkBgColor = if (isDarkTheme) MaterialTheme.colorScheme.surface
                else MaterialTheme.colorScheme.inverseSurface
                val pillColor = remember(MaterialTheme.colorScheme) {
                    Color(0xFF1C1C1E).harmonize(darkBgColor)
                }

                // Left pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .offset {
                            IntOffset(
                                x = (-pillOffsetX + leftPillSlide.dp).roundToPx(),
                                y = 0
                            )
                        }
                        .width(pillW)
                        .height(pillH)
                        .zIndex(0f)
                        .graphicsLayer { alpha = leftPillAlpha.value }
                        .clip(RoundedCornerShape(pillCorner))
                        .background(pillColor),
                )

                // Right pill
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset {
                            IntOffset(
                                x = (pillOffsetX + rightPillSlide.dp).roundToPx(),
                                y = 0
                            )
                        }
                        .width(pillW)
                        .height(pillH)
                        .zIndex(0f)
                        .graphicsLayer { alpha = rightPillAlpha.value }
                        .clip(RoundedCornerShape(pillCorner))
                        .background(pillColor),
                )

                // Phone frame
                PhoneFrame(
                    isReady = isReady,
                    frameWidth = noRotationFrameWidth,
                    frameHeight = noRotationFrameHeight,
                    bezelHorizontal = bezelH,
                    bezelVertical = bezelV,
                    wallpaperBytes = wallpaperBytes,
                    showPunchHole = showPunchHole,
                    modifier = Modifier.zIndex(1f),
                    content = { ScaledContent() }
                )

                val hapticFeedback = withHaptic { /* no-op */ }

                // Pager
                HorizontalPager(
                    userScrollEnabled = isLoaded && isReady,
                    state = pagerState,
                    flingBehavior = flingBehavior,
                    contentPadding = PaddingValues(horizontal = contentPad),
                    pageSpacing = pageSpacing,
                    verticalAlignment = Alignment.CenterVertically,
                    beyondViewportPageCount = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(noRotationFrameHeight)
                        .zIndex(2f)
                        .pointerInput(isLoaded, isReady, count) {
                            if (!isLoaded || !isReady) return@pointerInput

                            coroutineScope {
                                launch {
                                    detectTapGestures { offset ->
                                        val width = size.width

                                        scope.launch {
                                            when {
                                                offset.x < tapThresholdPx -> {
                                                    hapticFeedback()
                                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                                }

                                                offset.x > width - tapThresholdPx -> {
                                                    hapticFeedback()
                                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                                }
                                            }
                                        }
                                    }
                                }

                                launch {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        swipeDirection = 0

                                        var firstDragSeen = false
                                        while (true) {
                                            val event =
                                                awaitPointerEvent(pass = PointerEventPass.Initial)
                                            val drag = event.changes.firstOrNull() ?: break

                                            if (!drag.pressed) {
                                                swipeDirection = 0
                                                break
                                            }

                                            if (!firstDragSeen) {
                                                val dx = drag.position.x - drag.previousPosition.x
                                                if (dx != 0f) {
                                                    firstDragSeen = true
                                                    swipeDirection = if (dx < 0f) 1 else -1
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                ) { virtualPage ->
                    val scale by remember {
                        derivedStateOf {
                            pageScale(pagerState, virtualPage, sidePageScale)
                        }
                    }

                    if (isLoaded) {
                        AutoScalingDevicePreview(
                            modifier = Modifier
                                .width(screenAreaW)
                                .height(screenAreaH)
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                }
                                .clip(RoundedCornerShape(16.dp)),
                        ) {
                            val resId = layoutResIds[virtualPage.realIndex(count)]

                            key(resId) {
                                AndroidView(
                                    factory = { ctx ->
                                        val cachedView = viewCache[resId]
                                        val d = ctx.resources.displayMetrics.density
                                        val paddingTopPx = (paddingTopPx * d).toInt()
                                        val paddingHorizontalPx = (paddingHorizontalPx * d).toInt()

                                        FrameLayout(ctx).apply {
                                            setPaddingRelative(
                                                paddingHorizontalPx,
                                                paddingTopPx,
                                                paddingHorizontalPx,
                                                0
                                            )

                                            cachedView?.let { view ->
                                                (view.parent as? ViewGroup)?.removeView(view)
                                                addView(
                                                    view, FrameLayout.LayoutParams(
                                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                                        androidViewHeight,
                                                        androidViewGravity
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    update = { container ->
                                        val view = viewCache[resId]

                                        if (view == null || container.getChildAt(0) === view) return@AndroidView

                                        (view.parent as? ViewGroup)?.removeView(view)

                                        container.removeAllViews()
                                        container.addView(
                                            view,
                                            FrameLayout.LayoutParams(
                                                ViewGroup.LayoutParams.MATCH_PARENT,
                                                androidViewHeight,
                                                androidViewGravity
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }
                        }
                    }
                }

                val labelAlpha by remember {
                    derivedStateOf { (1f - fraction.absoluteValue * 2f).coerceIn(0f, 1f) }
                }
                val labelSlide by remember { derivedStateOf { fraction * -32f } }

                if (isLoaded && isReady) {
                    Text(
                        text = resolvedNames[pagerState.currentPage.realIndex(count)],
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .zIndex(3f)
                            .align(Alignment.Center)
                            .offset {
                                IntOffset(
                                    x = labelSlide.dp.toPx().roundToInt(),
                                    y = (frameHeight / 2 - bezelV - 32.dp).toPx().roundToInt(),
                                )
                            }
                            .graphicsLayer { alpha = labelAlpha },
                    )
                }
            }
        }

        val isSelected = rememberSaveable(pagerState.currentPage, startPageIndex) {
            mutableStateOf(
                pagerState.currentPage.realIndex(count) == startPageIndex
            )
        }

        SplitButtonLayout(
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    enabled = enabled && isReady && !isSelected.value,
                    onClick = withHaptic {
                        onSelect(pagerState.currentPage.realIndex(count))
                    }
                ) {
                    Text(text = stringResource(R.string.btn_select))
                }
            },
            trailingButton = {
                SplitButtonDefaults.TrailingButton(
                    enabled = isReady && !isSelected.value,
                    onClick = withHaptic {
                        scope.launch {
                            val current = pagerState.currentPage
                            // Find the nearest virtual page that maps to startPageIndex
                            val offset = (startPageIndex - current.realIndex(count) + count) % count
                            val nearestPage = when {
                                offset == 0 -> current
                                offset <= count / 2 -> current + offset        // go forward
                                else -> current - (count - offset)             // go backward
                            }
                            pagerState.animateScrollToPage(nearestPage)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.SubdirectoryArrowRight,
                        contentDescription = "Reset",
                        modifier = Modifier
                            .size(SplitButtonDefaults.TrailingIconSize)
                            .rotate(180f),
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun PhoneFrame(
    isReady: Boolean,
    frameWidth: Dp,
    frameHeight: Dp,
    bezelHorizontal: Dp,
    bezelVertical: Dp,
    showPunchHole: Boolean,
    wallpaperBytes: ByteArray?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = LocalDarkMode.current

    val cornerRadius = frameWidth * 0.10f
    val cornerRadius2 = cornerRadius - 8.dp

    val darkBgColor = if (isDarkTheme) MaterialTheme.colorScheme.surface
    else MaterialTheme.colorScheme.inverseSurface

    val colors = remember(MaterialTheme.colorScheme, wallpaperBytes) {
        object {
            val outerBodyColor = Color(0xFF1C1C1E).harmonize(darkBgColor)
            val screenCutOutColor = Color.Black.harmonize(darkBgColor).copy(alpha = 0.6f)
            val buttonsColor = Color(0xFF2C2C2C).harmonize(darkBgColor)
            val punchHoleColor = (if (wallpaperBytes != null) Color.Black else Color.White)
                .harmonize(darkBgColor)
                .copy(alpha = if (wallpaperBytes != null) 0.6f else 0.2f)
            val gestureIndicatorColor = Color(0xFF555555).harmonize(darkBgColor)
        }
    }

    Box(
        modifier = modifier
            .width(frameWidth)
            .height(frameHeight),
    ) {
        // Outer body
        Box(
            modifier = Modifier
                .fillMaxSize()
                .shadow(28.dp, RoundedCornerShape(cornerRadius))
                .clip(RoundedCornerShape(cornerRadius))
                .background(colors.outerBodyColor),
        )

        // Screen cut-out
        Box(
            modifier = Modifier
                .padding(
                    horizontal = bezelHorizontal,
                    vertical = bezelVertical,
                )
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius2))
                .background(colors.screenCutOutColor),
        ) {
            AnimatedVisibility(
                visible = isReady,
                modifier = Modifier.align(Alignment.TopCenter),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                content()
            }
        }

        if (isReady) {
            Box(
                modifier = Modifier
                    .padding(
                        horizontal = bezelHorizontal,
                        vertical = bezelVertical
                    )
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius2)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(wallpaperBytes)
                        .memoryCacheKey("lock_wallpaper")
                        .diskCacheKey("lock_wallpaper")
                        .crossfade(true)
                        .build(),
                    contentDescription = "Lock wallpaper",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x33000000))
                )
            }
        }

        // Volume up
        Box(
            modifier = Modifier
                .width(bezelHorizontal * 0.45f)
                .height(frameHeight * 0.09f)
                .offset(x = -(bezelHorizontal * 0.42f), y = frameHeight * 0.22f)
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                .background(colors.buttonsColor),
        )
        // Volume down
        Box(
            modifier = Modifier
                .width(bezelHorizontal * 0.45f)
                .height(frameHeight * 0.09f)
                .offset(x = -(bezelHorizontal * 0.42f), y = frameHeight * 0.33f)
                .clip(RoundedCornerShape(topStart = 3.dp, bottomStart = 3.dp))
                .background(colors.buttonsColor),
        )
        // Power / lock button
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(bezelHorizontal * 0.45f)
                .height(frameHeight * 0.11f)
                .offset(x = bezelHorizontal * 0.42f, y = -(frameHeight * 0.04f))
                .clip(RoundedCornerShape(topEnd = 3.dp, bottomEnd = 3.dp))
                .background(colors.buttonsColor),
        )

        // Camera punch-hole
        if (showPunchHole) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = bezelVertical + 8.dp)
                    .size(bezelVertical * 1.2f)
                    .clip(RoundedCornerShape(50))
                    .background(colors.punchHoleColor),
            )
        }

        // Gesture indicator pill
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bezelVertical + 8.dp)
                .width(frameWidth * 0.30f)
                .height(bezelVertical * 0.45f)
                .clip(RoundedCornerShape(50))
                .background(colors.gestureIndicatorColor),
        )

        // Loading indicator
        AnimatedVisibility(
            visible = !isReady,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.8f),
        ) {
            CompositionLocalProvider(LocalDarkMode provides true) {
                ContainedLoadingIndicator(
                    modifier = Modifier.size(48.dp),
                )
            }
        }
    }
}

/** Maps a virtual infinite-pager page index back to the real 0-based index. */
private fun Int.realIndex(count: Int): Int {
    return if (count == 0) 0 else ((this % count) + count) % count
}

private fun pageScale(state: PagerState, virtualPage: Int, sideScale: Float): Float {
    val offset = ((state.currentPage - virtualPage) + state.currentPageOffsetFraction).absoluteValue
    return lerp(1f, sideScale, offset.coerceIn(0f, 1f))
}

@Suppress("SameParameterValue")
private fun lerp(start: Float, stop: Float, fraction: Float): Float =
    start + fraction * (stop - start)

@Composable
private fun rememberDeviceAspectRatio(): Float {
    val configuration = LocalConfiguration.current
    val containerSize = LocalWindowInfo.current.containerSize

    return remember(containerSize) {
        val w = when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> containerSize.width
            else -> containerSize.height
        }.toFloat()
        val h = when (configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> containerSize.height
            else -> containerSize.width
        }.toFloat()

        (w / h).coerceIn(0.40f, 0.75f)
    }
}

private val clockLayouts = listOf(
    R.layout.preview_lockscreen_clock_1,
    R.layout.preview_lockscreen_clock_2,
    R.layout.preview_lockscreen_clock_3,
    R.layout.preview_lockscreen_clock_4,
    R.layout.preview_lockscreen_clock_5,
)

@Preview(showBackground = true)
@Composable
private fun ClockPagerPortraitPreview() {
    PreviewComposable {
        DevicePreviewPager(
            layoutResIds = clockLayouts,
            onPageChanged = {},
        )
    }
}

@Preview(
    name = "Landscape Mode",
    showBackground = true,
    device = "spec:parent=pixel_9_pro,orientation=landscape",
)
@Composable
private fun ClockPagerWidePreview() {
    PreviewComposable {
        DevicePreviewPager(
            layoutResIds = clockLayouts,
            onPageChanged = {},
        )
    }
}

@Preview(
    name = "Night Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ClockPagerNightPreview() {
    PreviewComposable {
        DevicePreviewPager(
            layoutResIds = clockLayouts,
            onPageChanged = {},
        )
    }
}