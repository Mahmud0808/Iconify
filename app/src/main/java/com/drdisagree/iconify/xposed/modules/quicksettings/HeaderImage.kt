package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
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
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_QS_HEADER_CONTAINER_TAG
import com.drdisagree.iconify.data.common.XposedConst.HEADER_IMAGE_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class HeaderImage(context: Context) : ModPack(context) {

    private var showHeaderImage = false
    private var imageHeight = 140
    private var headerImageAlpha = 100
    private var zoomToFit = false
    private var hideLandscapeHeaderImage = true
    private var mQsHeaderLayout: FadingEdgeLayout? = null
    private var mQsHeaderImageView: ImageView? = null
    private var bottomFadeAmount = 0
    private var notificationPanelViewControllerInstance: Any? = null
    private var mShadeHeaderExpansion = 0f
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
            showHeaderImage = getBoolean(XposedKey.CUSTOM_HEADER_IMAGE)
            headerImageAlpha = getInt(XposedKey.HEADER_IMAGE_OPACITY)
            imageHeight = getInt(XposedKey.HEADER_IMAGE_HEIGHT)
            zoomToFit = getBoolean(XposedKey.HEADER_IMAGE_ZOOM_TO_FIT)
            hideLandscapeHeaderImage = getBoolean(XposedKey.HEADER_IMAGE_HIDE_IN_LANDSCAPE)
            bottomFadeAmount = mContext.toPx(getInt(XposedKey.HEADER_IMAGE_BOTTOM_FADE_AMOUNT))
        }

        when (key.firstOrNull()) {
            XposedKey.CUSTOM_HEADER_IMAGE.name,
            XposedKey.HEADER_IMAGE_OPACITY.name,
            XposedKey.HEADER_IMAGE_HEIGHT.name,
            XposedKey.HEADER_IMAGE_ZOOM_TO_FIT.name,
            XposedKey.HEADER_IMAGE_HIDE_IN_LANDSCAPE.name,
            XposedKey.HEADER_IMAGE_BOTTOM_FADE_AMOUNT.name -> updateQSHeaderImage()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (!mBroadcastRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(ACTION_BOOT_COMPLETED)

            mContext.registerReceiver(
                mReceiver,
                intentFilter,
                Context.RECEIVER_EXPORTED
            )

            mBroadcastRegistered = true
        }

        val quickStatusBarHeader = findClass("$SYSTEMUI_PACKAGE.qs.QuickStatusBarHeader")
        val qsContainerImpl = findClass("$SYSTEMUI_PACKAGE.qs.QSContainerImpl")

        val notificationPanelViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController")

        notificationPanelViewControllerClass
            .hookMethod("onFinishInflate", "reInflateViews")
            .runAfter { param ->
                notificationPanelViewControllerInstance = param.thisObject

                val notificationPanelView = param.thisObject.getField("mView") as FrameLayout
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

                mQsHeaderLayout!!.reAddView(mQsHeaderImageView)
                notificationPanelView.reAddView(mQsHeaderLayout, 0)

                updateQSHeaderImage()
            }

        var notificationShadeWindowControllerHooks: Set<XC_MethodHook.Unhook>? = null
        notificationPanelViewControllerClass
            .hookMethod("setExpandedHeightInternal")
            .runBefore { param ->
                if (!showHeaderImage) return@runBefore

                val mNotificationShadeWindowController =
                    param.thisObject.getField("mNotificationShadeWindowController")

                notificationShadeWindowControllerHooks = XposedBridge.hookAllMethods(
                    mNotificationShadeWindowController::class.java,
                    "batchApplyWindowLayoutParams",
                    object : XC_MethodHook() {
                        override fun beforeHookedMethod(param2: MethodHookParam) {
                            if (param2.thisObject !== mNotificationShadeWindowController) return

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
                                notificationPanelView.post { updateQSHeaderImageState() }
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
                    }
                )
            }
            .runAfter {
                notificationShadeWindowControllerHooks?.forEach { it.unhook() }
                notificationShadeWindowControllerHooks = null
            }

        val configurationListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController$ConfigurationListener")

        configurationListenerClass
            .hookMethod("onConfigChanged")
            .runAfter {
                if (!showHeaderImage) return@runAfter

                val notificationPanelView = notificationPanelViewControllerInstance
                    .getField("mView") as FrameLayout
                notificationPanelView.post { updateQSHeaderImageState() }
            }

        val shadeLayoutChangeListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController$ShadeLayoutChangeListener")

        shadeLayoutChangeListenerClass
            .hookMethod("onLayoutChange")
            .runAfter {
                if (!showHeaderImage) return@runAfter

                val notificationPanelView = notificationPanelViewControllerInstance
                    .getField("mView") as FrameLayout
                notificationPanelView.post { updateQSHeaderImageState() }
            }

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

        qsContainerImpl
            .hookMethod("onFinishInflate")
            .runAfter { param ->
                val mHeader = param.thisObject.getField("mHeader") as FrameLayout

                (param.thisObject as FrameLayout).apply {
                    (mHeader.parent as? ViewGroup)?.removeView(mHeader)
                    addView(mHeader, 0)
                    requestLayout()
                }
            }
    }

    private fun updateQSHeaderImage() {
        if (mQsHeaderLayout == null || mQsHeaderImageView == null) return

        if (!showHeaderImage && mQsHeaderLayout!!.visibility != View.GONE) {
            mQsHeaderLayout!!.visibility = View.GONE
            return
        }

        mQsHeaderLayout!!.visibility = View.VISIBLE
        mQsHeaderImageView!!.loadImageOrGif()
        updateQSHeaderImageState()
    }

    private fun updateQSHeaderImageState() {
        if (mQsHeaderLayout == null
            || mQsHeaderImageView == null
            || notificationPanelViewControllerInstance == null
        ) return

        if (!showHeaderImage && mQsHeaderLayout!!.visibility != View.GONE) {
            mQsHeaderLayout!!.visibility = View.GONE
            return
        }

        val config = mContext.resources.configuration
        val shadeHeaderExpansion = notificationPanelViewControllerInstance
            .getField("mShadeHeaderController")
            .getField("shadeExpandedFraction") as Float

        if (shadeHeaderExpansion <= 0f
            || (config.orientation == Configuration.ORIENTATION_LANDSCAPE && hideLandscapeHeaderImage)
        ) {
            mQsHeaderLayout!!.visibility = View.GONE
        } else {
            mQsHeaderLayout!!.visibility = View.VISIBLE

            mQsHeaderLayout!!.layoutParams.height = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                imageHeight.toFloat(),
                mContext.resources.displayMetrics
            ).toInt()

            if (mShadeHeaderExpansion != shadeHeaderExpansion) {
                mShadeHeaderExpansion = shadeHeaderExpansion
                mQsHeaderImageView!!.imageAlpha =
                    (mShadeHeaderExpansion * (headerImageAlpha / 100.0 * 255.0)).toInt()
            }
        }

        mQsHeaderLayout!!.apply {
            setFadeEdges(false, false, bottomFadeAmount != 0, false)
            setFadeSizes(0, 0, bottomFadeAmount, 0)
            requestLayout()
        }
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
        BootCallback.registerBootListener {
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
}