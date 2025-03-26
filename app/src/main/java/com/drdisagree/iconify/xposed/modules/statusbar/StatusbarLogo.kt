package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageDecoder
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_CUSTOM
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_POSITION
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_SIZE
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_STYLE
import com.drdisagree.iconify.data.common.Preferences.STATUSBAR_LOGO_SWITCH
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.HeadsUpCallback
import com.drdisagree.iconify.xposed.modules.extras.callbacks.KeyguardShowingCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toCircularDrawable
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.views.logoview.LogoImage
import com.drdisagree.iconify.xposed.modules.extras.views.logoview.LogoImageView
import com.drdisagree.iconify.xposed.modules.extras.views.logoview.LogoImageViewRight
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("DiscouragedApi")
class StatusbarLogo(context: Context) : ModPack(context) {

    private var showLogo = false
    private var logoPosition = 0
    private var logoStyle = 0
    private var logoSize = 12
    private var customLogo = false
    private var logoImageView: LogoImageView? = null
    private var logoImageViewRight: LogoImageViewRight? = null
    private var darkIconDispatcherClass: Class<*>? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showLogo = getBoolean(STATUSBAR_LOGO_SWITCH, false)
            logoPosition = getString(STATUSBAR_LOGO_POSITION, "0")!!.toInt()
            logoStyle = getString(STATUSBAR_LOGO_STYLE, "0")!!.toInt()
            logoSize = getSliderInt(STATUSBAR_LOGO_SIZE, 12)
            customLogo = logoStyle == 33
        }

        when (key.firstOrNull()) {
            in setOf(
                STATUSBAR_LOGO_SWITCH,
                STATUSBAR_LOGO_POSITION,
                STATUSBAR_LOGO_STYLE
            ) -> {
                logoImageView?.updateSettings(showLogo, logoPosition, logoStyle)
                logoImageViewRight?.updateSettings(showLogo, logoPosition, logoStyle)
            }

            STATUSBAR_LOGO_CUSTOM -> {
                logoImageView?.loadCustomLogo()
                logoImageViewRight?.loadCustomLogo()
            }

            STATUSBAR_LOGO_SIZE -> {
                logoImageView?.updateLeftLogo()
                logoImageViewRight?.updateRightLogo()
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        darkIconDispatcherClass = findClass("$SYSTEMUI_PACKAGE.plugins.DarkIconDispatcher")

        val phoneStatusBarViewClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.PhoneStatusBarView")

        phoneStatusBarViewClass
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                val phoneStatusBarView = param.thisObject as ViewGroup

                val operatorName = phoneStatusBarView.findViewById<View>(
                    mContext.resources.getIdentifier(
                        "operator_name_stub",
                        "id",
                        mContext.packageName
                    )
                )
                val startSideExceptHeadsUp = operatorName.parent as ViewGroup
                val logoIndex = startSideExceptHeadsUp.indexOfChild(operatorName) + 1

                val systemIcons = phoneStatusBarView.findViewById<ViewGroup>(
                    mContext.resources.getIdentifier(
                        "system_icons",
                        "id",
                        mContext.packageName
                    )
                )

                if (logoImageView == null) {
                    logoImageView = LogoImageView(mContext).apply {
                        setupLogo(
                            "status_bar_left_clock_starting_padding",
                            "status_bar_left_clock_end_padding"
                        )
                    }
                }

                if (logoImageViewRight == null) {
                    logoImageViewRight = LogoImageViewRight(mContext).apply {
                        setupLogo(
                            "status_bar_clock_starting_padding",
                            "status_bar_clock_end_padding"
                        )
                    }
                }

                logoImageView!!.updateSettings(showLogo, logoPosition, logoStyle)
                logoImageViewRight!!.updateSettings(showLogo, logoPosition, logoStyle)

                logoImageView!!.loadCustomLogo()
                logoImageViewRight!!.loadCustomLogo()

                startSideExceptHeadsUp.reAddView(logoImageView, logoIndex)
                systemIcons.reAddView(logoImageViewRight)
            }

        HeadsUpCallback.getInstance().registerHeadsUpListener(
            object : HeadsUpCallback.HeadsUpListener {
                override fun onHeadsUpShown() {
                    logoImageView?.alpha = 0f
                }

                override fun onHeadsUpGone() {
                    logoImageView?.alpha = 1f
                }
            }
        )

        KeyguardShowingCallback.getInstance().registerKeyguardShowingListener(
            object : KeyguardShowingCallback.KeyguardShowingListener {
                override fun onKeyguardShown() {
                    logoImageView?.alpha = 0f
                }

                override fun onKeyguardDismissed() {
                    logoImageView?.alpha = 1f
                }
            }
        )

        val clockClass = findClass("$SYSTEMUI_PACKAGE.statusbar.policy.Clock")

        clockClass
            .hookMethod("onDarkChanged")
            .runAfter { param ->
                if (logoImageView == null || logoImageViewRight == null) return@runAfter

                val areas = param.args[0]
                val tint = param.args[2]

                val mTintColor = darkIconDispatcherClass.callStaticMethod(
                    "getTint",
                    areas,
                    logoImageView,
                    tint
                ) as Int

                logoImageView!!.mTintColor = mTintColor
                logoImageViewRight!!.mTintColor = mTintColor

                if (!showLogo) return@runAfter

                if (!customLogo) {
                    if (logoImageView!!.isLogoVisible) {
                        logoImageView!!.updateLogo()
                    }
                    if (logoImageViewRight!!.isLogoVisible) {
                        logoImageViewRight!!.updateLogo()
                    }
                }
            }
    }

    private fun LogoImage.updateLeftLogo() {
        setupLogo(
            "status_bar_left_clock_starting_padding",
            "status_bar_left_clock_end_padding"
        )
        updateSettings(showLogo, logoPosition, logoStyle)
    }

    private fun LogoImage.updateRightLogo() {
        setupLogo(
            "status_bar_clock_starting_padding",
            "status_bar_clock_end_padding"
        )
        updateSettings(showLogo, logoPosition, logoStyle)
    }

    private fun LogoImage.setupLogo(startPaddingRes: String, endPaddingRes: String) {
        layoutParams = LinearLayout.LayoutParams(
            mContext.toPx(logoSize),
            mContext.toPx(logoSize)
        ).apply {
            gravity = Gravity.CENTER_VERTICAL or Gravity.END
            marginStart = mContext.resources.getDimensionPixelSize(
                mContext.resources.getIdentifier(
                    startPaddingRes,
                    "dimen",
                    mContext.packageName
                )
            )
            marginEnd = mContext.resources.getDimensionPixelSize(
                mContext.resources.getIdentifier(
                    endPaddingRes,
                    "dimen",
                    mContext.packageName
                )
            )
        }
        scaleType = ImageView.ScaleType.FIT_CENTER
        visibility = View.GONE
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun LogoImage.loadCustomLogo() {
        if (!customLogo) return

        try {
            val executor = Executors.newSingleThreadScheduledExecutor()

            executor.scheduleWithFixedDelay({
                val androidDir = File(Environment.getExternalStorageDirectory(), "/Android")

                if (androidDir.isDirectory) {
                    try {
                        val drawable = ImageDecoder.decodeDrawable(
                            ImageDecoder.createSource(
                                File(
                                    Environment.getExternalStorageDirectory(),
                                    "/.iconify_files/statusbar_logo.png"
                                )
                            )
                        ).toCircularDrawable(mContext)

                        setImageDrawable(drawable)
                    } catch (_: Throwable) {
                        @Suppress("DEPRECATION")
                        modRes.getDrawable(R.drawable.ic_android_logo)
                    }

                    executor.shutdown()
                    executor.shutdownNow()
                }
            }, 0, 5, TimeUnit.SECONDS)
        } catch (_: Throwable) {
        }
    }
}