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
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Method

@SuppressLint("DiscouragedApi")
class ComposeSignalIcon(context: Context) : ModPack(context) {

    private var useDrawableSignalIcon = false
    @Volatile
    private var hookFailed = false
    private var drawWithContentMethod: Method? = null
    private var toArgbMethod: Method? = null
    private val drawableCache = mutableMapOf<Int, Drawable>()

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            useDrawableSignalIcon = getBoolean(XposedKey.STATUSBAR_DRAWABLE_SIGNAL_ICON)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val mobileIconClass = findClass(MOBILE_ICON_CLASS, suppressError = true) ?: return

        drawWithContentMethod =
            ComposeToolkit.modifierFunction(DRAW_MODIFIER_CLASS, "drawWithContent", null)
        toArgbMethod = ComposeToolkit.loadClass(COLOR_KT_CLASS)?.methods?.firstOrNull { method ->
            (method.name == "toArgb" || method.name.startsWith("toArgb-")) &&
                    method.parameterTypes.contentEquals(arrayOf(Long::class.javaPrimitiveType))
        }

        mobileIconClass
            .hookMethodMatchPattern("MobileSignalIcon(-.*)?")
            .suppressError()
            .runBefore { param ->
                if (!useDrawableSignalIcon || hookFailed) return@runBefore

                val parameterTypes = (param.method as Method).parameterTypes
                val modelIndex = parameterTypes.indexOfFirst { it.name.endsWith(CELLULAR_MODEL_SUFFIX) }
                val colorIndex = parameterTypes.indexOfFirst { it == Long::class.javaPrimitiveType }
                val modifierIndex =
                    parameterTypes.indexOfFirst { it.name == ComposeToolkit.MODIFIER_CLASS }
                if (modelIndex == -1 || colorIndex == -1 || modifierIndex == -1) return@runBefore

                val model = param.args[modelIndex] ?: return@runBefore
                val drawable = try {
                    signalDrawableFor(model)
                } catch (throwable: Throwable) {
                    hookFailed = true
                    log(this@ComposeSignalIcon, throwable)
                    null
                } ?: return@runBefore
                val color = colorToArgb(param.args[colorIndex] as Long) ?: return@runBefore
                val modifier = param.args[modifierIndex] ?: return@runBefore

                val drawer = ComposeToolkit.function1("IconifySignalIcon") { drawScope ->
                    try {
                        drawSignal(drawScope!!, drawable, color)
                    } catch (throwable: Throwable) {
                        hookFailed = true
                        log(this@ComposeSignalIcon, throwable)
                        drawScope?.callMethodSilently("drawContent")
                    }
                } ?: return@runBefore

                try {
                    drawWithContentMethod?.invoke(null, modifier, drawer)?.let {
                        param.args[modifierIndex] = it
                    }
                } catch (_: Throwable) {
                }
            }
    }

    private fun signalDrawableFor(model: Any): Drawable? {
        if (!model.javaClass.name.endsWith(CELLULAR_CLASS_SUFFIX)) return null
        if (model.getFieldSilently("carrierNetworkChange") == true) return null

        val numberOfLevels = model.callMethodSilently("getNumberOfLevels") as? Int ?: return null
        val level = model.callMethodSilently("getLevel") as? Int ?: return null
        val showExclamation = model.callMethodSilently("getShowExclamationMark") as? Boolean ?: false

        val bars = numberOfLevels - 1
        if (bars !in 4..5) return null

        val name = buildString {
            append("ic_mobile_").append(level.coerceIn(0, bars)).append('_').append(bars).append("_bar")
            if (showExclamation) append("_error")
        }

        val id = mContext.resources.getIdentifier(name, "drawable", SYSTEMUI_PACKAGE)
        if (id == 0) return null

        return drawableCache.getOrPut(id) {
            mContext.resources.getDrawable(id, mContext.theme).mutate()
        }
    }

    private fun drawSignal(drawScope: Any, drawable: Drawable, color: Int) {
        val canvas = nativeCanvasOf(drawScope)
        val packedSize = drawScope.callMethodSilently("getDrawContext")
            ?.let { context ->
                context.javaClass.methods.firstOrNull {
                    it.name.startsWith("getSize") && it.parameterTypes.isEmpty()
                }?.invoke(context) as? Long
            }

        if (canvas == null || packedSize == null) {
            drawScope.callMethodSilently("drawContent")
            return
        }

        val width = Float.fromBits((packedSize ushr 32).toInt())
        val height = Float.fromBits((packedSize and 0xFFFFFFFFL).toInt())
        if (width <= 0f || height <= 0f) return

        val intrinsicWidth = drawable.intrinsicWidth.takeIf { it > 0 }?.toFloat() ?: width
        val intrinsicHeight = drawable.intrinsicHeight.takeIf { it > 0 }?.toFloat() ?: height
        val scale = minOf(width / intrinsicWidth, height / intrinsicHeight)
        val drawWidth = intrinsicWidth * scale
        val drawHeight = intrinsicHeight * scale
        val left = ((width - drawWidth) / 2f).toInt()
        val top = ((height - drawHeight) / 2f).toInt()

        drawable.setTint(color)
        drawable.setBounds(left, top, left + drawWidth.toInt(), top + drawHeight.toInt())
        drawable.draw(canvas)
    }

    private fun nativeCanvasOf(drawScope: Any): Canvas? {
        val composeCanvas = drawScope.callMethodSilently("getDrawContext")
            ?.callMethodSilently("getCanvas") ?: return null
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
        private const val DRAW_MODIFIER_CLASS = "androidx.compose.ui.draw.DrawModifierKt"
        private const val COLOR_KT_CLASS = "androidx.compose.ui.graphics.ColorKt"
        private const val CELLULAR_MODEL_SUFFIX = "SignalIconModel\$CellularTypeIconModel"
        private const val CELLULAR_CLASS_SUFFIX = "\$CellularTypeIconModel\$Cellular"
    }
}
