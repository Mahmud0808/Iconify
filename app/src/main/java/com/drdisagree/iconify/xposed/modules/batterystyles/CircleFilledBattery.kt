package com.drdisagree.iconify.xposed.modules.batterystyles

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.interpolator.view.animation.FastOutSlowInInterpolator
import com.drdisagree.iconify.common.Preferences
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.config.XPrefs
import kotlin.math.max

@Suppress("UNUSED_PARAMETER")
open class CircleFilledBattery(private val mContext: Context, frameColor: Int) :
    BatteryDrawable() {

    private val mLevelAlphaAnimator: ValueAnimator = ValueAnimator.ofInt(255, 255, 255, 45)
    private var mChargingAnimationEnabled = true
    private var mDimension = INTRINSIC_DIMENSION
    private val mPadding = Rect()
    private var mFGColor = Color.WHITE
    private var mBGColor = Color.WHITE
    private var mAlpha = 255
    private var mShowPercentage = false
    private var powerSaveEnabled = false
    private var charging = false
    private var batteryLevel = 0
    private var batteryColors: IntArray? = null
    private var batteryLevels: List<Int>? = null
    private var mChargingColor = -0xcb38a7
    private var mPowerSaveColor = -0x5b00
    private var mShadeColors: IntArray? = null
    private var mShadeLevels: FloatArray? = null

    init {
        mLevelAlphaAnimator.setDuration(2000)
        mLevelAlphaAnimator.interpolator = FastOutSlowInInterpolator()
        mLevelAlphaAnimator.repeatMode = ValueAnimator.REVERSE
        mLevelAlphaAnimator.repeatCount = ValueAnimator.INFINITE
        mLevelAlphaAnimator.addUpdateListener { invalidateSelf() }
    }

    override fun getIntrinsicHeight(): Int {
        return INTRINSIC_DIMENSION
    }

    override fun getIntrinsicWidth(): Int {
        return INTRINSIC_DIMENSION
    }

    override fun draw(canvas: Canvas) {
        refreshShadeColors()

        val basePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        basePaint.color = mBGColor
        basePaint.alpha = Math.round(80f * (mAlpha / 255f))

        val levelPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        val centerX = mDimension / 2f + mPadding.left
        val centerY = mDimension / 2f + mPadding.top

        val baseRadius = mDimension / 2f

        val levelRadius: Float = baseRadius * batteryLevel / 100f

        try {
            setLevelBasedColor(levelPaint, centerX, centerY, baseRadius)
        } catch (t: Throwable) {
            levelPaint.color = Color.BLACK
        }

        if (charging && batteryLevel < 100) {
            if (!mLevelAlphaAnimator.isStarted && mChargingAnimationEnabled) {
                mLevelAlphaAnimator.start()
            }

            levelPaint.alpha = Math.round(
                if (mChargingAnimationEnabled) {
                    mLevelAlphaAnimator.animatedValue as Int
                } else {
                    255
                } * mAlpha / 255f
            )
        } else {
            if (mLevelAlphaAnimator.isStarted) {
                mLevelAlphaAnimator.end()
            }

            levelPaint.alpha = mAlpha
        }

        canvas.drawCircle(centerX, centerY, baseRadius, basePaint)
        canvas.drawCircle(centerY, centerY, levelRadius, levelPaint)
    }

    private fun setLevelBasedColor(paint: Paint, cx: Float, cy: Float, baseRadius: Float) {
        var singleColor = mFGColor

        paint.setShader(null)
        if (charging && batteryLevel < 100) {
            paint.color = chargingColor
            return
        } else if (powerSaveEnabled) {
            paint.color = mPowerSaveColor
            return
        }

        if (mShadeColors == null) {
            for (i in batteryLevels!!.indices) {
                if (batteryLevel <= batteryLevels!![i]) {
                    if (i > 0) {
                        val range: Float = (batteryLevels!![i] - batteryLevels!![i - 1]).toFloat()
                        val currentPos: Float = (batteryLevel - batteryLevels!![i - 1]).toFloat()
                        val ratio = currentPos / range
                        singleColor = ColorUtils.blendARGB(
                            batteryColors!![i - 1],
                            batteryColors!![i],
                            ratio
                        )
                    } else {
                        singleColor = batteryColors!![i]
                    }
                    break
                }
            }
            paint.color = singleColor
        } else {
            val shader = RadialGradient(
                cx, cy, baseRadius,
                mShadeColors!!, mShadeLevels, Shader.TileMode.CLAMP
            )
            paint.setShader(shader)
        }
    }

    override fun setAlpha(alpha: Int) {
        if (mAlpha != alpha) {
            mAlpha = alpha
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
    }

    override fun setBounds(bounds: Rect) {
        super.setBounds(bounds)
        mDimension = max(
            (bounds.height() - mPadding.height()).toDouble(),
            (bounds.width() - mPadding.width()).toDouble()
        )
            .toInt()
        invalidateSelf()
    }


    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.UNKNOWN", "android.graphics.PixelFormat")
    )
    override fun getOpacity(): Int {
        return PixelFormat.UNKNOWN
    }

    fun setShowPercent(showPercent: Boolean) {}

    fun setMeterStyle(batteryStyle: Int) {}

    override fun setBatteryLevel(mLevel: Int) {
        batteryLevel = mLevel
        invalidateSelf()
    }

    override fun setColors(fgColor: Int, bgColor: Int, singleToneColor: Int) {
        mFGColor = fgColor
        mBGColor = bgColor
        initColors()
        invalidateSelf()
    }

    private fun initColors() {
        customBlendColor = try {
            XPrefs.Xprefs?.getBoolean(Preferences.CUSTOM_BATTERY_BLEND_COLOR, false) ?: false
        } catch (ignored: Throwable) {
            RPrefs.getBoolean(Preferences.CUSTOM_BATTERY_BLEND_COLOR, false)
        }

        mChargingColor = if (customBlendColor && chargingColor != Color.BLACK) {
            chargingColor
        } else {
            -0xcb38a7
        }

        mPowerSaveColor = if (customBlendColor && powerSaveFillColor != Color.BLACK) {
            powerSaveFillColor
        } else {
            getColorAttrDefaultColor(android.R.attr.colorError, mContext)
        }

        @ColorInt val fillColor = customFillColor
        @ColorInt val fillGradColor = customFillGradColor

        batteryColors = if (customBlendColor) {
            if (fillColor != Color.BLACK && fillGradColor != Color.BLACK) {
                intArrayOf(fillGradColor, ColorUtils.blendARGB(fillColor, Color.WHITE, 0.4f))
            } else if (fillColor != Color.BLACK) {
                intArrayOf(fillColor, fillColor)
            } else if (fillGradColor != Color.BLACK) {
                intArrayOf(fillGradColor, fillGradColor)
            } else {
                intArrayOf(Color.RED, Color.YELLOW)
            }
        } else {
            intArrayOf(mFGColor, mFGColor)
        }

        batteryLevels = if (customBlendColor) {
            listOf(10, 30)
        } else {
            listOf(0, 0)
        }
    }

    private fun refreshShadeColors() {
        if (batteryColors == null || batteryLevels == null) return

        initColors()

        mShadeColors = IntArray(batteryLevels!!.size * 2 + 2)
        mShadeLevels = FloatArray(mShadeColors!!.size)

        var lastPCT = 0f
        for (i in batteryLevels!!.indices) {
            val rangeLength = batteryLevels!![i] - lastPCT
            val pointer = 2 * i
            mShadeLevels!![pointer] = (lastPCT + rangeLength * 0.6f) / 100
            mShadeColors!![pointer] = batteryColors!![i]
            mShadeLevels!![pointer + 1] = (batteryLevels!![i] - rangeLength * 0.6f) / 100
            mShadeColors!![pointer + 1] = batteryColors!![i]
            lastPCT = batteryLevels!![i].toFloat()
        }

        @ColorInt val fillColor = customFillColor
        mShadeLevels!![mShadeLevels!!.size - 2] =
            (batteryLevels!![batteryLevels!!.size - 1] + (100 - batteryLevels!![batteryLevels!!.size - 1] * 0.3f)) / 100
        mShadeColors!![mShadeColors!!.size - 2] =
            if (customBlendColor) if (fillColor != Color.BLACK) fillColor else Color.GREEN else mFGColor
        mShadeLevels!![mShadeLevels!!.size - 1] = 1f
        mShadeColors!![mShadeColors!!.size - 1] =
            if (customBlendColor) if (fillColor != Color.BLACK) fillColor else Color.GREEN else mFGColor
    }

    override fun setShowPercentEnabled(showPercent: Boolean) {
        mShowPercentage = showPercent
        postInvalidate()
    }

    override fun setChargingEnabled(charging: Boolean) {
        this.charging = charging
        postInvalidate()
    }

    override fun setPowerSavingEnabled(powerSaveEnabled: Boolean) {
        this.powerSaveEnabled = powerSaveEnabled
        postInvalidate()
    }

    fun setChargingAnimationEnabled(enabled: Boolean) {
        mChargingAnimationEnabled = enabled
    }

    private fun postInvalidate() {
        unscheduleSelf(invalidateRunnable)
        scheduleSelf(invalidateRunnable, 0)
    }

    private val invalidateRunnable = Runnable { invalidateSelf() }

    companion object {
        private const val INTRINSIC_DIMENSION = 45
    }
}