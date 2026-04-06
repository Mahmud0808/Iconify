/*
 * Copyright (C) 2023-2024 the risingOS Android Project
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

package com.drdisagree.iconify.xposed.modules.extras.views

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Build
import android.widget.ImageView
import androidx.core.content.res.ResourcesCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.xposed.HookEntry.Companion.moduleResources
import com.drdisagree.iconify.xposed.modules.extras.views.ArcProgressWidget.generateBitmap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import kotlin.math.max
import kotlin.math.min

@SuppressLint("AppCompatCustomView", "ViewConstructor")
class ArcProgressImageView(context: Context) : ImageView(context) {

    private var mContext: Context = context
    private var progressType: ProgressType
    private var progressPercent = -1
    private var batteryLevel = -1
    private var batteryTemperature = -1
    private var audioManager: AudioManager? = null
    private var scheduler: ScheduledExecutorService? = null
    private var updateTask: ScheduledFuture<*>? = null
    private var batteryReceiverRegistered = false
    private var volumeReceiverRegistered = false
    private var typeface: Typeface? = null
    private var mProgressColor = Color.WHITE
    private var mTextColor = Color.WHITE

    private val batteryReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            if (Intent.ACTION_BATTERY_CHANGED == intent.action) {
                batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                batteryLevel = max(0.0, min(batteryLevel.toDouble(), 100.0)).toInt()
                batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10
                updateProgress()
            }
        }
    }

    private val volumeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals("android.media.VOLUME_CHANGED_ACTION")) {
                updateProgress()
            }
        }
    }

    enum class ProgressType(val iconRes: Int) {
        BATTERY(R.drawable.ic_battery),
        MEMORY(R.drawable.ic_memory),
        TEMPERATURE(R.drawable.ic_temperature),
        VOLUME(R.drawable.ic_volume_eq),
        UNKNOWN(-1)
    }

    init {
        mContext = context
        progressType = ProgressType.UNKNOWN
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun setProgressType(progressType: ProgressType) {
        this.progressType = progressType
        updateProgress()
    }

    fun setTypeFace(typeface: Typeface) {
        this.typeface = typeface
        updateImageView()
    }

    /**
     * Set colors of progress and text
     * @param progressColor progress color
     * @param textColor text color
     */
    fun setColors(progressColor: Int, textColor: Int) {
        mProgressColor = progressColor
        mTextColor = textColor
        updateImageView()
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        when (progressType) {
            ProgressType.BATTERY, ProgressType.TEMPERATURE -> {
                if (!batteryReceiverRegistered) {
                    IntentFilter(Intent.ACTION_BATTERY_CHANGED).also { filter ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mContext.registerReceiver(
                                batteryReceiver,
                                filter,
                                Context.RECEIVER_EXPORTED
                            )
                        } else {
                            mContext.registerReceiver(batteryReceiver, filter)
                        }
                    }
                    batteryReceiverRegistered = true
                }
            }

            ProgressType.VOLUME -> {
                if (!volumeReceiverRegistered) {
                    IntentFilter("android.media.VOLUME_CHANGED_ACTION").also { filter ->
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mContext.registerReceiver(
                                volumeReceiver,
                                filter,
                                Context.RECEIVER_EXPORTED
                            )
                        } else {
                            mContext.registerReceiver(volumeReceiver, filter)
                        }
                    }
                    volumeReceiverRegistered = true
                }
            }

            else -> {}
        }

        startProgressUpdates()
        updateVisibility()
        updateProgress()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        if (batteryReceiverRegistered) {
            mContext.unregisterReceiver(batteryReceiver)
            batteryReceiverRegistered = false
        }

        if (volumeReceiverRegistered) {
            mContext.unregisterReceiver(volumeReceiver)
            volumeReceiverRegistered = false
        }

        stopProgressUpdates()
    }

    private fun startProgressUpdates() {
        if (progressType == ProgressType.MEMORY || progressType == ProgressType.VOLUME) {
            scheduler = Executors.newSingleThreadScheduledExecutor()
            updateTask = scheduler!!.scheduleWithFixedDelay(
                { this.updateProgress() },
                0,
                1,
                TimeUnit.SECONDS
            )
        }
    }

    private fun stopProgressUpdates() {
        updateTask?.cancel(true)
        scheduler?.shutdown()
    }

    private fun updateProgress() {
        val newProgressPercent = when (progressType) {
            ProgressType.BATTERY -> batteryLevel
            ProgressType.MEMORY -> memoryLevel
            ProgressType.TEMPERATURE -> batteryTemperature
            ProgressType.VOLUME -> volumeLevel
            else -> -1
        }

        if (newProgressPercent != progressPercent) {
            progressPercent = newProgressPercent
            updateImageView()
        }
    }

    private fun updateImageView() {
        if (progressType == ProgressType.UNKNOWN) return

        val progressText = if (progressType == ProgressType.TEMPERATURE) {
            if (progressPercent != -1) {
                "$progressPercent\u2103" // degree
            } else {
                "N/A"
            }
        } else {
            if (progressPercent == -1) {
                "..."
            } else {
                "$progressPercent%"
            }
        }
        val widgetBitmap = generateBitmap(
            mContext,
            if (progressPercent == -1) 0 else progressPercent,
            progressText,
            40,
            ResourcesCompat.getDrawable(moduleResources, progressType.iconRes, mContext.theme),
            36,
            typeface ?: Typeface.create(Typeface.DEFAULT, Typeface.BOLD),
            mProgressColor,
            mTextColor
        )
        setImageBitmap(widgetBitmap)
    }

    private val memoryLevel: Int
        get() {
            val activityManager =
                mContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memoryInfo = ActivityManager.MemoryInfo()
            activityManager.getMemoryInfo(memoryInfo)
            val usedMemory = memoryInfo.totalMem - memoryInfo.availMem
            val usedMemoryPercentage = ((usedMemory * 100) / memoryInfo.totalMem).toInt()
            return max(0.0, min(usedMemoryPercentage.toDouble(), 100.0)).toInt()
        }

    private val volumeLevel: Int
        get() {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            return max(0.0, min(((currentVolume * 100) / maxVolume).toDouble(), 100.0)).toInt()
        }

    private fun updateVisibility() {
        val enabled = progressType != ProgressType.UNKNOWN
        visibility = if (enabled) VISIBLE else GONE
    }
}