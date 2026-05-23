package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.CopyOnWriteArrayList

class QsShowingCallback(context: Context) : ModPack(context) {

    private val mQuickSettingsListeners = CopyOnWriteArrayList<QsShowingListener>()

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        instance = this

        val visualStabilityCoordinatorClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.collection.coordinator.VisualStabilityCoordinator")

        visualStabilityCoordinatorClass
            .hookConstructor()
            .runAfter { param ->
                val mStatusBarStateControllerListener =
                    param.thisObject.getField("mStatusBarStateControllerListener")

                mStatusBarStateControllerListener::class.java
                    .hookMethod("onExpandedChanged")
                    .runAfter runAfter2@{ param2 ->
                        val isQsExpanded = param2.args[0] as Boolean

                        notifyQuickSettingsExpandedChanged(isQsExpanded)
                    }
            }
    }

    interface QsShowingListener {
        fun onQuickSettingsExpanded()
        fun onQuickSettingsCollapsed()
    }

    private fun notifyQuickSettingsExpandedChanged(isQsExpanded: Boolean) {
        mQuickSettingsListeners.forEach {
            try {
                if (isQsExpanded) it.onQuickSettingsExpanded()
                else it.onQuickSettingsCollapsed()
            } catch (throwable: Throwable) {
                log(this@QsShowingCallback, "notifyQuickSettingsExpandedChanged: $throwable")
            }
        }
    }

    fun registerQsShowingListener(callback: QsShowingListener) {
        if (!mQuickSettingsListeners.contains(callback)) {
            mQuickSettingsListeners.add(callback)
        }
    }

    fun unregisterQsShowingListener(callback: QsShowingListener) {
        mQuickSettingsListeners.remove(callback)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: QsShowingCallback? = null

        fun getInstance(): QsShowingCallback {
            return checkNotNull(instance) { "QsShowingCallback is not initialized yet!" }
        }
    }
}