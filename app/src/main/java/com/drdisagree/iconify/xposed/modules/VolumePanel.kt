package com.drdisagree.iconify.xposed.modules

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_PERCENTAGE
import com.drdisagree.iconify.common.Preferences.VOLUME_PANEL_SAFETY_WARNING
import com.drdisagree.iconify.config.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.utils.ViewHelper.toPx
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge.hookAllMethods
import de.robv.android.xposed.XposedHelpers.callMethod
import de.robv.android.xposed.XposedHelpers.findClass
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.XposedHelpers.setObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlin.math.ceil

@SuppressLint("DiscouragedApi")
class VolumePanel(context: Context?) : ModPack(context!!) {

    private var showPercentage = false
    private var showWarning = true

    override fun updatePrefs(vararg key: String) {
        if (Xprefs == null) return

        showPercentage = Xprefs!!.getBoolean(VOLUME_PANEL_PERCENTAGE, false)
        showWarning = Xprefs!!.getBoolean(VOLUME_PANEL_SAFETY_WARNING, true)
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        showVolumePercentage(loadPackageParam)
    }

    private fun showVolumePercentage(loadPackageParam: LoadPackageParam) {
        val volumeDialogImplClass = findClass(
            "$SYSTEMUI_PACKAGE.volume.VolumeDialogImpl",
            loadPackageParam.classLoader
        )

        hookAllMethods(volumeDialogImplClass, "initRow", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showPercentage) return

                val rowHeader: TextView = getObjectField(
                    param.args[0],
                    "header"
                ) as TextView

                if ((rowHeader.parent as ViewGroup).findViewById<TextView>(
                        mContext.resources.getIdentifier(
                            "volume_number",
                            "id",
                            mContext.packageName
                        )
                    ) != null
                ) return

                val volumeNumber = createVolumeTextView()
                (rowHeader.parent as ViewGroup).addView(volumeNumber, 0)

                setObjectField(
                    param.args[0],
                    "number",
                    (getObjectField(param.args[0], "view") as View).findViewById(
                        mContext.resources.getIdentifier(
                            "volume_number",
                            "id",
                            mContext.packageName
                        )
                    )
                )
            }
        })

        hookAllMethods(volumeDialogImplClass, "updateVolumeRowH", object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                if (!showPercentage) return

                val volumeNumber: TextView =
                    (getObjectField(param.args[0], "view") as View).findViewById(
                        mContext.resources.getIdentifier(
                            "volume_number",
                            "id",
                            mContext.packageName
                        )
                    ) ?: return

                val mState: Any = getObjectField(param.thisObject, "mState") ?: return

                val ss = callMethod(
                    getObjectField(mState, "states"),
                    "get",
                    getObjectField(param.args[0], "stream")
                ) ?: return

                val levelMax: Int = getObjectField(ss, "levelMax") as Int

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
        })

        hookAllMethods(volumeDialogImplClass, "onShowSafetyWarning", object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (!showWarning) {
                    param.result = null;
                }
            }
        })
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
                ResourcesCompat.getColor(
                    mContext.resources,
                    android.R.color.system_accent1_300,
                    mContext.theme
                )
            )
            text = String.format("%d%%", 0)
        }

        return volumeNumber
    }

    companion object {
        private val TAG = "Iconify - ${VolumePanel::class.java.simpleName}: "
    }
}