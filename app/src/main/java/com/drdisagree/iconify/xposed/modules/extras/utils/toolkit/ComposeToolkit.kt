package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit

import androidx.core.graphics.toColorInt
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import de.robv.android.xposed.XposedHelpers
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.concurrent.ConcurrentHashMap

object ComposeToolkit {

    const val FUNCTION0_CLASS = "kotlin.jvm.functions.Function0"
    const val FUNCTION1_CLASS = "kotlin.jvm.functions.Function1"
    const val FUNCTION2_CLASS = "kotlin.jvm.functions.Function2"
    const val FUNCTION3_CLASS = "kotlin.jvm.functions.Function3"
    const val COMPOSER_CLASS = "androidx.compose.runtime.Composer"
    const val MODIFIER_CLASS = "androidx.compose.ui.Modifier"
    const val CONTENT_SCOPE_CLASS = "com.android.compose.animation.scene.ContentScope"

    @Volatile
    private var classLoader: ClassLoader? = null

    private val functionClasses = ConcurrentHashMap<Int, Class<*>>()
    private val invokeMethods = ConcurrentHashMap<Int, Method>()
    private val staticMethods = ConcurrentHashMap<String, Method>()

    val kotlinUnit: Any? by lazy {
        try {
            loadClass("kotlin.Unit")?.getField("INSTANCE")?.get(null)
        } catch (_: Throwable) {
            null
        }
    }

    private val modifierClass: Class<*>? by lazy { loadClass(MODIFIER_CLASS) }

    val emptyModifier: Any? by lazy {
        try {
            modifierClass?.getField("Companion")?.get(null)
        } catch (_: Throwable) {
            null
        }
    }

    private val modifierThen: Method? by lazy {
        try {
            modifierClass?.getMethod("then", modifierClass)
        } catch (_: Throwable) {
            null
        }
    }

    fun functionClass(arity: Int): Class<*>? = functionClasses[arity]
        ?: loadClass("kotlin.jvm.functions.Function$arity")
            ?.also { functionClasses[arity] = it }

    fun function0(name: String, block: () -> Any?): Any? =
        function(0, name) { block() }

    fun function1(name: String, block: (Any?) -> Any?): Any? =
        function(1, name) { args -> block(args[0]) }

    fun function2(name: String, block: (Any?, Any?) -> Any?): Any? =
        function(2, name) { args -> block(args[0], args[1]) }

    fun function3(name: String, block: (Any?, Any?, Any?) -> Any?): Any? =
        function(3, name) { args -> block(args[0], args[1], args[2]) }

    fun invokeFunction(function: Any, vararg args: Any?): Any? {
        val method = invokeMethods[args.size] ?: functionClass(args.size)
            ?.methods
            ?.firstOrNull { it.name == "invoke" && it.parameterTypes.size == args.size }
            ?.also { invokeMethods[args.size] = it }
            ?: return null
        return method.invoke(function, *args)
    }

    fun colorOf(argb: Int): Long = argb.toLong() shl 32

    fun colorOf(hex: String): Long = colorOf(hex.toColorInt())

    fun then(first: Any, second: Any): Any? = try {
        modifierThen?.invoke(first, second)
    } catch (_: Throwable) {
        null
    }

    fun staticMethod(className: String, cacheKey: String, predicate: (Method) -> Boolean): Method? {
        val key = "$className#$cacheKey"
        staticMethods[key]?.let { return it }

        return loadClass(className)
            ?.declaredMethods
            ?.firstOrNull(predicate)
            ?.apply { isAccessible = true }
            ?.also { staticMethods[key] = it }
    }

    fun modifierFunction(className: String, name: String, vararg extraTypes: Class<*>?): Method? =
        staticMethod(className, "$name/${extraTypes.joinToString { it?.name.toString() }}") { method ->
            val types = method.parameterTypes
            (method.name == name || method.name.startsWith("$name-")) &&
                    types.size == extraTypes.size + 1 &&
                    types[0].name == MODIFIER_CLASS &&
                    extraTypes.withIndex().all { (index, type) -> type == null || types[index + 1] == type }
        }

    fun init(loader: ClassLoader) {
        if (classLoader == null) classLoader = loader
    }

    fun loadClass(className: String): Class<*>? =
        classLoader?.let { XposedHelpers.findClassIfExists(className, it) }
            ?: findClass(className, suppressError = true)

    fun startGroup(composer: Any, key: Int) {
        try {
            composer.callMethod("startReplaceGroup", key)
        } catch (_: Throwable) {
            composer.callMethod("startReplaceableGroup", key)
        }
    }

    fun endGroup(composer: Any) {
        try {
            composer.callMethod("endReplaceGroup")
        } catch (_: Throwable) {
            composer.callMethod("endReplaceableGroup")
        }
    }

    fun inGroup(composer: Any, key: Int, block: () -> Unit) {
        startGroup(composer, key)
        try {
            block()
        } finally {
            endGroup(composer)
        }
    }

    fun parameterIndex(method: Method, typeName: String): Int =
        method.parameterTypes.indexOfFirst { it.name == typeName }

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

    fun isCalledFrom(className: String, vararg methodPrefixes: String): Boolean =
        Thread.currentThread().stackTrace.any { frame ->
            frame.className == className &&
                    (methodPrefixes.isEmpty() || methodPrefixes.any { frame.methodName.startsWith(it) })
        }

    private fun function(arity: Int, name: String, block: (Array<Any?>) -> Any?): Any? {
        val functionClass = functionClass(arity) ?: return null
        return Proxy.newProxyInstance(functionClass.classLoader, arrayOf(functionClass)) { proxy, method, args ->
            when (method.name) {
                "invoke" -> {
                    val result = block(args ?: emptyArray())
                    if (result === Unit) kotlinUnit else result
                }

                "equals" -> proxy === args?.firstOrNull()
                "hashCode" -> System.identityHashCode(proxy)
                "toString" -> name
                else -> null
            }
        }
    }
}
