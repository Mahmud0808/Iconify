package com.drdisagree.iconify.ui.adapters

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.R
import com.drdisagree.iconify.databinding.ViewListIconItemBinding
import com.drdisagree.iconify.databinding.ViewListOptionWeatherIconsBinding
import com.drdisagree.iconify.ui.dialogs.LoadingDialog
import com.drdisagree.iconify.ui.models.IconPackModel

class IconsAdapter(
    private val mEntries: Array<CharSequence>,
    private val mEntryValues: Array<CharSequence>,
    private var mValue: String,
    private val mAdapterType: Int,
    private val onItemClickListener: OnItemClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    companion object {
        const val ICONS_ADAPTER = 0
        const val WEATHER_ICONS_ADAPTER = 1
    }

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
        if (mAdapterType == ICONS_ADAPTER) {
            return IconsViewHolder(
                ViewListIconItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else if (mAdapterType == WEATHER_ICONS_ADAPTER) {
            return WeatherIconsViewHolder(
                ViewListOptionWeatherIconsBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else {
            throw IllegalStateException(javaClass.getSimpleName() + " - No adapter type provided")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (mAdapterType) {
            ICONS_ADAPTER -> {
                bindIconsViewHolder(holder as IconsViewHolder, position)
            }
            WEATHER_ICONS_ADAPTER -> {
                bindWeatherIconsViewHolder(holder as WeatherIconsViewHolder, position)
            }
        }
    }

    private fun bindIconsViewHolder(holder: IconsViewHolder, position: Int) {
        holder.binding.typeTitle.text = mEntries[position]

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
            holder.binding.rootLayout.strokeColor = appContext.getColor(R.color.colorAccent)
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

    private fun bindWeatherIconsViewHolder(holder: WeatherIconsViewHolder, position: Int) {
        holder.binding.text.text = mEntries[position]

        if (mEntryDrawables != null) {
            holder.binding.image.setImageDrawable(mEntryDrawables!![position])
        } else if (mEntryResIds != null) {
            holder.binding.image.setImageDrawable(
                ContextCompat.getDrawable(
                    holder.binding.getRoot().context,
                    mEntryResIds!![position]
                )
            )
        } else {
            throw IllegalStateException(javaClass.getSimpleName() + " - No icons provided")
        }

        if (mEntryValues[position].toString().contentEquals(mValue)) {
            holder.binding.rootLayout.strokeColor = appContext.getColor(R.color.colorAccent)
        } else {
            holder.binding.rootLayout.strokeColor = Color.TRANSPARENT
        }

        holder.binding.rootLayout.setOnClickListener { v: View ->
            val previousPosition = mEntryValues.indexOf(mValue)
            mValue = mEntryValues[position].toString()

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

    class WeatherIconsViewHolder internal constructor(val binding: ViewListOptionWeatherIconsBinding) :
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