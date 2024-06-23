package com.drdisagree.iconify.ui.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.R
import com.drdisagree.iconify.ui.models.InfoModel

class InfoAdapter(
    var context: Context,
    private var itemList: ArrayList<InfoModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                HeaderViewHolder(
                    LayoutInflater.from(
                        context
                    ).inflate(R.layout.view_list_info_header, parent, false)
                )
            }

            TYPE_ITEM -> {
                ItemViewHolder(
                    LayoutInflater.from(
                        context
                    ).inflate(R.layout.view_list_info_item, parent, false)
                )
            }

            else -> throw RuntimeException("There is no type that matches the type $viewType. + make sure you are using types correctly.")
        }
    }

    @SuppressLint("DiscouragedApi")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is HeaderViewHolder) {
            holder.header.text = itemList[position].title

            if (itemList[position].title == "") {
                holder.itemView.visibility = View.GONE
                holder.itemView.setLayoutParams(RecyclerView.LayoutParams(0, 0))
            }
        } else if (holder is ItemViewHolder) {
            holder.icon.setImageResource(itemList[position].icon)
            holder.title.text = itemList[position].title
            holder.desc.text = itemList[position].desc
            holder.container.setOnClickListener(itemList[position].onClickListener)

            val drawableName = context.resources.getResourceEntryName(itemList[position].icon)
            val typedValue = TypedValue()

            context.theme.resolveAttribute(
                com.google.android.material.R.attr.colorOnSurface,
                typedValue,
                true
            )

            val colorOnSurface = typedValue.data
            if (drawableName.contains("flag_")) {
                holder.icon.clearColorFilter()
            } else {
                holder.icon.colorFilter = BlendModeColorFilter(colorOnSurface, BlendMode.SRC_IN)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    internal class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var header: TextView = itemView.findViewById(R.id.list_info_header)
    }

    internal class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var icon: ImageView = itemView.findViewById(R.id.icon)
        var title: TextView = itemView.findViewById(R.id.title)
        var desc: TextView = itemView.findViewById(R.id.desc)
        var container: RelativeLayout = itemView.findViewById(R.id.list_info_item)
    }

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }
}