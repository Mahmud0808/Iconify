package com.drdisagree.iconify

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.createBitmap
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.drdisagree.iconify.data.common.Preferences
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.CircleBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.CircleFilledBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.DefaultBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryA
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryB
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryC
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryColorOS
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryD
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryE
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryF
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryG
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryH
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryI
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryJ
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryK
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryKim
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryL
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryM
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryMIUIPill
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryN
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryO
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryOneUI7
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatterySmiley
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryStyleA
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryStyleB
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryiOS15
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.LandscapeBatteryiOS16
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryAiroo
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryCapsule
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryLorn
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryMx
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.PortraitBatteryOrigami
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBattery
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBatteryColorOS
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBatteryStyleA
import com.drdisagree.iconify.xposed.modules.statusbar.batterystyles.RLandscapeBatteryStyleB
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlin.math.roundToInt

@RunWith(AndroidJUnit4::class)
class BatteryStyleExport {

    @Test
    fun exportBatteryStyles() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val labels = context.resources.getStringArray(R.array.custom_battery_style_entries)
        val color = Color.BLACK
        val faded = ColorUtils.setAlphaComponent(color, (255 * 0.4).toInt())
        val outDir = File(context.getExternalFilesDir(null), "battery-export").apply {
            deleteRecursively()
            mkdirs()
        }

        instrumentation.runOnMainSync {
            var circleSeen = false
            val drawables = listOf(
                DefaultBattery(context, color),
                RLandscapeBattery(context, color),
                LandscapeBattery(context, color),
                PortraitBatteryCapsule(context, color),
                PortraitBatteryLorn(context, color),
                PortraitBatteryMx(context, color),
                PortraitBatteryAiroo(context, color),
                RLandscapeBatteryStyleA(context, color),
                LandscapeBatteryStyleA(context, color),
                RLandscapeBatteryStyleB(context, color),
                LandscapeBatteryStyleB(context, color),
                LandscapeBatteryiOS15(context, color),
                LandscapeBatteryiOS16(context, color),
                PortraitBatteryOrigami(context, color),
                LandscapeBatterySmiley(context, color),
                LandscapeBatteryMIUIPill(context, color),
                LandscapeBatteryColorOS(context, color),
                RLandscapeBatteryColorOS(context, color),
                LandscapeBatteryA(context, color),
                LandscapeBatteryB(context, color),
                LandscapeBatteryC(context, color),
                LandscapeBatteryD(context, color),
                LandscapeBatteryE(context, color),
                LandscapeBatteryF(context, color),
                LandscapeBatteryG(context, color),
                LandscapeBatteryH(context, color),
                LandscapeBatteryI(context, color),
                LandscapeBatteryJ(context, color),
                LandscapeBatteryK(context, color),
                LandscapeBatteryL(context, color),
                LandscapeBatteryM(context, color),
                LandscapeBatteryN(context, color),
                LandscapeBatteryO(context, color),
                CircleBattery(context, color),
                CircleBattery(context, color),
                CircleFilledBattery(context, color),
                LandscapeBatteryKim(context, color),
                LandscapeBatteryOneUI7(context, color),
            )

            val index = StringBuilder()
            drawables.forEachIndexed { i, drawable ->
                if (drawable is CircleBattery) {
                    if (circleSeen) {
                        drawable.setMeterStyle(Preferences.BATTERY_STYLE_DOTTED_CIRCLE)
                    } else {
                        circleSeen = true
                    }
                }

                drawable.setBatteryLevel(LEVEL)
                drawable.setColors(color, faded, color)

                val fallback = (FALLBACK_DP * context.resources.displayMetrics.density).roundToInt()
                val nativeWidth = drawable.intrinsicWidth.takeIf { it > 0 } ?: fallback
                val nativeHeight = drawable.intrinsicHeight.takeIf { it > 0 } ?: fallback
                val scale = HEIGHT_PX.toFloat() / nativeHeight
                val bitmap = createBitmap((nativeWidth * scale).roundToInt(), HEIGHT_PX)
                drawable.bounds = Rect(0, 0, nativeWidth, nativeHeight)
                drawable.draw(Canvas(bitmap).apply { scale(scale, scale) })

                File(outDir, "%02d.png".format(i)).outputStream().use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                index.append(i).append('\t').append(labels.getOrElse(i) { "" }).append('\n')
            }

            File(outDir, "labels.tsv").writeText(index.toString())
        }
    }

    private companion object {
        const val LEVEL = 75
        const val HEIGHT_PX = 240
        const val FALLBACK_DP = 13
    }
}
