package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.TypedValue
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method

@SuppressLint("DiscouragedApi")
class ComposeSignalIcon(context: Context) : ModPack(context) {

    private class PendingSignal(
        val lambdaPrefix: String,
        val drawables: List<Drawable>,
        val color: Int,
        val sizeDp: Pair<Float, Float>?
    )

    private var useDrawableSignalIcon = false
    private var sizeMethod: Method? = null
    private var modifierCompanion: Any? = null
    @Volatile
    private var hookFailed = false
    private var toArgbMethod: Method? = null
    private val constantStateCache = mutableMapOf<Int, Drawable.ConstantState>()
    private val pendingSignal = ThreadLocal<PendingSignal?>()

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            useDrawableSignalIcon = getBoolean(XposedKey.STATUSBAR_DRAWABLE_SIGNAL_ICON)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val mobileIconClass = findClass(MOBILE_ICON_CLASS, suppressError = true)
        val stackedMobileIconClass = findClass(STACKED_MOBILE_ICON_CLASS, suppressError = true)
        if (mobileIconClass == null && stackedMobileIconClass == null) return

        toArgbMethod = ComposeToolkit.loadClass(COLOR_KT_CLASS)?.methods?.firstOrNull { method ->
            (method.name == "toArgb" || method.name.startsWith("toArgb-")) &&
                    method.parameterTypes.contentEquals(arrayOf(Long::class.javaPrimitiveType))
        }
        sizeMethod = ComposeToolkit.modifierFunction(
            SIZE_KT_CLASS,
            "size",
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType
        )
        modifierCompanion = try {
            ComposeToolkit.loadClass(ComposeToolkit.MODIFIER_CLASS)?.getField("Companion")?.get(null)
        } catch (_: Throwable) {
            null
        }

        mobileIconClass
            .hookMethodMatchPattern("MobileSignalIcon(-.*)?")
            .suppressError()
            .run(signalHook(MOBILE_ICON_CLASS, ::singleSizeSp) { param, parameterTypes ->
                val modelIndex =
                    parameterTypes.indexOfFirst { it.name.endsWith(CELLULAR_MODEL_SUFFIX) }
                if (modelIndex == -1) null else listOf(param.args[modelIndex])
            })

        stackedMobileIconClass
            .hookMethodMatchPattern("StackedMobileIcon(-.*)?")
            .suppressError()
            .run(signalHook(STACKED_MOBILE_ICON_CLASS, ::stackedSizeSp) { param, parameterTypes ->
                val dualSimIndex = parameterTypes.indexOfFirst { it.name.endsWith(DUAL_SIM_SUFFIX) }
                if (dualSimIndex == -1) {
                    null
                } else {
                    val dualSim = param.args[dualSimIndex] ?: return@signalHook null
                    listOf(
                        dualSim.callMethodSilently("getPrimary"),
                        dualSim.callMethodSilently("getSecondary")
                    )
                }
            })

        findClass(CANVAS_KT_CLASS, suppressError = true)
            .hookMethodMatchPattern("Canvas(-.*)?")
            .suppressError()
            .runBefore { param ->
                val pending = pendingSignal.get() ?: return@runBefore

                val parameterTypes = (param.method as Method).parameterTypes
                val onDrawIndex = parameterTypes.indexOfFirst { it.name == FUNCTION1_CLASS }
                if (onDrawIndex == -1) return@runBefore

                val original = param.args[onDrawIndex] ?: return@runBefore
                if (!original.javaClass.name.startsWith(pending.lambdaPrefix)) return@runBefore

                val drawer = ComposeToolkit.function1("IconifySignalIcon") { drawScope ->
                    try {
                        drawSignal(drawScope!!, pending.drawables, pending.color)
                    } catch (throwable: Throwable) {
                        hookFailed = true
                        log(this@ComposeSignalIcon, throwable)
                        ComposeToolkit.invokeFunction(original, drawScope)
                    }
                } ?: return@runBefore

                param.args[onDrawIndex] = drawer

                val sizeDp = pending.sizeDp ?: return@runBefore
                val modifierIndex =
                    parameterTypes.indexOfFirst { it.name == ComposeToolkit.MODIFIER_CLASS }
                if (modifierIndex == -1) return@runBefore
                val modifier = param.args[modifierIndex] ?: return@runBefore
                val companion = modifierCompanion ?: return@runBefore

                try {
                    val sizeModifier = sizeMethod?.invoke(
                        null,
                        companion,
                        sizeDp.first,
                        sizeDp.second
                    ) ?: return@runBefore
                    ComposeToolkit.then(sizeModifier, modifier)?.let {
                        param.args[modifierIndex] = it
                    }
                } catch (throwable: Throwable) {
                    log(this@ComposeSignalIcon, throwable)
                }
            }
    }

    private fun signalHook(
        ownerClass: String,
        naturalSizeSp: (Int) -> Pair<Float, Float>?,
        models: (XC_MethodHook.MethodHookParam, Array<Class<*>>) -> List<Any?>?
    ) = object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            pendingSignal.remove()
            if (!useDrawableSignalIcon || hookFailed) return

            try {
                val parameterTypes = (param.method as Method).parameterTypes
                val colorIndex = parameterTypes.indexOfFirst { it == Long::class.javaPrimitiveType }
                if (colorIndex == -1) return

                val signalModels = models(param, parameterTypes) ?: return
                if (signalModels.isEmpty()) return

                val drawables = signalModels.map { model ->
                    model?.let { signalDrawableFor(it) } ?: return
                }
                val color = colorToArgb(param.args[colorIndex] as Long) ?: return

                val bars = signalModels.maxOf {
                    (it?.callMethodSilently("getNumberOfLevels") as? Int ?: 5) - 1
                }
                val sizeDp = naturalSizeSp(bars)?.let { (width, height) ->
                    val scale = enlargedSignalScale(height)
                    if (scale > 1f) spToDp(width) * scale to spToDp(height) * scale else null
                }

                pendingSignal.set(PendingSignal("$ownerClass\$", drawables, color, sizeDp))
            } catch (throwable: Throwable) {
                hookFailed = true
                log(this@ComposeSignalIcon, throwable)
            }
        }

        override fun afterHookedMethod(param: MethodHookParam) {
            pendingSignal.remove()
        }
    }

    private fun singleSizeSp(bars: Int): Pair<Float, Float>? {
        val dimensions = staticField(
            MOBILE_ICON_CLASS,
            if (bars == 5) "mobileSignalFiveBarsDimensions" else "mobileSignalFourBarsDimensions"
        ) ?: return null
        val width = dimensions.getFieldSilently("totalWidth") as? Long ?: return null
        val height = staticField(MOBILE_ICON_DIMENSIONS_CLASS, "IconHeightSp") as? Long
            ?: return null
        return textUnitValue(width) to textUnitValue(height)
    }

    private fun stackedSizeSp(bars: Int): Pair<Float, Float>? {
        val dimensions = staticField(
            if (bars == 5) FIVE_BARS_DIMENSIONS_CLASS else FOUR_BARS_DIMENSIONS_CLASS,
            "INSTANCE"
        ) ?: return null
        val width = dimensions.getFieldSilently("totalWidth") as? Long ?: return null
        val height = staticField(STACKED_MOBILE_ICON_DIMENSIONS_CLASS, "IconHeightSp") as? Long
            ?: return null
        return textUnitValue(width) to textUnitValue(height)
    }

    private fun staticField(className: String, fieldName: String): Any? = try {
        findClass(className, suppressError = true)
            ?.getDeclaredField(fieldName)
            ?.apply { isAccessible = true }
            ?.get(null)
    } catch (_: Throwable) {
        null
    }

    private fun textUnitValue(packed: Long): Float =
        Float.fromBits((packed and 0xFFFFFFFFL).toInt())

    private fun enlargedSignalScale(naturalHeightSp: Float): Float {
        val id = mContext.resources.getIdentifier(
            SIGNAL_SIZE_DIMEN,
            "dimen",
            SYSTEMUI_PACKAGE
        )
        if (id == 0 || naturalHeightSp <= 0f) return 1f

        val metrics = mContext.resources.displayMetrics
        val naturalHeightPx =
            TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, naturalHeightSp, metrics)
        val scale = mContext.resources.getDimension(id) / naturalHeightPx
        return if (scale > 1.01f) scale.coerceAtMost(MAX_SIGNAL_SCALE) else 1f
    }

    private fun spToDp(sp: Float): Float {
        val metrics = mContext.resources.displayMetrics
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics) / metrics.density
    }

    private fun signalDrawableFor(model: Any): Drawable? {
        if (!model.javaClass.name.endsWith(CELLULAR_CLASS_SUFFIX)) return null
        if (model.getFieldSilently("carrierNetworkChange") == true) return null

        val numberOfLevels = model.callMethodSilently("getNumberOfLevels") as? Int ?: return null
        val level = model.callMethodSilently("getLevel") as? Int ?: return null
        val showExclamation =
            model.callMethodSilently("getShowExclamationMark") as? Boolean ?: false

        val bars = numberOfLevels - 1
        if (bars !in 4..5 || level !in 0..bars) return null

        val name = buildString {
            append("ic_mobile_").append(level).append('_').append(bars).append("_bar")
            if (showExclamation) append("_error")
        }

        val id = mContext.resources.getIdentifier(name, "drawable", SYSTEMUI_PACKAGE)
        if (id == 0) return null

        val constantState = constantStateCache.getOrPut(id) {
            mContext.resources.getDrawable(id, mContext.theme).constantState ?: return null
        }

        return constantState.newDrawable(mContext.resources, mContext.theme).mutate()
    }

    private fun drawSignal(drawScope: Any, drawables: List<Drawable>, color: Int) {
        val drawContext = drawScope.callMethodSilently("getDrawContext")
            ?: throw IllegalStateException("DrawScope has no draw context")
        val canvas = nativeCanvasOf(drawContext)
            ?: throw IllegalStateException("Native canvas not found")
        val packedSize = drawContext.javaClass.methods.firstOrNull {
            it.name.startsWith("getSize") && it.parameterTypes.isEmpty()
        }?.invoke(drawContext) as? Long
            ?: throw IllegalStateException("Draw size not found")

        val width = Float.fromBits((packedSize ushr 32).toInt())
        val height = Float.fromBits((packedSize and 0xFFFFFFFFL).toInt())
        if (width <= 0f || height <= 0f) return

        val rowHeight = height / drawables.size

        drawables.forEachIndexed { row, drawable ->
            val intrinsicWidth = drawable.intrinsicWidth.takeIf { it > 0 }?.toFloat() ?: width
            val intrinsicHeight =
                drawable.intrinsicHeight.takeIf { it > 0 }?.toFloat() ?: rowHeight
            val scale = minOf(width / intrinsicWidth, rowHeight / intrinsicHeight)
            val drawWidth = intrinsicWidth * scale
            val drawHeight = intrinsicHeight * scale
            val left = ((width - drawWidth) / 2f).toInt()
            val top = (rowHeight * row + (rowHeight - drawHeight) / 2f).toInt()

            drawable.setTint(color)
            drawable.setBounds(left, top, left + drawWidth.toInt(), top + drawHeight.toInt())
            drawable.draw(canvas)
        }
    }

    private fun nativeCanvasOf(drawContext: Any): Canvas? {
        val composeCanvas = drawContext.callMethodSilently("getCanvas") ?: return null
        return composeCanvas.javaClass.declaredFields
            .firstOrNull { it.type == Canvas::class.java }
            ?.apply { isAccessible = true }
            ?.get(composeCanvas) as? Canvas
    }

    private fun colorToArgb(color: Long): Int? = try {
        toArgbMethod?.invoke(null, color) as? Int ?: (color ushr 32).toInt()
    } catch (_: Throwable) {
        null
    }

    companion object {
        private const val MOBILE_ICON_CLASS =
            "$SYSTEMUI_PACKAGE.statusbar.pipeline.mobile.ui.compose.MobileIconKt"
        private const val STACKED_MOBILE_ICON_CLASS =
            "$SYSTEMUI_PACKAGE.statusbar.pipeline.shared.ui.composable.StackedMobileIconKt"
        private const val MOBILE_ICON_DIMENSIONS_CLASS =
            "$SYSTEMUI_PACKAGE.statusbar.pipeline.mobile.ui.compose.MobileIconDimensions"
        private const val STACKED_MOBILE_ICON_DIMENSIONS_CLASS =
            "$SYSTEMUI_PACKAGE.statusbar.pipeline.shared.ui.composable.StackedMobileIconDimensions"
        private const val FIVE_BARS_DIMENSIONS_CLASS =
            "$SYSTEMUI_PACKAGE.statusbar.pipeline.shared.ui.composable.FiveBarsDimensions"
        private const val FOUR_BARS_DIMENSIONS_CLASS =
            "$SYSTEMUI_PACKAGE.statusbar.pipeline.shared.ui.composable.FourBarsDimensions"
        private const val SIZE_KT_CLASS = "androidx.compose.foundation.layout.SizeKt"
        private const val SIGNAL_SIZE_DIMEN = "status_bar_mobile_signal_size_updated"
        private const val MAX_SIGNAL_SCALE = 2f
        private const val CANVAS_KT_CLASS = "androidx.compose.foundation.CanvasKt"
        private const val FUNCTION1_CLASS = "kotlin.jvm.functions.Function1"
        private const val COLOR_KT_CLASS = "androidx.compose.ui.graphics.ColorKt"
        private const val CELLULAR_MODEL_SUFFIX = "SignalIconModel\$CellularTypeIconModel"
        private const val CELLULAR_CLASS_SUFFIX = "\$CellularTypeIconModel\$Cellular"
        private const val DUAL_SIM_SUFFIX = ".mobile.ui.model.DualSim"
    }
}
