package com.drdisagree.iconify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.drdisagree.iconify.common.Preferences.XPOSED_ONLY_MODE
import com.drdisagree.iconify.config.Prefs
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.activities.OnboardingActivity
import com.drdisagree.iconify.utils.ModuleUtil
import com.drdisagree.iconify.utils.RootUtil
import com.drdisagree.iconify.utils.SystemUtil
import com.drdisagree.iconify.utils.overlay.OverlayUtil
import com.google.android.material.color.DynamicColors
import com.topjohnwu.superuser.Shell

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var keepShowing = true

    private val runner = Runnable {
        Shell.getShell { _ ->
            val intent: Intent

            val isRooted = RootUtil.deviceProperlyRooted()
            val isModuleInstalled = ModuleUtil.moduleExists()
            val isOverlayInstalled = OverlayUtil.overlayExists()
            var isXposedOnlyMode = Prefs.getBoolean(XPOSED_ONLY_MODE, false)
            val isVersionCodeCorrect = BuildConfig.VERSION_CODE == SystemUtil.getSavedVersionCode()

            if (isRooted) {
                if (isOverlayInstalled) {
                    Prefs.putBoolean(XPOSED_ONLY_MODE, false)
                } else if (isModuleInstalled) {
                    Prefs.putBoolean(XPOSED_ONLY_MODE, true)
                    isXposedOnlyMode = true
                }
            }

            val isModuleProperlyInstalled =
                isModuleInstalled && (isOverlayInstalled || isXposedOnlyMode)

            if (SKIP_TO_HOMEPAGE_FOR_TESTING || (isRooted && isModuleProperlyInstalled && isVersionCodeCorrect)) {
                keepShowing = false
                intent = Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                keepShowing = false
                intent = Intent(this@SplashActivity, OnboardingActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { keepShowing }
        DynamicColors.applyToActivitiesIfAvailable(application)

        Thread(runner).start()
    }

    companion object {
        private const val SKIP_INSTALLATION = false
        val SKIP_TO_HOMEPAGE_FOR_TESTING = SKIP_INSTALLATION && BuildConfig.DEBUG
    }
}