package com.drdisagree.iconify.xposed.modules.views

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class MediaPlayerPagerAdapter(
    private val context: Context,
    private val mediaPlayerViews: MutableList<Pair<String?, QsOpMediaPlayerView>>
) : PagerAdapter() {

    override fun getCount(): Int {
        return mediaPlayerViews.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = mediaPlayerViews[position].second
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as QsOpMediaPlayerView)
    }

    fun addMediaPlayerView(packageName: String, view: QsOpMediaPlayerView) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) return

        if (mediaPlayerViews.size == 1 && mediaPlayerViews[0].first == null) {
            mediaPlayerViews.removeAt(0)
            notifyDataSetChanged()
        }

        mediaPlayerViews.add(0, packageName to view)
        notifyDataSetChanged()
    }

    fun isMediaPlayerAvailable(packageName: String): Boolean {
        return mediaPlayerViews.any { it.first == packageName }
    }

    fun getMediaPlayer(packageName: String): QsOpMediaPlayerView? {
        return mediaPlayerViews.firstOrNull { it.first == packageName }?.second
    }
}