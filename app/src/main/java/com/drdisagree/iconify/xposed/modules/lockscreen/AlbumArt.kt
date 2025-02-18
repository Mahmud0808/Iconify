package com.drdisagree.iconify.xposed.modules.lockscreen

import android.content.Context
import android.graphics.drawable.Icon
import android.media.MediaMetadata
import android.media.session.PlaybackState
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import com.drdisagree.iconify.common.Const.SYSTEMUI_PACKAGE
import com.drdisagree.iconify.common.Preferences.ALBUM_ART_LOCKSCREEN_BLUR
import com.drdisagree.iconify.common.Preferences.ALBUM_ART_LOCKSCREEN_FILTER
import com.drdisagree.iconify.common.Preferences.ALBUM_ART_ON_LOCKSCREEN
import com.drdisagree.iconify.common.Preferences.DEPTH_WALLPAPER_SWITCH
import com.drdisagree.iconify.xposed.ModPack
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.applyBlur
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import com.drdisagree.iconify.xposed.utils.XPrefs.XprefsIsInitialized
import de.robv.android.xposed.XposedHelpers.getObjectField
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class AlbumArt(context: Context) : ModPack(context) {

    // NotificationUtils
    private var mDepthEnabled: Boolean = false
    private var mAlbumArtEnabled: Boolean = true
    private var mAlbumArtFilter: Int = 0
    private var mAlbumArtBlurLevel: Float = 7.5f

    private lateinit var mAlbumArtContainer: FrameLayout
    private lateinit var mAlbumArtView: ImageView

    private var mMediaMetadata: MediaMetadata? = null
    private var mPlaybackState: Int = PlaybackState.STATE_NONE

    private var mLayersCreated = false
    private var mScrimController: Any? = null

    override fun updatePrefs(vararg key: String) {
        if (!XprefsIsInitialized) return

        Xprefs.apply {
            mDepthEnabled = getBoolean(DEPTH_WALLPAPER_SWITCH, false)
            mAlbumArtEnabled = getBoolean(ALBUM_ART_ON_LOCKSCREEN, true)
            mAlbumArtFilter = getString(ALBUM_ART_LOCKSCREEN_FILTER, "0")!!.toInt()
            mAlbumArtBlurLevel = (Xprefs.getSliderInt(ALBUM_ART_LOCKSCREEN_BLUR, 30) / 100f) * 25f
        }

        //        when (key.firstOrNull()) {
        //            in setOf(
        //                ALBUM_ART_ON_LOCKSCREEN,
        //                ALBUM_ART_LOCKSCREEN_FILTER,
        //                ALBUM_ART_LOCKSCREEN_BLUR
        //            ) -> updateAlbumArtVisibility()
        //        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) return

        val qsImplClass = findClass(
            "$SYSTEMUI_PACKAGE.qs.QSImpl",
            "$SYSTEMUI_PACKAGE.qs.QSFragment"
        )
        val centralSurfacesImplClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.CentralSurfacesImpl"
        )
        val scrimControllerClass = findClass(
            "$SYSTEMUI_PACKAGE.statusbar.phone.ScrimController"
        )
        val mediaDataManager = findClass(
            "$SYSTEMUI_PACKAGE.media.controls.domain.pipeline.MediaDataManager"
        )

        // Get media metadata change
        scrimControllerClass
            .hookConstructor()
            .runAfter { param ->
                mScrimController = param.thisObject
            }

        centralSurfacesImplClass
            .hookMethod("start")
            .runAfter {
                if (!mAlbumArtEnabled || mScrimController == null) return@runAfter

                val scrimBehind = getObjectField(mScrimController, "mScrimBehind") as View
                val rootView = scrimBehind.parent as ViewGroup

                if (!mLayersCreated) {
                    createLayers()
                }

                rootView.addView(mAlbumArtContainer, if (mDepthEnabled) 1 else 0)
            }

        centralSurfacesImplClass
            .hookMethod("onStartedWakingUp")
            .runAfter { _ ->
                updateAlbumArtVisibility()
            }

        scrimControllerClass
            .hookMethod("applyAndDispatchState")
            .runAfter { _ ->
                updateAlbumArtVisibility()
            }

        qsImplClass
            .hookMethod("setQsExpansion")
            .runAfter { _ ->
                updateAlbumArtVisibility()
            }

        mediaDataManager
            .hookMethod("onMediaDataLoaded")
            .runAfter { param ->
                val mediaData = param.args[2]
                val artWork = mediaData.callMethodSilently("getArtwork") as? Icon
                    ?: mediaData.getField("artwork") as Icon
                val drawable = artWork.loadDrawable(mContext)
                mAlbumArtView.setImageDrawable(drawable!!.applyBlur(mContext, mAlbumArtBlurLevel))
            }
    }

    private fun updateAlbumArtVisibility() {
        if (mScrimController == null || !mAlbumArtEnabled) {
            if (mLayersCreated) {
                mAlbumArtContainer.post { mAlbumArtContainer.visibility = View.GONE }
            }
            return
        }

        val state = getObjectField(mScrimController, "mState").toString()
        shouldShowAlbumArt = (mAlbumArtEnabled &&
                /*(mPlaybackState == PlaybackState.STATE_PLAYING || mPlaybackState == PlaybackState.STATE_BUFFERING) &&*/
                (state == "KEYGUARD"))

        if (shouldShowAlbumArt) {
            mAlbumArtContainer.post { mAlbumArtContainer.visibility = View.VISIBLE }
        } else {
            mAlbumArtContainer.post { mAlbumArtContainer.visibility = View.GONE }
        }
    }


    private fun createLayers() {
        if (mLayersCreated) return

        val lp = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        mAlbumArtContainer = FrameLayout(mContext).apply {
            layoutParams = lp
        }
        mAlbumArtView = ImageView(mContext).apply {
            layoutParams = lp
            scaleType = ImageView.ScaleType.CENTER_CROP
            visibility = View.GONE
        }

        mAlbumArtContainer.addView(mAlbumArtView)

        mLayersCreated = true
    }

    companion object {
        private val TAG = "Iconify - ${AlbumArt::class.java.simpleName}: "
        var shouldShowAlbumArt: Boolean = false
    }

}