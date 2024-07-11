package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.topjohnwu.superuser.Shell
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@Suppress("unused")
object Helpers {
    fun enableOverlay(pkgName: String) {
        Shell.cmd(
            "cmd overlay enable --user current $pkgName",
            "cmd overlay set-priority $pkgName highest"
        ).exec()
    }

    fun disableOverlay(pkgName: String) {
        Shell.cmd("cmd overlay disable --user current $pkgName").exec()
    }

    fun enableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()
        for (pkgName in pkgNames) {
            command.append("cmd overlay enable --user current $pkgName; cmd overlay set-priority $pkgName highest; ")
        }
        Shell.cmd(command.toString().trim()).submit()
    }

    fun disableOverlays(vararg pkgNames: String?) {
        val command = StringBuilder()
        for (pkgName in pkgNames) {
            command.append("cmd overlay disable --user current $pkgName; ")
        }
        Shell.cmd(command.toString().trim()).submit()
    }

    fun findAndDumpClass(className: String, classLoader: ClassLoader?): Class<*> {
        dumpClass(className, classLoader)
        return XposedHelpers.findClass(className, classLoader)
    }

    fun findAndDumpClassIfExists(className: String, classLoader: ClassLoader?): Class<*> {
        dumpClass(className, classLoader)
        return XposedHelpers.findClassIfExists(className, classLoader)
    }

    fun dumpClass(className: String, classLoader: ClassLoader?) {
        val ourClass = XposedHelpers.findClassIfExists(className, classLoader)
        if (ourClass == null) {
            log("Class: $className not found")
            return
        }
        dumpClass(ourClass)
    }

    fun dumpClass(ourClass: Class<*>) {
        val ms = ourClass.getDeclaredMethods()
        log("\n\nClass: ${ourClass.getName()}")
        log("extends: ${ourClass.superclass.getName()}")

        log("Subclasses:")
        val scs = ourClass.getClasses()
        for (c in scs) {
            log(c.getName())
        }

        log("Methods:")
        val cons = ourClass.declaredConstructors
        for (m in cons) {
            log(m.name + " - " + " - " + m.parameterCount)
            val cs = m.getParameterTypes()
            for (c in cs) {
                log("\t\t" + c.getTypeName())
            }
        }
        for (m in ms) {
            log(m.name + " - " + m.returnType + " - " + m.parameterCount)
            val cs = m.getParameterTypes()
            for (c in cs) {
                log("\t\t" + c.getTypeName())
            }
        }

        log("Fields:")
        val fs = ourClass.declaredFields
        for (f in fs) {
            log("\t\t" + f.getName() + "-" + f.type.getName())
        }
        log("End dump\n\n")
    }

    fun tryHookAllMethods(clazz: Class<*>?, method: String?, hook: XC_MethodHook?) {
        try {
            hookAllMethods(clazz, method, hook)
        } catch (ignored: Throwable) {
        }
    }

    fun tryHookAllConstructors(clazz: Class<*>?, hook: XC_MethodHook?) {
        try {
            hookAllConstructors(clazz, hook)
        } catch (ignored: Throwable) {
        }
    }

    fun dumpChildViews(context: Context, view: View) {
        if (view is ViewGroup) {
            logViewInfo(context, view, 0)
            dumpChildViewsRecursive(context, view, 0)
        } else {
            logViewInfo(context, view, 0)
        }
    }

    private fun logViewInfo(context: Context, view: View, indentationLevel: Int) {
        val indentation = repeatString("\t", indentationLevel)
        val viewName = view.javaClass.simpleName
        val backgroundDrawable = view.background
        val childCount = if (view is ViewGroup) view.childCount else 0
        var resourceIdName = "none"
        try {
            val viewId = view.id
            resourceIdName = context.resources.getResourceName(viewId)
        } catch (ignored: Throwable) {
        }
        var logMessage = "$indentation$viewName - ID: $resourceIdName"
        if (childCount > 0) {
            logMessage += " - ChildCount: $childCount"
        }
        if (backgroundDrawable != null) {
            logMessage += " - Background: " + backgroundDrawable.javaClass.simpleName + ""
        }
        log(logMessage)
    }

    private fun dumpChildViewsRecursive(
        context: Context,
        viewGroup: ViewGroup,
        indentationLevel: Int
    ) {
        for (i in 0 until viewGroup.childCount) {
            val childView = viewGroup.getChildAt(i)
            logViewInfo(context, childView, indentationLevel + 1)
            if (childView is ViewGroup) {
                dumpChildViewsRecursive(context, childView, indentationLevel + 1)
            }
        }
    }

    fun findClassInArray(lpparam: LoadPackageParam, vararg classNames: String): Class<*>? {
        for (className in classNames) {
            val clazz = findClassIfExists(className, lpparam.classLoader)
            if (clazz != null) return clazz
        }
        return null
    }

    private fun repeatString(str: String, times: Int): String {
        val result = StringBuilder()
        for (i in 0 until times) {
            result.append(str)
        }
        return result.toString()
    }

    fun getColorResCompat(context: Context, @AttrRes id: Int): Int {
        val typedValue = TypedValue()
        val theme = context.theme
        theme.resolveAttribute(id, typedValue, false)
        val arr = context.obtainStyledAttributes(typedValue.data, intArrayOf(id))
        @ColorInt val color = arr.getColor(0, -1)
        arr.recycle()
        return color
    }

    val isPixelVariant: Boolean
        get() = Shell.cmd("[[ $(cmd overlay list | grep -oE '\\[x\\] IconifyComponentQSSP[0-9]+.overlay') ]] && echo 1 || echo 0")
            .exec().out[0] == "1"
}
