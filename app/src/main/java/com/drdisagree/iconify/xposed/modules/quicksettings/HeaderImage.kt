package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.bosphere.fadingedgelayout.FadingEdgeLayout
import com.drdisagree.iconify.data.common.Const.ACTION_BOOT_COMPLETED
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.Preferences.HEADER_IMAGE_ALPHA
import com.drdisagree.iconify.data.common.Preferences.HEADER_IMAGE_BOTTOM_FADE_AMOUNT
import com.drdisagree.iconify.data.common.Preferences.HEADER_IMAGE_HEIGHT
import com.drdisagree.iconify.data.common.Preferences.HEADER_IMAGE_LANDSCAPE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_IMAGE_OVERLAP
import com.drdisagree.iconify.data.common.Preferences.HEADER_IMAGE_SWITCH
import com.drdisagree.iconify.data.common.Preferences.HEADER_IMAGE_ZOOMTOFIT
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_QS_HEADER_CONTAINER_TAG
import com.drdisagree.iconify.data.common.XposedConst.HEADER_IMAGE_FILE
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class HeaderImage(context: Context) : ModPack(context) {

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
        Xprefs.apply {
            showHeaderImage = getBoolean(HEADER_IMAGE_SWITCH, false)
            headerImageAlpha = getSliderInt(HEADER_IMAGE_ALPHA, 100)
            imageHeight = getSliderInt(HEADER_IMAGE_HEIGHT, 140)
            zoomToFit = getBoolean(HEADER_IMAGE_ZOOMTOFIT, false)
            headerImageOverlap = getBoolean(HEADER_IMAGE_OVERLAP, false)
            hideLandscapeHeaderImage = getBoolean(HEADER_IMAGE_LANDSCAPE_SWITCH, true)
            bottomFadeAmount = mContext.toPx(getSliderInt(HEADER_IMAGE_BOTTOM_FADE_AMOUNT, 40))
        }

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

        val quickStatusBarHeader = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")
        val qsContainerImpl = findClass("$SYSTEMUI_PACKAGE.qs.QSContainerImpl")

        quickStatusBarHeader
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                val mQuickStatusBarHeader = param.thisObject as FrameLayout
                mQsHeaderLayout = FadingEdgeLayout(mContext).apply {
                    tag = ICONIFY_QS_HEADER_CONTAINER_TAG
                }

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

                mQsHeaderLayout!!.layoutParams = layoutParams
                mQsHeaderLayout!!.visibility = View.GONE

                mQsHeaderImageView = ImageView(mContext)
                mQsHeaderImageView!!.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                mQsHeaderLayout!!.addView(mQsHeaderImageView)
                mQuickStatusBarHeader.addView(mQsHeaderLayout, 0)

                updateQSHeaderImage()
            }

        quickStatusBarHeader
            .hookMethod("updateResources")
            .runAfter { updateQSHeaderImage() }

        quickStatusBarHeader
            .hookMethod("onMeasure")
            .suppressError()
            .runAfter { param ->
                val mDatePrivacyView = param.thisObject.getField("mDatePrivacyView") as View
                val mTopViewMeasureHeight =
                    param.thisObject.getField("mTopViewMeasureHeight") as Int

                if (callMethod(
                        mDatePrivacyView,
                        "getMeasuredHeight"
                    ) as Int != mTopViewMeasureHeight
                ) {
                    param.thisObject.setField(
                        "mTopViewMeasureHeight",
                        mDatePrivacyView.callMethod("getMeasuredHeight")
                    )

                    param.thisObject.callMethod("updateAnimators")
                }
            }
            .suppressError()

        qsContainerImpl
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                if (headerImageOverlap) return@runAfter

                val mHeader = param.thisObject.getField("mHeader") as FrameLayout

                (param.thisObject as FrameLayout).apply {
                    (mHeader.parent as? ViewGroup)?.removeView(mHeader)
                    addView(mHeader, 0)
                    requestLayout()
                }
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

        mQsHeaderImageView!!.loadImageOrGif()

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
                log(this@HeaderImage, "Invalid layoutParams: $layoutParams")
            }
        }

        setLayoutParams(layoutParams)
    }

    private fun ImageView.loadImageOrGif() {
        BootCallback.registerBootListener(
            object : BootCallback.BootListener {
                override fun onDeviceBooted() {
                    if (HEADER_IMAGE_FILE.exists()) {
                        val source = ImageDecoder.createSource(HEADER_IMAGE_FILE)
                        val drawable = ImageDecoder.decodeDrawable(source)

                        setImageDrawable(drawable)
                        clipToOutline = true

                        if (!zoomToFit) {
                            scaleType = ImageView.ScaleType.FIT_XY
                        } else {
                            scaleType = ImageView.ScaleType.CENTER_CROP
                            adjustViewBounds = false
                            cropToPadding = false
                            minimumWidth = ViewGroup.LayoutParams.MATCH_PARENT
                            addCenterProperty()
                        }

                        if (drawable is AnimatedImageDrawable) {
                            drawable.start()
                        }
                    }
                }
            }
        )
    }
}