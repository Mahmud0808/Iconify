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
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.log.log
import com.drdisagree.iconify.xposed.core.reflect.callMethod
import com.drdisagree.iconify.xposed.core.reflect.callStaticMethod
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getField
import com.drdisagree.iconify.xposed.core.reflect.getFieldSilently
import com.drdisagree.iconify.xposed.core.reflect.setField
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.DrawableSize
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import io.github.libxposed.api.XposedInterface

@SuppressLint("DiscouragedApi")
class AppIconsInStatusbar(context: Context) : ModPack(context) {

    private var mColoredStatusbarIcon = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            mColoredStatusbarIcon = getBoolean(XposedKey.COLORED_STATUSBAR_ICON)
        }
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
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

        fun removeTintForIconOf(chain: XposedInterface.Chain) {
            if (!mColoredStatusbarIcon) return

            val icon = chain.args[0] as View
            val isNotification = chain.thisObject.getFieldSilently("mNotification") != null

            removeTintForStatusbarIcon(icon, isNotification)
        }

        @Suppress("UNCHECKED_CAST")
        notificationIconContainerClass
            .hookMethod("applyIconStates")
            .runAfter { chain, _ ->
                if (!mColoredStatusbarIcon) return@runAfter

                val mIconStates: HashMap<View, Any> = chain.thisObject.getField(
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
            .runAfter { chain, _ ->
                if (!mColoredStatusbarIcon) return@runAfter

                removeTintForIconOf(chain)
            }

        statusBarIconViewClass
            .hookMethod("updateIconColor")
            .intercept { chain ->
                val isNotification = chain.thisObject.getFieldSilently("mNotification") != null
                if (mColoredStatusbarIcon && isNotification) null else chain.proceed()
            }

        legacyNotificationIconAreaControllerImplClass
            .hookMethod("updateTintForIcon")
            .runAfter { chain, _ ->
                if (!mColoredStatusbarIcon) return@runAfter

                removeTintForIconOf(chain)

                val view = chain.args[0] as? View
                view.callMethod("setStaticDrawableColor", 0) // StatusBarIconView.NO_COLOR
                view.callMethod("setDecorColor", Color.WHITE)
            }

        statusBarIconViewClass
            .hookMethod("getIcon")
            .intercept { chain ->
                if (!mColoredStatusbarIcon) return@intercept chain.proceed()

                val hasContext = chain.args.size > 1
                val hasSysUiContext = chain.args.size > 2
                val statusBarNotification = chain.thisObject.getFieldSilently("mNotification")

                val sysuiContext = if (hasSysUiContext) chain.args[0] as Context else mContext
                val context = if (hasContext)
                    if (hasSysUiContext)
                        chain.args[1] as Context
                    else
                        chain.args[0] as Context
                else
                    statusBarNotification.callMethod(
                        "getPackageContext",
                        mContext
                    ) as? Context ?: mContext
                val statusBarIcon = chain.args[chain.args.lastIndex]

                var icon: Drawable
                val res = sysuiContext.resources
                val pkgName = statusBarIcon.getField("pkg") as String

                if (listOf("com.android", "systemui").any { pkgName.contains(it) }) {
                    return@intercept chain.proceed()
                }

                try {
                    icon = context.packageManager.getApplicationIcon(pkgName)
                } catch (_: Throwable) {
                    return@intercept chain.proceed()
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
                    icon
                } else {
                    scalingDrawableWrapperClass.getConstructor(
                        Drawable::class.java,
                        Float::class.javaPrimitiveType
                    ).newInstance(icon, scaleFactor)
                }
            }
    }
}