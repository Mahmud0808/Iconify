package com.drdisagree.iconify.xposed.utils

import android.os.Build
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.core.hook.hookConstructor
import com.drdisagree.iconify.xposed.core.reflect.callMethodSilently
import com.drdisagree.iconify.xposed.core.reflect.findClass

object SceneContainer {

    val isEnabled: Boolean by lazy { readFlag() ?: isAndroid17Qpr1OrLater }

    private var shadeInteractor: Any? = null
    private var expansionHooked = false

    val panelExpansion: Float
        get() = (shadeExpansion + qsExpansion).coerceAtMost(1f)

    val shadeExpansion: Float
        get() = flowValue("getShadeExpansionFlow", "getShadeExpansion")

    val qsExpansion: Float
        get() = flowValue("getQsExpansionFlow", "getQsExpansion")

    fun trackShadeExpansion() {
        if (expansionHooked) return
        expansionHooked = true

        findClass(
            "$SYSTEMUI_PACKAGE.shade.domain.interactor.ShadeInteractorImpl",
            suppressError = true
        )
            .hookConstructor()
            .suppressError()
            .runAfter { chain, _ -> shadeInteractor = chain.thisObject }
    }

    private fun flowValue(vararg getters: String): Float {
        val interactor = shadeInteractor ?: return 0f
        for (getter in getters) {
            val flow = interactor.callMethodSilently(getter) ?: continue
            return flow.callMethodSilently("getValue") as? Float ?: 0f
        }
        return 0f
    }

    private fun readFlag(): Boolean? = try {
        val aconfigPackage = Class.forName("android.os.flagging.AconfigPackage")
        val systemUiFlags = aconfigPackage
            .getMethod("load", String::class.java)
            .invoke(null, SYSTEMUI_PACKAGE)
        aconfigPackage
            .getMethod(
                "getBooleanFlagValue",
                String::class.java,
                Boolean::class.javaPrimitiveType
            )
            .invoke(systemUiFlags, "scene_container", false) as Boolean
    } catch (_: Throwable) {
        null
    }

    private val isAndroid17Qpr1OrLater: Boolean
        get() = Build.VERSION.SDK_INT >= 37 &&
                (Build.VERSION.SDK_INT > 37 ||
                        Build.getMinorSdkVersion(Build.VERSION.SDK_INT_FULL) >= 1)
}
