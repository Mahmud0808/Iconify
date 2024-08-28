package com.drdisagree.iconify.ui.preferences

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.utils.SystemUtils.restartDevice

class RebootReminderPreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        holder.findViewById(R.id.btn_reboot).setOnClickListener {
            val rebootingDialog = LoadingDialog(context)

            rebootingDialog.show(context.resources.getString(R.string.rebooting_desc))

            Handler(Looper.getMainLooper()).postDelayed({
                rebootingDialog.hide()
                restartDevice()
            }, 5000)
        }
    }
}