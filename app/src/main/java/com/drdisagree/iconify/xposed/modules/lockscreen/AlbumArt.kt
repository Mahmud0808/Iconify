package com.drdisagree.iconify.xposed.modules.lockscreen

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.media.session.PlaybackState
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
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getColored
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.getGrayscaleBlurredImage
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.reAddView
import com.drdisagree.iconify.xposed.modules.extras.utils.ViewHelper.toGrayscale
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.XposedHook.Companion.findClass
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethod
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.callMethodSilently
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.getField
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookConstructor
import com.drdisagree.iconify.xposed.modules.extras.utils.toolkit.hookMethod
import com.drdisagree.iconify.xposed.utils.XPrefs.Xprefs
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class AlbumArt(context: Context) : ModPack(context) {

    private var shouldShowAlbumArt: Boolean = false
    private var mDepthEnabled: Boolean = false
    private var mAlbumArtEnabled: Boolean = true
    private var mAlbumArtFilter: Int = 0
    private var mAlbumArtBlurLevel: Float = 7.5f

    private lateinit var mAlbumArtContainer: FrameLayout
    private lateinit var mAlbumArtView: ImageView

    private var mArtworkDrawable: Drawable? = null
    private var mPlaybackState: Int = PlaybackState.STATE_NONE

    private var mLayersCreated = false
    private var mScrimControllerObj: Any? = null

    override fun updatePrefs(vararg key: String) {
        Xprefs.apply {
            mDepthEnabled = getBoolean(DEPTH_WALLPAPER_SWITCH, false)
            mAlbumArtEnabled = getBoolean(ALBUM_ART_ON_LOCKSCREEN, true)
            mAlbumArtFilter = getString(ALBUM_ART_LOCKSCREEN_FILTER, "0")!!.toInt()
            mAlbumArtBlurLevel = (Xprefs.getSliderInt(ALBUM_ART_LOCKSCREEN_BLUR, 30) / 100f) * 25f
        }

        when (key.firstOrNull()) {
            in setOf(
                ALBUM_ART_ON_LOCKSCREEN,
                ALBUM_ART_LOCKSCREEN_FILTER,
                ALBUM_ART_LOCKSCREEN_BLUR
            ) -> updateAlbumArtState()
        }
    }

    override fun handleLoadPackage(loadPackageParam: LoadPackageParam) {
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
        val keyguardSliceProviderClass = findClass(
            "$SYSTEMUI_PACKAGE.keyguard.KeyguardSliceProvider"
        )

        // Get media metadata change
        scrimControllerClass
            .hookConstructor()
            .runAfter { param -> mScrimControllerObj = param.thisObject }

        centralSurfacesImplClass
            .hookMethod("start")
            .runAfter {
                if (!mAlbumArtEnabled || mScrimControllerObj == null) return@runAfter

                val scrimBehind = mScrimControllerObj.getField("mScrimBehind") as View
                val rootView = scrimBehind.parent as ViewGroup

                if (!mLayersCreated) {
                    createLayers()
                }

                rootView.reAddView(mAlbumArtContainer, if (mDepthEnabled) 1 else 0)
            }

        scrimControllerClass
            .hookMethod("applyAndDispatchState")
            .runAfter { _ ->
                updateAlbumArtState()
            }

        qsImplClass
            .hookMethod("setQsExpansion")
            .runAfter { param ->
                if (param.thisObject.callMethod("isKeyguardState") as Boolean) {
                    updateAlbumArtState()
                }
            }

        mediaDataManager
            .hookMethod("onMediaDataLoaded")
            .runAfter { param ->
                val mediaData = param.args[2]
                val artWork = mediaData.callMethodSilently("getArtwork") as? Icon
                    ?: mediaData.getField("artwork") as Icon
                val drawable = artWork.loadDrawable(mContext)

                if (drawable != mArtworkDrawable) {
                    mArtworkDrawable = drawable
                    mAlbumArtView.setImageDrawable(
                        getFilteredArtWork(mArtworkDrawable!!)
                    )
                }
            }

        keyguardSliceProviderClass
            .hookMethod("onPrimaryMetadataOrStateChanged")
            .runAfter { param ->
                mPlaybackState = param.args[1] as Int

                updateAlbumArtState()
            }
    }

    private fun updateAlbumArtState() {
        if (mScrimControllerObj == null || !mAlbumArtEnabled) {
            if (mLayersCreated) {
                mAlbumArtContainer.post { mAlbumArtContainer.visibility = View.GONE }
            }
            return
        }

        shouldShowAlbumArt =
            (mPlaybackState == PlaybackState.STATE_PLAYING || mPlaybackState == PlaybackState.STATE_BUFFERING) &&
            mScrimControllerObj.getField("mState").toString() == "KEYGUARD"

        mAlbumArtContainer.post {
            val newVisibility = if (shouldShowAlbumArt) View.VISIBLE else View.GONE

            if (mAlbumArtContainer.visibility != newVisibility) {
                mAlbumArtContainer.visibility = newVisibility
            }
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
            visibility = View.GONE
        }
        mAlbumArtView = ImageView(mContext).apply {
            layoutParams = lp
            scaleType = ImageView.ScaleType.CENTER_CROP
        }

        mAlbumArtContainer.reAddView(mAlbumArtView)

        mLayersCreated = true
    }

    @SuppressLint("DiscouragedApi")
    private fun getFilteredArtWork(artwork: Drawable): Drawable {
        val mSystemAccent = mContext.resources.getColor(
            mContext.resources.getIdentifier(
                "android:color/system_accent1_300",
                "color",
                mContext.packageName
            ), mContext.theme
        )

        return when (mAlbumArtFilter) {
            1 -> artwork.toGrayscale(mContext)
            2 -> artwork.getColored(mContext, mSystemAccent)
            3 -> artwork.applyBlur(mContext, mAlbumArtBlurLevel)
            4 -> artwork.getGrayscaleBlurredImage(mContext, mAlbumArtBlurLevel)
            else -> artwork
        }
    }
}