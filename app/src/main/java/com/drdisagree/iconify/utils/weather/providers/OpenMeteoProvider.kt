package com.drdisagree.iconify.utils.weather.providers

import android.content.Context
import android.location.Location
import android.util.Log
import com.drdisagree.iconify.utils.weather.AbstractWeatherProvider
import com.drdisagree.iconify.utils.weather.WeatherInfo
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class OpenMeteoProvider(context: Context?) : AbstractWeatherProvider(context!!) {

    override fun getCustomWeather(lat: String?, lon: String?, metric: Boolean): WeatherInfo? {
        val coordinates =
            String.format(Locale.US, PART_COORDINATES, lat!!.toFloat(), lon!!.toFloat())
        return handleWeatherRequest(coordinates, metric)
    }

    override fun getLocationWeather(location: Location?, metric: Boolean): WeatherInfo? {
        val coordinates =
            String.format(Locale.US, PART_COORDINATES, location!!.latitude, location.longitude)
        return handleWeatherRequest(coordinates, metric)
    }

    private fun handleWeatherRequest(selection: String?, metric: Boolean): WeatherInfo? {
        val tempUnit = if (metric) "celsius" else "fahrenheit"
        val speedUnit = if (metric) "kmh" else "mph"
        val timeZone = TimeZone.getDefault().id
        val conditionUrl = String.format(
            Locale.US,
            URL_WEATHER + PART_PARAMETERS,
            selection,
            tempUnit,
            speedUnit,
            timeZone
        )
        val conditionResponse = retrieve(conditionUrl) ?: return null
        log(
            TAG,
            "Condition URL = $conditionUrl returning a response of $conditionResponse"
        )

        try {
            val weather = JSONObject(conditionResponse).getJSONObject("current")

            val city = getWeatherDataLocality(selection!!)

            val weathercode = weather.getInt("weather_code")
            val isDay = weather.getInt("is_day") == 1

            val w = WeatherInfo(
                mContext,
                /* id */ selection,
                /* cityId */ city!!,
                /* condition */ getWeatherDescription(weathercode),
                /* conditionCode */ mapConditionIconToCode(weathercode, isDay),
                /* temperature */ weather.getDouble("temperature_2m").toFloat(),  // Api: Humidity included in current
                /* humidity */ weather.getDouble("relative_humidity_2m").toFloat(),
                /* wind */ weather.getDouble("wind_speed_10m").toFloat(),
                /* windDir */weather.getInt("wind_direction_10m"),
                metric,
                parseHourlyForecasts(JSONObject(conditionResponse).getJSONObject("hourly"), metric),
                parseForecasts(JSONObject(conditionResponse).getJSONObject("daily"), metric),
                System.currentTimeMillis()
            )
            return w
        } catch (e: JSONException) {
            Log.e(
                TAG,
                "Received malformed weather data (coordinates = $selection)", e
            )
        }


        return null
    }

    @Throws(JSONException::class)
    private fun parseForecasts(
        forecasts: JSONObject,
        metric: Boolean
    ): ArrayList<WeatherInfo.DayForecast> {
        val result = java.util.ArrayList<WeatherInfo.DayForecast>(5)

        val timeJson = forecasts.getJSONArray("time")
        val temperatureMinJson = forecasts.getJSONArray("temperature_2m_min_best_match")
        val temperatureMaxJson = forecasts.getJSONArray("temperature_2m_max_best_match")
        val weatherCodeJson = forecasts.getJSONArray("weather_code_best_match")
        val altWeatherCodeJson = forecasts.getJSONArray("weather_code_gfs_seamless")
        val currentDay =
            SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().time)

        var startIndex = 1
        if (currentDay == timeJson.getString(0)) startIndex = 0
        else if (currentDay == timeJson.getString(2)) startIndex = 2

        var i = startIndex
        while (i < timeJson.length() && result.size < 5) {
            var item: WeatherInfo.DayForecast
            var weatherCode = weatherCodeJson.getInt(i)
            if (weatherCode == 45 || weatherCode == 48) weatherCode = altWeatherCodeJson.getInt(i)

            try {
                item = WeatherInfo.DayForecast( /* low */
                    temperatureMinJson.getDouble(i).toFloat(),  /* high */
                    temperatureMaxJson.getDouble(i).toFloat(),  /* condition */
                    getWeatherDescription(weatherCode),  /* conditionCode */
                    mapConditionIconToCode(weatherCode, true),
                    timeJson.getString(i),
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
                Log.w(TAG, "Missing forecast for day $i creating dummy")
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

        return result
    }

    @Throws(JSONException::class)
    private fun parseHourlyForecasts(
        forecasts: JSONObject,
        metric: Boolean
    ): java.util.ArrayList<WeatherInfo.HourForecast> {
        val result: ArrayList<WeatherInfo.HourForecast> = ArrayList()

        val timeJson = forecasts.getJSONArray("time")
        val temperature = forecasts.getJSONArray("temperature_2m_best_match")
        val weatherCodeJson = forecasts.getJSONArray("weather_code_best_match")
        val altWeatherCodeJson = forecasts.getJSONArray("weather_code_gfs_seamless")
        val currentDay =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(Calendar.getInstance().time)

        var startIndex = 1
        if (currentDay == timeJson.getString(0)) startIndex = 0
        else if (currentDay == timeJson.getString(2)) startIndex = 2

        var i = startIndex
        while (i < timeJson.length() && result.size < 10) {
            var item: WeatherInfo.HourForecast
            var weatherCode = weatherCodeJson.getInt(i)
            if (weatherCode == 45 || weatherCode == 48) weatherCode = altWeatherCodeJson.getInt(i)

            try {
                item = WeatherInfo.HourForecast( /* temp */
                    temperature.getDouble(i).toFloat(),  /* condition */
                    getWeatherDescription(weatherCode),  /* conditionCode */
                    mapConditionIconToCode(weatherCode, true),
                    timeJson.getString(i),
                    metric
                )
            } catch (e: JSONException) {
                Log.w(
                    TAG,
                    "Invalid forecast for day $i creating dummy", e
                )
                item = WeatherInfo.HourForecast( /* temp */
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
        if (result.size < 10) {
            for (i in result.size..9) {
                Log.w(TAG, "Missing forecast for hour $i creating dummy")
                val item: WeatherInfo.HourForecast = WeatherInfo.HourForecast( /* temp */
                    0F,  /* condition */
                    "",  /* conditionCode */
                    -1,
                    "NaN",
                    metric
                )
                result.add(item)
            }
        }

        return result
    }

    private val languageCode: String
        get() {
            val locale = mContext.resources.configuration.locale
            val selector = locale.language + "-" + locale.country

            for ((key, value) in LANGUAGE_CODE_MAPPING) {
                if (selector.startsWith(key)) {
                    return value
                }
            }

            return "en"
        }

    private fun mapConditionIconToCode(code: Int, isDay: Boolean): Int {
        return when (code) {
            0 ->  // Clear sky
                if (isDay) 32 else 31

            1 ->  // Mainly clear
                if (isDay) 34 else 33

            2 ->  // Partly cloudy
                if (isDay) 30 else 29

            3 ->  // Overcast
                26

            45, 48 ->  // Depositing rime fog
                20

            51 ->  // Light intensity drizzle
                9

            53 ->  // Moderate intensity drizzle
                9

            55 ->  // Dense intensity drizzle
                12

            56 ->  // Light intensity freezing drizzle
                8

            57 ->  // Dense intensity freezing drizzle
                8

            61 ->  // Slight intensity rain
                9

            63 ->  // Moderate intensity rain
                11

            65 ->  // Heavy intensity rain
                12

            66 ->  // Light intensity freezing rain
                10

            67 ->  // Heavy intensity freezing rain
                10

            71 ->  // Slight intensity snowfall
                14

            73 ->  // Moderate intensity snowfall
                16

            75 ->  // Heavy intensity snowfall
                43

            77 ->  // Snow grains
                16

            80 ->  // Slight intensity rain showers
                11

            81 ->  // Moderate intensity rain showers
                40

            82 ->  // Violent intensity rain showers
                40

            85 ->  // Slight intensity snow showers
                14

            86 ->  // Heavy intensity snow showers
                43

            95 ->  // Slight or moderate thunderstorm
                4

            96, 99 ->  // Thunderstorm with heavy hail
                38

            else ->  // Unknown
                -1
        }
    }

    override fun shouldRetry(): Boolean {
        return false
    }

    companion object {
        private const val TAG = "OpenWeatherMapProvider"

        private const val FORECAST_DAYS = 5
        private const val URL_WEATHER = "https://api.open-meteo.com/v1/forecast?"
        private const val PART_COORDINATES = "latitude=%f&longitude=%f"
        private const val PART_PARAMETERS =
            "%s&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,is_day&hourly=weather_code,temperature_2m&forecast_hours=24&daily=weather_code,temperature_2m_max,temperature_2m_min&temperature_unit=%s&windspeed_unit=%s&timezone=%s&past_days=1&models=best_match,gfs_seamless"


        /* OpenMeteo WMO Weather interpretation codes (WW)
         * 0 	Clear sky
         * 1, 2, 3 	Mainly clear, partly cloudy, and overcast
         * 45, 48 	Fog and depositing rime fog
         * 51, 53, 55 	Drizzle: Light, moderate, and dense intensity
         * 56, 57 	Freezing Drizzle: Light and dense intensity
         * 61, 63, 65 	Rain: Slight, moderate and heavy intensity
         * 66, 67 	Freezing Rain: Light and heavy intensity
         * 71, 73, 75 	Snow fall: Slight, moderate, and heavy intensity
         * 77 	Snow grains
         * 80, 81, 82 	Rain showers: Slight, moderate, and violent
         * 85, 86 	Snow showers slight and heavy
         * 95 * 	Thunderstorm: Slight or moderate
         * 96, 99 * 	Thunderstorm with slight and heavy hail
         */
        private fun getWeatherDescription(code: Int): String {
            return when (code) {
                0 -> "Clear sky"
                1 -> "Mainly clear"
                2 -> "Partly clouds"
                3 -> "Clouds"
                45 -> "Fog"
                48 -> "Depositing rime fog"
                51 -> "Light intensity drizzle rain"
                53 -> "Moderate intensity drizzle rain"
                55 -> "Dense intensity drizzle rain"
                56 -> "Light intensity freezing drizzle rain"
                57 -> "Dense intensity freezing drizzle rain"
                61 -> "Slight intensity rain"
                63 -> "Moderate intensity rain"
                65 -> "Heavy intensity rain"
                66 -> "Light intensity freezing rain"
                67 -> "Heavy intensity freezing rain"
                71 -> "Slight intensity snowfall"
                73 -> "Moderate intensity snowfall"
                75 -> "Heavy intensity snowfall"
                77 -> "Snow grains"
                80 -> "Slight intensity rain showers"
                81 -> "Moderate intensity rain showers"
                82 -> "Violent intensity rain showers"
                85 -> "Slight intensity snow showers"
                86 -> "Heavy intensity snow showers"
                95 -> "Slight or moderate thunderstorm"
                96 -> "Thunderstorm with slight hail"
                99 -> "Thunderstorm with heavy hail"
                else -> "Unknown"
            }
        }

        @Throws(JSONException::class)
        private fun getCurrentHumidity(hourlyJson: JSONObject): Float {
            val currentHour =
                SimpleDateFormat("yyyy-MM-dd'T'HH", Locale.US).format(Calendar.getInstance().time)
            val hourlyTimes = hourlyJson.getJSONArray("time")
            val hourlyHumidity = hourlyJson.getJSONArray("relativehumidity_2m_best_match")

            var currentIndex = 36
            for (i in 0 until hourlyTimes.length()) if (hourlyTimes.getString(i)
                    .startsWith(currentHour)
            ) {
                currentIndex = i
                break
            }

            return hourlyHumidity.getDouble(currentIndex).toFloat()
        }

        // OpenWeatherMap sometimes returns temperatures in Kelvin even if we ask it
        // for deg C or deg F. Detect this and convert accordingly.
        private fun sanitizeTemperature(value: Double, metric: Boolean): Float {
            // threshold chosen to work for both C and F. 170 deg F is hotter
            // than the hottest place on earth.
            var value = value
            if (value > 170) {
                // K -> deg C
                value -= 273.15
                if (!metric) {
                    // deg C -> deg F
                    value = (value * 1.8) + 32
                }
            }
            return value.toFloat()
        }

        private val LANGUAGE_CODE_MAPPING = HashMap<String, String>()

        init {
            LANGUAGE_CODE_MAPPING["bg-"] = "bg"
            LANGUAGE_CODE_MAPPING["de-"] = "de"
            LANGUAGE_CODE_MAPPING["es-"] = "sp"
            LANGUAGE_CODE_MAPPING["fi-"] = "fi"
            LANGUAGE_CODE_MAPPING["fr-"] = "fr"
            LANGUAGE_CODE_MAPPING["it-"] = "it"
            LANGUAGE_CODE_MAPPING["nl-"] = "nl"
            LANGUAGE_CODE_MAPPING["pl-"] = "pl"
            LANGUAGE_CODE_MAPPING["pt-"] = "pt"
            LANGUAGE_CODE_MAPPING["ro-"] = "ro"
            LANGUAGE_CODE_MAPPING["ru-"] = "ru"
            LANGUAGE_CODE_MAPPING["se-"] = "se"
            LANGUAGE_CODE_MAPPING["tr-"] = "tr"
            LANGUAGE_CODE_MAPPING["uk-"] = "ua"
            LANGUAGE_CODE_MAPPING["zh-CN"] = "zh_cn"
            LANGUAGE_CODE_MAPPING["zh-TW"] = "zh_tw"
        }
    }
}