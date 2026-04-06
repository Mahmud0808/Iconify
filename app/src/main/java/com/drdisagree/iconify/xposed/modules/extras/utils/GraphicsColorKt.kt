package com.drdisagree.iconify.xposed.modules.extras.utils

import android.content.Context
import androidx.core.graphics.toColorInt
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

class GraphicsColorKt(context: Context) : ModPack(context) {

    override fun onPreferenceUpdated(vararg key: String) {}

    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        ColorKtClass = findClass("androidx.compose.ui.graphics.ColorKt")
    }

    companion object {
        private var ColorKtClass: Class<*>? = null

        fun colorOf(color: Int): Any? {
            return ColorKtClass.callStaticMethod("Color", color)
        }

        fun colorOf(color: String): Any? {
            return colorOf(color.toColorInt())
        }
    }
}