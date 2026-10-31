package com.drdisagree.iconify.core.utils.clock

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextClock
import android.widget.TextView
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.data.common.Resources.LOCKSCREEN_CLOCK_LAYOUT
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.WeakHashMap

data class ClockElementEdit(
    val font: String = "",
    val x: Float = 0f,
    val y: Float = 0f,
    val size: Float = 0f,
    val color: String = "",
    val rotation: Float = 0f
) {
    val isEmpty: Boolean
        get() = font.isEmpty() && x == 0f && y == 0f && size == 0f && color.isEmpty() &&
                rotation == 0f
}

enum class ClockElementType { TIME, DATE, TEXT }

data class CustomClockElement(
    val type: ClockElementType,
    val value: String,
    val font: String = "",
    val size: Float = 24f,
    val color: String = ClockCustomizer.COLOR_TEXT1,
    val x: Float = 0f,
    val y: Float = 0f,
    val rotation: Float = 0f
)

object ClockCustomizer {

    const val CUSTOM_STYLE = 1000
    const val CUSTOM_LAYOUT = "preview_lockscreen_clock_custom"
    const val FONT_CUSTOM = "custom"
    private const val IMPORTED_FONT_PREFIX = "lsclock_font_"
    private const val IMPORTED_FONT_SUFFIX = ".iconify"

    const val COLOR_TEXT1 = "text1"
    const val COLOR_TEXT2 = "text2"
    const val COLOR_ACCENT1 = "accent1"
    const val COLOR_ACCENT2 = "accent2"
    const val COLOR_ACCENT3 = "accent3"

    val COLORS = listOf(COLOR_TEXT1, COLOR_TEXT2, COLOR_ACCENT1, COLOR_ACCENT2, COLOR_ACCENT3)
    val TIME_FORMATS = listOf("hh:mm", "HH:mm", "h:mm a", "hh", "mm")
    val DATE_FORMATS = listOf("EEEE, MMMM dd", "EEE, dd MMM", "EEEE", "MMMM dd", "dd/MM/yyyy")

    const val MIN_TEXT_SIZE = 10f
    const val MAX_TEXT_SIZE = 200f
    const val MAX_ROTATION = 180f

    private val originalTypefaces = WeakHashMap<TextView, Typeface>()
    private val originalTextSizes = WeakHashMap<TextView, Float>()
    private val originalTextColors = WeakHashMap<TextView, ColorStateList>()
    private val builtClocks = WeakHashMap<ViewGroup, List<CustomClockElement>>()
    private val fileTypefaces = HashMap<String, Pair<Long, Typeface>>()

    fun layoutName(style: Int): String =
        if (style == CUSTOM_STYLE) CUSTOM_LAYOUT else LOCKSCREEN_CLOCK_LAYOUT + style

    fun defaultCustomClock(): List<CustomClockElement> = listOf(
        CustomClockElement(ClockElementType.DATE, DATE_FORMATS.first(), size = 20f),
        CustomClockElement(ClockElementType.TIME, TIME_FORMATS.first(), size = 96f, y = 28f)
    )

    fun defaultSize(type: ClockElementType): Float =
        if (type == ClockElementType.TIME) 96f else 20f

    fun newElement(type: ClockElementType, value: String, y: Float): CustomClockElement =
        CustomClockElement(type = type, value = value, size = defaultSize(type), y = y)

    fun parseEdits(json: String): Map<Int, Map<Int, ClockElementEdit>> = try {
        val root = JSONObject(json)
        root.keys().asSequence().associate { style ->
            val elements = root.getJSONObject(style)
            style.toInt() to elements.keys().asSequence().associate { index ->
                val edit = elements.getJSONObject(index)
                index.toInt() to ClockElementEdit(
                    font = edit.optString("f"),
                    x = edit.optDouble("x", 0.0).toFloat(),
                    y = edit.optDouble("y", 0.0).toFloat(),
                    size = edit.optDouble("s", 0.0).toFloat().takeIf { it >= MIN_TEXT_SIZE } ?: 0f,
                    color = edit.optString("c"),
                    rotation = edit.optDouble("r", 0.0).toFloat()
                )
            }
        }
    } catch (_: Exception) {
        emptyMap()
    }

    fun serializeEdits(edits: Map<Int, Map<Int, ClockElementEdit>>): String {
        val root = JSONObject()
        edits.forEach { (style, elements) ->
            val styleJson = JSONObject()
            elements.filterValues { !it.isEmpty }.forEach { (index, edit) ->
                styleJson.put(
                    index.toString(),
                    JSONObject()
                        .put("f", edit.font)
                        .put("x", edit.x.toDouble())
                        .put("y", edit.y.toDouble())
                        .put("s", edit.size.toDouble())
                        .put("c", edit.color)
                        .put("r", edit.rotation.toDouble())
                )
            }
            if (styleJson.length() > 0) root.put(style.toString(), styleJson)
        }
        return root.toString()
    }

    fun parseCustomClock(json: String): List<CustomClockElement> {
        if (json.isEmpty()) return defaultCustomClock()

        return try {
            val array = JSONArray(json)
            List(array.length()) { i ->
                val item = array.getJSONObject(i)
                CustomClockElement(
                    type = ClockElementType.valueOf(item.getString("t")),
                    value = item.optString("v"),
                    font = item.optString("f"),
                    size = item.optDouble("s", 24.0).toFloat(),
                    color = item.optString("c", COLOR_TEXT1),
                    x = item.optDouble("x", 0.0).toFloat(),
                    y = item.optDouble("y", 0.0).toFloat(),
                    rotation = item.optDouble("r", 0.0).toFloat()
                )
            }
        } catch (_: Exception) {
            defaultCustomClock()
        }
    }

    fun serializeCustomClock(elements: List<CustomClockElement>): String {
        val array = JSONArray()
        elements.forEach { element ->
            array.put(
                JSONObject()
                    .put("t", element.type.name)
                    .put("v", element.value)
                    .put("f", element.font)
                    .put("s", element.size.toDouble())
                    .put("c", element.color)
                    .put("x", element.x.toDouble())
                    .put("y", element.y.toDouble())
                    .put("r", element.rotation.toDouble())
            )
        }
        return array.toString()
    }

    fun customEdits(elements: List<CustomClockElement>): Map<Int, ClockElementEdit> =
        elements.withIndex().associate { (index, element) ->
            index to ClockElementEdit(font = element.font, x = element.x, rotation = element.rotation)
        }

    fun textViews(root: View): List<TextView> {
        val result = mutableListOf<TextView>()

        fun collect(view: View) {
            if (view is TextView) result.add(view)
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) collect(view.getChildAt(i))
            }
        }

        collect(root)
        return result
    }

    fun isImportedFont(font: String): Boolean =
        font.startsWith(IMPORTED_FONT_PREFIX) && font.endsWith(IMPORTED_FONT_SUFFIX)

    fun importedFontFileName(displayName: String): String {
        val base = displayName.substringBeforeLast('.')
            .replace(Regex("[^A-Za-z0-9 _-]"), "_")
            .trim()
            .ifEmpty { System.currentTimeMillis().toString() }
        return IMPORTED_FONT_PREFIX + base + IMPORTED_FONT_SUFFIX
    }

    fun importedFontLabel(fileName: String): String =
        fileName.removePrefix(IMPORTED_FONT_PREFIX).removeSuffix(IMPORTED_FONT_SUFFIX)

    fun importedFonts(dir: File): List<File> = dir.listFiles { file ->
        file.isFile && isImportedFont(file.name)
    }?.sortedBy { it.name.lowercase() }.orEmpty()

    private fun fileTypeface(file: File): Typeface? {
        if (!file.exists()) return null

        val key = file.absolutePath
        val modified = file.lastModified()
        fileTypefaces[key]?.takeIf { it.first == modified }?.let { return it.second }

        return Typeface.createFromFile(file).also { fileTypefaces[key] = modified to it }
    }

    @SuppressLint("DiscouragedApi")
    fun resolveTypeface(context: Context, font: String, customFontFile: File?): Typeface? {
        if (font.isEmpty()) return null

        return try {
            if (font == FONT_CUSTOM) {
                customFontFile?.let { fileTypeface(it) }
            } else if (isImportedFont(font)) {
                customFontFile?.parentFile?.let { fileTypeface(File(it, font)) }
            } else {
                val id = context.resources.getIdentifier(font, "font", BuildConfig.APPLICATION_ID)
                if (id != 0) context.resources.getFont(id) else null
            }
        } catch (_: Exception) {
            null
        }
    }

    fun applyEdits(
        root: View,
        edits: Map<Int, ClockElementEdit>,
        fontContext: Context,
        customFontFile: File?,
        textScale: Float = 1f,
        colorOf: (String) -> Int = { defaultColor(root.context, it) }
    ) {
        val density = root.resources.displayMetrics.density

        textViews(root).forEachIndexed { index, textView ->
            val original = originalTypefaces.getOrPut(textView) {
                textView.typeface ?: Typeface.DEFAULT
            }
            val edit = edits[index]

            textView.typeface = edit?.font
                ?.let { resolveTypeface(fontContext, it, customFontFile) }
                ?: original
            textView.translationX = (edit?.x ?: 0f) * density
            textView.rotation = edit?.rotation ?: 0f
            if (!isCustomElement(textView)) textView.translationY = (edit?.y ?: 0f) * density

            val size = edit?.size ?: 0f
            if (size > 0f || originalTextSizes.containsKey(textView)) {
                val baseSize = originalTextSizes.getOrPut(textView) { textView.textSize }
                val targetSize = if (size > 0f) spToPx(textView, size) * textScale else baseSize * textScale
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, targetSize)
            }

            val color = edit?.color.orEmpty()
            if (color.isNotEmpty()) {
                originalTextColors.getOrPut(textView) { textView.textColors }
                textView.setTextColor(if (isHexColor(color)) parseHex(color) else colorOf(color))
            } else {
                originalTextColors.remove(textView)?.let { textView.setTextColor(it) }
            }

            if (edit != null && (edit.x != 0f || edit.y != 0f || edit.rotation != 0f)) {
                disableClipping(textView, root)
            }
        }
    }

    fun buildCustomClock(
        root: ViewGroup,
        elements: List<CustomClockElement>,
        fontContext: Context,
        customFontFile: File?
    ) {
        if (builtClocks[root] == elements && root.childCount == elements.size) return

        val reusable = root.childCount == elements.size &&
                elements.withIndex().all { (index, element) ->
                    (root.getChildAt(index) is TextClock) == (element.type != ClockElementType.TEXT)
                }

        if (!reusable) {
            root.removeAllViews()
            elements.forEach { element ->
                root.addView(
                    when (element.type) {
                        ClockElementType.TEXT -> TextView(root.context)
                        else -> TextClock(root.context)
                    }
                )
            }
        }

        elements.forEachIndexed { index, element ->
            val view = root.getChildAt(index) as TextView
            bindElementView(view, element)
            view.typeface = resolveTypeface(fontContext, element.font, customFontFile)
                ?: Typeface.DEFAULT
        }
        builtClocks[root] = elements
    }

    fun moveElement(view: View, isCustom: Boolean, x: Float, y: Float) {
        val density = view.resources.displayMetrics.density

        view.translationX = x * density
        if (isCustom) {
            (view.layoutParams as? ViewGroup.MarginLayoutParams)?.let { params ->
                params.topMargin = (y * density).toInt()
                view.layoutParams = params
            }
        } else {
            view.translationY = y * density
        }
    }

    fun originalTextSizeSp(view: TextView): Float = TypedValue.deriveDimension(
        TypedValue.COMPLEX_UNIT_SP,
        originalTextSizes[view] ?: view.textSize,
        view.resources.displayMetrics
    )

    fun originalTextColor(view: TextView): Int =
        (originalTextColors[view] ?: view.textColors).defaultColor

    fun isHexColor(color: String): Boolean = color.startsWith("#")

    fun defaultColor(context: Context, color: String): Int = when (color) {
        "" -> Color.WHITE
        COLOR_TEXT2 -> Color.BLACK
        COLOR_ACCENT1 -> context.getColor(android.R.color.system_accent1_100)
        COLOR_ACCENT2 -> context.getColor(android.R.color.system_accent2_100)
        COLOR_ACCENT3 -> context.getColor(android.R.color.system_accent3_100)
        else -> if (isHexColor(color)) parseHex(color) else Color.WHITE
    }

    private fun parseHex(color: String): Int = try {
        Color.parseColor(color)
    } catch (_: IllegalArgumentException) {
        Color.WHITE
    }

    private fun spToPx(view: View, sp: Float): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, view.resources.displayMetrics)

    private fun bindElementView(view: TextView, element: CustomClockElement) {
        val context = view.context
        val density = context.resources.displayMetrics.density

        originalTypefaces.remove(view)
        originalTextSizes.remove(view)
        originalTextColors.remove(view)

        if (view is TextClock) {
            if (view.format12Hour?.toString() != element.value) view.format12Hour = element.value
            if (view.format24Hour?.toString() != element.value) view.format24Hour = element.value
        } else if (view.text?.toString() != element.value) {
            view.text = element.value
        }

        view.apply {
            tag = element.color
            includeFontPadding = false
            isSingleLine = true
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, element.size)
            setTextColor(defaultColor(context, element.color))
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply { topMargin = (element.y * density).toInt() }
            translationX = element.x * density
            rotation = element.rotation
        }
    }

    private fun isCustomElement(view: View): Boolean =
        (view.parent as? ViewGroup)?.let { builtClocks.containsKey(it) } == true

    private fun disableClipping(view: View, root: View) {
        var parent = view.parent as? ViewGroup
        while (parent != null) {
            parent.clipChildren = false
            parent.clipToPadding = false
            if (parent === root) {
                (parent.parent as? ViewGroup)?.apply {
                    clipChildren = false
                    clipToPadding = false
                }
                break
            }
            parent = parent.parent as? ViewGroup
        }
    }
}
