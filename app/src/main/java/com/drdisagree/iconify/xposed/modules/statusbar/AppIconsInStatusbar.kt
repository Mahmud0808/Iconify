package com.drdisagree.iconify.xposed.modules.statusbar

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.View
import com.drdisagree.iconify.data.common.Const.FRAMEWORK_PACKAGE
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.DrawableSize
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
import de.robv.android.xposed.XposedHelpers.callStaticMethod
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

        fun setNotificationIcon(
            statusBarIcon: Any?,
            context: Context,
            sysuiContext: Context,
            param: MethodHookParam,
            scalingDrawableWrapper: Class<*>
        ) {
            if (!mColoredStatusbarIcon) return

            var icon: Drawable
            val res = sysuiContext.resources
            val pkgName = statusBarIcon.getField("pkg") as String

            if (listOf("com.android", "systemui").any { pkgName.contains(it) }) {
                return
            }

            try {
                icon = context.packageManager.getApplicationIcon(pkgName)
            } catch (e: Throwable) {
                return
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val isLowRamDevice = callStaticMethod(
                    ActivityManager::class.java,
                    "isLowRamDeviceStatic"
                ) as Boolean

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
            }

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
                param.result = scalingDrawableWrapper.getConstructor(
                    Drawable::class.java,
                    Float::class.javaPrimitiveType
                ).newInstance(icon, scaleFactor)
            }
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

        try {
            statusBarIconViewClass
                .hookMethod("getIcon")
                .parameters(
                    Context::class.java,
                    Context::class.java,
                    "com.android.internal.statusbar.StatusBarIcon"
                )
                .throwError()
                .runBefore { param ->
                    if (!mColoredStatusbarIcon) return@runBefore

                    val sysuiContext = param.args[0] as Context
                    val context = param.args[1] as Context
                    val statusBarIcon = param.args[2]

                    setNotificationIcon(
                        statusBarIcon,
                        context,
                        sysuiContext,
                        param,
                        scalingDrawableWrapperClass
                    )
                }
        } catch (_: Throwable) {
            statusBarIconViewClass
                .hookMethod("getIcon")
                .parameters("com.android.internal.statusbar.StatusBarIcon")
                .runBefore { param ->
                    if (!mColoredStatusbarIcon) return@runBefore

                    val sysuiContext = mContext
                    var context: Context? = null
                    val statusBarIcon = param.args[0]
                    val statusBarNotification = param.thisObject.getFieldSilently("mNotification")

                    if (statusBarNotification != null) {
                        context = statusBarNotification.callMethod(
                            "getPackageContext",
                            mContext
                        ) as Context?
                    }

                    if (context == null) {
                        context = mContext
                    }

                    setNotificationIcon(
                        statusBarIcon,
                        context,
                        sysuiContext,
                        param,
                        scalingDrawableWrapperClass
                    )
                }
        }
    }
}