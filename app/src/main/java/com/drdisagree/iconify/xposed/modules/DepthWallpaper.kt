package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_CHANGED
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FADE_ANIMATION
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_PARALLAX_EFFECT
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.common.Preferences.ICONIFY_DEPTH_WALLPAPER_TAG
import com.drdisagree.iconify.common.Preferences.ICONIFY_LOCKSCREEN_CLOCK_TAG
import com.drdisagree.iconify.common.Preferences.UNZOOM_DEPTH_WALLPAPER
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.DisplayUtils.isScreenOn
import com.drdisagree.iconify.xposed.modules.utils.ParallaxImageView
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.findClassIfExists
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@SuppressLint("DiscouragedApi")
class DepthWallpaper(context: Context?) : ModPack(context!!) {

    private var showDepthWallpaper = false
    private var showFadingAnimation = false
    private var enableParallaxEffect = false
    private var backgroundMovement = 1.0f
    private var foregroundMovement = 3.0f
    private var mDepthWallpaperLayout: FrameLayout? = null
    private var mDepthWallpaperBackground: ParallaxImageView? = null
    private var mDepthWallpaperForeground: ParallaxImageView? = null

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        showDepthWallpaper = Xprefs!!.getBoolean(DEPTH_WALLPAPER_SWITCH, false)
        showFadingAnimation = Xprefs!!.getBoolean(DEPTH_WALLPAPER_FADE_ANIMATION, false)
        enableParallaxEffect = Xprefs!!.getBoolean(DEPTH_WALLPAPER_PARALLAX_EFFECT, false)
        backgroundMovement = Xprefs!!.getFloat(DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER, 1.0f)
        foregroundMovement = Xprefs!!.getFloat(DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER, 3.0f)

        if (key.isNotEmpty() &&
            (key[0] == DEPTH_WALLPAPER_SWITCH ||
                    key[0] == DEPTH_WALLPAPER_CHANGED ||
                    key[0] == DEPTH_WALLPAPER_BACKGROUND_MOVEMENT_MULTIPLIER ||
                    key[0] == DEPTH_WALLPAPER_FOREGROUND_MOVEMENT_MULTIPLIER)
        ) {
            updateWallpaper()
        }

        if (key.isNotEmpty() && key[0] == DEPTH_WALLPAPER_PARALLAX_EFFECT) {
            if (enableParallaxEffect) {
                mDepthWallpaperBackground?.registerSensorListener()
                mDepthWallpaperForeground?.registerSensorListener()
            } else {
                mDepthWallpaperBackground?.unregisterSensorListener()
                mDepthWallpaperForeground?.unregisterSensorListener()
            }
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val keyguardBottomAreaViewClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.KeyguardBottomAreaView",
            loadPackageParam.classLoader
        )

        hookAllMethods(keyguardBottomAreaViewClass, "onFinishInflate", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showDepthWallpaper) return

                val view = param.thisObject as View
                val mIndicationArea = view.findViewById<ViewGroup>(
                    mContext.resources.getIdentifier(
                        "keyguard_indication_area",
                        "id",
                        mContext.packageName
                    )
                )
                mIndicationArea.setClipChildren(false)
                mIndicationArea.clipToPadding = false
                mIndicationArea.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                mIndicationArea.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
                (mIndicationArea.layoutParams as MarginLayoutParams).bottomMargin = 0

                // Create a new layout for the indication text views
                val mIndicationTextView = LinearLayout(mContext)
                val mIndicationViewParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                val bottomMargin = mContext.resources.getDimensionPixelSize(
                    mContext.resources.getIdentifier(
                        "keyguard_indication_margin_bottom",
                        "dimen",
                        mContext.packageName
                    )
                )

                mIndicationViewParams.setMargins(0, 0, 0, bottomMargin)
                mIndicationViewParams.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                mIndicationTextView.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                mIndicationTextView.orientation = LinearLayout.VERTICAL
                mIndicationTextView.setLayoutParams(mIndicationViewParams)

                // Get the indication text views
                val mTopIndicationView = mIndicationArea.findViewById<TextView>(
                    mContext.resources.getIdentifier(
                        "keyguard_indication_text",
                        "id",
                        mContext.packageName
                    )
                )
                val mLockScreenIndicationView = mIndicationArea.findViewById<TextView>(
                    mContext.resources.getIdentifier(
                        "keyguard_indication_text_bottom",
                        "id",
                        mContext.packageName
                    )
                )

                // We added a blank view to the top of the layout to push the indication text views to the bottom
                // The reason we did this is because gravity is not working properly on the indication text views
                val blankView = View(mContext)
                blankView.setLayoutParams(
                    LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        1.0f
                    )
                )

                // Remove the existing indication text views from the indication area
                (mTopIndicationView.parent as ViewGroup).removeView(mTopIndicationView)
                (mLockScreenIndicationView.parent as ViewGroup).removeView(mLockScreenIndicationView)

                // Add the indication text views to the new layout
                mIndicationTextView.addView(blankView)
                mIndicationTextView.addView(mTopIndicationView)
                mIndicationTextView.addView(mLockScreenIndicationView)

                val mIndicationAreaDupe = FrameLayout(mContext)
                mIndicationAreaDupe.setLayoutParams(
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                mIndicationAreaDupe.addView(mIndicationTextView, -1)
                mIndicationArea.addView(mIndicationAreaDupe)

                // Get the depth wallpaper layout
                mDepthWallpaperLayout =
                    mIndicationArea.findViewWithTag<FrameLayout>(ICONIFY_DEPTH_WALLPAPER_TAG)

                // Create the depth wallpaper layout if it doesn't exist
                if (mDepthWallpaperLayout == null) {
                    mDepthWallpaperLayout = FrameLayout(mContext)
                    mDepthWallpaperLayout!!.setLayoutParams(
                        FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    mDepthWallpaperLayout!!.tag = ICONIFY_DEPTH_WALLPAPER_TAG
                    mIndicationAreaDupe.addView(mDepthWallpaperLayout, 0)
                }

                mDepthWallpaperBackground = ParallaxImageView(mContext)
                mDepthWallpaperForeground = ParallaxImageView(mContext)

                mDepthWallpaperBackground!!.setLayoutParams(
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )
                mDepthWallpaperForeground!!.setLayoutParams(
                    FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                mDepthWallpaperLayout!!.addView(mDepthWallpaperBackground, 0)
                mDepthWallpaperLayout!!.addView(mDepthWallpaperForeground, -1)

                // Fix the bottom shortcuts pushing the wallpaper
                val offset = intArrayOf(0)
                try {
                    offset[0] = (mContext.resources.getDimensionPixelSize(
                        mContext.resources.getIdentifier(
                            "keyguard_affordance_fixed_height",
                            "dimen",
                            mContext.packageName
                        )
                    ) + mContext.resources.getDimensionPixelSize(
                        mContext.resources.getIdentifier(
                            "keyguard_affordance_horizontal_offset",
                            "dimen",
                            mContext.packageName
                        )
                    ))
                } catch (ignored: Throwable) {
                }

                try {
                    val startButton = view.findViewById<ImageView>(
                        mContext.resources.getIdentifier(
                            "start_button",
                            "id",
                            mContext.packageName
                        )
                    )
                    startButton.getViewTreeObserver().addOnGlobalLayoutListener {
                        (mIndicationTextView.layoutParams as MarginLayoutParams).setMarginStart(
                            if (startButton.visibility != View.GONE) offset[0] else 0
                        )
                    }
                } catch (ignored: Throwable) {
                }

                try {
                    val endButton = view.findViewById<ImageView>(
                        mContext.resources.getIdentifier(
                            "end_button",
                            "id",
                            mContext.packageName
                        )
                    )
                    endButton.getViewTreeObserver().addOnGlobalLayoutListener {
                        (mIndicationTextView.layoutParams as MarginLayoutParams).setMarginEnd(
                            if (endButton.visibility != View.GONE) offset[0] else 0
                        )
                    }
                } catch (ignored: Throwable) {
                }

                if (Build.VERSION.SDK_INT >= 34) {
                    try {
                        val keyguardSettingsButton = view.findViewById<LinearLayout>(
                            mContext.resources.getIdentifier(
                                "keyguard_settings_button",
                                "id",
                                mContext.packageName
                            )
                        )
                        keyguardSettingsButton.getViewTreeObserver()
                            .addOnGlobalLayoutListener {
                                (mIndicationTextView.layoutParams as MarginLayoutParams).setMarginStart(
                                    if (keyguardSettingsButton.visibility != View.GONE) offset[0] else 0
                                )
                                (mIndicationTextView.layoutParams as MarginLayoutParams).setMarginEnd(
                                    if (keyguardSettingsButton.visibility != View.GONE) offset[0] else 0
                                )
                            }
                    } catch (ignored: Throwable) {
                    }
                }

                updateWallpaper()
                registerScreenStateChecker()
            }
        })

        hookAllMethods(
            keyguardBottomAreaViewClass,
            "onConfigurationChanged",
            object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    updateWallpaper()
                }
            })

        var notificationPanelViewControllerClass = findClassIfExists(
            "$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController",
            loadPackageParam.classLoader
        )
        if (notificationPanelViewControllerClass == null) notificationPanelViewControllerClass =
            findClass(
                "$SYSTEMUI_PACKAGE.statusbar.phone.NotificationPanelViewController",
                loadPackageParam.classLoader
            )

        val moveKeyguardBottomArea: XC_MethodHook = object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showDepthWallpaper) return

                val mView = getObjectField(param.thisObject, "mView") as View
                val keyguardBottomArea = mView.findViewById<View>(
                    mContext.resources.getIdentifier(
                        "keyguard_bottom_area",
                        "id",
                        mContext.packageName
                    )
                )

                val parent = keyguardBottomArea.parent as ViewGroup
                parent.removeView(keyguardBottomArea)
                parent.addView(keyguardBottomArea, 0)
            }
        }

        hookAllMethods(
            notificationPanelViewControllerClass,
            "onFinishInflate",
            moveKeyguardBottomArea
        )
        hookAllMethods(
            notificationPanelViewControllerClass,
            "reInflateViews",
            moveKeyguardBottomArea
        )

        val noKeyguardIndicationPadding: XC_MethodHook = object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (showDepthWallpaper) {
                    if (Build.VERSION.SDK_INT >= 33) {
                        try {
                            val resId = mContext.resources.getIdentifier(
                                "keyguard_indication_area_padding",
                                "dimen",
                                mContext.packageName
                            )

                            if (param.args[0] == resId) {
                                param.result = 0
                            }
                        } catch (ignored: Throwable) {
                        }
                    } else {
                        // These resources are only available on Android 12L and below
                        try {
                            val resId = mContext.resources.getIdentifier(
                                "keyguard_indication_margin_bottom",
                                "dimen",
                                mContext.packageName
                            )

                            if (param.args[0] == resId) {
                                param.result = 0
                            }
                        } catch (ignored: Throwable) {
                        }
                        try {
                            val resId = mContext.resources.getIdentifier(
                                "keyguard_indication_margin_bottom_fingerprint_in_display",
                                "dimen",
                                mContext.packageName
                            )

                            if (param.args[0] == resId) {
                                param.result = 0
                            }
                        } catch (ignored: Throwable) {
                        }
                    }
                }
            }
        }

        hookAllMethods(
            Resources::class.java,
            "getDimensionPixelOffset",
            noKeyguardIndicationPadding
        )
        hookAllMethods(
            Resources::class.java,
            "getDimensionPixelSize",
            noKeyguardIndicationPadding
        )
    }

    // Broadcast receiver for checking screen state
    private fun registerScreenStateChecker() {
        if (mDepthWallpaperLayout == null) return

        val filter = IntentFilter()

        filter.addAction(Intent.ACTION_SCREEN_ON)
        filter.addAction(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_TIME_TICK)
        filter.addAction(Intent.ACTION_TIME_CHANGED)
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED)
        filter.addAction(Intent.ACTION_LOCALE_CHANGED)

        val screenStateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                Handler(Looper.getMainLooper()).post { updateFadeAnimation() }
            }
        }

        mContext.registerReceiver(screenStateReceiver, filter)

        updateFadeAnimation()
    }

    private fun updateWallpaper() {
        if (mDepthWallpaperLayout == null) return

        if (!showDepthWallpaper) {
            mDepthWallpaperLayout!!.visibility = View.GONE
            return
        }

        try {
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.scheduleAtFixedRate({
                val androidDir =
                    File(Environment.getExternalStorageDirectory().toString() + "/Android")
                if (androidDir.isDirectory()) {
                    Handler(Looper.getMainLooper()).post {
                        try {
                            val backgroundImg = ImageDecoder.createSource(
                                File(
                                    Environment.getExternalStorageDirectory()
                                        .toString() + "/.iconify_files/depth_wallpaper_bg.png"
                                )
                            )
                            val foregroundImg = ImageDecoder.createSource(
                                File(
                                    Environment.getExternalStorageDirectory()
                                        .toString() + "/.iconify_files/depth_wallpaper_fg.png"
                                )
                            )

                            val backgroundDrawable = ImageDecoder.decodeDrawable(backgroundImg)
                            val foregroundDrawable = ImageDecoder.decodeDrawable(foregroundImg)

                            mDepthWallpaperBackground!!.setImageDrawable(backgroundDrawable)
                            mDepthWallpaperBackground!!.setClipToOutline(true)
                            mDepthWallpaperBackground!!.setScaleType(ImageView.ScaleType.CENTER_CROP)
                            mDepthWallpaperBackground!!.setMovementMultiplier(backgroundMovement)

                            val zoomWallpaper: Boolean =
                                !Xprefs?.getBoolean(UNZOOM_DEPTH_WALLPAPER, false)!!

                            if (zoomWallpaper) {
                                mDepthWallpaperBackground!!.scaleX = 1.1f
                                mDepthWallpaperBackground!!.scaleY = 1.1f
                            }

                            mDepthWallpaperForeground!!.setImageDrawable(foregroundDrawable)
                            mDepthWallpaperForeground!!.setClipToOutline(true)
                            mDepthWallpaperForeground!!.setScaleType(ImageView.ScaleType.CENTER_CROP)
                            mDepthWallpaperForeground!!.setMovementMultiplier(foregroundMovement)

                            if (zoomWallpaper) {
                                mDepthWallpaperForeground!!.scaleX = 1.1f
                                mDepthWallpaperForeground!!.scaleY = 1.1f
                            }

                            mDepthWallpaperLayout!!.visibility = View.VISIBLE
                        } catch (ignored: Throwable) {
                        }
                    }

                    executor.shutdown()
                    executor.shutdownNow()
                }
            }, 0, 5, TimeUnit.SECONDS)
        } catch (ignored: Throwable) {
        }
    }

    private fun updateFadeAnimation() {
        if (!showDepthWallpaper) return

        val clockView = mDepthWallpaperLayout!!.findViewWithTag<View>(ICONIFY_LOCKSCREEN_CLOCK_TAG)
        val animDuration: Long = 800

        if (isScreenOn(mContext)) {
            if (mDepthWallpaperBackground != null && mDepthWallpaperBackground!!.alpha != 1f) {
                if (showFadingAnimation) {
                    val animation = mDepthWallpaperBackground!!.animation

                    if (!(animation != null && animation.hasStarted() && !animation.hasEnded())) {
                        mDepthWallpaperBackground!!
                            .animate()
                            .alpha(1f)
                            .setDuration(animDuration)
                            .start()
                    }
                } else {
                    mDepthWallpaperBackground!!.setAlpha(1f)
                }
            }

            if (mDepthWallpaperForeground != null && mDepthWallpaperForeground!!.alpha != 1f) {
                if (showFadingAnimation) {
                    val animation = mDepthWallpaperForeground!!.animation

                    if (!(animation != null && animation.hasStarted() && !animation.hasEnded())) {
                        mDepthWallpaperForeground!!.clearAnimation()
                        mDepthWallpaperForeground!!
                            .animate()
                            .alpha(1f)
                            .setDuration(animDuration)
                            .start()
                    }
                } else {
                    mDepthWallpaperForeground!!.setAlpha(1f)
                }
            }

            if (clockView != null && clockView.alpha != 1f) {
                if (showFadingAnimation) {
                    val animation = clockView.animation

                    if (!(animation != null && animation.hasStarted() && !animation.hasEnded())) {
                        clockView.clearAnimation()
                        clockView
                            .animate()
                            .alpha(1f)
                            .setDuration(animDuration)
                            .start()
                    }
                } else {
                    clockView.setAlpha(1f)
                }
            }
        } else {
            if (mDepthWallpaperBackground != null && mDepthWallpaperBackground!!.alpha != 0f) {
                if (showFadingAnimation) {
                    val animation = mDepthWallpaperBackground!!.animation

                    if (!(animation != null && animation.hasStarted() && !animation.hasEnded())) {
                        mDepthWallpaperBackground!!.clearAnimation()
                        mDepthWallpaperBackground!!
                            .animate()
                            .alpha(0f)
                            .setDuration(animDuration)
                            .start()
                    }
                }
            }

            if (mDepthWallpaperForeground != null && mDepthWallpaperForeground!!.alpha != 0f) {
                if (showFadingAnimation) {
                    val animation = mDepthWallpaperForeground!!.animation

                    if (!(animation != null && animation.hasStarted() && !animation.hasEnded())) {
                        mDepthWallpaperForeground!!.clearAnimation()
                        mDepthWallpaperForeground!!
                            .animate()
                            .alpha(0f)
                            .setDuration(animDuration)
                            .start()
                    }
                }
            }

            if (clockView != null && clockView.alpha != 0.7f) {
                if (showFadingAnimation) {
                    val animation = clockView.animation

                    if (!(animation != null && animation.hasStarted() && !animation.hasEnded())) {
                        clockView.clearAnimation()
                        clockView
                            .animate()
                            .alpha(0.7f)
                            .setDuration(animDuration)
                            .start()
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = "Iconify - ${DepthWallpaper::class.java.simpleName}: "
    }
}