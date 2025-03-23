/**
 * Copyright (c) 2025, The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.service.notification.StatusBarNotification
import androidx.core.content.ContextCompat

/** A class helping to fetch different versions of icons @LineageExtension  */
class IconFetcher(private val mContext: Context) {

    /** A class which stores whether icon is adaptive and icon itself.  */
    class AdaptiveDrawableResult(
        @JvmField var isAdaptive: Boolean,
        @JvmField var drawable: Drawable?
    )

    /**
     * Gets small icon from notification
     * If not found, returns standard package icon
     *
     * @param sbn notification for which small icon would be fetched
     */
    fun resolveSmallIcon(sbn: StatusBarNotification): AdaptiveDrawableResult {
        return try {
            val icon = sbn.notification.smallIcon
            return AdaptiveDrawableResult(false, icon.loadDrawable(mContext))
        } catch (_: Exception) {
            return getMonotonicPackageIcon(sbn.packageName)
        }
    }

    /**
     * Gets standard package icon
     *
     * @param packageName name of package for which icon would be fetched
     */
    private fun getPackageIcon(packageName: String): Drawable {
        return try {
            mContext.packageManager.getApplicationIcon(packageName)
        } catch (_: PackageManager.NameNotFoundException) {
            ContextCompat.getDrawable(mContext, android.R.drawable.sym_def_app_icon)!!
        }
    }

    /**
     * Returns a monotonic version of the app icon as a Drawable. The foreground of adaptive icons
     * is extracted and tinted, while non-adaptive icons are directly tinted.
     *
     * @param packageName The package name of the app whose icon is to be fetched.
     * @return A monotonic Drawable of the app icon or standard app icon within
     * AdaptiveDrawableResult
     */
    private fun getMonotonicPackageIcon(packageName: String): AdaptiveDrawableResult {
        val icon = getPackageIcon(packageName)

        if (icon is AdaptiveIconDrawable) {
            icon.foreground.colorFilter = PorterDuffColorFilter(
                Color.WHITE,
                PorterDuff.Mode.SRC_IN
            )
            return AdaptiveDrawableResult(true, icon)
        } else {
            return AdaptiveDrawableResult(false, icon)
        }
    }
}