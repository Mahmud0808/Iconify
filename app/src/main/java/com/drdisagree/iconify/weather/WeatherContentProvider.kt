package com.drdisagree.iconify.weather

import android.annotation.SuppressLint
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.services.WeatherScheduler.scheduleUpdateNow

class WeatherContentProvider : ContentProvider() {
    private var mContext: Context? = null

    override fun onCreate(): Boolean {
        mContext = context
        sCachedWeatherInfo = mContext?.let { Config.getWeatherData(it) }
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val projectionType = sUriMatcher.match(uri)
        val result = MatrixCursor(resolveProjection(projection, projectionType))


        if (projectionType == URI_TYPE_SETTINGS) {
            result.newRow()
                .add(COLUMN_ENABLED, if (Config.isEnabled(mContext!!)) 1 else 0)
                .add(COLUMN_PROVIDER, Config.getProviderId(mContext!!))
                .add(COLUMN_INTERVAL, Config.getUpdateInterval(mContext!!))
                .add(COLUMN_UNITS, if (Config.isMetric(mContext!!)) 0 else 1)
                .add(
                    COLUMN_LOCATION,
                    if (Config.isCustomLocation(mContext!!)) Config.getLocationName(mContext!!) else ""
                )
                .add(
                    COLUMN_SETUP,
                    if (!Config.isSetupDone(mContext!!) && sCachedWeatherInfo == null) 0 else 1
                )
                .add(
                    COLUMN_ICON_PACK,
                    if (Config.getIconPack(mContext!!) != null) Config.getIconPack(mContext!!) else ""
                )


            return result
        } else if (projectionType == URI_TYPE_WEATHER) {
            val weather = sCachedWeatherInfo
            if (weather != null) {
                // current
                result.newRow()
                    .add(COLUMN_CURRENT_CITY, weather.city)
                    .add(COLUMN_CURRENT_CITY_ID, weather.id)
                    .add(COLUMN_CURRENT_CONDITION, weather.getCondition())
                    .add(COLUMN_CURRENT_HUMIDITY, weather.formattedHumidity)
                    .add(COLUMN_CURRENT_WIND_SPEED, weather.windSpeed)
                    .add(COLUMN_CURRENT_WIND_DIRECTION, weather.windDirection)
                    .add(COLUMN_CURRENT_TEMPERATURE, weather.temperature)
                    .add(COLUMN_CURRENT_TIME_STAMP, weather.timestamp.toString())
                    .add(COLUMN_CURRENT_PIN_WHEEL, weather.pinWheel)
                    .add(COLUMN_CURRENT_CONDITION_CODE, weather.conditionCode)

                // forecast
                for (day in weather.forecasts) {
                    result.newRow()
                        .add(COLUMN_FORECAST_CONDITION, day.getCondition(mContext!!))
                        .add(COLUMN_FORECAST_LOW, day.low)
                        .add(COLUMN_FORECAST_HIGH, day.high)
                        .add(COLUMN_FORECAST_CONDITION_CODE, day.conditionCode)
                        .add(COLUMN_FORECAST_DATE, day.date)
                }


                return result
            }
        }
        return null
    }

    private fun resolveProjection(projection: Array<String>?, uriType: Int): Array<String> {
        if (projection != null) return projection
        return when (uriType) {
            URI_TYPE_SETTINGS -> PROJECTION_DEFAULT_SETTINGS
            else -> {
                PROJECTION_DEFAULT_WEATHER
                PROJECTION_DEFAULT_SETTINGS
            }
        }
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        val projectionType = sUriMatcher.match(uri)
        if (projectionType == URI_TYPE_CONTROL) {
            if (values!!.containsKey(COLUMN_FORCE_REFRESH) && values.getAsBoolean(
                    COLUMN_FORCE_REFRESH
                )
            ) {
                if (DEBUG) Log.i(
                    TAG,
                    "update: $uri $values"
                )
                scheduleUpdateNow(mContext)
            }
        }
        return 0
    }

    companion object {
        private const val TAG = "WeatherService:WeatherContentProvider"
        private const val DEBUG = true

        @SuppressLint("StaticFieldLeak")
        var sCachedWeatherInfo: WeatherInfo? = null

        private const val URI_TYPE_WEATHER = 1
        private const val URI_TYPE_SETTINGS = 2
        private const val URI_TYPE_CONTROL = 3

        private const val COLUMN_CURRENT_CITY_ID = "city_id"
        private const val COLUMN_CURRENT_CITY = "city"
        private const val COLUMN_CURRENT_CONDITION = "condition"
        private const val COLUMN_CURRENT_TEMPERATURE = "temperature"
        private const val COLUMN_CURRENT_HUMIDITY = "humidity"
        private const val COLUMN_CURRENT_WIND_SPEED = "wind_speed"
        private const val COLUMN_CURRENT_WIND_DIRECTION = "wind_direction"
        private const val COLUMN_CURRENT_TIME_STAMP = "time_stamp"
        private const val COLUMN_CURRENT_CONDITION_CODE = "condition_code"
        private const val COLUMN_CURRENT_PIN_WHEEL = "pin_wheel"

        private const val COLUMN_FORECAST_LOW = "forecast_low"
        private const val COLUMN_FORECAST_HIGH = "forecast_high"
        private const val COLUMN_FORECAST_CONDITION = "forecast_condition"
        private const val COLUMN_FORECAST_CONDITION_CODE = "forecast_condition_code"
        private const val COLUMN_FORECAST_DATE = "forecast_date"

        private const val COLUMN_ENABLED = "enabled"
        private const val COLUMN_PROVIDER = "provider"
        private const val COLUMN_INTERVAL = "interval"
        private const val COLUMN_UNITS = "units"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_SETUP = "setup"
        private const val COLUMN_ICON_PACK = "icon_pack"

        const val COLUMN_FORCE_REFRESH: String = "update"

        private val PROJECTION_DEFAULT_WEATHER = arrayOf(
            COLUMN_CURRENT_CITY_ID,
            COLUMN_CURRENT_CITY,
            COLUMN_CURRENT_CONDITION,
            COLUMN_CURRENT_TEMPERATURE,
            COLUMN_CURRENT_HUMIDITY,
            COLUMN_CURRENT_WIND_SPEED,
            COLUMN_CURRENT_WIND_DIRECTION,
            COLUMN_CURRENT_TIME_STAMP,
            COLUMN_CURRENT_PIN_WHEEL,
            COLUMN_CURRENT_CONDITION_CODE,
            COLUMN_FORECAST_LOW,
            COLUMN_FORECAST_HIGH,
            COLUMN_FORECAST_CONDITION,
            COLUMN_FORECAST_CONDITION_CODE,
            COLUMN_FORECAST_DATE
        )

        private val PROJECTION_DEFAULT_SETTINGS = arrayOf(
            COLUMN_ENABLED,
            COLUMN_PROVIDER,
            COLUMN_INTERVAL,
            COLUMN_UNITS,
            COLUMN_LOCATION,
            COLUMN_SETUP,
            COLUMN_ICON_PACK
        )

        private const val AUTHORITY: String = "com.drdisagree.iconify.weatherprovider"

        private val sUriMatcher = UriMatcher(URI_TYPE_WEATHER)

        init {
            sUriMatcher.addURI(AUTHORITY, "weather", URI_TYPE_WEATHER)
            sUriMatcher.addURI(AUTHORITY, "settings", URI_TYPE_SETTINGS)
            sUriMatcher.addURI(AUTHORITY, "control", URI_TYPE_CONTROL)
        }

        fun updateCachedWeatherInfo(context: Context) {
            if (DEBUG) Log.d(TAG, "updateCachedWeatherInfo()")
            sCachedWeatherInfo = Config.getWeatherData(context)
            context.contentResolver.notifyChange(
                Uri.parse("content://$AUTHORITY/weather"), null
            )
        }
    }
}