package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Environment
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
import com.drdisagree.iconify.common.Const.ACTION_BOOT_COMPLETED
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_CENTERED
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT1
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT2
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_ACCENT3
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT1
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_CODE_TEXT2
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_COLOR_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_FONT_TEXT_SCALING
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_LANDSCAPE_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SIDEMARGIN
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_CLOCK_TOPMARGIN
import com.drdisagree.iconify.common.Preferences.ICONIFY_HEADER_CLOCK_TAG
import com.drdisagree.iconify.common.Resources
import com.drdisagree.iconify.utils.TextUtils
import com.drdisagree.iconify.xposed.HookRes.Companion.resParams
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.Helpers.getColorResCompat
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.applyFontRecursively
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.applyTextScalingRecursively
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.findViewContainsTag
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.findViewWithTagAndChangeColor
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.setMargins
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllConstructors
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.callStaticMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


@SuppressLint("DiscouragedApi")
class HeaderClock(context: Context?) : ModPack(context!!) {

    private var appContext: Context? = null
    private var showHeaderClock = false
    private var centeredClockView = false
    private var hideLandscapeHeaderClock = true
    private var mQsClockContainer: LinearLayout? = LinearLayout(mContext)
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
        if (!XprefsIsInitialized) return

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

        val qsSecurityFooterUtilsClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.qs.QSSecurityFooterUtils",
            loadPackageParam.classLoader
        )
        val quickStatusBarHeaderClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
            loadPackageParam.classLoader
        )
        val dependencyClass =
            findClass("$SYSTEMUI_PACKAGE.Dependency", loadPackageParam.classLoader)
        val activityStarterClass = findClass(
            "$SYSTEMUI_PACKAGE.plugins.ActivityStarter",
            loadPackageParam.classLoader
        )

        if (qsSecurityFooterUtilsClass == null) {
            hookAllConstructors(quickStatusBarHeaderClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mActivityStarter =
                        callStaticMethod(dependencyClass, "get", activityStarterClass)
                }
            })
        } else {
            hookAllConstructors(qsSecurityFooterUtilsClass, object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    mActivityStarter =
                        getObjectField(param.thisObject, "mActivityStarter")
                }
            })
        }

        hookAllMethods(quickStatusBarHeaderClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showHeaderClock) return

                val mQuickStatusBarHeader = param.thisObject as FrameLayout
                val layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                mQsClockContainer!!.setLayoutParams(layoutParams)
                mQsClockContainer!!.visibility = View.GONE

                if (mQsClockContainer!!.parent != null) {
                    (mQsClockContainer!!.parent as ViewGroup).removeView(mQsClockContainer)
                }

                mQuickStatusBarHeader.addView(
                    mQsClockContainer,
                    mQuickStatusBarHeader.childCount
                )

                // Hide stock clock, date and carrier group
                try {
                    val mDateView =
                        getObjectField(param.thisObject, "mDateView") as View
                    mDateView.layoutParams.height = 0
                    mDateView.layoutParams.width = 0
                    mDateView.visibility = View.INVISIBLE
                } catch (ignored: Throwable) {
                }

                try {
                    val mClockView =
                        getObjectField(param.thisObject, "mClockView") as TextView
                    mClockView.visibility = View.INVISIBLE
                    mClockView.setTextAppearance(0)
                    mClockView.setTextColor(0)
                } catch (ignored: Throwable) {
                }

                try {
                    val mClockDateView = getObjectField(
                        param.thisObject,
                        "mClockDateView"
                    ) as TextView
                    mClockDateView.visibility = View.INVISIBLE
                    mClockDateView.setTextAppearance(0)
                    mClockDateView.setTextColor(0)
                } catch (ignored: Throwable) {
                }

                try {
                    val mQSCarriers =
                        getObjectField(param.thisObject, "mQSCarriers") as View
                    mQSCarriers.visibility = View.INVISIBLE
                } catch (ignored: Throwable) {
                }

                updateClockView()
            }
        })

        hookAllMethods(quickStatusBarHeaderClass, "updateResources", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                updateClockView()
            }
        })

        if (Build.VERSION.SDK_INT < 33) {
            try {
                val resParam: InitPackageResourcesParam? = resParams[SYSTEMUI_PACKAGE]

                resParam?.res?.setReplacement(
                    SYSTEMUI_PACKAGE,
                    "bool",
                    "config_use_large_screen_shade_header",
                    false
                )
            } catch (ignored: Throwable) {
            }
        }

        try {
            var shadeHeaderControllerClass = findClassIfExists(
                "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
                loadPackageParam.classLoader
            )
            if (shadeHeaderControllerClass == null) {
                shadeHeaderControllerClass = findClass(
                    "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController",
                    loadPackageParam.classLoader
                )
            }

            hookAllMethods(shadeHeaderControllerClass, "onInit", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (!showHeaderClock) return

                    try {
                        val clock =
                            getObjectField(param.thisObject, "clock") as TextView
                        (clock.parent as ViewGroup).removeView(clock)
                    } catch (ignored: Throwable) {
                    }

                    try {
                        val date =
                            getObjectField(param.thisObject, "date") as TextView
                        (date.parent as ViewGroup).removeView(date)
                    } catch (ignored: Throwable) {
                    }

                    try {
                        val qsCarrierGroup = getObjectField(
                            param.thisObject,
                            "qsCarrierGroup"
                        ) as LinearLayout
                        (qsCarrierGroup.parent as ViewGroup).removeView(qsCarrierGroup)
                    } catch (ignored: Throwable) {
                    }
                }
            })
        } catch (ignored: Throwable) {
        }

        hideStockClockDate()

        try {
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.scheduleAtFixedRate({
                val androidDir =
                    File(Environment.getExternalStorageDirectory().toString() + "/Android")

                if (androidDir.isDirectory()) {
                    updateClockView()
                    executor.shutdown()
                    executor.shutdownNow()
                }
            }, 0, 5, TimeUnit.SECONDS)
        } catch (ignored: Throwable) {
        }
    }

    private fun initResources(context: Context) {
        try {
            appContext = context.createPackageContext(
                BuildConfig.APPLICATION_ID,
                Context.CONTEXT_IGNORE_SECURITY
            )
        } catch (ignored: PackageManager.NameNotFoundException) {
        }

        Handler(Looper.getMainLooper()).post {
            mUserManager = context.getSystemService(Context.USER_SERVICE) as UserManager
        }
    }

    private fun hideStockClockDate() {
        val resParam: InitPackageResourcesParam = resParams[SYSTEMUI_PACKAGE] ?: return

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_qs_status_icons",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!showHeaderClock) return

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
                        } catch (ignored: Throwable) {
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
                        } catch (ignored: Throwable) {
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
                        } catch (ignored: Throwable) {
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }

        try {
            resParam.res.hookLayout(
                SYSTEMUI_PACKAGE,
                "layout",
                "quick_status_bar_header_date_privacy",
                object : XC_LayoutInflated() {
                    override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                        if (!showHeaderClock) return

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
                        } catch (ignored: Throwable) {
                        }
                    }
                })
        } catch (ignored: Throwable) {
        }
    }

    private fun updateClockView() {
        if (mQsClockContainer == null) return

        if (!showHeaderClock) {
            mQsClockContainer!!.visibility = View.GONE
            return
        }

        val isClockAdded =
            mQsClockContainer!!.findViewWithTag<View?>(ICONIFY_HEADER_CLOCK_TAG) != null

        val clockView = clockView

        if (isClockAdded) {
            mQsClockContainer!!.removeView(
                mQsClockContainer!!.findViewWithTag<View>(
                    ICONIFY_HEADER_CLOCK_TAG
                )
            )
        }

        if (clockView != null) {
            if (centeredClockView) {
                mQsClockContainer!!.gravity = Gravity.CENTER
            } else {
                mQsClockContainer!!.gravity = Gravity.START
            }

            clockView.tag = ICONIFY_HEADER_CLOCK_TAG

            TextUtils.convertTextViewsToTitleCase(clockView)

            mQsClockContainer!!.addView(clockView)

            modifyClockView(clockView)
            setOnClickListener(clockView)
        }

        val config = mContext.resources.configuration

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && hideLandscapeHeaderClock) {
            mQsClockContainer!!.visibility = View.GONE
        } else {
            mQsClockContainer!!.visibility = View.VISIBLE
        }
    }

    private val clockView: View?
        get() {
            if (appContext == null || !XprefsIsInitialized) return null

            val inflater = LayoutInflater.from(appContext)
            val clockStyle: Int = Xprefs.getInt(HEADER_CLOCK_STYLE, 0)

            return inflater.inflate(
                appContext!!.resources.getIdentifier(
                    Resources.HEADER_CLOCK_LAYOUT + clockStyle,
                    "layout",
                    BuildConfig.APPLICATION_ID
                ),
                null
            )
        }

    private fun modifyClockView(clockView: View) {
        if (!XprefsIsInitialized) return

        val clockStyle: Int = Xprefs.getInt(HEADER_CLOCK_STYLE, 0)
        val customFontEnabled: Boolean = Xprefs.getBoolean(HEADER_CLOCK_FONT_SWITCH, false)
        val clockScale: Float =
            (Xprefs.getSliderInt(HEADER_CLOCK_FONT_TEXT_SCALING, 10) / 10.0).toFloat()
        val sideMargin: Int = Xprefs.getSliderInt(HEADER_CLOCK_SIDEMARGIN, 0)
        val topMargin: Int = Xprefs.getSliderInt(HEADER_CLOCK_TOPMARGIN, 8)
        val customFont = Environment.getExternalStorageDirectory().toString() +
                "/.iconify_files/headerclock_font.ttf"
        val accent1: Int = Xprefs.getInt(
            HEADER_CLOCK_COLOR_CODE_ACCENT1,
            mContext.resources.getColor(
                mContext.resources.getIdentifier(
                    "android:color/system_accent1_300",
                    "color",
                    mContext.packageName
                ), mContext.theme
            )
        )
        val accent2: Int = Xprefs.getInt(
            HEADER_CLOCK_COLOR_CODE_ACCENT2,
            mContext.resources.getColor(
                mContext.resources.getIdentifier(
                    "android:color/system_accent2_300",
                    "color",
                    mContext.packageName
                ), mContext.theme
            )
        )
        val accent3: Int = Xprefs.getInt(
            HEADER_CLOCK_COLOR_CODE_ACCENT3,
            mContext.resources.getColor(
                mContext.resources.getIdentifier(
                    "android:color/system_accent3_300",
                    "color",
                    mContext.packageName
                ), mContext.theme
            )
        )
        val textPrimary: Int = Xprefs.getInt(
            HEADER_CLOCK_COLOR_CODE_TEXT1,
            getColorResCompat(mContext, android.R.attr.textColorPrimary)
        )
        val textInverse: Int = Xprefs.getInt(
            HEADER_CLOCK_COLOR_CODE_TEXT2,
            getColorResCompat(mContext, android.R.attr.textColorPrimaryInverse)
        )
        var typeface: Typeface? = null

        if (customFontEnabled && File(customFont).exists()) typeface =
            Typeface.createFromFile(File(customFont))

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
        findViewWithTagAndChangeColor(clockView, "gradient", accent1, accent2, 26);

        if (typeface != null) {
            applyFontRecursively(clockView, typeface)
        }

        if (clockScale != 1f) {
            applyTextScalingRecursively(clockView, clockScale)
        }

        when (clockStyle) {
            6 -> {
                val imageView = clockView.findViewContainsTag("user_profile_image") as ImageView?
                userImage?.let { imageView?.setImageDrawable(it) }
            }
        }
    }

    private val userImage: Drawable?
        get() = if (mUserManager == null) {
            ResourcesCompat.getDrawable(
                appContext!!.resources,
                R.drawable.default_avatar,
                appContext!!.theme
            )
        } else try {
            val getUserIconMethod =
                mUserManager!!.javaClass.getMethod("getUserIcon", Int::class.javaPrimitiveType)
            val userId = UserHandle::class.java.getDeclaredMethod("myUserId").invoke(null) as Int
            val bitmapUserIcon = getUserIconMethod.invoke(mUserManager, userId) as Bitmap

            BitmapDrawable(mContext.resources, bitmapUserIcon)
        } catch (throwable: Throwable) {
            if (throwable !is NullPointerException) {
                log(TAG + throwable)
            }

            ResourcesCompat.getDrawable(
                appContext!!.resources,
                R.drawable.default_avatar,
                appContext!!.theme
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

        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0)
    }

    private fun onDateClick() {
        if (mActivityStarter == null) return

        val builder = CalendarContract.CONTENT_URI.buildUpon()
        builder.appendPath("time")
        builder.appendPath(System.currentTimeMillis().toString())

        val intent = Intent(Intent.ACTION_VIEW, builder.build())

        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0)
    }

    companion object {
        private val TAG = "Iconify - ${HeaderClock::class.java.simpleName}: "
    }
}