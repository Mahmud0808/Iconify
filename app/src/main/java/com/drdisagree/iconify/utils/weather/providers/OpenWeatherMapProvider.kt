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
import java.util.Locale

class OpenWeatherMapProvider(context: Context?) : AbstractWeatherProvider(context!!) {

    private val mHasAPIKey: Boolean

    override fun getCustomWeather(lat: String?, lon: String?, metric: Boolean): WeatherInfo? {
        val coordinates = String.format(Locale.US, PART_COORDINATES, lat!!.toFloat(), lon!!.toFloat())
        return handleWeatherRequest(coordinates, metric)
    }

    override fun getLocationWeather(location: Location?, metric: Boolean): WeatherInfo? {
        val coordinates = String.format(Locale.US, PART_COORDINATES, location!!.latitude,location.longitude)
        return handleWeatherRequest(coordinates, metric)
    }

    private fun handleWeatherRequest(selection: String, metric: Boolean): WeatherInfo? {
        if (!mHasAPIKey) {
            return null
        }
        val units = if (metric) "metric" else "imperial"
        val locale = languageCode
        val conditionUrl = String.format(
            Locale.US, URL_WEATHER, selection, units, locale,
            aPIKey
        )
        val conditionResponse: String = retrieve(conditionUrl) ?: return null
        log(
            TAG,
            "Condition URL = $conditionUrl returning a response of $conditionResponse"
        )

        try {
            val conditions = JSONObject(conditionResponse)
            val conditionData = conditions.getJSONObject("main")
            val weather = conditions.getJSONArray("weather").getJSONObject(0)
            var forecasts: ArrayList<WeatherInfo.DayForecast> = ArrayList()
            if (conditions.has("daily")) {
                forecasts =
                    parseForecasts(conditions.getJSONArray("daily"), metric)
            }
            val wind = conditions.getJSONObject("wind")
            var windSpeed = wind.getDouble("speed").toFloat()
            if (metric) {
                // speeds are in m/s so convert to our common metric unit km/h
                windSpeed *= 3.6f
            }

            val city: String = getWeatherDataLocality(selection).toString()

            val w: WeatherInfo = WeatherInfo(
                mContext, selection, city,  /* condition */
                weather.getString("main"),  /* conditionCode */
                mapConditionIconToCode(
                    weather.getString("icon"), weather.getInt("id")
                ),  /* temperature */
                sanitizeTemperature(conditionData.getDouble("temp"), metric),  /* humidity */
                conditionData.getDouble("humidity").toFloat(),  /* wind */
                windSpeed,  /* windDir */
                if (wind.has("deg")) wind.getInt("deg") else 0,
                metric,
                forecasts,
                System.currentTimeMillis()
            )

            log(TAG, "Weather updated: $w")
            return w
        } catch (e: JSONException) {
            Log.w(
                TAG, "Received malformed weather data (selection = " + selection
                        + ", lang = " + locale + ")", e
            )
        }

        return null
    }

    @Throws(JSONException::class)
    private fun parseForecasts(forecasts: JSONArray, metric: Boolean): ArrayList<WeatherInfo.DayForecast> {
        val result: ArrayList<WeatherInfo.DayForecast> = ArrayList()
        val count = forecasts.length()

        if (count == 0) {
            throw JSONException("Empty forecasts array")
        }
        for (i in 0 until count) {
            val day: String = getDay(i)
            var item: WeatherInfo.DayForecast
            try {
                val forecast = forecasts.getJSONObject(i)
                val conditionData = forecast.getJSONObject("temp")
                val data = forecast.getJSONArray("weather").getJSONObject(0)
                item = WeatherInfo.DayForecast( /* low */
                    sanitizeTemperature(conditionData.getDouble("min"), metric),  /* high */
                    sanitizeTemperature(conditionData.getDouble("max"), metric),  /* condition */
                    data.getString("main"),  /* conditionCode */
                    mapConditionIconToCode(
                        data.getString("icon"), data.getInt("id")
                    ),
                    day,
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
        return result
    }

    init {
        mHasAPIKey = aPIKey != null
    }

    private val languageCode: String
        get() {
            val locale: Locale = mContext.getResources().getConfiguration().locale
            val selector = locale.language + "-" + locale.country

            for ((key, value) in LANGUAGE_CODE_MAPPING) {
                if (selector.startsWith(key)) {
                    return value
                }
            }

            return "en"
        }

    private fun mapConditionIconToCode(icon: String, conditionId: Int): Int {
        // First, use condition ID for specific cases

        return when (conditionId) {
            202, 232, 211 ->  // thunderstorm
                4

            212 ->  // heavy thunderstorm
                3

            221, 231, 201 ->  // thunderstorm with rain
                38

            230, 200, 210 ->  // light thunderstorm
                37

            300, 301, 302, 310, 311, 312, 313, 314, 321 ->  // shower drizzle
                9

            500, 501, 520, 521, 531 ->  // ragged shower rain
                11

            502, 503, 504, 522 ->  // heavy intensity shower rain
                12

            511 ->  // freezing rain
                10

            600, 620 -> 14
            601, 621 -> 16
            602, 622 -> 41
            611, 612 -> 18
            615, 616 -> 5
            741 ->  // fog
                20

            711, 762 ->  // volcanic ash
                22

            701, 721 ->  // haze
                21

            731, 751, 761 ->  // dust
                19

            771 ->  // squalls
                23

            781 ->  // tornado
                0

            800 ->  // clear sky
                if (icon.endsWith("n")) 31 else 32

            801 ->  // few clouds
                if (icon.endsWith("n")) 33 else 34

            802 ->  // scattered clouds
                if (icon.endsWith("n")) 27 else 28

            803, 804 ->  // overcast clouds
                if (icon.endsWith("n")) 29 else 30

            900 -> 0
            901 -> 1
            902 -> 2
            903 -> 25
            904 -> 36
            905 -> 24
            906 -> 17
            else ->  // hail
                -1
        }
    }

    private val aPIKey: String?
        get() {
            val customKey: String = WeatherConfig.getOwmKey(mContext).toString()
            if (!TextUtils.isEmpty(customKey)) {
                return customKey
            }
            return null
        }

    override fun shouldRetry(): Boolean {
        return false
    }

    companion object {
        private const val TAG = "OpenWeatherMapProvider"

        private const val FORECAST_DAYS = 5
        private const val URL_WEATHER =
            "https://api.openweathermap.org/data/2.5/weather?%s&mode=json&units=%s&lang=%s&cnt=" + FORECAST_DAYS + "&appid=%s"

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
