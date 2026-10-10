package com.drdisagree.iconify.xposed.modules.lockscreen

import android.content.Context
import android.media.session.PlaybackState
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.LockscreenSceneInjector
import com.drdisagree.iconify.xposed.modules.extras.utils.misc.ViewHelper.reAddView
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class AlbumArtCompose(context: Context) : AlbumArt(context) {

    private var lockscreenHost: FrameLayout? = null

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        createLayers()
        hookMediaUpdates()

        LockscreenSceneInjector.addBehindContent(
            order = 0,
            key = GROUP_KEY,
            isEnabled = { mAlbumArtEnabled }
        ) { context -> createHost(context) }
    }

    private fun createHost(context: Context): View = FrameLayout(context).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                val host = v as FrameLayout
                lockscreenHost = host
                host.reAddView(mAlbumArtContainer)
                updateAlbumArtState()
                broadcastAlbumArtUpdate()
            }

            override fun onViewDetachedFromWindow(v: View) {
                if (lockscreenHost !== v) return
                lockscreenHost = null
                updateAlbumArtState()
                broadcastAlbumArtUpdate()
            }
        })
    }

    override fun updateAlbumArtState() {
        val isPlaying = mPlaybackState == PlaybackState.STATE_PLAYING ||
                mPlaybackState == PlaybackState.STATE_BUFFERING
        showAlbumArt = mAlbumArtEnabled && isPlaying && lockscreenHost != null &&
                (!mIsDozing || mShowOnAod)

        applyAlbumArtVisibility(mIsDozing)
    }

    companion object {
        private const val GROUP_KEY = 0x1C0B1C21
    }
}
