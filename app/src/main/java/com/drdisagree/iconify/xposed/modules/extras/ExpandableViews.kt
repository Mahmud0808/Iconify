package com.drdisagree.iconify.xposed.modules.extras

import android.content.Context
import android.view.View
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.isMethodAvailable
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import org.objenesis.ObjenesisHelper

class ExpandableViews(context: Context) : ModPack(context) {

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        ExpandableClass = findClass("$SYSTEMUI_PACKAGE.animation.Expandable")
        ExpandableCompanionFromViewClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.animation.Expandable$Companion$fromView",
            $$"$$SYSTEMUI_PACKAGE.animation.Expandable$Companion$fromView$1",
            $$"$$SYSTEMUI_PACKAGE.animation.Expandable$Companion$fromView$2",
            $$"$$SYSTEMUI_PACKAGE.animation.Expandable$Companion$fromView$3",
        )
        RefObjectRefClass = findClass($$"kotlin.jvm.internal.Ref$ObjectRef")
    }

    companion object {
        private var ExpandableClass: Class<*>? = null
        private var ExpandableCompanionFromViewClass: Class<*>? = null
        private var RefObjectRefClass: Class<*>? = null

        fun View?.getExpandableView(): Any? {
            if (this == null) {
                log("getExpandableView", "View is null")
                return null
            }

            return if (ExpandableClass.isMethodAvailable("fromView", View::class.java)) {
                ExpandableClass!!.callStaticMethod("fromView", this)
            } else {
                runCatching {
                    ExpandableCompanionFromViewClass!!
                        .getConstructor(View::class.java)
                        .newInstance(this)
                }.getOrElse {
                    runCatching {
                        val refObject = ObjenesisHelper.newInstance(RefObjectRefClass!!)

                        try {
                            refObject.setField("element", callMethod("getAnimatedView"))
                        } catch (_: Throwable) {
                            refObject.setField("element", this)
                        }

                        ExpandableCompanionFromViewClass!!
                            .getConstructor(RefObjectRefClass)
                            .newInstance(refObject)
                    }.getOrElse {
                        val expandableObject =
                            ObjenesisHelper.newInstance(ExpandableCompanionFromViewClass!!)

                        try {
                            expandableObject.setField($$"$view", callMethod("getAnimatedView"))
                        } catch (_: Throwable) {
                            expandableObject.setField($$"$view", this)
                        }

                        expandableObject
                    }
                }
            }
        }
    }
}