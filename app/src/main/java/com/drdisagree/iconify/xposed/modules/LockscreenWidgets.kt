package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.view.ViewGroup
import android.widget.LinearLayout
import com.drdisagree.iconify.common.Const.ACTION_WEATHER_INFLATED
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BIG_INACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_BOTTOM_MARGIN
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_CUSTOM_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_ENABLED
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_EXTRAS
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE
import com.drdisagree.iconify.common.Preferences.LOCKSCREEN_WIDGETS_SMALL_INACTIVE
import com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.WEATHER_SWITCH
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.views.LockscreenWidgetsView
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage

class LockscreenWidgets(context: Context?) : ModPack(context!!) {

    // Parent
    private var mStatusViewContainer: ViewGroup? = null
    private var mStatusArea: ViewGroup? = null

    // Widgets Container
    private lateinit var mWidgetsContainer: LinearLayout

    // Activity Starter
    private var mActivityStarter: Any? = null

    // Ls custom clock
    private var customLockscreenClock = false

    // Ls weather
    private var lsWeather = false
    private var lsWeatherInflated = false

    // Widgets Prefs
    // Lockscreen Widgets
    private var mWidgetsEnabled: Boolean = false
    private var mDeviceWidgetEnabled = false
    private var mDeviceCustomColor = false
    private var mDeviceLinearColor = Color.WHITE
    private var mDeviceCircularColor = Color.WHITE
    private var mDeviceTextColor = Color.WHITE
    private var mWidgetsCustomColor = false
    private var mBigInactiveColor = Color.BLACK
    private var mBigActiveColor = Color.WHITE
    private var mSmallInactiveColor = Color.BLACK
    private var mSmallActiveColor = Color.WHITE
    private var mBigIconActiveColor = Color.WHITE
    private var mBigIconInactiveColor = Color.BLACK
    private var mSmallIconActiveColor = Color.WHITE
    private var mSmallIconInactiveColor = Color.BLACK
    private var mDeviceName = ""
    private var mMainWidgets: String = ""
    private var mExtraWidgets: String = ""
    private var mBottomMargin = 0

    private var mBroadcastRegistered = false
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null) {
                if (intent.action == ACTION_WEATHER_INFLATED && mWidgetsEnabled) {
                    log(TAG + "Weather inflated")
                    lsWeatherInflated = true
                    placeWidgets()
                }
            }
        }
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            // Ls custom clock
            customLockscreenClock = getBoolean(LSCLOCK_SWITCH, false)

            // Ls weather
            lsWeather = getBoolean(WEATHER_SWITCH, false)

            // Widgets
            mWidgetsEnabled = getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false)
            mDeviceWidgetEnabled = getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET, false)
            mMainWidgets = getString(LOCKSCREEN_WIDGETS, "")!!
            mExtraWidgets = getString(LOCKSCREEN_WIDGETS_EXTRAS, "")!!
            mDeviceCustomColor =
                getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false)
            mDeviceLinearColor = getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR, Color.WHITE)
            mDeviceCircularColor =
                getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR, Color.WHITE)
            mDeviceTextColor = getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR, Color.WHITE)
            mDeviceName = getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, "")!!
            mWidgetsCustomColor = getBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR, false)
            mBigInactiveColor = getInt(LOCKSCREEN_WIDGETS_BIG_INACTIVE, Color.BLACK)
            mBigActiveColor = getInt(LOCKSCREEN_WIDGETS_BIG_ACTIVE, Color.WHITE)
            mSmallInactiveColor = getInt(LOCKSCREEN_WIDGETS_SMALL_INACTIVE, Color.BLACK)
            mSmallActiveColor = getInt(LOCKSCREEN_WIDGETS_SMALL_ACTIVE, Color.WHITE)
            mBigIconActiveColor = getInt(LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE, Color.BLACK)
            mBigIconInactiveColor = getInt(LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE, Color.WHITE)
            mSmallIconActiveColor = getInt(LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE, Color.BLACK)
            mSmallIconInactiveColor = getInt(LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE, Color.WHITE)
            mBottomMargin = getInt(LOCKSCREEN_WIDGETS_BOTTOM_MARGIN, 0)
        }

        if (key.isNotEmpty()) {
            if (key[0] == LOCKSCREEN_WIDGETS_ENABLED ||
                key[0] == LOCKSCREEN_WIDGETS_DEVICE_WIDGET ||
                key[0] == LOCKSCREEN_WIDGETS ||
                key[0] == LOCKSCREEN_WIDGETS_EXTRAS
            ) {
                updateLockscreenWidgets()
            }
            if (key[0] == LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH ||
                key[0] == LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR ||
                key[0] == LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR ||
                key[0] == LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR ||
                key[0] == LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE
            ) {
                updateLsDeviceWidget()
            }
            if (key[0] == LOCKSCREEN_WIDGETS_CUSTOM_COLOR ||
                key[0] == LOCKSCREEN_WIDGETS_BIG_ACTIVE ||
                key[0] == LOCKSCREEN_WIDGETS_BIG_INACTIVE ||
                key[0] == LOCKSCREEN_WIDGETS_SMALL_ACTIVE ||
                key[0] == LOCKSCREEN_WIDGETS_SMALL_INACTIVE ||
                key[0] == LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE ||
                key[0] == LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE ||
                key[0] == LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE ||
                key[0] == LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE
            ) {
                updateLockscreenWidgetsColors()
            }
            if (key[0] == LOCKSCREEN_WIDGETS_BOTTOM_MARGIN) {
                updateMargins()
            }
        }

    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {

        // Receiver to handle weather inflated
        if (!mBroadcastRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_WEATHER_INFLATED)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.registerReceiver(
                    mReceiver,
                    intentFilter,
                    Context.RECEIVER_EXPORTED
                )
            } else {
                mContext.registerReceiver(
                    mReceiver,
                    intentFilter
                )
            }

            mBroadcastRegistered = true
        }

        mWidgetsContainer = LinearLayout(mContext)
        mWidgetsContainer.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        try {
            LaunchableImageView = findClass(
                "com.android.systemui.animation.view.LaunchableImageView",
                loadPackageParam.classLoader
            )
        } catch (t: Throwable) {
            log(ControllersProvider.TAG + " Error finding LaunchableImageView: " + t.message)
        }

        try {
            LaunchableLinearLayout = findClass(
                "com.android.systemui.animation.view.LaunchableLinearLayout",
                loadPackageParam.classLoader
            )
        } catch (t: Throwable) {
            log(ControllersProvider.TAG + " Error finding LaunchableLinearLayout: " + t.message)
        }

        try {
            val keyguardQuickAffordanceInteractor = findClass(
                "com.android.systemui.keyguard.domain.interactor.KeyguardQuickAffordanceInteractor",
                loadPackageParam.classLoader
            )
            XposedBridge.hookAllConstructors(
                keyguardQuickAffordanceInteractor,
                object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        mActivityStarter = getObjectField(param.thisObject, "activityStarter")
                        setActivityStarter()
                    }
                })
        } catch (ignored: Throwable) {
        }

        val keyguardStatusViewClass = findClass(
            "com.android.keyguard.KeyguardStatusView",
            loadPackageParam.classLoader
        )

        hookAllMethods(keyguardStatusViewClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!mWidgetsEnabled) return

                mStatusViewContainer = getObjectField(
                    param.thisObject,
                    "mStatusViewContainer"
                ) as ViewGroup

                placeWidgets()

            }
        })

        val keyguardClockSwitch = findClass(
            "com.android.keyguard.KeyguardClockSwitch",
            loadPackageParam.classLoader
        )

        hookAllMethods(keyguardClockSwitch, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!mWidgetsEnabled) return

                mStatusArea = getObjectField(
                    param.thisObject,
                    "mStatusArea"
                ) as ViewGroup

                placeWidgets()

            }
        })

        hookAllMethods(keyguardClockSwitch, "updateClockViews", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!mWidgetsEnabled) return
                updateLockscreenWidgetsOnClock(param.args[0] as Boolean)
            }
        })

        val dozeScrimControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.DozeScrimController",
            loadPackageParam.classLoader
        )

        hookAllMethods(dozeScrimControllerClass, "onDozingChanged", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                updateDozingState(param.args[0] as Boolean)
            }
        })
    }

    private fun placeWidgets() {
        if (mStatusViewContainer == null || mStatusArea == null) return
        if (lsWeather && !lsWeatherInflated) return
        try {
            val lsWidgets = LockscreenWidgetsView.getInstance(mContext, mActivityStarter)
            (lsWidgets.parent as ViewGroup?)?.removeView(lsWidgets)
            (mWidgetsContainer.parent as ViewGroup?)?.removeView(mWidgetsContainer)

            mWidgetsContainer.addView(lsWidgets)

            if (customLockscreenClock) {
                mStatusViewContainer!!.addView(mWidgetsContainer)
            } else {
                // Put widgets view inside the status area
                // But before notifications
                mStatusArea!!.addView(mWidgetsContainer, mStatusArea!!.childCount - 1)
            }
            updateLockscreenWidgets()
            updateLsDeviceWidget()
            updateLockscreenWidgetsColors()
            updateMargins()
        } catch (ignored: Throwable) {
        }

    }

    private fun updateLockscreenWidgets() {
        log(TAG + "Updating Lockscreen Widgets")
        val lsWidgets = LockscreenWidgetsView.getInstance() ?: return
        lsWidgets.setOptions(mWidgetsEnabled, mDeviceWidgetEnabled, mMainWidgets, mExtraWidgets)
    }

    private fun updateLockscreenWidgetsOnClock(isLargeClock: Boolean) {
        log(TAG + "Updating Lockscreen Widgets on Clock")
        val lsWidgets = LockscreenWidgetsView.getInstance() ?: return
        lsWidgets.setIsLargeClock(if (customLockscreenClock) false else isLargeClock)
    }

    private fun updateLsDeviceWidget() {
        log(TAG + "Updating Lockscreen Device Widget")
        val lsWidgets = LockscreenWidgetsView.getInstance() ?: return
        lsWidgets.setDeviceWidgetOptions(
            mDeviceCustomColor,
            mDeviceLinearColor,
            mDeviceCircularColor,
            mDeviceTextColor,
            mDeviceName
        )
    }

    private fun updateLockscreenWidgetsColors() {
        log(TAG + "Updating Lockscreen Widgets Colors")
        val lsWidgets = LockscreenWidgetsView.getInstance() ?: return
        lsWidgets.setCustomColors(
            mWidgetsCustomColor,
            mBigInactiveColor, mBigActiveColor,
            mSmallInactiveColor, mSmallActiveColor,
            mBigIconInactiveColor, mBigIconActiveColor,
            mSmallIconInactiveColor, mSmallIconActiveColor
        )
    }

    private fun updateMargins() {
        setMargins(
            mWidgetsContainer,
            mContext,
            0,
            0,
            0,
            mBottomMargin
        )
    }

    private fun updateDozingState(isDozing: Boolean) {
        val lsWidgets = LockscreenWidgetsView.getInstance() ?: return
        lsWidgets.setDozingState(isDozing)
    }

    private fun setActivityStarter() {
        val lsWidgets = LockscreenWidgetsView.getInstance() ?: return
        lsWidgets.setActivityStarter(mActivityStarter)
    }

    companion object {
        private val TAG = "Iconify - ${LockscreenWidgets::class.java.simpleName}: "

        var LaunchableImageView: Class<*>? = null
        var LaunchableLinearLayout: Class<*>? = null

    }

}