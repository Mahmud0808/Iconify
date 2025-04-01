package com.drdisagree.iconify.ui.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.data.config.RPrefs
import com.drdisagree.iconify.databinding.PreferenceListItemBinding
import com.drdisagree.iconify.databinding.ViewListIconItemBinding

class ListPreferenceAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private var mResImages: List<String> = ArrayList()
    private val mEntries: Array<CharSequence>
    private val mEntryValues: Array<CharSequence>
    private var mEntryIcons: IntArray?
    private var mEntryDrawables: Array<Drawable>?
    private val mKey: String
    private val mHasImage: Boolean
    private var onItemClickListener: OnItemClickListener
    private var mValue: String? = null
    var type: Int = DEFAULT_TYPE
    private var prevPos: Int = -1

    constructor(
        entries: Array<CharSequence>,
        entryValues: Array<CharSequence>,
        entryIcons: IntArray?,
        key: String,
        hasImage: Boolean,
        onItemClickListener: OnItemClickListener
    ) {
        this.mEntries = entries
        this.mEntryValues = entryValues
        this.mEntryIcons = entryIcons
        this.mEntryDrawables = null
        this.mKey = key
        this.mHasImage = hasImage
        this.onItemClickListener = onItemClickListener
        this.type = DEFAULT_TYPE
    }

    constructor(
        entries: Array<CharSequence>,
        entryValues: Array<CharSequence>,
        entryDrawables: Array<Drawable>,
        key: String,
        hasImage: Boolean,
        onItemClickListener: OnItemClickListener
    ) {
        this.mEntries = entries
        this.mEntryValues = entryValues
        this.mEntryDrawables = entryDrawables
        this.mEntryIcons = null
        this.mKey = key
        this.mHasImage = hasImage
        this.onItemClickListener = onItemClickListener
        this.type = DEFAULT_TYPE
    }

    fun setListener(onItemClickListener: OnItemClickListener) {
        this.onItemClickListener = onItemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        mValue = RPrefs.getString(mKey, "")
        val binding = PreferenceListItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        val batteryIconOptionsBinding = ViewListIconItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return if (type == TYPE_BATTERY_ICONS) {
            BatteryIconsViewHolder(batteryIconOptionsBinding)
        } else {
            ViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (type == TYPE_BATTERY_ICONS) {
            (holder as BatteryIconsViewHolder).binding.typeTitle.text = mEntries[holder.bindingAdapterPosition]

            if (mHasImage) {
                mEntryDrawables?.let {
                    holder.binding.batteryIcon.setImageDrawable(
                        it[holder.bindingAdapterPosition]
                    )
                }
            } else holder.binding.batteryIcon.setVisibility(View.GONE)

            if (mEntryValues[holder.bindingAdapterPosition].toString().contentEquals(mValue)) {
                prevPos = holder.bindingAdapterPosition
                holder.binding.rootLayout.strokeColor = appContext.getColor(R.color.colorAccent)
            } else {
                holder.binding.rootLayout.strokeColor = Color.TRANSPARENT
            }

            holder.binding.rootLayout.setOnClickListener { v ->
                onItemClickListener.onItemClick(v, holder.bindingAdapterPosition)
                mValue = mEntryValues[holder.bindingAdapterPosition].toString()
                notifyItemChanged(prevPos)
                notifyItemChanged(holder.bindingAdapterPosition)
            }
        } else {
            (holder as ViewHolder).binding.text.text = mEntries[holder.bindingAdapterPosition]
            if (mHasImage) {
                if (mEntryIcons != null && mEntryIcons!!.isNotEmpty()) holder.binding.image.setImageDrawable(
                    ContextCompat.getDrawable(
                        holder.binding.getRoot().context,
                        mEntryIcons!![holder.bindingAdapterPosition]
                    )
                )
                else if (!mEntryDrawables.isNullOrEmpty()) holder.binding.image.setImageDrawable(
                    mEntryDrawables!![holder.bindingAdapterPosition]
                )
            } else holder.binding.image.setVisibility(View.GONE)

            if (mEntryValues[holder.bindingAdapterPosition].toString().contentEquals(mValue)) {
                prevPos = holder.bindingAdapterPosition
                holder.binding.rootLayout.strokeColor =
                    appContext.getColor(R.color.colorAccent)
            } else {
                holder.binding.rootLayout.strokeColor = Color.TRANSPARENT
            }

            holder.binding.rootLayout.setOnClickListener { v ->
                onItemClickListener.onItemClick(v, holder.bindingAdapterPosition)
                mValue = mEntryValues[holder.bindingAdapterPosition].toString()
                notifyItemChanged(prevPos)
                notifyItemChanged(holder.bindingAdapterPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return mEntries.size
    }

    fun setImages(images: List<String>) {
        mResImages = images
    }

    fun updateDrawables(newDrawables: Array<Drawable>) {
        mEntryDrawables = newDrawables
    }

    fun updateIcons(newIcons: IntArray) {
        mEntryIcons = newIcons
    }

    class ViewHolder internal constructor(val binding: PreferenceListItemBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
    }

    class BatteryIconsViewHolder internal constructor(val binding: ViewListIconItemBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {
    }

    /**
     * Interface for the click on the item
     */
    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int)
    }

    companion object {
        const val DEFAULT_TYPE: Int = 0
        const val TYPE_BATTERY_ICONS: Int = 2
    }
}