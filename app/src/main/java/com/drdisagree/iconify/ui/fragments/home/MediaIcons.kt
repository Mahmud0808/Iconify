package com.drdisagree.iconify.ui.fragments.home

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.config.RPrefs.getBoolean
import com.drdisagree.iconify.databinding.FragmentMediaIconsBinding
import com.drdisagree.iconify.ui.base.BaseFragment
import com.drdisagree.iconify.ui.utils.ViewHelper.setHeader
import com.drdisagree.iconify.utils.AppUtils.getAppIcon
import com.drdisagree.iconify.utils.AppUtils.getAppName
import com.drdisagree.iconify.utils.AppUtils.isAppInstalledRoot
import com.drdisagree.iconify.utils.AppUtils.launchApp
import com.drdisagree.iconify.utils.overlay.OverlayUtils
import com.drdisagree.iconify.utils.overlay.manager.MediaPlayerIconManager.enableOverlay
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup

class MediaIcons : BaseFragment() {

    private val mediaPlayerIconPackKeys = ArrayList<Array<String>>()
    private val mediaPlayerIconPackList = ArrayList<Array<Any>>()

    private lateinit var binding: FragmentMediaIconsBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMediaIconsBinding.inflate(inflater, container, false)
        val view: View = binding.getRoot()

        // Header
        setHeader(
            requireContext(),
            getParentFragmentManager(),
            binding.header.toolbar,
            R.string.activity_title_media_icons
        )

        // Media Player Icon list items
        initMediaPlayerIconPackList()

        // Generate keys for preference
        for (i in mediaPlayerIconPackList.indices) {
            mediaPlayerIconPackKeys.add(
                arrayOf(
                    "IconifyComponentMPIP" + i + 1 + ".overlay",
                    "IconifyComponentMPIP" + i + 2 + ".overlay",
                    "IconifyComponentMPIP" + i + 3 + ".overlay"
                )
            )
        }

        Thread {
            // Check if packages are installed
            for (i in mediaPlayerIconPackList.indices) {
                if (i == 0) {
                    // default music player of a13
                    mediaPlayerIconPackList[i][1] = Build.VERSION.SDK_INT >= 33
                } else {
                    mediaPlayerIconPackList[i][1] = isAppInstalledRoot(
                        (mediaPlayerIconPackList[i][0] as String)
                    )
                }
            }

            Handler(Looper.getMainLooper()).post {
                var isMusicPlayerShown = false

                try {
                    val noSupportedPlayer =
                        requireActivity().findViewById<TextView>(R.id.no_supported_musicplayer)

                    for (i in mediaPlayerIconPackList.indices) {
                        if (mediaPlayerIconPackList[i][1] as Boolean) {
                            if (i == 0) {
                                addItem(
                                    resources.getString(R.string.a13_default_media_player),
                                    mediaPlayerIconPackList[i][0] as String,
                                    ContextCompat.getDrawable(
                                        appContext, R.drawable.ic_android
                                    ),
                                    mediaPlayerIconPackList[i][2] as Int
                                )
                            } else {
                                addItem(
                                    getAppName(mediaPlayerIconPackList[i][0] as String),
                                    mediaPlayerIconPackList[i][0] as String,
                                    getAppIcon(
                                        mediaPlayerIconPackList[i][0] as String
                                    ),
                                    mediaPlayerIconPackList[i][2] as Int
                                )
                            }

                            enableOnClickListener(i)

                            isMusicPlayerShown = true
                        }
                    }

                    refreshBackground()

                    if (!isMusicPlayerShown) {
                        noSupportedPlayer.visibility = View.VISIBLE
                    }
                } catch (ignored: Exception) {
                }
            }
        }.start()

        return view
    }

    private fun initMediaPlayerIconPackList() {
        mediaPlayerIconPackList.apply {
            add(
                arrayOf(
                    "defaultA13",
                    false,
                    R.id.defaulta13mp
                )
            )
            add(
                arrayOf(
                    "com.maxmpz.audioplayer",
                    false,
                    R.id.poweramp
                )
            )
            add(
                arrayOf(
                    "code.name.monkey.retromusic",
                    false,
                    R.id.retro
                )
            )
            add(
                arrayOf(
                    "com.awedea.nyx",
                    false,
                    R.id.nyx
                )
            )
            add(
                arrayOf(
                    "com.kapp.youtube.final",
                    false,
                    R.id.ymusic
                )
            )
            add(
                arrayOf(
                    "com.shadow.blackhole",
                    false,
                    R.id.blackhole
                )
            )
            add(
                arrayOf(
                    "in.krosbits.musicolet",
                    false,
                    R.id.musicolet
                )
            )
            add(
                arrayOf(
                    "com.google.android.youtube",
                    false,
                    R.id.youtube
                )
            )
            add(
                arrayOf(
                    "com.google.android.apps.youtube.music",
                    false,
                    R.id.yt_music
                )
            )
            add(
                arrayOf(
                    "app.revanced.android.youtube",
                    false,
                    R.id.youtube_revanced
                )
            )
            add(
                arrayOf(
                    "app.revanced.android.apps.youtube.music",
                    false,
                    R.id.yt_music_revanced
                )
            )
        }
    }

    // Function to check for button bg drawable changes
    private fun refreshBackground() {
        for (i in mediaPlayerIconPackList.indices) {
            if (mediaPlayerIconPackList[i][1] as Boolean) {
                val toggleButtonGroup = binding.getRoot().findViewById<View>(
                    (mediaPlayerIconPackList[i][2] as Int)
                ).findViewById<MaterialButtonToggleGroup>(R.id.toggleButtonGroup)

                val buttons = intArrayOf(R.id.aurora, R.id.gradicon, R.id.plumpy)

                for (j in 0..2) {
                    if (getBoolean(mediaPlayerIconPackKeys[i][j])) {
                        toggleButtonGroup.check(buttons[j])
                    } else {
                        toggleButtonGroup.uncheck(buttons[j])
                    }
                }
            }
        }
    }

    // Enable onClick event
    private fun enableOnClickListener(idx: Int) {
        val child =
            binding.getRoot().findViewById<LinearLayout>(mediaPlayerIconPackList[idx][2] as Int)
        val buttons = intArrayOf(R.id.aurora, R.id.gradicon, R.id.plumpy)

        for (i in 0..2) {
            val finalI = i + 1

            (child.findViewById<View>(buttons[i]) as MaterialButton).addOnCheckedChangeListener { _: MaterialButton?, isChecked: Boolean ->
                if (isChecked) {
                    enableOverlay(idx, finalI)
                } else {
                    OverlayUtils.disableOverlay("IconifyComponentMPIP$idx$finalI.overlay")
                }

                refreshBackground()
            }
        }
    }

    private fun addItem(appName: String, packageName: String?, appIcon: Drawable?, viewId: Int) {
        val list = LayoutInflater.from(requireContext()).inflate(
            R.layout.view_list_option_mediaplayer_icons,
            binding.mediaplayerIconList,
            false
        )
        list.setId(viewId)

        val launch = list.findViewById<LinearLayout>(R.id.launch_app)

        if (packageName != null) {
            if (packageName == "defaultA13") launch.setOnClickListener { } else launch.setOnClickListener {
                launchApp(
                    requireActivity(),
                    packageName
                )
            }
        }

        list.findViewById<View>(R.id.app_icon).background = appIcon

        val name = list.findViewById<TextView>(R.id.app_name)
        name.text = appName

        binding.mediaplayerIconList.addView(list)
    }
}