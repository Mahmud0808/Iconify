package com.drdisagree.iconify.xposed.core

import android.content.Context
import io.github.libxposed.api.XposedInterface
import java.lang.ref.WeakReference

object XposedRuntime {

    @Volatile
    private var framework: XposedInterface? = null

    @Volatile
    var processName: String = ""
        private set

    @Volatile
    lateinit var packageParam: PackageParam
        private set

    @Volatile
    private var hostContextRef: WeakReference<Context>? = null

    val hostContext: Context?
        get() = hostContextRef?.get()

    val api: XposedInterface
        get() = framework ?: throw IllegalStateException("Xposed framework is not attached")

    val isAttached: Boolean
        get() = framework != null

    val isPackageReady: Boolean
        get() = ::packageParam.isInitialized

    val classLoader: ClassLoader
        get() = packageParam.classLoader

    internal fun attach(framework: XposedInterface, processName: String) {
        this.framework = framework
        this.processName = processName
    }

    internal fun onPackageReady(packageParam: PackageParam) {
        this.packageParam = packageParam
    }

    fun attachContext(context: Context) {
        if (hostContext == null) hostContextRef = WeakReference(context)
    }
}
