package com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.toPx
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.getColorResCompat
import kotlin.math.max

@SuppressLint("DiscouragedApi", "UseCompatLoadingForDrawables")
class OnGoingActionChipView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private val appIcon: ImageView
    private val progressBar: ProgressBar
    private val pillBackground: Drawable?

    private var isCompact = false
    private var isIconMonochrome = false
    private var darkTint = Color.WHITE
    private var pillForeground = Color.WHITE

    private val ringStroke = dpToPx(2).toFloat()
    private val ringGap = dpToPx(2).toFloat()
    private val ringBounds = RectF()
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = ringStroke
    }
    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = ringStroke
        strokeCap = Paint.Cap.ROUND
    }

    init {
        layoutParams = LayoutParams(dpToPx(70), LayoutParams.WRAP_CONTENT).apply {
            marginStart = dpToPx(3)
            gravity = Gravity.CENTER_VERTICAL or Gravity.START
        }
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        visibility = GONE
        pillBackground = ContextCompat.getDrawable(
            context, context.resources.getIdentifier(
                "action_chip_container_background",
                "drawable",
                context.packageName
            )
        )?.mutate()

        appIcon = ImageView(context).apply {
            layoutParams = LayoutParams(spToPx(10), spToPx(10))
            setImageResource(android.R.drawable.sym_def_app_icon)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
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
        }

        addView(appIcon)
        addView(progressBar)
        refreshPillColors()
        applyStyle()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        refreshPillColors()
    }

    fun getAppIcon(): ImageView {
        return appIcon
    }

    fun setCompact(compact: Boolean) {
        if (isCompact == compact) return
        isCompact = compact
        applyStyle()
    }

    fun setProgress(max: Int, progress: Int) {
        if (progressBar.max == max && progressBar.progress == progress) return
        progressBar.max = max
        progressBar.progress = progress
        if (isCompact) invalidate()
    }

    fun setIconMonochrome(monochrome: Boolean) {
        isIconMonochrome = monochrome
        applyIconTint()
    }

    fun setDarkTint(tint: Int) {
        if (darkTint == tint) return
        darkTint = tint
        if (isCompact) {
            applyIconTint()
            invalidate()
        }
    }

    private fun applyStyle() {
        val iconSize = spToPx(if (isCompact) 11 else 10)
        appIcon.layoutParams = (appIcon.layoutParams as LayoutParams).apply {
            width = iconSize
            height = iconSize
            marginEnd = if (isCompact) 0 else spToPx(4)
        }
        progressBar.visibility = if (isCompact) GONE else VISIBLE
        background = if (isCompact) null else pillBackground

        val inset = if (isCompact) (ringStroke + ringGap).toInt() else spToPx(4)
        setPadding(inset, inset, inset, inset)
        layoutParams = layoutParams.apply {
            width = if (isCompact) LayoutParams.WRAP_CONTENT else dpToPx(70)
        }

        setWillNotDraw(!isCompact)
        applyIconTint()
        requestLayout()
        invalidate()
    }

    private fun applyIconTint() {
        if (!isIconMonochrome) {
            appIcon.colorFilter = null
            appIcon.imageTintList = null
            return
        }

        val color = if (isCompact) darkTint else pillForeground
        appIcon.setColorFilter(color)
        appIcon.imageTintList = ColorStateList.valueOf(color)
    }

    private fun refreshPillColors() {
        systemUiColor("materialColorSurfaceBright")?.let { pillBackground?.setTint(it) }
        pillForeground = systemUiColor("materialColorOnSurface")
            ?: getColorResCompat(context, android.R.attr.colorForeground)
        val progressColor = systemUiColor("materialColorPrimary")
            ?: systemUiColor("android:color/system_accent1_100")
        progressColor?.let {
            progressBar.progressDrawable.setTintList(ColorStateList.valueOf(it))
        }
        applyIconTint()
    }

    private fun systemUiColor(name: String): Int? {
        val id = context.resources.getIdentifier(name, "color", context.packageName)
            .takeIf { it != 0 }
            ?: context.resources.getIdentifier(name, "color", "android").takeIf { it != 0 }
            ?: return null
        return context.resources.getColor(id, context.theme)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!isCompact) return

        val centerX = appIcon.left + appIcon.width / 2f
        val centerY = appIcon.top + appIcon.height / 2f
        val radius = max(appIcon.width, appIcon.height) / 2f + ringGap + ringStroke / 2f
        ringBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        trackPaint.color = ColorUtils.setAlphaComponent(darkTint, TRACK_ALPHA)
        canvas.drawOval(ringBounds, trackPaint)

        val fraction = if (progressBar.max > 0) {
            (progressBar.progress.toFloat() / progressBar.max).coerceIn(0f, 1f)
        } else {
            0f
        }
        if (fraction > 0f) {
            progressPaint.color = darkTint
            canvas.drawArc(ringBounds, -90f, 360f * fraction, false, progressPaint)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return context.toPx(dp)
    }

    private fun spToPx(sp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, sp.toFloat(), context.resources.displayMetrics
        ).toInt()
    }

    private companion object {
        const val TRACK_ALPHA = 0x4D
    }
}
