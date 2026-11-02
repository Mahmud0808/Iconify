package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookConstructor
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getField
import java.util.concurrent.CopyOnWriteArrayList

class QsShowingCallback(context: Context) : ModPack(context) {

    private val mQuickSettingsListeners = CopyOnWriteArrayList<QsShowingListener>()

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(packageParam: PackageParam) {
        instance = this

        val visualStabilityCoordinatorClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.collection.coordinator.VisualStabilityCoordinator")

        visualStabilityCoordinatorClass
            .hookConstructor()
            .runAfter { chain, _ ->
                val mStatusBarStateControllerListener =
                    chain.thisObject.getField("mStatusBarStateControllerListener")

                mStatusBarStateControllerListener::class.java
                    .hookMethod("onExpandedChanged")
                    .runAfter runAfter2@{ chain2, _ ->
                        val isQsExpanded = chain2.args[0] as Boolean

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