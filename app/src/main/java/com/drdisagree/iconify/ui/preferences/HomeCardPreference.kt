package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.SHOW_HOME_CARD
import com.drdisagree.iconify.config.RPrefs
import com.google.android.material.button.MaterialButton

class HomeCardPreference(context: Context, attrs: AttributeSet?) : Preference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.itemView.findViewById<MaterialButton>(R.id.button).setOnClickListener {
            holder.itemView
                .animate()
                .setDuration(400)
                .translationX(holder.itemView.width * 2f)
                .alpha(0f)
                .withEndAction {
                    RPrefs.putBoolean(SHOW_HOME_CARD, false)
                }
                .start()
        }
    }
}