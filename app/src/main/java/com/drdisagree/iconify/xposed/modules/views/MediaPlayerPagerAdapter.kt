package com.drdisagree.iconify.xposed.modules.views

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class MediaPlayerPagerAdapter(
    private val context: Context,
    private val mediaPlayerViews: MutableList<Pair<String?, QsOpMediaPlayerView>>
) : RecyclerView.Adapter<MediaPlayerPagerAdapter.MediaPlayerViewHolder>() {

    inner class MediaPlayerViewHolder(mediaPlayerView: QsOpMediaPlayerView) :
        RecyclerView.ViewHolder(mediaPlayerView) {

        val mediaPlayerView: QsOpMediaPlayerView
            get() = itemView as QsOpMediaPlayerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaPlayerViewHolder {
        return MediaPlayerViewHolder(QsOpMediaPlayerView(context))
    }

    override fun onBindViewHolder(holder: MediaPlayerViewHolder, position: Int) {
        val (packageName, _) = mediaPlayerViews[position]

        if (packageName != null) {
            holder.itemView.setOnClickListener {
                Toast.makeText(context, packageName, Toast.LENGTH_SHORT).show()
            }
            val packageManager: PackageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val appName = packageManager.getApplicationLabel(appInfo).toString().trim()
            holder.mediaPlayerView.setMediaTitle(appName)
        } else {
            holder.mediaPlayerView.resetMediaAppIcon()
        }
    }

    override fun getItemCount(): Int = mediaPlayerViews.size

    fun addMediaPlayerView(packageName: String, view: QsOpMediaPlayerView) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) return

        if (mediaPlayerViews.size == 1 && mediaPlayerViews[0].first == null) {
            mediaPlayerViews.removeAt(0)
            notifyItemRemoved(0)
        }

        mediaPlayerViews.add(0, packageName to view)
        notifyItemInserted(0)
    }

    fun removeMediaPlayerView(packageName: String) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            mediaPlayerViews.removeAt(position)
            notifyItemRemoved(position)

            if (mediaPlayerViews.isEmpty()) {
                mediaPlayerViews.add(null to QsOpMediaPlayerView(context))
                notifyItemInserted(0)
            }
        }
    }

    fun isMediaPlayerAvailable(packageName: String): Boolean {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        return position != -1
    }

    fun getMediaPlayerBackground(packageName: String): ImageView? {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            return mediaPlayerViews[position].second.mediaPlayerBackground
        }
        return null
    }

    fun getMediaPlayerBackgroundDrawable(packageName: String): Drawable? {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            return mediaPlayerViews[position].second.mediaPlayerBackground.drawable
        }
        return null
    }

    fun setMediaPlayerBackground(packageName: String, drawable: Drawable?) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            mediaPlayerView.mediaPlayerBackground.apply {
                setImageDrawable(drawable)
                scaleType = ImageView.ScaleType.CENTER_CROP
                clipToOutline = true
            }
            notifyItemChanged(position)
        }
    }

    fun setMediaAppIcon(packageName: String, icon: Icon) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            if (mediaPlayerView.mediaAppIcon.drawable != icon.loadDrawable(context)) {
                mediaPlayerView.mediaAppIcon.setImageIcon(icon)
                notifyItemChanged(position)
            }
        }
    }

    fun resetMediaAppIcon(packageName: String) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            if (mediaPlayerView.mediaAppIcon.drawable != mediaPlayerView.mediaAppIconDrawable) {
                mediaPlayerView.mediaAppIcon.setImageDrawable(mediaPlayerView.mediaAppIconDrawable)
                notifyItemChanged(position)
            }
        }
    }

    fun setMediaAppIconDrawable(packageName: String, drawable: Drawable) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            if (mediaPlayerView.mediaAppIcon.drawable != drawable) {
                mediaPlayerView.mediaAppIcon.setImageDrawable(drawable)
                notifyItemChanged(position)
            }
        }
    }

    fun setMediaAppIconBitmap(packageName: String, bitmap: Bitmap) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            if (mediaPlayerView.mediaAppIcon.drawable !=
                BitmapDrawable(context.resources, bitmap)
            ) {
                mediaPlayerView.mediaAppIcon.setImageBitmap(bitmap)
                notifyItemChanged(position)
            }
        }
    }

    fun setMediaAppIconColor(packageName: String, backgroundColor: Int, iconColor: Int) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            mediaPlayerView.mediaAppIcon.backgroundTintList =
                ColorStateList.valueOf(backgroundColor)
            mediaPlayerView.mediaAppIcon.imageTintList = ColorStateList.valueOf(iconColor)
            notifyItemChanged(position)
        }
    }

    fun resetMediaAppIconColor(packageName: String, backgroundColor: Int) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            mediaPlayerView.mediaAppIcon.backgroundTintList =
                ColorStateList.valueOf(backgroundColor)
            mediaPlayerView.mediaAppIcon.imageTintList = null
            notifyItemChanged(position)
        }
    }

    fun setMediaTitle(packageName: String, title: String) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            if (mediaPlayerView.mediaPlayerTitle.text != title) {
                mediaPlayerView.mediaPlayerTitle.text = title
                notifyItemChanged(position)
            }
        }
    }

    fun setMediaArtist(packageName: String, artist: String?) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            if (mediaPlayerView.mediaPlayerSubtitle.text != artist) {
                mediaPlayerView.mediaPlayerSubtitle.text = artist
                notifyItemChanged(position)
            }

            if (artist.isNullOrEmpty()) {
                if (mediaPlayerView.mediaPlayerSubtitle.visibility != View.GONE) {
                    mediaPlayerView.mediaPlayerSubtitle.visibility = View.GONE
                    notifyItemChanged(position)
                }
            } else {
                if (mediaPlayerView.mediaPlayerSubtitle.visibility != View.VISIBLE) {
                    mediaPlayerView.mediaPlayerSubtitle.visibility = View.VISIBLE
                    notifyItemChanged(position)
                }
            }
        }
    }

    fun setMediaPlayingIcon(packageName: String, isPlaying: Boolean) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            if (isPlaying) {
                if (mediaPlayerView.mediaPlayerPlayPauseBtn.drawable != mediaPlayerView.mediaPauseIconDrawable) {
                    mediaPlayerView.mediaPlayerPlayPauseBtn.setImageDrawable(
                        mediaPlayerView.mediaPauseIconDrawable
                    )
                    notifyItemChanged(position)
                }
            } else {
                if (mediaPlayerView.mediaPlayerPlayPauseBtn.drawable != mediaPlayerView.mediaPlayIconDrawable) {
                    mediaPlayerView.mediaPlayerPlayPauseBtn.setImageDrawable(
                        mediaPlayerView.mediaPlayIconDrawable
                    )
                    notifyItemChanged(position)
                }
            }
        }
    }

    fun setMediaPlayerItemsColor(packageName: String, itemColor: Int?) {
        if (itemColor == null) return

        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            mediaPlayerViews[position].second.apply {
                mediaOutputSwitcher.setColorFilter(itemColor)
                mediaPlayerPrevBtn.setColorFilter(itemColor)
                mediaPlayerNextBtn.setColorFilter(itemColor)
                mediaPlayerPlayPauseBtn.setColorFilter(itemColor)
                mediaPlayerTitle.setTextColor(itemColor)
                mediaPlayerSubtitle.setTextColor(itemColor)
            }
            notifyItemChanged(position)
        }
    }

    fun setOnClickListeners(packageName: String, onClickListener: View.OnClickListener) {
        val position = mediaPlayerViews.indexOfFirst { it.first == packageName }
        if (position != -1) {
            val mediaPlayerView = mediaPlayerViews[position].second
            mediaPlayerView.mediaPlayerBackground.setOnClickListener(onClickListener)
            mediaPlayerView.mediaOutputSwitcher.setOnClickListener(onClickListener)
            mediaPlayerView.mediaPlayerPrevBtn.setOnClickListener(onClickListener)
            mediaPlayerView.mediaPlayerNextBtn.setOnClickListener(onClickListener)
            mediaPlayerView.mediaPlayerPlayPauseBtn.setOnClickListener(onClickListener)
            notifyItemChanged(position)
        }
    }
}