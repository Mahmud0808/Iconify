package com.drdisagree.iconify.xposed.modules.launcher

import android.content.Context
import android.graphics.Rect
import com.drdisagree.iconify.data.common.Preferences.APP_DRAWER_ICON_LABELS
import com.drdisagree.iconify.data.common.Preferences.DESKTOP_ICON_LABELS
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getExtraFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setExtraField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class IconLabels(context: Context) : ModPack(context) {

    private var showDesktopLabels = true
    private var showDrawerLabels = true
    private var invariantDeviceProfileInstance: Any? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showDesktopLabels = getBoolean(DESKTOP_ICON_LABELS, true)
            showDrawerLabels = getBoolean(APP_DRAWER_ICON_LABELS, true)
        }

        when (key.firstOrNull()) {
            in setOf(
                DESKTOP_ICON_LABELS,
                APP_DRAWER_ICON_LABELS
            ) -> reloadLauncher()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val invariantDeviceProfileClass = findClass("com.android.launcher3.InvariantDeviceProfile")
        val bubbleTextViewClass = findClass("com.android.launcher3.BubbleTextView")
        val deviceProfileClass = findClass("com.android.launcher3.DeviceProfile")

        invariantDeviceProfileClass
            .hookConstructor()
            .runAfter { param ->
                invariantDeviceProfileInstance = param.thisObject
            }

        fun XC_MethodHook.MethodHookParam.beforeHookedLabel() {
            val mDisplay = thisObject.getField("mDisplay") as Int
            val itemInfo = args[0]

            fun removeLabel() {
                val title = itemInfo.getFieldSilently("title")
                if (title != null) {
                    itemInfo.setExtraField("titleText", title)
                }
                itemInfo.setField("title", null)
            }

            if (mDisplay.isDesktop() && !showDesktopLabels) {
                removeLabel()
            } else if (mDisplay.isDrawer() && !showDrawerLabels) {
                removeLabel()
            }
        }

        fun XC_MethodHook.MethodHookParam.afterHookedLabel() {
            val mDisplay = thisObject.getField("mDisplay") as Int
            val itemInfo = args[0]

            fun reAddLabel() {
                val title = itemInfo.getExtraFieldSilently("titleText")
                if (title != null) {
                    itemInfo.setField("title", title)
                }
            }

            if (mDisplay.isDesktop() && !showDesktopLabels) {
                reAddLabel()
            } else if (mDisplay.isDrawer() && !showDrawerLabels) {
                reAddLabel()
            }
        }

        try {
            bubbleTextViewClass
                .hookMethod("applyLabel")
                .throwError()
                .runBefore { param -> param.beforeHookedLabel() }
                .runAfter { param -> param.afterHookedLabel() }
        } catch (_: Throwable) {
            bubbleTextViewClass
                .hookMethod("applyIconAndLabel")
                .parameters("com.android.launcher3.model.data.ItemInfoWithIcon")
                .runBefore { param -> param.beforeHookedLabel() }
                .runAfter { param -> param.afterHookedLabel() }
        }

        deviceProfileClass
            .hookMethod(
                "updateIconSize",
                "autoResizeAllAppsCells"
            )
            .runAfter { param ->
                if (showDrawerLabels) return@runAfter

                val cellLayoutPaddingPx = param.thisObject.getField("cellLayoutPaddingPx") as Rect
                val desiredWorkspaceHorizontalMarginPx =
                    param.thisObject.getField("desiredWorkspaceHorizontalMarginPx") as Int
                val availableWidthPx = param.thisObject.getField("availableWidthPx") as Int

                val cellLayoutHorizontalPadding =
                    (cellLayoutPaddingPx.left + cellLayoutPaddingPx.right) / 2
                val leftRightPadding =
                    desiredWorkspaceHorizontalMarginPx + cellLayoutHorizontalPadding
                val drawerWidth = availableWidthPx - leftRightPadding * 2
                val invariantDeviceProfile = param.thisObject.getField("inv")

                val allAppsCellHeightPx =
                    (drawerWidth / invariantDeviceProfile.getField("numAllAppsColumns") as Int)
                val allAppsIconDrawablePaddingPx = 0

                param.thisObject.setField(
                    "allAppsCellHeightPx",
                    allAppsCellHeightPx
                )
                param.thisObject.setField(
                    "allAppsIconDrawablePaddingPx",
                    allAppsIconDrawablePaddingPx
                )
            }
    }

    private fun Int.isDesktop(): Boolean {
        return this in setOf(
            DISPLAY_WORKSPACE,
            DISPLAY_FOLDER,
            DISPLAY_SEARCH_RESULT,
            DISPLAY_SEARCH_RESULT_SMALL
        )
    }

    private fun Int.isDrawer(): Boolean {
        return this in setOf(
            DISPLAY_ALL_APPS,
            DISPLAY_PREDICTION_ROW,
            DISPLAY_SEARCH_RESULT_APP_ROW,
        )
    }

    private fun reloadLauncher() {
        invariantDeviceProfileInstance.callMethod("onConfigChanged", mContext)
    }

    companion object {
        const val DISPLAY_WORKSPACE: Int = 0
        const val DISPLAY_ALL_APPS: Int = 1
        const val DISPLAY_FOLDER: Int = 2
        const val DISPLAY_SEARCH_RESULT: Int = 6
        const val DISPLAY_SEARCH_RESULT_SMALL: Int = 7
        const val DISPLAY_PREDICTION_ROW: Int = 8
        const val DISPLAY_SEARCH_RESULT_APP_ROW: Int = 9
    }
}