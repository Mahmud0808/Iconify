package com.drdisagree.iconify.features.xposed.lockscreen.clock.components

import android.graphics.Typeface
import android.graphics.fonts.Font
import android.provider.OpenableColumns
import android.text.format.DateFormat
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.preferences.stringRes
import com.drdisagree.iconify.core.ui.components.others.ColumnScrollIndicator
import com.drdisagree.iconify.core.ui.components.others.withHaptic
import com.drdisagree.iconify.core.ui.components.preferences.CategoryTitleRow
import com.drdisagree.iconify.core.ui.components.preferences.ColorPickerDialog
import com.drdisagree.iconify.core.ui.components.preferences.FilePickerType
import com.drdisagree.iconify.core.ui.components.preferences.PreferenceContainer
import com.drdisagree.iconify.core.ui.components.preferences.TitleSummaryBlock
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_LARGE
import com.drdisagree.iconify.core.ui.utils.CARD_CORNER_SMALL
import com.drdisagree.iconify.core.ui.utils.CARD_ITEM_SPACING
import com.drdisagree.iconify.core.ui.utils.ItemPosition
import com.drdisagree.iconify.core.ui.utils.cardCorners
import com.drdisagree.iconify.core.utils.FileUtils.copyToXposedSharedPath
import com.drdisagree.iconify.core.utils.clock.ClockCustomizer
import com.drdisagree.iconify.core.utils.clock.ClockElementEdit
import com.drdisagree.iconify.core.utils.clock.ClockElementType
import com.drdisagree.iconify.core.utils.clock.CustomClockElement
import com.drdisagree.iconify.data.common.XposedConst.LSCLOCK_FONT_FILE
import com.drdisagree.iconify.data.common.XposedConst.XPOSED_RESOURCE_DIR
import com.github.yohannestz.iconsax_compose.iconsax.Iconsax
import com.materialkolor.ktx.toHex
import java.io.File
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val COLOR_PICKER = "picker"
private const val FORMAT_CUSTOM = "custom"

private val HUE_COLORS = listOf(
    Color.Red,
    Color.Yellow,
    Color.Green,
    Color.Cyan,
    Color.Blue,
    Color.Magenta,
    Color.Red
)

private data class FontOption(
    val key: String,
    val label: String,
    val family: FontFamily?,
    val isImported: Boolean = false
)

private enum class EditorRow { FONT, VALUE, SIZE, ROTATION, COLOR }

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
fun LazyListScope.clockEditorItems(
    isCustom: Boolean,
    selectedIndex: Int?,
    selectedType: ClockElementType?,
    edit: ClockElementEdit?,
    element: CustomClockElement?,
    layoutSize: Float,
    layoutColor: Int,
    canResetClock: Boolean,
    onResetClock: () -> Unit,
    onEditChanged: (edit: ClockElementEdit, persist: Boolean) -> Unit,
    onElementChanged: (element: CustomClockElement, persist: Boolean) -> Unit,
    onRemove: () -> Unit,
    onAdd: (ClockElementType) -> Unit
) {
    if (selectedIndex == null || selectedType == null || (isCustom && element == null)) {
        item(key = "clock_editor_hint") {
            Text(
                text = stringResource(R.string.lsclock_editor_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            )
        }

        if (canResetClock) {
            item(key = "clock_editor_reset") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TextButton(onClick = withHaptic { onResetClock() }) {
                        Text(stringResource(R.string.lsclock_reset_clock))
                    }
                }
            }
        }

        if (!isCustom) return

        item(key = "clock_editor_add_title") {
            Column {
                Spacer(Modifier.height(24.dp))
                CategoryTitleRow(title = stringRes(R.string.lsclock_add_element))
            }
        }

        item(key = "clock_editor_add_tiles") {
            AddElementTiles(onAdd)
        }
        return
    }

    val current = edit ?: ClockElementEdit()
    val font = if (isCustom) element!!.font else current.font
    val size = when {
        isCustom -> element!!.size
        current.size > 0f -> current.size
        else -> layoutSize
    }
    val defaultSize = if (isCustom) ClockCustomizer.defaultSize(selectedType) else layoutSize
    val color = if (isCustom) element!!.color else current.color
    val colorOptions = if (isCustom) ClockCustomizer.COLORS else listOf("") + ClockCustomizer.COLORS

    fun setFont(value: String) {
        if (isCustom) onElementChanged(element!!.copy(font = value), true)
        else onEditChanged(current.copy(font = value), true)
    }

    fun setSize(value: Float, persist: Boolean) {
        val rounded = value.roundToInt().toFloat()
        if (isCustom) {
            onElementChanged(element!!.copy(size = rounded), persist)
        } else {
            val isDefault = rounded.roundToInt() == layoutSize.roundToInt()
            onEditChanged(current.copy(size = if (isDefault) 0f else rounded), persist)
        }
    }

    val rotation = if (isCustom) element!!.rotation else current.rotation

    fun setRotation(value: Float, persist: Boolean) {
        val rounded = value.roundToInt().toFloat()
        if (isCustom) onElementChanged(element!!.copy(rotation = rounded), persist)
        else onEditChanged(current.copy(rotation = rounded), persist)
    }

    fun setColor(value: String) {
        if (isCustom) onElementChanged(element!!.copy(color = value), true)
        else onEditChanged(current.copy(color = value), true)
    }

    item(key = "clock_editor_element_title") {
        val canReset = isCustom || !current.isEmpty

        Column {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .padding(start = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(selectedType.title()),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                AnimatedVisibility(visible = canReset, enter = fadeIn(), exit = fadeOut()) {
                    IconButton(
                        onClick = withHaptic { onRemove() },
                        shapes = IconButtonDefaults.shapes(),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (isCustom) Iconsax.Outline.Trash else Iconsax.Outline.RefreshTwo,
                            contentDescription = stringResource(
                                if (isCustom) R.string.lsclock_element_remove else R.string.btn_reset
                            ),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    val rows = buildList {
        add(EditorRow.FONT)
        if (isCustom) add(EditorRow.VALUE)
        add(EditorRow.SIZE)
        add(EditorRow.ROTATION)
        add(EditorRow.COLOR)
    }

    rows.forEachIndexed { index, row ->
        item(key = "clock_editor_row_$row") {
            val shape = shapeOf(index, rows.size)
            val modifier = Modifier.padding(top = if (index == 0) 0.dp else CARD_ITEM_SPACING)

            when (row) {
                EditorRow.FONT -> FontCard(font, shape, modifier, ::setFont)

                EditorRow.VALUE -> ValueCard(element!!, shape, modifier) { updated ->
                    onElementChanged(updated, true)
                }

                EditorRow.SIZE -> SliderCard(
                    title = R.string.lsclock_element_size,
                    label = R.string.lsclock_size_sp,
                    value = size,
                    defaultValue = defaultSize,
                    range = ClockCustomizer.MIN_TEXT_SIZE..ClockCustomizer.MAX_TEXT_SIZE,
                    shape = shape,
                    modifier = modifier,
                    onChanged = ::setSize
                )

                EditorRow.ROTATION -> SliderCard(
                    title = R.string.lsclock_element_rotation,
                    label = R.string.lsclock_rotation_deg,
                    value = rotation,
                    defaultValue = 0f,
                    range = -ClockCustomizer.MAX_ROTATION..ClockCustomizer.MAX_ROTATION,
                    shape = shape,
                    modifier = modifier,
                    onChanged = ::setRotation
                )

                EditorRow.COLOR -> ColorCard(
                    options = colorOptions,
                    selected = color,
                    layoutColor = layoutColor,
                    shape = shape,
                    modifier = modifier,
                    onSelected = ::setColor
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddElementTiles(onAdd: (ClockElementType) -> Unit) {
    val now = remember { System.currentTimeMillis() }
    val types = ClockElementType.entries

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(CARD_ITEM_SPACING)
    ) {
        types.forEachIndexed { index, type ->
            val start = if (index == 0) CARD_CORNER_LARGE else CARD_CORNER_SMALL
            val end = if (index == types.lastIndex) CARD_CORNER_LARGE else CARD_CORNER_SMALL
            val shape = RoundedCornerShape(
                topStart = start,
                bottomStart = start,
                topEnd = end,
                bottomEnd = end
            )
            val sample = when (type) {
                ClockElementType.TIME -> DateFormat.format(ClockCustomizer.TIME_FORMATS.first(), now)
                ClockElementType.DATE -> DateFormat.format("EEE d", now)
                ClockElementType.TEXT -> "Aa"
            }.toString()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh, shape)
                    .clickable(onClick = withHaptic { onAdd(type) })
                    .padding(vertical = 20.dp, horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = sample,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1
                )
                Text(
                    text = stringResource(type.title()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FontCard(
    selectedKey: String,
    shape: RoundedCornerShape,
    modifier: Modifier,
    onSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var fontsVersion by remember { mutableIntStateOf(0) }
    val fonts = rememberFontOptions(fontsVersion)
    val selected = fonts.firstOrNull { it.key == selectedKey } ?: fonts.first()
    var showDialog by rememberSaveable { mutableStateOf(false) }

    val fontPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        scope.launch {
            val displayName = context.contentResolver
                .query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor -> if (cursor.moveToFirst()) cursor.getString(0) else null }
                ?: uri.lastPathSegment.orEmpty()
            val fileName = ClockCustomizer.importedFontFileName(displayName)
            val path = uri.copyToXposedSharedPath(fileName) ?: return@launch

            val isValid = withContext(Dispatchers.IO) {
                runCatching { Font.Builder(File(path)).build() }.isSuccess
            }
            if (!isValid) {
                withContext(Dispatchers.IO) { File(path).delete() }
                Toast.makeText(context, R.string.lsclock_invalid_font, Toast.LENGTH_SHORT).show()
                return@launch
            }

            fontsVersion++
            onSelected(fileName)
            showDialog = false
        }
    }

    fun removeFont(font: FontOption) {
        scope.launch {
            withContext(Dispatchers.IO) { File(XPOSED_RESOURCE_DIR, font.key).delete() }
            fontsVersion++
            if (font.key == selectedKey) onSelected("")
        }
    }

    PreferenceContainer(
        shape = shape,
        isEnabled = true,
        modifier = modifier,
        minLine = 2,
        onClick = withHaptic { showDialog = true }
    ) {
        TitleSummaryBlock(stringRes(R.string.lsclock_element_font), selected.label, isEnabled = true)
    }

    if (showDialog) {
        ChoiceDialog(
            title = stringResource(R.string.lsclock_element_font),
            options = fonts,
            isSelected = { it.key == selected.key },
            onSelect = { onSelected(it.key) },
            onDismiss = { showDialog = false },
            secondaryAction = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic {
                        fontPicker.launch(FilePickerType.Font.mimeTypes.toTypedArray())
                    }
                ) {
                    Icon(
                        imageVector = Iconsax.Outline.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.lsclock_add_font))
                }
            }
        ) { font ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = font.label,
                    fontFamily = font.family,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (font.isImported) {
                    IconButton(
                        onClick = withHaptic { removeFont(font) },
                        shapes = IconButtonDefaults.shapes(),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Iconsax.Outline.Trash,
                            contentDescription = stringResource(R.string.lsclock_element_remove),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ValueCard(
    element: CustomClockElement,
    shape: RoundedCornerShape,
    modifier: Modifier,
    onChanged: (CustomClockElement) -> Unit
) {
    var showChoices by rememberSaveable { mutableStateOf(false) }
    var showInput by rememberSaveable { mutableStateOf(false) }
    val isText = element.type == ClockElementType.TEXT
    val now = remember { System.currentTimeMillis() }
    val title = stringResource(
        if (isText) R.string.lsclock_element_text else R.string.lsclock_element_format
    )
    val formats = when (element.type) {
        ClockElementType.TIME -> ClockCustomizer.TIME_FORMATS
        ClockElementType.DATE -> ClockCustomizer.DATE_FORMATS
        ClockElementType.TEXT -> emptyList()
    }

    fun preview(format: String): String =
        runCatching { DateFormat.format(format, now).toString() }.getOrDefault("")

    PreferenceContainer(
        shape = shape,
        isEnabled = true,
        modifier = modifier,
        minLine = 2,
        onClick = withHaptic { if (isText) showInput = true else showChoices = true }
    ) {
        TitleSummaryBlock(
            stringRes(title),
            if (isText) element.value else preview(element.value),
            isEnabled = true
        )
    }

    if (showChoices) {
        val customLabel = stringResource(R.string.lsclock_custom_style)

        ChoiceDialog(
            title = title,
            options = formats + FORMAT_CUSTOM,
            isSelected = {
                if (it == FORMAT_CUSTOM) element.value !in formats else it == element.value
            },
            onSelect = {
                if (it == FORMAT_CUSTOM) showInput = true else onChanged(element.copy(value = it))
            },
            onDismiss = { showChoices = false }
        ) { format ->
            Text(
                text = if (format == FORMAT_CUSTOM) customLabel else preview(format),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }

    if (showInput) {
        var draft by rememberSaveable { mutableStateOf(element.value) }
        val draftPreview = if (isText) "" else preview(draft)
        val isValid = draft.isNotBlank() && (isText || draftPreview.isNotBlank())

        AlertDialog(
            onDismissRequest = { showInput = false },
            title = { Text(title) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    singleLine = true,
                    supportingText = if (isText) null else {
                        { Text(draftPreview) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    shapes = ButtonDefaults.shapes(),
                    enabled = isValid,
                    onClick = withHaptic {
                        onChanged(element.copy(value = draft))
                        showInput = false
                    }
                ) { Text(stringResource(R.string.btn_select)) }
            },
            dismissButton = {
                OutlinedButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = withHaptic { showInput = false }
                ) { Text(stringResource(android.R.string.cancel)) }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SliderCard(
    @StringRes title: Int,
    @StringRes label: Int,
    value: Float,
    defaultValue: Float,
    range: ClosedFloatingPointRange<Float>,
    shape: RoundedCornerShape,
    modifier: Modifier,
    onChanged: (value: Float, persist: Boolean) -> Unit
) {
    val isDefault = value.roundToInt() == defaultValue.roundToInt()
    val tick = withHaptic { }
    var lastValue by remember { mutableIntStateOf(value.roundToInt()) }
    var pendingValue by remember { mutableFloatStateOf(value) }

    fun update(newValue: Float, persist: Boolean) {
        val clamped = newValue.coerceIn(range.start, range.endInclusive)
        if (clamped.roundToInt() != lastValue) {
            tick()
            lastValue = clamped.roundToInt()
        }
        pendingValue = clamped
        onChanged(clamped, persist)
    }

    PreferenceContainer(
        shape = shape,
        isEnabled = true,
        modifier = modifier,
        minLine = 2
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TitleSummaryBlock(stringRes(title), null, isEnabled = true)
                Text(
                    text = stringResource(label, value.roundToInt()),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    enabled = value > range.start,
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    onClick = { update(value.roundToInt() - 1f, true) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Iconsax.Outline.Minus,
                        contentDescription = stringResource(R.string.btn_decrease),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Slider(
                    value = value,
                    onValueChange = { update(it, false) },
                    onValueChangeFinished = { onChanged(pendingValue, true) },
                    valueRange = range,
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1f)
                )
                IconButton(
                    enabled = value < range.endInclusive,
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(),
                    onClick = { update(value.roundToInt() + 1f, true) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Iconsax.Outline.Add,
                        contentDescription = stringResource(R.string.btn_increase),
                        modifier = Modifier.size(16.dp)
                    )
                }
                IconButton(
                    enabled = !isDefault,
                    shapes = IconButtonDefaults.shapes(),
                    colors = IconButtonDefaults.filledIconButtonColors(),
                    onClick = { update(defaultValue, true) }
                ) {
                    Icon(
                        imageVector = Iconsax.Outline.RefreshTwo,
                        contentDescription = stringResource(R.string.btn_reset),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorCard(
    options: List<String>,
    selected: String,
    layoutColor: Int,
    shape: RoundedCornerShape,
    modifier: Modifier,
    onSelected: (String) -> Unit
) {
    val context = LocalContext.current
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showPicker by rememberSaveable { mutableStateOf(false) }
    val isHex = ClockCustomizer.isHexColor(selected)

    fun resolve(color: String): Color =
        Color(if (color.isEmpty()) layoutColor else ClockCustomizer.defaultColor(context, color))

    PreferenceContainer(
        shape = shape,
        isEnabled = true,
        modifier = modifier,
        minLine = 2,
        onClick = withHaptic { showDialog = true }
    ) {
        TitleSummaryBlock(
            stringRes(R.string.lsclock_element_color),
            if (isHex) selected.uppercase() else stringResource(colorTitle(selected)),
            isEnabled = true
        )
        ColorSwatch(SolidColor(resolve(selected)), 40.dp)
    }

    if (showDialog) {
        ChoiceDialog(
            title = stringResource(R.string.lsclock_element_color),
            options = options + COLOR_PICKER,
            isSelected = { if (it == COLOR_PICKER) isHex else it == selected },
            onSelect = { if (it == COLOR_PICKER) showPicker = true else onSelected(it) },
            onDismiss = { showDialog = false }
        ) { color ->
            val isPicker = color == COLOR_PICKER

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ColorSwatch(
                    brush = when {
                        !isPicker -> SolidColor(resolve(color))
                        isHex -> SolidColor(resolve(selected))
                        else -> Brush.sweepGradient(HUE_COLORS)
                    },
                    size = 28.dp
                )
                Text(
                    text = stringResource(
                        if (isPicker) R.string.lsclock_custom_style else colorTitle(color)
                    ),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }

    if (showPicker) {
        ColorPickerDialog(
            currentColor = if (isHex) selected else resolve(selected).toHex(),
            showAlphaSlider = false,
            onDismiss = { showPicker = false },
            onSelect = onSelected
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ColorSwatch(brush: Brush, size: Dp) {
    val shape = MaterialShapes.Cookie9Sided.toShape()

    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(brush)
            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun <T> ChoiceDialog(
    title: String,
    options: List<T>,
    isSelected: (T) -> Boolean,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit,
    secondaryAction: (@Composable () -> Unit)? = null,
    label: @Composable (T) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            val listState = rememberLazyListState(
                initialFirstVisibleItemIndex = options.indexOfFirst(isSelected).coerceAtLeast(0)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .wrapContentHeight()
                        .weight(1f)
                ) {
                    items(options) { option ->
                        val select = withHaptic {
                            onSelect(option)
                            onDismiss()
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(onClick = select),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            RadioButton(selected = isSelected(option), onClick = select)
                            label(option)
                        }
                    }
                }

                ColumnScrollIndicator(listState = listState)
            }
        },
        confirmButton = {
            OutlinedButton(
                shapes = ButtonDefaults.shapes(),
                onClick = withHaptic { onDismiss() }
            ) { Text(stringResource(android.R.string.cancel)) }
        },
        dismissButton = secondaryAction
    )
}

private fun shapeOf(index: Int, count: Int): RoundedCornerShape {
    val position = when {
        count <= 1 -> ItemPosition.SOLO
        index == 0 -> ItemPosition.FIRST
        index == count - 1 -> ItemPosition.LAST
        else -> ItemPosition.MIDDLE
    }
    val corners = cardCorners(position)
    return RoundedCornerShape(
        topStart = corners.topStart,
        topEnd = corners.topEnd,
        bottomStart = corners.bottomStart,
        bottomEnd = corners.bottomEnd
    )
}

private fun ClockElementType.title(): Int = when (this) {
    ClockElementType.TIME -> R.string.lsclock_element_time
    ClockElementType.DATE -> R.string.lsclock_element_date
    ClockElementType.TEXT -> R.string.lsclock_element_text
}

private fun colorTitle(color: String): Int = when (color) {
    "" -> R.string.lsclock_font_default
    ClockCustomizer.COLOR_TEXT2 -> R.string.text_color_inverse
    ClockCustomizer.COLOR_ACCENT1 -> R.string.accent_primary
    ClockCustomizer.COLOR_ACCENT2 -> R.string.accent_secondary
    ClockCustomizer.COLOR_ACCENT3 -> R.string.accent_tertiary
    else -> R.string.text_color_primary
}

@Composable
private fun rememberFontOptions(version: Int): List<FontOption> {
    val resources = LocalResources.current
    val defaultLabel = stringResource(R.string.lsclock_font_default)
    val customLabel = stringResource(R.string.lsclock_font_custom)

    return remember(defaultLabel, customLabel, version) {
        buildList {
            add(FontOption("", defaultLabel, null))

            if (LSCLOCK_FONT_FILE.exists()) {
                try {
                    add(
                        FontOption(
                            ClockCustomizer.FONT_CUSTOM,
                            customLabel,
                            FontFamily(Typeface.createFromFile(LSCLOCK_FONT_FILE))
                        )
                    )
                } catch (_: Exception) {
                }
            }

            ClockCustomizer.importedFonts(XPOSED_RESOURCE_DIR).forEach { file ->
                val family = try {
                    FontFamily(Typeface.createFromFile(file))
                } catch (_: Exception) {
                    null
                }
                add(
                    FontOption(
                        key = file.name,
                        label = ClockCustomizer.importedFontLabel(file.name),
                        family = family,
                        isImported = true
                    )
                )
            }

            val array = resources.obtainTypedArray(R.array.lsclock_fonts)
            for (i in 0 until array.length()) {
                val id = array.getResourceId(i, 0)
                if (id == 0) continue

                val name = resources.getResourceEntryName(id)
                val family = try {
                    FontFamily(resources.getFont(id))
                } catch (_: Exception) {
                    null
                }
                val label = name.split('_').joinToString(" ") { part ->
                    part.replaceFirstChar { it.uppercase() }
                }
                add(FontOption(name, label, family))
            }
            array.recycle()
        }
    }
}
