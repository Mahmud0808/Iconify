package com.drdisagree.iconify.xposed.modules.lockscreen

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenComposeContainer
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.removeViewFromParent
import com.drdisagree.iconify.xposed.modules.extras.views.AodBurnInProtection
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class LockscreenClockCompose(context: Context) : LockscreenClock(context) {

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        initResources(mContext)
        hookClockUpdates()

        LockscreenComposeContainer.register(
            isEnabled = { showLockscreenClock },
            replacesClock = { true },
            listener = object : LockscreenComposeContainer.Listener {
                override fun onContainerAttached(
                    container: LinearLayout,
                    slot: LockscreenComposeContainer.Slot
                ) {
                    if (slot != LockscreenComposeContainer.Slot.MAIN) return
                    if (!showLockscreenClock) return

                    mLsItemsContainer = container
                    aodBurnInProtection = AodBurnInProtection.registerForView(container).apply {
                        setMovementEnabled(DozeCallback.getInstance().isDozing())
                    }
                    registerClockUpdater()
                    ensureClockAdded(container)
                }

                override fun onContainerDetached(
                    container: LinearLayout,
                    slot: LockscreenComposeContainer.Slot
                ) {
                    if (slot != LockscreenComposeContainer.Slot.MAIN) return
                    unregisterClockUpdater()
                }
            }
        )
    }

    override fun updatePrefs(vararg key: String) {
        super.updatePrefs(*key)
        if (key.firstOrNull() != XposedKey.CUSTOM_LOCKSCREEN_CLOCK.name) return

        val container = LockscreenComposeContainer.containerOrNull() ?: return

        if (showLockscreenClock) {
            if (mLsItemsContainer == null && container.isAttachedToWindow) {
                mLsItemsContainer = container
                registerClockUpdater()
            }
        } else {
            unregisterClockUpdater()
            container.findViewWithTag<View?>(ICONIFY_LOCKSCREEN_CLOCK_TAG).removeViewFromParent()
            mLsItemsContainer = null
        }
    }

    private fun ensureClockAdded(container: LinearLayout, attempt: Int = 0) {
        if (attempt >= CLOCK_RETRY_LIMIT) return

        container.postDelayed({
            if (!showLockscreenClock || mLsItemsContainer !== container) return@postDelayed
            if (!container.isAttachedToWindow) return@postDelayed
            if (container.findViewWithTag<View?>(ICONIFY_LOCKSCREEN_CLOCK_TAG) != null) return@postDelayed

            updateClockView(force = true)
            ensureClockAdded(container, attempt + 1)
        }, CLOCK_RETRY_DELAY)
    }

    override fun resetStockClock() {}

    companion object {
        private const val CLOCK_RETRY_LIMIT = 20
        private const val CLOCK_RETRY_DELAY = 250L
    }
}
