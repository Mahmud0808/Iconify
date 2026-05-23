package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
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
import androidx.core.graphics.drawable.toDrawable
import androidx.core.text.TextUtilsCompat
import androidx.core.view.children
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.core.utils.TextUtils
import com.drdisagree.iconify.data.common.Const.ACTION_BOOT_COMPLETED
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_HEADER_CLOCK_TAG
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_QS_HEADER_IMAGE_CONTAINER_TAG
import com.drdisagree.iconify.data.common.Resources.HEADER_CLOCK_LAYOUT
import com.drdisagree.iconify.data.common.XposedConst.HEADER_CLOCK_FONT_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.TouchAnimator
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.findChildIndexContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.findViewContainingTag
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.hideView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.UnhookHandle
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setExtraField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.modules.quicksettings.HeaderImage.Companion.ANIM_END_FRACTION
import com.drdisagree.iconify.xposed.modules.quicksettings.HeaderImage.Companion.ANIM_START_FRACTION
import com.drdisagree.iconify.xposed.modules.quicksettings.HeaderImage.Companion.STATE_CLOSED
import com.drdisagree.iconify.xposed.modules.quicksettings.HeaderImage.Companion.STATE_OPEN
import com.drdisagree.iconify.xposed.modules.quicksettings.HeaderImage.Companion.STATE_OPENING
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

@SuppressLint("DiscouragedApi")
class HeaderClock(context: Context) : ModPack(context) {

    private var showHeaderClock = false
    private var clockStyle = 0
    private var customColorEnabled = false
    private var mAccentColor1 = 0
    private var mAccentColor2 = 0
    private var mAccentColor3 = 0
    private var mTextColor1 = Color.WHITE
    private var mTextColor2 = Color.BLACK
    private var customFontEnabled = false
    private var sideMargin = 0
    private var topMargin = 8
    private var centeredClockView = false
    private var textScaling = 1f
    private var mQQSExpansionY: Float = 8f
    private var landscapeOffsetY: Float = 8f
    private var halfWidthLandscapeHeaderImage = true
    private var hideQsCarrierGroup = false
    private var hideStatusIcons = false
    private var mQsHeaderClockContainer: LinearLayout = LinearLayout(mContext)
    private var mQsClockContainer: LinearLayout = LinearLayout(mContext)
    private var mQsIconsContainer: LinearLayout = LinearLayout(mContext)
    private var mUserManager: UserManager? = null
    private var mActivityStarter: Any? = null
    private var mQQSContainerAnimator: TouchAnimator? = null
    private var systemBarUtilsClass: Class<*>? = null
    private var notificationPanelViewControllerInstance: Any? = null
    private var shadeHeaderControllerInstance: Any? = null
    private var qsOpeningJob: Job? = null
    private var lastShadeExpandedFraction = -1f
    private var lastQsExpandedFraction = -1f
    private val mViewOnClickListener = View.OnClickListener { v: View ->
        val tag = v.tag.toString()
        if (tag == "clock") {
            onClockClick()
        } else if (tag == "date") {
            onDateClick()
        }
    }
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

    private val clockView: View?
        get() {
            if (!XprefsIsInitialized) return null

            return LayoutInflater.from(appContext).inflate(
                appContext.resources.getIdentifier(
                    HEADER_CLOCK_LAYOUT + clockStyle,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ),
                null
            )
        }

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showHeaderClock = getBoolean(XposedKey.CUSTOM_HEADER_CLOCK)
            clockStyle = getInt(
                XposedKey.HEADER_CLOCK_STYLE.name,
                XposedKey.HEADER_CLOCK_STYLE.default as Int
            )
            customColorEnabled = getBoolean(XposedKey.HEADER_CLOCK_CUSTOM_COLOR)
            mAccentColor1 = getColor(XposedKey.HEADER_CLOCK_COLOR_ACCENT_PRIMARY)
            mAccentColor2 = getColor(XposedKey.HEADER_CLOCK_COLOR_ACCENT_SECONDARY)
            mAccentColor3 = getColor(XposedKey.HEADER_CLOCK_COLOR_ACCENT_TERTIARY)
            mTextColor1 = getColor(XposedKey.HEADER_CLOCK_COLOR_TEXT_PRIMARY)
            mTextColor2 = getColor(XposedKey.HEADER_CLOCK_COLOR_TEXT_INVERSE)
            customFontEnabled = getString(XposedKey.HEADER_CLOCK_FONT_FILE_URI).isNotEmpty()
            sideMargin = getInt(XposedKey.HEADER_CLOCK_SIDE_MARGIN)
            topMargin = getInt(XposedKey.HEADER_CLOCK_TOP_MARGIN)
            centeredClockView = getBoolean(XposedKey.HEADER_CLOCK_CENTER_VIEW)
            textScaling = getFloat(XposedKey.HEADER_CLOCK_TEXT_SCALE)
            mQQSExpansionY = getFloat(XposedKey.HEADER_CLOCK_EXPANSION_Y)
            landscapeOffsetY = getFloat(XposedKey.HEADER_CLOCK_LANDSCAPE_OFFSET_Y)
            halfWidthLandscapeHeaderImage =
                getBoolean(XposedKey.HEADER_CLOCK_HALF_WIDTH_IN_LANDSCAPE)
            hideQsCarrierGroup = getBoolean(XposedKey.QS_PANEL_HIDE_CARRIER)
            hideStatusIcons = getBoolean(XposedKey.HIDE_STATUS_ICONS)
        }

        when (key.firstOrNull()) {
            XposedKey.CUSTOM_HEADER_CLOCK.name,
            XposedKey.HEADER_CLOCK_STYLE.name,
            XposedKey.HEADER_CLOCK_CUSTOM_COLOR.name,
            XposedKey.HEADER_CLOCK_COLOR_ACCENT_PRIMARY.name,
            XposedKey.HEADER_CLOCK_COLOR_ACCENT_SECONDARY.name,
            XposedKey.HEADER_CLOCK_COLOR_ACCENT_TERTIARY.name,
            XposedKey.HEADER_CLOCK_COLOR_TEXT_PRIMARY.name,
            XposedKey.HEADER_CLOCK_COLOR_TEXT_INVERSE.name,
            XposedKey.HEADER_CLOCK_FONT_FILE_URI.name,
            XposedKey.HEADER_CLOCK_SIDE_MARGIN.name,
            XposedKey.HEADER_CLOCK_TOP_MARGIN.name,
            XposedKey.HEADER_CLOCK_CENTER_VIEW.name,
            XposedKey.HEADER_CLOCK_TEXT_SCALE.name,
            XposedKey.HEADER_CLOCK_LANDSCAPE_OFFSET_Y.name,
            XposedKey.QS_PANEL_HIDE_CARRIER.name,
            XposedKey.HIDE_STATUS_ICONS.name -> updateClockView()

            XposedKey.HEADER_CLOCK_EXPANSION_Y.name -> buildHeaderViewExpansion()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (!mBroadcastRegistered) {
            mContext.registerReceiver(
                mReceiver,
                IntentFilter().apply {
                    addAction(ACTION_BOOT_COMPLETED)
                },
                Context.RECEIVER_EXPORTED
            )

            mBroadcastRegistered = true
        }

        initResources(mContext)

        val shadeHeaderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController"
        )
        val qsSecurityFooterUtilsClass = findClass("$SYSTEMUI_PACKAGE.qs.QSSecurityFooterUtils")
        val qsContainerImplClass = findClass("$SYSTEMUI_PACKAGE.qs.QSContainerImpl")
        val notificationPanelViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController")
        val configurationListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController$ConfigurationListener")
        val shadeLayoutChangeListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController$ShadeLayoutChangeListener")
        val configurationControllerListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.ShadeHeaderController$configurationControllerListener$1")
        systemBarUtilsClass = findClass("com.android.internal.policy.SystemBarUtils")

        qsSecurityFooterUtilsClass
            .hookConstructor()
            .runAfter { param ->
                if (mActivityStarter == null) {
                    mActivityStarter = param.thisObject.getField("mActivityStarter")
                }
            }

        qsSecurityFooterUtilsClass
            .hookMethod("createDialogView")
            .runAfter { param ->
                if (mActivityStarter == null) {
                    mActivityStarter = param.thisObject.getField("mActivityStarter")
                }
            }

        notificationPanelViewControllerClass
            .hookMethod("onFinishInflate", "reInflateViews")
            .runAfter { param ->
                notificationPanelViewControllerInstance = param.thisObject

                val notificationPanelView = param.thisObject.getField("mView") as FrameLayout
                val screenWidth = mContext.resources.displayMetrics.widthPixels

                mQsHeaderClockContainer.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        if (mContext.isLandscape && halfWidthLandscapeHeaderImage) screenWidth / 2
                        else ViewGroup.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setPadding(
                            mContext.toPx(16),
                            0,
                            mContext.toPx(16),
                            0
                        )
                    }
                    orientation = LinearLayout.HORIZONTAL
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
                    reAddView(mQsClockContainer, 0)
                    reAddView(mQsIconsContainer, 1)
                }

                val headerImageIndex = notificationPanelView.findChildIndexContainsTag(
                    ICONIFY_QS_HEADER_IMAGE_CONTAINER_TAG
                )
                notificationPanelView.reAddView(
                    mQsHeaderClockContainer,
                    if (headerImageIndex == -1) headerImageIndex else headerImageIndex + 1
                )

                buildHeaderViewExpansion()
                updateClockView()
            }

        notificationPanelViewControllerClass
            .hookMethod("setExpandedHeightInternal")
            .run(object : XC_MethodHook() {
                private val hookTracker = ThreadLocal<UnhookHandle>()

                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!showHeaderClock) return

                    val mNotificationShadeWindowController =
                        param.thisObject.getField("mNotificationShadeWindowController")

                    val handle = mNotificationShadeWindowController::class.java
                        .hookMethod("batchApplyWindowLayoutParams")
                        .runBefore batchApply@{ param2 ->
                            if (param2.thisObject !== mNotificationShadeWindowController) return@batchApply

                            val scope = param2.args[0] as Runnable

                            val mDeferWindowLayoutParams =
                                param2.thisObject.getFieldSilently("mDeferWindowLayoutParams") as? Int
                            mDeferWindowLayoutParams?.let {
                                param2.thisObject.setFieldSilently(
                                    "mDeferWindowLayoutParams",
                                    it + 1
                                )
                            }
                            scope.run()
                            Runnable {
                                val notificationPanelView =
                                    param.thisObject.getField("mView") as FrameLayout
                                notificationPanelView.post { updateQSHeaderClockState() }
                            }.run()
                            mDeferWindowLayoutParams?.let {
                                param2.thisObject.setFieldSilently(
                                    "mDeferWindowLayoutParams",
                                    it
                                )
                            }
                            param2.thisObject.callMethodSilently("applyWindowLayoutParams")

                            param2.result = null
                        }
                        .getUnhookHandle()

                    hookTracker.set(handle)
                }

                override fun afterHookedMethod(param: MethodHookParam) {
                    hookTracker.get()?.let {
                        it.unhook()
                        hookTracker.remove()
                    }
                }
            })

        configurationListenerClass
            .hookMethod("onConfigChanged")
            .runAfter {
                if (!showHeaderClock) return@runAfter

                val notificationPanelView = notificationPanelViewControllerInstance
                    .getField("mView") as FrameLayout

                buildHeaderViewExpansion()
                notificationPanelView.post { updateQSHeaderClockState() }
            }

        shadeLayoutChangeListenerClass
            .hookMethod("onLayoutChange")
            .runAfter {
                if (!showHeaderClock) return@runAfter

                val notificationPanelView = notificationPanelViewControllerInstance
                    .getField("mView") as FrameLayout

                notificationPanelView.post { updateQSHeaderClockState() }
            }

        notificationPanelViewControllerClass
            .hookMethodMatchPattern("onPanelStateChanged.*")
            .runAfter {
                if (!showHeaderClock) return@runAfter

                val state = it.args[0] as Int

                when (state) {
                    STATE_OPENING -> startQsOpeningLoop()
                    STATE_OPEN, STATE_CLOSED -> stopQsOpeningLoop()
                }
            }

        qsContainerImplClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                val mHeader = param.thisObject.getField("mHeader") as FrameLayout

                (param.thisObject as FrameLayout).apply {
                    (mHeader.parent as? ViewGroup)?.removeView(mHeader)
                    addView(mHeader, 0)
                    requestLayout()
                }
            }

        shadeHeaderControllerClass
            .hookMethod("onInit")
            .runAfter { param ->
                shadeHeaderControllerInstance = param.thisObject

                if (!showHeaderClock) {
                    param.thisObject.setExtraField("mQsIconsContainer", null)
                    return@runAfter
                }

                val clock = param.thisObject.getField("clock") as TextView
                clock.hideView()

                val date = param.thisObject.getField("date") as TextView
                date.hideView()

                mQsIconsContainer.removeAllViews()

                val qsCarrierGroup = param.thisObject.getAnyField(
                    "qsCarrierGroup",
                    "mShadeCarrierGroup"
                ) as LinearLayout
                (qsCarrierGroup.parent as? ViewGroup)?.removeView(qsCarrierGroup)
                if (hideQsCarrierGroup) qsCarrierGroup.visibility = View.GONE
                mQsIconsContainer.addView(qsCarrierGroup)

                val systemIconsHoverContainer = param.thisObject.getField(
                    "systemIconsHoverContainer"
                ) as LinearLayout
                (systemIconsHoverContainer.parent as? ViewGroup)
                    ?.removeView(systemIconsHoverContainer)
                if (hideStatusIcons) systemIconsHoverContainer.visibility = View.GONE
                mQsIconsContainer.addView(systemIconsHoverContainer)

                param.thisObject.setExtraField("mQsIconsContainer", mQsIconsContainer)
            }

        configurationControllerListenerClass
            .hookMethod("onThemeChanged", "onUiModeChanged")
            .runAfter { updateClockView() }

        BootCallback.registerBootListener { updateClockView() }
    }

    private fun startQsOpeningLoop() {
        if (qsOpeningJob?.isActive == true ||
            notificationPanelViewControllerInstance == null
        ) return

        val notificationPanelView = notificationPanelViewControllerInstance
            .getField("mView") as FrameLayout

        qsOpeningJob = CoroutineScope(Dispatchers.Main).launch {
            while (isActive) {
                awaitFrame()
                if (!notificationPanelView.isLaidOut) continue
                updateQSHeaderClockState()
            }
        }
    }

    private fun stopQsOpeningLoop() {
        qsOpeningJob?.cancel()
        qsOpeningJob = null
    }

    private fun buildHeaderViewExpansion() {
        val isLandscape = mContext.isLandscape
        val density = mContext.resources.displayMetrics.density

        ResourceHookManager
            .hookDimen()
            .forPackageName(FRAMEWORK_PACKAGE)
            .whenCondition { showHeaderClock && mContext.isLandscape }
            .addResource("status_bar_height_default") {
                (mQsHeaderClockContainer.height / density + 16).toInt()
            }
            .forPackageName(SYSTEMUI_PACKAGE)
            .whenCondition { showHeaderClock && mContext.isLandscape }
            .addResource("config_use_split_notification_shade") { true }
            .apply()

        if (isLandscape) {
            mQsHeaderClockContainer.translationY =
                -mContext.toPx(topMargin).toFloat() + landscapeOffsetY
            mQQSContainerAnimator = null
            return
        }

        val startTranslationY = 0f
        val endTranslationY = mContext.toPx(mQQSExpansionY.toInt()).toFloat()

        val builderP: TouchAnimator.Builder = TouchAnimator.Builder()
            .addFloat(
                mQsHeaderClockContainer,
                "translationY",
                startTranslationY,
                endTranslationY
            )

        mQQSContainerAnimator = builderP.build()
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
            updateQSHeaderClockState()
        }
    }

    private fun updateQSHeaderClockState() {
        val shadeHeader = shadeHeaderControllerInstance ?: return

        if (!showHeaderClock) {
            if (mQsHeaderClockContainer.visibility != View.GONE) {
                mQsHeaderClockContainer.visibility = View.GONE
            }
            return
        }

        val shadeExpandedFraction = shadeHeader.getField("shadeExpandedFraction") as Float
        val qsExpandedFraction = shadeHeader.getField("qsExpandedFraction") as Float

        val normalizedFraction =
            ((shadeExpandedFraction - ANIM_START_FRACTION) / (ANIM_END_FRACTION - ANIM_START_FRACTION))
                .coerceIn(0f, 1f)

        if (shadeExpandedFraction <= 0f) {
            if (mQsHeaderClockContainer.visibility != View.GONE) {
                mQsHeaderClockContainer.visibility = View.GONE
            }
            return
        }

        if (mQsHeaderClockContainer.visibility != View.VISIBLE) {
            mQsHeaderClockContainer.visibility = View.VISIBLE
        }

        val isLandscape = mContext.isLandscape
        val screenWidth = mContext.resources.displayMetrics.widthPixels

        val lp = mQsHeaderClockContainer.layoutParams
        val targetWidth = if (isLandscape && halfWidthLandscapeHeaderImage) screenWidth / 2
        else ViewGroup.LayoutParams.MATCH_PARENT

        if (targetWidth != lp.width) {
            lp.width = targetWidth
            mQsHeaderClockContainer.layoutParams = lp
        }

        if (normalizedFraction != lastShadeExpandedFraction) {
            mQsHeaderClockContainer.alpha = normalizedFraction
            lastShadeExpandedFraction = normalizedFraction
        }

        if (qsExpandedFraction != lastQsExpandedFraction) {
            mQQSContainerAnimator?.setPosition(qsExpandedFraction)
            lastQsExpandedFraction = qsExpandedFraction
        }

        mQsHeaderClockContainer.requestLayout()
    }

    private fun modifyClockView(clockView: View) {
        val accent1 = if (customColorEnabled) mAccentColor1
        else mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent1_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        val accent2 = if (customColorEnabled) mAccentColor2
        else mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent2_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        val accent3 = if (customColorEnabled) mAccentColor3
        else mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent3_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )
        val textPrimary = if (customColorEnabled) mTextColor1
        else mContext.getColor(
            mContext.resources.getIdentifier(
                "shade_header_text_color",
                "color",
                SYSTEMUI_PACKAGE
            )
        )
        val textInverse = if (customColorEnabled) mTextColor2
        else mContext.getColor(
            mContext.resources.getIdentifier(
                "shade_header_text_color_bg",
                "color",
                SYSTEMUI_PACKAGE
            )
        )

        val typeface: Typeface? = if (customFontEnabled && HEADER_CLOCK_FONT_FILE.exists()) {
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

        if (textScaling != 1f) {
            applyTextScalingRecursively(clockView, textScaling)
        }

        when (clockStyle) {
            6 -> {
                val imageView = clockView.findViewContainingTag("profile_picture") as? ImageView
                userImage?.let { imageView?.setImageDrawable(it) }
            }
        }
    }

    private val userImage: Drawable?
        get() = if (mUserManager == null) {
            ResourcesCompat.getDrawable(
                appContext.resources,
                R.drawable.img_default_avatar,
                appContext.theme
            )
        } else try {
            val getUserIconMethod = mUserManager!!.javaClass.getMethod(
                "getUserIcon",
                Int::class.javaPrimitiveType
            )
            val userId = UserHandle::class.java
                .getDeclaredMethod("myUserId")
                .invoke(null) as Int
            val bitmapUserIcon = getUserIconMethod.invoke(mUserManager, userId) as Bitmap

            bitmapUserIcon.toDrawable(mContext.resources)
        } catch (throwable: Throwable) {
            if (throwable !is NullPointerException) {
                log(this@HeaderClock, throwable)
            }

            ResourcesCompat.getDrawable(
                appContext.resources,
                R.drawable.img_default_avatar,
                appContext.theme
            )
        }

    private fun setOnClickListener(view: View?) {
        if (view == null) return

        if (view is ViewGroup) {
            view.children.forEach { child ->
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
            log(this@HeaderClock, "mActivityStarter is null")
            return
        }

        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        mActivityStarter.callMethod("postStartActivityDismissingKeyguard", intent, 0)
    }

    private fun onDateClick() {
        if (mActivityStarter == null) {
            log(this@HeaderClock, "mActivityStarter is null")
            return
        }

        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        builder.appendPath(System.currentTimeMillis().toString())

        val intent = Intent(Intent.ACTION_VIEW, builder.build())

        mActivityStarter.callMethod("postStartActivityDismissingKeyguard", intent, 0)
    }
}