package com.drdisagree.iconify.ui.base

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.utils.ThemeHelper
import com.drdisagree.iconify.utils.helper.LocaleHelper
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.shape.MaterialShapeDrawable

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(ThemeHelper.theme)
        super.onCreate(savedInstanceState)
        setupEdgeToEdge()
    }

    private fun setupEdgeToEdge() {
        try {
            (findViewById<View>(R.id.appBarLayout) as AppBarLayout).statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(
                    applicationContext
                )
        } catch (ignored: Exception) {
        }

        val window = window
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (getResources().configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            val viewGroup = getWindow().decorView.findViewById<ViewGroup>(android.R.id.content)

            ViewCompat.setOnApplyWindowInsetsListener(viewGroup) { v: View, windowInsets: WindowInsetsCompat ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val params = v.layoutParams as MarginLayoutParams

                v.setPadding(
                    params.leftMargin + insets.left,
                    0,
                    params.rightMargin + insets.right,
                    0
                )

                params.topMargin = 0
                params.bottomMargin = 0

                v.setLayoutParams(params)

                windowInsets
            }
        }
    }

    @Suppress("deprecation")
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}