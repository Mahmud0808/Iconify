package com.drdisagree.iconify.xposed.modules.quicksettings

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Shader
import android.os.Handler
import android.os.Looper
import androidx.core.graphics.toColorInt
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.GraphicsColorKt
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.lang.reflect.Proxy
import java.util.Collections
import java.util.WeakHashMap

class QSTheme(context: Context) : ModPack(context) {

    private var customQsTheme = false

    private var activeBgColor = "#FFFFFF"
    private var activeIconColor = "#FFFFFF"
    private var activeIconBgColor = "#FFFFFF"
    private var activeLabelColor = "#FFFFFF"
    private var activeSecondaryLabelColor = "#FFFFFF"

    private var inactiveBgColor = "#FFFFFF"
    private var inactiveIconColor = "#FFFFFF"
    private var inactiveIconBgColor = "#FFFFFF"
    private var inactiveLabelColor = "#FFFFFF"
    private var inactiveSecondaryLabelColor = "#FFFFFF"

    private var unavailableBgColor = "#FFFFFF"
    private var unavailableIconColor = "#FFFFFF"
    private var unavailableIconBgColor = "#FFFFFF"
    private var unavailableLabelColor = "#FFFFFF"
    private var unavailableSecondaryLabelColor = "#FFFFFF"

    private var activeTileGradient = false
    private var footerInactiveButtonBgColor = "#FFFFFF"
    private var footerInactiveButtonIconColor = "#FFFFFF"
    private var footerActiveButtonBgColor = "#FFFFFF"
    private var footerActiveButtonIconColor = "#FFFFFF"
    private var footerChipBgColor = "#FFFFFF"
    private var footerChipContentColor = "#FFFFFF"
    private val footerButtonTypes = ArrayDeque<String>()
    private var footerGradient = false
    private var footerGradientEndColor = "#FFFFFF"
    private val footerModifierToOriginal = WeakHashMap<Any, Any>()
    private var footerButtonHeightDp: Float? = null
    private var activeTileGradientEndColor = "#FFFFFF"
    private var brightnessActiveColor = "#FFFFFF"
    private var brightnessInactiveColor = "#FFFFFF"
    private var brightnessThumbColor = "#FFFFFF"
    private var brightnessGradient = false
    private var brightnessGradientEndColor = "#FFFFFF"

    private var sliderColorsConstructor: Constructor<*>? = null
    private var drawWithContentMethod: Method? = null
    private var function1Class: Class<*>? = null
    private var kotlinUnit: Any? = null
    private val themedSliderColors = WeakHashMap<Any, Boolean>()
    private val gradientDrawers = WeakHashMap<Any, Any>()
    private val gradientPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
    }
    private val tileGradientPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var drawBehindMethod: Method? = null
    private var toArgbMethod: Method? = null
    private val tileModifierToOriginal = WeakHashMap<Any, Any>()
    private val tileRecomposeScopes = Collections.newSetFromMap(WeakHashMap<Any, Boolean>())
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            customQsTheme = getBoolean(XposedKey.CUSTOM_QS_THEME)

            activeBgColor = getString(XposedKey.ACTIVE_QS_TILE_BACKGROUND_COLOR)
            activeIconColor = getString(XposedKey.ACTIVE_QS_TILE_ICON_COLOR)
            activeIconBgColor = getString(XposedKey.ACTIVE_QS_TILE_ICON_BACKGROUND_COLOR)
            activeLabelColor = getString(XposedKey.ACTIVE_QS_TILE_LABEL_COLOR)
            activeSecondaryLabelColor = getString(XposedKey.ACTIVE_QS_TILE_SECONDARY_LABEL_COLOR)

            inactiveBgColor = getString(XposedKey.INACTIVE_QS_TILE_BACKGROUND_COLOR)
            inactiveIconColor = getString(XposedKey.INACTIVE_QS_TILE_ICON_COLOR)
            inactiveIconBgColor = getString(XposedKey.INACTIVE_QS_TILE_ICON_BACKGROUND_COLOR)
            inactiveLabelColor = getString(XposedKey.INACTIVE_QS_TILE_LABEL_COLOR)
            inactiveSecondaryLabelColor =
                getString(XposedKey.INACTIVE_QS_TILE_SECONDARY_LABEL_COLOR)

            unavailableBgColor = getString(XposedKey.UNAVAILABLE_QS_TILE_BACKGROUND_COLOR)
            unavailableIconColor = getString(XposedKey.UNAVAILABLE_QS_TILE_ICON_COLOR)
            unavailableIconBgColor = getString(XposedKey.UNAVAILABLE_QS_TILE_ICON_BACKGROUND_COLOR)
            unavailableLabelColor = getString(XposedKey.UNAVAILABLE_QS_TILE_LABEL_COLOR)
            unavailableSecondaryLabelColor =
                getString(XposedKey.UNAVAILABLE_QS_TILE_SECONDARY_LABEL_COLOR)

            activeTileGradient = getBoolean(XposedKey.ACTIVE_QS_TILE_GRADIENT)
            footerInactiveButtonBgColor = getString(XposedKey.QS_FOOTER_INACTIVE_BUTTON_BACKGROUND_COLOR)
            footerInactiveButtonIconColor = getString(XposedKey.QS_FOOTER_INACTIVE_BUTTON_ICON_COLOR)
            footerActiveButtonBgColor = getString(XposedKey.QS_FOOTER_ACTIVE_BUTTON_BACKGROUND_COLOR)
            footerActiveButtonIconColor = getString(XposedKey.QS_FOOTER_ACTIVE_BUTTON_ICON_COLOR)
            footerChipBgColor = getString(XposedKey.QS_FOOTER_CHIP_BACKGROUND_COLOR)
            footerChipContentColor = getString(XposedKey.QS_FOOTER_CHIP_CONTENT_COLOR)
            footerGradient = getBoolean(XposedKey.QS_FOOTER_GRADIENT)
            footerGradientEndColor = getString(XposedKey.QS_FOOTER_GRADIENT_END_COLOR)
            activeTileGradientEndColor = getString(XposedKey.ACTIVE_QS_TILE_GRADIENT_END_COLOR)
            brightnessActiveColor = getString(XposedKey.BRIGHTNESS_SLIDER_ACTIVE_COLOR)
            brightnessInactiveColor = getString(XposedKey.BRIGHTNESS_SLIDER_INACTIVE_COLOR)
            brightnessThumbColor = getString(XposedKey.BRIGHTNESS_SLIDER_THUMB_COLOR)
            brightnessGradient = getBoolean(XposedKey.BRIGHTNESS_SLIDER_GRADIENT)
            brightnessGradientEndColor = getString(XposedKey.BRIGHTNESS_SLIDER_GRADIENT_END_COLOR)
        }

        if (key.any { it in TILE_THEME_KEYS }) {
            mainHandler.post {
                tileRecomposeScopes.toList().forEach {
                    it.callMethodSilently("invalidateForResult", null)
                }
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val tileDefaultsClass =
            findClass("$SYSTEMUI_PACKAGE.qs.panels.ui.compose.infinitegrid.TileDefaults")

        function1Class = findClass("kotlin.jvm.functions.Function1", suppressError = true)
        kotlinUnit = try {
            findClass("kotlin.Unit", suppressError = true)?.getField("INSTANCE")?.get(null)
        } catch (_: Throwable) {
            null
        }

        hookBrightnessSlider()
        hookTileGradient()
        hookFooterActions()

        tileDefaultsClass
            .hookMethod("getColorForState")
            .runAfter { param ->
                captureRecomposeScope(param)

                if (!customQsTheme) return@runAfter

                val tileUiState = param.args[0]
                val state = tileUiState.getAnyField("visualState", "state") as? Int
                val isDualTarget = if (param.args[1] is Boolean) {
                    val iconOnly = param.args[1] as Boolean
                    val handlesSecondaryClick = tileUiState.getAnyField(
                        "handlesSecondaryClick",
                        "handlesToggleClick"
                    ) as Boolean

                    handlesSecondaryClick && !iconOnly
                } else {
                    true
                }

                param.result.apply {
                    when (state) {
                        STATE_ACTIVE -> {
                            val backgroundColor =
                                (if (isDualTarget) activeBgColor else activeIconBgColor).toColorInt()
                            setField(
                                "background",
                                GraphicsColorKt.colorOf(
                                    if (activeTileGradient) {
                                        backgroundColor and 0x00FFFFFF
                                    } else {
                                        backgroundColor
                                    }
                                )
                            )
                            setField(
                                "icon",
                                GraphicsColorKt.colorOf(activeIconColor)
                            )
                            setField(
                                "iconBackground",
                                GraphicsColorKt.colorOf(activeIconBgColor)
                            )
                            setField(
                                "label",
                                GraphicsColorKt.colorOf(activeLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                GraphicsColorKt.colorOf(activeSecondaryLabelColor)
                            )
                        }

                        STATE_INACTIVE -> {
                            setField(
                                "background",
                                GraphicsColorKt.colorOf(inactiveBgColor)
                            )
                            setField(
                                "icon",
                                GraphicsColorKt.colorOf(inactiveIconColor)
                            )
                            setField(
                                "iconBackground",
                                GraphicsColorKt.colorOf(inactiveIconBgColor)
                            )
                            setField(
                                "label",
                                GraphicsColorKt.colorOf(inactiveLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                GraphicsColorKt.colorOf(inactiveSecondaryLabelColor)
                            )
                        }

                        STATE_UNAVAILABLE -> {
                            setField(
                                "background",
                                GraphicsColorKt.colorOf(unavailableBgColor)
                            )
                            setField(
                                "icon",
                                GraphicsColorKt.colorOf(unavailableIconColor)
                            )
                            setField(
                                "iconBackground",
                                GraphicsColorKt.colorOf(unavailableIconBgColor)
                            )
                            setField(
                                "label",
                                GraphicsColorKt.colorOf(unavailableLabelColor)
                            )
                            setField(
                                "secondaryLabel",
                                GraphicsColorKt.colorOf(unavailableSecondaryLabelColor)
                            )
                        }

                        else -> log(this@QSTheme, "Unknown state: $state")
                    }

                    setFieldSilently("iconBackgroundGradient", null)
                }
            }
    }

    private fun hookBrightnessSlider() {
        sliderColorsConstructor = findClass(
            SLIDER_COLORS_CLASS,
            suppressError = true
        )?.declaredConstructors?.firstOrNull { constructor ->
            constructor.parameterTypes.size == SLIDER_COLOR_FIELDS.size &&
                    constructor.parameterTypes.all { it == Long::class.javaPrimitiveType }
        } ?: return

        drawWithContentMethod = findClass(
            "androidx.compose.ui.draw.DrawModifierKt",
            suppressError = true
        )?.declaredMethods?.firstOrNull { method ->
            method.name == "drawWithContent" && method.parameterTypes.size == 2
        }
        findClass("androidx.compose.material3.SliderDefaults", suppressError = true)
            .hookMethodMatchPattern("(Track|Thumb).*")
            .suppressError()
            .runBefore { param -> themeBrightnessSlider(param) }
    }

    private fun themeBrightnessSlider(param: XC_MethodHook.MethodHookParam) {
        if (!customQsTheme) return

        val parameterTypes = (param.method as Method).parameterTypes
        val colorsIndex = parameterTypes.indexOfFirst { it.name == SLIDER_COLORS_CLASS }
        if (colorsIndex == -1) return

        val colors = param.args[colorsIndex] ?: return
        if (themedSliderColors.containsKey(colors) || !isCalledFromBrightnessSlider()) return

        val themedColors = createThemedSliderColors(colors) ?: return
        themedSliderColors[themedColors] = true
        param.args[colorsIndex] = themedColors

        if (!brightnessGradient || !param.method.name.startsWith("Track")) return

        val modifierIndex = parameterTypes.indexOfFirst { it.name == COMPOSE_MODIFIER_CLASS }
        val stateIndex = parameterTypes.indexOfFirst { it.name == SLIDER_STATE_CLASS }
        if (modifierIndex == -1 || stateIndex == -1) return

        val modifier = param.args[modifierIndex] ?: return
        val sliderState = param.args[stateIndex] ?: return
        val drawer = gradientDrawerFor(sliderState) ?: return

        try {
            drawWithContentMethod?.invoke(null, modifier, drawer)?.let {
                param.args[modifierIndex] = it
            }
        } catch (_: Throwable) {
        }
    }

    private fun isCalledFromBrightnessSlider(): Boolean {
        return Throwable().stackTrace.any { it.className.startsWith(BRIGHTNESS_COMPOSE_PACKAGE) }
    }

    private fun createThemedSliderColors(original: Any): Any? {
        val values = SLIDER_COLOR_FIELDS.map { name ->
            when (name) {
                "thumbColor" -> colorValue(brightnessThumbColor)
                "activeTrackColor" -> colorValue(brightnessActiveColor)
                "inactiveTrackColor" -> colorValue(brightnessInactiveColor)
                else -> original.getFieldSilently(name)
            } ?: return null
        }

        return try {
            sliderColorsConstructor?.newInstance(*values.toTypedArray())
        } catch (_: Throwable) {
            null
        }
    }

    private fun colorValue(color: String): Any? = try {
        GraphicsColorKt.colorOf(color)
    } catch (_: Throwable) {
        null
    }

    private fun gradientDrawerFor(sliderState: Any): Any? {
        gradientDrawers[sliderState]?.let { return it }

        val drawer = drawProxy { drawScope -> drawGradientTrack(drawScope, sliderState) }
            ?: return null

        gradientDrawers[sliderState] = drawer
        return drawer
    }

    private fun drawGradientTrack(drawScope: Any, sliderState: Any) {
        var contentDrawn = false

        try {
            val canvas = nativeCanvasOf(drawScope)
            val packedSize = drawSizeOf(drawScope)

            if (canvas == null || packedSize == null) {
                drawScope.callMethod("drawContent")
                return
            }

            val width = Float.fromBits((packedSize ushr 32).toInt())
            val height = Float.fromBits((packedSize and 0xFFFFFFFFL).toInt())
            val fraction = (sliderState.callMethod("getCoercedValueAsFraction") as? Float)
                ?.coerceIn(0f, 1f) ?: 0f
            val isRtl = drawScope.callMethod("getLayoutDirection").toString() == "Rtl"

            val saveCount = canvas.saveLayer(0f, 0f, width, height, null)
            drawScope.callMethod("drawContent")
            contentDrawn = true

            val startColor = brightnessActiveColor.toColorInt()
            val endColor = brightnessGradientEndColor.toColorInt()
            gradientPaint.shader = LinearGradient(
                if (isRtl) width else 0f, 0f,
                if (isRtl) 0f else width, 0f,
                startColor, endColor,
                Shader.TileMode.CLAMP
            )

            val activeWidth = width * fraction
            if (isRtl) {
                canvas.drawRect(width - activeWidth, 0f, width, height, gradientPaint)
            } else {
                canvas.drawRect(0f, 0f, activeWidth, height, gradientPaint)
            }

            canvas.restoreToCount(saveCount)
        } catch (throwable: Throwable) {
            if (!contentDrawn) {
                try {
                    drawScope.callMethod("drawContent")
                } catch (_: Throwable) {
                }
            }
            log(this@QSTheme, "Brightness gradient failed: $throwable")
        }
    }

    private fun captureRecomposeScope(param: XC_MethodHook.MethodHookParam) {
        val composerIndex = (param.method as Method).parameterTypes
            .indexOfFirst { it.name == COMPOSER_CLASS }
        param.args.getOrNull(composerIndex)
            ?.callMethodSilently("getRecomposeScope")
            ?.let { tileRecomposeScopes.add(it) }
    }

    private fun hookFooterActions() {
        val footerActionsClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.compose.FooterActionsKt",
            suppressError = true
        ) ?: return

        footerActionsClass
            .hookMethod("IconButton")
            .suppressError()
            .run(object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val model = param.args.firstOrNull {
                        it?.javaClass?.name?.startsWith(FOOTER_VIEW_MODEL_PREFIX) == true
                    } ?: return

                    footerButtonTypes.addLast(model.javaClass.simpleName)
                    param.setObjectExtra(FOOTER_TYPE_PUSHED, true)
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    if (param.getObjectExtra(FOOTER_TYPE_PUSHED) == true) {
                        footerButtonTypes.removeLastOrNull()
                    }
                }
            })

        findClass("$SYSTEMUI_PACKAGE.qs.footer.ui.compose.ButtonColors", suppressError = true)
            .hookConstructor()
            .suppressError()
            .runBefore { param ->
                if (!customQsTheme || param.args.size != 2) return@runBefore
                val type = footerButtonTypes.lastOrNull() ?: return@runBefore
                val isActive = type.contains(ACTIVE_FOOTER_BUTTON)

                colorValue(if (isActive) footerActiveButtonIconColor else footerInactiveButtonIconColor)
                    ?.let { param.args[0] = it }
                colorValue(if (isActive) footerActiveButtonBgColor else footerInactiveButtonBgColor)
                    ?.let { param.args[1] = it }
            }

        footerButtonHeightDp = findClass(
            "$SYSTEMUI_PACKAGE.qs.footer.ui.compose.FooterActionsDefaults",
            suppressError = true
        )?.let { defaults ->
            try {
                defaults.getDeclaredField("FooterButtonHeight")
                    .apply { isAccessible = true }
                    .let { field ->
                        val owner = if (Modifier.isStatic(field.modifiers)) {
                            null
                        } else {
                            defaults.getField("INSTANCE").get(null)
                        }
                        field.getFloat(owner)
                    }
            } catch (_: Throwable) {
                null
            }
        }

        footerActionsClass
            .hookMethodMatchPattern("CircleExpandable.*")
            .suppressError()
            .runBefore { param -> applyFooterGradient(param) }

        listOf(
            footerActionsClass,
            findClass(
                "$SYSTEMUI_PACKAGE.qs.footer.ui.compose.FooterActionsDefaults",
                suppressError = true
            )
        ).forEach { clazz ->
            clazz
                .hookMethod("textButtonColors", "blurTextButtonColors")
                .suppressError()
                .runAfter { param ->
                    if (!customQsTheme) return@runAfter

                    param.result?.apply {
                        colorValue(footerChipContentColor)?.let { setFieldSilently("content", it) }
                        colorValue(footerChipBgColor)?.let { setFieldSilently("background", it) }
                    }
                }
        }
    }

    private fun applyFooterGradient(param: XC_MethodHook.MethodHookParam) {
        val parameterTypes = (param.method as Method).parameterTypes
        val modifierIndex = parameterTypes.indexOfFirst { it.name == COMPOSE_MODIFIER_CLASS }
        if (modifierIndex == -1 || parameterTypes.firstOrNull() != Long::class.javaPrimitiveType) {
            return
        }

        val modifier = param.args[modifierIndex] ?: return
        footerModifierToOriginal[modifier]?.let { return }

        if (!customQsTheme || !footerGradient) return
        if (footerButtonTypes.lastOrNull()?.contains(ACTIVE_FOOTER_BUTTON) != true) return

        val packedColor = param.args[0] as? Long ?: return
        val startColor = toArgbMethod?.invoke(null, packedColor) as? Int ?: return
        val transparent = colorValue("#00000000") ?: return
        val drawer = drawProxy { drawScope -> drawFooterGradient(drawScope, startColor) }
            ?: return

        try {
            drawBehindMethod?.invoke(null, modifier, drawer)?.let {
                footerModifierToOriginal[it] = modifier
                param.args[modifierIndex] = it
                param.args[0] = transparent
            }
        } catch (_: Throwable) {
        }
    }

    private fun drawFooterGradient(drawScope: Any, startColor: Int) {
        try {
            val canvas = nativeCanvasOf(drawScope) ?: return
            val packedSize = drawSizeOf(drawScope) ?: return
            val width = Float.fromBits((packedSize ushr 32).toInt())
            val height = Float.fromBits((packedSize and 0xFFFFFFFFL).toInt())
            val isRtl = drawScope.callMethod("getLayoutDirection").toString() == "Rtl"
            val density = drawScope.callMethod("getDensity") as? Float ?: 1f
            val diameter = footerButtonHeightDp
                ?.let { it * density }
                ?.coerceAtMost(minOf(width, height))
                ?: minOf(width, height)
            val left = (width - diameter) / 2f
            val top = (height - diameter) / 2f

            tileGradientPaint.alpha = 255
            tileGradientPaint.shader = LinearGradient(
                if (isRtl) left + diameter else left, 0f,
                if (isRtl) left else left + diameter, 0f,
                startColor,
                footerGradientEndColor.toColorInt(),
                Shader.TileMode.CLAMP
            )
            canvas.drawOval(left, top, left + diameter, top + diameter, tileGradientPaint)
        } catch (throwable: Throwable) {
            log(this@QSTheme, "Footer gradient failed: $throwable")
        }
    }

    private fun Any.callMethodByPrefix(prefix: String, vararg args: Any?): Any? {
        val method = javaClass.methods.firstOrNull {
            it.name.startsWith(prefix) && it.parameterTypes.size == args.size
        } ?: return null
        return method.invoke(this, *args)
    }

    private fun hookTileGradient() {
        drawBehindMethod = findClass(
            "androidx.compose.ui.draw.DrawModifierKt",
            suppressError = true
        )?.declaredMethods?.firstOrNull { method ->
            method.name == "drawBehind" && method.parameterTypes.size == 2
        } ?: return
        toArgbMethod = findClass(
            "androidx.compose.ui.graphics.ColorKt",
            suppressError = true
        )?.declaredMethods?.firstOrNull { method ->
            method.name.startsWith("toArgb") &&
                    method.parameterTypes.size == 1 &&
                    method.parameterTypes[0] == Long::class.javaPrimitiveType
        } ?: return

        findClass(
            "$SYSTEMUI_PACKAGE.qs.panels.ui.compose.infinitegrid.TileKt",
            suppressError = true
        )
            .hookMethod("TileExpandable")
            .suppressError()
            .runBefore { param -> applyTileGradient(param) }
    }

    private fun applyTileGradient(param: XC_MethodHook.MethodHookParam) {
        val parameterTypes = (param.method as Method).parameterTypes
        val modifierIndex = parameterTypes.indexOfFirst { it.name == COMPOSE_MODIFIER_CLASS }
        val shapeIndex = parameterTypes.indexOfFirst { it.name == COMPOSE_SHAPE_CLASS }
        val colorIndex = parameterTypes.indexOfFirst { it.name == FUNCTION0_CLASS }
        if (modifierIndex == -1 || shapeIndex == -1 || colorIndex == -1) return

        val modifier = param.args[modifierIndex] ?: return
        val original = tileModifierToOriginal[modifier] ?: modifier
        param.args[modifierIndex] = original

        if (!customQsTheme || !activeTileGradient) return

        val shape = param.args[shapeIndex] ?: return
        val colorProvider = param.args[colorIndex] ?: return
        val drawer = drawProxy { drawScope -> drawTileGradient(drawScope, shape, colorProvider) }
            ?: return

        try {
            drawBehindMethod?.invoke(null, original, drawer)?.let {
                tileModifierToOriginal[it] = original
                param.args[modifierIndex] = it
            }
        } catch (_: Throwable) {
        }
    }

    private fun drawTileGradient(drawScope: Any, shape: Any, colorProvider: Any) {
        try {
            val currentColor = colorProvider.callMethod("invoke")?.getFieldSilently("value")
                    as? Long ?: return
            val argb = toArgbMethod?.invoke(null, currentColor) as? Int ?: return
            val restingAlpha = minOf(
                Color.alpha(inactiveBgColor.toColorInt()),
                Color.alpha(unavailableBgColor.toColorInt())
            )
            val currentAlpha = Color.alpha(argb)
            val gradientAlpha = if (restingAlpha == 0) {
                if (currentAlpha == 0) 1f else 0f
            } else {
                (1f - currentAlpha.toFloat() / restingAlpha).coerceIn(0f, 1f)
            }
            if (gradientAlpha <= 0f) return

            val canvas = nativeCanvasOf(drawScope) ?: return
            val packedSize = drawSizeOf(drawScope) ?: return
            val width = Float.fromBits((packedSize ushr 32).toInt())
            val outline = shape.callMethodByPrefix(
                "createOutline",
                packedSize,
                drawScope.callMethod("getLayoutDirection"),
                drawScope
            ) ?: return
            val path = outlineToPath(outline) ?: return
            val isRtl = drawScope.callMethod("getLayoutDirection").toString() == "Rtl"

            tileGradientPaint.shader = LinearGradient(
                if (isRtl) width else 0f, 0f,
                if (isRtl) 0f else width, 0f,
                argb or 0xFF000000.toInt(),
                activeTileGradientEndColor.toColorInt(),
                Shader.TileMode.CLAMP
            )
            tileGradientPaint.alpha = (gradientAlpha * 255).toInt()
            canvas.drawPath(path, tileGradientPaint)
        } catch (throwable: Throwable) {
            log(this@QSTheme, "Tile gradient failed: $throwable")
        }
    }

    private fun outlineToPath(outline: Any): Path? {
        outline.getFieldSilently("roundRect")?.let { roundRect ->
            fun float(name: String) = roundRect.getFieldSilently(name) as? Float ?: 0f
            fun radius(name: String): Pair<Float, Float> {
                val packed = roundRect.getFieldSilently(name) as? Long ?: 0L
                return Float.fromBits((packed ushr 32).toInt()) to
                        Float.fromBits((packed and 0xFFFFFFFFL).toInt())
            }

            val radii = listOf(
                "topLeftCornerRadius",
                "topRightCornerRadius",
                "bottomRightCornerRadius",
                "bottomLeftCornerRadius"
            ).flatMap { radius(it).toList() }.toFloatArray()

            return Path().apply {
                addRoundRect(
                    RectF(float("left"), float("top"), float("right"), float("bottom")),
                    radii,
                    Path.Direction.CW
                )
            }
        }

        outline.getFieldSilently("rect")?.let { rect ->
            fun float(name: String) = rect.getFieldSilently(name) as? Float ?: 0f
            return Path().apply {
                addRect(
                    float("left"), float("top"), float("right"), float("bottom"),
                    Path.Direction.CW
                )
            }
        }

        return outline.getFieldSilently("path")
            ?.javaClass?.declaredFields
            ?.firstOrNull { it.type == Path::class.java }
            ?.apply { isAccessible = true }
            ?.get(outline.getFieldSilently("path")) as? Path
    }

    private fun drawProxy(onDraw: (Any) -> Unit): Any? {
        val functionClass = function1Class ?: return null
        return Proxy.newProxyInstance(
            functionClass.classLoader,
            arrayOf(functionClass)
        ) { proxy, method, args ->
            when (method.name) {
                "invoke" -> {
                    onDraw(args[0])
                    kotlinUnit
                }

                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "IconifyDrawProxy"
                else -> null
            }
        }
    }

    private fun nativeCanvasOf(drawScope: Any): Canvas? {
        val composeCanvas = drawScope.callMethod("getDrawContext").callMethod("getCanvas")
        return composeCanvas?.javaClass?.declaredFields
            ?.firstOrNull { it.type == Canvas::class.java }
            ?.apply { isAccessible = true }
            ?.get(composeCanvas) as? Canvas
    }

    private fun drawSizeOf(drawScope: Any): Long? {
        return drawScope.callMethod("getDrawContext")?.callMethodByPrefix("getSize") as? Long
    }

    companion object {
        private const val COMPOSER_CLASS = "androidx.compose.runtime.Composer"
        private val TILE_THEME_KEYS = setOf(
            XposedKey.CUSTOM_QS_THEME,
            XposedKey.ACTIVE_QS_TILE_BACKGROUND_COLOR,
            XposedKey.ACTIVE_QS_TILE_ICON_COLOR,
            XposedKey.ACTIVE_QS_TILE_ICON_BACKGROUND_COLOR,
            XposedKey.ACTIVE_QS_TILE_LABEL_COLOR,
            XposedKey.ACTIVE_QS_TILE_SECONDARY_LABEL_COLOR,
            XposedKey.ACTIVE_QS_TILE_GRADIENT,
            XposedKey.ACTIVE_QS_TILE_GRADIENT_END_COLOR,
            XposedKey.INACTIVE_QS_TILE_BACKGROUND_COLOR,
            XposedKey.INACTIVE_QS_TILE_ICON_COLOR,
            XposedKey.INACTIVE_QS_TILE_ICON_BACKGROUND_COLOR,
            XposedKey.INACTIVE_QS_TILE_LABEL_COLOR,
            XposedKey.INACTIVE_QS_TILE_SECONDARY_LABEL_COLOR,
            XposedKey.UNAVAILABLE_QS_TILE_BACKGROUND_COLOR,
            XposedKey.UNAVAILABLE_QS_TILE_ICON_COLOR,
            XposedKey.UNAVAILABLE_QS_TILE_ICON_BACKGROUND_COLOR,
            XposedKey.UNAVAILABLE_QS_TILE_LABEL_COLOR,
            XposedKey.UNAVAILABLE_QS_TILE_SECONDARY_LABEL_COLOR
        ).map { it.name }.toSet()
        private const val FOOTER_VIEW_MODEL_PREFIX =
            "$SYSTEMUI_PACKAGE.qs.footer.ui.viewmodel.FooterActionsButtonViewModel"
        private const val FOOTER_TYPE_PUSHED = "iconify_footer_type_pushed"
        private const val ACTIVE_FOOTER_BUTTON = "PowerActionViewModel"
        private const val COMPOSE_SHAPE_CLASS = "androidx.compose.ui.graphics.Shape"
        private const val FUNCTION0_CLASS = "kotlin.jvm.functions.Function0"
        private const val SLIDER_COLORS_CLASS = "androidx.compose.material3.SliderColors"
        private const val SLIDER_STATE_CLASS = "androidx.compose.material3.SliderState"
        private const val COMPOSE_MODIFIER_CLASS = "androidx.compose.ui.Modifier"
        private const val BRIGHTNESS_COMPOSE_PACKAGE = "$SYSTEMUI_PACKAGE.brightness.ui.compose."
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
        private const val STATE_UNAVAILABLE = 0
        private const val STATE_INACTIVE = 1
        private const val STATE_ACTIVE = 2
    }
}