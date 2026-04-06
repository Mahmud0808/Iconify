package com.drdisagree.iconify.xposed.modules.extras.utils

import android.content.Context
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

@Suppress("unused")
class MyConstraintSet(context: Context) : ModPack(context) {

    override fun onPreferenceUpdated(vararg key: String) {}

    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        ConstraintSetClass = findClass("androidx.constraintlayout.widget.ConstraintSet")
            ?: ConstraintSet::class.java
    }

    companion object {
        private var ConstraintSetClass: Class<*>? = null

        // We use reflection otherwise we get ClassCastException
        val constraintSetInstance: Any?
            get() = ConstraintSetClass?.getDeclaredConstructor()?.newInstance()

        fun Any.clone(view: View) {
            this.callMethod("clone", view)
        }

        fun Any.applyTo(view: View) {
            this.callMethod("applyTo", view)
        }

        fun Any.connect(startID: Int, startSide: Int, endID: Int, endSide: Int) {
            this.callMethod("connect", startID, startSide, endID, endSide)
        }

        fun Any.connect(startID: Int, startSide: Int, endID: Int, endSide: Int, margin: Int) {
            this.callMethod("connect", startID, startSide, endID, endSide, margin)
        }

        fun Any.clear(viewId: Int, anchor: Int) {
            this.callMethod("clear", viewId, anchor)
        }

        fun Any.setMargin(viewId: Int, anchor: Int, value: Int) {
            this.callMethod("setMargin", viewId, anchor, value)
        }

        fun Any.setGoneMargin(viewId: Int, anchor: Int, value: Int) {
            this.callMethod("setGoneMargin", viewId, anchor, value)
        }

        fun Any.setVisibility(viewId: Int, visibility: Int) {
            this.callMethod("setVisibility", viewId, visibility)
        }

        fun Any.constrainHeight(viewId: Int, height: Int) {
            this.callMethod("constrainHeight", viewId, height)
        }

        fun Any.constrainWidth(viewId: Int, height: Int) {
            this.callMethod("constrainWidth", viewId, height)
        }

        fun Any.createBarrier(id: Int, direction: Int, margin: Int, vararg referenced: Int) {
            val method = this::class.java.getDeclaredMethod(
                "createBarrier",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                IntArray::class.java
            )
            method.isAccessible = true
            method.invoke(this, id, direction, margin, referenced)
        }
    }
}