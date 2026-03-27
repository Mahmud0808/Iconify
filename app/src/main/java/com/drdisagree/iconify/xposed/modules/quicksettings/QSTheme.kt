package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.common.XposedConst.HEADER_IMAGE_FILE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage
import androidx.core.graphics.toColorInt

@SuppressLint("DiscouragedApi")
class QSTheme(context: Context) : ModPack(context) {

    private var customQsTheme = false
    private var bgColor = Color.WHITE
    private var iconColor = Color.WHITE
    private var iconBgColor = Color.WHITE
    private var labelColor = Color.WHITE
    private var secondaryLabelColor = Color.WHITE

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            customQsTheme = getBoolean(XposedKey.CUSTOM_QS_THEME)
            bgColor = getString(XposedKey.CUSTOM_QS_THEME)!!.toColorInt()
            iconColor = getString(XposedKey.CUSTOM_QS_THEME)!!.toColorInt()
            iconBgColor = getString(XposedKey.CUSTOM_QS_THEME)!!.toColorInt()
            labelColor = getString(XposedKey.CUSTOM_QS_THEME)!!.toColorInt()
            secondaryLabelColor = getString(XposedKey.CUSTOM_QS_THEME)!!.toColorInt()
        }
    }

    override fun handleLoadPackage(loadPackageParam: XC_LoadPackage.LoadPackageParam) {
        val colorKtClass = findClass("androidx.compose.ui.graphics.ColorKt")
        val tileColorsClass =
            findClass("$SYSTEMUI_PACKAGE.qs.panels.ui.compose.infinitegrid.TileColors")

        tileColorsClass
            .hookConstructor()
            .runAfter { param ->
                if (!customQsTheme) return@runAfter

                param.thisObject.apply {
                    setField("background", colorKtClass.callStaticMethod("Color", bgColor))
                    setField("icon", colorKtClass.callStaticMethod("Color", iconColor))
                    setField("iconBackground", colorKtClass.callStaticMethod("Color", iconBgColor))
                    setField("label", colorKtClass.callStaticMethod("Color", labelColor))
                    setField(
                        "secondaryLabel",
                        colorKtClass.callStaticMethod("Color", secondaryLabelColor)
                    )
                }
            }
    }
}