package com.drdisagree.iconify.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.ViewListForecastDayItemBinding
import com.drdisagree.iconify.utils.OmniJawsClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ForecastDayAdapter(weatherClient: OmniJawsClient) :
    RecyclerView.Adapter<ForecastDayAdapter.ViewHolder>() {

    private val mList: MutableList<OmniJawsClient.DayForecast> =
        ArrayList()
    private val mWeatherClient: OmniJawsClient = weatherClient

    init {
        mWeatherClient.queryWeather()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewListForecastDayItemBinding = ViewListForecastDayItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, mWeatherClient)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast: OmniJawsClient.DayForecast = mList[position]
        holder.bind(forecast)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateList(list: List<OmniJawsClient.DayForecast>?) {
        mList.clear()
        mList.addAll(list!!)
        notifyDataSetChanged()
    }

    class ViewHolder internal constructor(
        binding: ViewListForecastDayItemBinding,
        weatherClient: OmniJawsClient
    ) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val binding: ViewListForecastDayItemBinding = binding
        private val mWeatherClient: OmniJawsClient = weatherClient

        @SuppressLint("SetTextI18n")
        fun bind(forecast: OmniJawsClient.DayForecast) {
            binding.forecastTime.setText(formatDate(forecast.date!!))
            binding.forecastIcon.setImageDrawable(mWeatherClient.getWeatherConditionImage(forecast.conditionCode))
            binding.forecastTemperature.setText((forecast.low + "° / " + forecast.high).toString() + "°")
        }

        private fun formatDate(inputDate: String): String? {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dayMonthFormat = SimpleDateFormat("dd MMM", Locale.getDefault())

            try {
                val date = inputFormat.parse(inputDate)
                val inputCalendar = Calendar.getInstance()
                inputCalendar.time = date

                val today = Calendar.getInstance()
                val tomorrow = Calendar.getInstance()
                tomorrow.add(Calendar.DAY_OF_YEAR, 1)

                val formattedDate = dayMonthFormat.format(date)

                if (isSameDay(inputCalendar, today)) {
                    return formattedDate + " " + appContextLocale.getString(R.string.omnijaws_today)
                } else if (isSameDay(inputCalendar, tomorrow)) {
                    return formattedDate + " " + appContextLocale.getString(R.string.omnijaws_tomorrow)
                } else {
                    val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    val dayOfWeek = dayOfWeekFormat.format(date)
                    return "$formattedDate $dayOfWeek"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return null
            }
        }

        private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
            return cal1[Calendar.YEAR] == cal2[Calendar.YEAR] &&
                    cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
        }
    }
}
