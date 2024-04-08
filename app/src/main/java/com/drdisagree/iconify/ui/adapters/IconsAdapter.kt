package com.drdisagree.iconify.ui.adapters

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.Iconify
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.ViewListIconItemBinding

class IconsAdapter(
    private val mEntries: Array<CharSequence>,
    private val mEntryValues: Array<CharSequence>,
    private var mValue: String,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mEntryDrawables: Array<Drawable>? = null
    private var mEntryResIds: IntArray? = null

    /**
     * Sets drawables for the icons
     * @param drawables The drawables of the icons
     */
    fun setDrawables(drawables: Array<Drawable>?) {
        mEntryDrawables = drawables
    }

    /**
     * Set icons from Resources
     * This should be used when the icons are from resources
     * @param resIds The resource ids of the icons
     */
    fun setResIds(resIds: IntArray?) {
        mEntryResIds = resIds
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return IconsViewHolder(
            ViewListIconItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as IconsViewHolder).binding.typeTitle.text = mEntries[position]

        if (mEntryDrawables != null) {
            holder.binding.batteryIcon.setImageDrawable(mEntryDrawables!![position])
        } else if (mEntryResIds != null) {
            holder.binding.batteryIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.getRoot().context,
                    mEntryResIds!![position]
                )
            )
        } else {
            throw IllegalStateException(javaClass.getSimpleName() + " - No icons provided")
        }

        if (mEntryValues[position].toString().contentEquals(mValue)) {
            holder.binding.rootLayout.strokeColor =
                Iconify.getAppContext().getColor(R.color.colorAccent)
        } else {
            holder.binding.rootLayout.strokeColor = Color.TRANSPARENT
        }

        holder.binding.rootLayout.setOnClickListener { v: View ->
            val previousPosition = mValue.toInt()
            mValue = position.toString()

            notifyItemChanged(previousPosition)
            notifyItemChanged(position)

            onItemClickListener.onItemClick(v, position)
        }
    }

    override fun getItemCount(): Int {
        return mEntries.size
    }

    fun setCurrentValue(currentValue: String) {
        mValue = currentValue
    }

    class IconsViewHolder internal constructor(val binding: ViewListIconItemBinding) :
        RecyclerView.ViewHolder(
            binding.getRoot()
        )

    /**
     * Interface for the click on the item
     */
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }
}