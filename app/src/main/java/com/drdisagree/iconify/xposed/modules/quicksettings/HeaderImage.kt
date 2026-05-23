package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import com.drdisagree.iconify.data.common.Preferences.ICONIFY_QS_HEADER_IMAGE_CONTAINER_TAG
import com.drdisagree.iconify.data.common.XposedConst.HEADER_IMAGE_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.callbacks.BootCallback
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DisplayUtils.isLandscape
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.UnhookHandle
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethodMatchPattern
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HeaderImage(context: Context) : ModPack(context) {

    private var showHeaderImage = false
    private var imageHeight = 140
    private var headerImageAlpha = 100
    private var zoomToFit = false
    private var hideLandscapeHeaderImage = true
    private var halfWidthLandscapeHeaderImage = false
    private var mQsHeaderImageLayout: FadingEdgeLayout? = null
    private var mQsHeaderImageView: ImageView? = null
    private var bottomFadeAmount = 0
    private var notificationPanelViewControllerInstance: Any? = null
    private var shadeHeaderControllerInstance: Any? = null
    private var qsOpeningJob: Job? = null
    private var showHeaderClock = false
    private var lastLayoutAlpha = -1f
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
            showHeaderImage = getBoolean(XposedKey.CUSTOM_HEADER_IMAGE) &&
                    getString(XposedKey.HEADER_IMAGE_FILE_URI).isNotEmpty()
            headerImageAlpha = getInt(XposedKey.HEADER_IMAGE_OPACITY)
            imageHeight = if (getBoolean(XposedKey.HEADER_IMAGE_MAXIMUM_HEIGHT)) -1
            else getInt(XposedKey.HEADER_IMAGE_HEIGHT)
            zoomToFit = getBoolean(XposedKey.HEADER_IMAGE_ZOOM_TO_FIT)
            hideLandscapeHeaderImage = getBoolean(XposedKey.HEADER_IMAGE_HIDE_IN_LANDSCAPE)
            halfWidthLandscapeHeaderImage =
                getBoolean(XposedKey.HEADER_IMAGE_HALF_WIDTH_IN_LANDSCAPE)
            bottomFadeAmount = mContext.toPx(getInt(XposedKey.HEADER_IMAGE_BOTTOM_FADE_AMOUNT))
            showHeaderClock = getBoolean(XposedKey.CUSTOM_HEADER_CLOCK)
        }

        when (key.firstOrNull()) {
            XposedKey.CUSTOM_HEADER_IMAGE.name,
            XposedKey.HEADER_IMAGE_FILE_URI.name,
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
            mContext.registerReceiver(
                mReceiver,
                IntentFilter().apply {
                    addAction(ACTION_BOOT_COMPLETED)
                },
                Context.RECEIVER_EXPORTED
            )

            mBroadcastRegistered = true
        }

        val shadeHeaderControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.shade.LargeScreenShadeHeaderController",
            "$SYSTEMUI_PACKAGE.shade.ShadeHeaderController"
        )
        val qsContainerImplClass = findClass("$SYSTEMUI_PACKAGE.qs.QSContainerImpl")
        val notificationPanelViewControllerClass =
            findClass("$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController")
        val configurationListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController$ConfigurationListener")
        val shadeLayoutChangeListenerClass =
            findClass($$"$$SYSTEMUI_PACKAGE.shade.NotificationPanelViewController$ShadeLayoutChangeListener")

        notificationPanelViewControllerClass
            .hookMethod("onFinishInflate", "reInflateViews")
            .runAfter { param ->
                notificationPanelViewControllerInstance = param.thisObject

                val notificationPanelView = param.thisObject.getField("mView") as FrameLayout

                mQsHeaderImageLayout = FadingEdgeLayout(mContext).apply {
                    tag = ICONIFY_QS_HEADER_IMAGE_CONTAINER_TAG
                }

                mQsHeaderImageLayout!!.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    if (imageHeight == -1) ViewGroup.LayoutParams.MATCH_PARENT
                    else TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        imageHeight.toFloat(),
                        mContext.resources.displayMetrics
                    ).toInt()
                ).apply {
                    gravity = Gravity.START
                    leftMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        -16f,
                        mContext.resources.displayMetrics
                    ).toInt()
                    rightMargin = TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        -16f,
                        mContext.resources.displayMetrics
                    ).toInt()
                }

                mQsHeaderImageView = ImageView(mContext)
                mQsHeaderImageView!!.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                mQsHeaderImageView!!.visibility = View.INVISIBLE

                mQsHeaderImageLayout!!.reAddView(mQsHeaderImageView)
                notificationPanelView.reAddView(mQsHeaderImageLayout, 0)

                updateQSHeaderImage()
            }

        notificationPanelViewControllerClass
            .hookMethod("setExpandedHeightInternal")
            .run(object : XC_MethodHook() {
                private val hookTracker = ThreadLocal<UnhookHandle>()

                override fun beforeHookedMethod(param: MethodHookParam) {
                    if (!showHeaderImage && !showHeaderClock) return

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
                if (!showHeaderImage && !showHeaderClock) return@runAfter

                val notificationPanelView = notificationPanelViewControllerInstance
                    .getField("mView") as FrameLayout

                notificationPanelView.post { updateQSHeaderImageState() }
            }

        shadeLayoutChangeListenerClass
            .hookMethod("onLayoutChange")
            .runAfter {
                if (!showHeaderImage && !showHeaderClock) return@runAfter

                val notificationPanelView = notificationPanelViewControllerInstance
                    .getField("mView") as FrameLayout

                notificationPanelView.post { updateQSHeaderImageState() }
            }

        notificationPanelViewControllerClass
            .hookMethodMatchPattern("onPanelStateChanged.*")
            .runAfter {
                if (!showHeaderImage && !showHeaderClock) return@runAfter

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
            }
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
                updateQSHeaderImageState()
            }
        }
    }

    private fun stopQsOpeningLoop() {
        qsOpeningJob?.cancel()
        qsOpeningJob = null
    }

    private fun updateQSHeaderImage() {
        if (showHeaderImage && mQsHeaderImageView != null) {
            mQsHeaderImageView!!.apply {
                if (visibility != View.VISIBLE) {
                    visibility = View.VISIBLE
                }
                loadImageOrGif()
                updateImageProperties()
            }

            mQsHeaderImageLayout?.apply {
                setFadeEdges(false, false, bottomFadeAmount != 0, false)
                setFadeSizes(0, 0, bottomFadeAmount, 0)
            }
        }
        updateQSHeaderImageState()
    }

    private fun updateQSHeaderImageState() {
        val layout = mQsHeaderImageLayout ?: return
        val imageView = mQsHeaderImageView ?: return
        val shadeHeader = shadeHeaderControllerInstance ?: return

        val shadeExpandedFraction = shadeHeader.getField("shadeExpandedFraction") as Float
        val normalizedFraction =
            ((shadeExpandedFraction - ANIM_START_FRACTION) / (ANIM_END_FRACTION - ANIM_START_FRACTION))
                .coerceIn(0f, 1f)
        val computedAlpha = normalizedFraction * (headerImageAlpha / 100f)

        val isLandscape = mContext.isLandscape
        val screenWidth = mContext.resources.displayMetrics.widthPixels

        if (!showHeaderImage || shadeExpandedFraction <= 0f || (isLandscape && hideLandscapeHeaderImage)) {
            if (imageView.visibility != View.INVISIBLE) {
                imageView.visibility = View.INVISIBLE
            }
            return
        }

        if (imageView.visibility != View.VISIBLE) {
            imageView.visibility = View.VISIBLE
        }

        if (lastLayoutAlpha != computedAlpha || lastLayoutAlpha == -1f) {
            layout.alpha = computedAlpha
            lastLayoutAlpha = computedAlpha
        }

        val lp = imageView.layoutParams
        var requiresLpUpdate = false

        val targetWidth = if (isLandscape && halfWidthLandscapeHeaderImage) screenWidth / 2
        else ViewGroup.LayoutParams.MATCH_PARENT

        val targetHeight = if (imageHeight == -1) ViewGroup.LayoutParams.MATCH_PARENT
        else TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            imageHeight.toFloat(),
            mContext.resources.displayMetrics
        ).toInt()

        if (lp.width != targetWidth) {
            lp.width = targetWidth
            requiresLpUpdate = true
        }

        if (lp.height != targetHeight) {
            lp.height = targetHeight
            requiresLpUpdate = true
        }

        if (requiresLpUpdate) {
            imageView.layoutParams = lp
            imageView.updateImageProperties()
        }

        layout.requestLayout()
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
                updateImageProperties()

                if (drawable is AnimatedImageDrawable) {
                    drawable.start()
                }
            }
        }
    }

    private fun ImageView.updateImageProperties() {
        if (!zoomToFit) {
            scaleType = ImageView.ScaleType.FIT_XY
        } else {
            scaleType = ImageView.ScaleType.CENTER_CROP
            adjustViewBounds = false
            cropToPadding = false
            minimumWidth = ViewGroup.LayoutParams.MATCH_PARENT
            addCenterProperty()
        }
    }

    companion object {
        // Source: https://cs.android.com/android/platform/superproject/+/android-latest-release:frameworks/base/packages/SystemUI/src/com/android/systemui/shade/ShadeExpansionStateManager.kt;l=156-158
        const val STATE_CLOSED = 0
        const val STATE_OPENING = 1
        const val STATE_OPEN = 2

        const val ANIM_START_FRACTION = 0.43f
        const val ANIM_END_FRACTION = 0.7f
    }
}