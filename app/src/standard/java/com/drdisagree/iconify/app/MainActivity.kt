package com.drdisagree.iconify.app

import android.util.Log
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.BitmapSubjectSegmenter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseMainActivity() {

    override fun initializeMLKit() {
        BitmapSubjectSegmenter(this).checkModelAvailability { response ->
            Log.d(
                "MLKit",
                "Model availability: ${response.areModulesAvailable()}"
            )
        }
    }
}