package com.drdisagree.iconify.utils.weather

import android.content.Context
import java.text.DecimalFormat
import java.util.Date

class WeatherInfo private constructor(
    context: Context, val id: String,
    val city: String, private val condition: String, val conditionCode: Int, var temperature: Float,
    private val humidity: Float, private val wind: Float, val windDirection: Int,
    private val metric: Boolean, val forecasts: ArrayList<DayForecast>, val timestamp: Long,
    var pinWheel: String
) {
    private val mContext: Context = context.applicationContext

    constructor(
        context: Context, id: String,
        city: String, condition: String, conditionCode: Int, temp: Float,
        humidity: Float, wind: Float, windDir: Int,
        metric: Boolean, forecasts: ArrayList<DayForecast>, timestamp: Long
    ) : this(
        context, id, city, condition, conditionCode, temp, humidity, wind, windDir,
        metric, forecasts, timestamp, ""
    ) {
        this.pinWheel = getFormattedWindDirection(windDir)
    }

    class WeatherLocation {
        var id: String? = null
        var city: String? = null
        var postal: String? = null
        var countryId: String? = null
        var country: String? = null
    }

    class DayForecast(
        val low: Float,
        val high: Float,
        val condition: String,
        val conditionCode: Int,
        var date: String,
        var metric: Boolean
    ) {
        fun getCondition(context: Context): String {
            return getCondition(context, conditionCode, condition)
        }
    }


    fun getCondition(): String {
        return getCondition(mContext, conditionCode, condition)
    }

    val formattedTimestamp: Date
        get() = Date(timestamp)

    val formattedHumidity: String
        get() = getFormattedValue(humidity, "%")

    val windSpeed: Float
        get() {
            if (wind < 0) {
                return 0F
            }
            return wind
        }

    private val formattedWindSpeed: String
        get() {
            if (wind < 0) {
                return "0"
            }
            return getFormattedValue(wind, if (metric) "km/h" else "m/h")
        }

    private fun getFormattedWindDirection(direction: Int): String {
        val value = ((direction / 22.5) + 0.5).toInt()
        val pw = WIND_DIRECTION[value % 16]
        return pw
    }

    private val temperatureUnit: String
        get() = "\u00b0" + (if (metric) "C" else "F")

    override fun toString(): String {
        val builder = StringBuilder()
        builder.append("WeatherInfo for ")
        builder.append(city)
        builder.append(" (")
        builder.append(id)
        builder.append(") @ ")
        builder.append(formattedTimestamp)
        builder.append(": ")
        builder.append(getCondition())
        builder.append("(")
        builder.append(conditionCode)
        builder.append("), temperature ")
        builder.append(
            getFormattedValue(
                temperature,
                temperatureUnit
            )
        )
        builder.append(", humidity ")
        builder.append(formattedHumidity)
        builder.append(", wind ")
        builder.append(formattedWindSpeed)
        builder.append(" at ")
        builder.append(windDirection)
        if (!forecasts.isEmpty()) {
            builder.append(", forecasts:")
        }
        for (i in forecasts.indices) {
            val d = forecasts[i]
            if (i != 0) {
                builder.append(";")
            }
            builder.append(" day ").append(i + 1).append(":")
            builder.append(d.date)
            builder.append(" high ").append(
                getFormattedValue(
                    d.high,
                    temperatureUnit
                )
            )
            builder.append(", low ").append(
                getFormattedValue(
                    d.low,
                    temperatureUnit
                )
            )
            builder.append(", ").append(d.condition)
            builder.append("(").append(d.conditionCode).append(")")
        }
        return builder.toString()
    }

    fun toSerializedString(): String {
        val builder = StringBuilder()
        builder.append(id).append('|')
        builder.append(city).append('|')
        builder.append(condition).append('|')
        builder.append(conditionCode).append('|')
        builder.append(temperature).append('|')
        builder.append(humidity).append('|')
        builder.append(wind).append('|')
        builder.append(windDirection).append('|')
        builder.append(metric).append('|')
        builder.append(timestamp).append('|')
        builder.append(pinWheel)
        if (!forecasts.isEmpty()) {
            serializeForecasts(builder)
        }
        return builder.toString()
    }

    private fun serializeForecasts(builder: StringBuilder) {
        builder.append('|')
        builder.append(forecasts.size)
        for (d in forecasts) {
            builder.append(';')
            builder.append(d.high).append(';')
            builder.append(d.low).append(';')
            builder.append(d.condition).append(';')
            builder.append(d.conditionCode).append(';')
            builder.append(d.date)
        }
    }

    companion object {
        private val sNoDigitsFormat = DecimalFormat("0")

        val WIND_DIRECTION: Array<String> = arrayOf(
            "N",
            "NNE",
            "NE",
            "ENE",
            "E",
            "ESE",
            "SE",
            "SSE",
            "S",
            "SSW",
            "SW",
            "WSW",
            "W",
            "WNW",
            "NW",
            "NNW"
        )

        private fun getCondition(context: Context, conditionCode: Int, condition: String): String {
            val res = context.resources
            val resId = res.getIdentifier("weather_$conditionCode", "string", context.packageName)
            if (resId != 0) {
                return res.getString(resId)
            }
            return condition
        }

        private fun getFormattedValue(value: Float, unit: String): String {
            if (java.lang.Float.isNaN(value)) {
                return "-"
            }
            var formatted = sNoDigitsFormat.format(value.toDouble())
            if (formatted == "-0") {
                formatted = "0"
            }
            return formatted + unit
        }

        fun fromSerializedString(context: Context, input: String?): WeatherInfo? {
            if (input == null) {
                return null
            }

            val parts = input.split("\\|".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            val hasForecast = parts.size == 12

            val conditionCode: Int
            val windDirection: Int
            val timestamp: Long
            val temperature: Float
            val humidity: Float
            val wind: Float
            val metric: Boolean
            val pinWheel: String
            var forecastParts: Array<String>? = null
            if (hasForecast) {
                forecastParts =
                    parts[11].split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            }
            val forecastItems: Int
            val forecasts = ArrayList<DayForecast>()

            // Parse the core data
            try {
                conditionCode = parts[3].toInt()
                temperature = parts[4].toFloat()
                humidity = parts[5].toFloat()
                wind = parts[6].toFloat()
                windDirection = parts[7].toInt()
                metric = parts[8].toBoolean()
                timestamp = parts[9].toLong()
                pinWheel = parts[10]
                forecastItems = forecastParts?.get(0)?.toInt() ?: 0
            } catch (e: NumberFormatException) {
                return null
            }

            if (hasForecast && (forecastItems == 0 || forecastParts!!.size != 5 * forecastItems + 1)) {
                return null
            }

            // Parse the forecast data
            try {
                for (item in 0 until forecastItems) {
                    val offset = item * 5 + 1
                    val day = DayForecast( /* low */
                        forecastParts!![offset + 1].toFloat(),  /* high */
                        forecastParts[offset].toFloat(),  /* condition */
                        forecastParts[offset + 2],  /* conditionCode */
                        forecastParts[offset + 3].toInt(),
                        forecastParts[offset + 4],
                        metric
                    )
                    if (!java.lang.Float.isNaN(day.low) && !java.lang.Float.isNaN(day.high) /*&& day.conditionCode >= 0*/) {
                        forecasts.add(day)
                    }
                }
            } catch (ignored: NumberFormatException) {
            }

            if (hasForecast && forecasts.isEmpty()) {
                return null
            }

            return WeatherInfo(
                context,  /* id */
                parts[0],  /* city */parts[1],  /* condition */parts[2],
                conditionCode, temperature,
                humidity, wind, windDirection, metric,  /* forecasts */
                forecasts, timestamp, pinWheel
            )
        }
    }
}
