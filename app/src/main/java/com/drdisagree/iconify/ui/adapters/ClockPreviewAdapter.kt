package com.drdisagree.iconify.ui.adapters

import android.annotation.SuppressLint
import android.app.WallpaperManager
import android.content.Context
import android.graphics.Bitmap
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.LSCLOCK_SWITCH
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.ui.models.ClockModel
import com.drdisagree.iconify.ui.utils.ViewBindingHelpers.setBitmapWithAnimation
import com.drdisagree.iconify.utils.WallpaperUtils
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Suppress("deprecation")
class ClockPreviewAdapter(
    private val context: Context,
    private val itemList: ArrayList<ClockModel>,
    prefSwitch: String?,
    prefStyle: String,
    private val mOnStyleSelected: OnStyleSelected? = null
) : RecyclerView.Adapter<ClockPreviewAdapter.ViewHolder>() {

    private val prefStyle: String
    private var linearLayoutManager: LinearLayoutManager? = null

    init {
        Companion.prefSwitch = prefSwitch
        this.prefStyle = prefStyle
        loadWallpaper(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(
            if (prefSwitch == LSCLOCK_SWITCH) {
                R.layout.view_clock_preview_lockscreen
            } else {
                R.layout.view_clock_preview_header
            },
            parent,
            false
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = itemList[position]
        holder.bind(model, position)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val container: LinearLayout = itemView.findViewById(R.id.clock_preview_child)
        private val title: TextView = itemView.findViewById(R.id.clock_title)
        private val clockContainer: LinearLayout = itemView.findViewById(R.id.clock_view_container)
        val checkIcon: ImageView = itemView.findViewById(R.id.icon_selected)
        val button: MaterialButton = itemView.findViewById(R.id.btn_select_style)
        private val wallpaperView: ImageView = itemView.findViewById(R.id.wallpaper_view)

        fun bind(model: ClockModel, position: Int) {
            title.text = model.title

            button.setOnClickListener {
                RPrefs.putInt(prefStyle, position)
                mOnStyleSelected?.onStyleSelected(position)
                refreshLayout(this)
            }

            clockContainer.removeAllViews()

            val viewStub = ViewStub(context)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            params.gravity = Gravity.CENTER_VERTICAL
            viewStub.setLayoutParams(params)
            viewStub.layoutResource = model.layout

            clockContainer.addView(viewStub)

            if (viewStub.parent != null) {
                viewStub.inflate()
            }

            val isSelected = adapterPosition == getBindingAdapterPosition()
            button.setEnabled(!isSelected)
            checkIcon.setVisibility(if (isSelected) View.VISIBLE else View.GONE)

            if (wallpaperBitmap != null) {
                setBitmapWithAnimation(wallpaperView, wallpaperBitmap)
            }
        }
    }

    private fun refreshLayout(holder: ViewHolder) {
        val firstVisible = linearLayoutManager!!.findFirstVisibleItemPosition() - 1
        val lastVisible = linearLayoutManager!!.findLastVisibleItemPosition() + 1

        for (i in firstVisible..lastVisible) {
            val view = linearLayoutManager!!.findViewByPosition(i) ?: continue
            val child = view.findViewById<LinearLayout>(R.id.clock_preview_child) ?: continue
            val isSelected = view === holder.container

            child.findViewById<View>(R.id.btn_select_style).setEnabled(!isSelected)
            child.findViewById<View>(R.id.icon_selected).visibility =
                if (!isSelected) View.GONE else View.VISIBLE
        }
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)

        if (RPrefs.getInt(prefStyle, 0) != holder.getBindingAdapterPosition()) {
            holder.checkIcon.setVisibility(View.GONE)
            holder.button.setEnabled(true)
        } else {
            holder.checkIcon.setVisibility(View.VISIBLE)
            holder.button.setEnabled(false)
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
    }

    @SuppressLint("NotifyDataSetChanged")
    fun loadWallpaper(adapter: ClockPreviewAdapter) {
        CoroutineScope(Dispatchers.Main).launch {
            val bitmap = withContext(Dispatchers.IO) {
                WallpaperUtils.getCompressedWallpaper(
                    adapter.context,
                    80,
                    if (prefSwitch == LSCLOCK_SWITCH) {
                        WallpaperManager.FLAG_LOCK
                    } else {
                        WallpaperManager.FLAG_SYSTEM
                    }
                )
            }

            if (bitmap != null && prefSwitch == LSCLOCK_SWITCH) {
                wallpaperBitmap = bitmap
                adapter.notifyDataSetChanged()
            }
        }
    }

    interface OnStyleSelected {
        /**
         * Interface for style selection
         * @param position The position of the selected style,
         * in our case is the num of the layout
         */
        fun onStyleSelected(position: Int)
    }

    companion object {
        private var prefSwitch: String? = null
        private var wallpaperBitmap: Bitmap? = null
    }
}
