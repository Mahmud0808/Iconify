package com.drdisagree.iconify.xposed.modules.lockscreen

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.View.OnAttachStateChangeListener
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.graphics.toColorInt
import androidx.core.view.children
import com.drdisagree.iconify.data.common.Const.ACTION_LS_CLOCK_INFLATED
import com.drdisagree.iconify.data.common.Const.ACTION_WEATHER_INFLATED
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_LOCKSCREEN_WEATHER_TAG
import com.drdisagree.iconify.data.common.XposedConst.LOCKSCREEN_WEATHER_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.DozeCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.applyTo
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clear
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.clone
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.connect
import com.drdisagree.iconify.xposed.modules.extras.utils.MyConstraintSet.Companion.constraintSetInstance
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.assignIdsToViews
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getLsItemsContainer
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.removeViewFromParent
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.AodBurnInProtection
import com.drdisagree.iconify.xposed.modules.extras.views.CurrentWeatherView
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.callbacks.XC_LoadPackage

class LockscreenWeather(context: Context) : ModPack(context) {

    private var mWeatherEnabled = false
    private var weatherShowLocation = true
    private var weatherShowCondition = true
    private var weatherShowHumidity = false
    private var weatherShowWind = false
    private var weatherCustomColor = false
    private var weatherColor = Color.WHITE
    private var weatherTextSize: Int = 16
    private var weatherImageSize: Int = 18
    private var mSideMargin: Int = 0
    private var mTopMargin: Int = 0
    private var mBottomMargin: Int = 0
    private var mWeatherBackground = 0
    private var mCenterWeather = false
    private var mLockscreenRootView: ViewGroup? = null
    private var mLsItemsContainer: LinearLayout? = null
    private var mLockscreenClockEnabled = false
    private var mLockscreenClockInflated = false
    private var mWidgetsEnabled = false
    private var dateSmartSpaceViewAvailable = false
    private lateinit var mWeatherContainer: LinearLayout
    private var aodBurnInProtection: AodBurnInProtection? = null
    private var mCustomFontEnabled = false

    private var mBroadcastRegistered = false
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null) {
                if (intent.action == ACTION_LS_CLOCK_INFLATED && mWeatherEnabled) {
                    mLockscreenClockInflated = true
                    placeWeatherView()
                }
            }
        }
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            mWeatherEnabled = getBoolean(XposedKey.LOCKSCREEN_WEATHER)
            weatherShowLocation = getBoolean(XposedKey.WEATHER_SHOW_LOCATION)
            weatherShowCondition = getBoolean(XposedKey.WEATHER_SHOW_CONDITION)
            weatherShowHumidity = getBoolean(XposedKey.WEATHER_SHOW_HUMIDITY)
            weatherShowWind = getBoolean(XposedKey.WEATHER_SHOW_WIND)
            weatherCustomColor = getBoolean(XposedKey.WEATHER_TEXT_COLOR)
            weatherColor = getString(XposedKey.WEATHER_TEXT_COLOR_CODE).toColorInt()
            weatherTextSize = getInt(XposedKey.WEATHER_TEXT_SIZE)
            weatherImageSize = getInt(XposedKey.WEATHER_ICON_SIZE)
            mSideMargin = getInt(XposedKey.WEATHER_CUSTOM_MARGINS_SIDE)
            mTopMargin = getInt(XposedKey.WEATHER_CUSTOM_MARGINS_TOP)
            mBottomMargin = getInt(XposedKey.WEATHER_CUSTOM_MARGINS_BOTTOM)
            mWeatherBackground = getString(XposedKey.WEATHER_STYLE).toInt()
            mCenterWeather = getBoolean(XposedKey.WEATHER_CENTER_VIEW)
            mLockscreenClockEnabled = getBoolean(XposedKey.CUSTOM_LOCKSCREEN_CLOCK)
            mWidgetsEnabled = getBoolean(XposedKey.LOCKSCREEN_WIDGETS)
            mCustomFontEnabled = getString(XposedKey.WEATHER_CUSTOM_FONT_FILE_URI).isNotEmpty()
        }

        when (key.firstOrNull()) {
            in setOf(
                XposedKey.WEATHER_SHOW_LOCATION.name,
                XposedKey.WEATHER_SHOW_CONDITION.name,
                XposedKey.WEATHER_SHOW_HUMIDITY.name,
                XposedKey.WEATHER_SHOW_WIND.name,
                XposedKey.WEATHER_TEXT_COLOR.name,
                XposedKey.WEATHER_TEXT_COLOR_CODE.name,
                XposedKey.WEATHER_TEXT_SIZE.name,
                XposedKey.WEATHER_ICON_SIZE.name,
                XposedKey.WEATHER_STYLE.name,
                XposedKey.WEATHER_CUSTOM_MARGINS_BOTTOM.name,
                XposedKey.WEATHER_CUSTOM_MARGINS_SIDE.name,
                XposedKey.WEATHER_CUSTOM_MARGINS_TOP.name,
                XposedKey.WEATHER_CENTER_VIEW.name,
                XposedKey.WEATHER_CUSTOM_FONT_FILE_URI.name
            ) -> {
                if (::mWeatherContainer.isInitialized) {
                    applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)
                    updateWeatherView()
                }
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "DiscouragedApi")
    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        // Receiver to handle lockscreen clock inflated
        if (!mBroadcastRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_LS_CLOCK_INFLATED)

            mContext.registerReceiver(
                mReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )

            mBroadcastRegistered = true
        }

        mWeatherContainer = LinearLayout(mContext).apply {
            id = View.generateViewId()
            tag = ICONIFY_LOCKSCREEN_WEATHER_TAG
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        val aodBurnInSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.AodBurnInSection")

        fun viewAttached(entryV: View) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!mWeatherEnabled) return@postDelayed

                val rootView = (entryV.parent as? ViewGroup)
                    ?.rootView
                    ?.findViewById<ViewGroup>(
                        mContext.resources.getIdentifier(
                            "keyguard_root_view",
                            "id",
                            mContext.packageName
                        )
                    ) ?: return@postDelayed

                dateSmartSpaceViewAvailable = rootView.findViewById<View?>(
                    mContext.resources.getIdentifier(
                        "date_smartspace_view",
                        "id",
                        mContext.packageName
                    )
                ) != null

                mLockscreenRootView = rootView

                mWeatherContainer.removeViewFromParent()

                if (mLockscreenClockEnabled || mWidgetsEnabled) {
                    mLsItemsContainer = rootView.getLsItemsContainer()

                    // Add weather view after clock view if exists
                    mLsItemsContainer!!.addView(
                        mWeatherContainer,
                        if (mLsItemsContainer!!.findViewWithTag<View?>(
                                ICONIFY_LOCKSCREEN_CLOCK_TAG
                            ) != null
                        ) 1 else 0
                    )
                } else {
                    mLockscreenRootView!!.addView(mWeatherContainer)
                }

                applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)
                aodBurnInProtection = AodBurnInProtection.registerForView(
                    mLsItemsContainer ?: mWeatherContainer
                )

                placeWeatherView()
            }, 1000)
        }

        aodBurnInSectionClass
            .hookMethod("addViews")
            .runAfter { param ->
                if (!mWeatherEnabled) return@runAfter

                val entryV = param.args[0] as View

                entryV.addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        viewAttached(entryV)
                    }

                    override fun onViewDetachedFromWindow(v: View) {}
                })

                if (entryV.isAttachedToWindow) {
                    viewAttached(entryV)
                }
            }

        val defaultNotificationStackScrollLayoutSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.DefaultNotificationStackScrollLayoutSection")

        val notificationContainerId = mContext.resources.getIdentifier(
            "nssl_placeholder",
            "id",
            mContext.packageName
        )

        defaultNotificationStackScrollLayoutSectionClass
            .hookMethod("applyConstraints")
            .runAfter { param ->
                if (!mWeatherEnabled) return@runAfter

                val constraintSet = param.args[0]

                constraintSet.clear(
                    notificationContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    notificationContainerId,
                    ConstraintSet.TOP,
                    (mLsItemsContainer ?: mWeatherContainer).id,
                    ConstraintSet.BOTTOM
                )
            }

        val smartspaceSectionClass =
            findClass("$SYSTEMUI_PACKAGE.keyguard.ui.view.layout.sections.SmartspaceSection")

        val bcSmartSpaceViewId = mContext.resources.getIdentifier(
            "bc_smartspace_view",
            "id",
            mContext.packageName
        )
        val dateSmartSpaceViewId = mContext.resources.getIdentifier(
            "date_smartspace_view",
            "id",
            mContext.packageName
        )

        smartspaceSectionClass
            .hookMethod("applyConstraints")
            .runAfter { param ->
                if (!mWeatherEnabled) return@runAfter

                val constraintSet = param.args[0]

                val smartSpaceViewId = if (dateSmartSpaceViewAvailable) {
                    dateSmartSpaceViewId
                } else {
                    // Some ROMs don't have date smartspace view
                    bcSmartSpaceViewId
                }

                // Connect weather view to bottom of date smartspace
                if (!mLockscreenClockEnabled && mWidgetsEnabled && mLsItemsContainer != null) {
                    constraintSet.clear(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP,
                        smartSpaceViewId,
                        ConstraintSet.BOTTOM
                    )
                } else if (mLockscreenClockEnabled && mLsItemsContainer != null) {
                    constraintSet.clear(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        mLsItemsContainer!!.id,
                        ConstraintSet.TOP,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.TOP
                    )
                } else if (!mLockscreenClockEnabled && !mWidgetsEnabled) {
                    constraintSet.clear(
                        mWeatherContainer.id,
                        ConstraintSet.TOP
                    )
                    constraintSet.connect(
                        mWeatherContainer.id,
                        ConstraintSet.TOP,
                        smartSpaceViewId,
                        ConstraintSet.BOTTOM
                    )
                }
            }

        // For unknown reason, rotating device makes the height of view to 0
        // This is a workaround to make sure the view is visible
        DozeCallback.getInstance().registerDozeChangeListener(
            object : DozeCallback.DozeListener {
                fun updateLayoutParams() {
                    if (!mWeatherEnabled || !::mWeatherContainer.isInitialized) return

                    if (mLsItemsContainer?.width == 0 || mLsItemsContainer?.height == 0) {
                        mLsItemsContainer?.layoutParams?.apply {
                            width = ViewGroup.LayoutParams.MATCH_PARENT
                            height = ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    }
                    if (mWeatherContainer.width == 0 || mWeatherContainer.height == 0) {
                        mWeatherContainer.layoutParams.apply {
                            width = ViewGroup.LayoutParams.MATCH_PARENT
                            height = ViewGroup.LayoutParams.WRAP_CONTENT
                        }
                    }
                }

                override fun onDozingStarted() {
                    aodBurnInProtection?.setMovementEnabled(true)
                    updateLayoutParams()
                }

                override fun onDozingStopped() {
                    aodBurnInProtection?.setMovementEnabled(false)
                    updateLayoutParams()
                }
            }
        )

        BootCallback.registerBootListener { updateWeatherView() }
    }

    @SuppressLint("DiscouragedApi")
    private fun placeWeatherView() {
        if (!mWeatherEnabled || mLockscreenRootView == null) return
        if (mLockscreenClockEnabled && !mLockscreenClockInflated) return

        val currentWeatherView: CurrentWeatherView = CurrentWeatherView.getInstance(
            mContext,
            LOCKSCREEN_WEATHER
        )

        if (currentWeatherView.parent != mWeatherContainer) {
            mWeatherContainer.reAddView(currentWeatherView)

            refreshWeatherView(currentWeatherView)
            applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)

            // Weather placed, now inflate widgets
            val broadcast = Intent(ACTION_WEATHER_INFLATED)
            broadcast.flags = Intent.FLAG_RECEIVER_FOREGROUND
            Thread { mContext.sendBroadcast(broadcast) }.start()
        }
    }

    @SuppressLint("DiscouragedApi")
    private fun applyLayoutConstraints(weatherView: ViewGroup) {
        if (mLockscreenRootView == null) return

        mLockscreenRootView.assignIdsToViews()

        mLsItemsContainer?.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

        val notificationContainerId = mContext.resources.getIdentifier(
            "nssl_placeholder",
            "id",
            mContext.packageName
        )
        val aodNotificationIconContainerId = mContext.resources.getIdentifier(
            "aod_notification_icon_container",
            "id",
            mContext.packageName
        )

        constraintSetInstance?.also { constraintSet ->
            constraintSet.clone(mLockscreenRootView!!)

            // Connect weather view to parent
            constraintSet.connect(
                weatherView.id,
                ConstraintSet.START,
                ConstraintSet.PARENT_ID,
                ConstraintSet.START
            )
            constraintSet.connect(
                weatherView.id,
                ConstraintSet.END,
                ConstraintSet.PARENT_ID,
                ConstraintSet.END
            )

            if ((weatherView == mWeatherContainer && !mLockscreenClockEnabled && !mWidgetsEnabled) ||
                (weatherView == mLsItemsContainer && !mLockscreenClockEnabled && mWidgetsEnabled)
            ) {
                val smartspaceViewId = if (dateSmartSpaceViewAvailable) {
                    mContext.resources.getIdentifier(
                        "date_smartspace_view",
                        "id",
                        mContext.packageName
                    )
                } else {
                    // Some ROMs don't have date smartspace view
                    mContext.resources.getIdentifier(
                        "bc_smartspace_view",
                        "id",
                        mContext.packageName
                    )
                }
                // If no custom clock or widgets enabled, or only widgets enabled
                // then connect weather view to bottom of date smartspace
                constraintSet.connect(
                    weatherView.id,
                    ConstraintSet.TOP,
                    smartspaceViewId,
                    ConstraintSet.BOTTOM
                )
            } else if (weatherView == mLsItemsContainer && mLockscreenClockEnabled) {
                // If custom clock enabled, then connect whole container to top of parent
                constraintSet.connect(
                    weatherView.id,
                    ConstraintSet.TOP,
                    ConstraintSet.PARENT_ID,
                    ConstraintSet.TOP
                )
            }

            // Connect notification container below weather
            if (notificationContainerId != 0) {
                constraintSet.clear(
                    notificationContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    notificationContainerId,
                    ConstraintSet.TOP,
                    weatherView.id,
                    ConstraintSet.BOTTOM
                )
            }

            // Connect aod notification icon container below weather
            if (aodNotificationIconContainerId != 0) {
                constraintSet.clear(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP
                )
                constraintSet.connect(
                    aodNotificationIconContainerId,
                    ConstraintSet.TOP,
                    weatherView.id,
                    ConstraintSet.BOTTOM
                )
            }

            constraintSet.applyTo(mLockscreenRootView!!)
        }
    }

    private fun refreshWeatherView(currentWeatherView: CurrentWeatherView?) {
        if (currentWeatherView == null) return

        applyLayoutConstraints(mLsItemsContainer ?: mWeatherContainer)

        currentWeatherView.apply {
            updateSizes(
                weatherTextSize,
                weatherImageSize,
                getName()
            )
            updateColors(
                if (weatherCustomColor) weatherColor else Color.WHITE,
                getName()
            )
            updateWeatherSettings(
                weatherShowLocation,
                weatherShowCondition,
                weatherShowHumidity,
                weatherShowWind,
                getName()
            )
            visibility = if (mWeatherEnabled) View.VISIBLE else View.GONE
            updateWeatherBg(
                mWeatherBackground,
                getName()
            )
        }

        updateMargins()
        updateFont()
    }

    private fun updateMargins() {
        val childView = mWeatherContainer.getChildAt(0) as LinearLayout

        mWeatherContainer.layoutParams.width = if (mLsItemsContainer == null) {
            0
        } else {
            LinearLayout.LayoutParams.MATCH_PARENT
        }

        setMargins(
            childView,
            mContext,
            mSideMargin,
            mTopMargin,
            mSideMargin,
            mBottomMargin
        )

        mWeatherContainer.gravity = if (mCenterWeather) Gravity.CENTER_HORIZONTAL else Gravity.START
        childView.gravity = if (mCenterWeather) Gravity.CENTER_HORIZONTAL else Gravity.START
        (mWeatherContainer.getChildAt(0) as LinearLayout?)?.children?.forEach {
            (it as LinearLayout).gravity = if (mCenterWeather) {
                Gravity.CENTER_HORIZONTAL
            } else {
                Gravity.START or Gravity.CENTER_VERTICAL
            }
        }
    }

    private fun updateFont() {
        if (mCustomFontEnabled && LOCKSCREEN_WEATHER_FONT_FILE.exists()) {
            Typeface.createFromFile(LOCKSCREEN_WEATHER_FONT_FILE)
        } else {
            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }.also { typeface ->
            applyFontRecursively(mWeatherContainer, typeface)
        }
    }

    private fun updateWeatherView() {
        refreshWeatherView(CurrentWeatherView.getInstance(LOCKSCREEN_WEATHER))
    }

    companion object {
        const val LOCKSCREEN_WEATHER = "iconify_ls_weather"
    }
}
