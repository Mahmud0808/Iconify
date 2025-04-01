package com.drdisagree.iconify.xposed.modules.quicksettings.headerclock

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.XResources
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.UserHandle
import android.os.UserManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.text.TextUtilsCompat
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.ACTION_BOOT_COMPLETED
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_CENTERED
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_COLOR_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_EXPANSION_Y
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_SIDEMARGIN
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_STYLE
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_TOPMARGIN
import com.drdisagree.iconify.data.common.Preferences.HIDE_STATUS_ICONS_SWITCH
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_HEADER_CLOCK_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_QS_HEADER_CONTAINER_TAG
import com.drdisagree.iconify.data.common.Preferences.OP_QS_HEADER_SWITCH
import com.drdisagree.iconify.data.common.Preferences.QSPANEL_HIDE_CARRIER
import com.drdisagree.iconify.data.common.Resources.HEADER_CLOCK_LAYOUT
import com.drdisagree.iconify.data.common.XposedConst.HEADER_CLOCK_FONT_FILE
import com.drdisagree.iconify.utils.TextUtils
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.TouchAnimator
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findChildIndexContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.getColorResCompat
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.Locale

@SuppressLint("DiscouragedApi")
class HeaderClockA14(context: Context) : ModPack(context) {

    private var showHeaderClock = false
    private var centeredClockView = false
    private var mQsHeaderClockContainer: LinearLayout = LinearLayout(mContext)
    private var mQsHeaderContainerShade: LinearLayout = LinearLayout(mContext).apply {
        tag = ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG
    }
    private var mQsClockContainer: LinearLayout = LinearLayout(mContext)
    private var mQsIconsContainer: LinearLayout = LinearLayout(mContext)
    private var mQsPanelView: ViewGroup? = null
    private var mUserManager: UserManager? = null
    private var mActivityStarter: Any? = null
    private var mQQSContainerAnimator: TouchAnimator? = null
    private var mQQSExpansionY: Float = 8f
    private var hideQsCarrierGroup = false
    private var hideStatusIcons = false
    private var mBroadcastRegistered = false
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null) {
                if (intent.action == ACTION_BOOT_COMPLETED) {
                    updateClockView()
                }
            }
        }
    }
    private val mViewOnClickListener = View.OnClickListener { v: View ->
        val tag = v.tag.toString()
        if (tag == "clock") {
            onClockClick()
        } else if (tag == "date") {
            onDateClick()
        }
    }
    private var showOpQsHeaderView = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showHeaderClock = getBoolean(HEADER_CLOCK_SWITCH, false)
            centeredClockView = getBoolean(HEADER_CLOCK_CENTERED, false)
            mQQSExpansionY = getSliderInt(HEADER_CLOCK_EXPANSION_Y, 24).toFloat()
            hideQsCarrierGroup = getBoolean(QSPANEL_HIDE_CARRIER, false)
            hideStatusIcons = getBoolean(HIDE_STATUS_ICONS_SWITCH, false)
            showOpQsHeaderView = getBoolean(OP_QS_HEADER_SWITCH, false)
        }

        if (key.isNotEmpty()) {
            if (key[0] == HEADER_CLOCK_EXPANSION_Y) {
                buildHeaderViewExpansion()
            }

            if (key[0] == HEADER_CLOCK_SWITCH ||
                key[0] == HEADER_CLOCK_COLOR_SWITCH ||
                key[0] == HEADER_CLOCK_COLOR_CODE_ACCENT1 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_ACCENT2 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_ACCENT3 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_TEXT1 ||
                key[0] == HEADER_CLOCK_COLOR_CODE_TEXT2 ||
                key[0] == HEADER_CLOCK_FONT_SWITCH ||
                key[0] == HEADER_CLOCK_SIDEMARGIN ||
                key[0] == HEADER_CLOCK_TOPMARGIN ||
                key[0] == HEADER_CLOCK_STYLE ||
                key[0] == HEADER_CLOCK_CENTERED ||
                key[0] == HEADER_CLOCK_FONT_TEXT_SCALING ||
                key[0] == HEADER_CLOCK_LANDSCAPE_SWITCH
            ) {
                updateClockView()
            }
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (!mBroadcastRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_BOOT_COMPLETED)

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

        initResources(mContext)

        val qsPanelClass = findClass("$SYSTEMUI_PACKAGE.qs.QSPanel")
        val qsImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSImpl",
            "$SYSTEMUI_PACKAGE.qs.QSFragment"
        )
        val shadeHeaderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController"
        )
        val qsPanelControllerBaseClass = findClass("$SYSTEMUI_PACKAGE.qs.QSPanelControllerBase")
        val qsSecurityFooterUtilsClass = findClass("$SYSTEMUI_PACKAGE.qs.QSSecurityFooterUtils")
        val quickStatusBarHeaderClass = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")
        val dependencyClass = findClass("$SYSTEMUI_PACKAGE.Dependency")
        val activityStarterClass = findClass("$SYSTEMUI_PACKAGE.plugins.ActivityStarter")

        quickStatusBarHeaderClass
            .hookConstructor()
            .runAfter {
                if (mActivityStarter == null) {
                    mActivityStarter = dependencyClass.callStaticMethodSilently(
                        "get",
                        activityStarterClass
                    )
                }
            }

        qsSecurityFooterUtilsClass
            .hookConstructor()
            .runAfter { param ->
                if (mActivityStarter == null) {
                    mActivityStarter = param.thisObject.getField("mActivityStarter")
                }
            }

        quickStatusBarHeaderClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!showHeaderClock) return@runAfter

                val mQuickStatusBarHeader = param.thisObject as FrameLayout

                mQsHeaderClockContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        bottomMargin = mContext.toPx(16)
                    }
                    orientation = LinearLayout.HORIZONTAL
                }

                mQsHeaderContainerShade.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    orientation = LinearLayout.VERTICAL
                }

                mQsClockContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        1f
                    )
                    orientation = LinearLayout.VERTICAL
                }

                mQsIconsContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                    )
                    orientation = LinearLayout.VERTICAL
                    gravity = Gravity.END or Gravity.CENTER
                }

                mQsHeaderClockContainer.apply {
                    (parent as? ViewGroup)?.removeView(this)
                    removeAllViews()
                    addView(mQsClockContainer)
                    addView(mQsIconsContainer)
                }

                val headerImageIndex = mQuickStatusBarHeader.findChildIndexContainsTag(
                    ICONIFY_QS_HEADER_CONTAINER_TAG
                )
                mQuickStatusBarHeader.reAddView(
                    mQsHeaderClockContainer,
                    if (headerImageIndex == -1) headerImageIndex else headerImageIndex + 1
                )

                handleOldHeaderView(param)

                updateClockView()
            }

        quickStatusBarHeaderClass
            .hookMethod("updateResources")
            .runAfter { param ->
                if (!showHeaderClock) return@runAfter

                val mQuickStatusBarHeader = param.thisObject as FrameLayout

                buildHeaderViewExpansion()

                if (mContext.isLandscape) {
                    mQsHeaderContainerShade.reAddView(mQsHeaderClockContainer, 0)
                    mQsHeaderContainerShade.visibility = View.VISIBLE
                } else {
                    val headerImageIndex = mQuickStatusBarHeader.findChildIndexContainsTag(
                        ICONIFY_QS_HEADER_CONTAINER_TAG
                    )
                    mQuickStatusBarHeader.reAddView(
                        mQsHeaderClockContainer,
                        if (headerImageIndex == -1) 0 else headerImageIndex + 1
                    )
                    mQsHeaderContainerShade.visibility = View.GONE
                }

                updateClockView()
            }

        qsPanelControllerBaseClass
            .hookMethod("onInit")
            .runBefore { param ->
                mQsPanelView = param.thisObject.getField("mView") as ViewGroup
            }

        qsPanelControllerBaseClass
            .hookMethod("onInit")
            .runAfter { param ->
                if (!showHeaderClock) return@runAfter

                val qsPanel = param.thisObject.getField("mView") as LinearLayout
                val mHorizontalLinearLayout =
                    qsPanel.getFieldSilently("mHorizontalLinearLayout") as? LinearLayout

                if (mHorizontalLinearLayout == null) {
                    val dummyLayout = LinearLayout(mContext).apply {
                        layoutParams = LinearLayout.LayoutParams(0, 0)
                        orientation = LinearLayout.HORIZONTAL
                        visibility = View.GONE
                    }
                    qsPanel.setField("mHorizontalLinearLayout", dummyLayout)
                }
            }

        qsPanelClass
            .hookMethod("switchToParent")
            .parameters(
                View::class.java,
                ViewGroup::class.java,
                Int::class.java,
                String::class.java
            )
            .runBefore { param ->
                if (!showHeaderClock || mQsPanelView == null) return@runBefore

                val child = param.args[0] as View
                val parent = param.args[1] as? ViewGroup ?: return@runBefore
                val targetParentId = mContext.resources.getIdentifier(
                    "quick_settings_panel",
                    "id",
                    SYSTEMUI_PACKAGE
                )

                if (parent.id == targetParentId) {
                    parent.findViewWithTag<LinearLayout?>(ICONIFY_QS_HEADER_CONTAINER_SHADE_TAG)
                        ?.also { mQsHeaderContainerShade = it }

                    if (parent.indexOfChild(mQsHeaderContainerShade) == 0) {
                        val index = ((param.args[2] as Int) + 1).coerceAtMost(parent.childCount)
                        parent.reAddView(child, index)
                        param.result = null
                        return@runBefore
                    }

                    parent.reAddView(mQsHeaderContainerShade, 0)
                }

                parent.reAddView(child, (param.args[2] as Int) + 1)

                param.result = null
            }

        qsImplClass
            .hookMethod("setQsExpansion")
            .runAfter { param ->
                if (!showHeaderClock) return@runAfter

                val onKeyguard = param.thisObject.callMethod("isKeyguardState") as Boolean
                val mShowCollapsedOnKeyguard = param.thisObject.getField(
                    "mShowCollapsedOnKeyguard"
                ) as Boolean

                val onKeyguardAndExpanded = onKeyguard && !mShowCollapsedOnKeyguard
                val expansion = param.args[0] as Float

                setExpansion(onKeyguardAndExpanded, expansion)
            }

        ResourceHookManager
            .hookBoolean()
            .whenCondition { showHeaderClock }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("config_use_split_notification_shade") { mContext.isLandscape }
            .addResource("config_skinnyNotifsInLandscape") { false }
            .apply()

        ResourceHookManager
            .hookDimen()
            .whenCondition { showHeaderClock }
            .forPackageName(SYSTEMUI_PACKAGE)
            .addResource("qs_brightness_margin_top") { 0 }
            .apply()

        shadeHeaderControllerClass
            .hookMethod("onInit")
            .runAfter { param ->
                if (!showHeaderClock) return@runAfter

                val clock = param.thisObject.getField("clock") as TextView
                (clock.parent as? ViewGroup)?.removeView(clock)

                val date = param.thisObject.getField("date") as TextView
                (date.parent as? ViewGroup)?.removeView(date)

                mQsIconsContainer.removeAllViews()

                try {
                    val qsCarrierGroup = param.thisObject.getField("qsCarrierGroup") as LinearLayout
                    (qsCarrierGroup.parent as? ViewGroup)?.removeView(qsCarrierGroup)
                    if (hideQsCarrierGroup) qsCarrierGroup.visibility = View.GONE
                    mQsIconsContainer.addView(qsCarrierGroup)
                } catch (_: Throwable) {
                    val mShadeCarrierGroup =
                        param.thisObject.getField("mShadeCarrierGroup") as LinearLayout
                    (mShadeCarrierGroup.parent as? ViewGroup)?.removeView(mShadeCarrierGroup)
                    if (hideQsCarrierGroup) mShadeCarrierGroup.visibility = View.GONE
                    mQsIconsContainer.addView(mShadeCarrierGroup)
                }

                try {
                    val systemIconsHoverContainer = param.thisObject.getField(
                            "systemIconsHoverContainer"
                        ) as LinearLayout
                    (systemIconsHoverContainer.parent as? ViewGroup)?.removeView(
                        systemIconsHoverContainer
                    )
                    if (hideStatusIcons) systemIconsHoverContainer.visibility = View.GONE
                    mQsIconsContainer.addView(systemIconsHoverContainer)
                } catch (_: Throwable) {
                    val iconsContainer = LinearLayout(mContext).apply {
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            mContext.toPx(32)
                        )
                        orientation = LinearLayout.HORIZONTAL
                        gravity = Gravity.END or Gravity.CENTER
                    }

                    val statusIcons = param.thisObject.getField("iconContainer") as View
                    (statusIcons.parent as? ViewGroup)?.removeView(statusIcons)

                    val batteryIcon = param.thisObject.getField("batteryIcon") as View
                    (batteryIcon.parent as? ViewGroup)?.removeView(batteryIcon)

                    iconsContainer.apply {
                        addView(statusIcons)
                        addView(batteryIcon)
                        if (hideStatusIcons) visibility = View.GONE
                        mQsIconsContainer.addView(this)
                    }
                }
            }

        handleLegacyHeaderView()

        BootCallback.registerBootListener(
            object : BootCallback.BootListener {
                override fun onDeviceBooted() {
                    updateClockView()
                }
            }
        )
    }

    private fun buildHeaderViewExpansion() {
        val mQQSExpansionY = if (mContext.isLandscape) 0 else mQQSExpansionY

        val builderP: TouchAnimator.Builder = TouchAnimator.Builder()
            .addFloat(
                mQsHeaderClockContainer,
                "translationY",
                0F,
                mContext.toPx(mQQSExpansionY.toInt()).toFloat()
            )

        mQQSContainerAnimator = builderP.build()
    }

    private fun setExpansion(forceExpanded: Boolean, expansionFraction: Float) {
        val keyguardExpansionFraction = if (forceExpanded) 1f else expansionFraction
        mQQSContainerAnimator?.setPosition(keyguardExpansionFraction)

        mQsHeaderClockContainer.alpha = if (forceExpanded) expansionFraction else 1f
    }

    private fun initResources(context: Context) {
        Handler(Looper.getMainLooper()).post {
            mUserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        }
    }

    private fun updateClockView() {
        if (!showHeaderClock) return

        val isClockAdded =
            mQsClockContainer.findViewWithTag<View?>(ICONIFY_HEADER_CLOCK_TAG) != null

        if (isClockAdded) {
            mQsClockContainer.removeView(
                mQsClockContainer.findViewWithTag(
                    ICONIFY_HEADER_CLOCK_TAG
                )
            )
        }

        clockView?.let {
            if (centeredClockView) {
                mQsClockContainer.gravity = Gravity.CENTER
            } else {
                mQsClockContainer.gravity = Gravity.START
            }

            it.tag = ICONIFY_HEADER_CLOCK_TAG

            TextUtils.convertTextViewsToTitleCase(it)

            mQsClockContainer.addView(it)

            it.layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT

            modifyClockView(it)
            setOnClickListener(it)
        }
    }

    private val clockView: View?
        get() {
            if (!XprefsIsInitialized) return null

            val inflater = LayoutInflater.from(appContext)
            val clockStyle: Int = Xprefs.getInt(HEADER_CLOCK_STYLE, 0)

            return inflater.inflate(
                appContext.resources.getIdentifier(
                    HEADER_CLOCK_LAYOUT + clockStyle,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ),
                null
            )
        }

    private fun modifyClockView(clockView: View) {
        val clockStyle: Int = Xprefs.getInt(HEADER_CLOCK_STYLE, 0)
        val customFontEnabled: Boolean = Xprefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false)
        val clockScale: Float =
            (Xprefs.getSliderInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10) / 10.0).toFloat()
        val sideMargin: Int = Xprefs.getSliderInt(HEADER_CLOCK_SIDEMARGIN, 0)
        val topMargin: Int =
            if (mContext.isLandscape) 0 else Xprefs.getSliderInt(HEADER_CLOCK_TOPMARGIN, 8)

        val customColorEnabled = Xprefs.getBoolean(HEADER_CLOCK_COLOR_SWITCH, false)
        var accent1: Int = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent1_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        var accent2: Int = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent2_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        var accent3: Int = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent3_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        var textPrimary: Int = getColorResCompat(mContext, android.R.attr.textColorPrimary)
        var textInverse: Int = getColorResCompat(mContext, android.R.attr.textColorPrimaryInverse)

        if (customColorEnabled) {
            accent1 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT1,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent1_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            accent2 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT2,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent2_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            accent3 = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_ACCENT3,
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent3_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            textPrimary = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT1,
                getColorResCompat(mContext, android.R.attr.textColorPrimary)
            )
            textInverse = Xprefs.getInt(
                HEADER_CLOCK_COLOR_CODE_TEXT2,
                getColorResCompat(mContext, android.R.attr.textColorPrimaryInverse)
            )
        }

        var typeface: Typeface? = if (customFontEnabled && HEADER_CLOCK_FONT_FILE.exists()) {
            Typeface.createFromFile(HEADER_CLOCK_FONT_FILE)
        } else {
            null
        }

        setMargins(mQsHeaderClockContainer, mContext, 0, topMargin, 0, 0)

        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
            setMargins(clockView, mContext, 0, 0, sideMargin, 0)
        } else {
            setMargins(clockView, mContext, sideMargin, 0, 0, 0)
        }

        findViewWithTagAndChangeColor(clockView, "accent1", accent1)
        findViewWithTagAndChangeColor(clockView, "accent2", accent2)
        findViewWithTagAndChangeColor(clockView, "accent3", accent3)
        findViewWithTagAndChangeColor(clockView, "text1", textPrimary)
        findViewWithTagAndChangeColor(clockView, "text2", textInverse)
        findViewWithTagAndChangeColor(clockView, "gradient", accent1, accent2, 26)

        if (typeface != null) {
            applyFontRecursively(clockView, typeface)
        }

        if (clockScale != 1f) {
            applyTextScalingRecursively(clockView, clockScale)
        }

        when (clockStyle) {
            6 -> {
                val imageView =
                    clockView.findViewContainsTag("profile_picture") as ImageView?
                userImage?.let { imageView?.setImageDrawable(it) }
            }
        }
    }

    private val userImage: Drawable?
        get() = if (mUserManager == null) {
            ResourcesCompat.getDrawable(
                appContext.resources,
                R.drawable.default_avatar,
                appContext.theme
            )
        } else try {
            val getUserIconMethod =
                mUserManager!!.javaClass.getMethod(
                    "getUserIcon",
                    Int::class.javaPrimitiveType
                )
            val userId =
                UserHandle::class.java.getDeclaredMethod("myUserId").invoke(null) as Int
            val bitmapUserIcon = getUserIconMethod.invoke(mUserManager, userId) as Bitmap

            BitmapDrawable(mContext.resources, bitmapUserIcon)
        } catch (throwable: Throwable) {
            if (throwable !is NullPointerException) {
                log(this@HeaderClockA14, throwable)
            }

            ResourcesCompat.getDrawable(
                appContext.resources,
                R.drawable.default_avatar,
                appContext.theme
            )
        }

    private fun setOnClickListener(view: View?) {
        if (view == null) return

        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                val child: View = view.getChildAt(i)
                val tag = if (child.tag == null) "" else child.tag.toString()

                if (tag.lowercase(Locale.getDefault()).contains("clock") ||
                    tag.lowercase(Locale.getDefault()).contains("date")
                ) {
                    child.setOnClickListener(mViewOnClickListener)
                }

                (child as? ViewGroup)?.let { setOnClickListener(it) }
            }
        } else {
            val tag = if (view.tag == null) "" else view.tag.toString()

            if (tag.lowercase(Locale.getDefault()).contains("clock") ||
                tag.lowercase(Locale.getDefault()).contains("date")
            ) {
                view.setOnClickListener(mViewOnClickListener)
            }
        }
    }

    private fun onClockClick() {
        if (mActivityStarter == null) {
            log(this@HeaderClockA14, "mActivityStarter is null")
            return
        }

        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)

        mActivityStarter.callMethod("postStartActivityDismissingKeyguard", intent, 0)
    }

    private fun onDateClick() {
        if (mActivityStarter == null) {
            log(this@HeaderClockA14, "mActivityStarter is null")
            return
        }

        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        builder.appendPath(System.currentTimeMillis().toString())

        val intent = Intent(Intent.ACTION_VIEW, builder.build())

        mActivityStarter.callMethod("postStartActivityDismissingKeyguard", intent, 0)
    }

    private fun handleOldHeaderView(param: XC_MethodHook.MethodHookParam) {
        if (!showHeaderClock) return

        try {
            val mDateView = param.thisObject.getField("mDateView") as View
            mDateView.layoutParams.height = 0
            mDateView.layoutParams.width = 0
            mDateView.visibility = View.INVISIBLE
        } catch (_: Throwable) {
        }

        try {
            val mClockView = param.thisObject.getField("mClockView") as TextView
            mClockView.visibility = View.INVISIBLE
            mClockView.setTextAppearance(0)
            mClockView.setTextColor(0)
        } catch (_: Throwable) {
        }

        try {
            val mClockDateView = param.thisObject.getField("mClockDateView") as TextView
            mClockDateView.visibility = View.INVISIBLE
            mClockDateView.setTextAppearance(0)
            mClockDateView.setTextColor(0)
        } catch (_: Throwable) {
        }

        try {
            val mQSCarriers = param.thisObject.getField("mQSCarriers") as View
            mQSCarriers.visibility = View.INVISIBLE
        } catch (_: Throwable) {
        }
    }

    private fun handleLegacyHeaderView() {
        if (!showHeaderClock) return

        val xResources: XResources = resParams[SYSTEMUI_PACKAGE]?.res ?: return

        xResources
            .hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "quick_qs_status_icons")
            .suppressError()
            .run { liparam ->
                if (!showHeaderClock) return@run

                // Ricedroid date
                try {
                    val date =
                        liparam.view.findViewById<TextView>(
                            liparam.res.getIdentifier(
                                "date",
                                "id",
                                mContext.packageName
                            )
                        )
                    date.layoutParams.height = 0
                    date.layoutParams.width = 0
                    date.setTextAppearance(0)
                    date.setTextColor(0)
                    date.visibility = View.GONE
                } catch (_: Throwable) {
                }

                // Nusantara clock
                try {
                    val jrClock =
                        liparam.view.findViewById<TextView>(
                            liparam.res.getIdentifier(
                                "jr_clock",
                                "id",
                                mContext.packageName
                            )
                        )
                    jrClock.layoutParams.height = 0
                    jrClock.layoutParams.width = 0
                    jrClock.setTextAppearance(0)
                    jrClock.setTextColor(0)
                    jrClock.visibility = View.GONE
                } catch (_: Throwable) {
                }

                // Nusantara date
                try {
                    val jrDateContainer =
                        liparam.view.findViewById<LinearLayout>(
                            liparam.res.getIdentifier(
                                "jr_date_container",
                                "id",
                                mContext.packageName
                            )
                        )
                    val jrDate = jrDateContainer.getChildAt(0) as TextView
                    jrDate.layoutParams.height = 0
                    jrDate.layoutParams.width = 0
                    jrDate.setTextAppearance(0)
                    jrDate.setTextColor(0)
                    jrDate.visibility = View.GONE
                } catch (_: Throwable) {
                }
            }

        xResources
            .hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "quick_status_bar_header_date_privacy")
            .suppressError()
            .run { liparam ->
                liparam.view.findViewById<View>(
                    liparam.res.getIdentifier(
                        "lock_icon_view",
                        "id",
                        mContext.packageName
                    )
                ).apply {
                    if (!showHeaderClock) return@apply

                    try {
                        val date =
                            liparam.view.findViewById<TextView>(
                                liparam.res.getIdentifier(
                                    "date",
                                    "id",
                                    mContext.packageName
                                )
                            )
                        date.layoutParams.height = 0
                        date.layoutParams.width = 0
                        date.setTextAppearance(0)
                        date.setTextColor(0)
                        date.visibility = View.GONE
                    } catch (_: Throwable) {
                    }
                }
            }
    }
}