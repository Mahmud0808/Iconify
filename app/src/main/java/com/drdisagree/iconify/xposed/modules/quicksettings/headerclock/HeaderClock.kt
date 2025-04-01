package com.drdisagree.iconify.xposed.modules.quicksettings.headerclock

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
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
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_SIDEMARGIN
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_STYLE
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_CLOCK_TOPMARGIN
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_HEADER_CLOCK_TAG
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.common.XposedConst.HEADER_CLOCK_FONT_FILE
import com.drdisagree.iconify.utils.TextUtils
import com.drdisagree.iconify.utils.color.ColorUtils.getColorResCompat
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewContainsTag
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.Locale


@SuppressLint("DiscouragedApi")
class HeaderClock(context: Context) : ModPack(context) {

    private var showHeaderClock = false
    private var centeredClockView = false
    private var hideLandscapeHeaderClock = true
    private var mQsClockContainer: LinearLayout = LinearLayout(mContext)
    private var mUserManager: UserManager? = null
    private var mActivityStarter: Any? = null
    private val mOnClickListener = View.OnClickListener { v: View ->
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

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showHeaderClock = getBoolean(HEADER_CLOCK_SWITCH, false)
            centeredClockView = getBoolean(HEADER_CLOCK_CENTERED, false)
            hideLandscapeHeaderClock = getBoolean(HEADER_CLOCK_LANDSCAPE_SWITCH, true)
        }

        if (key.isNotEmpty() &&
            (key[0] == HEADER_CLOCK_SWITCH ||
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
                    key[0] == HEADER_CLOCK_LANDSCAPE_SWITCH)
        ) {
            updateClockView()
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

        val qsSecurityFooterUtilsClass = findClass("$SYSTEMUI_PACKAGE.qs.QSSecurityFooterUtils")
        val quickStatusBarHeaderClass = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")
        val dependencyClass = findClass("$SYSTEMUI_PACKAGE.Dependency")
        val activityStarterClass = findClass("$SYSTEMUI_PACKAGE.plugins.ActivityStarter")

        quickStatusBarHeaderClass
            .hookConstructor()
            .runAfter {
                try {
                    mActivityStarter =
                        callStaticMethod(dependencyClass, "get", activityStarterClass)
                } catch (_: Throwable) {
                }
            }

        qsSecurityFooterUtilsClass
            .hookConstructor()
            .runAfter { param ->
                mActivityStarter = param.thisObject.getField("mActivityStarter")
            }

        quickStatusBarHeaderClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (!showHeaderClock) return@runAfter

                val mQuickStatusBarHeader = param.thisObject as FrameLayout

                mQsClockContainer.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                mQsClockContainer.visibility = View.GONE

                if (mQsClockContainer.parent != null) {
                    (mQsClockContainer.parent as ViewGroup).removeView(mQsClockContainer)
                }

                mQuickStatusBarHeader.addView(
                    mQsClockContainer,
                    mQuickStatusBarHeader.childCount
                )

                // Hide stock clock, date and carrier group
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

                updateClockView()
            }

        quickStatusBarHeaderClass
            .hookMethod("updateResources")
            .runAfter { updateClockView() }

        if (Build.VERSION.SDK_INT < 33) {
            try {
                val xResources: XResources? = resParams[SYSTEMUI_PACKAGE]?.res

                xResources?.setReplacement(
                    SYSTEMUI_PACKAGE,
                    "bool",
                    "config_use_large_screen_shade_header",
                    false
                )
            } catch (_: Throwable) {
            }
        }

        val shadeHeaderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController"
        )

        shadeHeaderControllerClass
            .hookMethod("onInit")
            .runAfter { param ->
                if (!showHeaderClock) return@runAfter

                val clock = param.thisObject.getFieldSilently("clock") as? TextView
                (clock?.parent as? ViewGroup)?.removeView(clock)

                val date = param.thisObject.getFieldSilently("date") as? TextView
                (date?.parent as? ViewGroup)?.removeView(date)

                val qsCarrierGroup =
                    param.thisObject.getFieldSilently("qsCarrierGroup") as? LinearLayout
                (qsCarrierGroup?.parent as? ViewGroup)?.removeView(qsCarrierGroup)
            }

        hideStockClockDate()

        BootCallback.registerBootListener(
            object : BootCallback.BootListener {
                override fun onDeviceBooted() {
                    updateClockView()
                }
            }
        )
    }

    private fun initResources(context: Context) {
        Handler(Looper.getMainLooper()).post {
            mUserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        }
    }

    private fun hideStockClockDate() {
        val xResources: XResources = resParams[SYSTEMUI_PACKAGE]?.res ?: return

        xResources
            .hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "quick_qs_status_icons")
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
            }

        xResources
            .hookLayout()
            .packageName(SYSTEMUI_PACKAGE)
            .resource("layout", "quick_status_bar_header_date_privacy")
            .suppressError()
            .run { liparam ->
                if (!showHeaderClock) return@run

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

    private fun updateClockView() {
        if (!showHeaderClock) {
            mQsClockContainer.visibility = View.GONE
            return
        }

        val isClockAdded =
            mQsClockContainer.findViewWithTag<View?>(ICONIFY_HEADER_CLOCK_TAG) != null

        val clockView = clockView

        if (isClockAdded) {
            mQsClockContainer.removeView(
                mQsClockContainer.findViewWithTag<View>(
                    ICONIFY_HEADER_CLOCK_TAG
                )
            )
        }

        if (clockView != null) {
            if (centeredClockView) {
                mQsClockContainer.gravity = Gravity.CENTER
            } else {
                mQsClockContainer.gravity = Gravity.START
            }

            clockView.tag = ICONIFY_HEADER_CLOCK_TAG

            TextUtils.convertTextViewsToTitleCase(clockView)

            mQsClockContainer.addView(clockView)

            modifyClockView(clockView)
            setOnClickListener(clockView)
        }

        val config = mContext.resources.configuration

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && hideLandscapeHeaderClock) {
            mQsClockContainer.visibility = View.GONE
        } else {
            mQsClockContainer.visibility = View.VISIBLE
        }
    }

    private val clockView: View?
        get() {
            if (!XprefsIsInitialized) return null

            val inflater = LayoutInflater.from(appContext)
            val clockStyle: Int = Xprefs.getInt(HEADER_CLOCK_STYLE, 0)

            return inflater.inflate(
                appContext.resources.getIdentifier(
                    Resources.HEADER_CLOCK_LAYOUT + clockStyle,
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
        val topMargin: Int = Xprefs.getSliderInt(HEADER_CLOCK_TOPMARGIN, 8)

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

        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
            setMargins(clockView, mContext, 0, topMargin, sideMargin, 0)
        } else {
            setMargins(clockView, mContext, sideMargin, topMargin, 0, 0)
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
                val imageView = clockView.findViewContainsTag("profile_picture") as ImageView?
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
                mUserManager!!.javaClass.getMethod("getUserIcon", Int::class.javaPrimitiveType)
            val userId = UserHandle::class.java.getDeclaredMethod("myUserId").invoke(null) as Int
            val bitmapUserIcon = getUserIconMethod.invoke(mUserManager, userId) as Bitmap

            BitmapDrawable(mContext.resources, bitmapUserIcon)
        } catch (throwable: Throwable) {
            if (throwable !is NullPointerException) {
                log(this@HeaderClock, throwable)
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
                    child.setOnClickListener(mOnClickListener)
                }

                (child as? ViewGroup)?.let { setOnClickListener(it) }
            }
        } else {
            val tag = if (view.tag == null) "" else view.tag.toString()

            if (tag.lowercase(Locale.getDefault()).contains("clock") ||
                tag.lowercase(Locale.getDefault()).contains("date")
            ) {
                view.setOnClickListener(mOnClickListener)
            }
        }
    }

    private fun onClockClick() {
        if (mActivityStarter == null) return

        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP)

        mActivityStarter.callMethod("postStartActivityDismissingKeyguard", intent, 0)
    }

    private fun onDateClick() {
        if (mActivityStarter == null) return

        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        builder.appendPath(System.currentTimeMillis().toString())

        val intent = Intent(Intent.ACTION_VIEW, builder.build())

        mActivityStarter.callMethod("postStartActivityDismissingKeyguard", intent, 0)
    }
}