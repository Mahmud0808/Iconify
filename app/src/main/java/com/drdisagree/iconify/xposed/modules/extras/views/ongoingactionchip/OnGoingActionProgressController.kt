/**
 * Copyright (c) 2025, The LineageOS Project
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip

import android.app.Notification
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.service.notification.StatusBarNotification
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.drdisagree.iconify.xposed.modules.extras.utils.getColorResCompat
import com.drdisagree.iconify.xposed.modules.extras.views.ongoingactionchip.IconFetcher.AdaptiveDrawableResult

/**
 * Controls the ongoing progress chip based on notifications @LineageExtension
 */
class OnGoingActionProgressController(
    private val mContext: Context,
    private val chipView: OnGoingActionChipView,
    private val isEnabledCondition: () -> Boolean
) {

    // Views of chip
    private val mProgressBar: ProgressBar = chipView.getProgressBar()
    private val mIconView: ImageView = chipView.getAppIcon()

    // Visibility state
    private var mIsForceHidden = false

    // Progress tracking variables
    private var mIsTrackingProgress = false
    private var mCurrentProgress = 0
    private var mCurrentProgressMax = 0
    private var mCurrentDrawable: Drawable? = null
    private var mTrackedNotificationKey: String? = null

    private val mIconFetcher: IconFetcher = IconFetcher(mContext)

    /**
     * Starts tracking progress of certain notification @AsyncUnsafe
     */
    private fun trackProgress(sbn: StatusBarNotification) {
        // Here we set progress tracking and update view if needed
        mIsTrackingProgress = true
        mTrackedNotificationKey = sbn.key
        val notification = sbn.notification
        mCurrentProgressMax = notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX, 100)
        mCurrentProgress = notification.extras.getInt(Notification.EXTRA_PROGRESS, 0)
        val drawable = mIconFetcher.resolveSmallIcon(sbn)
        mCurrentDrawable = drawable.drawable
        updateIconImageView(drawable)
        updateViews()
    }

    /**
     * Updates icon based on result from IconFetcher @AsyncUnsafe
     */
    private fun updateIconImageView(drawable: AdaptiveDrawableResult) {
        if (drawable.isAdaptive) {
            mIconView.setImageTintList(
                ColorStateList.valueOf(
                    getColorResCompat(mContext, android.R.attr.colorForeground)
                )
            )
        } else {
            mIconView.setImageTintList(null)
        }
        mIconView.setImageDrawable(drawable.drawable)
    }

    /**
     * Updates progress if needed @AsyncUnsafe
     */
    private fun updateProgressIfNeeded(sbn: StatusBarNotification) {
        if (!mIsTrackingProgress) return

        val notification = sbn.notification
        if (sbn.key == mTrackedNotificationKey) {
            mCurrentProgressMax = notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX, 100)
            mCurrentProgress = notification.extras.getInt(Notification.EXTRA_PROGRESS, 0)
            updateViews()
        }
    }

    /**
     * Updates progress views
     */
    private fun updateViews() {
        if (mIsForceHidden) { // Keyguard locked, user-disabled, etc.
            chipView.visibility = View.GONE
            return
        }

        if (!mIsTrackingProgress) {
            chipView.visibility = View.GONE
            return
        }

        if (!isEnabled) return

        if (chipView.visibility != View.VISIBLE) {
            chipView.visibility = View.VISIBLE
        }

        // Check if there's actually a change before updating views
        if (mProgressBar.max != mCurrentProgressMax ||
            mProgressBar.progress != mCurrentProgress ||
            mIconView.drawable != mCurrentDrawable
        ) {
            if (mCurrentProgressMax == 0) {
                mCurrentProgressMax = 100
            }

            mProgressBar.setMax(mCurrentProgressMax)
            mProgressBar.progress = mCurrentProgress

            if (mCurrentDrawable != null) {
                mIconView.setImageDrawable(mCurrentDrawable)
            }
        }
    }

    private val isEnabled: Boolean
        /**
         * Checks that progress bar chip is enabled
         *
         * @return whether progressbar chip is enabled
         * @implNote In future this function should be refactored to integrate TunerService with it;
         */
        get() = isEnabledCondition()

    /**
     * Should be called when new notification is posted
     */
    fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!hasProgress(sbn.notification)) {
            if (sbn.key == mTrackedNotificationKey) {
                // The notification we track has no progress anymore
                synchronized(this) {
                    mIsTrackingProgress = false
                    updateViews()
                }
            }
            return
        }

        if (!isEnabled) return

        synchronized(this) {
            if (!mIsTrackingProgress) {
                trackProgress(sbn)
            } else {
                updateProgressIfNeeded(sbn)
            }
        }
    }

    /**
     * Should be call when notification is removed
     */
    fun onNotificationRemoved(sbn: StatusBarNotification) {
        synchronized(this) {
            if (!mIsTrackingProgress) return

            if (sbn.key == mTrackedNotificationKey) {
                mIsTrackingProgress = false
                mCurrentDrawable = null
                updateViews()
            }
        }
    }

    /**
     * Sets hide chip override
     *
     * @param forceHidden if settled to true the chip would not be visible under any circumstances
     */
    fun setForceHidden(forceHidden: Boolean) {
        mIsForceHidden = forceHidden
        updateViews()
    }

    companion object {
        private val TAG = "Iconify - ${OnGoingActionProgressController::class.java.simpleName}: "

        /**
         * Checks whether notification has progress
         */
        private fun hasProgress(notification: Notification): Boolean {
            val extras = notification.extras
            val indeterminate =
                notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE, false)

            val maxProgressValid =
                notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX, 0) > 0

            return extras!!.containsKey(Notification.EXTRA_PROGRESS)
                    && extras.containsKey(Notification.EXTRA_PROGRESS_MAX)
                    && !indeterminate && maxProgressValid
        }
    }
}