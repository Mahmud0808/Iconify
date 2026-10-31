package com.drdisagree.iconify.features.xposed.lockscreen.clock.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.graphics.RectF
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.common.LocalPreferenceController
import com.drdisagree.iconify.core.ui.components.others.AutoScalingDevicePreview
import com.drdisagree.iconify.core.ui.components.others.innerPaddingValues
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.pagers.PhoneFrame
import com.drdisagree.iconify.core.ui.components.scaffolds.AppScaffold
import com.drdisagree.iconify.core.utils.clock.ClockCustomizer
import com.drdisagree.iconify.core.utils.clock.ClockElementEdit
import com.drdisagree.iconify.core.utils.clock.ClockElementType
import com.drdisagree.iconify.core.utils.clock.CustomClockElement
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.features.xposed.lockscreen.clock.components.clockEditorItems
import com.drdisagree.iconify.features.xposed.lockscreen.clock.viewmodels.LockscreenClockViewModel
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlinx.coroutines.flow.flowOf

private const val SNAP_DP = 6f
private const val HORIZONTAL_PADDING_DP = 28
private const val MIN_HIT_DP = 24f
private const val HIT_PADDING_DP = 6f
private const val SAMPLE_WIDTH = 160
private const val CONTRAST_SAMPLES = 8
private const val WALLPAPER_DIM = 0.8f
private const val PREVIEW_WIDTH_FRACTION = 0.62f
private const val NEW_ELEMENT_GAP_DP = 16f

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@SuppressLint("DiscouragedApi")
@Composable
fun ClockEditorScreen(
    lockscreenClockViewModel: LockscreenClockViewModel? = hiltViewModel()
) {
    val context = LocalContext.current
    val resources = LocalResources.current
    val haptic = LocalHapticFeedback.current
    val prefController = LocalPreferenceController.current

    val style by prefController.observe(XposedKey.LSCLOCK_STYLE.name, 0)
    val topMargin by prefController.observe(XposedKey.LSCLOCK_TOP_MARGIN.name, 100f)
    val isCustom = style == ClockCustomizer.CUSTOM_STYLE

    LaunchedEffect(Unit) { lockscreenClockViewModel?.loadWallpaper() }

    val wallpaperBytes by (lockscreenClockViewModel?.wallpaperBytes ?: flowOf(null))
        .collectAsStateWithLifecycle(initialValue = null)
    val wallpaperReady by (lockscreenClockViewModel?.wallpaperReady ?: flowOf(true))
        .collectAsStateWithLifecycle(initialValue = false)

    var edits by remember(style) {
        mutableStateOf(
            ClockCustomizer.parseEdits(prefController.getString(XposedKey.LSCLOCK_ELEMENT_EDITS))[style]
                .orEmpty()
        )
    }
    var elements by remember {
        mutableStateOf(
            ClockCustomizer.parseCustomClock(prefController.getString(XposedKey.LSCLOCK_CUSTOM_CLOCK))
        )
    }
    var selected by remember(style) { mutableStateOf<Int?>(null) }
    var container by remember { mutableStateOf<ViewGroup?>(null) }
    var clockRoot by remember { mutableStateOf<ViewGroup?>(null) }
    var layoutVersion by remember { mutableIntStateOf(0) }
    var snappedToCenter by remember { mutableStateOf(false) }
    var selectionBounds by remember { mutableStateOf<ElementBounds?>(null) }
    var outlineColor by remember { mutableStateOf(Color.White) }
    val wallpaperSample by rememberUpdatedState(
        remember(wallpaperBytes) { wallpaperBytes?.let { decodeSample(it) } }
    )
    var showResetDialog by remember { mutableStateOf(false) }

    fun saveEdits(newEdits: Map<Int, ClockElementEdit>) {
        edits = newEdits
        val all = ClockCustomizer.parseEdits(prefController.getString(XposedKey.LSCLOCK_ELEMENT_EDITS))
            .toMutableMap()
        all[style] = newEdits
        prefController.setString(XposedKey.LSCLOCK_ELEMENT_EDITS, ClockCustomizer.serializeEdits(all))
    }

    fun saveElements(newElements: List<CustomClockElement>) {
        elements = newElements
        prefController.setString(
            XposedKey.LSCLOCK_CUSTOM_CLOCK,
            ClockCustomizer.serializeCustomClock(newElements)
        )
    }

    fun elementOffset(index: Int): Pair<Float, Float> = if (isCustom) {
        elements.getOrNull(index)?.let { it.x to it.y } ?: (0f to 0f)
    } else {
        edits[index]?.let { it.x to it.y } ?: (0f to 0f)
    }

    fun refreshSelection() {
        val frame = container
        val root = clockRoot
        val index = selected
        val bounds = if (frame == null || root == null || index == null) {
            null
        } else {
            ClockCustomizer.textViews(root).getOrNull(index)?.let { view ->
                textBounds(frame, view, frame.resources.displayMetrics.density)
            }
        }
        selectionBounds = bounds
        if (frame != null && bounds != null) {
            outlineColor = contrastColor(wallpaperSample, bounds.rect, frame.width, frame.height)
        }
    }

    fun commitMove(index: Int, x: Float, y: Float) {
        if (isCustom) {
            saveElements(elements.mapIndexed { i, e -> if (i == index) e.copy(x = x, y = y) else e })
        } else {
            val current = edits[index] ?: ClockElementEdit()
            saveEdits(edits + (index to current.copy(x = x, y = y)))
        }
    }

    LaunchedEffect(clockRoot, edits, elements, isCustom) {
        val root = clockRoot ?: return@LaunchedEffect
        if (isCustom) {
            ClockCustomizer.buildCustomClock(root, elements, context, LSCLOCK_FONT_FILE)
        } else {
            ClockCustomizer.applyEdits(root, edits, context, LSCLOCK_FONT_FILE)
        }
        layoutVersion++
        refreshSelection()
    }

    LaunchedEffect(selected, wallpaperSample) { refreshSelection() }

    val layoutId = remember(style) {
        resources.getIdentifier(
            ClockCustomizer.layoutName(style),
            "layout",
            BuildConfig.APPLICATION_ID
        )
    }

    val selectedIndex = selected
    val selectedView = remember(clockRoot, layoutVersion, selectedIndex) {
        selectedIndex?.let { index ->
            clockRoot?.let { ClockCustomizer.textViews(it).getOrNull(index) }
        }
    }
    val selectedType = if (isCustom) {
        selectedIndex?.let { elements.getOrNull(it)?.type }
    } else {
        selectedView?.let { elementType(it) }
    }
    val layoutSize = remember(selectedView, layoutVersion) {
        selectedView?.let { ClockCustomizer.originalTextSizeSp(it) } ?: 0f
    }
    val layoutColor = remember(selectedView, layoutVersion) {
        selectedView?.let { ClockCustomizer.originalTextColor(it) } ?: Color.White.toArgb()
    }

    AppScaffold(
        title = stringResource(R.string.lsclock_editor_title),
        showBackIcon = true
    ) { innerPadding, scrollBehavior ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = innerPaddingValues(
                innerPadding = innerPadding,
                horizontal = 16.dp,
                vertical = 16.dp
            )
        ) {
            item(key = "clock_editor_preview") {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    val metrics = resources.displayMetrics
                    val aspect = (min(metrics.widthPixels, metrics.heightPixels).toFloat() /
                            max(metrics.widthPixels, metrics.heightPixels)).coerceIn(0.40f, 0.75f)

                    val bezel = 8.dp
                    val frameWidth = maxWidth * PREVIEW_WIDTH_FRACTION
                    val frameHeight = frameWidth / aspect
                    val screenWidth = frameWidth - bezel * 2
                    val screenHeight = frameHeight - bezel * 2
                    val screenCorner = frameWidth * 0.10f - bezel

                    PhoneFrame(
                        isReady = wallpaperReady,
                        frameWidth = frameWidth,
                        frameHeight = frameHeight,
                        bezelHorizontal = bezel,
                        bezelVertical = bezel,
                        showPunchHole = true,
                        wallpaperBytes = wallpaperBytes,
                        content = {}
                    )

                    Box(
                        modifier = Modifier
                            .size(screenWidth, screenHeight)
                            .clip(RoundedCornerShape(screenCorner))
                    ) {
                        if (layoutId != 0) {
                            AutoScalingDevicePreview(
                                modifier = Modifier.size(screenWidth, screenHeight)
                            ) {
                                AndroidView(
                                    factory = { ctx ->
                                        FrameLayout(ctx).apply {
                                            clipChildren = false
                                            clipToPadding = false
                                            val root = LayoutInflater.from(ctx)
                                                .inflate(layoutId, this, false) as ViewGroup
                                            addView(
                                                root,
                                                FrameLayout.LayoutParams(
                                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                                    Gravity.CENTER_HORIZONTAL
                                                )
                                            )
                                            container = this
                                            clockRoot = root
                                        }
                                    },
                                    update = { frame ->
                                        val d = frame.resources.displayMetrics.density
                                        val horizontal = (HORIZONTAL_PADDING_DP * d).toInt()
                                        frame.setPadding(
                                            horizontal,
                                            (topMargin * d).toInt(),
                                            horizontal,
                                            0
                                        )
                                    }
                                )
                            }
                        }

                        container?.let { frame ->
                            DisposableEffect(frame) {
                                val listener = ViewTreeObserver.OnGlobalLayoutListener {
                                    layoutVersion++
                                    refreshSelection()
                                }
                                frame.viewTreeObserver.addOnGlobalLayoutListener(listener)
                                onDispose { frame.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
                            }
                        }

                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .pointerInput(isCustom, container, clockRoot) {
                                    val frame = container ?: return@pointerInput
                                    val root = clockRoot ?: return@pointerInput

                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        val scale = previewScale(size.width.toFloat(), size.height.toFloat(), frame)
                                        val viewDensity = frame.resources.displayMetrics.density
                                        val hit = hitTest(frame, root, down.position / scale, viewDensity)

                                        selected = hit
                                        if (hit == null) return@awaitEachGesture

                                        val view = ClockCustomizer.textViews(root).getOrNull(hit)
                                            ?: return@awaitEachGesture
                                        val (startX, startY) = elementOffset(hit)
                                        var total = Offset.Zero
                                        var dragging = false
                                        var x = startX
                                        var y = startY

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == down.id }
                                                ?: break
                                            if (!change.pressed) break

                                            total += change.positionChange()
                                            if (!dragging && total.getDistance() > viewConfiguration.touchSlop) {
                                                dragging = true
                                            }
                                            if (!dragging) continue

                                            change.consume()
                                            x = startX + total.x / scale / viewDensity
                                            y = startY + total.y / scale / viewDensity

                                            val snap = abs(x) < SNAP_DP
                                            if (snap) x = 0f
                                            if (snap && !snappedToCenter) {
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            }
                                            snappedToCenter = snap
                                            if (isCustom) y = y.coerceAtLeast(0f)

                                            ClockCustomizer.moveElement(view, isCustom, x, y)
                                            refreshSelection()
                                        }

                                        snappedToCenter = false
                                        if (dragging) commitMove(hit, x, y)
                                    }
                                }
                                .drawWithContent {
                                    drawContent()

                                    val frame = container ?: return@drawWithContent
                                    val scale = previewScale(size.width, size.height, frame)

                                    val strokeWidth = 1.5.dp.toPx()
                                    val dashEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(6.dp.toPx(), 4.dp.toPx())
                                    )

                                    if (snappedToCenter) {
                                        val centerX = frame.width / 2f * scale
                                        drawLine(
                                            color = outlineColor,
                                            start = Offset(centerX, 0f),
                                            end = Offset(centerX, size.height),
                                            strokeWidth = strokeWidth,
                                            pathEffect = dashEffect
                                        )
                                    }

                                    val bounds = selectionBounds ?: return@drawWithContent
                                    val rect = bounds.rect

                                    rotate(
                                        degrees = bounds.rotation,
                                        pivot = Offset(bounds.pivotX * scale, bounds.pivotY * scale)
                                    ) {
                                        drawRoundRect(
                                            color = outlineColor,
                                            topLeft = Offset(rect.left * scale, rect.top * scale),
                                            size = Size(rect.width() * scale, rect.height() * scale),
                                            cornerRadius = CornerRadius(6.dp.toPx()),
                                            style = Stroke(width = strokeWidth, pathEffect = dashEffect)
                                        )
                                    }
                                }
                        )
                    }
                }
            }

            clockEditorItems(
                isCustom = isCustom,
                selectedIndex = selectedIndex?.takeIf { selectedView != null },
                selectedType = selectedType,
                edit = selectedIndex?.let { edits[it] },
                element = selectedIndex?.let { elements.getOrNull(it) },
                layoutSize = layoutSize,
                layoutColor = layoutColor,
                canResetClock = if (isCustom) {
                    elements != ClockCustomizer.defaultCustomClock()
                } else {
                    edits.isNotEmpty()
                },
                onResetClock = { showResetDialog = true },
                onEditChanged = { edit, persist ->
                    val index = selectedIndex ?: return@clockEditorItems
                    val updated = edits + (index to edit)
                    if (persist) saveEdits(updated) else edits = updated
                },
                onElementChanged = { element, persist ->
                    val index = selectedIndex ?: return@clockEditorItems
                    val updated = elements.mapIndexed { i, e -> if (i == index) element else e }
                    if (persist) saveElements(updated) else elements = updated
                },
                onRemove = {
                    val index = selectedIndex ?: return@clockEditorItems
                    selected = null
                    if (isCustom) {
                        saveElements(elements.filterIndexed { i, _ -> i != index })
                    } else {
                        saveEdits(edits - index)
                    }
                },
                onAdd = { type ->
                    val value = when (type) {
                        ClockElementType.TIME -> ClockCustomizer.TIME_FORMATS.first()
                        ClockElementType.DATE -> ClockCustomizer.DATE_FORMATS.first()
                        ClockElementType.TEXT -> context.getString(R.string.lsclock_element_text)
                    }
                    val bottom = clockRoot?.let { root ->
                        root.height / root.resources.displayMetrics.density
                    } ?: 0f
                    saveElements(
                        elements + ClockCustomizer.newElement(type, value, bottom + NEW_ELEMENT_GAP_DP)
                    )
                    selected = elements.lastIndex
                }
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.lsclock_reset_title)) },
            text = { Text(stringResource(R.string.lsclock_reset_desc)) },
            confirmButton = {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                        showResetDialog = false
                        selected = null
                        if (isCustom) saveElements(ClockCustomizer.defaultCustomClock())
                        else saveEdits(emptyMap())
                    }
                ) { Text(stringResource(R.string.btn_positive)) }
            },
            dismissButton = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic { showResetDialog = false }
                ) { Text(stringResource(R.string.btn_negative)) }
            }
        )
    }
}

private fun previewScale(width: Float, height: Float, frame: View): Float = max(
    width / frame.width.coerceAtLeast(1),
    height / frame.height.coerceAtLeast(1)
)

private fun decodeSample(bytes: ByteArray): Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    var sampleSize = 1
    while (bounds.outWidth / (sampleSize * 2) >= SAMPLE_WIDTH) sampleSize *= 2

    BitmapFactory.decodeByteArray(
        bytes,
        0,
        bytes.size,
        BitmapFactory.Options().apply { inSampleSize = sampleSize }
    )
} catch (_: Exception) {
    null
}

private fun contrastColor(wallpaper: Bitmap?, bounds: RectF, width: Int, height: Int): Color {
    if (wallpaper == null || width == 0 || height == 0) return Color.White

    val scale = max(width.toFloat() / wallpaper.width, height.toFloat() / wallpaper.height)
    val offsetX = (wallpaper.width * scale - width) / 2f
    val offsetY = (wallpaper.height * scale - height) / 2f

    var luminance = 0f
    for (i in 0 until CONTRAST_SAMPLES) {
        for (j in 0 until CONTRAST_SAMPLES) {
            val x = bounds.left + bounds.width() * (i + 0.5f) / CONTRAST_SAMPLES
            val y = bounds.top + bounds.height() * (j + 0.5f) / CONTRAST_SAMPLES
            val pixelX = ((x + offsetX) / scale).toInt().coerceIn(0, wallpaper.width - 1)
            val pixelY = ((y + offsetY) / scale).toInt().coerceIn(0, wallpaper.height - 1)
            luminance += Color(wallpaper.getPixel(pixelX, pixelY)).luminance()
        }
    }
    luminance /= CONTRAST_SAMPLES * CONTRAST_SAMPLES

    return if (luminance * WALLPAPER_DIM > 0.5f) Color.Black else Color.White
}

private fun elementType(view: TextView): ClockElementType {
    if (view !is TextClock) return ClockElementType.TEXT

    val format = if (view.is24HourModeEnabled) view.format24Hour else view.format12Hour
    return if (format?.any { it in "hHkKm" } == true) ClockElementType.TIME else ClockElementType.DATE
}

private data class ElementBounds(
    val rect: RectF,
    val rotation: Float,
    val pivotX: Float,
    val pivotY: Float
) {
    fun contains(point: Offset): Boolean {
        val radians = Math.toRadians(-rotation.toDouble())
        val dx = point.x - pivotX
        val dy = point.y - pivotY
        val x = pivotX + dx * cos(radians) - dy * sin(radians)
        val y = pivotY + dx * sin(radians) + dy * cos(radians)
        return rect.contains(x.toFloat(), y.toFloat())
    }
}

private fun hitTest(frame: ViewGroup, root: ViewGroup, point: Offset, density: Float): Int? =
    ClockCustomizer.textViews(root)
        .withIndex()
        .mapNotNull { (index, view) ->
            textBounds(frame, view, density)
                ?.takeIf { it.contains(point) }
                ?.let { index to it.rect.width() * it.rect.height() }
        }
        .minByOrNull { it.second }
        ?.first

private fun textBounds(frame: ViewGroup, view: TextView, density: Float): ElementBounds? {
    if (!view.isShown || view.width == 0) return null
    val layout = view.layout ?: return null

    val rect = Rect(0, 0, view.width, view.height)
    try {
        frame.offsetDescendantRectToMyCoords(view, rect)
    } catch (_: IllegalArgumentException) {
        return null
    }
    rect.offset(view.scrollX, view.scrollY)
    val pivotX = rect.left + view.translationX + view.pivotX
    val pivotY = rect.top + view.translationY + view.pivotY

    var lineLeft = Float.MAX_VALUE
    var lineRight = -Float.MAX_VALUE
    for (line in 0 until layout.lineCount) {
        lineLeft = min(lineLeft, layout.getLineLeft(line))
        lineRight = max(lineRight, layout.getLineRight(line))
    }
    if (lineRight < lineLeft) return null

    val textLeft = rect.left + view.totalPaddingLeft - view.scrollX + view.translationX
    val viewRight = rect.left + view.width - view.totalPaddingRight + view.translationX
    val left = max(textLeft + lineLeft, rect.left + view.totalPaddingLeft + view.translationX)
    val right = min(textLeft + lineRight, viewRight)
    val top = rect.top + view.totalPaddingTop + view.translationY
    val bounds = RectF(left, top, max(left, right), top + layout.height)

    val minSize = MIN_HIT_DP * density
    if (bounds.width() < minSize) bounds.inset(-(minSize - bounds.width()) / 2f, 0f)
    if (bounds.height() < minSize) bounds.inset(0f, -(minSize - bounds.height()) / 2f)
    bounds.inset(-HIT_PADDING_DP * density, -HIT_PADDING_DP * density)

    return ElementBounds(bounds, view.rotation, pivotX, pivotY)
}
