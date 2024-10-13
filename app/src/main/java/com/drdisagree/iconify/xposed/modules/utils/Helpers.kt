package com.drdisagree.iconify.xposed.modules.utils

import android.content.Context
import android.util.ArraySet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.topjohnwu.superuser.Shell
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.hookMethod
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import java.lang.reflect.Method
import java.util.regex.Pattern

@Suppress("unused")
object Helpers {

    fun findAndDumpClass(className: String, classLoader: ClassLoader?): Class<*> {
        dumpClass(className, classLoader)
        return findClass(className, classLoader)
    }

    fun findAndDumpClassIfExists(className: String, classLoader: ClassLoader?): Class<*> {
        dumpClass(className, classLoader)
        return findClassIfExists(className, classLoader)
    }

    fun dumpClassObj(classObj: Class<*>?) {
        if (classObj == null) {
            log("Class: null not found")
            return
        }
        dumpClass(classObj)
    }

    private fun dumpClass(className: String, classLoader: ClassLoader?) {
        val ourClass = findClassIfExists(className, classLoader)
        if (ourClass == null) {
            log("Class: $className not found")
            return
        }
        dumpClass(ourClass)
    }

    private fun dumpClass(ourClass: Class<*>) {
        val ms = ourClass.declaredMethods
        log("\n\nClass: ${ourClass.name}")
        log("extends: ${ourClass.superclass.name}")

        log("Subclasses:")
        val scs = ourClass.classes
        for (c in scs) {
            log(c.name)
        }

        log("Methods:")
        val cons = ourClass.declaredConstructors
        for (m in cons) {
            log(m.name + " - " + " - " + m.parameterCount)
            val cs = m.parameterTypes
            for (c in cs) {
                log("\t\t" + c.typeName)
            }
        }
        for (m in ms) {
            log(m.name + " - " + m.returnType + " - " + m.parameterCount)
            val cs = m.parameterTypes
            for (c in cs) {
                log("\t\t" + c.typeName)
            }
        }

        log("Fields:")
        val fs = ourClass.declaredFields
        for (f in fs) {
            log("\t\t" + f.name + "-" + f.type.name)
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

    fun hookAllMethodsMatchPattern(
        clazz: Class<*>,
        namePattern: String,
        callback: XC_MethodHook
    ): Set<XC_MethodHook.Unhook> {
        val result: MutableSet<XC_MethodHook.Unhook> = ArraySet()

        for (method in findMethods(clazz, namePattern)) {
            try {
                result.add(hookMethod(method, callback))
            } catch (ignored: Throwable) {
            }
        }

        return result
    }

    private fun findMethods(clazz: Class<*>, namePattern: String): Set<Method> {
        val result: MutableSet<Method> = ArraySet()
        val methods: Array<Method> = clazz.methods

        for (method in methods) {
            if (Pattern.matches(namePattern, method.name)) {
                result.add(method)
            }
        }

        return result
    }

    fun findMethod(clazz: Class<*>, namePattern: String): Method? {
        val methods: Array<Method> = clazz.methods

        for (method in methods) {
            if (Pattern.matches(namePattern, method.name)) {
                return method
            }
        }

        return null
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

    fun findClassInArray(classLoader: ClassLoader, vararg classNames: String): Class<*>? {
        for (className in classNames) {
            val clazz = findClassIfExists(className, classLoader)
            if (clazz != null) return clazz
        }
        return null
    }

    @Suppress("SameParameterValue")
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

    fun isMethodAvailable(
        target: Any?,
        methodName: String,
        vararg parameterTypes: Class<*>
    ): Boolean {
        if (target == null) return false

        return try {
            target::class.java.getMethod(methodName, *parameterTypes)
            true
        } catch (ignored: NoSuchMethodException) {
            false
        }
    }

    val isQsTileOverlayEnabled: Boolean
        get() {
            val output = Shell.cmd(
                "[[ $(cmd overlay list | grep -oE '\\[x\\] IconifyComponentQSS[N|P][0-9]+.overlay') ]] && echo 1 || echo 0"
            ).exec().out
            return output.isNotEmpty() && output[0] == "1"
        }

    val isPixelVariant: Boolean
        get() {
            val output = Shell.cmd(
                "[[ $(cmd overlay list | grep -oE '\\[x\\] IconifyComponentQSSP[0-9]+.overlay') ]] && echo 1 || echo 0"
            ).exec().out
            return output.isNotEmpty() && output[0] == "1"
        }
}
