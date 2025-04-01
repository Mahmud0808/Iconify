package com.drdisagree.iconify.ui.activities

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Color
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.ActivityWeatherBinding
import com.drdisagree.iconify.ui.adapters.ForecastDayAdapter
import com.drdisagree.iconify.ui.adapters.ForecastHourAdapter
import com.drdisagree.iconify.ui.base.BaseActivity
import com.drdisagree.iconify.utils.OmniJawsClient
import com.drdisagree.iconify.utils.weather.WeatherContentProvider
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WeatherActivity : BaseActivity(), OmniJawsClient.OmniJawsObserver {

    private lateinit var mWeatherClient: OmniJawsClient
    private lateinit var binding: ActivityWeatherBinding
    private var mForecastDayAdapter: ForecastDayAdapter? = null
    private var mForecastHourAdapter: ForecastHourAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)

        binding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        mWeatherClient = OmniJawsClient(this)
        mForecastDayAdapter = ForecastDayAdapter(mWeatherClient)
        mForecastHourAdapter = ForecastHourAdapter(mWeatherClient)
        updateHourColor()

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finishAffinity()
            }
        })

        binding.settings.setOnClickListener { startActivity(mWeatherClient.getSettingsIntent()) }
        binding.refresh.setOnClickListener { forceRefresh() }

        binding.hourlyForecastRecycler.setAdapter(mForecastHourAdapter)
        binding.hourlyForecastRecycler.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        binding.dailyForecastRecycler.setAdapter(mForecastDayAdapter)
        binding.dailyForecastRecycler.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )

        startProgress()

        queryAndUpdateWeather()
    }

    @SuppressLint("SetTextI18n")
    private fun updateViews() {
        // Title

        binding.currentLocation.text = mWeatherClient.mCachedInfo!!.city

        // Current Condition
        binding.currentTemperature.text = mWeatherClient.mCachedInfo!!.temp
        binding.currentTemperatureUnit.text = mWeatherClient.mCachedInfo!!.tempUnits
        binding.currentCondition.text = getWeatherCondition()
        binding.currentConditionIcon.setImageDrawable(
            mWeatherClient.getWeatherConditionImage(
                mWeatherClient.mCachedInfo!!.conditionCode
            )
        )

        // Wind and Humidity
        binding.currentWind.text =
            mWeatherClient.mCachedInfo!!.windSpeed + " " + mWeatherClient.mCachedInfo!!.windUnits
        binding.currentWindDirection.text = mWeatherClient.mCachedInfo!!.pinWheel
        binding.currentHumidity.text = mWeatherClient.mCachedInfo!!.humidity

        // Provider Info
        binding.currentProvider.text = mWeatherClient.mCachedInfo!!.provider
        val format = if (DateFormat.is24HourFormat(this)) "HH:mm" else "hh:mm a"
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        binding.lastUpdate.text = sdf.format(mWeatherClient.mCachedInfo!!.timeStamp)
    }

    private fun getWeatherCondition(): String {
        val formattedConditionLowercase = mWeatherClient.mCachedInfo!!.condition!!.lowercase()

        val formattedCondition = when {
            formattedConditionLowercase.contains("clouds") -> {
                appContextLocale.getString(R.string.weather_condition_clouds)
            }

            formattedConditionLowercase.contains("rain") -> {
                appContextLocale.getString(R.string.weather_condition_rain)
            }

            formattedConditionLowercase.contains("clear") -> {
                appContextLocale.getString(R.string.weather_condition_clear)
            }

            formattedConditionLowercase.contains("storm") -> {
                appContextLocale.getString(R.string.weather_condition_storm)
            }

            formattedConditionLowercase.contains("snow") -> {
                appContextLocale.getString(R.string.weather_condition_snow)
            }

            formattedConditionLowercase.contains("wind") -> {
                appContextLocale.getString(R.string.weather_condition_wind)
            }

            formattedConditionLowercase.contains("mist") -> {
                appContextLocale.getString(R.string.weather_condition_mist)
            }

            else -> {
                mWeatherClient.mCachedInfo!!.condition.toString()
            }
        }
        return formattedCondition
    }

    public override fun onResume() {
        super.onResume()
        mWeatherClient.addObserver(this)
        queryAndUpdateWeather()
    }

    public override fun onPause() {
        super.onPause()
        mWeatherClient.removeObserver(this)
    }

    override fun weatherUpdated() {
        queryAndUpdateWeather()
    }

    override fun weatherError(errorReason: Int) {
    }

    private fun queryAndUpdateWeather() {
        stopProgress()
        mWeatherClient.queryWeather()
        if (mWeatherClient.mCachedInfo!!.hourlyForecasts!!.size >= 2) {
            mForecastHourAdapter!!.updateList(mWeatherClient.mCachedInfo!!.hourlyForecasts)
            binding.hourlyForecastCard.visibility = View.VISIBLE
            binding.hourlyForecastRecycler.scrollToPosition(0)
        } else {
            binding.hourlyForecastCard.visibility = View.GONE
        }
        if (mWeatherClient.mCachedInfo!!.dailyForecasts!!.isNotEmpty()) {
            mForecastDayAdapter!!.updateList(mWeatherClient.mCachedInfo!!.dailyForecasts)
            binding.dailyForecastCard.visibility = View.VISIBLE
        } else {
            binding.dailyForecastCard.visibility = View.GONE
        }
        updateViews()
    }

    private fun forceRefresh() {
        if (mWeatherClient.isOmniJawsEnabled) {
            startProgress()
            val values = ContentValues()
            values.put(WeatherContentProvider.COLUMN_FORCE_REFRESH, true)
            this.contentResolver.update(
                OmniJawsClient.CONTROL_URI,
                values, "", null
            )

            //WeatherUpdateService.scheduleUpdateNow(getContext());
        }
    }

    private fun startProgress() {
        binding.progress.visibility = View.VISIBLE
        binding.weatherLayout.visibility = View.GONE
    }

    private fun stopProgress() {
        binding.progress.visibility = View.GONE
        binding.weatherLayout.visibility = View.VISIBLE
    }

    private val currentHourColor: Int
        get() {
            val hourOfDay = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            return Color.parseColor(BACKGROUND_SPECTRUM[hourOfDay])
        }

    private val currentCardColor: Int
        get() {
            val hourOfDay = Calendar.getInstance()[Calendar.HOUR_OF_DAY]
            return Color.parseColor(BACKGROUND_CARD_SPECTRUM[hourOfDay])
        }

    @Suppress("deprecation")
    private fun updateHourColor() {
        window.decorView.setBackgroundColor(currentHourColor)
        window.navigationBarColor = currentHourColor
        window.statusBarColor = currentHourColor
        binding.hourlyForecastCard.setCardBackgroundColor(currentCardColor)
        binding.hourlyForecastCard.strokeColor = currentCardColor
        binding.dailyForecastCard.setCardBackgroundColor(currentCardColor)
        binding.dailyForecastCard.strokeColor = currentCardColor
    }

    companion object {
        private const val TAG = "WeatherActivity"
        private const val DEBUG = false

        /** The background colors of the app, it changes thru out the day to mimic the sky.  */
        val BACKGROUND_SPECTRUM: Array<String> = arrayOf(
            "#212121", "#27232e", "#2d253a",
            "#332847", "#382a53", "#3e2c5f", "#442e6c", "#393a7a", "#2e4687", "#235395", "#185fa2",
            "#0d6baf", "#0277bd", "#0d6cb1", "#1861a6", "#23569b", "#2d4a8f", "#383f84", "#433478",
            "#3d3169", "#382e5b", "#322b4d", "#2c273e", "#272430"
        )
        val BACKGROUND_CARD_SPECTRUM: Array<String> = arrayOf(
            "#171717", "#1b1820", "#201a29",
            "#241c32", "#271d3a", "#2b1f42", "#30204c", "#282955", "#20315e", "#183a68", "#114271",
            "#094b7a", "#015384", "#094c7c", "#114474", "#183c6c", "#203464", "#272c5c", "#2f2454",
            "#2b224a", "#272040", "#231e36", "#1f1b2b", "#1b1922"
        )
    }
}
