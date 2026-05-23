package com.drdisagree.iconify.core.preferences

import android.app.Activity
import android.content.Context
import androidx.navigation.NavController

data class PrefParam<out T>(
    val key: String,
    val defValue: T,
    val newValue: T,
    val context: Context,
    val activity: Activity?,
    val prefController: PreferenceController,
    val navController: NavController
)