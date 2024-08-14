package com.drdisagree.iconify.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.text.TextUtils
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_DISABLED
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_NETWORK
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_LOCATION
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_NO_PERMISSIONS
import com.drdisagree.iconify.weather.Config.getLocationLat
import com.drdisagree.iconify.weather.Config.getLocationLon
import com.drdisagree.iconify.weather.Config.getProvider
import com.drdisagree.iconify.weather.Config.isCustomLocation
import com.drdisagree.iconify.weather.Config.isEnabled
import com.drdisagree.iconify.weather.Config.isMetric
import com.drdisagree.iconify.weather.Config.setUpdateError
import com.drdisagree.iconify.weather.Config.setWeatherData
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

class WeatherWork(val mContext: Context, workerParams: WorkerParameters) :
    ListenableWorker(mContext, workerParams) {
    override fun startWork(): ListenableFuture<Result> {
        if (DEBUG) Log.d(TAG, "startWork")

        return CallbackToFutureAdapter.getFuture<Result> { completer: CallbackToFutureAdapter.Completer<Result> ->
            if (!isEnabled(mContext)) {
                handleError(
                    completer,
                    EXTRA_ERROR_DISABLED,
                    "Service started, but not enabled ... stopping"
                )
                return@getFuture completer
            }
            if (!checkPermissions()) {
                handleError(
                    completer,
                    EXTRA_ERROR_NO_PERMISSIONS,
                    "Location permissions are not granted"
                )
                return@getFuture completer
            }

            if (!doCheckLocationEnabled()) {
                handleError(completer, EXTRA_ERROR_NETWORK, "Location services are disabled")
                return@getFuture completer
            }

            executor.execute {
                val location = currentLocation
                if (location != null) {
                    Log.d(TAG, "Location retrieved")
                    updateWeather(location, completer)
                } else if (isCustomLocation(mContext)) {
                    Log.d(
                        TAG,
                        "Using custom location configuration"
                    )
                    updateWeather(null, completer)
                } else {
                    handleError(completer, EXTRA_ERROR_LOCATION, "Failed to retrieve location")
                }
            }
            completer
        }
    }

    private fun handleError(
        completer: CallbackToFutureAdapter.Completer<Result>,
        errorExtra: Int,
        logMessage: String
    ) {
        Log.w(TAG, logMessage)
        val errorIntent = Intent(ACTION_ERROR)
        errorIntent.putExtra(EXTRA_ERROR, errorExtra)
        mContext.sendBroadcast(errorIntent)
        completer.set(Result.retry())
    }

    private fun doCheckLocationEnabled(): Boolean {
        val locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var gpsEnabled = false
        var networkEnabled = false

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (ex: Exception) {
            Log.d(TAG, "doCheckLocationEnabled: " + ex.message)
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (ex: Exception) {
            Log.d(TAG, "doCheckLocationEnabled: " + ex.message)
        }

        if (DEBUG) Log.d(
            TAG,
            "gpsEnabled: $gpsEnabled networkEnabled: $networkEnabled"
        )

        return gpsEnabled || networkEnabled
    }

    @get:SuppressLint("MissingPermission")
    private val currentLocation: Location?
        get() {
            val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

            if (!doCheckLocationEnabled()) {
                Log.w(TAG, "locations disabled")
                return null
            }

            val location =
                AtomicReference(lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER))
            Log.d(TAG, "Current location is $location")

            if (location.get() != null && location.get()!!.accuracy > LOCATION_ACCURACY_THRESHOLD_METERS) {
                Log.w(TAG, "Ignoring inaccurate location")
                location.set(null)
            }

            var needsUpdate = location.get() == null
            if (location.get() != null) {
                val delta = System.currentTimeMillis() - location.get()!!.time
                needsUpdate = delta > OUTDATED_LOCATION_THRESHOLD_MILLIS
                Log.d(TAG, "Location is " + delta + "ms old")
                if (needsUpdate) {
                    Log.w(
                        TAG, "Ignoring too old location from " + dayFormat.format(
                            location.get()!!.time
                        )
                    )
                    location.set(null)
                }
            }

            if (needsUpdate) {
                Log.d(TAG, "Requesting current location")
                val locationProvider = lm.getBestProvider(sLocationCriteria, true)
                if (TextUtils.isEmpty(locationProvider)) {
                    Log.e(TAG, "No available location providers matching criteria.")
                } else {
                    Log.d(
                        TAG,
                        "Getting current location with provider $locationProvider"
                    )
                    lm.getCurrentLocation(
                        locationProvider!!, null, mContext.mainExecutor
                    ) { location1: Location? ->
                        if (location1 != null) {
                            Log.d(
                                TAG,
                                "Got valid location now update"
                            )
                            location.set(location1)
                        } else {
                            Log.w(
                                TAG,
                                "Failed to retrieve location"
                            )
                        }
                    }
                }
            }

            return location.get()
        }

    private fun updateWeather(
        location: Location?,
        completer: CallbackToFutureAdapter.Completer<Result>
    ) {
        var w: WeatherInfo? = null
        try {
            val provider = getProvider(mContext)
            val isMetric = isMetric(mContext)
            var i = 0
            while (i < RETRY_MAX_NUM) {
                w = if (location != null && !isCustomLocation(mContext)) {
                    provider.getLocationWeather(location, isMetric)
                } else if (!TextUtils.isEmpty(getLocationLat(mContext)) && !TextUtils.isEmpty(getLocationLon(mContext))) {
                    provider.getCustomWeather(getLocationLat(mContext)!!, getLocationLon(mContext)!!, isMetric)
                } else {
                    Log.w(TAG, "No valid custom location and location is null")
                    break
                }

                if (w != null) {
                    setWeatherData(w, mContext)
                    WeatherContentProvider.updateCachedWeatherInfo(mContext)
                    Log.d(TAG, "Weather updated updateCachedWeatherInfo")
                    completer.set(Result.success())
                    return
                } else {
                    if (!provider.shouldRetry()) {
                        break
                    } else {
                        Log.w(TAG, "retry count = $i")
                        try {
                            Thread.sleep(RETRY_DELAY_MS.toLong())
                        } catch (ignored: InterruptedException) {
                        }
                    }
                }
                i++
            }
        } finally {
            if (w == null) {
                Log.d(TAG, "error updating weather")
                setUpdateError(mContext, true)
                completer.set(Result.retry())
            }
            val updateIntent = Intent(ACTION_BROADCAST)
            mContext.sendBroadcast(updateIntent)
        }
    }

    private fun checkPermissions(): Boolean {
        return mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "WeatherWork"
        private const val DEBUG = false
        private val ACTION_BROADCAST = BuildConfig.APPLICATION_ID.replace(".debug", "") + ".WEATHER_UPDATE"
        private val ACTION_ERROR = BuildConfig.APPLICATION_ID.replace(".debug", "") + ".WEATHER_ERROR"

        private const val EXTRA_ERROR = "error"

        private const val LOCATION_ACCURACY_THRESHOLD_METERS = 10000f
        private const val OUTDATED_LOCATION_THRESHOLD_MILLIS = 10L * 60L * 1000L // 10 minutes
        private const val RETRY_DELAY_MS = 5000
        private const val RETRY_MAX_NUM = 5

        private val executor: ExecutorService = Executors.newSingleThreadExecutor()
        private val dayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

        private val sLocationCriteria = Criteria()

        init {
            sLocationCriteria.powerRequirement = Criteria.POWER_LOW
            sLocationCriteria.accuracy = Criteria.ACCURACY_COARSE
            sLocationCriteria.isCostAllowed = false
        }
    }
}