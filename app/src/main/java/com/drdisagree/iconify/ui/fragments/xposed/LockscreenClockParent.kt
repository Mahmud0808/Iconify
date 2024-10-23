package com.drdisagree.iconify.ui.fragments.xposed

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import com.drdisagree.iconify.BuildConfig
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.LSCLOCK_STYLE
import com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.common.Resources.LOCKSCREEN_CLOCK_LAYOUT
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.config.RPrefs.getInt
import com.drdisagree.iconify.config.RPrefs.putBoolean
import com.drdisagree.iconify.config.RPrefs.putInt
import com.drdisagree.iconify.databinding.FragmentXposedLockscreenClockBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.models.ClockCarouselItemViewModel
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.ui.views.ClockCarouselView
import com.drdisagree.iconify.utils.SystemUtils
import com.drdisagree.iconify.utils.WallpaperUtils.loadWallpaper
import com.topjohnwu.superuser.internal.UiThreadHandler.handler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockscreenClockParent : BaseFragment() {

    private lateinit var binding: FragmentXposedLockscreenClockBinding
    private var updateRunnable: Runnable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SystemUtils.hasStoragePermission()) {
            SystemUtils.requestStoragePermission(requireContext())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentXposedLockscreenClockBinding.inflate(inflater, container, false)

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_lockscreen_clock
        )

        val lockscreenClockFragment = LockscreenClock()
        childFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, lockscreenClockFragment)
            .commit()

        binding.lsClockSwitch.isSwitchChecked = getBoolean(LSCLOCK_SWITCH, false)
        binding.lsClockSwitch.setSwitchChangeListener { _: CompoundButton?, isChecked: Boolean ->
            putBoolean(LSCLOCK_SWITCH, isChecked)
            lockscreenClockFragment.updateScreen(LSCLOCK_SWITCH)
        }

        loadAndSetWallpaper()

        return binding.root
    }

    @SuppressLint("DiscouragedApi")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clockCarouselView.clockCarouselViewStub.layoutResource =
            R.layout.clock_carousel_view
        val clockCarouselView =
            binding.clockCarouselView.clockCarouselViewStub.inflate() as ClockCarouselView

        val lsClock: MutableList<ClockCarouselItemViewModel> = ArrayList()
        var maxIndex = 0
        while (resources.getIdentifier(
                "preview_lockscreen_clock_$maxIndex",
                "layout",
                BuildConfig.APPLICATION_ID
            ) != 0
        ) {
            maxIndex++
        }

        for (i in 0 until maxIndex) {
            lsClock.add(
                ClockCarouselItemViewModel(
                    if (i == 0) {
                        requireContext().getString(R.string.clock_none)
                    } else {
                        requireContext().getString(R.string.clock_style_name, i)
                    },
                    i,
                    getInt(LSCLOCK_STYLE, 0) == i,
                    LOCKSCREEN_CLOCK_LAYOUT + i,
                    LayoutInflater.from(requireContext()).inflate(
                        resources.getIdentifier(
                            LOCKSCREEN_CLOCK_LAYOUT + i,
                            "layout",
                            BuildConfig.APPLICATION_ID
                        ),
                        null
                    ).apply {
                        layoutParams = FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT,
                            FrameLayout.LayoutParams.WRAP_CONTENT
                        ).apply {
                            gravity = Gravity.CENTER_HORIZONTAL
                        }
                    }
                )
            )
        }

        Handler(Looper.getMainLooper()).postDelayed({
            clockCarouselView.setUpClockCarouselView(lsClock) { onClockSelected ->
                updateRunnable?.let {
                    handler.removeCallbacks(it)
                }
                updateRunnable = Runnable {
                    putInt(LSCLOCK_STYLE, onClockSelected.clockLayout)
                }
                updateRunnable?.let {
                    handler.postDelayed(it, 500)
                }
            }

            binding.clockCarouselView.screenPreviewClickView.setOnSideClickedListener { isStart ->
                if (isStart) clockCarouselView.scrollToPrevious()
                else clockCarouselView.scrollToNext()
            }
        }, 50)
    }

    private fun loadAndSetWallpaper() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            val bitmap = loadWallpaper(requireContext(), isLockscreen = true).await()

            if (bitmap != null) {
                binding.clockCarouselView.preview.wallpaperDimmingScrim.visibility = View.VISIBLE
                binding.clockCarouselView.preview.wallpaperFadeinScrim.visibility = View.VISIBLE
                binding.clockCarouselView.preview.wallpaperPreviewSpinner.visibility = View.GONE
                binding.clockCarouselView.preview.wallpaperFadeinScrim.setImageBitmap(bitmap)
            }
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        loadAndSetWallpaper()
    }

    override fun onPause() {
        super.onPause()
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}