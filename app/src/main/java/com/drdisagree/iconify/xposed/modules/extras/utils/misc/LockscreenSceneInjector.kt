package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
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
    private val behindLayers = CopyOnWriteArrayList<Pair<Int, ElementHost>>()
    private val clockRegionOverlays = CopyOnWriteArrayList<Overlay>()
    private var hooked = false
    private var replacement: ElementHost? = null
    private val anchors = HashMap<Anchor, ElementHost>()
    private var factoryHooked = false
    private val clockSizeListeners = CopyOnWriteArrayList<(Boolean) -> Unit>()
    private var lastLargeClock: Boolean? = null
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    @Volatile
    private var lockscreenAlpha = 1f
    private var transitionHooked = false

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

    fun addAboveClockRegion(
        key: Int,
        isEnabled: () -> Boolean,
        modifier: () -> Any?,
        factory: (Context) -> View
    ) {
        clockRegionOverlays.removeAll { it.key == key }
        clockRegionOverlays += Overlay(key, isEnabled, modifier, factory)
        hookElementFactory()
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

                val replacement = replacement?.takeIf { it.isEnabled() }
                val composer = if (replacement != null) composerOf(param) else null
                if (replacement == null || composer == null) {
                    regionStack.addLast(key == largeClockRegionKey)
                    return@runBefore
                }

                ComposeViewHost.emitInGroup(composer, replacement.key, modifierOf(param), replacement.factory)
                emitClockRegionOverlays(composer)
                param.setObjectExtra(REPLACED_EXTRA, true)
                param.result = null
            }

        factoryClass
            .hookMethod("LockscreenElement")
            .suppressError()
            .runAfter { param ->
                val key = param.args.getOrNull(1) ?: return@runAfter
                if (clockRegionKeys.any { it == key }) {
                    if (param.getObjectExtra(REPLACED_EXTRA) == true) return@runAfter
                    regionStack.removeLastOrNull()
                    if (param.throwable != null) return@runAfter

                    composerOf(param)?.let { emitClockRegionOverlays(it) }
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

    private fun emitClockRegionOverlays(composer: Any) {
        clockRegionOverlays.forEach { overlay ->
            if (!overlay.isEnabled()) return@forEach
            ComposeViewHost.emitInGroup(composer, overlay.key, overlay.modifier(), overlay.factory)
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

    fun addBehindContent(
        order: Int,
        key: Int,
        isEnabled: () -> Boolean,
        factory: (Context) -> View
    ) {
        behindLayers.removeAll { it.second.key == key }
        behindLayers += order to ElementHost(key, isEnabled, factory)
        val sorted = behindLayers.sortedBy { it.first }
        behindLayers.clear()
        behindLayers.addAll(sorted)
        hookLockscreenContent()
    }

    private fun hookLockscreenContent() {
        if (hooked) return
        hooked = true
        if (!ComposeViewHost.isAvailable) return

        val lockscreenContentClass = findClass(LOCKSCREEN_CONTENT_CLASS, suppressError = true)
        hookKeyguardTransitions()

        lockscreenContentClass
            .hookMethod("Content")
            .suppressError()
            .runBefore { param ->
                if (behindLayers.isEmpty()) return@runBefore

                val composer = composerOf(param) ?: return@runBefore
                behindLayers.forEach { (_, layer) ->
                    if (layer.isEnabled()) {
                        ComposeViewHost.emitInGroup(composer, layer.key, factory = fading(layer.factory))
                    }
                }
            }

        lockscreenContentClass
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
                    ComposeViewHost.emitInGroup(composer, overlay.key, overlay.modifier(), fading(overlay.factory))
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

    private fun hookKeyguardTransitions() {
        if (transitionHooked) return
        transitionHooked = true

        findClass(KEYGUARD_TRANSITION_REPOSITORY_CLASS, suppressError = true)
            .hookMethod("emitTransition")
            .suppressError()
            .runAfter { param ->
                val step = param.args.getOrNull(0) ?: return@runAfter
                val from = step.getFieldSilently("from")?.toString() ?: return@runAfter
                val to = step.getFieldSilently("to")?.toString() ?: return@runAfter
                val value = (step.getFieldSilently("value") as? Float ?: return@runAfter)
                    .coerceIn(0f, 1f)

                lockscreenAlpha = when {
                    from.isLockscreenState() && !to.isLockscreenState() ->
                        (1f - value / FADE_OUT_END).coerceIn(0f, 1f)

                    to.isLockscreenState() && !from.isLockscreenState() -> value
                    else -> lockscreenAlpha
                }
            }
    }

    private fun String.isLockscreenState(): Boolean = this in LOCKSCREEN_STATES

    private fun fading(factory: (Context) -> View): (Context) -> View = { context ->
        factory(context).also { view ->
            ComposeViewHost.runBeforeEachDraw(view) {
                val alpha = lockscreenAlpha
                if (view.alpha != alpha) view.alpha = alpha
            }
        }
    }

    private const val REPLACED_EXTRA = "iconify_clock_region_replaced"
    private const val KEYGUARD_TRANSITION_REPOSITORY_CLASS =
        "$SYSTEMUI_PACKAGE.keyguard.data.repository.KeyguardTransitionRepositoryImpl"
    private const val FADE_OUT_END = 0.6f
    private val LOCKSCREEN_STATES = setOf("LOCKSCREEN", "AOD", "DOZING")
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
