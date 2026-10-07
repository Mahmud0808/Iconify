package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.utils.SceneContainer
import java.lang.reflect.Method
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.CopyOnWriteArrayList

object ShadeSceneInjector {

    private enum class Scene { SHADE, QUICK_SETTINGS }

    private class Layer(
        val order: Int,
        val key: Int,
        val elementName: String,
        val factory: (Context) -> View
    )

    private val layers = CopyOnWriteArrayList<Layer>()
    private val attachedHosts: MutableMap<View, Scene> = Collections.synchronizedMap(WeakHashMap())
    private val backgroundScrimKey: Any? by lazy {
        ComposeViewHost.staticField(SHADE_ELEMENTS_CLASS, "BackgroundScrim")
    }
    private var hooked = false

    fun addBehindContent(order: Int, key: Int, elementName: String, factory: (Context) -> View) {
        layers.removeAll { it.key == key }
        layers += Layer(order, key, elementName, factory)
        val sorted = layers.sortedBy { it.order }
        layers.clear()
        layers.addAll(sorted)
        hookShadePanelScrim()
    }

    private fun hookShadePanelScrim() {
        if (hooked) return
        hooked = true

        ComposeViewHost.init()
        if (!ComposeViewHost.isAvailable) return

        findClass("$SYSTEMUI_PACKAGE.shade.ui.composable.ShadePanelScrimKt", suppressError = true)
            .hookMethod("ShadePanelScrim")
            .suppressError()
            .runAfter { param ->
                if (layers.isEmpty()) return@runAfter

                val scene = when {
                    ComposeViewHost.isCalledFrom(QS_SCENE_CLASS, "QuickSettingsScene") ->
                        Scene.QUICK_SETTINGS

                    ComposeViewHost.isCalledFrom(SHADE_SCENE_CLASS, "SingleShade", "SplitShade") ->
                        Scene.SHADE

                    else -> return@runAfter
                }

                val method = param.method as Method
                val composer = param.args.getOrNull(
                    ComposeViewHost.parameterIndex(method, ComposeViewHost.COMPOSER_CLASS)
                ) ?: return@runAfter
                val scope = param.args.getOrNull(
                    ComposeViewHost.parameterIndex(method, ComposeViewHost.CONTENT_SCOPE_CLASS)
                )

                layers.forEach { layer ->
                    val factory: (Context) -> View = { context ->
                        layer.factory(context).also { host -> trackHost(host, scene) }
                    }
                    val elementKey = ComposeViewHost.elementKey(layer.elementName, backgroundScrimKey)

                    if (scope != null && elementKey != null) {
                        ComposeViewHost.emitElement(scope, elementKey, composer, layer.key, factory)
                    } else {
                        ComposeViewHost.emitInGroup(composer, layer.key, factory)
                    }
                }
            }
    }

    private fun trackHost(host: View, scene: Scene) {
        host.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                attachedHosts[v] = scene
            }

            override fun onViewDetachedFromWindow(v: View) {
                attachedHosts.remove(v)
            }
        })

        ComposeViewHost.runEveryFrameWhileAttached(host) {
            val visibility = if (isShownScene(scene)) View.VISIBLE else View.INVISIBLE
            if (host.visibility != visibility) host.visibility = visibility
        }
    }

    private fun isShownScene(scene: Scene): Boolean {
        val scenes = synchronized(attachedHosts) { attachedHosts.values.toSet() }
        if (scenes.size < 2) return true

        val quickSettingsShown = SceneContainer.qsExpansion >= 0.5f
        return (scene == Scene.QUICK_SETTINGS) == quickSettingsShown
    }

    private const val SHADE_SCENE_CLASS = "$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeSceneKt"
    private const val SHADE_ELEMENTS_CLASS = "$SYSTEMUI_PACKAGE.shade.ui.composable.Shade\$Elements"
    private const val QS_SCENE_CLASS = "$SYSTEMUI_PACKAGE.qs.ui.composable.QuickSettingsSceneKt"
}
