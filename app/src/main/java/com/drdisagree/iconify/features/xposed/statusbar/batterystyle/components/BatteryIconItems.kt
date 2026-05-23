package com.drdisagree.iconify.features.xposed.statusbar.batterystyle.components

import android.content.Context
import android.os.BatteryManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringArrayResource
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.drawable.toBitmap
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Preferences
import com.drdisagree.iconify.data.models.SingleIconPreview
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberBatteryIconItems(context: Context): List<SingleIconPreview> {
    val labels = stringArrayResource(R.array.custom_battery_style_entries)
    val values = stringArrayResource(R.array.custom_battery_style_values)

    val batteryIconItems = remember { mutableStateOf<List<SingleIconPreview>>(emptyList()) }

    LaunchedEffect(Unit) {
        val bitmaps = loadBatteryIconBitmaps(context)
        batteryIconItems.value = labels.mapIndexed { index, label ->
            SingleIconPreview(
                label = label,
                value = values.getOrNull(index) ?: index.toString(),
                bitmap = bitmaps.getOrNull(index)
            )
        }
    }

    return batteryIconItems.value
}

private suspend fun loadBatteryIconBitmaps(context: Context): List<ImageBitmap> =
    withContext(Dispatchers.IO) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val batteryColor = context.getColor(R.color.textColorPrimary)
        var wasFilledCircleBattery = false
        val alpha = (255 * 0.4).toInt()
        val batteryColorWithOpacity = ColorUtils.setAlphaComponent(batteryColor, alpha)

        return@withContext arrayOf(
            DefaultBattery(context, batteryColor),
            RLandscapeBattery(context, batteryColor),
            LandscapeBattery(context, batteryColor),
            PortraitBatteryCapsule(context, batteryColor),
            PortraitBatteryLorn(context, batteryColor),
            PortraitBatteryMx(context, batteryColor),
            PortraitBatteryAiroo(context, batteryColor),
            RLandscapeBatteryStyleA(context, batteryColor),
            LandscapeBatteryStyleA(context, batteryColor),
            RLandscapeBatteryStyleB(context, batteryColor),
            LandscapeBatteryStyleB(context, batteryColor),
            LandscapeBatteryiOS15(context, batteryColor),
            LandscapeBatteryiOS16(context, batteryColor),
            PortraitBatteryOrigami(context, batteryColor),
            LandscapeBatterySmiley(context, batteryColor),
            LandscapeBatteryMIUIPill(context, batteryColor),
            LandscapeBatteryColorOS(context, batteryColor),
            RLandscapeBatteryColorOS(context, batteryColor),
            LandscapeBatteryA(context, batteryColor),
            LandscapeBatteryB(context, batteryColor),
            LandscapeBatteryC(context, batteryColor),
            LandscapeBatteryD(context, batteryColor),
            LandscapeBatteryE(context, batteryColor),
            LandscapeBatteryF(context, batteryColor),
            LandscapeBatteryG(context, batteryColor),
            LandscapeBatteryH(context, batteryColor),
            LandscapeBatteryI(context, batteryColor),
            LandscapeBatteryJ(context, batteryColor),
            LandscapeBatteryK(context, batteryColor),
            LandscapeBatteryL(context, batteryColor),
            LandscapeBatteryM(context, batteryColor),
            LandscapeBatteryN(context, batteryColor),
            LandscapeBatteryO(context, batteryColor),
            CircleBattery(context, batteryColor),
            CircleBattery(context, batteryColor),
            CircleFilledBattery(context, batteryColor),
            LandscapeBatteryKim(context, batteryColor),
            LandscapeBatteryOneUI7(context, batteryColor),
        ).map { batteryIcon ->
            if (batteryIcon is CircleBattery) {
                if (wasFilledCircleBattery) {
                    batteryIcon.setMeterStyle(Preferences.BATTERY_STYLE_DOTTED_CIRCLE)
                } else {
                    wasFilledCircleBattery = true
                }
            }

            batteryIcon.setBatteryLevel(batteryLevel)
            batteryIcon.setColors(batteryColor, batteryColorWithOpacity, batteryColor)

            batteryIcon.toBitmap().asImageBitmap()
        }
    }