/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.drdisagree.iconify.xposed.mods.batterystyles

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.LinearGradient
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.graphics.PathParser
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookRes.modRes
import com.drdisagree.iconify.xposed.utils.SettingsLibUtils
import kotlin.math.floor

@SuppressLint("DiscouragedApi")
open class LandscapeBatteryI(private val context: Context, frameColor: Int) :
    BatteryDrawable() {

    // Need to load:
    // 1. perimeter shape
    // 2. fill mask (if smaller than perimeter, this would create a fill that
    //    doesn't touch the walls
    private val perimeterPath = Path()
    private val scaledPerimeter = Path()
    private val errorPerimeterPath = Path()
    private val scaledErrorPerimeter = Path()

    // Fill will cover the whole bounding rect of the fillMask, and be masked by the path
    private val fillMask = Path()
    private val scaledFill = Path()

    // custompath
    private val fillOutlinePath = Path()
    private val scaledfillOutline = Path()
    private val fillMask1 = Path()
    private val scaledFill1 = Path()
    private val fillMask2 = Path()
    private val scaledFill2 = Path()
    private val fillMask3 = Path()
    private val scaledFill3 = Path()
    private val fillMask4 = Path()
    private val scaledFill4 = Path()
    private val fillMask5 = Path()
    private val scaledFill5 = Path()
    private val fillMask6 = Path()
    private val scaledFill6 = Path()
    private val fillMask7 = Path()
    private val scaledFill7 = Path()
    private val fillMask8 = Path()
    private val scaledFill8 = Path()
    private val fillMask9 = Path()
    private val scaledFill9 = Path()
    private val fillMask10 = Path()
    private val scaledFill10 = Path()
    private val fillNgguyu = Path()
    private val scaledNgguyu = Path()
    private val fillMingkem = Path()
    private val scaledMingkem = Path()
    private val fillMrengut = Path()
    private val scaledMrengut = Path()

    // Based off of the mask, the fill will interpolate across this space
    private val fillRect = RectF()

    // Top of this rect changes based on level, 100% == fillRect
    private val levelRect = RectF()
    private val levelPath = Path()

    // Updates the transform of the paths when our bounds change
    private val scaleMatrix = Matrix()
    private val padding = Rect()

    // The net result of fill + perimeter paths
    private val unifiedPath = Path()

    // Bolt path (used while charging)
    private val boltPath = Path()
    private val scaledBolt = Path()

    // Plus sign (used for power save mode)
    private val plusPath = Path()
    private val scaledPlus = Path()

    private var intrinsicHeight: Int
    private var intrinsicWidth: Int

    // To implement hysteresis, keep track of the need to invert the interior icon of the battery
    private var invertFillIcon = false

    // Colors can be configured based on battery level (see res/values/arrays.xml)
    private var colorLevels: IntArray

    private var fillColor: Int = Color.WHITE
    private var backgroundColor: Int = Color.WHITE

    // updated whenever level changes
    private var levelColor: Int = Color.WHITE

    // Dual tone implies that battery level is a clipped overlay over top of the whole shape
    private var dualTone = false

    private var batteryLevel = 0

    private val invalidateRunnable: () -> Unit = {
        invalidateSelf()
    }

    open var criticalLevel: Int = 5

    var charging = false
        set(value) {
            field = value
            postInvalidate()
        }

    override fun setChargingEnabled(charging: Boolean) {
        this.charging = charging
        postInvalidate()
    }

    var powerSaveEnabled = false
        set(value) {
            field = value
            postInvalidate()
        }

    override fun setPowerSavingEnabled(powerSaveEnabled: Boolean) {
        this.powerSaveEnabled = powerSaveEnabled
        postInvalidate()
    }

    var showPercent = false
        set(value) {
            field = value
            postInvalidate()
        }

    override fun setShowPercentEnabled(showPercent: Boolean) {
        this.showPercent = showPercent
        postInvalidate()
    }

    private val fillColorStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
        p.alpha = 255
        p.isDither = true
        p.strokeWidth = 5f
        p.style = Paint.Style.STROKE
        p.blendMode = BlendMode.SRC
        p.strokeMiter = 5f
        p.strokeJoin = Paint.Join.ROUND
    }

    private val fillColorStrokeProtection = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.isDither = true
        p.strokeWidth = 5f
        p.style = Paint.Style.STROKE
        p.blendMode = BlendMode.CLEAR
        p.strokeMiter = 5f
        p.strokeJoin = Paint.Join.ROUND
    }

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
        p.alpha = 255
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
    }

    private val errorPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = SettingsLibUtils.getColorAttrDefaultColor(context, android.R.attr.colorError)
        p.alpha = 255
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor1: Int = 0xffff4931.toInt()
    private val customFillPaint1 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor1
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor2: Int = 0xffff4931.toInt()
    private val customFillPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor2
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor3: Int = 0xffff9e19.toInt()
    private val customFillPaint3 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor3
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor4: Int = 0xffff9e19.toInt()
    private val customFillPaint4 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor4
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor5: Int = 0xfffff32f.toInt()
    private val customFillPaint5 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor5
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor6: Int = 0xfffff32f.toInt()
    private val customFillPaint6 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor6
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor7: Int = 0xff9aff00.toInt()
    private val customFillPaint7 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor7
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor8: Int = 0xff9aff00.toInt()
    private val customFillPaint8 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor8
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor9: Int = 0xff2ed200.toInt()
    private val customFillPaint9 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor9
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private var fillPaintColor10: Int = 0xff2ed200.toInt()
    private val customFillPaint10 = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = fillPaintColor10
        p.alpha = 110
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private val defaultFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
        p.alpha = 255
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
        p.blendMode = BlendMode.SRC
    }

    private val chargingPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
    }

    private val customFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
    }

    private val powerSavePaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
    }

    private val powerSaveFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
    }

    private val scaledFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
    }

    private val scaledPerimeterPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
    }

    private val scaledPerimeterPaintDef = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
    }

    // Only used if dualTone is set to true
    private val dualToneBackgroundFill = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.color = frameColor
        p.alpha = 85 // ~0.3 alpha by default
        p.isDither = true
        p.strokeWidth = 0f
        p.style = Paint.Style.FILL_AND_STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).also { p ->
        p.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        p.textAlign = Paint.Align.CENTER
    }

    init {
        val density = context.resources.displayMetrics.density
        intrinsicHeight = (HEIGHT * density).toInt()
        intrinsicWidth = (WIDTH * density).toInt()

        val res = context.resources
        val levels = res.obtainTypedArray(
            res.getIdentifier(
                "batterymeter_color_levels", "array", context.packageName
            )
        )
        val colors = res.obtainTypedArray(
            res.getIdentifier(
                "batterymeter_color_values", "array", context.packageName
            )
        )
        val n = levels.length()
        colorLevels = IntArray(2 * n)
        for (i in 0 until n) {
            colorLevels[2 * i] = levels.getInt(i, 0)
            if (colors.getType(i) == TypedValue.TYPE_ATTRIBUTE) {
                colorLevels[2 * i + 1] = SettingsLibUtils.getColorAttrDefaultColor(
                    colors.getResourceId(i, 0), context
                )
            } else {
                colorLevels[2 * i + 1] = colors.getColor(i, 0)
            }
        }
        levels.recycle()
        colors.recycle()

        loadPaths()
    }

    override fun draw(c: Canvas) {
        c.saveLayer(null, null)
        unifiedPath.reset()
        levelPath.reset()
        levelRect.set(fillRect)
        val fillFraction = batteryLevel / 100f
        val fillRight =
            if (batteryLevel == 100)
                fillRect.right + 1f
            else
                fillRect.right - (fillRect.width() * (1 - fillFraction))

        levelRect.right = floor(fillRight.toDouble()).toFloat()
        levelPath.addRoundRect(
            levelRect,
            floatArrayOf(
                4.2f,
                4.2f,
                4.2f,
                4.2f,
                4.2f,
                4.2f,
                4.2f,
                4.2f
            ), Path.Direction.CCW
        )

        scaledFillPaint.alpha = if (scaledFillAlpha) 100 else 0
        scaledPerimeterPaint.alpha =
            if (scaledPerimeterAlpha) 100 else scaledPerimeterPaintDef.alpha

        // The perimeter should never change
        c.drawPath(scaledFill, scaledFillPaint)
        unifiedPath.addPath(scaledPerimeter)
        // If drawing dual tone, the level is used only to clip the whole drawable path
        if (!dualTone) {
            unifiedPath.op(levelPath, Path.Op.UNION)
        }

        fillPaint.color = levelColor
        val black = Color.BLACK

        powerSavePaint.color =
            if (customBlendColor && powerSaveColor != black) powerSaveColor else SettingsLibUtils.getColorAttrDefaultColor(
                context,
                android.R.attr.colorError
            )

        // Deal with unifiedPath clipping before it draws
        if (!showPercent) {
            if (charging) {
                // Clip out the bolt shape
                unifiedPath.op(scaledBolt, Path.Op.DIFFERENCE)
                if (!invertFillIcon) {
                    c.drawPath(scaledBolt, fillPaint)
                }
            }
        }

        if (dualTone) {
            // Dual tone means we draw the shape again, clipped to the charge level
            c.drawPath(unifiedPath, dualToneBackgroundFill)
            c.save()
            c.clipRect(
                bounds.left.toFloat(),
                0f,
                bounds.right + bounds.width() * fillFraction,
                bounds.left.toFloat()
            )
            c.drawPath(unifiedPath, fillPaint)
            c.restore()
        } else {
            // Non dual-tone means we draw the perimeter (with the level fill), and potentially
            // draw the fill again with a critical color
            if (customFillRainbow) {
                if (charging) {
                    c.drawPath(scaledPerimeter, scaledPerimeterPaint)
                    c.clipOutPath(scaledfillOutline)
                    if (batteryLevel in 91..100) {
                        c.drawPath(scaledFill10, customFillPaint10)
                        c.drawPath(scaledFill9, customFillPaint9)
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 81..90) {
                        c.drawPath(scaledFill9, customFillPaint9)
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 71..80) {
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 61..70) {
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 51..60) {
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 41..50) {
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 31..40) {
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 21..30) {
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 11..20) {
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 0..10) {
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    c.drawPath(scaledBolt, fillPaint)
                    c.clipPath(scaledFill)
                } else if (powerSaveEnabled) {
                    c.drawPath(scaledErrorPerimeter, powerSavePaint)
                    c.clipOutPath(scaledfillOutline)
                    if (batteryLevel in 91..100) {
                        c.drawPath(scaledFill10, customFillPaint10)
                        c.drawPath(scaledFill9, customFillPaint9)
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 81..90) {
                        c.drawPath(scaledFill9, customFillPaint9)
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 71..80) {
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 61..70) {
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 51..60) {
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 41..50) {
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 31..40) {
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 21..30) {
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 11..20) {
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 0..10) {
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    c.clipPath(scaledFill)
                } else {
                    c.drawPath(scaledPerimeter, scaledPerimeterPaint)
                    c.clipOutPath(scaledfillOutline)
                    if (batteryLevel in 91..100) {
                        c.drawPath(scaledFill10, customFillPaint10)
                        c.drawPath(scaledFill9, customFillPaint9)
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 81..90) {
                        c.drawPath(scaledFill9, customFillPaint9)
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 71..80) {
                        c.drawPath(scaledFill8, customFillPaint8)
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 61..70) {
                        c.drawPath(scaledFill7, customFillPaint7)
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 51..60) {
                        c.drawPath(scaledFill6, customFillPaint6)
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 41..50) {
                        c.drawPath(scaledFill5, customFillPaint5)
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 31..40) {
                        c.drawPath(scaledFill4, customFillPaint4)
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 21..30) {
                        c.drawPath(scaledFill3, customFillPaint3)
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 11..20) {
                        c.drawPath(scaledFill2, customFillPaint2)
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    if (batteryLevel in 0..10) {
                        c.drawPath(scaledFill1, customFillPaint1)
                    }
                    c.clipPath(scaledFill)
                }
            } else {
                // else rainbow
                if (charging) {
                    c.drawPath(scaledPerimeter, scaledPerimeterPaint)
                    c.clipOutPath(scaledfillOutline)
                    chargingPaint.color = chargingColor
                    c.drawPath(
                        levelPath,
                        if (customBlendColor && chargingColor != black) chargingPaint else defaultFillPaint
                    )
                    c.drawPath(scaledBolt, fillPaint)
                    c.clipPath(scaledFill)
                } else if (powerSaveEnabled) {
                    c.drawPath(scaledErrorPerimeter, powerSavePaint)
                    c.clipOutPath(scaledfillOutline)
                    powerSaveFillPaint.color = powerSaveFillColor
                    c.drawPath(
                        levelPath,
                        if (customBlendColor && powerSaveFillColor != black) powerSaveFillPaint else defaultFillPaint
                    )
                    c.clipPath(scaledFill)
                } else {
                    c.drawPath(scaledPerimeter, scaledPerimeterPaint)
                    c.clipOutPath(scaledfillOutline)
                    customFillPaint.color = customFillColor
                    customFillPaint.shader =
                        if (customFillColor != black && customFillGradColor != black) LinearGradient(
                            levelRect.right, 0f, 0f, levelRect.bottom,
                            customFillColor, customFillGradColor,
                            Shader.TileMode.CLAMP
                        ) else null
                    c.drawPath(
                        levelPath,
                        if (customBlendColor && customFillColor != black) customFillPaint else defaultFillPaint
                    )
                    c.clipPath(scaledFill)
                }
            }
            if (!charging && !showPercent) {
                if (batteryLevel in 71..100) {
                    c.drawPath(scaledNgguyu, fillPaint)
                }
                if (batteryLevel in 41..70) {
                    c.drawPath(scaledMingkem, fillPaint)
                }
                if (batteryLevel in 0..40) {
                    c.drawPath(scaledMrengut, fillPaint)
                }
            }

            // Show colorError below this level
            if (batteryLevel <= CRITICAL_LEVEL && !charging && !customFillRainbow) {
                c.save()
                c.clipPath(scaledFill)
                c.drawPath(levelPath, fillPaint)
                c.restore()
            }
        }

        if (charging) {
            c.clipOutPath(scaledBolt)
            if (invertFillIcon) {
                c.drawPath(scaledBolt, fillPaint)
            } else {
                c.drawPath(scaledBolt, fillPaint)
            }
        } else if (powerSaveEnabled) {
            // If power save is enabled draw the perimeter path with colorError
            c.drawPath(scaledErrorPerimeter, powerSavePaint)
            // And draw the plus sign on top of the fill
            if (!showPercent) {
                c.drawPath(scaledPlus, powerSavePaint)
            }
        }
        c.restore()

        if (showPercent && !charging) {
            textPaint.textSize = bounds.width() * 0.24f
            val textHeight = +textPaint.fontMetrics.ascent
            val pctX = (bounds.width() + textHeight) * 0.56f
            val pctY = bounds.height() * 0.65f

            textPaint.color = fillColor
            if (isRotation) {
                c.rotate(180f, pctX, pctY * 0.78f)
            }
            c.drawText(batteryLevel.toString(), pctX, pctY, textPaint)
            c.restore()
        }
    }

    private fun batteryColorForLevel(level: Int): Int {
        return when {
            charging || powerSaveEnabled -> fillColor
            else -> getColorForLevel(level)
        }
    }

    private fun getColorForLevel(level: Int): Int {
        var thresh: Int
        var color = 0
        var i = 0
        while (i < colorLevels.size) {
            thresh = colorLevels[i]
            color = colorLevels[i + 1]
            if (level <= thresh) {

                // Respect tinting for "normal" level
                return if (i == colorLevels.size - 2) {
                    fillColor
                } else {
                    color
                }
            }
            i += 2
        }
        return color
    }

    /**
     * Alpha is unused internally, and should be defined in the colors passed to {@link setColors}.
     * Further, setting an alpha for a dual tone battery meter doesn't make sense without bounds
     * defining the minimum background fill alpha. This is because fill + background must be equal
     * to the net alpha passed in here.
     */
    override fun setAlpha(alpha: Int) {
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        fillPaint.colorFilter = colorFilter
        fillColorStrokePaint.colorFilter = colorFilter
        dualToneBackgroundFill.colorFilter = colorFilter
    }

    /**
     * Deprecated, but required by Drawable
     */
    @Deprecated(
        "Deprecated in Java",
        ReplaceWith("PixelFormat.OPAQUE", "android.graphics.PixelFormat"),
    )
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    override fun getIntrinsicHeight(): Int {
        return intrinsicHeight
    }

    override fun getIntrinsicWidth(): Int {
        return intrinsicWidth
    }

    /**
     * Set the fill level
     */
    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
    override fun setBatteryLevel(l: Int) {
        // invertFillIcon = if (l >= 67) true else if (l <= 33) false else invertFillIcon
        batteryLevel = l
        levelColor = batteryColorForLevel(batteryLevel)
        invalidateSelf()
    }

    fun getBatteryLevel(): Int {
        return batteryLevel
    }

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        updateSize()
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        padding.left = left
        padding.top = top
        padding.right = right
        padding.bottom = bottom

        updateSize()
    }

    override fun setColors(fgColor: Int, bgColor: Int, singleToneColor: Int) {
        fillColor = if (dualTone) fgColor else singleToneColor

        fillPaint.color = fillColor
        defaultFillPaint.color = fillColor
        defaultFillPaint.alpha = 85
        fillColorStrokePaint.color = fillColor

        scaledFillPaint.color = fillColor
        scaledPerimeterPaint.color = fillColor
        scaledPerimeterPaintDef.color = fillColor

        backgroundColor = bgColor
        dualToneBackgroundFill.color = bgColor

        // Also update the level color, since fillColor may have changed
        levelColor = batteryColorForLevel(batteryLevel)

        invalidateSelf()
    }

    private fun postInvalidate() {
        unscheduleSelf(invalidateRunnable)
        scheduleSelf(invalidateRunnable, 0)
    }

    private fun updateSize() {
        val b = bounds
        if (b.isEmpty) {
            scaleMatrix.setScale(1f, 1f)
        } else {
            scaleMatrix.setScale((b.right / WIDTH), (b.bottom / HEIGHT))
        }

        perimeterPath.transform(scaleMatrix, scaledPerimeter)
        errorPerimeterPath.transform(scaleMatrix, scaledErrorPerimeter)
        fillMask.transform(scaleMatrix, scaledFill)
        scaledFill.computeBounds(fillRect, true)
        fillOutlinePath.transform(scaleMatrix, scaledfillOutline)
        fillMask1.transform(scaleMatrix, scaledFill1)
        fillMask2.transform(scaleMatrix, scaledFill2)
        fillMask3.transform(scaleMatrix, scaledFill3)
        fillMask4.transform(scaleMatrix, scaledFill4)
        fillMask5.transform(scaleMatrix, scaledFill5)
        fillMask6.transform(scaleMatrix, scaledFill6)
        fillMask7.transform(scaleMatrix, scaledFill7)
        fillMask8.transform(scaleMatrix, scaledFill8)
        fillMask9.transform(scaleMatrix, scaledFill9)
        fillMask10.transform(scaleMatrix, scaledFill10)
        fillNgguyu.transform(scaleMatrix, scaledNgguyu)
        fillMingkem.transform(scaleMatrix, scaledMingkem)
        fillMrengut.transform(scaleMatrix, scaledMrengut)
        boltPath.transform(scaleMatrix, scaledBolt)
        plusPath.transform(scaleMatrix, scaledPlus)

        // It is expected that this view only ever scale by the same factor in each dimension, so
        // just pick one to scale the strokeWidths
        val scaledStrokeWidth =
            (b.right / WIDTH * PROTECTION_STROKE_WIDTH).coerceAtLeast(PROTECTION_MIN_STROKE_WIDTH)

        fillColorStrokePaint.strokeWidth = scaledStrokeWidth
        fillColorStrokeProtection.strokeWidth = scaledStrokeWidth
    }

    @SuppressLint("RestrictedApi")
    private fun loadPaths() {
        val pathString = modRes.getString(R.string.config_landscapeBatteryPerimeterPathI)
        perimeterPath.set(PathParser.createPathFromPathData(pathString))
        perimeterPath.computeBounds(RectF(), true)

        val errorPathString = modRes.getString(R.string.config_landscapeBatteryErrorPerimeterPathI)
        errorPerimeterPath.set(PathParser.createPathFromPathData(errorPathString))
        errorPerimeterPath.computeBounds(RectF(), true)

        val fillMaskString = modRes.getString(R.string.config_landscapeBatteryFillMaskI)
        fillMask.set(PathParser.createPathFromPathData(fillMaskString))
        // Set the fill rect so we can calculate the fill properly
        fillMask.computeBounds(fillRect, true)

        val fillOutlinePathString =
            modRes.getString(R.string.config_landscapeBatteryFillOutlinePathI)
        fillOutlinePath.set(PathParser.createPathFromPathData(fillOutlinePathString))
        fillOutlinePath.computeBounds(RectF(), true)

        val fillMask1String = modRes.getString(R.string.config_landscapeBatteryFillMaskI1)
        fillMask1.set(PathParser.createPathFromPathData(fillMask1String))
        fillMask1.computeBounds(RectF(), true)

        val fillMask2String = modRes.getString(R.string.config_landscapeBatteryFillMaskI2)
        fillMask2.set(PathParser.createPathFromPathData(fillMask2String))
        fillMask2.computeBounds(RectF(), true)

        val fillMask3String = modRes.getString(R.string.config_landscapeBatteryFillMaskI3)
        fillMask3.set(PathParser.createPathFromPathData(fillMask3String))
        fillMask3.computeBounds(RectF(), true)

        val fillMask4String = modRes.getString(R.string.config_landscapeBatteryFillMaskI4)
        fillMask4.set(PathParser.createPathFromPathData(fillMask4String))
        fillMask4.computeBounds(RectF(), true)

        val fillMask5String = modRes.getString(R.string.config_landscapeBatteryFillMaskI5)
        fillMask5.set(PathParser.createPathFromPathData(fillMask5String))
        fillMask5.computeBounds(RectF(), true)

        val fillMask6String = modRes.getString(R.string.config_landscapeBatteryFillMaskI6)
        fillMask6.set(PathParser.createPathFromPathData(fillMask6String))
        fillMask6.computeBounds(RectF(), true)

        val fillMask7String = modRes.getString(R.string.config_landscapeBatteryFillMaskI7)
        fillMask7.set(PathParser.createPathFromPathData(fillMask7String))
        fillMask7.computeBounds(RectF(), true)

        val fillMask8String = modRes.getString(R.string.config_landscapeBatteryFillMaskI8)
        fillMask8.set(PathParser.createPathFromPathData(fillMask8String))
        fillMask8.computeBounds(RectF(), true)

        val fillMask9String = modRes.getString(R.string.config_landscapeBatteryFillMaskI9)
        fillMask9.set(PathParser.createPathFromPathData(fillMask9String))
        fillMask9.computeBounds(RectF(), true)

        val fillMask10String = modRes.getString(R.string.config_landscapeBatteryFillMaskI10)
        fillMask10.set(PathParser.createPathFromPathData(fillMask10String))
        fillMask10.computeBounds(RectF(), true)

        val fillNgguyuString = modRes.getString(R.string.config_landscapeBatteryFillNgguyuI)
        fillNgguyu.set(PathParser.createPathFromPathData(fillNgguyuString))
        fillNgguyu.computeBounds(RectF(), true)

        val fillMingkemString = modRes.getString(R.string.config_landscapeBatteryFillMingkemI)
        fillMingkem.set(PathParser.createPathFromPathData(fillMingkemString))
        fillMingkem.computeBounds(RectF(), true)

        val fillMrengutString = modRes.getString(R.string.config_landscapeBatteryFillMrengutI)
        fillMrengut.set(PathParser.createPathFromPathData(fillMrengutString))
        fillMrengut.computeBounds(RectF(), true)

        val boltPathString = modRes.getString(R.string.config_landscapeBatteryBoltPathI)
        boltPath.set(PathParser.createPathFromPathData(boltPathString))

        val plusPathString = modRes.getString(R.string.config_landscapeBatteryPowersavePathI)
        plusPath.set(PathParser.createPathFromPathData(plusPathString))

        dualTone = false
    }

    companion object {
        private val TAG = LandscapeBatteryI::class.java.simpleName
        private const val WIDTH = 23f
        private const val HEIGHT = 13f
        private const val CRITICAL_LEVEL = 15

        // On a 22x12 grid, how wide to make the fill protection stroke.
        // Scales when our size changes
        private const val PROTECTION_STROKE_WIDTH = 3f

        // Arbitrarily chosen for visibility at small sizes
        private const val PROTECTION_MIN_STROKE_WIDTH = 6f
    }
}
