package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ComposeToolkit
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import java.lang.reflect.Method
import java.util.concurrent.CopyOnWriteArrayList

object ShadeSceneInjector {

    private class Layer(
        val order: Int,
        val key: Int,
        val elementName: String,
        val factory: (Context) -> View
    )

    private val layers = CopyOnWriteArrayList<Layer>()
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
        if (!ComposeViewHost.isAvailable) return

        findClass("$SYSTEMUI_PACKAGE.shade.ui.composable.ShadePanelScrimKt", suppressError = true)
            .hookMethod("ShadePanelScrim")
            .suppressError()
            .runAfter { param ->
                if (layers.isEmpty()) return@runAfter

                val isShadeScene =
                    ComposeToolkit.isCalledFrom(QS_SCENE_CLASS, "QuickSettingsScene") ||
                            ComposeToolkit.isCalledFrom(SHADE_SCENE_CLASS, "SingleShade", "SplitShade")
                if (!isShadeScene) return@runAfter

                val method = param.method as Method
                val composer = param.args.getOrNull(
                    ComposeToolkit.parameterIndex(method, ComposeToolkit.COMPOSER_CLASS)
                ) ?: return@runAfter
                val scope = param.args.getOrNull(
                    ComposeToolkit.parameterIndex(method, ComposeToolkit.CONTENT_SCOPE_CLASS)
                )

                layers.forEach { layer ->
                    val elementKey = ComposeViewHost.elementKey(layer.elementName, backgroundScrimKey)

                    if (scope != null && elementKey != null) {
                        ComposeViewHost.emitElement(scope, elementKey, composer, layer.key, layer.factory)
                    } else {
                        ComposeViewHost.emitInGroup(composer, layer.key, factory = layer.factory)
                    }
                }
            }
    }

    private const val SHADE_SCENE_CLASS = "$SYSTEMUI_PACKAGE.shade.ui.composable.ShadeSceneKt"
    private const val SHADE_ELEMENTS_CLASS = "$SYSTEMUI_PACKAGE.shade.ui.composable.Shade\$Elements"
    private const val QS_SCENE_CLASS = "$SYSTEMUI_PACKAGE.qs.ui.composable.QuickSettingsSceneKt"
}
