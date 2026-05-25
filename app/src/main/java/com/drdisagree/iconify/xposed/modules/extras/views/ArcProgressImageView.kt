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
import com.drdisagree.iconify.xposed.HookRes.Companion.modRes
import com.drdisagree.iconify.xposed.modules.extras.views.ArcProgressWidget.generateBitmap
import java.io.File
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
    private var progressText = ""
    private var batteryLevel = -1
    private var batteryTemperature = -1
    private var previousCpuTotal = -1L
    private var previousCpuIdle = -1L
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
        CPU(R.drawable.ic_cpu),
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
        if (
            progressType == ProgressType.MEMORY ||
            progressType == ProgressType.VOLUME ||
            progressType == ProgressType.CPU ||
            progressType == ProgressType.BATTERY
        ) {
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
            ProgressType.CPU -> cpuLevel
            else -> -1
        }

        val newProgressText = when (progressType) {
            ProgressType.BATTERY -> batteryCurrentText
            ProgressType.TEMPERATURE -> {
                if (newProgressPercent != -1) {
                    "$newProgressPercent\u2103" // degree
                } else {
                    "N/A"
                }
            }

            else -> {
                if (newProgressPercent == -1) {
                    "..."
                } else {
                    "$newProgressPercent%"
                }
            }
        }

        if (newProgressPercent != progressPercent || newProgressText != progressText) {
            progressPercent = newProgressPercent
            progressText = newProgressText
            updateImageView()
        }
    }

    private fun updateImageView() {
        if (progressType == ProgressType.UNKNOWN) return

        val widgetBitmap = generateBitmap(
            mContext,
            if (progressPercent == -1) 0 else progressPercent,
            progressText,
            if (progressType == ProgressType.BATTERY) 30 else 40,
            ResourcesCompat.getDrawable(modRes, progressType.iconRes, mContext.theme),
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

    private val batteryCurrentText: String
        get() {
            val batteryManager = mContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val currentNowMicroAmpere =
                batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)

            if (currentNowMicroAmpere == Long.MIN_VALUE || currentNowMicroAmpere == 0L) {
                return "N/A"
            }

            val currentNowMilliAmpere = currentNowMicroAmpere / 1000
            return if (currentNowMilliAmpere > 0) {
                "+${currentNowMilliAmpere}mA"
            } else {
                "${currentNowMilliAmpere}mA"
            }
        }

    private val cpuLevel: Int
        get() = readCpuUsageLevel() ?: readCpuFrequencyLevel() ?: -1

    private fun readCpuUsageLevel(): Int? {
        val cpuStats = readCpuStats() ?: return null
        val total = cpuStats.first
        val idle = cpuStats.second

        if (previousCpuTotal < 0 || previousCpuIdle < 0) {
            previousCpuTotal = total
            previousCpuIdle = idle
            return 0
        }

        val totalDelta = total - previousCpuTotal
        val idleDelta = idle - previousCpuIdle

        previousCpuTotal = total
        previousCpuIdle = idle

        if (totalDelta <= 0) return null

        val usage = ((totalDelta - idleDelta) * 100 / totalDelta).toInt()
        return max(0.0, min(usage.toDouble(), 100.0)).toInt()
    }

    private fun readCpuStats(): Pair<Long, Long>? {
        return runCatching {
            val values = File("/proc/stat")
                .readLines()
                .firstOrNull { it.startsWith("cpu ") }
                ?.trim()
                ?.split(Regex("\\s+"))
                ?.drop(1)
                ?.map { it.toLong() }
                ?: return null

            val idle = values.getOrNull(3).orZero() + values.getOrNull(4).orZero()
            val total = values.sum()
            total to idle
        }.getOrNull()
    }

    private fun readCpuFrequencyLevel(): Int? {
        return runCatching {
            val cpuRoot = File("/sys/devices/system/cpu")
            val cpuDirs = cpuRoot.listFiles { file ->
                file.isDirectory && file.name.matches(Regex("cpu\\d+"))
            }.orEmpty()

            var totalPercent = 0
            var validCores = 0

            cpuDirs.forEach { cpuDir ->
                val curFreq = File(cpuDir, "cpufreq/scaling_cur_freq")
                    .takeIf { it.canRead() }
                    ?.readText()
                    ?.trim()
                    ?.toLongOrNull()

                val maxFreq = File(cpuDir, "cpufreq/cpuinfo_max_freq")
                    .takeIf { it.canRead() }
                    ?.readText()
                    ?.trim()
                    ?.toLongOrNull()

                if (curFreq != null && maxFreq != null && maxFreq > 0) {
                    totalPercent += ((curFreq * 100) / maxFreq).toInt()
                    validCores++
                }
            }

            if (validCores == 0) return null

            max(0.0, min((totalPercent / validCores).toDouble(), 100.0)).toInt()
        }.getOrNull()
    }

    private fun Long?.orZero() = this ?: 0L

    private fun updateVisibility() {
        val enabled = progressType != ProgressType.UNKNOWN
        visibility = if (enabled) VISIBLE else GONE
    }
}