package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.service.notification.StatusBarNotification
import android.view.View
import android.view.ViewGroup
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.reflect.callMethodSilently
import com.drdisagree.iconify.xposed.core.reflect.callStaticMethod
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getField
import com.drdisagree.iconify.xposed.core.reflect.getFieldSilently
import com.drdisagree.iconify.xposed.core.reflect.setField
import com.drdisagree.iconify.xposed.modules.extras.callbacks.HeadsUpCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip.OnGoingActionChipView
import com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip.OnGoingActionProgressController
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import java.util.WeakHashMap

@SuppressLint("DiscouragedApi")
class OnGoingActionChip(context: Context) : ModPack(context) {

    private var onGoingActionChipEnabled = false
    private var mOnGoingActionChipView: OnGoingActionChipView? = null
    private var mOnGoingActionProgressController: OnGoingActionProgressController? = null
    private var mNotificationListener: Any? = null
    private var mColoredStatusbarIcon = false
    private var mCompactStyle = false
    private val mStatusBarIconContainers = WeakHashMap<ViewGroup, Unit>()
    private val mNotificationIconAreaId by lazy { idOf("notification_icon_area") }

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            onGoingActionChipEnabled = getBoolean(XposedKey.ONGOING_ACTION_CHIP)
            mColoredStatusbarIcon = getBoolean(XposedKey.COLORED_STATUSBAR_ICON)
            mCompactStyle = getString(XposedKey.ONGOING_ACTION_CHIP_STYLE) == "1"
        }

        mOnGoingActionChipView?.setCompact(mCompactStyle)

        when (key.firstOrNull()) {
            XposedKey.ONGOING_ACTION_CHIP.name -> mOnGoingActionProgressController?.setForceHidden(!onGoingActionChipEnabled)
        }
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
        val phoneStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarView")

        phoneStatusBarViewClass
            .hookMethod("onFinishInflate")
            .runAfter { chain, _ ->
                val phoneStatusBarView = chain.thisObject as ViewGroup
                val notificationIconArea =
                    phoneStatusBarView.findViewById<View>(mNotificationIconAreaId)
                        ?: return@runAfter
                val startSideExceptHeadsUp = phoneStatusBarView.findViewById(
                    idOf("status_bar_start_side_except_heads_up")
                ) ?: notificationIconArea.parent as ViewGroup
                val anchor = notificationIconArea.childOf(startSideExceptHeadsUp)
                    ?: notificationIconArea
                val container = anchor.parent as ViewGroup

                if (mOnGoingActionChipView == null) {
                    mOnGoingActionChipView = OnGoingActionChipView(mContext).apply {
                        setCompact(mCompactStyle)
                    }
                }

                if (mOnGoingActionProgressController == null) {
                    mOnGoingActionProgressController = OnGoingActionProgressController(
                        mContext,
                        mOnGoingActionChipView!!,
                        !mColoredStatusbarIcon
                    ) { onGoingActionChipEnabled }.apply {
                        onChipKeyChanged = { relayoutStatusBarIcons() }
                    }
                }

                container.reAddView(mOnGoingActionChipView, container.indexOfChild(anchor))
                replayActiveNotifications()
            }

        val darkIconDispatcherClass = findClass("$SYSTEMUI_PACKAGE.plugins.DarkIconDispatcher")

        findClass("$SYSTEMUI_PACKAGE.statusbar.policy.Clock")
            .hookMethod("onDarkChanged")
            .runAfter { chain, _ ->
                val chipView = mOnGoingActionChipView ?: return@runAfter
                val tint = darkIconDispatcherClass.callStaticMethod(
                    "getTint",
                    chain.args[0],
                    chipView,
                    chain.args[2]
                ) as? Int ?: return@runAfter
                chipView.setDarkTint(tint)
            }

        val notificationIconContainerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.NotificationIconContainer")

        notificationIconContainerClass
            .hookMethod("onLayout")
            .runBefore { chain ->
                val iconContainer = chain.thisObject as ViewGroup
                if (iconContainer.isStatusBarIconContainer()) {
                    mStatusBarIconContainers[iconContainer] = Unit
                }
            }

        notificationIconContainerClass
            .hookMethod("calculateIconXTranslations")
            .intercept { chain ->
                val hiddenStates = hideChipIconStates(chain.thisObject as ViewGroup)

                try {
                    chain.proceed()
                } finally {
                    hiddenStates.forEach { (iconState, appearAmount) ->
                        iconState.setField("iconAppearAmount", appearAmount)
                    }
                }
            }

        val notificationListenerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.NotificationListener")

        notificationListenerClass
            .hookMethod("onListenerConnected")
            .runAfter { chain, _ ->
                mNotificationListener = chain.thisObject
                replayActiveNotifications()
            }

        notificationListenerClass
            .hookMethod("onNotificationPosted")
            .runAfter { chain, _ ->
                val sbn = chain.args[0] as StatusBarNotification
                mOnGoingActionProgressController?.onNotificationPosted(sbn)
            }

        notificationListenerClass
            .hookMethod("onNotificationRemoved")
            .runAfter { chain, _ ->
                val sbn = chain.args[0] as StatusBarNotification
                mOnGoingActionProgressController?.onNotificationRemoved(sbn)
            }

        val keyguardStateControllerImplClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.policy.KeyguardStateControllerImpl")

        keyguardStateControllerImplClass
            .hookMethod("notifyKeyguardState")
            .runAfter { chain, _ ->
                val showing = chain.args[0] as Boolean
                mOnGoingActionProgressController?.setForceHidden(showing || !onGoingActionChipEnabled)
            }

        HeadsUpCallback.getInstance().registerHeadsUpListener(
            object : HeadsUpCallback.HeadsUpListener {
                override fun onHeadsUpShown() {
                    mOnGoingActionChipView?.alpha = 0f
                }

                override fun onHeadsUpGone() {
                    mOnGoingActionChipView?.alpha = 1f
                }
            }
        )
    }

    private fun idOf(name: String): Int =
        mContext.resources.getIdentifier(name, "id", mContext.packageName)

    private fun View.childOf(ancestor: ViewGroup): View? {
        var child: View = this
        while (true) {
            val parent = child.parent as? View ?: return null
            if (parent === ancestor) return child
            child = parent
        }
    }

    private fun ViewGroup.isStatusBarIconContainer(): Boolean =
        (parent as? View)?.id == mNotificationIconAreaId

    private fun replayActiveNotifications() {
        val listener = mNotificationListener ?: return
        val controller = mOnGoingActionProgressController ?: return
        val chipView = mOnGoingActionChipView ?: return

        chipView.post {
            val notifications =
                listener.callMethodSilently("getActiveNotifications") as? Array<*> ?: return@post
            notifications.filterIsInstance<StatusBarNotification>()
                .forEach { controller.onNotificationPosted(it) }
        }
    }

    private fun relayoutStatusBarIcons() {
        mStatusBarIconContainers.keys.toList().forEach { iconContainer ->
            iconContainer.post { iconContainer.requestLayout() }
        }
    }

    private fun hideChipIconStates(iconContainer: ViewGroup): List<Pair<Any, Float>> {
        val chipKey = mOnGoingActionProgressController?.chipNotificationKey ?: return emptyList()
        if (!iconContainer.isStatusBarIconContainer()) return emptyList()

        @Suppress("UNCHECKED_CAST")
        val iconStates = iconContainer.getFieldSilently("mIconStates") as? Map<View, Any>
            ?: return emptyList()
        val hiddenStates = mutableListOf<Pair<Any, Float>>()

        for (i in 0 until iconContainer.childCount) {
            val icon = iconContainer.getChildAt(i)
            val sbn = icon.getFieldSilently("mNotification") as? StatusBarNotification
            if (sbn?.key != chipKey) continue

            val iconState = iconStates[icon] ?: continue
            hiddenStates += iconState to iconState.getField("iconAppearAmount") as Float
            iconState.setField("iconAppearAmount", 0f)
            iconState.setField("hidden", true)
        }

        return hiddenStates
    }
}