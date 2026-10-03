package com.drdisagree.iconify.xposed.modules.extras.callbacks

import android.annotation.SuppressLint
import android.content.Context
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.util.concurrent.CopyOnWriteArrayList

class HeadsUpCallback(context: Context) : ModPack(context) {

    private val mHeadsUpListeners = CopyOnWriteArrayList<HeadsUpListener>()
    private var mHeadsUpShowing = false
    private var mHasHeadsUpState = false

    override fun updatePrefs(vararg key: String) {}

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        instance = this

        val headsUpManagerImplClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.headsup.HeadsUpManagerImpl")

        headsUpManagerImplClass
            .hookMethod("onEntryAdded")
            .runAfter { param -> updateHeadsUpState(param.thisObject.hasHeadsUp() ?: true) }

        headsUpManagerImplClass
            .hookMethod("onEntryRemoved")
            .runAfter { param -> param.thisObject.hasHeadsUp()?.let { updateHeadsUpState(it) } }

        val notificationEntryAdapterClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.collection.NotificationEntryAdapter")

        notificationEntryAdapterClass
            .hookMethod("onEntryAnimatingAwayEnded")
            .suppressError()
            .runAfter {
                if (!mHasHeadsUpState) updateHeadsUpState(false)
            }
    }

    private fun Any.hasHeadsUp(): Boolean? {
        val showing = (callMethodSilently("hasNotifications")
            ?: callMethodSilently("hasPinnedHeadsUp")) as? Boolean
        if (showing != null) mHasHeadsUpState = true
        return showing
    }

    private fun updateHeadsUpState(showing: Boolean) {
        if (showing == mHeadsUpShowing) return
        mHeadsUpShowing = showing
        if (showing) notifyHeadsUpShown() else notifyHeadsUpGone()
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