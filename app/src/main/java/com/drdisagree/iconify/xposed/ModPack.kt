package com.drdisagree.iconify.xposed

import android.content.Context
import android.content.pm.PackageManager
import com.drdisagree.iconify.BuildConfig
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam

abstract class ModPack(private val context: Context) {

    protected val mContext: Context get() = context

    protected val appContext: Context
        get() = try {
            context.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (exception: PackageManager.NameNotFoundException) {
            throw RuntimeException(exception)
        }

    abstract fun onPreferenceUpdated(vararg key: String)

    @Throws(Throwable::class)
    abstract fun onPackageLoaded(packageReadyParam: PackageReadyParam)
}