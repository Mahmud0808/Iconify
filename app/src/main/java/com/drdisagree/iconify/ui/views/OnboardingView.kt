package com.drdisagree.iconify.ui.views

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieProperty
import com.airbnb.lottie.RenderMode
import com.airbnb.lottie.SimpleColorFilter
import com.airbnb.lottie.model.KeyPath
import com.airbnb.lottie.value.LottieValueCallback
import com.drdisagree.iconify.BaseSplashActivity.Companion.FORCE_OVERLAY_INSTALLATION
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.common.Const.TRANSITION_DELAY
import com.drdisagree.iconify.data.common.Dynamic.DATA_DIR
import com.drdisagree.iconify.data.common.Dynamic.skippedInstallation
import com.drdisagree.iconify.data.common.Preferences.FIRST_INSTALL
import com.drdisagree.iconify.data.common.Preferences.UPDATE_DETECTED
import com.drdisagree.iconify.data.common.Preferences.VER_CODE
import com.drdisagree.iconify.data.common.Preferences.XPOSED_ONLY_MODE
import com.drdisagree.iconify.data.common.Resources
import com.drdisagree.iconify.data.common.Resources.BACKUP_DIR
import com.drdisagree.iconify.data.common.Resources.MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.SIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_MODULE_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.TEMP_OVERLAY_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_DIR
import com.drdisagree.iconify.data.common.Resources.UNSIGNED_UNALIGNED_DIR
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.data.config.RPrefs.clearPref
import com.drdisagree.iconify.data.config.RPrefs.getBoolean
import com.drdisagree.iconify.data.config.RPrefs.getInt
import com.drdisagree.iconify.data.config.RPrefs.putBoolean
import com.drdisagree.iconify.data.config.RPrefs.putInt
import com.drdisagree.iconify.data.database.DynamicResourceDatabase
import com.drdisagree.iconify.data.entity.OnboardingPage
import com.drdisagree.iconify.data.repository.DynamicResourceRepository
import com.drdisagree.iconify.databinding.ViewOnboardingPageBinding
import com.drdisagree.iconify.ui.activities.MainActivity
import com.drdisagree.iconify.ui.adapters.OnboardingAdapter
import com.drdisagree.iconify.ui.core.Transform
import com.drdisagree.iconify.ui.dialogs.ErrorDialog
import com.drdisagree.iconify.ui.dialogs.InstallationDialog
import com.drdisagree.iconify.ui.utils.Animatoo.animateSlideLeft
import com.drdisagree.iconify.utils.FileUtils.copyAssets
import com.drdisagree.iconify.utils.ModuleUtils.createModule
import com.drdisagree.iconify.utils.ModuleUtils.flashModule
import com.drdisagree.iconify.utils.ModuleUtils.handleModule
import com.drdisagree.iconify.utils.ModuleUtils.moduleExists
import com.drdisagree.iconify.utils.RootUtils.deviceProperlyRooted
import com.drdisagree.iconify.utils.RootUtils.isDeviceRooted
import com.drdisagree.iconify.utils.SystemUtils.hasStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.requestStoragePermission
import com.drdisagree.iconify.utils.SystemUtils.restartDevice
import com.drdisagree.iconify.utils.SystemUtils.savedVersionCode
import com.drdisagree.iconify.utils.helper.BackupRestore.restoreFiles
import com.drdisagree.iconify.utils.helper.Logger.writeLog
import com.drdisagree.iconify.utils.overlay.OverlayUtils.overlayExists
import com.drdisagree.iconify.utils.overlay.compiler.OnboardingCompiler.apkSigner
import com.drdisagree.iconify.utils.overlay.compiler.OnboardingCompiler.createManifest
import com.drdisagree.iconify.utils.overlay.compiler.OnboardingCompiler.runAapt
import com.drdisagree.iconify.utils.overlay.compiler.OnboardingCompiler.zipAlign
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

@Suppress("unused")
class OnboardingView : FrameLayout {

    private var installationTask: Job? = null
    private lateinit var progressDialog: InstallationDialog

    constructor(context: Context) : super(context) {
        initialize(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
        initialize(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(context)
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int
    ) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    ) {
        initialize(context)
    }

    private fun initialize(
        context: Context
    ) {
        val inflater = LayoutInflater.from(context)

        binding = ViewOnboardingPageBinding.inflate(inflater, this, true)

        setupSlidingView()
        initButtonClickListeners()
        setLottieColorFilter()

        progressDialog = InstallationDialog(getContext())

        binding.startBtn.getViewTreeObserver().addOnDrawListener {
            if (binding.startBtn.alpha <= 0.1f) {
                binding.startBtn.visibility = GONE
            } else {
                binding.startBtn.visibility = VISIBLE
            }
        }
    }

    private fun setupSlidingView() {
        binding.slider.apply {
            setAdapter(OnboardingAdapter())
            setPageTransformer { v: View, page: Float ->
                Transform.setParallaxTransformation(v, page)
            }
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                    if (numberOfPages > 1) {
                        val newProgress = (position + positionOffset) / (numberOfPages - 1)
                        binding.onboardingRoot.progress = newProgress
                    }
                }
            })
        }

        binding.pageIndicator.attachTo(binding.slider)
    }

    private fun initButtonClickListeners() {
        binding.nextBtn.setOnClickListener { navigateToNextSlide() }
        binding.skipBtn.setOnClickListener { navigateToLastSlide() }
        binding.startBtn.setOnClickListener { startOnClickAction() }
        binding.startBtn.setOnLongClickListener { startOnLongClickAction() }
    }

    private fun startOnClickAction() {
        if (!isButtonClickable) return

        isButtonClickable = false
        skippedInstallation = false
        hasErroredOut = false

        Shell.getShell {
            if (!isDeviceRooted) {
                ErrorDialog(context).show(
                    R.string.root_not_found_title,
                    R.string.root_not_found_desc
                )

                return@getShell
            }

            if (!deviceProperlyRooted()) {
                ErrorDialog(context).show(
                    R.string.compatible_root_not_found_title,
                    R.string.compatible_root_not_found_desc
                )

                return@getShell
            }

            if (!hasStoragePermission()) {
                Toast.makeText(
                    context,
                    R.string.need_storage_perm_title,
                    Toast.LENGTH_SHORT
                ).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    clickedButton = true
                    requestStoragePermission(context)
                }, (if (clickedButton) 10 else 1000).toLong())
            } else {
                val moduleExists = moduleExists()
                val overlayExists = overlayExists()

                if (FORCE_OVERLAY_INSTALLATION ||
                    getInt(VER_CODE) != BuildConfig.VERSION_CODE ||
                    !moduleExists ||
                    !overlayExists
                ) {
                    if (!moduleExists || !overlayExists) {
                        // Clear shared preferences
                        RPrefs.clearAllPrefs()

                        // Clear dynamic resource database
                        CoroutineScope(Dispatchers.IO).launch {
                            DynamicResourceRepository(
                                DynamicResourceDatabase.getInstance().dynamicResourceDao()
                            ).apply {
                                deleteResources(getAllResources())
                            }
                        }
                    }

                    handleInstallation()
                } else {
                    putBoolean(XPOSED_ONLY_MODE, false)

                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    animateSlideLeft(context)
                }
            }
        }

        Handler(Looper.getMainLooper()).postDelayed(
            { isButtonClickable = true },
            TRANSITION_DELAY + 50L
        )
    }

    private fun startOnLongClickAction(): Boolean {
        skippedInstallation = true
        hasErroredOut = false

        Shell.getShell {
            if (!isDeviceRooted) {
                ErrorDialog(context).show(
                    R.string.root_not_found_title,
                    R.string.root_not_found_desc
                )

                return@getShell
            }

            if (!deviceProperlyRooted()) {
                ErrorDialog(context).show(
                    R.string.compatible_root_not_found_title,
                    R.string.compatible_root_not_found_desc
                )

                return@getShell
            }

            if (!hasStoragePermission()) {
                Toast.makeText(
                    context,
                    R.string.need_storage_perm_title,
                    Toast.LENGTH_SHORT
                ).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    clickedButton = true
                    requestStoragePermission(context)
                }, (if (clickedButton) 10 else 1000).toLong())
            } else {
                if (!moduleExists()) {
                    // Clear shared preferences
                    RPrefs.clearAllPrefs()

                    // Clear dynamic resource database
                    CoroutineScope(Dispatchers.IO).launch {
                        DynamicResourceRepository(
                            DynamicResourceDatabase.getInstance().dynamicResourceDao()
                        ).apply {
                            deleteResources(getAllResources())
                        }
                    }

                    handleInstallation()
                } else {
                    putBoolean(XPOSED_ONLY_MODE, !overlayExists())

                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    animateSlideLeft(context)

                    Toast.makeText(
                        context,
                        R.string.toast_skipped_installation,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        return true
    }

    private fun handleInstallation() {
        LottieCompositionFactory.fromRawRes(context, R.raw.loading_anim)
            .addListener {
                binding.loadingAnim.apply {
                    maxWidth = binding.startBtn.height
                    maxHeight = binding.startBtn.height
                    setAnimation(R.raw.loading_anim)
                    renderMode = RenderMode.HARDWARE
                    setVisibility(VISIBLE)
                }

                binding.startBtn.setTextColor(Color.TRANSPARENT)

                startInstallationTask()
            }
    }

    private fun startInstallationTask() {
        if (installationTask?.isActive != true) {
            installationTask = CoroutineScope(Dispatchers.IO).launch {
                onPreExecute()
                doInBackground()
                onPostExecute()
            }
        }
    }

    private suspend fun updateProgressBar(step: Int? = null, message: String? = null) {
        withContext(Dispatchers.Main) {
            if (step != null) {
                val title = resources.getString(R.string.step_number, step, 6)
                val desc = when (step) {
                    1 -> resources.getString(R.string.module_installation_step1)
                    2 -> resources.getString(R.string.module_installation_step2)
                    3 -> resources.getString(R.string.module_installation_step3)
                    4 -> resources.getString(R.string.module_installation_step4)
                    5 -> resources.getString(R.string.module_installation_step5)
                    6 -> resources.getString(R.string.module_installation_step6)
                    else -> resources.getString(R.string.loading_dialog_wait)
                }

                progressDialog.setMessage(title, desc)
            }

            message?.let { progressDialog.setLogs(it) }
        }
    }

    private suspend fun onPreExecute() {
        withContext(Dispatchers.Main) {
            binding.startBtn.setText(R.string.btn_lets_go)

            progressDialog.show(
                resources.getString(R.string.installing),
                resources.getString(R.string.init_module_installation)
            )
        }
    }

    private suspend fun doInBackground() {
        var step = 0
        var dir: File

        updateProgressBar(step = ++step, message = "I: Creating blank module template")
        handleModule()

        updateProgressBar(step = ++step)
        try {
            // Clean data directory
            updateProgressBar(message = "I: Cleaning iconify data directory")
            Shell.cmd("rm -rf $DATA_DIR/Overlays").exec()

            updateProgressBar(message = "I: Extracting overlays from assets")
            if (skippedInstallation) {
                delay(100)
                updateProgressBar(message = "W: Skipped...")
                delay(100)
            } else {
                // Extract overlays from assets
                copyAssets("Overlays")
            }

            // Create temp directory
            updateProgressBar(message = "I: Creating temporary directories")
            Shell.cmd("mkdir -p $TEMP_OVERLAY_DIR").exec()
            Shell.cmd("mkdir -p $UNSIGNED_UNALIGNED_DIR").exec()
            Shell.cmd("mkdir -p $UNSIGNED_DIR").exec()
            Shell.cmd("mkdir -p $SIGNED_DIR").exec()
        } catch (e: IOException) {
            hasErroredOut = true
            Log.e(TAG, e.toString())
        }

        updateProgressBar(step = ++step)

        if (skippedInstallation) {
            updateProgressBar(message = "W: Skipping overlay builder...")
            delay(100)
        } else {
            // Create AndroidManifest.xml and build Overlay using AAPT
            dir = File("$DATA_DIR/Overlays")

            if (dir.listFiles() == null) hasErroredOut = true

            if (!hasErroredOut) {
                for (pkg in dir.listFiles()!!) {
                    if (pkg.isDirectory()) {
                        for (overlay in pkg.listFiles()!!) {
                            if (overlay.isDirectory()) {
                                val overlayName = overlay.toString().replace("$pkg/", "")

                                updateProgressBar(message = "I: Building Overlay for $overlayName")

                                if (createManifest(
                                        overlayName,
                                        pkg.toString()
                                            .replace("$DATA_DIR/Overlays/", ""),
                                        overlay.absolutePath
                                    )
                                ) {
                                    hasErroredOut = true
                                }

                                if (!hasErroredOut && runAapt(overlay.absolutePath, overlayName)) {
                                    hasErroredOut = true
                                }
                            }

                            if (hasErroredOut) break
                        }
                    }

                    if (hasErroredOut) break
                }
            }
        }

        updateProgressBar(step = ++step)

        if (skippedInstallation) {
            updateProgressBar(message = "W: Skipping zipalign process...")
            delay(100)
        } else {
            // ZipAlign the Overlay
            dir = File(Resources.UNSIGNED_UNALIGNED_DIR)

            if (dir.listFiles() == null) hasErroredOut = true

            if (!hasErroredOut) {
                for (overlay in dir.listFiles()!!) {
                    if (!overlay.isDirectory()) {
                        val overlayName = overlay.toString()
                            .replace(Resources.UNSIGNED_UNALIGNED_DIR + '/', "")
                            .replace("-unaligned", "")

                        updateProgressBar(
                            message = "I: Zip aligning Overlay " + overlayName.replace(
                                "-unsigned.apk",
                                ""
                            )
                        )

                        if (zipAlign(overlay.absolutePath, overlayName)) {
                            hasErroredOut = true
                        }
                    }

                    if (hasErroredOut) break
                }
            }
        }

        updateProgressBar(step = ++step)

        if (skippedInstallation) {
            updateProgressBar(message = "W: Skipping signing process...")
            delay(100)
        } else {
            // Sign the Overlay
            dir = File(Resources.UNSIGNED_DIR)

            if (dir.listFiles() == null) hasErroredOut = true

            if (!hasErroredOut) {
                for (overlay in dir.listFiles()!!) {
                    if (!overlay.isDirectory()) {
                        val overlayName =
                            overlay.toString()
                                .replace(Resources.UNSIGNED_DIR + '/', "")
                                .replace("-unsigned", "")

                        updateProgressBar(
                            message = "I: Signing Overlay " + overlayName.replace(
                                ".apk",
                                ""
                            )
                        )

                        var attempt = 3
                        while (attempt-- != 0) {
                            hasErroredOut = apkSigner(overlay.absolutePath, overlayName)

                            if (!hasErroredOut) {
                                break
                            } else {
                                delay(1000)
                            }
                        }
                    }

                    if (hasErroredOut) break
                }
            }
        }

        updateProgressBar(step = ++step, message = "I: Moving overlays to system directory")

        if (skippedInstallation) {
            delay(100)
            updateProgressBar(message = "W: Skipping...")
        }

        // Move all generated overlays to system dir and flash as module
        if (!hasErroredOut) {
            Shell.cmd("cp -a $SIGNED_DIR/. $TEMP_MODULE_OVERLAY_DIR").exec()

            restoreFiles()

            try {
                hasErroredOut = flashModule(
                    createModule(
                        TEMP_MODULE_DIR,
                        "$TEMP_DIR/Iconify.zip"
                    )
                )
            } catch (e: Exception) {
                hasErroredOut = true
                writeLog(TAG, "Failed to create/flash module zip", e)
                Log.e(TAG, "Failed to create/flash module zip\n$e")

                if (e.toString().contains("Function not implemented", ignoreCase = true)) {
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(
                            context,
                            "Downgrade KernelSU app and try again",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        updateProgressBar(message = "I: Cleaning temporary directories")

        // Clean temp directory
        Shell.cmd("rm -rf $TEMP_DIR").exec()
        Shell.cmd("rm -rf $DATA_DIR/Overlays").exec()
        if (!hasErroredOut) {
            updateProgressBar(message = "I: Installation process finished")
            delay(100)
            updateProgressBar(message = "I: You should reboot your device")
        } else {
            updateProgressBar(message = "E: Installation process failed")
        }

        delay(500)

        updateProgressBar(message = "Closing in...")
        delay(1000)

        updateProgressBar(message = "3....")
        delay(1000)

        updateProgressBar(message = "2...")
        delay(1000)

        updateProgressBar(message = "1..")
        delay(1000)
    }

    private suspend fun onPostExecute() {
        withContext(Dispatchers.Main) {
            progressDialog.hide()

            if (!hasErroredOut) {
                if (!skippedInstallation) {
                    if (BuildConfig.VERSION_CODE != savedVersionCode) {
                        if (getBoolean(FIRST_INSTALL, true)) {
                            putBoolean(FIRST_INSTALL, true)
                            putBoolean(UPDATE_DETECTED, false)
                        } else {
                            putBoolean(FIRST_INSTALL, false)
                            putBoolean(UPDATE_DETECTED, true)
                        }

                        putInt(VER_CODE, BuildConfig.VERSION_CODE)
                    }

                    putBoolean(XPOSED_ONLY_MODE, false)

                    if (overlayExists()) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(context, MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                            animateSlideLeft(context)
                        }, 10)
                    } else {
                        Toast.makeText(
                            context,
                            R.string.need_reboot_title,
                            Toast.LENGTH_SHORT
                        ).show()

                        binding.startBtn.apply {
                            setText(R.string.btn_reboot)
                            setTextColor(buttonTextColor)
                            setOnClickListener { restartDevice() }
                            setOnLongClickListener(null)
                        }
                    }
                } else {
                    putBoolean(XPOSED_ONLY_MODE, true)
                    val intent = Intent(context, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                    animateSlideLeft(context)

                    Toast.makeText(
                        context,
                        R.string.one_time_reboot_needed,
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                onCancelled()

                ErrorDialog(context).show(
                    R.string.installation_failed_title,
                    R.string.installation_failed_desc
                )

                binding.startBtn.setText(R.string.btn_lets_go)
            }

            binding.loadingAnim.setVisibility(GONE)
            binding.startBtn.setTextColor(buttonTextColor)
        }
    }

    private fun onCancelled() {
        clearPref(XPOSED_ONLY_MODE)

        Shell.cmd("rm -rf $DATA_DIR").exec()
        Shell.cmd("rm -rf $TEMP_DIR").exec()
        Shell.cmd("rm -rf $BACKUP_DIR").exec()
        Shell.cmd("rm -rf $MODULE_DIR").exec()
    }

    @get:ColorInt
    private val buttonTextColor: Int
        get() {
            val typedValue = TypedValue()
            context.theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnPrimary,
                typedValue,
                true
            )
            return typedValue.data
        }

    private fun setLottieColorFilter() {
        val callback = LottieValueCallback<ColorFilter>(SimpleColorFilter(buttonTextColor))
        binding.loadingAnim.addValueCallback(
            KeyPath("**"),
            LottieProperty.COLOR_FILTER,
            callback
        )
    }

    override fun onDetachedFromWindow() {
        installationTask?.cancel()

        super.onDetachedFromWindow()
    }

    companion object {
        private val TAG = OnboardingView::class.java.getSimpleName()
        private lateinit var binding: ViewOnboardingPageBinding
        private var isButtonClickable = true
        private var clickedButton = false
        private var hasErroredOut = false
        private var numberOfPages = OnboardingPage.entries.size

        fun navigateToPrevSlide() {
            val prevSlidePos = binding.slider.currentItem - 1

            if (prevSlidePos < 0) {
                throw IndexOutOfBoundsException("Can't navigate to previous slide")
            }

            binding.slider.setCurrentItem(prevSlidePos, true)
        }

        fun navigateToNextSlide() {
            val nextSlidePos = binding.slider.currentItem + 1

            if (nextSlidePos >= numberOfPages) {
                throw IndexOutOfBoundsException("Can't navigate to next slide")
            }

            binding.slider.setCurrentItem(nextSlidePos, true)
        }

        fun navigateToLastSlide() {
            binding.slider.setCurrentItem(numberOfPages - 1, true)
        }
    }
}