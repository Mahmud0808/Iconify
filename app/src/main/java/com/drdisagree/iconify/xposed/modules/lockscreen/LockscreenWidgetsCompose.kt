package com.drdisagree.iconify.xposed.modules.lockscreen

import android.content.Context
import android.widget.LinearLayout
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenComposeContainer
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenComposeContainer.Slot
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenSceneInjector
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.removeViewFromParent
import com.drdisagree.iconify.xposed.modules.extras.views.AodBurnInProtection
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class LockscreenWidgetsCompose(context: Context) : LockscreenWidgets(context) {

    private var isLargeClock = false

    private val targetSlot: Slot
        get() = if (!mLockscreenClockEnabled && isLargeClock) Slot.BELOW_LARGE_CLOCK else Slot.MAIN

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setupWidgetsCommon()

        LockscreenComposeContainer.register(
            isEnabled = { mWidgetsEnabled },
            wantsLowerSlot = { !mLockscreenClockEnabled },
            listener = object : LockscreenComposeContainer.Listener {
                override fun onContainerAttached(container: LinearLayout, slot: Slot) {
                    if (mWidgetsEnabled && slot == targetSlot) attachTo(container)
                }
            }
        )

        LockscreenSceneInjector.addClockSizeListener { largeClock ->
            isLargeClock = largeClock
            if (!mWidgetsEnabled) return@addClockSizeListener

            updateLockscreenWidgetsOnClock(largeClock)
            LockscreenComposeContainer.containerOrNull(targetSlot)
                ?.takeIf { it.isAttachedToWindow }
                ?.let { attachTo(it) }
        }
    }

    override fun updatePrefs(vararg key: String) {
        super.updatePrefs(*key)
        if (key.firstOrNull() != XposedKey.LOCKSCREEN_WIDGETS.name) return

        if (mWidgetsEnabled) {
            LockscreenComposeContainer.containerOrNull(targetSlot)
                ?.takeIf { it.isAttachedToWindow }
                ?.let { attachTo(it) }
        } else {
            mWidgetsContainer.removeViewFromParent()
            mLsItemsContainer = null
        }
    }

    override fun isWidgetsHostReady(): Boolean = mLsItemsContainer != null

    private fun attachTo(container: LinearLayout) {
        mLsItemsContainer = container

        if (mWidgetsContainer.parent !== container) {
            mWidgetsContainer.removeViewFromParent()
            container.addView(mWidgetsContainer)
        }

        aodBurnInProtection = AodBurnInProtection.registerForView(container).apply {
            setMovementEnabled(DozeCallback.getInstance().isDozing())
        }

        placeWidgetsView()
    }
}
