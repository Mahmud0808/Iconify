package com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.getColorResCompat

@SuppressLint("DiscouragedApi", "UseCompatLoadingForDrawables")
class OnGoingActionChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val appIcon: ImageView
    private val progressBar: ProgressBar

    init {
        layoutParams = LayoutParams(dpToPx(70), LayoutParams.WRAP_CONTENT).apply {
            marginStart = dpToPx(3)
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        visibility = GONE
        setPadding(spToPx(4), spToPx(4), spToPx(4), spToPx(4))
        background = ContextCompat.getDrawable(
            context, context.resources.getIdentifier(
                "action_chip_container_background",
                "drawable",
                context.packageName
            )
        )

        appIcon = ImageView(context).apply {
            layoutParams = LayoutParams(spToPx(10), spToPx(10)).apply {
                marginEnd = spToPx(4)
            }
            setImageResource(android.R.drawable.sym_def_app_icon)
            setColorFilter(getColorResCompat(context, android.R.attr.colorForeground))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }

        progressBar = ProgressBar(context, null, 0).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginEnd = spToPx(2)
            }
            minHeight = 0
            isIndeterminate = false
            max = 100
            progress = 0
            gravity = Gravity.CENTER_VERTICAL
            progressDrawable = modRes.getDrawable(
                R.drawable.ongoing_action_chip_progress_bar,
                context.theme
            )
            progressDrawable.setTintList(
                ColorStateList.valueOf(
                    context.resources.getColor(
                        context.resources.getIdentifier(
                            "android:color/system_accent1_100",
                            "color",
                            context.packageName
                        ),
                        context.theme
                    )
                )
            )
        }

        addView(appIcon)
        addView(progressBar)
    }

    fun getAppIcon(): ImageView {
        return appIcon
    }

    fun getProgressBar(): ProgressBar {
        return progressBar
    }

    private fun dpToPx(dp: Int): Int {
        return context.toPx(dp)
    }

    private fun spToPx(sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics
        ).toInt()
    }
}