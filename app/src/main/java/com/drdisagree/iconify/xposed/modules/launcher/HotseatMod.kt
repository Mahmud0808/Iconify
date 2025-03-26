package com.drdisagree.iconify.xposed.modules.launcher

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.drdisagree.iconify.data.common.Preferences.DESKTOP_DOCK_SPACING
import com.drdisagree.iconify.data.common.Preferences.DESKTOP_SEARCH_BAR
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.launcher.LauncherUtils.Companion.restartLauncher
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class HotseatMod(context: Context) : ModPack(context) {

    private var hideDesktopSearchBar = false
    private var desktopDockSpacing = -1
    private var mQuickSearchBar: View? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            hideDesktopSearchBar = getBoolean(DESKTOP_SEARCH_BAR, false)
            desktopDockSpacing = getSliderInt(DESKTOP_DOCK_SPACING, -1)
        }

        when (key.firstOrNull()) {
            DESKTOP_SEARCH_BAR -> {
                triggerSearchBarVisibility()
                restartLauncher(mContext)
            }

            DESKTOP_DOCK_SPACING -> restartLauncher(mContext)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val hotseatClass = findClass("com.android.launcher3.Hotseat")
        val workspaceClass = findClass("com.android.launcher3.Workspace")

        hotseatClass
            .hookConstructor()
            .parameters(
                Context::class.java,
                AttributeSet::class.java,
                Int::class.javaPrimitiveType
            )
            .runAfter { param ->
                mQuickSearchBar = param.thisObject.getField("mQsb") as View
                triggerSearchBarVisibility()
            }

        hotseatClass
            .hookMethod("setInsets")
            .runAfter { param ->
                mQuickSearchBar = param.thisObject.getField("mQsb") as View
                triggerSearchBarVisibility()
            }

        workspaceClass
            .hookMethod("setInsets")
            .runAfter { param ->
                val mLauncher = param.thisObject.getField("mLauncher")
                val grid = mLauncher.callMethod("getDeviceProfile")
                val padding = grid.getField("workspacePadding") as Rect
                val workspace = param.thisObject as View

                workspace.setPadding(
                    padding.left,
                    padding.top,
                    padding.right,
                    if (desktopDockSpacing == -1) padding.bottom
                    else mContext.toPx(desktopDockSpacing + 20)
                )
            }

        ResourceHookManager
            .hookDimen()
            .whenCondition { hideDesktopSearchBar }
            .forPackageName(loadPackageParam.packageName)
            .addResource("qsb_widget_height") { 0 }
            .apply()
    }

    private fun triggerSearchBarVisibility() {
        mQuickSearchBar?.visibility = if (hideDesktopSearchBar) View.GONE else View.VISIBLE
    }
}