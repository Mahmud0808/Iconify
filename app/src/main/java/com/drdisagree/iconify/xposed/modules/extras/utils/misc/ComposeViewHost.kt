package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.view.Choreographer
import android.view.View
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.Collections
import java.util.WeakHashMap

object ComposeViewHost {

    private const val FUNCTION1_CLASS = "kotlin.jvm.functions.Function1"
    private const val FUNCTION2_CLASS = "kotlin.jvm.functions.Function2"
    const val FUNCTION3_CLASS = "kotlin.jvm.functions.Function3"
    const val COMPOSER_CLASS = "androidx.compose.runtime.Composer"
    const val MODIFIER_CLASS = "androidx.compose.ui.Modifier"
    const val CONTENT_SCOPE_CLASS = "com.android.compose.animation.scene.ContentScope"
    private const val ELEMENT_KEY_CLASS = "com.android.compose.animation.scene.ElementKey"
    private const val ELEMENT_KEY_DEFAULTS = 14

    private var androidViewMethod: Method? = null
    private var function1Class: Class<*>? = null
    private var function2Class: Class<*>? = null
    private var function2Invoke: Method? = null
    private var function3Class: Class<*>? = null
    private var function3Invoke: Method? = null
    private var modifierCompanion: Any? = null
    private var modifierThen: Method? = null
    private var alphaModifier: Method? = null
    private var kotlinUnit: Any? = null
    private val hiddenModifiers: MutableSet<Any> = Collections.newSetFromMap(WeakHashMap())
    private val elementKeys = HashMap<String, Any>()

    val isAvailable: Boolean
        get() = androidViewMethod != null && function1Class != null && function2Class != null

    fun init() {
        if (isAvailable) return

        function1Class = findClass(FUNCTION1_CLASS, suppressError = true)
        function2Class = findClass(FUNCTION2_CLASS, suppressError = true)
        function2Invoke = function2Class?.methods?.firstOrNull {
            it.name == "invoke" && it.parameterTypes.size == 2
        }
        function3Class = findClass(FUNCTION3_CLASS, suppressError = true)
        function3Invoke = function3Class?.methods?.firstOrNull {
            it.name == "invoke" && it.parameterTypes.size == 3
        }
        kotlinUnit = try {
            findClass("kotlin.Unit", suppressError = true)?.getField("INSTANCE")?.get(null)
        } catch (_: Throwable) {
            null
        }
        val modifierClass = findClass(MODIFIER_CLASS, suppressError = true)
        modifierCompanion = try {
            modifierClass?.getField("Companion")?.get(null)
        } catch (_: Throwable) {
            null
        }
        modifierThen = try {
            modifierClass?.getMethod("then", modifierClass)
        } catch (_: Throwable) {
            null
        }
        alphaModifier = findClass(
            "androidx.compose.ui.draw.AlphaKt",
            suppressError = true
        )?.declaredMethods?.firstOrNull { method ->
            method.name == "alpha" &&
                    method.parameterTypes.size == 2 &&
                    method.parameterTypes[0].name == MODIFIER_CLASS &&
                    method.parameterTypes[1] == Float::class.javaPrimitiveType
        }
        androidViewMethod = findClass(
            "androidx.compose.ui.viewinterop.AndroidView_androidKt",
            suppressError = true
        )?.declaredMethods?.firstOrNull { method ->
            val types = method.parameterTypes
            method.name == "AndroidView" &&
                    types.size == 6 &&
                    types[0].name == FUNCTION1_CLASS &&
                    types[1].name == MODIFIER_CLASS &&
                    types[2].name == FUNCTION1_CLASS &&
                    types[3].name == COMPOSER_CLASS
        }
    }

    fun emit(composer: Any, factory: (Context) -> View) {
        val method = androidViewMethod ?: return
        val factoryFunction = function1(factory) ?: return

        method.invoke(null, factoryFunction, modifierCompanion, null, composer, 0, UPDATE_DEFAULT_MASK)
    }

    fun elementKey(name: String, template: Any? = null): Any? = elementKeys.getOrPut(name) {
        try {
            val constructors = findClass(ELEMENT_KEY_CLASS, suppressError = true)
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
        findClass(className, suppressError = true)
            ?.getDeclaredField(fieldName)
            ?.apply { isAccessible = true }
            ?.get(null)
    } catch (_: Throwable) {
        null
    }

    fun emitElement(scope: Any, key: Any, composer: Any, groupKey: Int, factory: (Context) -> View) {
        val elementMethod = scope.javaClass.methods.firstOrNull {
            it.name == "Element" && it.parameterTypes.size == 5
        }
        val content = elementContent { contentComposer -> emit(contentComposer, factory) }

        composer.startGroup(groupKey)
        try {
            if (elementMethod == null || content == null) {
                emit(composer, factory)
            } else {
                elementMethod.invoke(scope, key, modifierCompanion, content, composer, 0)
            }
        } finally {
            composer.endGroup()
        }
    }

    fun emitInGroup(composer: Any, key: Int, factory: (Context) -> View) {
        composer.startGroup(key)
        try {
            emit(composer, factory)
        } finally {
            composer.endGroup()
        }
    }

    fun runEveryFrameWhileAttached(view: View, onFrame: () -> Unit) {
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (!view.isAttachedToWindow) return
                onFrame()
                Choreographer.getInstance().postFrameCallback(this)
            }
        }

        view.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                Choreographer.getInstance().removeFrameCallback(callback)
                Choreographer.getInstance().postFrameCallback(callback)
            }

            override fun onViewDetachedFromWindow(v: View) {
                Choreographer.getInstance().removeFrameCallback(callback)
            }
        })
    }

    fun hiddenModifier(original: Any): Any? {
        if (original in hiddenModifiers) return original

        val hidden = try {
            val companion = modifierCompanion ?: return null
            val alpha = alphaModifier?.invoke(null, companion, 0f) ?: return null
            modifierThen?.invoke(alpha, original)
        } catch (_: Throwable) {
            null
        } ?: return null

        hiddenModifiers.add(hidden)
        return hidden
    }

    fun isHiddenModifier(modifier: Any): Boolean = modifier in hiddenModifiers

    val emptyModifier: Any?
        get() = modifierCompanion

    fun clearDefaultBit(method: Method, args: Array<Any?>, index: Int) {
        val types = method.parameterTypes
        val composerIndex = types.indexOfFirst { it.name == COMPOSER_CLASS }
        if (composerIndex == -1) return

        val changedCount = maxOf(1, (composerIndex + 9) / 10)
        val trailing = types.size - composerIndex - 1
        if (trailing <= changedCount) return

        val maskIndex = composerIndex + 1 + changedCount + index / 31
        val mask = args.getOrNull(maskIndex) as? Int ?: return
        args[maskIndex] = mask and (1 shl (index % 31)).inv()
    }

    fun isCalledFrom(className: String, vararg methodPrefixes: String): Boolean {
        return Thread.currentThread().stackTrace.any { frame ->
            frame.className == className &&
                    (methodPrefixes.isEmpty() || methodPrefixes.any { frame.methodName.startsWith(it) })
        }
    }

    fun parameterIndex(method: Method, typeName: String): Int =
        method.parameterTypes.indexOfFirst { it.name == typeName }

    fun invokeSlot(slot: Any, composer: Any, changed: Any?) {
        function2Invoke?.invoke(slot, composer, changed)
    }

    fun composableSlot(block: (composer: Any, changed: Any?) -> Unit): Any? {
        val functionClass = function2Class ?: return null
        return Proxy.newProxyInstance(functionClass.classLoader, arrayOf(functionClass)) { proxy, method, args ->
            when (method.name) {
                "invoke" -> {
                    block(args[0], args[1])
                    kotlinUnit
                }

                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "IconifyComposableSlot"
                else -> null
            }
        }
    }

    fun scopedComposableSlot(block: (scope: Any?, composer: Any, changed: Any?) -> Unit): Any? {
        val functionClass = function3Class ?: return null
        return Proxy.newProxyInstance(functionClass.classLoader, arrayOf(functionClass)) { proxy, method, args ->
            when (method.name) {
                "invoke" -> {
                    block(args[0], args[1], args[2])
                    kotlinUnit
                }

                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "IconifyScopedComposableSlot"
                else -> null
            }
        }
    }

    fun invokeScopedSlot(slot: Any, scope: Any?, composer: Any, changed: Any?) {
        function3Invoke?.invoke(slot, scope, composer, changed)
    }

    private fun Any.startGroup(key: Int) {
        try {
            callMethod("startReplaceGroup", key)
        } catch (_: Throwable) {
            callMethod("startReplaceableGroup", key)
        }
    }

    private fun Any.endGroup() {
        try {
            callMethod("endReplaceGroup")
        } catch (_: Throwable) {
            callMethod("endReplaceableGroup")
        }
    }

    private fun elementContent(block: (composer: Any) -> Unit): Any? {
        val functionClass = function3Class ?: return null
        return Proxy.newProxyInstance(functionClass.classLoader, arrayOf(functionClass)) { proxy, method, args ->
            when (method.name) {
                "invoke" -> {
                    block(args[1])
                    kotlinUnit
                }

                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "IconifyElementContent"
                else -> null
            }
        }
    }

    private fun function1(block: (Context) -> View): Any? {
        val functionClass = function1Class ?: return null
        return Proxy.newProxyInstance(functionClass.classLoader, arrayOf(functionClass)) { proxy, method, args ->
            when (method.name) {
                "invoke" -> block(args[0] as Context)
                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> "IconifyViewFactory"
                else -> null
            }
        }
    }

    private const val UPDATE_DEFAULT_MASK = 1 shl 2
}
