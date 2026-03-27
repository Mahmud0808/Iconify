package com.drdisagree.iconify.core.ui.components.others

import android.content.Context
import android.widget.Toast

fun showComingSoonToast(context: Context) {
    Toast.makeText(
        context,
        "Coming soon!",
        Toast.LENGTH_SHORT
    ).show()
}