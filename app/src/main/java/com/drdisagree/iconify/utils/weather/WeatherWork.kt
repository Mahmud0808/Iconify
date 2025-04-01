package com.drdisagree.iconify.utils.weather

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.text.TextUtils
import android.util.Log
import androidx.concurrent.futures.CallbackToFutureAdapter
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_DISABLED
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_LOCATION
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_NETWORK
import com.drdisagree.iconify.utils.OmniJawsClient.Companion.EXTRA_ERROR_NO_PERMISSIONS
import com.drdisagree.iconify.utils.weather.WeatherConfig.getLocationLat
import com.drdisagree.iconify.utils.weather.WeatherConfig.getLocationLon
import com.drdisagree.iconify.utils.weather.WeatherConfig.getProvider
import com.drdisagree.iconify.utils.weather.WeatherConfig.isCustomLocation
import com.drdisagree.iconify.utils.weather.WeatherConfig.isEnabled
import com.drdisagree.iconify.utils.weather.WeatherConfig.isMetric
import com.drdisagree.iconify.utils.weather.WeatherConfig.setUpdateError
import com.drdisagree.iconify.utils.weather.WeatherConfig.setWeatherData
import com.google.common.util.concurrent.ListenableFuture
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

@Suppress("deprecation")
class WeatherWork(val mContext: Context, workerParams: WorkerParameters) :
    ListenableWorker(mContext, workerParams) {

    override fun startWork(): ListenableFuture<Result> {
        return CallbackToFutureAdapter.getFuture { completer: CallbackToFutureAdapter.Completer<Result> ->
            if (!isEnabled(mContext)) {
                handleError(
                    completer,
                    EXTRA_ERROR_DISABLED,
                    "Service started, but not enabled ... stopping"
                )
                return@getFuture completer
            }

            if (!isCustomLocation(mContext)) {
                // Check permissions and location enabled
                // only if not using custom location
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
            }

            executor.execute {
                getCurrentLocation().thenAccept { location: Location? ->
                    if (location != null) {
                        Log.d(TAG, "Location retrieved")
                        updateWeather(location, completer)
                    } else if (isCustomLocation(mContext)) {
                        Log.d(TAG, "Using custom location configuration")
                        updateWeather(null, completer)
                    } else {
                        handleError(completer, EXTRA_ERROR_LOCATION, "Failed to retrieve location")
                    }
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

        return gpsEnabled || networkEnabled
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(): CompletableFuture<Location?> {
        val locationFuture = CompletableFuture<Location?>()

        if (isCustomLocation(mContext)) {
            locationFuture.complete(null)
            return locationFuture
        }

        val lm = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!doCheckLocationEnabled()) {
            Log.w(TAG, "locations disabled")
            locationFuture.complete(null)
            return locationFuture
        }

        val location = AtomicReference(lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER))
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
                locationFuture.complete(null)
            } else {
                Log.d(
                    TAG,
                    "Getting current location with provider $locationProvider"
                )
                lm.getCurrentLocation(
                    locationProvider!!, null, mContext.mainExecutor
                ) { location1: Location? ->
                    if (location1 != null) {
                        Log.d(TAG, "Got valid location now update")
                        location.set(location1)
                        locationFuture.complete(location1)
                    } else {
                        Log.e(TAG, "Failed to retrieve location")
                        locationFuture.complete(null)
                    }
                }
            }
        } else {
            locationFuture.complete(location.get())
        }

        return locationFuture
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
                Log.i(TAG, "location: $location, isCustomLocation: ${isCustomLocation(mContext)}")
                Log.i(TAG, "getLocationLat: ${getLocationLat(mContext)}, getLocationLon: ${getLocationLon(mContext)}")
                w = if (location != null && !isCustomLocation(mContext)) {
                    provider.getLocationWeather(location, isMetric)
                } else if (!TextUtils.isEmpty(getLocationLat(mContext)) && !TextUtils.isEmpty(
                        getLocationLon(mContext)
                    )
                ) {
                    provider.getCustomWeather(
                        getLocationLat(mContext)!!,
                        getLocationLon(mContext)!!,
                        isMetric
                    )
                } else {
                    Log.w(TAG, "No valid custom location and location is null")
                    break
                }
                Log.i(TAG, "w: $w")

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
                        } catch (_: InterruptedException) {
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

    @Suppress("deprecation")
    companion object {
        private const val TAG = "WeatherWork"
        private const val ACTION_BROADCAST = "${BuildConfig.APPLICATION_ID}.WEATHER_UPDATE"
        private const val ACTION_ERROR = "${BuildConfig.APPLICATION_ID}.WEATHER_ERROR"

        private const val EXTRA_ERROR = "error"

        private const val LOCATION_ACCURACY_THRESHOLD_METERS = 10000f
        private const val OUTDATED_LOCATION_THRESHOLD_MILLIS = 10L * 60L * 1000L // 10 minutes
        private const val RETRY_DELAY_MS = 5000
        private const val RETRY_MAX_NUM = 5

        private val executor: ExecutorService = Executors.newSingleThreadExecutor()
        private val dayFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

        private val sLocationCriteria = android.location.Criteria()

        init {
            sLocationCriteria.powerRequirement = android.location.Criteria.POWER_LOW
            sLocationCriteria.accuracy = android.location.Criteria.ACCURACY_COARSE
            sLocationCriteria.isCostAllowed = false
        }
    }
}