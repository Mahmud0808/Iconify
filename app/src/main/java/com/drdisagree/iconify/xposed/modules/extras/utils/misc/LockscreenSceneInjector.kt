package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

object LockscreenSceneInjector {

    private class Layer(
        val order: Int,
        val key: Int,
        val elementName: String,
        val factory: (Context) -> View
    )

    private class ElementHost(
        val key: Int,
        val isEnabled: () -> Boolean,
        val factory: (Context) -> View
    )

    enum class Anchor { SMALL_CLOCK_SMARTSPACE, LARGE_CLOCK_SMARTSPACE, LARGE_CLOCK_DATE }

    private class Overlay(
        val key: Int,
        val isEnabled: () -> Boolean,
        val modifier: () -> Any?,
        val factory: (Context) -> View
    )

    private val layers = CopyOnWriteArrayList<Layer>()
    private val overlays = CopyOnWriteArrayList<Overlay>()
    private var hooked = false
    private var replacement: ElementHost? = null
    private val anchors = HashMap<Anchor, ElementHost>()
    private var factoryHooked = false
    private val clockSizeListeners = CopyOnWriteArrayList<(Boolean) -> Unit>()
    private var lastLargeClock: Boolean? = null
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    private val largeClockRegionKey: Any? by lazy {
        ComposeViewHost.staticField(REGION_CLOCK_KEYS_CLASS, "INSTANCE")
            ?.callMethodSilently("getLarge")
    }

    private val clockRegionKeys: List<Any> by lazy {
        val regionClock = ComposeViewHost.staticField(REGION_CLOCK_KEYS_CLASS, "INSTANCE")
            ?: return@lazy emptyList()
        listOfNotNull(
            regionClock.callMethodSilently("getSmall"),
            regionClock.callMethodSilently("getLarge")
        )
    }

    private val largeClockKey: Any? by lazy {
        ComposeViewHost.staticField(CLOCK_KEYS_CLASS, "INSTANCE")
            ?.callMethodSilently("getLarge")
    }

    private var largeClockBelowSeen = false

    private val largeClockBelowKey: Any? by lazy {
        ComposeViewHost.staticField(DWA_LARGE_CLOCK_KEYS_CLASS, "INSTANCE")
            ?.callMethodSilently("getBelow")
    }

    private val regionStack = ArrayDeque<Boolean>()

    private val smartspaceKeys: List<Any> by lazy {
        val smartspace = ComposeViewHost.staticField(SMARTSPACE_KEYS_CLASS, "INSTANCE")
            ?: return@lazy emptyList()
        listOfNotNull(
            smartspace.callMethodSilently("getCards"),
            smartspace.callMethodSilently("getSliceView")
        )
    }

    fun replaceClockRegion(key: Int, isEnabled: () -> Boolean, factory: (Context) -> View) {
        replacement = ElementHost(key, isEnabled, factory)
        hookElementFactory()
    }

    fun addClockSizeListener(listener: (isLargeClock: Boolean) -> Unit) {
        clockSizeListeners += listener
        lastLargeClock?.let { listener(it) }
        hookElementFactory()
    }

    private fun dispatchClockSize(key: Any) {
        val isLarge = key == largeClockRegionKey
        if (lastLargeClock == isLarge) return
        lastLargeClock = isLarge
        mainHandler.post { clockSizeListeners.forEach { it(isLarge) } }
    }

    fun addAnchor(anchor: Anchor, key: Int, isEnabled: () -> Boolean, factory: (Context) -> View) {
        anchors[anchor] = ElementHost(key, isEnabled, factory)
        hookElementFactory()
    }

    fun addOverlay(
        key: Int,
        isEnabled: () -> Boolean,
        modifier: () -> Any?,
        factory: (Context) -> View
    ) {
        overlays.removeAll { it.key == key }
        overlays += Overlay(key, isEnabled, modifier, factory)
        hookLockscreenContent()
    }

    private fun hookElementFactory() {
        if (factoryHooked) return
        factoryHooked = true
        if (!ComposeViewHost.isAvailable) return

        val factoryClass = findClass(ELEMENT_FACTORY_CLASS, suppressError = true)

        factoryClass
            .hookMethod("LockscreenElement")
            .suppressError()
            .runBefore { param ->
                val key = param.args.getOrNull(1) ?: return@runBefore
                if (clockRegionKeys.none { it == key }) return@runBefore
                dispatchClockSize(key)
                regionStack.addLast(key == largeClockRegionKey)

                val replacement = replacement ?: return@runBefore
                if (!replacement.isEnabled()) return@runBefore

                val composer = composerOf(param) ?: return@runBefore
                ComposeViewHost.emitInGroup(composer, replacement.key, modifierOf(param), replacement.factory)
                param.result = null
            }

        factoryClass
            .hookMethod("LockscreenElement")
            .suppressError()
            .runAfter { param ->
                val key = param.args.getOrNull(1) ?: return@runAfter
                if (clockRegionKeys.any { it == key }) {
                    regionStack.removeLastOrNull()
                    return@runAfter
                }
                if (param.throwable != null) return@runAfter

                val inLargeRegion = regionStack.lastOrNull() == true
                val anchor = when {
                    smartspaceKeys.any { it == key } -> if (inLargeRegion) {
                        Anchor.LARGE_CLOCK_SMARTSPACE
                    } else {
                        Anchor.SMALL_CLOCK_SMARTSPACE
                    }

                    inLargeRegion && isLargeClockAnchor(key) -> Anchor.LARGE_CLOCK_DATE
                    else -> null
                } ?: return@runAfter
                val target = anchors[anchor] ?: return@runAfter
                if (!target.isEnabled()) return@runAfter

                val composer = composerOf(param) ?: return@runAfter
                ComposeViewHost.emitInGroup(composer, target.key, factory = target.factory)
            }
    }

    private fun isLargeClockAnchor(key: Any): Boolean {
        if (key == largeClockBelowKey) {
            largeClockBelowSeen = true
            return true
        }
        return !largeClockBelowSeen && key == largeClockKey
    }

    private fun composerOf(param: MethodHookParam): Any? {
        val method = param.method as Method
        return param.args.getOrNull(ComposeToolkit.parameterIndex(method, ComposeToolkit.COMPOSER_CLASS))
    }

    private fun modifierOf(param: MethodHookParam): Any? {
        val method = param.method as Method
        return param.args.getOrNull(ComposeToolkit.parameterIndex(method, ComposeToolkit.MODIFIER_CLASS))
    }

    fun addContent(order: Int, key: Int, elementName: String, factory: (Context) -> View) {
        layers.removeAll { it.key == key }
        layers += Layer(order, key, elementName, factory)
        val sorted = layers.sortedBy { it.order }
        layers.clear()
        layers.addAll(sorted)
        hookLockscreenContent()
    }

    private fun hookLockscreenContent() {
        if (hooked) return
        hooked = true
        if (!ComposeViewHost.isAvailable) return

        findClass(LOCKSCREEN_CONTENT_CLASS, suppressError = true)
            .hookMethod("Content")
            .suppressError()
            .runAfter { param ->
                if (layers.isEmpty() && overlays.isEmpty()) return@runAfter

                val method = param.method as Method
                val composer = param.args.getOrNull(
                    ComposeToolkit.parameterIndex(method, ComposeToolkit.COMPOSER_CLASS)
                ) ?: return@runAfter
                val scope = param.args.getOrNull(
                    ComposeToolkit.parameterIndex(method, ComposeToolkit.CONTENT_SCOPE_CLASS)
                )

                overlays.forEach { overlay ->
                    if (!overlay.isEnabled()) return@forEach
                    ComposeViewHost.emitInGroup(composer, overlay.key, overlay.modifier(), overlay.factory)
                }

                layers.forEach { layer ->
                    val elementKey = ComposeViewHost.elementKey(layer.elementName)

                    if (scope != null && elementKey != null) {
                        ComposeViewHost.emitElement(scope, elementKey, composer, layer.key, layer.factory)
                    } else {
                        ComposeViewHost.emitInGroup(composer, layer.key, factory = layer.factory)
                    }
                }
            }
    }

    private const val LOCKSCREEN_CONTENT_CLASS =
        "$SYSTEMUI_PACKAGE.keyguard.ui.composable.LockscreenContent"
    private const val ELEMENT_FACTORY_CLASS =
        "$SYSTEMUI_PACKAGE.keyguard.ui.composable.elements.LockscreenElementFactoryImpl"
    private const val CLOCK_KEYS_CLASS =
        "$SYSTEMUI_PACKAGE.plugins.keyguard.ui.composable.elements.LockscreenElementKeys\$Clock"
    private const val DWA_LARGE_CLOCK_KEYS_CLASS =
        "$SYSTEMUI_PACKAGE.plugins.keyguard.ui.composable.elements.LockscreenElementKeys\$Smartspace\$DWA\$LargeClock"
    private const val SMARTSPACE_KEYS_CLASS =
        "$SYSTEMUI_PACKAGE.plugins.keyguard.ui.composable.elements.LockscreenElementKeys\$Smartspace"
    private const val REGION_CLOCK_KEYS_CLASS =
        "$SYSTEMUI_PACKAGE.plugins.keyguard.ui.composable.elements.LockscreenElementKeys\$Region\$Clock"
}
