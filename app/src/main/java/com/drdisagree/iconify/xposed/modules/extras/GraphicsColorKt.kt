package com.drdisagree.iconify.xposed.modules.extras

import android.content.Context
import androidx.core.graphics.toColorInt
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class GraphicsColorKt(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
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