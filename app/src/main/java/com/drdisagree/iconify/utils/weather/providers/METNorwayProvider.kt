package com.drdisagree.iconify.utils.weather.providers

import android.content.Context
import android.location.Location
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.utils.weather.AbstractWeatherProvider
import com.drdisagree.iconify.utils.weather.WeatherInfo
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.max

class METNorwayProvider(context: Context?) : AbstractWeatherProvider(context!!) {

    override fun getLocationWeather(location: Location?, metric: Boolean): WeatherInfo? {
        val coordinates = java.lang.String.format(
            Locale.US,
            PART_COORDINATES,
            location!!.latitude,
            location!!.longitude
        )
        return getAllWeather(coordinates, metric)
    }

    override fun getCustomWeather(lat: String?, lon: String?, metric: Boolean): WeatherInfo? {
        val coordinates =
            java.lang.String.format(Locale.US, PART_COORDINATES, lat!!.toFloat(), lon!!.toFloat())
        return getAllWeather(coordinates, metric)
    }

    private fun getAllWeather(coordinates: String?, metric: Boolean): WeatherInfo? {
        val url = URL_WEATHER + coordinates
        val response: String = retrieve(url, arrayOf("User-Agent", "Iconify/${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}"))

        log(TAG, "URL = $url returning a response of $response")
        Log.w(TAG, "Response: $response")

        try {
            val timeseries =
                JSONObject(response).getJSONObject("properties").getJSONArray("timeseries")
            val weather = timeseries.getJSONObject(0).getJSONObject("data").getJSONObject("instant")
                .getJSONObject("details")

            val symbolCode =
                timeseries.getJSONObject(0).getJSONObject("data").getJSONObject("next_1_hours")
                    .getJSONObject("summary").getString("symbol_code")
            val conditionDescription = getWeatherCondition(symbolCode)
            var weatherCode = arrayWeatherIconToCode[getPriorityCondition(symbolCode)]

            // Check Available Night Icon
            if (symbolCode.contains("_night") && (weatherCode == 30 || weatherCode == 32 || weatherCode == 34)) {
                weatherCode -= 1
            }

            val city = getWeatherDataLocality(coordinates!!)
            val hourlyForecasts: ArrayList<WeatherInfo.HourForecast> =
                ArrayList()

            val w: WeatherInfo = WeatherInfo(
                mContext,  /* id */
                coordinates,  /* cityId */
                city!!,  /* condition */
                conditionDescription,  /* conditionCode */
                weatherCode,  /* temperature */
                convertTemperature(weather.getDouble("air_temperature"), metric),  /* humidity */
                weather.getDouble("relative_humidity").toFloat(),  /* wind */
                convertWindSpeed(weather.getDouble("wind_speed"), metric),  /* windDir */
                weather.getDouble("wind_from_direction").toInt(),
                metric,
                parseHourlyForecasts(timeseries, metric),
                parseForecasts(timeseries, metric),
                System.currentTimeMillis()
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
    private fun parseForecasts(timeseries: JSONArray, metric: Boolean): ArrayList<WeatherInfo.DayForecast> {
        val result: ArrayList<WeatherInfo.DayForecast> = ArrayList(5)
        val count = timeseries.length()

        if (count == 0) {
            throw JSONException("Empty forecasts array")
        }

        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DATE, -1)
        val yesterday = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

        var whileIndex = 0

        while (convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")).contains(
                yesterday
            )
        ) {
            whileIndex++
        }

        val endDay =
            (whileIndex == 0) && isEndDay(
                convertTimeZone(
                    timeseries.getJSONObject(whileIndex).getString("time")
                )
            )

        for (i in 0..4) {
            var item: WeatherInfo.DayForecast
            try {
                // temp = temperature
                var temp_max = Double.MIN_VALUE
                var temp_min = Double.MAX_VALUE
                val day: String = getDay(i)
                var symbolCode = 0
                var scSixToTwelve = 0 // symbolCode next_6_hours at 06:00
                var scTwelveToEighteen = 0 // symbolCode next_6_hours at 12:00
                var scSixToEighteen = 0 // symbolCode next_12_hours at 06:00
                var hasFastCondition =
                    false // If true, there is no need to calculate "symbolCode" and "conditionDescription".
                var conditionDescription = ""
                var cdSixToEighteen = "" // conditionDescription at 06:00 or 12:00

                while (convertTimeZone(
                        timeseries.getJSONObject(whileIndex).getString("time")
                    ).contains(day)
                ) {
                    val tempI = timeseries.getJSONObject(whileIndex).getJSONObject("data")
                        .getJSONObject("instant").getJSONObject("details")
                        .getDouble("air_temperature")

                    if (tempI > temp_max) {
                        temp_max = tempI
                    }
                    if (tempI < temp_min) {
                        temp_min = tempI
                    }

                    val hasOneHour = timeseries.getJSONObject(whileIndex).getJSONObject("data")
                        .has("next_1_hours")
                    val hasSixHours = timeseries.getJSONObject(whileIndex).getJSONObject("data")
                        .has("next_6_hours")
                    val hasTwelveHours = timeseries.getJSONObject(whileIndex).getJSONObject("data")
                        .has("next_12_hours")

                    hasFastCondition =
                        scSixToEighteen != 0 || (scSixToTwelve != 0 && scTwelveToEighteen != 0)

                    if (!hasFastCondition && ((i == 0 && endDay) || isMorningOrAfternoon(
                            convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")),
                            hasOneHour
                        ))
                    ) {
                        val stepHours = if (hasOneHour) "next_1_hours" else "next_6_hours"

                        val stepTextSymbolCode =
                            timeseries.getJSONObject(whileIndex).getJSONObject("data")
                                .getJSONObject(stepHours).getJSONObject("summary")
                                .getString("symbol_code")
                        val stepSymbolCode = getPriorityCondition(stepTextSymbolCode)

                        if (stepSymbolCode > symbolCode) {
                            symbolCode = stepSymbolCode
                            conditionDescription = stepTextSymbolCode
                        }

                        if (hasSixHours || hasTwelveHours) {
                            if (convertTimeZone(
                                    timeseries.getJSONObject(whileIndex).getString("time")
                                ).contains("T06")
                            ) {
                                val textSymbolCode =
                                    timeseries.getJSONObject(whileIndex).getJSONObject("data")
                                        .getJSONObject(if (hasTwelveHours) "next_12_hours" else "next_6_hours")
                                        .getJSONObject("summary").getString("symbol_code")
                                if (hasTwelveHours) {
                                    scSixToEighteen = getPriorityCondition(textSymbolCode)
                                    cdSixToEighteen =
                                        timeseries.getJSONObject(whileIndex).getJSONObject("data")
                                            .getJSONObject("next_12_hours").getJSONObject("summary")
                                            .getString("symbol_code")
                                } else {
                                    scSixToTwelve = getPriorityCondition(textSymbolCode)
                                    cdSixToEighteen = textSymbolCode
                                }
                            } else if (scSixToTwelve != 0 && convertTimeZone(
                                    timeseries.getJSONObject(
                                        whileIndex
                                    ).getString("time")
                                ).contains("T12")
                            ) {
                                val textSymbolCode =
                                    timeseries.getJSONObject(whileIndex).getJSONObject("data")
                                        .getJSONObject("next_6_hours").getJSONObject("summary")
                                        .getString("symbol_code")
                                scTwelveToEighteen = getPriorityCondition(textSymbolCode)

                                if (scSixToTwelve < scTwelveToEighteen) {
                                    cdSixToEighteen = textSymbolCode
                                }
                            }
                        }
                    }
                    whileIndex++
                }

                if (hasFastCondition) {
                    symbolCode = if ((scSixToEighteen != 0)) scSixToEighteen else max(
                        scSixToTwelve.toDouble(),
                        scTwelveToEighteen.toDouble()
                    )
                        .toInt()
                    conditionDescription = cdSixToEighteen
                }

                val formattedConditionDescription = getWeatherCondition(conditionDescription)

                item = WeatherInfo.DayForecast( /* low */
                    convertTemperature(temp_min, metric),  /* high */
                    convertTemperature(temp_max, metric),  /* condition */
                    formattedConditionDescription,  /* conditionCode */
                    arrayWeatherIconToCode[symbolCode],
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
                val item = WeatherInfo.DayForecast( /* low */
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
        timeseries: JSONArray,
        metric: Boolean
    ): ArrayList<WeatherInfo.HourForecast> {
        val result: ArrayList<WeatherInfo.HourForecast> = ArrayList(10)

        val count = timeseries.length()
        if (count == 0) {
            throw JSONException("Empty forecasts array")
        }

        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        for (i in 0 until count) {
            val item = timeseries.getJSONObject(i)
            val timeString = item.getString("time")
            val time = LocalDateTime.parse(timeString, formatter)

            val data = item.getJSONObject("data")
            val next1Hours = data.optJSONObject("next_1_hours")

            if (next1Hours != null) {
                if (time.isAfter(now)) {
                    val instant = data.getJSONObject("instant").getJSONObject("details")
                    val temperature = instant.getDouble("air_temperature")
                    val symbolCode = next1Hours.getJSONObject("summary").getString("symbol_code")
                    val formattedConditionDescription = getWeatherCondition(symbolCode)

                    val hour = WeatherInfo.HourForecast( /* temp */
                        convertTemperature(temperature, metric),  /* condition */
                        formattedConditionDescription,  /* conditionCode */
                        arrayWeatherIconToCode[getPriorityCondition(symbolCode)],  /* date */
                        timeString,
                        metric
                    )

                    result.add(hour)

                    if (result.size >= 10) {
                        break
                    }
                }
            }
        }
        return result
    }

    init {
        initTimeZoneFormat()
    }

    private fun getPriorityCondition(condition: String): Int {
        var cond = condition
        val endIndex = cond.indexOf("_")
        if (endIndex != -1) {
            cond = condition.substring(0, endIndex)
        }
        return SYMBOL_CODE_MAPPING.getOrDefault(cond, 0)
    }

    private fun getWeatherCondition(condition: String): String {
        var cond = condition
        val endIndex = cond.indexOf("_")
        if (endIndex != -1) {
            cond = cond.substring(0, endIndex)
        }
        return WEATHER_CONDITION_MAPPING.getOrDefault(cond, cond)
    }

    private fun initTimeZoneFormat() {
        gmt0Format.timeZone = TimeZone.getTimeZone("GMT")
        userTimeZoneFormat.timeZone = TimeZone.getDefault()
    }

    private fun convertTimeZone(tmp: String): String {
        return try {
            userTimeZoneFormat.format(gmt0Format.parse(tmp))
        } catch (e: ParseException) {
            tmp
        }
    }

    private fun isMorningOrAfternoon(time: String, hasOneHour: Boolean): Boolean {
        val endI = if (hasOneHour) 17 else 13
        for (i in 6..endI) {
            if (time.contains(if ((i < 10)) "T0" else "T$i")) {
                return true
            }
        }
        return false
    }

    private fun isEndDay(time: String): Boolean {
        for (i in 18..23) {
            if (time.contains("T$i")) {
                return true
            }
        }
        return false
    }

    override fun shouldRetry(): Boolean {
        return false
    }

    companion object {
        private const val TAG = "METNorwayProvider"

        private const val URL_WEATHER =
            "https://api.met.no/weatherapi/locationforecast/2.0/compact?"

        private val gmt0Format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        private val userTimeZoneFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)

        private val WEATHER_CONDITION_MAPPING = HashMap<String, String>()

        init {
            WEATHER_CONDITION_MAPPING["clearsky"] = "Clear Sky"
            WEATHER_CONDITION_MAPPING["fair"] = "Mostly Clear"
            WEATHER_CONDITION_MAPPING["partlycloudy"] = "Mostly Cloudy"
            WEATHER_CONDITION_MAPPING["cloudy"] = "Cloudy"
            WEATHER_CONDITION_MAPPING["rainshowers"] = "Showers"
            WEATHER_CONDITION_MAPPING["rainshowersandthunder"] = "Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["sleetshowers"] = "Sleet Showers"
            WEATHER_CONDITION_MAPPING["snowshowers"] = "Snow Showers"
            WEATHER_CONDITION_MAPPING["rain"] = "Rainfall"
            WEATHER_CONDITION_MAPPING["heavyrain"] = "Heavy Rainfall"
            WEATHER_CONDITION_MAPPING["heavyrainandthunder"] = "Heavy Rainfall and Thunderstorms"
            WEATHER_CONDITION_MAPPING["sleet"] = "Sleet"
            WEATHER_CONDITION_MAPPING["snow"] = "Snowfall"
            WEATHER_CONDITION_MAPPING["snowandthunder"] = "Snowfall and Thunderstorms"
            WEATHER_CONDITION_MAPPING["fog"] = "Foggy"
            WEATHER_CONDITION_MAPPING["sleetshowersandthunder"] = "Sleet Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["snowshowersandthunder"] = "Snow Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["rainandthunder"] = "Rainfall and Thunderstorms"
            WEATHER_CONDITION_MAPPING["sleetandthunder"] = "Sleet and Thunderstorms"
            WEATHER_CONDITION_MAPPING["lightrainshowersandthunder"] =
                "Light Rain Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["heavyrainshowersandthunder"] =
                "Heavy Rain Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["lightssleetshowersandthunder"] =
                "Light Sleet Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["heavysleetshowersandthunder"] =
                "Heavy Sleet Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["lightssnowshowersandthunder"] =
                "Light Snow Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["heavysnowshowersandthunder"] =
                "Heavy Snow Showers and Thunderstorms"
            WEATHER_CONDITION_MAPPING["lightrainandthunder"] = "Light Rain and Thunderstorms"
            WEATHER_CONDITION_MAPPING["lightsleetandthunder"] = "Light Sleet and Thunderstorms"
            WEATHER_CONDITION_MAPPING["heavysleetandthunder"] = "Heavy Sleet and Thunderstorms"
            WEATHER_CONDITION_MAPPING["lightsnowandthunder"] = "Light Snow and Thunderstorms"
            WEATHER_CONDITION_MAPPING["heavysnowandthunder"] = "Heavy Snow and Thunderstorms"
            WEATHER_CONDITION_MAPPING["lightrainshowers"] = "Light Rain Showers"
            WEATHER_CONDITION_MAPPING["heavyrainshowers"] = "Heavy Rain Showers"
            WEATHER_CONDITION_MAPPING["lightsleetshowers"] = "Light Sleet Showers"
            WEATHER_CONDITION_MAPPING["heavysleetshowers"] = "Heavy Sleet Showers"
            WEATHER_CONDITION_MAPPING["lightsnowshowers"] = "Light Snow Showers"
            WEATHER_CONDITION_MAPPING["heavysnowshowers"] = "Heavy Snow Showers"
            WEATHER_CONDITION_MAPPING["lightrain"] = "Light Rain"
            WEATHER_CONDITION_MAPPING["lightsleet"] = "Light Sleet"
            WEATHER_CONDITION_MAPPING["heavysleet"] = "Heavy Sleet"
            WEATHER_CONDITION_MAPPING["lightsnow"] = "Light Snow"
            WEATHER_CONDITION_MAPPING["heavysnow"] = "Heavy Snow"
        }

        private val SYMBOL_CODE_MAPPING = HashMap<String, Int>()

        init {
            SYMBOL_CODE_MAPPING["clearsky"] = 1
            SYMBOL_CODE_MAPPING["fair"] = 2
            SYMBOL_CODE_MAPPING["partlycloudy"] = 3
            SYMBOL_CODE_MAPPING["cloudy"] = 4
            SYMBOL_CODE_MAPPING["rainshowers"] = 5
            SYMBOL_CODE_MAPPING["rainshowersandthunder"] = 6
            SYMBOL_CODE_MAPPING["sleetshowers"] = 7
            SYMBOL_CODE_MAPPING["snowshowers"] = 8
            SYMBOL_CODE_MAPPING["rain"] = 9
            SYMBOL_CODE_MAPPING["heavyrain"] = 10
            SYMBOL_CODE_MAPPING["heavyrainandthunder"] = 11
            SYMBOL_CODE_MAPPING["sleet"] = 12
            SYMBOL_CODE_MAPPING["snow"] = 13
            SYMBOL_CODE_MAPPING["snowandthunder"] = 14
            SYMBOL_CODE_MAPPING["fog"] = 15
            SYMBOL_CODE_MAPPING["sleetshowersandthunder"] = 20
            SYMBOL_CODE_MAPPING["snowshowersandthunder"] = 21
            SYMBOL_CODE_MAPPING["rainandthunder"] = 22
            SYMBOL_CODE_MAPPING["sleetandthunder"] = 23
            SYMBOL_CODE_MAPPING["lightrainshowersandthunder"] = 24
            SYMBOL_CODE_MAPPING["heavyrainshowersandthunder"] = 25
            SYMBOL_CODE_MAPPING["lightssleetshowersandthunder"] = 26
            SYMBOL_CODE_MAPPING["heavysleetshowersandthunder"] = 27
            SYMBOL_CODE_MAPPING["lightssnowshowersandthunder"] = 28
            SYMBOL_CODE_MAPPING["heavysnowshowersandthunder"] = 29
            SYMBOL_CODE_MAPPING["lightrainandthunder"] = 30
            SYMBOL_CODE_MAPPING["lightsleetandthunder"] = 31
            SYMBOL_CODE_MAPPING["heavysleetandthunder"] = 32
            SYMBOL_CODE_MAPPING["lightsnowandthunder"] = 33
            SYMBOL_CODE_MAPPING["heavysnowandthunder"] = 34
            SYMBOL_CODE_MAPPING["lightrainshowers"] = 40
            SYMBOL_CODE_MAPPING["heavyrainshowers"] = 41
            SYMBOL_CODE_MAPPING["lightsleetshowers"] = 42
            SYMBOL_CODE_MAPPING["heavysleetshowers"] = 43
            SYMBOL_CODE_MAPPING["lightsnowshowers"] = 44
            SYMBOL_CODE_MAPPING["heavysnowshowers"] = 45
            SYMBOL_CODE_MAPPING["lightrain"] = 46
            SYMBOL_CODE_MAPPING["lightsleet"] = 47
            SYMBOL_CODE_MAPPING["heavysleet"] = 48
            SYMBOL_CODE_MAPPING["lightsnow"] = 49
            SYMBOL_CODE_MAPPING["heavysnow"] = 50
        }

        /* Thanks Chronus(app) */
        private val arrayWeatherIconToCode = intArrayOf(
            -1,  /*1*/
            32,  /*2*/
            34,  /*3*/
            30,  /*4*/
            26,  /*5*/
            40,  /*6*/
            39,  /*7*/
            6,  /*8*/
            14,  /*9*/
            11,  /*10*/
            12,  /*11*/
            4,  /*12*/
            18,  /*13*/
            16,  /*14*/
            15,  /*15*/
            20,  /*16*/
            -1,  /*17*/
            -1,  /*18*/
            -1,  /*19*/
            -1,  /*20*/
            42,  /*21*/
            42,  /*22*/
            4,  /*23*/
            6,  /*24*/
            39,  /*25*/
            39,  /*26*/
            42,  /*27*/
            42,  /*28*/
            42,  /*29*/
            42,  /*30*/
            4,  /*31*/
            6,  /*32*/
            6,  /*33*/
            15,  /*34*/
            15,  /*35*/
            -1,  /*36*/
            -1,  /*37*/
            -1,  /*38*/
            -1,  /*39*/
            -1,  /*40*/
            40,  /*41*/
            40,  /*42*/
            6,  /*43*/
            6,  /*44*/
            14,  /*45*/
            14,  /*46*/
            9,  /*47*/
            18,  /*48*/
            18,  /*49*/
            16,  /*50*/
            16
        )

        private fun convertTemperature(value: Double, metric: Boolean): Float {
            var value = value
            if (!metric) {
                value = (value * 1.8) + 32
            }
            return value.toFloat()
        }

        private fun convertWindSpeed(valueMs: Double, metric: Boolean): Float {
            return (valueMs * (if (metric) 3.6 else 2.2369362920544)).toFloat()
        }
    }
}