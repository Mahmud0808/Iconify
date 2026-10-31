package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ComposeViewHost
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getStaticFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.SceneContainer
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.Constructor
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.Collections
import java.util.WeakHashMap

@SuppressLint("DiscouragedApi")
class QSBrightnessSlider(context: Context) : ModPack(context) {

    private var brightnessBelowTiles = false
    private var brightnessInQqs = false
    private var mediaOnTop = false
    private var reportedComposeLayout: Boolean? = null

    private var rememberViewModelMethod: Method? = null
    private var sceneBrightnessElementKey: Any? = null
    private var sharedElementKey: Any? = null
    private var brightnessContainerMethod: Method? = null
    private var containerColorsConstructor: Constructor<*>? = null
    private val paddingModifierMethod: Method? by lazy {
        ComposeToolkit.modifierFunction(
            PADDING_KT_CLASS,
            "padding",
            Float::class.javaPrimitiveType,
            Float::class.javaPrimitiveType
        )
    }

    private var qsFragment: Any? = null
    private var qqsScope: Any? = null
    private var qsScope: Any? = null

    private val qsBrightnessSlots = WeakHashMap<Any, Any>()
    private val qqsTilesSlots = WeakHashMap<Any, Any>()
    private val mediaRowSlots = WeakHashMap<Any, Pair<Any, Any>>()
    private val sceneMediaRowSlots = WeakHashMap<Any, Pair<Any, Any>>()
    private val arrangedFirstSlots: MutableSet<Any> = Collections.newSetFromMap(WeakHashMap())
    private var emptyComposableSlot: Any? = null
    private var emptyScopedComposableSlot: Any? = null

    private var punchHoleProvider: Any? = null
    private var shadePunchHole: Any? = null

    private var shadeSceneViewModel: Any? = null
    private var shadeScope: Any? = null
    private val shadeQqsSlots = WeakHashMap<Any, Any>()

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            brightnessBelowTiles = getBoolean(XposedKey.QS_BRIGHTNESS_SLIDER_BOTTOM)
            brightnessInQqs = getBoolean(XposedKey.QQS_BRIGHTNESS_SLIDER)
            mediaOnTop = getBoolean(XposedKey.QS_MEDIA_PLAYER_TOP)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        resolveComposeApis()
        hookQsLayoutDetection()
        hookQsFragmentCompose()
        hookSceneContainer()
    }

    private fun hookQsLayoutDetection() {
        if (SceneContainer.isEnabled) {
            reportComposeLayout(true)
            return
        }

        findClass("$SYSTEMUI_PACKAGE.qs.composefragment.QSFragmentCompose", suppressError = true)
            .hookConstructor()
            .suppressError()
            .runAfter { reportComposeLayout(true) }

        findClass(
            "$SYSTEMUI_PACKAGE.qs.QSFragmentLegacy",
            "$SYSTEMUI_PACKAGE.qs.QSFragment",
            suppressError = true
        )
            .hookConstructor()
            .suppressError()
            .runAfter { reportComposeLayout(false) }
    }

    private fun reportComposeLayout(active: Boolean) {
        if (reportedComposeLayout == active) return
        reportedComposeLayout = active

        Thread {
            if (Xprefs.getBoolean(XposedKey.QS_COMPOSE_LAYOUT) == active) return@Thread
            Xprefs.boundedCommit { it.putBoolean(XposedKey.QS_COMPOSE_LAYOUT.name, active) }
        }.start()
    }

    private fun mediaFirstOrder(
        param: XC_MethodHook.MethodHookParam,
        brightness: Any,
        tiles: Any,
        media: Any
    ): Boolean = arrangeSlots(
        param,
        0 to media,
        1 to if (brightnessBelowTiles) tiles else brightness,
        2 to if (brightnessBelowTiles) brightness else tiles
    )

    private fun arrangeSlots(
        param: XC_MethodHook.MethodHookParam,
        vararg slots: Pair<Int, Any?>
    ): Boolean {
        if (!ComposeToolkit.setSlots(param, *slots)) return false
        param.args[slots.first().first]?.let { arrangedFirstSlots.add(it) }
        return true
    }

    private fun List<*>.withMediaFirst(): List<*>? {
        val media = firstOrNull { (it as? Enum<*>)?.name == QS_SHADE_MEDIA_COMPONENT }
            ?: return null
        if (firstOrNull() === media) return null
        return listOf(media) + filter { it !== media }
    }

    private fun hookQsFragmentCompose() {
        val qsFragmentClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.composefragment.QSFragmentCompose",
            suppressError = true
        ) ?: return
        val qsLayoutClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.composefragment.QSFragmentComposeKt",
            suppressError = true
        ) ?: return

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
                if (!brightnessBelowTiles && !brightnessInQqs && !mediaOnTop) return@runBefore
                if (param.args.size < 3) return@runBefore

                val brightness = param.args[0] ?: return@runBefore
                if (brightness in arrangedFirstSlots) return@runBefore

                val tiles = param.args[1] ?: return@runBefore
                val brightnessSlot = if (brightnessInQqs) {
                    sharedQsBrightnessSlot(brightness) ?: brightness
                } else {
                    brightness
                }

                val mediaInRowIndex = param.args.indexOfFirst { it is Boolean }
                val mediaInRow = param.args.getOrNull(mediaInRowIndex) as? Boolean
                val media = param.args[2]

                if (mediaOnTop && mediaInRow == false && media != null) {
                    if (mediaFirstOrder(param, brightnessSlot, tiles, media)) return@runBefore
                }

                if (!brightnessBelowTiles) {
                    ComposeToolkit.setSlots(param, 0 to brightnessSlot)
                    return@runBefore
                }
                val emptySlot = emptySlot()
                if (mediaInRow != true || media == null || emptySlot == null) {
                    arrangeSlots(param, 0 to tiles, 1 to brightnessSlot)
                    return@runBefore
                }

                val method = param.method as Method
                val composer = param.args.getOrNull(
                    method.parameterTypes.indexOfFirst { it.name == COMPOSER_CLASS }
                ) ?: return@runBefore
                val tilesAndMediaRow = mediaRowSlots[tiles]
                    ?.takeIf { it.first === media }
                    ?.second
                    ?: composableSlot { rowComposer, _ ->
                        composeOriginalLayout(method, emptySlot, tiles, media, true, rowComposer)
                    }?.also {
                        mediaRowSlots[tiles] = media to it
                        arrangedFirstSlots.add(it)
                    }
                    ?: return@runBefore

                ComposeToolkit.startGroup(composer, LANDSCAPE_LAYOUT_GROUP_KEY)
                try {
                    composeOriginalLayout(
                        method,
                        tilesAndMediaRow,
                        brightnessSlot,
                        emptySlot,
                        false,
                        composer
                    )
                } finally {
                    ComposeToolkit.endGroup(composer)
                }
                param.result = null
            }

        qsLayoutClass
            .hookMethod("QuickQuickSettingsLayout")
            .suppressError()
            .runBefore { param ->
                if (!brightnessInQqs && !mediaOnTop) return@runBefore

                val mediaInRow = param.args.firstOrNull { it is Boolean } as? Boolean
                if (mediaInRow != false) return@runBefore

                val tiles = param.args.firstOrNull() ?: return@runBefore
                if (tiles in qqsTilesSlots.values) return@runBefore

                val tilesSlot = if (brightnessInQqs) {
                    qqsTilesSlots[tiles] ?: composableSlot { composer, changed ->
                        if (brightnessBelowTiles) {
                            tiles.callMethod("invoke", composer, changed)
                            composeQqsBrightness(composer)
                        } else {
                            composeQqsBrightness(composer)
                            tiles.callMethod("invoke", composer, changed)
                        }
                    }?.also { qqsTilesSlots[tiles] = it } ?: return@runBefore
                } else {
                    tiles
                }
                if (!ComposeToolkit.setSlots(param, 0 to tilesSlot)) return@runBefore

                if (!mediaOnTop) return@runBefore

                val parameterTypes = (param.method as Method).parameterTypes
                val slotIndices = parameterTypes.indices
                    .filter { ComposeToolkit.isSlotType(parameterTypes[it]) }
                if (slotIndices.size != 2) return@runBefore

                val media = param.args[slotIndices[1]] ?: return@runBefore
                ComposeToolkit.setSlots(param, slotIndices[0] to media, slotIndices[1] to tilesSlot)
            }
    }

    private fun hookSceneContainer() {
        findClass(
            "$SYSTEMUI_PACKAGE.qs.ui.composable.QuickSettingsContentKt",
            suppressError = true
        )
            .hookMethodMatchPattern("QuickSettingsPanelLayout.*")
            .suppressError()
            .runBefore { param ->
                if (!brightnessBelowTiles && !mediaOnTop) return@runBefore
                if (param.args.size < 3) return@runBefore

                val brightness = param.args[0] ?: return@runBefore
                if (brightness in arrangedFirstSlots) return@runBefore
                val tiles = param.args[1] ?: return@runBefore

                val method = param.method as Method
                val mediaInRowIndex = method.parameterTypes.indexOfFirst {
                    it == Boolean::class.javaPrimitiveType
                }
                val mediaInRow = param.args.getOrNull(mediaInRowIndex) as? Boolean
                val mediaVisible = method.parameterTypes.indices
                    .filter { method.parameterTypes[it] == Boolean::class.javaPrimitiveType }
                    .getOrNull(1)
                    ?.let { param.args[it] as? Boolean }
                val media = param.args[2]

                if (mediaOnTop && mediaInRow == false && mediaVisible == true && media != null) {
                    if (mediaFirstOrder(param, brightness, tiles, media)) return@runBefore
                }

                if (!brightnessBelowTiles) return@runBefore
                val emptySlot = emptyScopedSlot()
                val composer = param.args.getOrNull(
                    ComposeToolkit.parameterIndex(method, COMPOSER_CLASS)
                )

                if (mediaInRow != true || media == null || emptySlot == null || composer == null) {
                    arrangeSlots(param, 0 to tiles, 1 to brightness)
                    return@runBefore
                }

                val originalArgs = param.args.clone()
                val tilesAndMediaRow = sceneMediaRowSlots[tiles]
                    ?.takeIf { it.first === media }
                    ?.second
                    ?: ComposeToolkit.function3("IconifyQsTilesMediaRow") { _, rowComposer, _ ->
                        invokeScenePanelLayout(
                            method,
                            originalArgs,
                            arrayOf(emptySlot, tiles, media),
                            mediaInRowIndex,
                            true,
                            ComposeToolkit.emptyModifier,
                            rowComposer!!
                        )
                    }?.also {
                        sceneMediaRowSlots[tiles] = media to it
                        arrangedFirstSlots.add(it)
                    }
                    ?: return@runBefore

                ComposeToolkit.inGroup(composer, SCENE_LANDSCAPE_LAYOUT_GROUP_KEY) {
                    invokeScenePanelLayout(
                        method,
                        originalArgs,
                        arrayOf(tilesAndMediaRow, brightness, emptySlot),
                        mediaInRowIndex,
                        false,
                        null,
                        composer
                    )
                }
                param.result = null
            }

        findClass(
            "$SYSTEMUI_PACKAGE.qs.ui.composable.QuickSettingsShadeOverlayKt",
            suppressError = true
        )
            .hookMethod("QuickSettingsLayoutContent")
            .suppressError()
            .runBefore { param ->
                if (!brightnessBelowTiles) return@runBefore

                val slots = (param.method as Method).parameterTypes
                    .withIndex()
                    .filter { ComposeToolkit.isSlotType(it.value) }
                    .map { it.index }
                if (slots.size != 3) return@runBefore

                val brightness = param.args[slots[1]] ?: return@runBefore
                if (brightness in arrangedFirstSlots) return@runBefore
                val tiles = param.args[slots[2]] ?: return@runBefore

                arrangeSlots(param, slots[1] to tiles, slots[2] to brightness)
            }

        hookShadeTransitionBrightnessFade()
        hookNotificationPunchHole()

        val shadeSceneClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeSceneKt",
            suppressError = true
        ) ?: return

        shadeSceneClass
            .hookMethod("SingleShade")
            .suppressError()
            .runBefore { param ->
                param.args.forEach { arg ->
                    when (arg?.javaClass?.name) {
                        SHADE_SCENE_VIEW_MODEL_CLASS -> shadeSceneViewModel = arg
                    }
                    if (arg != null && isContentScope(arg)) shadeScope = arg
                }
            }

        shadeSceneClass
            .hookMethodMatchPattern("MediaAndQqsLayout.*")
            .suppressError()
            .runBefore { param ->
                if ((!brightnessInQqs && !mediaOnTop) || isSplitShade()) return@runBefore

                val mediaInRow = param.args.firstOrNull { it is Boolean } as? Boolean
                if (mediaInRow != false) return@runBefore

                val qqs = param.args.firstOrNull() ?: return@runBefore
                if (qqs in shadeQqsSlots.values) return@runBefore

                val qqsSlot = if (brightnessInQqs) {
                    shadeQqsSlots[qqs] ?: composableSlot { composer, changed ->
                        if (brightnessBelowTiles) {
                            qqs.callMethod("invoke", composer, changed)
                            composeShadeBrightness(composer)
                        } else {
                            composeShadeBrightness(composer)
                            qqs.callMethod("invoke", composer, changed)
                        }
                    }?.also { shadeQqsSlots[qqs] = it } ?: return@runBefore
                } else {
                    qqs
                }
                if (!ComposeToolkit.setSlots(param, 0 to qqsSlot)) return@runBefore

                if (!mediaOnTop) return@runBefore

                val orderIndex = (param.method as Method).parameterTypes
                    .indexOfFirst { it == List::class.java }
                val order = param.args.getOrNull(orderIndex) as? List<*>
                if (order != null) {
                    order.withMediaFirst()?.let { param.args[orderIndex] = it }
                } else {
                    val media = param.args[1] ?: return@runBefore
                    ComposeToolkit.setSlots(param, 0 to media, 1 to qqsSlot)
                }
            }
    }

    private fun hookShadeTransitionBrightnessFade() {
        val builderClass = findClass(TRANSITION_BUILDER_CLASS, suppressError = true) ?: return
        val fadeMethod = builderClass.declaredMethods.firstOrNull { method ->
            method.name.startsWith("fade") &&
                    Modifier.isStatic(method.modifiers) &&
                    method.parameterTypes.size == 2 &&
                    method.parameterTypes[0] == builderClass &&
                    method.parameterTypes[1].name == ELEMENT_MATCHER_CLASS
        }?.apply { isAccessible = true } ?: return

        val qsElementsClass = findClass(QS_ELEMENTS_CLASS, suppressError = true)
        val qsContentKey = qsElementsClass.getStaticFieldSilently("QuickSettingsContent")
        val gridAnchorKey = qsElementsClass.getStaticFieldSilently("GridAnchor")

        if (qsContentKey != null && gridAnchorKey != null) {
            builderClass
                .hookMethod("anchoredTranslate")
                .suppressError()
                .runBefore { param ->
                    if (!brightnessBelowTiles) return@runBefore
                    if (param.args.getOrNull(0) !== qsContentKey) return@runBefore
                    if (param.args.getOrNull(1) !== gridAnchorKey) return@runBefore

                    param.result = null
                }
        }

        findClass(TO_SINGLE_SHADE_TRANSITION_CLASS, suppressError = true)
            .hookMethod("toSingleShadeTransition")
            .suppressError()
            .runAfter { param ->
                if (!brightnessInQqs) return@runAfter

                val builder = param.args.firstOrNull() ?: return@runAfter
                val key = sceneBrightnessElementKey ?: sharedElementKey ?: return@runAfter
                fadeMethod.invoke(null, builder, key)
            }
    }

    private fun hookNotificationPunchHole() {
        val elementClass = findClass(PUNCH_HOLE_ELEMENT_CLASS, suppressError = true) ?: return

        elementClass
            .hookMethod("create")
            .suppressError()
            .runBefore { param ->
                if (param.thisObject === shadePunchHole) return@runBefore
                param.thisObject.getFieldSilently("roundRectProvider")?.let {
                    punchHoleProvider = it
                }
            }

        val provider = function0Proxy { punchHoleProvider?.callMethod("invoke") } ?: return
        shadePunchHole = try {
            allocateInstance(elementClass)?.apply { setField("roundRectProvider", provider) }
        } catch (throwable: Throwable) {
            log(this@QSBrightnessSlider, throwable)
            null
        }
    }

    private fun allocateInstance(clazz: Class<*>): Any? {
        val unsafeClass = Class.forName("sun.misc.Unsafe")
        val unsafe = unsafeClass.getDeclaredField("theUnsafe")
            .apply { isAccessible = true }
            .get(null)
        return unsafeClass.getMethod("allocateInstance", Class::class.java).invoke(unsafe, clazz)
    }

    private fun composeShadeBrightness(composer: Any) {
        val containerViewModel = rememberShadeContainerViewModel(composer) ?: return
        val brightnessViewModel = containerViewModel
            .getFieldSilently("brightnessSliderViewModel") ?: return
        val scope = shadeScope
        val key = sceneBrightnessElementKey ?: sharedElementKey
        val sliderModifier = sceneSliderModifier()
        val punchHole = shadePunchHole
        val modifier = if (sliderModifier != null && punchHole != null) {
            ComposeToolkit.then(sliderModifier, punchHole) ?: sliderModifier
        } else {
            sliderModifier
        }

        if (scope != null && key != null) {
            composeElement(scope, key, composer) {
                composeBrightnessContainer(composer, brightnessViewModel, modifier)
            }
        } else {
            composeBrightnessContainer(composer, brightnessViewModel, modifier)
        }
    }

    private fun rememberShadeContainerViewModel(composer: Any): Any? {
        val method = rememberViewModelMethod ?: return null
        val factory = shadeSceneViewModel?.getFieldSilently("qsContainerViewModelFactory")
            ?: return null
        val provider = function0Proxy { factory.callMethod("create", false) } ?: return null

        val parameterTypes = method.parameterTypes
        val composerIndex = parameterTypes.indexOfFirst { it.name == COMPOSER_CLASS }
        if (composerIndex == -1) return null

        var defaultMask = 0
        var stringFilled = false
        val args = arrayOfNulls<Any>(parameterTypes.size)

        parameterTypes.forEachIndexed { index, type ->
            args[index] = when {
                index == composerIndex -> composer
                index > composerIndex -> 0
                type == String::class.java && !stringFilled -> {
                    stringFilled = true
                    QQS_BRIGHTNESS_TRACE
                }

                type.name == FUNCTION0_CLASS -> provider
                else -> {
                    defaultMask = defaultMask or (1 shl index)
                    null
                }
            }
        }

        if (parameterTypes.size - composerIndex - 1 == 2) {
            args[parameterTypes.size - 1] = defaultMask
        }

        return method.invoke(null, *args)
    }

    private fun isContentScope(arg: Any): Boolean {
        return arg.javaClass.interfaces.any { it.name == CONTENT_SCOPE_CLASS } ||
                arg.javaClass.methods.any { it.name == "Element" && it.parameterTypes.size == 5 }
    }

    private fun isSplitShade(): Boolean {
        val id = mContext.resources.getIdentifier(
            "config_use_split_notification_shade",
            "bool",
            SYSTEMUI_PACKAGE
        )
        return id != 0 && mContext.resources.getBoolean(id)
    }

    private fun resolveComposeApis() {
        rememberViewModelMethod = findClass(
            "$SYSTEMUI_PACKAGE.lifecycle.SysUiViewModelKt",
            suppressError = true
        )?.declaredMethods?.firstOrNull { it.name == "rememberViewModel" }

        sceneBrightnessElementKey = try {
            findClass(
                "$SYSTEMUI_PACKAGE.qs.shared.ui.QuickSettings\$Elements",
                suppressError = true
            )?.getDeclaredField("BrightnessSlider")
                ?.apply { isAccessible = true }
                ?.get(null)
        } catch (_: Throwable) {
            null
        }

        sharedElementKey = ComposeViewHost.elementKey(SHARED_ELEMENT_NAME)

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
            composeElement(scope, key, composer) { composeQsFragmentBrightness(composer) }
        } else {
            composeQsFragmentBrightness(composer)
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

        elementMethod.invoke(scope, key, ComposeToolkit.emptyModifier, elementContent, composer, 0)
    }

    private fun composeQsFragmentBrightness(composer: Any) {
        val viewModel = qsFragment
            .getFieldSilently("viewModel")
            .getFieldSilently("containerViewModel")
            .getFieldSilently("brightnessSliderViewModel") ?: return
        composeBrightnessContainer(composer, viewModel)
    }

    private fun sceneSliderModifier(): Any? {
        val empty = ComposeToolkit.emptyModifier
        val method = paddingModifierMethod ?: return empty
        val resources = mContext.resources
        val id = resources.getIdentifier(QS_BRIGHTNESS_MARGIN_DIMEN, "dimen", SYSTEMUI_PACKAGE)
        if (id == 0) return empty

        val verticalDp = resources.getDimension(id) / resources.displayMetrics.density
        return try {
            method.invoke(null, empty, 0f, verticalDp) ?: empty
        } catch (_: Throwable) {
            empty
        }
    }

    private fun composeBrightnessContainer(
        composer: Any,
        viewModel: Any,
        modifier: Any? = ComposeToolkit.emptyModifier
    ) {
        val method = brightnessContainerMethod ?: return
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
                type.name == COMPOSE_MODIFIER_CLASS -> modifier
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
                ComposeToolkit.colorOf(0),
                ComposeToolkit.colorOf(mirrorColor)
            )
        } catch (throwable: Throwable) {
            log(this@QSBrightnessSlider, throwable)
            null
        }
    }

    private fun emptySlot(): Any? {
        return emptyComposableSlot ?: composableSlot { _, _ -> }?.also {
            emptyComposableSlot = it
            arrangedFirstSlots.add(it)
        }
    }

    private fun composeOriginalLayout(
        method: Method,
        brightness: Any,
        tiles: Any,
        media: Any,
        mediaInRow: Boolean,
        composer: Any
    ) {
        val slots = arrayOf(brightness, tiles, media)
        var slotIndex = 0

        val args = method.parameterTypes.map { type ->
            when {
                ComposeToolkit.isSlotType(type) ->
                    ComposeToolkit.fitSlot(type, slots.getOrNull(slotIndex++)) ?: return
                type == Boolean::class.javaPrimitiveType -> mediaInRow
                type.name == COMPOSER_CLASS -> composer
                type == Int::class.javaPrimitiveType -> 0
                else -> null
            }
        }.toTypedArray()

        XposedBridge.invokeOriginalMethod(method, null, args)
    }

    private fun emptyScopedSlot(): Any? {
        return emptyScopedComposableSlot
            ?: ComposeToolkit.function3("IconifyEmptySlot") { _, _, _ -> }
                ?.also {
                    emptyScopedComposableSlot = it
                    arrangedFirstSlots.add(it)
                }
    }

    private fun invokeScenePanelLayout(
        method: Method,
        originalArgs: Array<Any?>,
        slots: Array<Any>,
        mediaInRowIndex: Int,
        mediaInRow: Boolean,
        modifier: Any?,
        composer: Any
    ) {
        val args = originalArgs.clone()
        val types = method.parameterTypes
        slots.forEachIndexed { index, slot ->
            args[index] = ComposeToolkit.fitSlot(types[index], slot) ?: return
        }
        args[mediaInRowIndex] = mediaInRow
        if (modifier != null) {
            val modifierIndex = ComposeToolkit.parameterIndex(method, COMPOSE_MODIFIER_CLASS)
            if (modifierIndex != -1) args[modifierIndex] = modifier
        }
        val composerIndex = ComposeToolkit.parameterIndex(method, COMPOSER_CLASS)
        args[composerIndex] = composer
        for (index in composerIndex + 1 until args.size) args[index] = 0

        XposedBridge.invokeOriginalMethod(method, null, args)
    }

    private fun composableSlot(block: (composer: Any, changed: Any?) -> Unit): Any? =
        ComposeToolkit.function2("IconifyBrightnessSlot") { composer, changed ->
            block(composer!!, changed)
        }

    private fun function0Proxy(block: () -> Any?): Any? =
        ComposeToolkit.function0("IconifyViewModelFactory", block)

    private fun composableContent(block: () -> Unit): Any? =
        ComposeToolkit.function3("IconifyBrightnessElement") { _, _, _ -> block() }

    companion object {
        private const val SHARED_ELEMENT_NAME = "IconifyBrightnessSlider"
        private const val QS_SHADE_MEDIA_COMPONENT = "MEDIA"
        private const val PADDING_KT_CLASS = "androidx.compose.foundation.layout.PaddingKt"
        private const val QS_BRIGHTNESS_MARGIN_DIMEN = "qs_brightness_margin_top"
        private const val TRANSITION_BUILDER_CLASS =
            "com.android.compose.animation.scene.TransitionBuilderImpl"
        private const val ELEMENT_MATCHER_CLASS =
            "com.android.compose.animation.scene.ElementMatcher"
        private const val QS_ELEMENTS_CLASS =
            $$"$$SYSTEMUI_PACKAGE.qs.shared.ui.QuickSettings$Elements"
        private const val TO_SINGLE_SHADE_TRANSITION_CLASS =
            "$SYSTEMUI_PACKAGE.scene.ui.composable.transitions.ToSingleShadeTransitionKt"
        private const val COMPOSER_CLASS = ComposeToolkit.COMPOSER_CLASS
        private const val LANDSCAPE_LAYOUT_GROUP_KEY = 0x1C0B5A1D
        private const val SCENE_LANDSCAPE_LAYOUT_GROUP_KEY = 0x1C0B5A1E
        private const val FUNCTION0_CLASS = ComposeToolkit.FUNCTION0_CLASS
        private const val CONTENT_SCOPE_CLASS = ComposeToolkit.CONTENT_SCOPE_CLASS
        private const val SHADE_SCENE_VIEW_MODEL_CLASS =
            "$SYSTEMUI_PACKAGE.shade.ui.viewmodel.ShadeSceneContentViewModel"
        private const val QQS_BRIGHTNESS_TRACE = "iconify_qqs_brightness"
        private const val PUNCH_HOLE_ELEMENT_CLASS =
            "$SYSTEMUI_PACKAGE.notifications.ui.modifier.DrawPunchHoleElement"
        private const val COMPOSE_MODIFIER_CLASS = ComposeToolkit.MODIFIER_CLASS
        private const val BRIGHTNESS_VIEW_MODEL_CLASS =
            "$SYSTEMUI_PACKAGE.brightness.ui.viewmodel.BrightnessSliderViewModel"
        private const val CONTAINER_COLORS_CLASS =
            "$SYSTEMUI_PACKAGE.brightness.ui.compose.ContainerColors"
    }
}
