package com.drdisagree.iconify.xposed.modules.quicksettings

import android.annotation.SuppressLint
import android.app.Notification
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.core.graphics.drawable.toDrawable
import com.drdisagree.iconify.data.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.data.keys.XposedKey
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.core.PackageParam
import com.drdisagree.iconify.xposed.core.hook.hookMethod
import com.drdisagree.iconify.xposed.core.reflect.callMethod
import com.drdisagree.iconify.xposed.core.reflect.findClass
import com.drdisagree.iconify.xposed.core.reflect.getAnyField
import com.drdisagree.iconify.xposed.core.reflect.getField
import com.drdisagree.iconify.xposed.core.reflect.getFieldSilently
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs

@SuppressLint("DiscouragedApi")
class AppIconInNotification(context: Context) : ModPack(context) {

    private var coloredNotificationIcon = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            coloredNotificationIcon = getBoolean(XposedKey.COLORED_NOTIFICATION_ICON)
        }
    }

    override fun handleLoadPackage(packageParam: PackageParam) {
        val notificationHeaderViewWrapperClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper")

        notificationHeaderViewWrapperClass
            .hookMethod("onContentUpdated")
            .runAfter { chain, _ ->
                if (!coloredNotificationIcon) return@runAfter

                val row = chain.args[0]
                val notifyEntries = try {
                    row.callMethod("getEntry")
                } catch (_: Throwable) {
                    row.getAnyField("mEntry", "mEntryAdapter")
                }
                val notifySbn = try {
                    notifyEntries.callMethod("getSbn")
                } catch (_: Throwable) {
                    notifyEntries.getField("mSbn")
                }
                val notification = notifySbn.callMethod("getNotification") as Notification
                val pkgName = notifySbn.callMethod("getPackageName") as? String ?: return@runAfter
                val appIcon: Drawable = try {
                    mContext.packageManager.getApplicationIcon(pkgName)
                } catch (_: Throwable) {
                    return@runAfter
                }
                val mIcon = chain.thisObject.getFieldSilently("mIcon") as ImageView
                val mWorkProfileImage =
                    chain.thisObject.getFieldSilently("mWorkProfileImage") as? ImageView
                val mImageTransformStateIconTag = mContext.resources.getIdentifier(
                    "image_icon_tag",
                    "id",
                    SYSTEMUI_PACKAGE
                )

                if (mWorkProfileImage != null) {
                    mIcon.setImageDrawable(appIcon);
                    mWorkProfileImage.setImageIcon(notification.smallIcon);
                    // The work profile image is always the same
                    // Lets just set the icon tag for it not to animate
                    mWorkProfileImage.setTag(
                        mImageTransformStateIconTag,
                        notification.smallIcon
                    )
                }
                mIcon.setTag(mImageTransformStateIconTag, notification.smallIcon);
            }

        val notificationRowIconViewClass =
            findClass("com.android.internal.widget.NotificationRowIconView")

        notificationRowIconViewClass
            .hookMethod("onFinishInflate", "setImageIcon", "setImageIconAsync")
            .runAfter { chain, _ ->
                if (!coloredNotificationIcon) return@runAfter

                val iconView = chain.thisObject as ImageView

                iconView.post {
                    iconView.setPadding(0, 0, 0, 0)
                    iconView.background = Color.TRANSPARENT.toDrawable()
                }
            }
    }
}