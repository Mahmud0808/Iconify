package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.layout.hookLayout
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs

@SuppressLint("DiscouragedApi")
class SwapSignalNetworkType(context: Context) : ModPack(context) {

    private var swapCellularAndNetworkTypeIcon = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            swapCellularAndNetworkTypeIcon =
                getBoolean(XposedKey.STATUSBAR_SWAP_CELLULAR_NETWORK_TYPE)
        }
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
        try {
            hookLayout()
                .packageName(SYSTEMUI_PACKAGE)
                .resource("layout", "status_bar_mobile_signal_group_inner")
                .throwError()
                .run { liparam ->
                    if (!swapCellularAndNetworkTypeIcon) return@run

                    val networkTypeContainer = liparam.view.findViewById<ViewGroup>(
                        liparam.res.getIdentifier(
                            "mobile_type_container",
                            "id",
                            mContext.packageName
                        )
                    )

                    (networkTypeContainer.parent as ViewGroup).reAddView(networkTypeContainer)
                }

            val modernStatusBarMobileViewClass =
                findClass("$SYSTEMUI_PACKAGE.statusbar.pipeline.mobile.ui.view.ModernStatusBarMobileView")

            modernStatusBarMobileViewClass
                .hookMethod("configureLayoutForNewStatusBarIcons")
                .runAfter { chain, _ ->
                    if (!swapCellularAndNetworkTypeIcon) return@runAfter

                    val parent = chain.thisObject as ViewGroup

                    val networkTypeContainer = parent.findViewById<ViewGroup>(
                        parent.resources.getIdentifier(
                            "mobile_type_container",
                            "id",
                            mContext.packageName
                        )
                    )
                    val networkType = networkTypeContainer.findViewById<View>(
                        networkTypeContainer.resources.getIdentifier(
                            "mobile_type",
                            "id",
                            mContext.packageName
                        )
                    )

                    (networkTypeContainer.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        marginStart = networkTypeContainer.resources.getDimensionPixelSize(
                            networkTypeContainer.resources.getIdentifier(
                                "status_bar_mobile_type_container_margin_end",
                                "dimen",
                                mContext.packageName
                            )
                        )
                        marginEnd = 0
                    }
                    (networkType.layoutParams as ViewGroup.MarginLayoutParams).apply {
                        marginStart = 0
                        marginEnd = 0
                    }
                }
        } catch (_: Throwable) {
            hookLayout()
                .packageName(SYSTEMUI_PACKAGE)
                .resource("layout", "status_bar_mobile_signal_group")
                .run { liparam ->
                    if (!swapCellularAndNetworkTypeIcon) return@run

                    val networkType = liparam.view.findViewById<View>(
                        liparam.res.getIdentifier(
                            "mobile_type",
                            "id",
                            mContext.packageName
                        )
                    )

                    (networkType.parent as ViewGroup).reAddView(networkType)
                }
        }
    }
}