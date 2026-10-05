package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.GraphicsColorKt
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.WeakHashMap

@SuppressLint("DiscouragedApi")
class QSBrightnessSlider(context: Context) : ModPack(context) {

    private var brightnessBelowTiles = false
    private var brightnessInQqs = false

    private var function2Class: Class<*>? = null
    private var function3Class: Class<*>? = null
    private var kotlinUnit: Any? = null
    private var modifierCompanion: Any? = null
    private var sharedElementKey: Any? = null
    private var brightnessContainerMethod: Method? = null
    private var containerColorsConstructor: Constructor<*>? = null

    private var qsFragment: Any? = null
    private var qqsScope: Any? = null
    private var qsScope: Any? = null

    private val qsBrightnessSlots = WeakHashMap<Any, Any>()
    private val qqsTilesSlots = WeakHashMap<Any, Any>()

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            brightnessBelowTiles = getBoolean(XposedKey.QS_BRIGHTNESS_SLIDER_BOTTOM)
            brightnessInQqs = getBoolean(XposedKey.QQS_BRIGHTNESS_SLIDER)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val qsFragmentClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.composefragment.QSFragmentCompose",
            suppressError = true
        ) ?: return
        val qsLayoutClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.composefragment.QSFragmentComposeKt",
            suppressError = true
        ) ?: return

        resolveComposeApis()

        qsFragmentClass
            .hookMethod("QuickQuickSettingsElement")
            .suppressError()
            .runBefore { param ->
                qsFragment = param.thisObject
                qqsScope = param.args.firstOrNull()
            }

        qsFragmentClass
            .hookMethod("QuickSettingsElement")
            .suppressError()
            .runBefore { param ->
                qsFragment = param.thisObject
                qsScope = param.args.firstOrNull()
            }

        qsLayoutClass
            .hookMethod("QuickSettingsLayout")
            .suppressError()
            .runBefore { param ->
                if (!brightnessBelowTiles && !brightnessInQqs) return@runBefore
                if (param.args.size < 3) return@runBefore

                val brightness = param.args[0] ?: return@runBefore
                val tiles = param.args[1] ?: return@runBefore
                val brightnessSlot = if (brightnessInQqs) {
                    sharedQsBrightnessSlot(brightness) ?: brightness
                } else {
                    brightness
                }

                if (brightnessBelowTiles) {
                    param.args[0] = tiles
                    param.args[1] = brightnessSlot
                } else {
                    param.args[0] = brightnessSlot
                }
            }

        qsLayoutClass
            .hookMethod("QuickQuickSettingsLayout")
            .suppressError()
            .runBefore { param ->
                if (!brightnessInQqs) return@runBefore

                val mediaInRow = param.args.firstOrNull { it is Boolean } as? Boolean
                if (mediaInRow != false) return@runBefore

                val tiles = param.args.firstOrNull() ?: return@runBefore
                if (tiles in qqsTilesSlots.values) return@runBefore

                qqsTilesSlots[tiles]?.let {
                    param.args[0] = it
                    return@runBefore
                }

                val slot = composableSlot { composer, changed ->
                    if (brightnessBelowTiles) {
                        tiles.callMethod("invoke", composer, changed)
                        composeQqsBrightness(composer)
                    } else {
                        composeQqsBrightness(composer)
                        tiles.callMethod("invoke", composer, changed)
                    }
                } ?: return@runBefore

                qqsTilesSlots[tiles] = slot
                param.args[0] = slot
            }
    }

    private fun resolveComposeApis() {
        function2Class = findClass("kotlin.jvm.functions.Function2", suppressError = true)
        function3Class = findClass("kotlin.jvm.functions.Function3", suppressError = true)

        try {
            kotlinUnit = findClass("kotlin.Unit", suppressError = true)
                ?.getField("INSTANCE")?.get(null)
            modifierCompanion = findClass("androidx.compose.ui.Modifier", suppressError = true)
                ?.getField("Companion")?.get(null)
        } catch (_: Throwable) {
        }

        sharedElementKey = try {
            findClass("com.android.compose.animation.scene.ElementKey", suppressError = true)
                ?.declaredConstructors
                ?.firstOrNull { it.parameterTypes.size == 6 }
                ?.newInstance(SHARED_ELEMENT_NAME, null, null, false, ELEMENT_KEY_DEFAULTS, null)
        } catch (_: Throwable) {
            null
        }

        brightnessContainerMethod = findClass(
            "$SYSTEMUI_PACKAGE.brightness.ui.compose.BrightnessSliderKt",
            suppressError = true
        )?.declaredMethods?.firstOrNull { it.name == "BrightnessSliderContainer" }

        containerColorsConstructor = findClass(
            "$SYSTEMUI_PACKAGE.brightness.ui.compose.ContainerColors",
            suppressError = true
        )?.declaredConstructors?.firstOrNull { constructor ->
            constructor.parameterTypes.size == 2 &&
                    constructor.parameterTypes.all { it == Long::class.javaPrimitiveType }
        }
    }

    private fun sharedQsBrightnessSlot(brightness: Any): Any? {
        if (brightness in qsBrightnessSlots.values) return brightness
        qsBrightnessSlots[brightness]?.let { return it }

        val scope = qsScope ?: return null
        val key = sharedElementKey ?: return null
        val slot = composableSlot { composer, changed ->
            composeElement(scope, key, composer) {
                brightness.callMethod("invoke", composer, changed)
            }
        } ?: return null

        qsBrightnessSlots[brightness] = slot
        return slot
    }

    private fun composeQqsBrightness(composer: Any) {
        val scope = qqsScope
        val key = sharedElementKey

        if (scope != null && key != null) {
            composeElement(scope, key, composer) { composeBrightnessContainer(composer) }
        } else {
            composeBrightnessContainer(composer)
        }
    }

    private fun composeElement(scope: Any, key: Any, composer: Any, content: () -> Unit) {
        val elementMethod = scope.javaClass.methods
            .firstOrNull { it.name == "Element" && it.parameterTypes.size == 5 }
        val elementContent = composableContent { content() }

        if (elementMethod == null || elementContent == null) {
            content()
            return
        }

        elementMethod.invoke(scope, key, modifierCompanion, elementContent, composer, 0)
    }

    private fun composeBrightnessContainer(composer: Any) {
        val method = brightnessContainerMethod ?: return
        val viewModel = qsFragment
            .getFieldSilently("viewModel")
            .getFieldSilently("containerViewModel")
            .getFieldSilently("brightnessSliderViewModel") ?: return
        val colors = containerColors() ?: return

        val parameterTypes = method.parameterTypes
        val composerIndex = parameterTypes.indexOfFirst { it.name == COMPOSER_CLASS }
        if (composerIndex == -1) return

        var defaultMask = 0
        val args = arrayOfNulls<Any>(parameterTypes.size)

        parameterTypes.forEachIndexed { index, type ->
            args[index] = when {
                index == composerIndex -> composer
                index > composerIndex -> 0
                type.name == BRIGHTNESS_VIEW_MODEL_CLASS -> viewModel
                type.name == COMPOSE_MODIFIER_CLASS -> modifierCompanion
                type.name == CONTAINER_COLORS_CLASS -> colors
                type == Boolean::class.javaPrimitiveType -> {
                    defaultMask = defaultMask or (1 shl index)
                    false
                }

                else -> {
                    defaultMask = defaultMask or (1 shl index)
                    null
                }
            }
        }

        if (parameterTypes.size - composerIndex - 1 == 2) {
            args[parameterTypes.size - 1] = defaultMask
        }

        method.invoke(null, *args)
    }

    private fun containerColors(): Any? {
        val mirrorColorId = mContext.resources.getIdentifier(
            "shade_panel_fallback",
            "color",
            SYSTEMUI_PACKAGE
        )
        val mirrorColor = if (mirrorColorId != 0) {
            mContext.getColor(mirrorColorId)
        } else {
            0
        }

        return try {
            containerColorsConstructor?.newInstance(
                GraphicsColorKt.colorOf(0),
                GraphicsColorKt.colorOf(mirrorColor)
            )
        } catch (throwable: Throwable) {
            log(this@QSBrightnessSlider, throwable)
            null
        }
    }

    private fun composableSlot(block: (composer: Any, changed: Any?) -> Unit): Any? {
        val functionClass = function2Class ?: return null
        return Proxy.newProxyInstance(
            functionClass.classLoader,
            arrayOf(functionClass)
        ) { proxy, method, args ->
            when (method.name) {
                "invoke" -> {
                    block(args[0], args[1])
                    kotlinUnit
                }

                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "IconifyBrightnessSlot"
                else -> null
            }
        }
    }

    private fun composableContent(block: () -> Unit): Any? {
        val functionClass = function3Class ?: return null
        return Proxy.newProxyInstance(
            functionClass.classLoader,
            arrayOf(functionClass)
        ) { proxy, method, args ->
            when (method.name) {
                "invoke" -> {
                    block()
                    kotlinUnit
                }

                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "IconifyBrightnessElement"
                else -> null
            }
        }
    }

    companion object {
        private const val SHARED_ELEMENT_NAME = "IconifyBrightnessSlider"
        private const val ELEMENT_KEY_DEFAULTS = 14
        private const val COMPOSER_CLASS = "androidx.compose.runtime.Composer"
        private const val COMPOSE_MODIFIER_CLASS = "androidx.compose.ui.Modifier"
        private const val BRIGHTNESS_VIEW_MODEL_CLASS =
            "$SYSTEMUI_PACKAGE.brightness.ui.viewmodel.BrightnessSliderViewModel"
        private const val CONTAINER_COLORS_CLASS =
            "$SYSTEMUI_PACKAGE.brightness.ui.compose.ContainerColors"
    }
}
