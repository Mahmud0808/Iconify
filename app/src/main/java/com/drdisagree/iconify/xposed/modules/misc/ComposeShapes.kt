package com.drdisagree.iconify.xposed.modules.misc

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getStaticFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

class ComposeShapes(context: Context) : ModPack(context) {

    private var qsCornerRadiusOffset = 0f
    private var notificationCornerRadiusOffset = 0f
    private var qsSliderStyle = SliderStyle()
    private var volumeSliderStyle = SliderStyle()
    private var pillShapeCache: Pair<Float, Any>? = null
    private val shapeParamIndices = ConcurrentHashMap<Member, Int>()

    private var circleShape: Any? = null

    private val backgroundModifier: Method? by lazy {
        ComposeToolkit.modifierFunction(
            "androidx.compose.foundation.BackgroundKt",
            "background",
            Long::class.javaPrimitiveType,
            null
        )
    }

    private val clipModifier: Method? by lazy {
        ComposeToolkit.modifierFunction("androidx.compose.ui.draw.ClipKt", "clip", null)
    }

    private val roundedCornerShape: Method? by lazy {
        ComposeToolkit.staticMethod(
            ROUNDED_CORNER_SHAPE_KT_CLASS,
            "RoundedCornerShape/4f"
        ) { method ->
            method.name.startsWith("RoundedCornerShape-") &&
                    method.parameterTypes.size == 4 &&
                    method.parameterTypes.all { it == Float::class.javaPrimitiveType }
        }
    }

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            qsCornerRadiusOffset = getFloat(XposedKey.QS_CORNER_RADIUS_OFFSET)
            notificationCornerRadiusOffset = getFloat(XposedKey.NOTIFICATION_CORNER_RADIUS_OFFSET)
            qsSliderStyle = SliderStyle(
                cornerRadiusOffset = qsCornerRadiusOffset,
                hideThumb = getBoolean(XposedKey.HIDE_QS_SLIDER_THUMB)
            )
            volumeSliderStyle = SliderStyle(
                cornerRadiusOffset = getFloat(XposedKey.VOLUME_CORNER_RADIUS_OFFSET),
                hideThumb = getBoolean(XposedKey.HIDE_VOLUME_SLIDER_THUMB)
            )
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        hookDimenOffsets()
        hookTileCornerRadius()
        hookSliderTrack()
        hookSliderThumb()
        hookNotificationFooterButtons()
        hookQsCircleButtons()
    }

    private fun hookQsCircleButtons() {
        circleShape = findClass(ROUNDED_CORNER_SHAPE_KT_CLASS, suppressError = true)
            .getStaticFieldSilently("CircleShape")
            ?: return

        CIRCLE_SHAPE_CONSUMERS.forEach { (className, methodPattern) ->
            findClass(className, suppressError = true)
                .hookMethodMatchPattern(methodPattern)
                .suppressError()
                .runBefore { param -> param.replaceQsButtonCircleShape() }
        }
    }

    private fun MethodHookParam.replaceQsButtonCircleShape() {
        if (qsCornerRadiusOffset >= 0f) return

        val shapeIndex = shapeParamIndices.getOrPut(method) {
            (method as Method).parameterTypes.indexOfFirst { it.name == SHAPE_CLASS }
        }
        val shape = circleShape ?: return
        if (shapeIndex == -1 || args[shapeIndex] !== shape) return

        val isQsButton = Thread.currentThread().stackTrace.any { frame ->
            CIRCLE_SHAPE_OWNERS.any { frame.className.startsWith(it) }
        }
        if (!isQsButton) return

        args[shapeIndex] = pillShape(qsCornerRadiusOffset) ?: return
    }

    private fun pillShape(offset: Float): Any? {
        pillShapeCache?.takeIf { it.first == offset }?.let { return it.second }

        val shape = try {
            val cornerSizeClass = ComposeToolkit.loadClass(CORNER_SIZE_CLASS) ?: return null
            val cornerSize = Proxy.newProxyInstance(
                cornerSizeClass.classLoader,
                arrayOf(cornerSizeClass)
            ) { proxy, method, args ->
                when {
                    method.name.startsWith("toPx") -> {
                        val size = args[0] as Long
                        val width = Float.fromBits((size ushr 32).toInt())
                        val height = Float.fromBits(size.toInt())
                        val density = args[1]?.callMethodSilently("getDensity") as? Float ?: 1f
                        val maxRadius = minOf(width, height) / 2f
                        (maxRadius + offset * density).coerceIn(0f, maxRadius)
                    }

                    method.name == "equals" -> proxy === args?.firstOrNull()
                    method.name == "hashCode" -> System.identityHashCode(proxy)
                    method.name == "toString" -> "IconifyPillCornerSize"
                    else -> null
                }
            }

            ComposeToolkit.loadClass(ROUNDED_CORNER_SHAPE_CLASS)
                ?.constructors
                ?.firstOrNull { constructor ->
                    constructor.parameterTypes.size == 4 &&
                            constructor.parameterTypes.all { it == cornerSizeClass }
                }
                ?.newInstance(cornerSize, cornerSize, cornerSize, cornerSize)
        } catch (_: Throwable) {
            null
        } ?: return null

        pillShapeCache = offset to shape
        return shape
    }

    private fun hookDimenOffsets() {
        val dimenHook = ResourceHookManager
            .hookDimen()
            .forPackageName(SYSTEMUI_PACKAGE)

        QS_RADIUS_DIMENS.forEach { dimenHook.addOffset(it) { qsCornerRadiusOffset } }
        NOTIFICATION_RADIUS_DIMENS.forEach {
            dimenHook.addOffset(it) { notificationCornerRadiusOffset }
        }
        VOLUME_RADIUS_DIMENS.forEach {
            dimenHook.addOffset(it) { volumeSliderStyle.cornerRadiusOffset }
        }

        dimenHook.apply()
    }

    private fun hookNotificationFooterButtons() {
        findClass(*FOOTER_VIEW_CLASSES, suppressError = true)
            .hookMethodMatchPattern("updateColors.*")
            .suppressError()
            .runAfter { param ->
                if (notificationCornerRadiusOffset == 0f) return@runAfter

                val offsetPx =
                    notificationCornerRadiusOffset * mContext.resources.displayMetrics.density
                FOOTER_BUTTON_FIELDS.forEach { field ->
                    (param.thisObject.getFieldSilently(field) as? View)
                        ?.background
                        ?.offsetCorners(offsetPx)
                }
            }
    }

    private fun Drawable.offsetCorners(offsetPx: Float) {
        when (this) {
            is LayerDrawable -> (0 until numberOfLayers).forEach {
                getDrawable(it)?.offsetCorners(offsetPx)
            }

            is DrawableWrapper -> drawable?.offsetCorners(offsetPx)

            is GradientDrawable -> try {
                mutate()
                val radii = cornerRadii
                if (radii != null) {
                    cornerRadii = FloatArray(radii.size) { index ->
                        radii[index].let { if (it > 0f) (it + offsetPx).coerceAtLeast(0f) else it }
                    }
                } else if (cornerRadius > 0f) {
                    cornerRadius = (cornerRadius + offsetPx).coerceAtLeast(0f)
                }
            } catch (_: Throwable) {
            }
        }
    }

    private fun staticDp(className: String, fieldName: String): Float? =
        findClass(className, suppressError = true)
            .getStaticFieldSilently(fieldName) as? Float

    private fun hookTileCornerRadius() {
        val inactiveCornerRadius = staticDp(COMMON_TILE_DEFAULTS_CLASS, "InactiveCornerRadius")

        findClass(TILE_DEFAULTS_CLASS, suppressError = true)
            .hookMethodMatchPattern("animateShapeAsState(-.*)?")
            .suppressError()
            .runBefore { param ->
                if (qsCornerRadiusOffset == 0f) return@runBefore

                val types = (param.method as Method).parameterTypes
                val radiusIndex = types.indexOfFirst { it == Float::class.javaPrimitiveType }
                if (radiusIndex == -1) return@runBefore
                val radius = param.args[radiusIndex] as? Float ?: return@runBefore

                val stateIndex = (radiusIndex - 1).takeIf {
                    it >= 0 && types[it] == Int::class.javaPrimitiveType
                } ?: return@runBefore

                val modeIndex = (radiusIndex + 2).takeIf {
                    it < types.size &&
                            types[radiusIndex + 1] == String::class.java &&
                            types[it] == Int::class.javaPrimitiveType
                }

                val usesRadius = when (modeIndex?.let { param.args[it] as? Int }) {
                    MODE_ACTIVE_RADIUS -> true
                    MODE_INACTIVE_RADIUS, MODE_INACTIVE_RADIUS_ALT -> false
                    else -> param.args[stateIndex] == STATE_ACTIVE
                }
                val defaultRadius = if (usesRadius) {
                    radius
                } else {
                    inactiveCornerRadius ?: return@runBefore
                }

                modeIndex?.let { param.args[it] = MODE_ACTIVE_RADIUS }
                param.args[stateIndex] = STATE_ACTIVE
                param.args[radiusIndex] = defaultRadius.withOffset(qsCornerRadiusOffset)
            }
    }

    private fun hookSliderTrack() {
        findClass(SLIDER_DEFAULTS_CLASS, suppressError = true)
            .hookMethodMatchPattern("Track(-.*)?")
            .suppressError()
            .runBefore { param ->
                val style = currentSliderStyle() ?: return@runBefore

                val method = param.method as Method
                val floatIndices = method.parameterTypes.indices.filter {
                    method.parameterTypes[it] == Float::class.javaPrimitiveType
                }
                if (floatIndices.size < 2) return@runBefore

                val gapIndex = floatIndices[floatIndices.size - 2]
                val insideCornerIndex = floatIndices.last()
                val cornerIndex = floatIndices.firstOrNull { it < gapIndex }

                if (style.cornerRadiusOffset != 0f) {
                    cornerIndex?.let { param.offsetDpArg(method, it, style.cornerRadiusOffset) }
                }

                val trackCornerSize = cornerIndex
                    ?.takeUnless { ComposeToolkit.isDefaultArg(method, param.args, it) }
                    ?.let { param.args[it] as? Float }
                    ?.takeUnless { it.isNaN() }

                if (style.hideThumb && trackCornerSize != null &&
                    param.fillInactiveTrack(method, trackCornerSize)
                ) {
                    param.args[gapIndex] = 0f
                    param.args[insideCornerIndex] = trackCornerSize
                    ComposeToolkit.clearDefaultBit(method, param.args, gapIndex)
                    ComposeToolkit.clearDefaultBit(method, param.args, insideCornerIndex)
                } else if (style.cornerRadiusOffset != 0f) {
                    param.offsetDpArg(method, insideCornerIndex, style.cornerRadiusOffset)
                }
            }
    }

    private fun hookSliderThumb() {
        findClass(SLIDER_DEFAULTS_CLASS, suppressError = true)
            .hookMethodMatchPattern("Thumb(-.*)?")
            .suppressError()
            .runBefore { param ->
                if (currentSliderStyle()?.hideThumb != true) return@runBefore

                val method = param.method as Method
                val sizeIndex = method.parameterTypes.indexOfFirst {
                    it == Long::class.javaPrimitiveType
                }
                if (sizeIndex == -1) return@runBefore

                param.args[sizeIndex] = 0L
                ComposeToolkit.clearDefaultBit(method, param.args, sizeIndex)
            }
    }

    private fun MethodHookParam.fillInactiveTrack(method: Method, cornerSize: Float): Boolean {
        val types = method.parameterTypes
        val colorsIndex = types.indexOfFirst { it.name == SLIDER_COLORS_CLASS }
        val modifierIndex = ComposeToolkit.parameterIndex(method, ComposeToolkit.MODIFIER_CLASS)
        if (colorsIndex == -1 || modifierIndex == -1) return false

        val colors = args[colorsIndex] ?: return false
        val enabledIndex = types.indexOfFirst { it == Boolean::class.javaPrimitiveType }
        val enabled = enabledIndex == -1 ||
                ComposeToolkit.isDefaultArg(method, args, enabledIndex) ||
                args[enabledIndex] == true
        val color = colors.getFieldSilently(
            if (enabled) "inactiveTrackColor" else "disabledInactiveTrackColor"
        ) as? Long ?: return false

        val shape = roundedCornerShape?.invoke(
            null, cornerSize, cornerSize, cornerSize, cornerSize
        ) ?: return false
        val background = backgroundModifier?.invoke(
            null, ComposeToolkit.emptyModifier, color, shape
        ) ?: return false
        val clip = clipModifier?.invoke(null, ComposeToolkit.emptyModifier, shape) ?: return false

        val transparentInactiveColors = colors.withTransparentInactiveTrack() ?: return false

        val original = args[modifierIndex]
            ?.takeUnless { ComposeToolkit.isDefaultArg(method, args, modifierIndex) }
            ?: ComposeToolkit.emptyModifier
            ?: return false
        val clippedBackground = ComposeToolkit.then(background, clip) ?: return false
        args[modifierIndex] = ComposeToolkit.then(clippedBackground, original) ?: return false
        ComposeToolkit.clearDefaultBit(method, args, modifierIndex)
        args[colorsIndex] = transparentInactiveColors
        return true
    }

    private fun Any.withTransparentInactiveTrack(): Any? = try {
        val values = SLIDER_COLOR_FIELDS.map { field ->
            if (field in TRANSPARENT_SLIDER_COLOR_FIELDS) {
                0L
            } else {
                getFieldSilently(field) as? Long ?: return null
            }
        }
        javaClass.declaredConstructors
            .firstOrNull { constructor ->
                constructor.parameterTypes.size == SLIDER_COLOR_FIELDS.size &&
                        constructor.parameterTypes.all { it == Long::class.javaPrimitiveType }
            }
            ?.apply { isAccessible = true }
            ?.newInstance(*values.toTypedArray())
    } catch (_: Throwable) {
        null
    }

    private fun currentSliderStyle(): SliderStyle? {
        if (!qsSliderStyle.isActive && !volumeSliderStyle.isActive) return null

        val caller = Thread.currentThread().stackTrace.firstOrNull {
            it.className.startsWith("$SYSTEMUI_PACKAGE.")
        }?.className ?: return null

        return when {
            caller.startsWith(BRIGHTNESS_PACKAGE) -> qsSliderStyle
            caller.contains(VOLUME_PACKAGE_SEGMENT) -> volumeSliderStyle
            else -> null
        }?.takeIf { it.isActive }
    }

    private fun MethodHookParam.offsetDpArg(method: Method, index: Int, offset: Float) {
        if (ComposeToolkit.isDefaultArg(method, args, index)) return
        val value = args[index] as? Float ?: return
        if (value.isNaN()) return
        args[index] = value.withOffset(offset)
    }

    private fun Float.withOffset(offset: Float): Float = (this + offset).coerceAtLeast(0f)

    private data class SliderStyle(
        val cornerRadiusOffset: Float = 0f,
        val hideThumb: Boolean = false
    ) {
        val isActive: Boolean
            get() = cornerRadiusOffset != 0f || hideThumb
    }

    companion object {
        private const val TILE_DEFAULTS_CLASS =
            "$SYSTEMUI_PACKAGE.qs.panels.ui.compose.infinitegrid.TileDefaults"
        private const val COMMON_TILE_DEFAULTS_CLASS =
            "$SYSTEMUI_PACKAGE.qs.panels.ui.compose.infinitegrid.CommonTileDefaults"
        private const val BRIGHTNESS_PACKAGE = "$SYSTEMUI_PACKAGE.brightness."
        private const val VOLUME_PACKAGE_SEGMENT = ".volume."
        private const val ROUNDED_CORNER_SHAPE_KT_CLASS =
            "androidx.compose.foundation.shape.RoundedCornerShapeKt"
        private val FOOTER_VIEW_CLASSES = arrayOf(
            "$SYSTEMUI_PACKAGE.statusbar.notification.footer.ui.view.FooterView",
            "$SYSTEMUI_PACKAGE.statusbar.notification.row.FooterView"
        )
        private val FOOTER_BUTTON_FIELDS =
            listOf("mClearAllButton", "mHistoryButton", "mSettingsButton")
        private val NOTIFICATION_RADIUS_DIMENS = listOf(
            "notification_corner_radius",
            "notification_scrim_corner_radius"
        )
        private val QS_RADIUS_DIMENS = listOf(
            "common_tile_default_active_tile_corner_radius",
            "common_tile_default_inactive_tile_corner_radius",
            "common_tile_default_active_icon_corner_radius",
            "common_tile_default_inactive_icon_corner_radius"
        )
        private val VOLUME_RADIUS_DIMENS = listOf(
            "volume_dialog_background_corner_radius",
            "volume_dialog_background_square_corner_radius",
            "volume_dialog_floating_slider_background_corner_radius",
            "volume_dialog_horizontal_background_corner_radius",
            "volume_dialog_ringer_selected_button_background_radius"
        )
        private const val ROUNDED_CORNER_SHAPE_CLASS =
            "androidx.compose.foundation.shape.RoundedCornerShape"
        private const val CORNER_SIZE_CLASS = "androidx.compose.foundation.shape.CornerSize"
        private const val SHAPE_CLASS = "androidx.compose.ui.graphics.Shape"
        private val CIRCLE_SHAPE_OWNERS = listOf(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.compose.FooterActionsKt",
            "$SYSTEMUI_PACKAGE.qs.panels.ui.compose.toolbar."
        )
        private val CIRCLE_SHAPE_CONSUMERS = listOf(
            "androidx.compose.ui.draw.ClipKt" to "clip",
            "androidx.compose.foundation.BackgroundKt" to "background(-.*)?",
            "com.android.compose.animation.ExpandableKt" to "Expandable(-.*)?",
            "com.android.compose.animation.ExpandableControllerKt" to
                    "rememberExpandableController(-.*)?"
        )
        private const val SLIDER_DEFAULTS_CLASS = "androidx.compose.material3.SliderDefaults"
        private const val SLIDER_COLORS_CLASS = "androidx.compose.material3.SliderColors"
        private val SLIDER_COLOR_FIELDS = listOf(
            "thumbColor",
            "activeTrackColor",
            "activeTickColor",
            "inactiveTrackColor",
            "inactiveTickColor",
            "disabledThumbColor",
            "disabledActiveTrackColor",
            "disabledActiveTickColor",
            "disabledInactiveTrackColor",
            "disabledInactiveTickColor"
        )
        private val TRANSPARENT_SLIDER_COLOR_FIELDS =
            setOf("inactiveTrackColor", "disabledInactiveTrackColor")
        private const val STATE_ACTIVE = 2
        private const val MODE_INACTIVE_RADIUS = 1
        private const val MODE_ACTIVE_RADIUS = 2
        private const val MODE_INACTIVE_RADIUS_ALT = 3
    }
}
