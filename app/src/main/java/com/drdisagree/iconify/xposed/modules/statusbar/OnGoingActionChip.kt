package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.service.notification.StatusBarNotification
import android.view.View
import android.view.ViewGroup
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ONGOING_ACTION_CHIP_SWITCH
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip.OnGoingActionChipView
import com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip.OnGoingActionProgressController
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class OnGoingActionChip(context: Context) : ModPack(context) {

    private var onGoingActionChipEnabled = false
    private var mOnGoingActionChipView: OnGoingActionChipView? = null
    private var mOnGoingActionProgressController: OnGoingActionProgressController? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            onGoingActionChipEnabled = getBoolean(ONGOING_ACTION_CHIP_SWITCH, false)
        }

        when (key.firstOrNull()) {
            ONGOING_ACTION_CHIP_SWITCH -> mOnGoingActionProgressController?.setForceHidden(!onGoingActionChipEnabled)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val phoneStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarView")

        phoneStatusBarViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                val phoneStatusBarView = param.thisObject as ViewGroup
                val notificationIconArea = phoneStatusBarView.findViewById<View>(
                    mContext.resources.getIdentifier(
                        "notification_icon_area",
                        "id",
                        mContext.packageName
                    )
                )
                val startSideExceptHeadsUp = notificationIconArea.parent as ViewGroup
                val activityChipIndex = startSideExceptHeadsUp.indexOfChild(notificationIconArea)

                if (mOnGoingActionChipView == null) {
                    mOnGoingActionChipView = OnGoingActionChipView(mContext)
                }

                if (mOnGoingActionProgressController == null) {
                    mOnGoingActionProgressController = OnGoingActionProgressController(
                        mContext,
                        mOnGoingActionChipView!!
                    ) { onGoingActionChipEnabled }
                }

                startSideExceptHeadsUp.reAddView(mOnGoingActionChipView, activityChipIndex)
            }

        val notificationListenerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.NotificationListener")

        notificationListenerClass
            .hookMethod("onNotificationPosted")
            .runAfter { param ->
                val sbn = param.args[0] as StatusBarNotification
                mOnGoingActionProgressController?.onNotificationPosted(sbn)
            }

        notificationListenerClass
            .hookMethod("onNotificationRemoved")
            .runAfter { param ->
                val sbn = param.args[0] as StatusBarNotification
                mOnGoingActionProgressController?.onNotificationRemoved(sbn)
            }

        val keyguardStateControllerImplClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.policy.KeyguardStateControllerImpl")

        keyguardStateControllerImplClass
            .hookMethod("notifyKeyguardState")
            .runAfter { param ->
                val showing = param.args[0] as Boolean
                mOnGoingActionProgressController?.setForceHidden(showing || !onGoingActionChipEnabled)
            }

        val headsUpAppearanceControllerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.HeadsUpAppearanceController")

        headsUpAppearanceControllerClass
            .hookMethod("updateTopEntry")
            .runBefore { param ->
                var newEntry: Any? = null
                val shouldBeVisible = (param.thisObject.callMethodSilently("shouldBeVisible")
                    ?: param.thisObject.callMethod("shouldBeVisible$1")) as Boolean

                if (shouldBeVisible) {
                    val mHeadsUpManager = param.thisObject.getField("mHeadsUpManager")

                    newEntry = try {
                        mHeadsUpManager.callMethod("getTopEntry")
                    } catch (_: Throwable) {
                        mHeadsUpManager.callMethod("getTopHeadsUpEntry")?.getFieldSilently("mEntry")
                    }
                }

                val headsUpStatusBarView = param.thisObject.getField("mView")
                val previousEntry = try {
                    headsUpStatusBarView.callMethod("getShowingEntry")
                } catch (_: Throwable) {
                    headsUpStatusBarView.getFieldSilently("mShowingEntry")
                }

                if (previousEntry != newEntry) {
                    if (newEntry == null) {
                        mOnGoingActionChipView?.alpha = 1f
                    } else if (previousEntry == null) {
                        mOnGoingActionChipView?.alpha = 0f
                    }
                }
            }
    }
}