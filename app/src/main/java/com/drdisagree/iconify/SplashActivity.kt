package com.drdisagree.iconify

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.drdisagree.iconify.common.Preferences.XPOSED_ONLY_MODE
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.activities.OnboardingActivity
import com.drdisagree.iconify.utils.ModuleUtils
import com.drdisagree.iconify.utils.RootUtils
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.xposed.modules.utils.BitmapSubjectSegmenter
import com.google.android.material.color.DynamicColors
import com.topjohnwu.superuser.Shell

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private var keepShowing = true
    private val runner = Runnable {
        Shell.getShell { _: Shell? ->
            val isRooted = RootUtils.deviceProperlyRooted()
            val isModuleInstalled = ModuleUtils.moduleExists()
            val isOverlayInstalled = OverlayUtils.overlayExists()
            var isXposedOnlyMode = RPrefs.getBoolean(XPOSED_ONLY_MODE, false)
            val isVersionCodeCorrect = BuildConfig.VERSION_CODE == SystemUtils.savedVersionCode

            if (isRooted) {
                if (isOverlayInstalled) {
                    RPrefs.putBoolean(XPOSED_ONLY_MODE, false)
                } else if (isModuleInstalled) {
                    RPrefs.putBoolean(XPOSED_ONLY_MODE, true)
                    isXposedOnlyMode = true
                }
            }

            val isModuleProperlyInstalled = isModuleInstalled &&
                    (isOverlayInstalled || isXposedOnlyMode)

            val intent: Intent =
                if (SKIP_TO_HOMEPAGE_FOR_TESTING ||
                    isRooted &&
                    isModuleProperlyInstalled &&
                    isVersionCodeCorrect
                ) {
                    keepShowing = false
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        BitmapSubjectSegmenter(this@SplashActivity)
                    }
                    Intent(this@SplashActivity, MainActivity::class.java)
                } else {
                    keepShowing = false
                    Intent(this@SplashActivity, OnboardingActivity::class.java)
                }

            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen: SplashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { keepShowing }
        DynamicColors.applyToActivitiesIfAvailable(application)
        Thread(runner).start()
    }

    companion object {
        private const val SKIP_INSTALLATION = false
        val SKIP_TO_HOMEPAGE_FOR_TESTING = SKIP_INSTALLATION && BuildConfig.DEBUG

        init {
            Shell.enableVerboseLogging = BuildConfig.DEBUG
            if (Shell.getCachedShell() == null) {
                Shell.setDefaultBuilder(
                    Shell.Builder.create()
                        .setFlags(Shell.FLAG_MOUNT_MASTER)
                        .setFlags(Shell.FLAG_REDIRECT_STDERR)
                        .setTimeout(20)
                )
            }
        }
    }
}
