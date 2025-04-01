package com.drdisagree.iconify.ui.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.BatteryManager
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.FRAGMENT_BACK_BUTTON_DELAY
import com.drdisagree.iconify.data.common.Preferences.BATTERY_STYLE_DOTTED_CIRCLE
import com.drdisagree.iconify.data.common.XposedConst.STATUSBAR_LOGO_FILE
import com.drdisagree.iconify.xposed.modules.batterystyles.BatteryDrawable
import com.drdisagree.iconify.xposed.modules.batterystyles.CircleBattery
import com.drdisagree.iconify.xposed.modules.batterystyles.CircleFilledBattery
import com.drdisagree.iconify.xposed.modules.batterystyles.DefaultBattery
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBattery
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryA
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryB
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryC
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryColorOS
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryD
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryE
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryF
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryG
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryH
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryI
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryJ
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryK
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryKim
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryL
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryM
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryMIUIPill
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryN
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryO
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatterySmiley
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryStyleA
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryStyleB
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryiOS15
import com.drdisagree.iconify.xposed.modules.batterystyles.LandscapeBatteryiOS16
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryAiroo
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryCapsule
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryLorn
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryMx
import com.drdisagree.iconify.xposed.modules.batterystyles.PortraitBatteryOrigami
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBattery
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBatteryColorOS
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBatteryStyleA
import com.drdisagree.iconify.xposed.modules.batterystyles.RLandscapeBatteryStyleB
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toCircularDrawable

object ViewHelper {

    fun disableNestedScrolling(viewPager: ViewPager2) {
        var recyclerView: RecyclerView? = null

        for (i in 0 until viewPager.childCount) {
            if (viewPager.getChildAt(i) is RecyclerView) {
                recyclerView = viewPager.getChildAt(i) as RecyclerView
                break
            }
        }

        if (recyclerView != null) {
            recyclerView.isNestedScrollingEnabled = false
        }
    }

    fun setHeader(context: Context, toolbar: Toolbar, title: Any) {
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        context.supportActionBar?.setDisplayShowHomeEnabled(true)
        if (title is Int) {
            toolbar.setTitle(title)
        } else if (title is String) {
            toolbar.setTitle(title)
        }
    }

    fun setHeader(
        context: Context,
        fragmentManager: FragmentManager,
        toolbar: Toolbar,
        title: Int
    ) {
        setHeader(
            context,
            fragmentManager,
            toolbar,
            context.resources.getString(title)
        )
    }

    fun setHeader(
        context: Context,
        fragmentManager: FragmentManager,
        toolbar: Toolbar,
        title: String
    ) {
        toolbar.setTitle(title)
        (context as AppCompatActivity).setSupportActionBar(toolbar)
        context.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        context.supportActionBar?.setDisplayShowHomeEnabled(true)

        toolbar.setNavigationOnClickListener {
            Handler(Looper.getMainLooper()).postDelayed(
                { fragmentManager.popBackStack() }, FRAGMENT_BACK_BUTTON_DELAY.toLong()
            )
        }
    }

    fun dp2px(dp: Float): Int {
        return dp2px(dp.toInt())
    }

    fun dp2px(dp: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            appContext.resources.displayMetrics
        ).toInt()
    }

    fun getBatteryDrawables(context: Context): Array<Drawable> {
        val batteryColor = appContext.getColor(R.color.textColorPrimary)

        val batteryDrawables = arrayOf<Drawable>(
            DefaultBattery(context, batteryColor),
            DefaultBattery(context, batteryColor),
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
            LandscapeBatteryKim(context, batteryColor)
        )

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        var wasFilledCircleBattery = false

        for (batteryIcon in batteryDrawables) {
            val alpha = (255 * 0.4).toInt()
            val batteryColorWithOpacity = ColorUtils.setAlphaComponent(batteryColor, alpha)

            (batteryIcon as BatteryDrawable).setBatteryLevel(batteryLevel)
            batteryIcon.setColors(batteryColor, batteryColorWithOpacity, batteryColor)

            if (batteryIcon is CircleBattery) {
                if (wasFilledCircleBattery) {
                    batteryIcon.setMeterStyle(BATTERY_STYLE_DOTTED_CIRCLE)
                } else {
                    wasFilledCircleBattery = true
                }
            } else if (batteryIcon === batteryDrawables[1]) {
                batteryDrawables[1] = getRotateDrawable(batteryIcon, 90f)
            } else if (batteryIcon === batteryDrawables[2]) {
                batteryDrawables[2] = getRotateDrawable(batteryIcon, -90f)
            }
        }

        return batteryDrawables
    }

    fun getChargingIcons(context: Context): Array<Drawable> {
        val chargingIcons = arrayOf(
            getDrawable(context, R.drawable.ic_charging_bold)!!,  // Bold
            getDrawable(context, R.drawable.ic_charging_asus)!!,  // Asus
            getDrawable(context, R.drawable.ic_charging_buddy)!!,  // Buddy
            getDrawable(context, R.drawable.ic_charging_evplug)!!,  // EV Plug
            getDrawable(context, R.drawable.ic_charging_idc)!!,  // IDC
            getDrawable(context, R.drawable.ic_charging_ios)!!,  // IOS
            getDrawable(context, R.drawable.ic_charging_koplak)!!,  // Koplak
            getDrawable(context, R.drawable.ic_charging_miui)!!,  // MIUI
            getDrawable(context, R.drawable.ic_charging_mmk)!!,  // MMK
            getDrawable(context, R.drawable.ic_charging_moto)!!,  // Moto
            getDrawable(context, R.drawable.ic_charging_nokia)!!,  // Nokia
            getDrawable(context, R.drawable.ic_charging_plug)!!,  // Plug
            getDrawable(context, R.drawable.ic_charging_powercable)!!,  // Power Cable
            getDrawable(context, R.drawable.ic_charging_powercord)!!,  // Power Cord
            getDrawable(context, R.drawable.ic_charging_powerstation)!!,  // Power Station
            getDrawable(context, R.drawable.ic_charging_realme)!!,  // Realme
            getDrawable(context, R.drawable.ic_charging_soak)!!,  // Soak
            getDrawable(context, R.drawable.ic_charging_stres)!!,  // Stres
            getDrawable(context, R.drawable.ic_charging_strip)!!,  // Strip
            getDrawable(context, R.drawable.ic_charging_usbcable)!!,  // USB Cable
            getDrawable(context, R.drawable.ic_charging_xiaomi)!! // Xiaomi
        )

        val iconColor = appContext.getColor(R.color.textColorPrimary)

        for (chargingIcon in chargingIcons) {
            chargingIcon.setTint(iconColor)
        }

        return chargingIcons
    }

    fun getStatusbarLogoDrawables(context: Context): Array<Drawable> {
        val logoColor = appContext.getColor(R.color.textColorPrimary)

        val predefinedLogos = arrayOf(
            R.drawable.ic_android_logo,
            R.drawable.ic_adidas,
            R.drawable.ic_alien,
            R.drawable.ic_apple_logo,
            R.drawable.ic_avengers,
            R.drawable.ic_batman,
            R.drawable.ic_batman_tdk,
            R.drawable.ic_beats,
            R.drawable.ic_biohazard,
            R.drawable.ic_blackberry,
            R.drawable.ic_cannabis,
            R.drawable.ic_emoticon_cool,
            R.drawable.ic_emoticon_devil,
            R.drawable.ic_fire,
            R.drawable.ic_heart,
            R.drawable.ic_nike,
            R.drawable.ic_pac_man,
            R.drawable.ic_puma,
            R.drawable.ic_rog,
            R.drawable.ic_spiderman,
            R.drawable.ic_superman,
            R.drawable.ic_windows,
            R.drawable.ic_xbox,
            R.drawable.ic_ghost,
            R.drawable.ic_ninja,
            R.drawable.ic_robot,
            R.drawable.ic_ironman,
            R.drawable.ic_captain_america,
            R.drawable.ic_flash,
            R.drawable.ic_tux_logo,
            R.drawable.ic_ubuntu_logo,
            R.drawable.ic_mint_logo,
            R.drawable.ic_amogus
        )

        val logoDrawables = predefinedLogos.map { getDrawable(context, it)!! }.toMutableList()
        val customDrawable = try {
            ImageDecoder.decodeDrawable(
                ImageDecoder.createSource(STATUSBAR_LOGO_FILE)
            ).toCircularDrawable(context)
        } catch (_: Throwable) {
            @Suppress("DEPRECATION")
            getDrawable(context, R.drawable.ic_upload_file)?.apply { setTint(logoColor) }
        }

        logoDrawables.forEach { it.setTint(logoColor) }
        customDrawable?.let { logoDrawables.add(it) }

        return logoDrawables.toTypedArray()
    }

    private fun getRotateDrawable(d: Drawable, angle: Float): Drawable {
        val arD = arrayOf(d)
        return object : LayerDrawable(arD) {
            override fun draw(canvas: Canvas) {
                canvas.save()
                canvas.rotate(
                    angle,
                    d.getBounds().width().toFloat() / 2,
                    d.getBounds().height().toFloat() / 2
                )
                super.draw(canvas)
                canvas.restore()
            }

            override fun getConstantState(): ConstantState {
                return RotateDrawableConstantState(d, angle)
            }
        }
    }

    private class RotateDrawableConstantState(
        private val drawable: Drawable,
        private val angle: Float
    ) : Drawable.ConstantState() {
        override fun newDrawable(): Drawable {
            return getRotateDrawable(drawable.constantState?.newDrawable() ?: drawable, angle)
        }

        override fun getChangingConfigurations(): Int {
            return drawable.changingConfigurations
        }
    }

    private fun getDrawable(context: Context, @DrawableRes batteryRes: Int): Drawable? {
        return ResourcesCompat.getDrawable(context.resources, batteryRes, context.theme)
    }

    fun setTextRecursively(viewGroup: ViewGroup, text: String?) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                setTextRecursively(child, text)
            } else if (child is TextView) {
                child.text = text
            }
        }
    }

    fun applyTextSizeRecursively(viewGroup: ViewGroup, textSize: Int) {
        for (i in 0 until viewGroup.childCount) {
            val child = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                applyTextSizeRecursively(child, textSize)
            } else if (child is TextView) {
                child.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize.toFloat())
            }
        }
    }

}
