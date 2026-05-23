package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.View
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DrawableSize
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callStaticMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.log
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.setField
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class AppIconsInStatusbar(context: Context) : ModPack(context) {

    private var mColoredStatusbarIcon = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            mColoredStatusbarIcon = getBoolean(XposedKey.COLORED_STATUSBAR_ICON)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val notificationIconContainerClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.phone.NotificationIconContainer")
        val iconStateClass =
            findClass($$"$$SYSTEMUI_PACKAGE.statusbar.phone.NotificationIconContainer$IconState")
        val legacyNotificationIconAreaControllerImplClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.LegacyNotificationIconAreaControllerImpl",
            "$SYSTEMUI_PACKAGE.statusbar.phone.NotificationIconAreaController",
            suppressError = true
        )
        val drawableSizeClass = findClass(
            "$SYSTEMUI_PACKAGE.util.drawable.DrawableSize",
            suppressError = true
        )
        val scalingDrawableWrapperClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.ScalingDrawableWrapper")!!
        val statusBarIconViewClass = findClass("$SYSTEMUI_PACKAGE.statusbar.StatusBarIconView")

        fun removeTintForStatusbarIcon(icon: View, isNotification: Boolean = false) {
            if (!mColoredStatusbarIcon) return

            try {
                val pkgName = icon
                    .getField("mIcon")
                    .getField("pkg") as String

                if (isNotification && !pkgName.contains("systemui")) {
                    icon.setField("mCurrentSetColor", 0) // StatusBarIconView.NO_COLOR
                    icon.callMethod("updateIconColor")
                }
            } catch (throwable: Throwable) {
                log(this@AppIconsInStatusbar, throwable)
            }
        }

        fun removeTintForStatusbarIcon(param: MethodHookParam) {
            if (!mColoredStatusbarIcon) return

            val icon = param.args[0] as View
            val isNotification = param.thisObject.getFieldSilently("mNotification") != null

            removeTintForStatusbarIcon(icon, isNotification)
        }

        @Suppress("UNCHECKED_CAST")
        notificationIconContainerClass
            .hookMethod("applyIconStates")
            .runAfter { param ->
                if (!mColoredStatusbarIcon) return@runAfter

                val mIconStates: HashMap<View, Any> = param.thisObject.getField(
                    "mIconStates"
                ) as HashMap<View, Any>

                for (icon in mIconStates.keys) {
                    removeTintForStatusbarIcon(icon)
                }
            }

        iconStateClass
            .hookMethod(
                "initFrom",
                "applyToView"
            )
            .runAfter { param ->
                if (!mColoredStatusbarIcon) return@runAfter

                removeTintForStatusbarIcon(param)
            }

        statusBarIconViewClass
            .hookMethod("updateIconColor")
            .runBefore { param ->
                if (!mColoredStatusbarIcon) return@runBefore

                val isNotification = param.thisObject.getFieldSilently("mNotification") != null

                if (isNotification) {
                    param.result = null
                }
            }

        legacyNotificationIconAreaControllerImplClass
            .hookMethod("updateTintForIcon")
            .runAfter { param ->
                if (!mColoredStatusbarIcon) return@runAfter

                removeTintForStatusbarIcon(param)

                val view = param.args[0] as? View
                view.callMethod("setStaticDrawableColor", 0) // StatusBarIconView.NO_COLOR
                view.callMethod("setDecorColor", Color.WHITE)
            }

        statusBarIconViewClass
            .hookMethod("getIcon")
            .runBefore { param ->
                if (!mColoredStatusbarIcon) return@runBefore

                val hasContext = param.args.size > 1
                val hasSysUiContext = param.args.size > 2
                val statusBarNotification = param.thisObject.getFieldSilently("mNotification")

                val sysuiContext = if (hasSysUiContext) param.args[0] as Context else mContext
                val context = if (hasContext)
                    if (hasSysUiContext)
                        param.args[1] as Context
                    else
                        param.args[0] as Context
                else
                    statusBarNotification.callMethod(
                        "getPackageContext",
                        mContext
                    ) as? Context ?: mContext
                val statusBarIcon = param.args[param.args.lastIndex]

                var icon: Drawable
                val res = sysuiContext.resources
                val pkgName = statusBarIcon.getField("pkg") as String

                if (listOf("com.android", "systemui").any { pkgName.contains(it) }) {
                    return@runBefore
                }

                try {
                    icon = context.packageManager.getApplicationIcon(pkgName)
                } catch (_: Throwable) {
                    return@runBefore
                }

                val isLowRamDevice =
                    ActivityManager::class.java.callStaticMethod("isLowRamDeviceStatic") as Boolean

                val maxIconSize = res.getDimensionPixelSize(
                    res.getIdentifier(
                        if (isLowRamDevice) {
                            "notification_small_icon_size_low_ram"
                        } else {
                            "notification_small_icon_size"
                        },
                        "dimen",
                        FRAMEWORK_PACKAGE
                    )
                )

                icon = if (drawableSizeClass != null) {
                    drawableSizeClass.callStaticMethod(
                        "downscaleToSize",
                        res,
                        icon,
                        maxIconSize,
                        maxIconSize
                    )
                } else {
                    DrawableSize.downscaleToSize(
                        res,
                        icon,
                        maxIconSize,
                        maxIconSize
                    )
                } as Drawable

                val typedValue = TypedValue()
                res.getValue(
                    res.getIdentifier(
                        "status_bar_icon_scale_factor",
                        "dimen",
                        SYSTEMUI_PACKAGE
                    ),
                    typedValue,
                    true
                )
                val scaleFactor = typedValue.float

                if (scaleFactor == 1f) {
                    param.result = icon
                } else {
                    param.result = scalingDrawableWrapperClass.getConstructor(
                        Drawable::class.java,
                        Float::class.javaPrimitiveType
                    ).newInstance(icon, scaleFactor)
                }
            }
    }
}