package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.EASTER_EGG
import com.drdisagree.iconify.config.RPrefs
import java.util.Date

class EasterPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    private var clickTimestamps = LongArray(NUM_CLICKS_REQUIRED)
    private var oldestIndex = 0
    private var nextIndex = 0

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<TextView>(R.id.title).setOnClickListener {
            onEasterViewClicked()
        }
    }

    private fun onEasterViewClicked() {
        val timeMillis = Date().time

        if (nextIndex == NUM_CLICKS_REQUIRED - 1 || oldestIndex > 0) {
            val diff = (timeMillis - clickTimestamps[oldestIndex]).toInt()
            if (diff < SECONDS_FOR_CLICKS * 1000) {
                if (!RPrefs.getBoolean(EASTER_EGG)) {
                    RPrefs.putBoolean(EASTER_EGG, true)

                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.toast_easter_egg),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        context.resources.getString(R.string.toast_easter_egg_activated),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                oldestIndex = 0
                nextIndex = 0
            } else {
                oldestIndex++
            }
        }

        clickTimestamps[nextIndex] = timeMillis
        nextIndex++

        if (nextIndex == NUM_CLICKS_REQUIRED) nextIndex = 0
        if (oldestIndex == NUM_CLICKS_REQUIRED) oldestIndex = 0
    }

    companion object {
        private const val SECONDS_FOR_CLICKS = 3.0
        private const val NUM_CLICKS_REQUIRED = 7
    }
}