package com.drdisagree.iconify.ui.activities

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.drdisagree.iconify.databinding.ActivityOnboardingBinding
import com.drdisagree.iconify.ui.views.OnboardingView
import kotlin.system.exitProcess

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                try {
                    OnboardingView.navigateToPrevSlide()
                } catch (ignored: Exception) {
                    finish()
                    exitProcess(0)
                }
            }
        })
    }

    public override fun onResume() {
        super.onResume()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    public override fun onPause() {
        super.onPause()

        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
