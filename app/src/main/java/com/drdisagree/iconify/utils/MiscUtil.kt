package com.drdisagree.iconify.utils

import android.content.Context
import com.drdisagree.iconify.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object MiscUtil {

    @JvmStatic
    fun showSystemUiRestartDialog(context: Context) {
        MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.systemui_restart_required_title))
            .setMessage(context.getString(R.string.systemui_restart_required_desc))
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }
}