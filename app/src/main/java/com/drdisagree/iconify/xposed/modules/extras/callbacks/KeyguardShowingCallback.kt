package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookConstructor
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.hook.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getField
import java.util.concurrent.CopyOnWriteArrayList

class KeyguardShowingCallback(context: Context) : ModPack(context) {

    @Volatile
    private var isKeyguardState: Boolean = false
    private var keyguardUpdateMonitorInstance: Any? = null
    private val mKeyguardShowingListeners = CopyOnWriteArrayList<KeyguardShowingListener>()

    val isKeyguardShowing: Boolean
        get() = isKeyguardState

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(packageParam: PackageParam) {
        instance = this

        val notificationPanelViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController")

        notificationPanelViewControllerClass
            .hookMethodMatchPattern("onPanelStateChanged.*")
            .suppressError()
            .runAfter { chain, _ ->
                val isKeyguardState =
                    chain.thisObject.getField("mBarState") in listOf(KEYGUARD, SHADE_LOCKED)

                synchronized(this) {
                    if (this.isKeyguardState != isKeyguardState) {
                        if (isKeyguardState) {
                            notifyKeyguardShown()
                        } else {
                            notifyKeyguardDismissed()
                        }
                        this.isKeyguardState = isKeyguardState
                    }
                }
            }

        val visualStabilityCoordinatorClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.collection.coordinator.VisualStabilityCoordinator")

        visualStabilityCoordinatorClass
            .hookConstructor()
            .runAfter { chain, _ ->
                val mStatusBarStateControllerListener =
                    chain.thisObject.getField("mStatusBarStateControllerListener")

                mStatusBarStateControllerListener::class.java
                    .hookMethod("onExpandedChanged")
                    .runAfter runAfter2@{ _, _ ->
                        if (keyguardUpdateMonitorInstance == null) return@runAfter2

                        val isKeyguardState =
                            keyguardUpdateMonitorInstance.getField("mStatusBarState") in listOf(
                                KEYGUARD,
                                SHADE_LOCKED
                            )

                        synchronized(this) {
                            if (this.isKeyguardState != isKeyguardState) {
                                if (isKeyguardState) {
                                    notifyKeyguardShown()
                                } else {
                                    notifyKeyguardDismissed()
                                }
                                this.isKeyguardState = isKeyguardState
                            }
                        }
                    }
            }

        val keyguardUpdateMonitorClass = findClass("com.android.keyguard.KeyguardUpdateMonitor")

        keyguardUpdateMonitorClass
            .hookConstructor()
            .runAfter { chain, _ ->
                keyguardUpdateMonitorInstance = chain.thisObject
            }

        keyguardUpdateMonitorClass
            ?.declaredFields
            ?.firstOrNull { it.name == "mStatusBarStateControllerListener" }
            ?.type
            .hookMethod("onStateChanged")
            .suppressError()
            .runAfter { chain, _ ->
                val isKeyguardState = chain.args[0] in listOf(KEYGUARD, SHADE_LOCKED)

                synchronized(this) {
                    if (this.isKeyguardState != isKeyguardState) {
                        if (isKeyguardState) {
                            notifyKeyguardShown()
                        } else {
                            notifyKeyguardDismissed()
                        }
                        this.isKeyguardState = isKeyguardState
                    }
                }
            }
    }

    interface KeyguardShowingListener {
        fun onKeyguardShown()
        fun onKeyguardDismissed()
    }

    private fun notifyKeyguardShown() {
        mKeyguardShowingListeners.forEach {
            try {
                it.onKeyguardShown()
            } catch (throwable: Throwable) {
                log(this@KeyguardShowingCallback, "notifyKeyguardShown: $throwable")
            }
        }
    }

    private fun notifyKeyguardDismissed() {
        mKeyguardShowingListeners.forEach {
            try {
                it.onKeyguardDismissed()
            } catch (throwable: Throwable) {
                log(this@KeyguardShowingCallback, "notifyKeyguardDismissed: $throwable")
            }
        }
    }

    fun registerKeyguardShowingListener(callback: KeyguardShowingListener) {
        if (!mKeyguardShowingListeners.contains(callback)) {
            mKeyguardShowingListeners.add(callback)
        }
    }

    fun unregisterKeyguardShowingListener(callback: KeyguardShowingListener) {
        mKeyguardShowingListeners.remove(callback)
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: KeyguardShowingCallback? = null

        fun getInstance(): KeyguardShowingCallback {
            return checkNotNull(instance) { "KeyguardShowingCallback is not initialized yet!" }
        }

        // StatusBarState: https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/StatusBarState.java

        /**
         * The status bar is in the "normal", unlocked mode or the device is still locked, but we're
         * accessing camera from power button double-tap shortcut.
         */
        const val SHADE: Int = 0

        /**
         * Status bar is currently the Keyguard. In single column mode, when you swipe from the top of
         * the keyguard to expand QS immediately, it's still KEYGUARD state.
         */
        const val KEYGUARD: Int = 1

        /**
         * Status bar is in the special mode, where it was transitioned from lockscreen to shade.
         * Depending on user's security settings, dismissing the shade will either show the
         * bouncer or go directly to unlocked [.SHADE] mode.
         */
        const val SHADE_LOCKED: Int = 2
    }
}