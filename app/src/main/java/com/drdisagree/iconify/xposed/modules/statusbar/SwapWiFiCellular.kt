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
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class SwapWiFiCellular(context: Context) : ModPack(context) {

    private var swapWifiAndCellularIcon = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            swapWifiAndCellularIcon = getBoolean(XposedKey.STATUSBAR_SWAP_WIFI_CELLULAR)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val statusIconContainerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.ui.IconManager")

        statusIconContainerClass
            .hookMethod("addNewWifiIcon", "addNewMobileIcon", "addHolder")
            .runAfter { param ->
                if (!swapWifiAndCellularIcon) return@runAfter

                val parent = param.thisObject.getField("mGroup") as ViewGroup

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
                val mobileViews = parent.children
                    .filter { it.id == mobileId }
                    .toMutableList()

                if (mobileViews.isNotEmpty() && wifiView != null) {
                    val firstMobileView = mobileViews.first()
                    val firstMobileIndex = parent.indexOfChild(firstMobileView)

                    if (firstMobileIndex < parent.indexOfChild(wifiView)) {
                        parent.reAddView(wifiView, firstMobileIndex - 1)
                    }
                }
            }

        val configStatusBarIconsId = mContext.resources.getIdentifier(
            "config_statusBarIcons",
            "array",
            FRAMEWORK_PACKAGE
        )

        @Suppress("UNCHECKED_CAST")
        Resources::class.java
            .hookMethod("getStringArray")
            .runAfter { param ->
                if (swapWifiAndCellularIcon && param.args[0] == configStatusBarIconsId) {
                    val result = (param.result as Array<String>).toMutableList()
                    val mobileIndex = result.indexOf("mobile")

                    if (mobileIndex != -1) {
                        result.remove("wifi")
                        result.add(mobileIndex - 1, "wifi")
                    }

                    param.result = result.toTypedArray()
                }
            }
    }
}