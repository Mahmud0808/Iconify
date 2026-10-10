package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.view.View
import android.view.ViewTreeObserver
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import java.lang.reflect.Method
import java.util.Collections
import java.util.WeakHashMap

object ComposeViewHost {

    private const val ELEMENT_KEY_CLASS = "com.android.compose.animation.scene.ElementKey"
    private const val ELEMENT_KEY_DEFAULTS = 14
    private const val UPDATE_DEFAULT_MASK = 1 shl 2

    private val hiddenModifiers: MutableSet<Any> = Collections.newSetFromMap(WeakHashMap())
    private val elementKeys = HashMap<String, Any>()

    private val androidViewMethod: Method? by lazy {
        ComposeToolkit.staticMethod(
            "androidx.compose.ui.viewinterop.AndroidView_androidKt",
            "AndroidView/6"
        ) { method ->
            val types = method.parameterTypes
            method.name == "AndroidView" &&
                    types.size == 6 &&
                    types[0].name == ComposeToolkit.FUNCTION1_CLASS &&
                    types[1].name == ComposeToolkit.MODIFIER_CLASS &&
                    types[2].name == ComposeToolkit.FUNCTION1_CLASS &&
                    types[3].name == ComposeToolkit.COMPOSER_CLASS
        }
    }

    private val alphaModifier: Method? by lazy {
        ComposeToolkit.modifierFunction(
            "androidx.compose.ui.draw.AlphaKt",
            "alpha",
            Float::class.javaPrimitiveType
        )
    }

    private val offsetModifierMethod: Method? by lazy {
        ComposeToolkit.modifierFunction(
            "androidx.compose.foundation.layout.OffsetKt",
            "offset",
            ComposeToolkit.functionClass(1)
        )
    }

    private val intOffsetBox: Method? by lazy {
        ComposeToolkit.staticMethod("androidx.compose.ui.unit.IntOffset", "box-impl") {
            it.name == "box-impl" && it.parameterTypes.size == 1
        }
    }

    fun offsetModifier(offset: () -> Pair<Int, Int>): Any? = try {
        val provider = ComposeToolkit.function1("IconifyOffset") {
            val (x, y) = offset()
            intOffsetBox?.invoke(null, (x.toLong() shl 32) or (y.toLong() and 0xFFFFFFFFL))
        }
        if (provider == null) null
        else offsetModifierMethod?.invoke(null, ComposeToolkit.emptyModifier, provider)
    } catch (_: Throwable) {
        null
    }

    private val layoutModifierMethod: Method? by lazy {
        ComposeToolkit.modifierFunction(
            "androidx.compose.ui.layout.LayoutModifierKt",
            "layout",
            ComposeToolkit.functionClass(3)
        )
    }

    private val constraintsMethod: Method? by lazy {
        ComposeToolkit.staticMethod("androidx.compose.ui.unit.ConstraintsKt", "Constraints/4") {
            it.name == "Constraints" && it.parameterTypes.size == 4
        }
    }

    private val measureMethod: Method? by lazy {
        ComposeToolkit.loadClass("androidx.compose.ui.layout.Measurable")
            ?.methods
            ?.firstOrNull { it.name.startsWith("measure") && it.parameterTypes.size == 1 }
    }

    private val layoutResultMethod: Method? by lazy {
        ComposeToolkit.loadClass("androidx.compose.ui.layout.MeasureScope")
            ?.methods
            ?.firstOrNull {
                it.name == "layout" && it.parameterTypes.size == 4 &&
                        it.parameterTypes[2] == Map::class.java
            }
    }

    private val placeMethod: Method? by lazy {
        ComposeToolkit.loadClass("androidx.compose.ui.layout.Placeable\$PlacementScope")
            ?.methods
            ?.firstOrNull { it.name == "place" && it.parameterTypes.size == 4 }
    }

    private val placementCoordinatesMethod: Method? by lazy {
        ComposeToolkit.loadClass("androidx.compose.ui.layout.Placeable\$PlacementScope")
            ?.methods
            ?.firstOrNull { it.name == "getCoordinates" && it.parameterTypes.isEmpty() }
    }

    private val findRootCoordinatesMethod: Method? by lazy {
        ComposeToolkit.staticMethod(
            "androidx.compose.ui.layout.LayoutCoordinatesKt",
            "findRootCoordinates/1"
        ) { it.name == "findRootCoordinates" && it.parameterTypes.size == 1 }
    }

    private val localPositionOfMethod: Method? by lazy {
        ComposeToolkit.loadClass("androidx.compose.ui.layout.LayoutCoordinates")
            ?.methods
            ?.firstOrNull {
                it.name.startsWith("localPositionOf") && it.parameterTypes.size == 2 &&
                        it.parameterTypes[1] == Long::class.javaPrimitiveType
            }
    }

    private fun positionInRoot(placementScope: Any?): Pair<Int, Int> {
        val coordinates = placementCoordinatesMethod?.invoke(placementScope) ?: return 0 to 0
        val root = findRootCoordinatesMethod?.invoke(null, coordinates) ?: return 0 to 0
        val packed = localPositionOfMethod?.invoke(root, coordinates, 0L) as? Long ?: return 0 to 0
        val x = Float.fromBits((packed ushr 32).toInt())
        val y = Float.fromBits((packed and 0xFFFFFFFFL).toInt())
        return Math.round(x) to Math.round(y)
    }

    fun windowFillModifier(size: () -> Pair<Int, Int>): Any? = try {
        val constraints = constraintsMethod ?: return null
        val measure = measureMethod ?: return null
        val layoutResult = layoutResultMethod ?: return null
        val place = placeMethod ?: return null

        val block = ComposeToolkit.function3("IconifyWindowFill") { scope, measurable, _ ->
            val (width, height) = size()
            val w = width.coerceAtLeast(0)
            val h = height.coerceAtLeast(0)
            val placeable = measure.invoke(measurable, constraints.invoke(null, w, w, h, h))
            val placement = ComposeToolkit.function1("IconifyWindowFillPlacement") { placementScope ->
                val (x, y) = try {
                    positionInRoot(placementScope)
                } catch (_: Throwable) {
                    0 to 0
                }
                place.invoke(placementScope, placeable, -x, -y, 0f)
            }
            layoutResult.invoke(scope, 0, 0, emptyMap<Any, Any>(), placement)
        }
        if (block == null) null
        else layoutModifierMethod?.invoke(null, ComposeToolkit.emptyModifier, block)
    } catch (_: Throwable) {
        null
    }

    val isAvailable: Boolean
        get() = androidViewMethod != null && ComposeToolkit.functionClass(1) != null

    fun emit(composer: Any, modifier: Any? = null, factory: (Context) -> View) {
        val method = androidViewMethod ?: return
        val factoryFunction = ComposeToolkit.function1("IconifyViewFactory") { context ->
            factory(context as Context)
        } ?: return

        method.invoke(
            null,
            factoryFunction,
            modifier ?: ComposeToolkit.emptyModifier,
            null,
            composer,
            0,
            UPDATE_DEFAULT_MASK
        )
    }

    fun emitInGroup(
        composer: Any,
        key: Int,
        modifier: Any? = null,
        factory: (Context) -> View
    ) {
        ComposeToolkit.inGroup(composer, key) { emit(composer, modifier, factory) }
    }

    fun emitElement(scope: Any, key: Any, composer: Any, groupKey: Int, factory: (Context) -> View) {
        val elementMethod = scope.javaClass.methods.firstOrNull {
            it.name == "Element" && it.parameterTypes.size == 5
        }
        val content = ComposeToolkit.function3("IconifyElementContent") { _, contentComposer, _ ->
            emit(contentComposer!!, factory = factory)
        }

        ComposeToolkit.inGroup(composer, groupKey) {
            if (elementMethod == null || content == null) {
                emit(composer, factory = factory)
            } else {
                elementMethod.invoke(scope, key, ComposeToolkit.emptyModifier, content, composer, 0)
            }
        }
    }

    fun elementKey(name: String, template: Any? = null): Any? = elementKeys.getOrPut(name) {
        try {
            val constructors = ComposeToolkit.loadClass(ELEMENT_KEY_CLASS)
                ?.declaredConstructors
                ?: return null
            val picker = template?.callMethodSilently("getContentPicker")
            if (picker != null) {
                constructors.firstOrNull { it.parameterTypes.size == 4 }
                    ?.newInstance(name, Any(), picker, false)
            } else {
                constructors.firstOrNull { it.parameterTypes.size == 6 }
                    ?.newInstance(name, null, null, false, ELEMENT_KEY_DEFAULTS, null)
            }
        } catch (_: Throwable) {
            null
        } ?: return null
    }

    fun staticField(className: String, fieldName: String): Any? = try {
        ComposeToolkit.loadClass(className)
            ?.getDeclaredField(fieldName)
            ?.apply { isAccessible = true }
            ?.get(null)
    } catch (_: Throwable) {
        null
    }

    fun hiddenModifier(original: Any): Any? {
        if (original in hiddenModifiers) return original

        val hidden = try {
            val companion = ComposeToolkit.emptyModifier ?: return null
            val alpha = alphaModifier?.invoke(null, companion, 0f) ?: return null
            ComposeToolkit.then(alpha, original)
        } catch (_: Throwable) {
            null
        } ?: return null

        hiddenModifiers.add(hidden)
        return hidden
    }

    fun isHiddenModifier(modifier: Any): Boolean = modifier in hiddenModifiers

    fun hideModifierArgument(param: MethodHookParam) {
        val method = param.method as Method
        val index = ComposeToolkit.parameterIndex(method, ComposeToolkit.MODIFIER_CLASS)
        if (index == -1) return

        val original = param.args[index]
        if (original != null && isHiddenModifier(original)) return

        val hidden = hiddenModifier(original ?: ComposeToolkit.emptyModifier ?: return)
            ?: return
        param.args[index] = hidden
        ComposeToolkit.clearDefaultBit(method, param.args, index)
    }

    fun runBeforeEachDraw(view: View, onDraw: () -> Unit) {
        val listener = ViewTreeObserver.OnPreDrawListener {
            if (view.isAttachedToWindow) onDraw()
            true
        }

        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.viewTreeObserver.removeOnPreDrawListener(listener)
                v.viewTreeObserver.addOnPreDrawListener(listener)
            }

            override fun onViewDetachedFromWindow(v: View) {
                v.viewTreeObserver.removeOnPreDrawListener(listener)
            }
        })

        if (view.isAttachedToWindow) view.viewTreeObserver.addOnPreDrawListener(listener)
    }
}
