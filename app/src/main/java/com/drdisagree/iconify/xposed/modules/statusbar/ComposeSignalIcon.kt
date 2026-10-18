package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
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
        val color: Int
    )

    private var useDrawableSignalIcon = false
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

        mobileIconClass
            .hookMethodMatchPattern("MobileSignalIcon(-.*)?")
            .suppressError()
            .run(signalHook(MOBILE_ICON_CLASS) { param, parameterTypes ->
                val modelIndex =
                    parameterTypes.indexOfFirst { it.name.endsWith(CELLULAR_MODEL_SUFFIX) }
                if (modelIndex == -1) null else listOf(param.args[modelIndex])
            })

        stackedMobileIconClass
            .hookMethodMatchPattern("StackedMobileIcon(-.*)?")
            .suppressError()
            .run(signalHook(STACKED_MOBILE_ICON_CLASS) { param, parameterTypes ->
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
            }
    }

    private fun signalHook(
        ownerClass: String,
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

                pendingSignal.set(PendingSignal("$ownerClass\$", drawables, color))
            } catch (throwable: Throwable) {
                hookFailed = true
                log(this@ComposeSignalIcon, throwable)
            }
        }

        override fun afterHookedMethod(param: MethodHookParam) {
            pendingSignal.remove()
        }
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
        private const val CANVAS_KT_CLASS = "androidx.compose.foundation.CanvasKt"
        private const val FUNCTION1_CLASS = "kotlin.jvm.functions.Function1"
        private const val COLOR_KT_CLASS = "androidx.compose.ui.graphics.ColorKt"
        private const val CELLULAR_MODEL_SUFFIX = "SignalIconModel\$CellularTypeIconModel"
        private const val CELLULAR_CLASS_SUFFIX = "\$CellularTypeIconModel\$Cellular"
        private const val DUAL_SIM_SUFFIX = ".mobile.ui.model.DualSim"
    }
}
