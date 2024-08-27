package com.drdisagree.iconify.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.drdisagree.iconify.Iconify.Companion.appContext
import com.drdisagree.iconify.Iconify.Companion.appContextLocale
import com.drdisagree.iconify.R
import com.drdisagree.iconify.common.Preferences.SELECTED_TOAST_FRAME
import com.drdisagree.iconify.config.RPrefs
import com.drdisagree.iconify.databinding.ViewToastFrameBinding
import com.drdisagree.iconify.ui.models.ToastModel

class ToastAdapter (
    var context: Context,
    private var itemList: ArrayList<ToastModel>,
    private var toastClick: OnToastClick
) : RecyclerView.Adapter<ToastAdapter.ViewHolder>() {

    private var selected = RPrefs.getInt(SELECTED_TOAST_FRAME, -1)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding: ViewToastFrameBinding =
            ViewToastFrameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        if (selected != -1) {
            itemList[selected].selected = true
        } else {
            itemList[0].selected = true
        }
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val model = itemList[holder.bindingAdapterPosition]
        holder.binding.styleName.text = model.title
        holder.binding.toastContainer.background = ContextCompat.getDrawable(appContext, model.style)
        if (model.selected) {
            holder.binding.styleName.setTextColor(
                appContextLocale.resources.getColor(
                    R.color.colorAccent,
                    appContext.theme
                )
            )
        } else {
            holder.binding.styleName.setTextColor(
                appContextLocale.resources.getColor(
                    R.color.textColorSecondary,
                    appContext.theme
                )
            )
        }
        holder.binding.listItemToast.setOnClickListener {
            toastClick.onToastClick(holder.bindingAdapterPosition, model)
        }
    }

    fun notifyChange() {
        refresh(false)
        selected = RPrefs.getInt(SELECTED_TOAST_FRAME, -1)
        refresh(true)
    }

    private fun refresh(select: Boolean) {
        if (selected != -1) {
            itemList[selected].selected = select
            notifyItemChanged(selected)
        } else {
            itemList[0].selected = select
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    class ViewHolder(val binding: ViewToastFrameBinding) : RecyclerView.ViewHolder(binding.getRoot()) {
    }

    /**
     * Interface for the click on the item
     */
    interface OnToastClick {
        fun onToastClick(position: Int, item: ToastModel)
    }

}