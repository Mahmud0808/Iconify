package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.view.Choreographer
import android.view.View
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
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

    val isAvailable: Boolean
        get() = androidViewMethod != null && ComposeToolkit.functionClass(1) != null

    fun emit(composer: Any, factory: (Context) -> View) {
        val method = androidViewMethod ?: return
        val factoryFunction = ComposeToolkit.function1("IconifyViewFactory") { context ->
            factory(context as Context)
        } ?: return

        method.invoke(
            null,
            factoryFunction,
            ComposeToolkit.emptyModifier,
            null,
            composer,
            0,
            UPDATE_DEFAULT_MASK
        )
    }

    fun emitInGroup(composer: Any, key: Int, factory: (Context) -> View) {
        ComposeToolkit.inGroup(composer, key) { emit(composer, factory) }
    }

    fun emitElement(scope: Any, key: Any, composer: Any, groupKey: Int, factory: (Context) -> View) {
        val elementMethod = scope.javaClass.methods.firstOrNull {
            it.name == "Element" && it.parameterTypes.size == 5
        }
        val content = ComposeToolkit.function3("IconifyElementContent") { _, contentComposer, _ ->
            emit(contentComposer!!, factory)
        }

        ComposeToolkit.inGroup(composer, groupKey) {
            if (elementMethod == null || content == null) {
                emit(composer, factory)
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
}
