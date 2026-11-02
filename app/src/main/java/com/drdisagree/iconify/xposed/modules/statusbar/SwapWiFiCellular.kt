package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.core.view.children
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookConstructor
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.callMethodSilently
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getField
import com.drdisagree.iconify.xposed.core.reflect.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import java.lang.ref.WeakReference

@SuppressLint("DiscouragedApi")
class SwapWiFiCellular(context: Context) : ModPack(context) {

    private var swapWifiAndCellularIcon = false
    private var orderedSlotNamesRepository: WeakReference<Any>? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            swapWifiAndCellularIcon = getBoolean(XposedKey.STATUSBAR_SWAP_WIFI_CELLULAR)
        }

        applyToOrderedSlotNames()
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
        val iconManagerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.ui.IconManager",
            suppressError = true
        )

        if (iconManagerClass != null) {
            iconManagerClass
                .hookMethod("addNewWifiIcon", "addNewMobileIcon", "addHolder")
                .runAfter { chain, _ -> reorderIconGroup(chain.thisObject) }
        } else {
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ui.TintedIconManager")
                .hookMethod("onIconAdded")
                .runAfter { chain, _ -> reorderIconGroup(chain.thisObject) }
        }

        findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.ui.StatusBarIconControllerImpl",
            suppressError = true
        )
            .hookMethod("onTuningChanged")
            .suppressError()
            .runAfter { chain, _ ->
                (chain.thisObject.getFieldSilently("mIconGroups") as? Collection<*>)
                    ?.filterNotNull()
                    ?.forEach { reorderIconGroup(it) }
                applyToOrderedSlotNames()
            }

        val configStatusBarIconsId = mContext.resources.getIdentifier(
            "config_statusBarIcons",
            "array",
            FRAMEWORK_PACKAGE
        )

        @Suppress("UNCHECKED_CAST")
        Resources::class.java
            .hookMethod("getStringArray")
            .intercept { chain ->
                var result = chain.proceed()
                if (swapWifiAndCellularIcon && chain.args[0] == configStatusBarIconsId) {
                    result = (result as Array<String>).toList()
                        .withWifiBeforeMobile()
                        .toTypedArray()
                }

                result
            }

        findClass(
            "$SYSTEMUI_PACKAGE.statusbar.systemstatusicons.data.repository.OrderedIconSlotNamesRepository",
            suppressError = true
        )
            .hookConstructor()
            .runAfter { chain, _ ->
                orderedSlotNamesRepository = WeakReference(chain.thisObject)
                applyToOrderedSlotNames()
            }

        BootCallback.registerBootListener {
            val enabled = Xprefs.getBoolean(XposedKey.STATUSBAR_SWAP_WIFI_CELLULAR)
            if (enabled != swapWifiAndCellularIcon) {
                swapWifiAndCellularIcon = enabled
            }
            applyToOrderedSlotNames()
        }
    }

    private fun reorderIconGroup(iconManager: Any) {
        if (!swapWifiAndCellularIcon) return

        val parent = iconManager.getField("mGroup") as ViewGroup

        val wifiView = parent.findViewById<View?>(
            mContext.resources.getIdentifier(
                "wifi_combo",
                "id",
                mContext.packageName
            )
        )

        val mobileId = mContext.resources.getIdentifier(
            "mobile_combo",
            "id",
            mContext.packageName
        )
        val firstMobileView = parent.children.firstOrNull { it.id == mobileId }

        if (firstMobileView != null && wifiView != null) {
            val firstMobileIndex = parent.indexOfChild(firstMobileView)

            if (firstMobileIndex < parent.indexOfChild(wifiView)) {
                parent.reAddView(wifiView, firstMobileIndex)
            }
        }
    }

    private fun applyToOrderedSlotNames() {
        val repository = orderedSlotNamesRepository?.get() ?: return
        val stateFlow = repository.getFieldSilently("_orderedIconSlotNames") ?: return

        @Suppress("UNCHECKED_CAST")
        val current = stateFlow.callMethodSilently("getValue") as? List<String> ?: return
        val updated = if (swapWifiAndCellularIcon) {
            current.withWifiBeforeMobile()
        } else {
            current.withWifiAfterMobile()
        }

        if (updated != current) {
            stateFlow.callMethodSilently("setValue", updated)
        }
    }

    private fun List<String>.withWifiBeforeMobile(): List<String> {
        val mobileIndex = indexOf(MOBILE_SLOT)
        if (mobileIndex == -1 || indexOf(WIFI_SLOT) < mobileIndex) return this

        return toMutableList().apply {
            remove(WIFI_SLOT)
            add((mobileIndex - 1).coerceAtLeast(0), WIFI_SLOT)
        }
    }

    private fun List<String>.withWifiAfterMobile(): List<String> {
        val wifiIndex = indexOf(WIFI_SLOT)
        if (wifiIndex == -1 || wifiIndex > indexOf(MOBILE_SLOT)) return this

        return toMutableList().apply {
            remove(WIFI_SLOT)
            add(indexOf(MOBILE_SLOT) + 1, WIFI_SLOT)
        }
    }

    companion object {
        private const val WIFI_SLOT = "wifi"
        private const val MOBILE_SLOT = "mobile"
    }
}