package com.drdisagree.iconify.utils.weather

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.text.TextUtils
import android.util.Log
import com.drdisagree.iconify.R
import com.drdisagree.iconify.utils.NetworkUtils
import com.drdisagree.iconify.utils.weather.WeatherConfig.getLocationName
import com.drdisagree.iconify.utils.weather.WeatherConfig.isCustomLocation
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.CountDownLatch

abstract class AbstractWeatherProvider(protected var mContext: Context) {

    protected fun retrieve(url: String?): String {
        response = ""
        val latch = CountDownLatch(1)

        NetworkUtils.downloadUrlMemoryAsString(url) { result: String? ->
            if (result != null) {
                response = result
            } else {
                response = ""
                Log.d(TAG, "Download failed")
            }
            latch.countDown()
        }

        try {
            latch.await() // Wait until the response is set
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt() // Restore interrupt status
            Log.e(TAG, "retrieve interrupted", e)
        }

        return response
    }

    protected fun retrieve(url: String?, header: Array<String>): String {
        response = ""
        val latch = CountDownLatch(1)

        NetworkUtils.asynchronousGetRequest(url, header) { result ->
            if (!TextUtils.isEmpty(result)) {
                Log.d(TAG, "Download success $result")
                response = result
            } else {
                response = ""
                Log.d(TAG, "Download failed")
            }
            latch.countDown()
        }

        try {
            latch.await() // Wait until the response is set
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt() // Restore interrupt status
            Log.e(TAG, "retrieve interrupted", e)
        }

        return response
    }

    abstract fun getCustomWeather(lat: String?, lon: String?, metric: Boolean): WeatherInfo?

    abstract fun getLocationWeather(location: Location?, metric: Boolean): WeatherInfo?

    abstract fun shouldRetry(): Boolean

    protected fun log(tag: String, msg: String?) {
        if (DEBUG) Log.d(
            "WeatherService:$tag",
            msg!!
        )
    }

    private fun getCoordinatesLocalityWithGoogle(coordinate: String): String? {
        val latitude =
            coordinate.substring(coordinate.indexOf("=") + 1, coordinate.indexOf("&")).toDouble()
        val longitude = coordinate.substring(coordinate.lastIndexOf("=") + 1).toDouble()

        val geocoder = Geocoder(mContext.applicationContext, Locale.getDefault())
        try {
            val listAddresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!listAddresses.isNullOrEmpty()) {
                val a = listAddresses[0]
                return if (TextUtils.isEmpty(a.locality)) a.adminArea else a.locality
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

    protected fun getCoordinatesLocality(coordinate: String): String? {
        val cityGoogle = getCoordinatesLocalityWithGoogle(coordinate)
        if (!TextUtils.isEmpty(cityGoogle)) {
            return cityGoogle
        }
        val latitude =
            coordinate.substring(coordinate.indexOf("=") + 1, coordinate.indexOf("&")).toDouble()
        val longitude = coordinate.substring(coordinate.lastIndexOf("=") + 1).toDouble()

        val lang = Locale.getDefault().language.replaceFirst("_".toRegex(), "-")
        val url = String.format(URL_LOCALITY, latitude, longitude, lang)
        val response = retrieve(url, arrayOf())
        log(TAG, "URL = $url returning a response of $response")

        try {
            val jsonResults = JSONObject(response)
            if (jsonResults.has("address")) {
                val address = jsonResults.getJSONObject("address")
                val city = address.getString("placename")
                val area = address.getString("adminName2")
                if (!TextUtils.isEmpty(city)) {
                    return city
                }
                if (!TextUtils.isEmpty(area)) {
                    return area
                }
            } else if (jsonResults.has("geonames")) {
                val jsonResultsArray = jsonResults.getJSONArray("geonames")
                val count = jsonResultsArray.length()

                for (i in count - 1 downTo 0) {
                    val geoname = jsonResultsArray.getJSONObject(i)
                    val fcode = geoname.getString("fcode")
                    val name = geoname.getString("name")
                    if (TextUtils.isEmpty(name)) {
                        continue
                    }
                    if (fcode == "ADM3") {
                        return name
                    }
                    if (fcode == "ADM2") {
                        return name
                    }
                    if (fcode == "ADM1") {
                        return name
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(
                TAG,
                "Received malformed location data (coordinate=$coordinate)", e
            )
        }
        return null
    }

    protected fun getWeatherDataLocality(coordinates: String): String? {
        var city: String?
        if (isCustomLocation(mContext)) {
            city = getLocationName(mContext)
            if (TextUtils.isEmpty(city)) {
                city = getCoordinatesLocality(coordinates)
            }
        } else {
            city = getCoordinatesLocality(coordinates)
        }
        if (TextUtils.isEmpty(city)) {
            city = mContext.resources.getString(R.string.omnijaws_city_unkown)
        }
        log(TAG, "getWeatherDataLocality = $city")
        return city
    }

    protected fun getDay(i: Int): String {
        val calendar = Calendar.getInstance()
        if (i > 0) {
            calendar.add(Calendar.DATE, i)
        }
        return dayFormat.format(calendar.time)
    }

    companion object {
        private const val TAG = "AbstractWeatherProvider"
        private const val DEBUG = false
        private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        private const val URL_PLACES =
            "https://secure.geonames.org/searchJSON?name_startsWith=%s&lang=%s&username=omnijaws&maxRows=20"
        private const val URL_LOCALITY =
            "https://secure.geonames.org/extendedFindNearbyJSON?lat=%f&lng=%f&lang=%s&username=omnijaws"
        const val PART_COORDINATES: String = "lat=%f&lon=%f"
        private var response = ""
    }
}