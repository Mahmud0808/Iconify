package com.drdisagree.iconify.xposed.modules.extras.utils.misc

import android.content.Context
import android.content.res.Resources

@Suppress("DEPRECATION")
abstract class FakeIntegerResource(context: Context) : Resources(
    context.assets,
    context.resources.displayMetrics,
    context.resources.configuration
) {
    abstract override fun getInteger(id: Int): Int
}