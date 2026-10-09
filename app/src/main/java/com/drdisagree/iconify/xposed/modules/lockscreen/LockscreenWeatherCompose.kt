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

class LockscreenWeatherCompose(context: Context) : LockscreenWeather(context) {

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        setupWeatherCommon()

        LockscreenComposeContainer.register(
            isEnabled = { mWeatherEnabled },
            listener = object : LockscreenComposeContainer.Listener {
                override fun onContainerAttached(
                    container: LinearLayout,
                    slot: LockscreenComposeContainer.Slot
                ) {
                    if (slot != LockscreenComposeContainer.Slot.MAIN) return
                    if (mWeatherEnabled) attachTo(container)
                }
            }
        )
    }

    override fun updatePrefs(vararg key: String) {
        super.updatePrefs(*key)
        if (key.firstOrNull() != XposedKey.LOCKSCREEN_WEATHER.name) return

        val container = LockscreenComposeContainer.containerOrNull() ?: return

        if (mWeatherEnabled) {
            if (container.isAttachedToWindow) attachTo(container)
        } else {
            mWeatherContainer.removeViewFromParent()
            mLsItemsContainer = null
        }
    }

    override fun isWeatherHostReady(): Boolean = mLsItemsContainer != null

    private fun attachTo(container: LinearLayout) {
        mLsItemsContainer = container

        if (mWeatherContainer.parent !== container) {
            mWeatherContainer.removeViewFromParent()
            val index = if (container.findViewWithTag<View?>(ICONIFY_LOCKSCREEN_CLOCK_TAG) != null) 1 else 0
            container.addView(mWeatherContainer, index)
        }

        aodBurnInProtection = AodBurnInProtection.registerForView(container).apply {
            setMovementEnabled(DozeCallback.getInstance().isDozing())
        }

        placeWeatherView()
    }
}
