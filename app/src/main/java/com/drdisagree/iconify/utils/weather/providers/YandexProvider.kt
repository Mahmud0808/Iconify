package com.drdisagree.iconify.utils.weather.providers

import android.content.Context
import android.location.Location
import android.text.TextUtils
import android.util.Log
import com.drdisagree.iconify.utils.weather.AbstractWeatherProvider
import com.drdisagree.iconify.utils.weather.WeatherConfig
import com.drdisagree.iconify.utils.weather.WeatherInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class YandexProvider(context: Context?) : AbstractWeatherProvider(context!!) {

    override fun getCustomWeather(lat: String?, lon: String?, metric: Boolean): WeatherInfo? {
        val coordinates = String.format(Locale.US, PART_COORDINATES, lat!!.toFloat(), lon!!.toFloat())
        return getAllWeather(coordinates, metric)
    }

    override fun getLocationWeather(location: Location?, metric: Boolean): WeatherInfo? {
        val coordinates =
            String.format(Locale.US, PART_COORDINATES, location!!.latitude, location.longitude)
        return getAllWeather(coordinates, metric)
    }

    private fun getAllWeather(coordinates: String, metric: Boolean): WeatherInfo? {
        val url = String.format(
            URL_WEATHER + coordinates + PART_PARAMETERS,
            language
        )
        val apiKey: String = WeatherConfig.getYandexKey(mContext)
        // Check API Key first
        if (TextUtils.isEmpty(apiKey)) {
            Log.e(TAG, "Yandex API key is not set")
            return null
        }
        val response: String =
            retrieve(url, arrayOf("X-Yandex-Weather-Key", apiKey))
        log(TAG, "URL = $url returning a response of $response")
        Log.w(
            TAG,
            "URL = $url returning a response of $response"
        )

        try {
            val weather = JSONObject(response)
            val current = weather.getJSONObject("fact")

            val city = getWeatherDataLocality(coordinates)

            val w = WeatherInfo(
                mContext,  /* id */
                coordinates,  /* cityId */
                city!!,  /* condition */
                CONDITION_MAPPING.getOrDefault(
                    current.optString("condition"),
                    "error"
                ),  /* conditionCode */
                ICON_MAPPING.getOrDefault(current.getString("icon"), -1),  /* temperature */
                convertTemperature(current.getInt("temp"), metric),  /* humidity */
                current.getInt("humidity").toFloat(),  /* wind */
                convertWind(current.getInt("wind_speed"), metric),  /* windDir */
                convertWindDegree(current.getString("wind_dir")),
                metric,
                parseHourlyForecasts(weather.getJSONArray("forecasts"), metric),
                parseForecasts(weather.getJSONArray("forecasts"), metric),
                weather.getLong("now") * 1000L
            )

            log(TAG, "Weather updated: $w")
            return w
        } catch (e: JSONException) {
            Log.w(
                TAG,
                "Received malformed weather data (coordinates = $coordinates)", e
            )
        }

        return null
    }

    @Throws(JSONException::class)
    private fun parseForecasts(
        forecasts: JSONArray,
        metric: Boolean
    ): ArrayList<WeatherInfo.DayForecast> {
        val result: ArrayList<WeatherInfo.DayForecast> = ArrayList(5)
        val count = forecasts.length()

        if (count == 0) {
            throw JSONException("Empty forecasts array")
        }

        var i = 0
        while (i < count && result.size < 5) {
            var item: WeatherInfo.DayForecast
            try {
                val forecast = forecasts.getJSONObject(i)

                if (i == 0 && !checkYesterday(forecast.getString("date"))) {
                    // skip if yesterday
                    i++
                    continue
                }

                val forecastParts = forecast.getJSONObject("parts")
                item = WeatherInfo.DayForecast( /* low */
                    convertTemperature(getMinMaxTemp(forecastParts, false), metric),  /* high */
                    convertTemperature(getMinMaxTemp(forecastParts, true), metric),  /* condition */
                    forecastParts.getJSONObject("day").getString("condition"),  /* conditionCode */
                    ICON_MAPPING.getOrDefault(
                        forecastParts.getJSONObject("day_short").getString("icon"), -1
                    ),
                    forecast.getString("date"),
                    metric
                )
            } catch (e: JSONException) {
                Log.w(
                    TAG,
                    "Invalid forecast for day $i creating dummy", e
                )
                item = WeatherInfo.DayForecast( /* low */
                    0F,  /* high */
                    0F,  /* condition */
                    "",  /* conditionCode */
                    -1,
                    "NaN",
                    metric
                )
            }
            result.add(item)
            i++
        }
        // clients assume there are 5  entries - so fill with dummy if needed
        if (result.size < 5) {
            for (i in result.size..4) {
                Log.w(
                    TAG,
                    "Missing forecast for day $i creating dummy"
                )
                val item: WeatherInfo.DayForecast = WeatherInfo.DayForecast( /* low */
                    0F,  /* high */
                    0F,  /* condition */
                    "",  /* conditionCode */
                    -1,
                    "NaN",
                    metric
                )
                result.add(item)
            }
        }

        for (item in result) {
            Log.d(TAG, "Day forecast: " + item.toString())
        }

        return result
    }

    @Throws(JSONException::class)
    private fun parseHourlyForecasts(
        forecasts: JSONArray,
        metric: Boolean
    ): ArrayList<WeatherInfo.HourForecast> {
        val result: ArrayList<WeatherInfo.HourForecast> = ArrayList(10)
        val count = forecasts.length()

        if (count == 0) {
            throw JSONException("Empty forecasts array")
        }

        if (count >= 2) {
            val firstForecast = forecasts.getJSONObject(0)
            val secondForecast = forecasts.getJSONObject(1)

            val currentHour = LocalTime.now().hour

            val nextHours: MutableList<JSONObject> =
                ArrayList(getNextHours(firstForecast, currentHour, 10))

            if (nextHours.size < 10) {
                val remainingHours = 10 - nextHours.size
                nextHours.addAll(getNextHours(secondForecast, 0, remainingHours))
            }

            for (hour in nextHours) {
                Log.w(TAG, "Hour forecast: $hour")
                result.add(
                    WeatherInfo.HourForecast( /* temp */
                        convertTemperature(hour.getInt("temp"), metric),  /* condition */
                        hour.getString("condition"),  /* conditionCode */
                        ICON_MAPPING.getOrDefault(hour.getString("icon"), -1),  /* date */
                        formatDate(hour.getString("hour_ts")),
                        metric
                    )
                )
            }
        }

        return result
    }

    private fun formatDate(unixTimestamp: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
        val dateTime = LocalDateTime.ofInstant(
            Instant.ofEpochSecond(unixTimestamp.toLong()),
            ZoneId.systemDefault()
        )
        return dateTime.format(formatter)
    }

    override fun shouldRetry(): Boolean {
        return false
    }

    companion object {
        private const val TAG = "YandexProvider"

        private const val URL_WEATHER = "https://api.weather.yandex.ru/v2/forecast?"
        private const val PART_PARAMETERS = "&limit=6&hours=true&lang=%s"
        private const val URL_PLACES =
            "http://api.geonames.org/searchJSON?q=%s&lang=%s&username=omnijaws&isNameRequired=true"
        private const val PART_COORDINATES = "lat=%f&lon=%f"

        @Throws(JSONException::class)
        fun getNextHours(forecast: JSONObject, startHour: Int, maxHours: Int): List<JSONObject> {
            val hoursList: MutableList<JSONObject> = ArrayList()


            val hours = forecast.getJSONArray("hours")
            for (i in 0 until hours.length()) {
                val hour = hours.getJSONObject(i)
                val hourValue = hour.getInt("hour")

                if (hourValue >= startHour && hoursList.size < maxHours) {
                    Log.w(TAG, "Adding hour $hourValue")
                    hoursList.add(hour)
                }

                if (hoursList.size == maxHours) {
                    break
                }
            }

            return hoursList
        }

        private val ICON_MAPPING = HashMap<String, Int>()

        init {
            ICON_MAPPING["bkn_d"] = 30
            ICON_MAPPING["bkn_n"] = 29
            ICON_MAPPING["bkn_ra_d"] = 11
            ICON_MAPPING["bkn_ra_n"] = 11
            ICON_MAPPING["bkn_-ra_d"] = 9
            ICON_MAPPING["bkn_-ra_n"] = 9
            ICON_MAPPING["bkn_-sn_d"] = 14
            ICON_MAPPING["bkn_-sn_n"] = 14
            ICON_MAPPING["ovc_-ra"] = 9
            ICON_MAPPING["ovc_-sn"] = 14
            ICON_MAPPING["bkn_+ra_d"] = 12
            ICON_MAPPING["bkn_+ra_n"] = 12
            ICON_MAPPING["bkn_+sn_d"] = 13
            ICON_MAPPING["bkn_+sn_n"] = 13
            ICON_MAPPING["ovc_+ra"] = 12
            ICON_MAPPING["ovc_+sn"] = 13
            ICON_MAPPING["bkn_sn_d"] = 14
            ICON_MAPPING["bkn_sn_n"] = 14
            ICON_MAPPING["bl"] = 15
            ICON_MAPPING["fg_d"] = 20
            ICON_MAPPING["fg_n"] = 20
            ICON_MAPPING["ovc"] = 26
            ICON_MAPPING["ovc_gr"] = 17
            ICON_MAPPING["ovc_ra"] = 12
            ICON_MAPPING["ovc_ra_sn"] = 18
            ICON_MAPPING["ovc_sn"] = 16
            ICON_MAPPING["ovc_ts_ra"] = 4
            ICON_MAPPING["skc_d"] = 32
            ICON_MAPPING["skc_n"] = 31
        }

        /*
            clear — Clear.
            partly-cloudy — Partly cloudy.
            cloudy — Cloudy.- overcast — Overcast.
            drizzle — Drizzle.
            light-rain — Light rain.
            rain — Rain.
            moderate-rain — Moderate rain.
            heavy-rain — Heavy rain.
            continuous-heavy-rain — Continuous heavy rain.
            showers — Showers.- wet-snow — Sleet.
            light-snow — Light snow.
            snow — Snow.
            snow-showers — Snowfall.
            hail — Hail.
            thunderstorm — Thunderstorm.
            thunderstorm-with-rain — Rain, thunderstorm.
            thunderstorm-with-hail — Thunderstorm, hail.
         */

        private val CONDITION_MAPPING = HashMap<String, String>()

        init {
            CONDITION_MAPPING["clear"] = "Clear sky"
            CONDITION_MAPPING["partly-cloudy"] = "Partly clouds"
            CONDITION_MAPPING["cloudy"] = "Clouds"
            CONDITION_MAPPING["overcast"] = "Clouds"
            CONDITION_MAPPING["partly-cloudy-and-light-rain"] = "partly_clouds_and_light_rain"
            CONDITION_MAPPING["partly-cloudy-and-rain"] = "partly_cloudy_and_rain"
            CONDITION_MAPPING["overcast-and-rain"] = "Light intensity drizzle rain"
            CONDITION_MAPPING["overcast-thunderstorms-with-rain"] =
                "overcast_thunderstorms_with_rain"
            CONDITION_MAPPING["cloudy-and-light-rain"] = "cloudy_and_light_rain"
            CONDITION_MAPPING["overcast-and-light-rain"] = "overcast_and_light_rain"
            CONDITION_MAPPING["cloudy-and-rain"] = "cloudy_and_rain"
            CONDITION_MAPPING["overcast-and-wet-snow"] = "overcast_and_wet_snow"
            CONDITION_MAPPING["partly-cloudy-and-light-snow"] = "partly_cloudy_and_light_snow"
            CONDITION_MAPPING["partly-cloudy-and-snow"] = "partly_cloudy_and_snow"
            CONDITION_MAPPING["overcast-and-snow"] = "overcast_and_snow"
            CONDITION_MAPPING["cloudy-and-light-snow"] = "cloudy_and_light_snow"
            CONDITION_MAPPING["overcast-and-light-snow"] = "overcast_and_light_snow"
            CONDITION_MAPPING["cloudy-and-snow"] = "snow"
        }

        private val language: String
            get() {
                val currentLang = Locale.getDefault().toString()
                val availableLang =
                    arrayOf("ru_RU", "ru_UA", "uk_UA", "be_BY", "kk_KZ", "tr_TR", "en_US")
                return if (listOf(*availableLang)
                        .contains(currentLang)
                ) currentLang else "en_US"
            }

        // if yesterday returns false
        private fun checkYesterday(valueDate: String): Boolean {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DATE, -1)
            val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)
            return yesterday != valueDate
        }

        // !needMax = needMin
        @Throws(JSONException::class)
        private fun getMinMaxTemp(dayPart: JSONObject, needMax: Boolean): Int {
            val typePart = arrayOf("night", "morning", "day", "evening")
            var result = if (needMax) Int.MIN_VALUE else Int.MAX_VALUE

            for (s in typePart) {
                val tmp = dayPart.getJSONObject(s).getInt(if (needMax) "temp_max" else "temp_min")

                if ((needMax && tmp > result) || (!needMax && tmp < result)) {
                    result = tmp
                }
            }
            return result
        }

        private fun convertTemperature(value: Int, metric: Boolean): Float {
            return if (metric) value.toFloat() else (value * 1.8f) + 32
        }

        private fun convertWind(value: Int, metric: Boolean): Float {
            return if (metric) value * 3.6f else value / 0.44704f
        }

        private fun convertWindDegree(value: String): Int {
            return when (value) {
                "nw" -> 315
                "n" -> 360
                "ne" -> 45
                "e" -> 90
                "se" -> 135
                "s" -> 180
                "sw" -> 225
                "w" -> 270
                else -> 0
            }
        }
    }
}
