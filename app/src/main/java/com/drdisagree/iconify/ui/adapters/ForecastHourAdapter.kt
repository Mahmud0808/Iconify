package com.drdisagree.iconify.ui.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.databinding.ViewListForecastHourItemBinding
import com.drdisagree.iconify.utils.OmniJawsClient
import java.text.SimpleDateFormat
import java.util.Locale

class ForecastHourAdapter(client: OmniJawsClient) :
    RecyclerView.Adapter<ForecastHourAdapter.ViewHolder>() {

    private val mList: MutableList<OmniJawsClient.HourForecast> =
        ArrayList()
    private val mWeatherClient: OmniJawsClient = client

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewListForecastHourItemBinding = ViewListForecastHourItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding, mWeatherClient)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val forecast: OmniJawsClient.HourForecast = mList[position]
        holder.bind(forecast)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    fun updateList(list: List<OmniJawsClient.HourForecast>?) {
        mList.clear()
        mList.addAll(list!!)
        notifyDataSetChanged()
    }

    class ViewHolder internal constructor(
        binding: ViewListForecastHourItemBinding,
        weatherClient: OmniJawsClient
    ) :
        RecyclerView.ViewHolder(binding.getRoot()) {
        private val binding: ViewListForecastHourItemBinding = binding
        private val mWeatherClient: OmniJawsClient = weatherClient

        @SuppressLint("SetTextI18n")
        fun bind(forecast: OmniJawsClient.HourForecast) {
            binding.forecastTime.setText(fotmatHour(forecast.time!!))
            binding.forecastIcon.setImageDrawable(mWeatherClient.getWeatherConditionImage(forecast.conditionCode))
            binding.forecastTemperature.setText(forecast.temperature + "°")
        }

        private fun fotmatHour(inputDate: String): String? {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val dayMonthFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

            try {
                val date = inputFormat.parse(inputDate)

                return dayMonthFormat.format(date)
            } catch (e: Exception) {
                Log.e("ForecastHourAdapter", "Error parsing hours", e)
                return null
            }
        }
    }
}
