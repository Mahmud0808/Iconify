package com.drdisagree.iconify.xposed.modules.launcher

import android.app.Activity
import android.content.Context
import androidx.core.view.WindowInsetsCompat
import com.drdisagree.iconify.data.common.Preferences.LAUNCHER_HIDE_STATUSBAR
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getExtraField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getStaticField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setExtraField
import com.drdisagree.iconify.xposed.modules.launcher.LauncherUtils.Companion.restartLauncher
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy

class HideStatusbar(context: Context) : ModPack(context) {

    private var hideStatusbarEnabled = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            hideStatusbarEnabled = getBoolean(LAUNCHER_HIDE_STATUSBAR, false)
        }

        when (key.firstOrNull()) {
            LAUNCHER_HIDE_STATUSBAR -> restartLauncher(mContext)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val launcherStateClass = findClass("com.android.launcher3.LauncherState")!!
        OVERVIEW = launcherStateClass.getStaticField("OVERVIEW")

        val quickstepLauncherClass =
            findClass("com.android.launcher3.uioverrides.QuickstepLauncher")

        quickstepLauncherClass
            .hookConstructor()
            .runAfter { param ->
                if (!hideStatusbarEnabled) return@runAfter

                val launcherActivity = param.thisObject as Activity

                val noStatusBarStateListener = object : CustomStateListener() {
                    override fun onStateTransitionStart() {
                        launcherActivity.window.decorView.windowInsetsController?.show(
                            WindowInsetsCompat.Type.statusBars()
                        )
                    }

                    override fun onStateTransitionComplete() {
                        launcherActivity.window.decorView.windowInsetsController?.hide(
                            WindowInsetsCompat.Type.statusBars()
                        )
                    }
                }

                launcherActivity.setExtraField(
                    "noStatusBarStateListener",
                    getListener(noStatusBarStateListener)
                )
            }

        quickstepLauncherClass
            .hookMethod("onCreate")
            .runAfter { param ->
                if (!hideStatusbarEnabled) return@runAfter

                param.thisObject
                    .callMethod("getStateManager")
                    .callMethod(
                        "addStateListener",
                        param.thisObject.getExtraField("noStatusBarStateListener")
                    )
            }

        quickstepLauncherClass
            .hookMethod("onDestroy")
            .runAfter { param ->
                if (!hideStatusbarEnabled) return@runAfter

                param.thisObject
                    .callMethod("getStateManager")
                    .callMethod(
                        "removeStateListener",
                        param.thisObject.getExtraField("noStatusBarStateListener")
                    )
            }
    }

    private fun getListener(listener: CustomStateListener): Any {
        val listenerClass =
            findClass("com.android.launcher3.statemanager.StateManager\$StateListener")!!

        return Proxy.newProxyInstance(
            listenerClass.classLoader,
            arrayOf(listenerClass),
            listener
        )
    }

    private abstract class CustomStateListener : InvocationHandler {

        override fun invoke(proxy: Any?, method: Method?, args: Array<out Any?>?): Any? {
            when (method?.name) {
                "onStateTransitionStart" -> {
                    val toState = args!![0]

                    if (toState == OVERVIEW) {
                        onStateTransitionStart()
                    }
                }

                "onStateTransitionComplete" -> {
                    val finalState = args!![0]

                    if (finalState != OVERVIEW) {
                        onStateTransitionComplete()
                    }
                }
            }

            return null
        }

        abstract fun onStateTransitionStart()

        abstract fun onStateTransitionComplete()
    }

    companion object {
        private lateinit var OVERVIEW: Any
    }
}