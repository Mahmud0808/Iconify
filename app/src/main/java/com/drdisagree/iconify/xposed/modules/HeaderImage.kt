package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.os.Environment
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bosphere.fadingedgelayout.FadingEdgeLayout
import com.drdisagree.iconify.common.Const.ACTION_BOOT_COMPLETED
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ALPHA
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_BOTTOM_FADE_AMOUNT
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_HEIGHT
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_LANDSCAPE_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_OVERLAP
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_SWITCH
import com.drdisagree.iconify.common.Preferences.HEADER_IMAGE_ZOOMTOFIT
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedBridge.log
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getIntField
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class HeaderImage(context: Context?) : ModPack(context!!) {

    private var showHeaderImage = false
    private var imageHeight = 140
    private var headerImageAlpha = 100
    private var zoomToFit = false
    private var headerImageOverlap = false
    private var hideLandscapeHeaderImage = true
    private var mQsHeaderLayout: FadingEdgeLayout? = null
    private var mQsHeaderImageView: ImageView? = null
    private var bottomFadeAmount = 0
    private var mBroadcastRegistered = false
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent != null && intent.action != null) {
                if (intent.action == ACTION_BOOT_COMPLETED) {
                    updateQSHeaderImage()
                }
            }
        }
    }

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        showHeaderImage = Xprefs.getBoolean(HEADER_IMAGE_SWITCH, false)
        headerImageAlpha = Xprefs.getInt(HEADER_IMAGE_ALPHA, 100)
        imageHeight = Xprefs.getInt(HEADER_IMAGE_HEIGHT, 140)
        zoomToFit = Xprefs.getBoolean(HEADER_IMAGE_ZOOMTOFIT, false)
        headerImageOverlap = Xprefs.getBoolean(HEADER_IMAGE_OVERLAP, false)
        hideLandscapeHeaderImage = Xprefs.getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true)
        bottomFadeAmount = mContext.toPx(Xprefs.getInt(HEADER_IMAGE_BOTTOM_FADE_AMOUNT, 40))

        if (key.isNotEmpty() &&
            (key[0] == HEADER_IMAGE_SWITCH ||
                    key[0] == HEADER_IMAGE_LANDSCAPE_SWITCH ||
                    key[0] == HEADER_IMAGE_ALPHA ||
                    key[0] == HEADER_IMAGE_HEIGHT ||
                    key[0] == HEADER_IMAGE_ZOOMTOFIT ||
                    key[0] == HEADER_IMAGE_BOTTOM_FADE_AMOUNT)
        ) {
            updateQSHeaderImage()
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

        val quickStatusBarHeader = findClass(
            "$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader",
            loadPackageParam.classLoader
        )
        val qsContainerImpl = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSContainerImpl",
            loadPackageParam.classLoader
        )

        try {
            hookAllMethods(quickStatusBarHeader, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mQuickStatusBarHeader = param.thisObject as FrameLayout
                    mQsHeaderLayout = FadingEdgeLayout(mContext)

                    val layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            imageHeight.toFloat(),
                            mContext.resources.displayMetrics
                        ).toInt()
                    )
                    layoutParams.leftMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        -16f,
                        mContext.resources.displayMetrics
                    ).toInt()
                    layoutParams.rightMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        -16f,
                        mContext.resources.displayMetrics
                    ).toInt()

                    mQsHeaderLayout!!.setLayoutParams(layoutParams)
                    mQsHeaderLayout!!.visibility = View.GONE

                    mQsHeaderImageView = ImageView(mContext)
                    mQsHeaderImageView!!.setLayoutParams(
                        LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )

                    mQsHeaderLayout!!.addView(mQsHeaderImageView)
                    mQuickStatusBarHeader.addView(mQsHeaderLayout, 0)

                    updateQSHeaderImage()
                }
            })

            hookAllMethods(quickStatusBarHeader, "updateResources", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    updateQSHeaderImage()
                }
            })

            hookAllMethods(quickStatusBarHeader, "onMeasure", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val mDatePrivacyView = getObjectField(
                        param.thisObject,
                        "mDatePrivacyView"
                    ) as View
                    val mTopViewMeasureHeight =
                        getIntField(param.thisObject, "mTopViewMeasureHeight")

                    if (callMethod(
                            mDatePrivacyView,
                            "getMeasuredHeight"
                        ) as Int != mTopViewMeasureHeight
                    ) {
                        setObjectField(
                            param.thisObject,
                            "mTopViewMeasureHeight",
                            callMethod(mDatePrivacyView, "getMeasuredHeight")
                        )

                        callMethod(param.thisObject, "updateAnimators")
                    }
                }
            })

            hookAllMethods(qsContainerImpl, "onFinishInflate", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    if (headerImageOverlap) return

                    val mHeader =
                        getObjectField(param.thisObject, "mHeader") as FrameLayout

                    (param.thisObject as FrameLayout).removeView(mHeader)
                    (param.thisObject as FrameLayout).addView(mHeader, 0)
                    (param.thisObject as FrameLayout).requestLayout()
                }
            })
        } catch (throwable: Throwable) {
            log(TAG + throwable)
        }
    }

    private fun updateQSHeaderImage() {
        if (mQsHeaderLayout == null || mQsHeaderImageView == null) {
            return
        }

        if (!showHeaderImage) {
            mQsHeaderLayout!!.visibility = View.GONE
            return
        }

        loadImageOrGif(mQsHeaderImageView!!)

        mQsHeaderImageView!!.imageAlpha = (headerImageAlpha / 100.0 * 255.0).toInt()
        mQsHeaderLayout!!.layoutParams.height = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            imageHeight.toFloat(),
            mContext.resources.displayMetrics
        ).toInt()
        mQsHeaderLayout!!.requestLayout()

        val config = mContext.resources.configuration

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE && hideLandscapeHeaderImage) {
            mQsHeaderLayout!!.visibility = View.GONE
        } else {
            mQsHeaderLayout!!.visibility = View.VISIBLE
        }

        mQsHeaderLayout!!.setFadeEdges(false, false, bottomFadeAmount != 0, false)
        mQsHeaderLayout!!.setFadeSizes(0, 0, bottomFadeAmount, 0)
    }

    private fun ImageView.addCenterProperty() {
        val layoutParams = layoutParams

        when (layoutParams) {
            is RelativeLayout.LayoutParams -> {
                layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
            }

            is LinearLayout.LayoutParams -> {
                layoutParams.gravity = Gravity.CENTER
            }

            is FrameLayout.LayoutParams -> {
                layoutParams.gravity = Gravity.CENTER
            }

            else -> {
                log(TAG + "Invalid layoutParams: $layoutParams")
            }
        }

        setLayoutParams(layoutParams)
    }

    private fun loadImageOrGif(iv: ImageView) {
        try {
            val executor = Executors.newSingleThreadScheduledExecutor()
            executor.scheduleWithFixedDelay({
                val androidDir =
                    File(Environment.getExternalStorageDirectory().toString() + "/Android")

                if (androidDir.isDirectory()) {
                    try {
                        val source = ImageDecoder.createSource(
                            File(
                                Environment.getExternalStorageDirectory()
                                    .toString() + "/.iconify_files/header_image.png"
                            )
                        )
                        val drawable = ImageDecoder.decodeDrawable(source)

                        iv.setImageDrawable(drawable)
                        iv.setClipToOutline(true)

                        if (!zoomToFit) {
                            iv.setScaleType(ImageView.ScaleType.FIT_XY)
                        } else {
                            iv.setScaleType(ImageView.ScaleType.CENTER_CROP)
                            iv.setAdjustViewBounds(false)
                            iv.cropToPadding = false
                            iv.setMinimumWidth(ViewGroup.LayoutParams.MATCH_PARENT)
                            iv.addCenterProperty()
                        }

                        if (drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    } catch (ignored: Throwable) {
                    }

                    executor.shutdown()
                    executor.shutdownNow()
                }
            }, 0, 5, TimeUnit.SECONDS)
        } catch (ignored: Throwable) {
        }
    }

    companion object {
        private val TAG = "Iconify - ${HeaderImage::class.java.simpleName}: "
    }
}