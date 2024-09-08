package com.drdisagree.iconify.xposed.modules.views

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class MediaPlayerPagerAdapter(
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
}