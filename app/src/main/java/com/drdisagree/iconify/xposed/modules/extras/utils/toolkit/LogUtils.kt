package com.drdisagree.iconify.xposed.modules.extras.utils.toolkit


import android.view.View
import android.view.ViewGroup
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists

fun log(message: String?) {
    XposedBridge.log(message)
}

fun log(message: Any?) {
    XposedBridge.log(message.toString())
}

fun log(tag: String, message: Any?) {
    XposedBridge.log(
        "Iconify - $tag: $message"
    )
}

fun <T : Any> log(clazz: T, message: Any?) {
    XposedBridge.log(
        "Iconify - ${
            clazz.javaClass.simpleName.replace(
                "\$Companion",
                ""
            )
        }: $message"
    )
}

fun <T : Any> log(clazz: T, throwable: Throwable?) {
    XposedBridge.log(
        "Iconify - ${
            clazz.javaClass.simpleName.replace(
                "\$Companion",
                ""
            )
        }: $throwable"
    )
}

fun <T : Any> log(clazz: T, exception: Exception?) {
    XposedBridge.log(
        "Iconify - ${
            clazz.javaClass.simpleName.replace(
                "\$Companion",
                ""
            )
        }: $exception"
    )
}

fun findAndDumpClass(className: String, classLoader: ClassLoader?): Class<*> {
    dumpClass(className, classLoader)
    return findClass(className, classLoader)
}

fun findAndDumpClassIfExists(className: String, classLoader: ClassLoader?): Class<*> {
    dumpClass(className, classLoader)
    return findClassIfExists(className, classLoader)
}

private fun dumpClass(className: String, classLoader: ClassLoader?) {
    val ourClass = findClassIfExists(className, classLoader)
    if (ourClass == null) {
        XposedBridge.log("DumpClass: Class is null")
        return
    }
    ourClass.dumpClass()
}

fun Class<*>?.dumpClass() {
    if (this == null) {
        XposedBridge.log("DumpClass: Class is null")
        return
    }

    XposedBridge.log("\n\nClass: $name")
    XposedBridge.log("extends: ${superclass.name}")

    XposedBridge.log("Subclasses:")
    val scs = classes.toList().union(declaredClasses.toList())
    for (c in scs) {
        XposedBridge.log("\t" + c.name)
    }
    if (scs.isEmpty()) {
        XposedBridge.log("\tNone")
    }

    XposedBridge.log("Constructors:")
    val cons = declaredConstructors
    for (m in cons) {
        XposedBridge.log("\t" + m.name + " - " + this::class.java.simpleName + " - " + m.parameterCount)
        val cs = m.parameterTypes
        for (c in cs) {
            XposedBridge.log("\t\t" + c.typeName)
        }
    }
    if (cons.isEmpty()) {
        XposedBridge.log("\tNone")
    }

    XposedBridge.log("Methods:")
    val ms = declaredMethods.toList().union(methods.toList())
    for (m in ms) {
        XposedBridge.log("\t" + m.name + " - " + m.returnType + " - " + m.parameterCount)
        val cs = m.parameterTypes
        for (c in cs) {
            XposedBridge.log("\t\t" + c.typeName)
        }
    }
    if (ms.isEmpty()) {
        XposedBridge.log("\tNone")
    }

    XposedBridge.log("Fields:")
    val fs = declaredFields
    for (f in fs) {
        XposedBridge.log("\t" + f.name + " - " + f.type.name)
    }
    if (fs.isEmpty()) {
        XposedBridge.log("\tNone")
    }
    XposedBridge.log("End dump\n\n")
}

fun View.dumpChildViews() {
    if (this is ViewGroup) {
        logViewInfo(this, 0)
        dumpChildViewsRecursive(this, 0)
    } else {
        logViewInfo(this, 0)
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

private fun logViewInfo(view: View, indentationLevel: Int) {
    val indentation = repeatString("\t", indentationLevel)
    val viewName = view.javaClass.simpleName
    val superclassName = view.javaClass.superclass?.simpleName ?: "None"
    val backgroundDrawable = view.background
    val childCount = if (view is ViewGroup) view.childCount else 0
    var resourceIdName = "none"
    try {
        val viewId = view.id
        resourceIdName = view.context.resources.getResourceName(viewId)
    } catch (ignored: Throwable) {
    }
    var logMessage = "$indentation$viewName (Extends: $superclassName) - ID: $resourceIdName"
    if (childCount > 0) {
        logMessage += " - ChildCount: $childCount"
    }
    if (backgroundDrawable != null) {
        logMessage += " - Background: ${backgroundDrawable.javaClass.simpleName}"
    }
    XposedBridge.log(logMessage)
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