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
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getAnyField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getFieldSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

@SuppressLint("DiscouragedApi")
class AppIconInNotification(context: Context) : ModPack(context) {

    private var coloredNotificationIcon = false

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            coloredNotificationIcon = getBoolean(XposedKey.COLORED_NOTIFICATION_ICON)
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        val notificationHeaderViewWrapperClass =
            findClass("$SYSTEMUI_PACKAGE.statusbar.notification.row.wrapper.NotificationHeaderViewWrapper")

        notificationHeaderViewWrapperClass
            .hookMethod("onContentUpdated")
            .runAfter { param ->
                if (!coloredNotificationIcon) return@runAfter

                val row = param.args[0]
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
                val mIcon = param.thisObject.getFieldSilently("mIcon") as ImageView
                val mWorkProfileImage =
                    param.thisObject.getFieldSilently("mWorkProfileImage") as? ImageView
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
            .runAfter { param ->
                if (!coloredNotificationIcon) return@runAfter

                val iconView = param.thisObject as ImageView

                iconView.post {
                    iconView.setPadding(0, 0, 0, 0)
                    iconView.background = Color.TRANSPARENT.toDrawable()
                }
            }
    }
}