package com.drdisagree.iconify.features.xposed.statusbar.batterystyle.components

import android.content.Context
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringArrayResource
import androidx.core.graphics.drawable.toBitmap
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.models.SingleIconPreview
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun rememberChargingIconItems(context: Context): List<SingleIconPreview> {
    val labels = stringArrayResource(R.array.custom_charging_icon_style_entries)
    val values = stringArrayResource(R.array.custom_charging_icon_style_values)

    val chargingIconItems = remember { mutableStateOf<List<SingleIconPreview>>(emptyList()) }

    LaunchedEffect(Unit) {
        val bitmaps = loadChargingIconBitmaps(context)

        chargingIconItems.value = labels.mapIndexed { index, label ->
            SingleIconPreview(
                label = label,
                value = values.getOrNull(index) ?: index.toString(),
                bitmap = bitmaps.getOrNull(index)
            )
        }
    }

    return chargingIconItems.value
}

private suspend fun loadChargingIconBitmaps(context: Context): List<ImageBitmap> =
    withContext(Dispatchers.IO) {
        val iconColor = context.getColor(R.color.textColorPrimary)

        return@withContext arrayOf(
            R.drawable.ic_charging_bold,  // Bold
            R.drawable.ic_charging_asus,  // Asus
            R.drawable.ic_charging_buddy,  // Buddy
            R.drawable.ic_charging_evplug,  // EV Plug
            R.drawable.ic_charging_idc,  // IDC
            R.drawable.ic_charging_ios,  // IOS
            R.drawable.ic_charging_koplak,  // Koplak
            R.drawable.ic_charging_miui,  // MIUI
            R.drawable.ic_charging_mmk,  // MMK
            R.drawable.ic_charging_moto,  // Moto
            R.drawable.ic_charging_nokia,  // Nokia
            R.drawable.ic_charging_plug,  // Plug
            R.drawable.ic_charging_powercable,  // Power Cable
            R.drawable.ic_charging_powercord,  // Power Cord
            R.drawable.ic_charging_powerstation,  // Power Station
            R.drawable.ic_charging_realme,  // Realme
            R.drawable.ic_charging_soak,  // Soak
            R.drawable.ic_charging_stres,  // Stres
            R.drawable.ic_charging_strip,  // Strip
            R.drawable.ic_charging_usbcable,  // USB Cable
            R.drawable.ic_charging_xiaomi // Xiaomi
        ).map { chargingIconResId ->
            AppCompatResources.getDrawable(context, chargingIconResId)!!.also {
                it.setTint(iconColor)
            }.toBitmap().asImageBitmap()
        }
    }