package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHelpers.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.log
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import java.util.concurrent.CopyOnWriteArrayList

class HeadsUpCallback(context: Context) : ModPack(context) {

    private val mHeadsUpListeners = CopyOnWriteArrayList<HeadsUpListener>()

    override fun onPreferenceUpdated(vararg key: String) {}

    override fun onPackageLoaded(packageReadyParam: PackageReadyParam) {
        instance = this

        val headsUpAppearanceControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.HeadsUpAppearanceController")

        headsUpAppearanceControllerClass
            .hookMethod("updateTopEntry", "updatePinnedStatus")
            .runBefore { param ->
                var newEntry: Any? = null
                val shouldBeVisible = (param.thisObject.callMethodSilently("shouldBeVisible")
                    ?: param.thisObject.callMethodSilently("shouldBeVisible$1")) as? Boolean
                    ?: param.thisObject.callMethod("shouldHeadsUpStatusBarBeVisible") as Boolean

                if (shouldBeVisible) {
                    val mHeadsUpManager = param.thisObject.getField("mHeadsUpManager")

                    newEntry = try {
                        mHeadsUpManager.callMethod("getTopEntry")
                    } catch (_: Throwable) {
                        mHeadsUpManager.callMethod("getTopHeadsUpEntry")?.getFieldSilently("mEntry")
                    }
                }

                val headsUpStatusBarView = param.thisObject.getField("mView")
                val previousEntry = try {
                    headsUpStatusBarView.callMethod("getShowingEntry")
                } catch (_: Throwable) {
                    headsUpStatusBarView.getFieldSilently("mShowingEntry")
                }

                if (previousEntry != newEntry) {
                    if (newEntry == null) {
                        notifyHeadsUpGone()
                    } else if (previousEntry == null) {
                        notifyHeadsUpShown()
                    }
                }
            }
    }

    interface HeadsUpListener {
        fun onHeadsUpShown()
        fun onHeadsUpGone()
    }

    private fun notifyHeadsUpShown() {
        mHeadsUpListeners.forEach {
            try {
                it.onHeadsUpShown()
            } catch (throwable: Throwable) {
                log(this@HeadsUpCallback, "notifyHeadsUpShown: $throwable")
            }
        }
    }

    private fun notifyHeadsUpGone() {
        mHeadsUpListeners.forEach {
            try {
                it.onHeadsUpGone()
            } catch (throwable: Throwable) {
                log(this@HeadsUpCallback, "notifyHeadsUpGone: $throwable")
            }
        }
    }

    fun registerHeadsUpListener(callback: HeadsUpListener) {
        if (!mHeadsUpListeners.contains(callback)) {
            mHeadsUpListeners.add(callback)
        }
    }

    fun unregisterHeadsUpListener(callback: HeadsUpListener) {
        mHeadsUpListeners.remove(callback)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: HeadsUpCallback? = null

        fun getInstance(): HeadsUpCallback {
            return checkNotNull(instance) { "HeadsUpCallback is not initialized yet!" }
        }
    }
}