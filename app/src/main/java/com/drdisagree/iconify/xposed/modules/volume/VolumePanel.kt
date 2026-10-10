package com.drdisagree.iconify.xposed.modules.volume

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.ResourceHookManager
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.util.WeakHashMap
import kotlin.math.ceil
import kotlin.math.roundToInt

@SuppressLint("DiscouragedApi", "DefaultLocale")
class VolumePanel(context: Context) : ModPack(context) {

    private var showPercentage = false
    private var showWarning = true
    private val sliderPercentViews = WeakHashMap<Any, TextView>()
    private val sliderPercentages = WeakHashMap<Any, Int>()

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            showPercentage = getBoolean(XposedKey.VOLUME_PANEL_PERCENTAGE)
            showWarning = getBoolean(XposedKey.VOLUME_PANEL_SAFETY_WARNING)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        showVolumePercentage()
        showSafetyWarning()
    }

    private fun showVolumePercentage() {
        val volumeDialogImplClass = findClass(
            "$SYSTEMUI_PACKAGE.volume.VolumeDialogImpl",
            suppressError = true
        )
        val audioStreamStateClass =
            findClass($$"$$SYSTEMUI_PACKAGE.volume.panel.component.volume.slider.ui.viewmodel.AudioStreamSliderViewModel$State")
        val audioStreamToStateClass = findClass(
            $$"$$SYSTEMUI_PACKAGE.volume.panel.component.volume.slider.ui.viewmodel.AudioStreamSliderViewModel$toState$1",
            $$"$$SYSTEMUI_PACKAGE.volume.panel.component.volume.slider.ui.viewmodel.AudioStreamSliderViewModel$toState$2",
            suppressError = true
        )

        volumeDialogImplClass
            .hookMethod("initRow")
            .runAfter { param ->
                if (!showPercentage) return@runAfter

                val rowHeader: TextView = param.args[0].getField("header") as TextView

                if ((rowHeader.parent as ViewGroup).findViewById<TextView>(
                        mContext.resources.getIdentifier(
                            "volume_number",
                            "id",
                            mContext.packageName
                        )
                    ) != null
                ) return@runAfter

                val volumeNumber = createVolumeTextView()
                (rowHeader.parent as ViewGroup).addView(volumeNumber, 0)

                param.args[0].setField(
                    "number",
                    (param.args[0].getField("view") as View).findViewById(
                        mContext.resources.getIdentifier(
                            "volume_number",
                            "id",
                            mContext.packageName
                        )
                    )
                )
            }

        volumeDialogImplClass
            .hookMethod("updateVolumeRowH")
            .runAfter { param ->
                if (!showPercentage) return@runAfter

                val volumeNumber: TextView =
                    (param.args[0].getField("view") as View).findViewById(
                        mContext.resources.getIdentifier(
                            "volume_number",
                            "id",
                            mContext.packageName
                        )
                    ) ?: return@runAfter

                val mState: Any = param.thisObject.getFieldSilently("mState") ?: return@runAfter

                val ss = mState
                    .getField("states")
                    .callMethod(
                        "get",
                        param.args[0].getField("stream")
                    ) ?: return@runAfter

                val levelMax: Int = ss.getField("levelMax") as Int

                volumeNumber.let {
                    if (it.text.isEmpty()) {
                        it.text = "0"
                    }

                    if (it.text.contains("%")) {
                        it.text = it.text.subSequence(0, it.text.length - 1)
                    }

                    var level = ceil(it.text.toString().toFloat() / levelMax * 100f).toInt()

                    if (level > 100) {
                        level = 100
                    } else if (level < 0) {
                        level = 0
                    }

                    it.text = String.format("%d%%", level)
                }
            }

        // Compose implementation of extended volume panel
        fun updateVolumeLabel(thisObject: Any) {
            val currentValue = thisObject.getField("value") as Float
            val maxValue = thisObject
                .getField("valueRange")
                .getField("_endInclusive") as Float
            val percentage = 100 * currentValue / maxValue
            var label = thisObject.getField("label") as String
            label = String.format("$label - ${percentage.roundToInt()}%%")

            thisObject.setField("label", label)
        }

        audioStreamStateClass
            .hookConstructor()
            .runAfter { param ->
                if (!showPercentage) return@runAfter

                updateVolumeLabel(param.thisObject)
            }

        audioStreamToStateClass
            .hookMethod("invokeSuspend")
            .runAfter { param ->
                if (!showPercentage) return@runAfter

                val state = param.result
                updateVolumeLabel(state)
                param.result = state
            }

        showVolumeDialogPercentage()
    }

    private fun showVolumeDialogPercentage() {
        findClass(
            "$SYSTEMUI_PACKAGE.volume.dialog.sliders.ui.VolumeDialogSlidersViewBinder",
            suppressError = true
        )
            .hookMethod("bindSlider")
            .suppressError()
            .runAfter { param ->
                if (!showPercentage) return@runAfter

                val sliderView = param.args.getOrNull(2) as? FrameLayout ?: return@runAfter
                val viewModel = param.args.getOrNull(1)
                    ?.getFieldSilently("volumeDialogSliderViewBinderProvider")
                    ?.callMethod("get")
                    ?.getFieldSilently("viewModel") ?: return@runAfter
                val composeView = sliderView.findViewById<View?>(
                    mContext.resources.getIdentifier(
                        "volume_dialog_slider",
                        "id",
                        mContext.packageName
                    )
                ) ?: return@runAfter

                val volumeNumber = sliderView.findViewWithTag<TextView?>(VOLUME_NUMBER_TAG)
                    ?: createVolumeTextView().apply {
                        tag = VOLUME_NUMBER_TAG
                        includeFontPadding = false
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT,
                            Gravity.TOP or Gravity.CENTER_HORIZONTAL
                        )
                        sliderView.addView(this)

                        measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
                        val extraHeight = measuredHeight + mContext.toPx(6)

                        (composeView.layoutParams as? FrameLayout.LayoutParams)?.let { params ->
                            params.gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
                            params.topMargin = extraHeight
                            composeView.layoutParams = params
                        }

                        sliderView.layoutParams?.let { params ->
                            val maxHeight = params.getFieldSilently("matchConstraintMaxHeight") as? Int
                            if (maxHeight != null && maxHeight > 0) {
                                params.setField("matchConstraintMaxHeight", maxHeight + extraHeight)
                                sliderView.layoutParams = params
                            }
                        }
                    }

                volumeNumber.setTextColor(dialogTextColor(sliderView.context))
                sliderPercentViews[viewModel] = volumeNumber
                sliderPercentages[viewModel]?.let { volumeNumber.text = String.format("%d%%", it) }
            }

        findClass(
            $$"$$SYSTEMUI_PACKAGE.volume.dialog.sliders.ui.viewmodel.VolumeDialogSliderViewModel$state$2",
            suppressError = true
        )
            .hookMethod("invokeSuspend")
            .suppressError()
            .runAfter { param ->
                if (!showPercentage) return@runAfter

                val state = param.result ?: return@runAfter
                if (!state.javaClass.name.endsWith("VolumeDialogSliderStateModel")) return@runAfter

                val viewModel = param.thisObject.getFieldSilently("this$0") ?: return@runAfter
                val value = state.getFieldSilently("value") as? Float ?: return@runAfter
                val range = state.getFieldSilently("valueRange") ?: return@runAfter
                val start = range.getFieldSilently("_start") as? Float ?: return@runAfter
                val end = range.getFieldSilently("_endInclusive") as? Float ?: return@runAfter
                if (end <= start) return@runAfter

                val percentage = (100 * (value - start) / (end - start)).roundToInt().coerceIn(0, 100)
                sliderPercentages[viewModel] = percentage

                sliderPercentViews[viewModel]?.let { view ->
                    view.post {
                        view.setTextColor(dialogTextColor(view.context))
                        view.text = String.format("%d%%", percentage)
                    }
                }
            }
    }

    private fun showSafetyWarning() {
        val volumeDialogImplClass = findClass(
            "$SYSTEMUI_PACKAGE.volume.VolumeDialogImpl",
            suppressError = true
        )

        if (volumeDialogImplClass == null) {
            ResourceHookManager
                .hookBoolean()
                .whenCondition { !showWarning }
                .forPackageName(SYSTEMUI_PACKAGE)
                .addResource("enable_safety_warning") { false }
                .apply()
        } else {
            try {
                volumeDialogImplClass
                    .hookMethod(
                        "onShowSafetyWarning",
                        "showSafetyWarningH"
                    )
                    .throwError()
                    .runBefore { param ->
                        if (!showWarning) {
                            param.result = null
                        }
                    }
            } catch (_: Throwable) {
                volumeDialogImplClass
                    .hookConstructor()
                    .runAfter { param ->
                        if (showWarning) return@runAfter

                        val mControllerCallbackH = param.thisObject.getField("mControllerCallbackH")

                        mControllerCallbackH.javaClass
                            .hookMethod("onShowSafetyWarning")
                            .runBefore { param ->
                                if (!showWarning) {
                                    param.result = null
                                }
                            }
                    }
            }
        }
    }

    private fun dialogTextColor(context: Context): Int {
        val isDark = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        return context.getColor(
            if (isDark) android.R.color.system_accent1_200 else android.R.color.system_accent1_600
        )
    }

    private fun createVolumeTextView(): TextView {
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = mContext.toPx(8)
        }

        val volumeNumber = TextView(mContext).apply {
            layoutParams = params
            id = mContext.resources.getIdentifier(
                "volume_number",
                "id",
                mContext.packageName
            )
            gravity = Gravity.CENTER
            textSize = 12f
            setTextColor(
                mContext.resources.getColor(
                    mContext.resources.getIdentifier(
                        "android:color/system_accent1_300",
                        "color",
                        mContext.packageName
                    ), mContext.theme
                )
            )
            text = String.format("%d%%", 0)
        }

        return volumeNumber
    }

    companion object {
        private const val VOLUME_NUMBER_TAG = "iconify_volume_number"
    }
}