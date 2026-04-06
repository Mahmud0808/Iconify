@file:Suppress("Unused")

package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit

import android.view.View
import android.view.ViewGroup
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log

fun findAndDumpClass(className: String, classLoader: ClassLoader?): Class<*>? {
    dumpClass(className, classLoader)
    return findClass(
        className,
        classLoader = classLoader,
        suppressError = true,
        throwException = false
    )
}

private fun dumpClass(className: String, classLoader: ClassLoader?) {
    findClass(className, classLoader = classLoader).dumpClass()
}

fun Class<*>?.dumpClass() {
    if (this == null) {
        log("DumpClass: Class is null")
        return
    }

    log("\n\nClass: $name")
    log("extends: ${superclass?.name ?: "None"}")

    log("Subclasses:")
    val scs = classes.toList().union(declaredClasses.toList())
    for (c in scs) {
        log("\t" + c.name)
    }
    if (scs.isEmpty()) {
        log("\tNone")
    }

    log("Constructors:")
    val cons = declaredConstructors
    for (m in cons) {
        log("\t" + m.name + " - " + this::class.java.simpleName + " - " + m.parameterCount)
        val cs = m.parameterTypes
        for (c in cs) {
            log("\t\t" + c.typeName)
        }
    }
    if (cons.isEmpty()) {
        log("\tNone")
    }

    log("Methods:")
    val ms = declaredMethods.toList().union(methods.toList())
    for (m in ms) {
        log("\t" + m.name + " - " + m.returnType + " - " + m.parameterCount)
        val cs = m.parameterTypes
        for (c in cs) {
            log("\t\t" + c.typeName)
        }
    }
    if (ms.isEmpty()) {
        log("\tNone")
    }

    log("Fields:")
    val fs = declaredFields
    for (f in fs) {
        log("\t" + f.name + " - " + f.type.name)
    }
    if (fs.isEmpty()) {
        log("\tNone")
    }
    log("End dump\n\n")
}

fun View.dumpChildViews() {
    if (this is ViewGroup) {
        logViewInfo(this, 0, true)
        dumpChildViewsRecursive(this, 0)
    } else {
        logViewInfo(this, 0, true)
    }
}

private fun dumpChildViewsRecursive(
    viewGroup: ViewGroup,
    indentationLevel: Int
) {
    for (i in 0 until viewGroup.childCount) {
        val childView = viewGroup.getChildAt(i)
        logViewInfo(childView, indentationLevel + 1)
        if (childView is ViewGroup) {
            dumpChildViewsRecursive(childView, indentationLevel + 1)
        }
    }
}

private fun logViewInfo(view: View, indentationLevel: Int, isSingle: Boolean = false) {
    val indentation = repeatString("\t", indentationLevel)
    val viewName = view.javaClass.simpleName
    val superclassName = view.javaClass.superclass?.simpleName ?: "None"
    val backgroundDrawable = view.background
    val childCount = if (view is ViewGroup) view.childCount else 0
    var resourceIdName = "none"
    try {
        val viewId = view.id
        resourceIdName = view.context.resources.getResourceName(viewId)
    } catch (_: Throwable) {
    }
    var logMessage = "$indentation${if (isSingle) "" else "↳ "}$viewName (Extends: $superclassName) - ID: $resourceIdName"
    if (childCount > 0) {
        logMessage += " - ChildCount: $childCount"
    }
    if (backgroundDrawable != null) {
        logMessage += " - Background: ${backgroundDrawable.javaClass.simpleName}"
    }
    log(logMessage)
}

@Suppress("SameParameterValue")
private fun repeatString(str: String, times: Int): String {
    val result = StringBuilder()
    for (i in 0 until times) {
        result.append(str)
    }
    return result.toString()
}

fun Any.dumpPreferenceKeys() {
    for (i in 0 until callMethod("getPreferenceCount") as Int) {
        val preference = callMethod("getPreference", i)!!

        log("${preference::class.java.simpleName} -> Key: ${preference.callMethod("getKey")}")

        if (preference::class.java.simpleName == "PreferenceCategory") {
            preference.dumpPreferenceKeys()
        }
    }
}